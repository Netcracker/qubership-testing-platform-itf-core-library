# Velocity directives reference

This library ships a set of custom [Apache Velocity](https://velocity.apache.org/) directives under
`org.qubership.automation.itf.core.template.velocity.directives`. They implement `Directive` directly and are all
`LINE`-type directives, so each one is invoked on its own line as `#directiveName(arg1, arg2, ...)`, with no matching
`#end`. Velocity evaluates every argument as a normal expression before the directive runs, so an argument can be a
`$variable`, a quoted string literal, or any other expression Velocity accepts.

This library does not register the directives with a `VelocityEngine` itself; a consuming service registers the
classes it needs (typically through Velocity's `userdirective` configuration property) before using them in a
template.

The directives are not named consistently: some use `snake_case` (`add_date`, `hashsum`), others use `camelCase`
(`decodeUrl`, `generateUUID`). There is no pattern to guess from the class name alone, so the invocation name is
called out explicitly for each directive below.

Error handling is not consistent across directives either. Some throw `IllegalArgumentException`, which fails the
whole template render; others catch the failure, log it, and write an error marker or nothing into the rendered
output instead. Each entry below states which one applies.

## Directives at a glance

| Invocation | Class | Purpose |
| --- | --- | --- |
| [`#add_date`](#add_date) | `AddDate` | Adds day/hour/minute offsets to a date string |
| [`#hashsum`](#hashsum) | `ComputeHash` | Computes a hash, HMAC, AES-256 encryption, or a JWS signature |
| [`#decode_base64`](#decode_base64) | `DecodeBase64` | Base64-decodes one or more values |
| [`#decode_hashsum`](#decode_hashsum) | `DecodeHashsum` | Reads the payload out of an RS256/RS512 JWS, without verifying it |
| [`#decode_saml`](#decode_saml) | `DecodeSaml` | Decodes a SAML HTTP-Redirect-binding parameter |
| [`#decodeUrl`](#decodeurl) | `DecodeUrl` | URL-decodes a value with an explicit encoding |
| [`#encode_base64`](#encode_base64) | `EncodeBase64` | Base64-encodes one or more values |
| [`#encode_saml`](#encode_saml) | `EncodeSaml` | Encodes a value for a SAML HTTP-Redirect-binding parameter |
| [`#encodeUrl`](#encodeurl) | `EncodeUrl` | URL-encodes a value with an explicit encoding |
| [`#escape_xml`](#escape_xml) | `EscapeXml` | XML 1.0-escapes one or more values |
| [`#generateUUID`](#generateuuid) | `GenerateUuid` | Renders a random UUID |
| [`#json_path`](#json_path) | `JsonPathDirective` | Evaluates one or more JsonPath expressions against a JSON string |
| [`#toJson`](#tojson) | `ToJsonString` | Serializes an object to a JSON string |
| [`#transliterate`](#transliterate) | `Transliterate` | Applies an ICU4J script transliteration |

## `#add_date`

```text
#add_date($date)
#add_date($date, $offset1)
#add_date($date, $offset1, $offset2, ...)
```

Adds one or more day/hour/minute offsets to `$date` and renders the result.

### Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `$date` | Yes | A date string in `yyyy-MM-dd HH:mm:ss` (exactly 19 characters) or `yyyy-MM-dd HH:mm:ss.SSS'Z'`. A `T` in place of the space (`2019-03-23T21:59:32`) is accepted the same way. |
| `$offsetN` | No, repeatable | A signed integer followed by a unit, with an optional space between them, matched case-insensitively: `d`/`day`/`days`, `h`/`hour`/`hours`, or `m`/`min`/`mins`/`minute`/`minutes`. Each offset is applied to the result of the previous one, in argument order. A blank offset leaves the date unchanged for that step. |

The rendered date always uses `T` as the date/time separator, regardless of which separator `$date` used.

### Errors

An offset that isn't a signed integer followed by one of the recognized units throws `IllegalArgumentException`
naming the accepted units. If `$date` itself cannot be parsed, the failure is logged as an error and the directive
renders nothing — it does not throw.

### Example

```text
#add_date($now, '5d')
#add_date($now, '-1 hour', '30 min')
```

## `#hashsum`

```text
#hashsum($content)
#hashsum($algorithm, $content)
#hashsum($algorithm, $content, $encoding)
#hashsum($algorithm, $content, $encoding, $key)
#hashsum($algorithm, $content, $encoding, $key, $encodeAsBase64)
```

Computes a hash, an HMAC, an AES-256 encryption, or a JWS signature over `$content`, depending on `$algorithm`.

### Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `$algorithm` | No | One of `SHA-1` (default when omitted), `SHA-256`, `SHA-384`, `SHA-512`, `MD2`, `MD5`, `CRC-32`, `AES-256`, `RS256`, `RS512`, or `HMAC-xxx` (for example `HMAC-SHA256`). Matched case-insensitively. |
| `$content` | Yes | The text to hash, encrypt, or sign. |
| `$encoding` | No | The charset `$content` is encoded with before processing. Defaults to `UTF-8`. |
| `$key` | Required for `AES-256`, `RS256`, `RS512`, and `HMAC-xxx` | See below; the shape depends on `$algorithm`. |
| `$encodeAsBase64` | No | Only affects `HMAC-xxx`. `"true"` renders the digest as Base64; anything else (including omitting it) renders lowercase hex. |

For `AES-256`, `$key` must decode to exactly 32 bytes in `$encoding`; the initialization vector is a fixed 16
zero bytes, not caller-supplied. For `RS256`/`RS512`, `$key` is a Base64-encoded PKCS8 RSA private key. For
`HMAC-xxx`, `$key` is used directly as the HMAC key.

A blank `$content` always renders an empty string, regardless of `$algorithm`.

### Errors

Zero arguments, or more than five, are logged as a warning and the directive renders nothing. An `$algorithm` that
is not one of the recognized values (and does not start with `HMAC`) throws `IllegalArgumentException`. A wrong-size
AES-256 key, an unsupported encoding, or a JWS/HMAC failure is also wrapped and thrown as
`IllegalArgumentException`.

### Example

```text
#hashsum($password)
#hashsum('SHA-256', $password)
#hashsum('HMAC-SHA256', $payload, 'UTF-8', $secretKey, 'true')
```

## `#decode_base64`

```text
#decode_base64($value)
#decode_base64($value1, $value2, ...)
```

Base64-decodes (UTF-8) each argument independently and appends the results in order, with no separator. This is not
a `content, encoding` pair like [`#decodeUrl`](#decodeurl): every argument is decoded the same way. A blank
argument contributes an empty string; no arguments render nothing.

### Example

```text
#decode_base64($token)
#decode_base64($tokenA, $tokenB)
```

## `#decode_hashsum`

```text
#decode_hashsum($algorithm, $content)
```

Reads the payload out of a compact JWS string in `$content` and renders it as text. The signature is **not**
verified — there is no key parameter.

### Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `$algorithm` | Yes | `RS256` or `RS512`, matched case-insensitively. No other value is accepted. |
| `$content` | Yes | A compact JWS string (`header.payload.signature`). |

### Errors

Any argument count other than 2, a `null` argument, an unrecognized `$algorithm`, or a `$content` that isn't a
parseable JWS all throw `IllegalArgumentException`.

### Example

```text
#decode_hashsum('RS256', $signedToken)
```

## `#decode_saml`

```text
#decode_saml($content)
#decode_saml($content, $encoding)
```

Decodes `$content` the way the SAML HTTP-Redirect binding encodes a `SAMLRequest`/`SAMLResponse` query parameter:
URL-decode, then Base64-decode, then raw-inflate (no zlib header). `$encoding` defaults to `UTF-8`.

The inflated size is capped at 16 MiB by default, overridable with the JVM system property
`itf.decode_saml.max.decoded.size.bytes`. The cap exists because the compression ratio of the input is chosen by
whoever sent the original message; without it, decompressing a hostile message can exhaust the heap before this
directive gets a chance to check the result.

### Errors

An argument count other than 1 or 2 is logged as an error, and the directive writes the literal text
`#decode_saml:incorrect parameters` into the output — it does not throw. Exceeding the size cap throws
`IllegalArgumentException`. Any other I/O failure during decoding is wrapped and thrown as an unchecked
`RuntimeException`.

### Example

```text
#decode_saml($samlResponseParam)
```

## `#decodeUrl`

```text
#decodeUrl($content, $encoding)
```

URL-decodes `$content` using `$encoding` (any name from the
[IANA Charset Registry](https://www.iana.org/assignments/character-sets/character-sets.xhtml)). Both arguments are
required — there is no default encoding. When `$encoding` contains `jis` (case-insensitive, matching names such as
`Shift_JIS`), the value is decoded through an intermediate ISO-8859-1 round trip, which is the usual workaround for
the JDK's URL codec with Japanese encodings.

A blank `$content` renders an empty string.

### Errors

An argument count other than 2 is logged as an error, and the error message itself is written into the output. An
unsupported `$encoding` is logged as an error (only when the directive is running inside a real Velocity render)
and, again, the error message is written into the output. Neither case throws.

### Example

```text
#decodeUrl($queryParam, 'UTF-8')
```

## `#encode_base64`

```text
#encode_base64($value)
#encode_base64($value1, $value2, ...)
```

Base64-encodes (UTF-8) each argument independently and appends the results in order, with no separator — the
encoding counterpart of [`#decode_base64`](#decode_base64), with the same one-argument-at-a-time behavior. A blank
argument contributes an empty string; no arguments render nothing.

### Example

```text
#encode_base64($secret)
```

## `#encode_saml`

```text
#encode_saml($content)
#encode_saml($content, $encoding)
```

Encodes `$content` the way the SAML HTTP-Redirect binding encodes a `SAMLRequest`/`SAMLResponse` query parameter:
raw-deflate (no zlib header), then Base64-encode, then URL-encode. `$encoding` defaults to `UTF-8`. This is the
inverse of [`#decode_saml`](#decode_saml).

### Errors

An argument count other than 1 or 2 is logged as an error, and the directive writes the literal text
`#encode_saml:incorrect parameters` into the output — it does not throw. Any I/O failure during encoding is wrapped
and thrown as an unchecked `RuntimeException`.

### Example

```text
#encode_saml($samlRequestXml)
```

## `#encodeUrl`

```text
#encodeUrl($content, $encoding)
```

URL-encodes `$content` using `$encoding` (any name from the
[IANA Charset Registry](https://www.iana.org/assignments/character-sets/character-sets.xhtml)). Both arguments are
required. The `jis` special case and the error behavior are the same as [`#decodeUrl`](#decodeurl): a wrong
argument count or an unsupported encoding is logged and the message is written into the output, not thrown.

A blank `$content` renders an empty string.

### Example

```text
#encodeUrl($redirectTarget, 'UTF-8')
```

## `#escape_xml`

```text
#escape_xml($value)
#escape_xml($value1, $value2, ...)
```

XML 1.0-escapes (`&`, `<`, `>`, `'`, `"`) each argument and appends the results in order, with no separator, the
same one-argument-at-a-time shape as [`#decode_base64`](#decode_base64). A `null`-valued argument contributes an
empty string; a literally missing child node is logged as a warning and contributes nothing.

### Example

```text
#escape_xml($userInput)
```

## `#generateUUID`

```text
#generateUUID()
```

Renders a random UUID (`UUID.randomUUID()`) — a different one on every call. Takes no arguments; any that are
passed are ignored.

### Example

```text
#generateUUID()
```

## `#json_path`

```text
#json_path($json, $path1)
#json_path($json, $path1, $path2, ...)
```

Parses `$json` once, evaluates each `$pathN` as a [JsonPath](https://github.com/json-path/JsonPath) expression
against it, and appends the string form of each result in order, with no separator. At least one path is required.

### Errors

Fewer than 2 total arguments throws `IllegalArgumentException`. An invalid `$json` string or an invalid JsonPath
expression propagates as JsonPath's own runtime exception — this directive does not catch it.

### Example

```text
#json_path($responseBody, '$.data.id')
#json_path($responseBody, '$.data.id', '$.data.name')
```

## `#toJson`

```text
#toJson($obj)
#toJson($obj, $prettyPrint)
```

Serializes `$obj` to a JSON string. How depends on its runtime type:

| `$obj` is | Result |
| --- | --- |
| `null` | Nothing is rendered. |
| a `JsonContext` (this library's own type) | Its own `getJsonString()`; `$prettyPrint` is ignored. |
| a `Map` or a `List` | Serialized with a fresh Jackson `ObjectMapper`, pretty-printed if `$prettyPrint` is truthy. |
| anything else | Its `toString()`; `$prettyPrint` is ignored. |

`$prettyPrint` accepts an actual Velocity `Boolean` value, or is parsed leniently from its string form: only the
literal text `true` (case-insensitive) is truthy, and anything else — including a typo — is silently treated as
`false`.

### Errors

Calling `#toJson` with no arguments throws `IllegalArgumentException`.

### Example

```text
#toJson($responseMap)
#toJson($responseMap, true)
```

## `#transliterate`

```text
#transliterate($content, $fromTo)
```

Applies an [ICU4J](https://unicode-org.github.io/icu/userguide/transforms/general/) transliterator to `$content`.
`$fromTo` is a transliterator ID such as `Halfwidth-Fullwidth` or `Fullwidth-Halfwidth`; any ID ICU4J recognizes is
accepted.

### Errors

An argument count other than 2 is logged as an error and the directive renders nothing (it returns `false`, unlike
every other directive on this page, which return `true` regardless of outcome). An unrecognized `$fromTo` ID, or
any other transliteration failure, is caught, logged, and the literal text `#err` is written instead of throwing.

### Example

```text
#transliterate($fullWidthText, 'Fullwidth-Halfwidth')
```
