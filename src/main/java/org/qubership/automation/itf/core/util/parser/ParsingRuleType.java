/*
 *  Copyright 2024-2025 NetCracker Technology Corporation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.qubership.automation.itf.core.util.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Text;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.qubership.atp.common.utils.regex.TimeoutRegexCharSequence;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.model.jpa.message.parser.MessageParameter;
import org.qubership.automation.itf.core.model.jpa.message.parser.ParsingRule;

import com.google.gson.Gson;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum ParsingRuleType {

    REGEX {
        private static final int maxRegexTimeoutSeconds = 60; // Should be made configurable (on exec and stubs sides)

        @Override
        public String toString() {
            return "Regex";
        }

        @Override
        public MessageParameter parse(Message message, ParsingRule parsingRule) {
            MessageParameter.Builder builder = MessageParameter.build(parsingRule.getParamName(), parsingRule)
                    .multiple(parsingRule.getMultiple()).setAutosave(parsingRule.getAutosave());
            if (!StringUtils.isEmpty(message.getText())) {
                Pattern pattern = Pattern.compile(parsingRule.getParsedExpression());
                Matcher matcher = pattern.matcher(new TimeoutRegexCharSequence(message.getText(),
                        maxRegexTimeoutSeconds));
                while (matcher.find()) {
                    if (matcher.groupCount() > 0) {
                        builder.multipleValue(matcher.group(1));
                    } else {
                        builder.multipleValue(matcher.group());
                    }
                    if (!parsingRule.getMultiple()) {
                        break;
                    }
                }
            }
            return builder.get();
        }
    }, XPATH {
        private final transient ThreadLocal<XMLOutputter> xmlOutputter = ThreadLocal.withInitial(XMLOutputter::new);

        @Override
        public String toString() {
            return "Xpath";
        }

        @Override
        public MessageParameter parse(Message message, ParsingRule parsingRule) {
            MessageParameter.Builder builder = MessageParameter.build(parsingRule.getParamName(), parsingRule)
                    .multiple(parsingRule.getMultiple())
                    .setAutosave(parsingRule.getAutosave());
            Element element = (Element) message.getContent().get();
            /*
            List<Namespace> namespaces = new ArrayList<>();
            namespaces.add(Namespace.NO_NAMESPACE);
            if (!(element.getNamespace().getPrefix().equals(Namespace.NO_NAMESPACE.getPrefix())))
                namespaces.add(element.getNamespace());
            namespaces.addAll(element.getAdditionalNamespaces());
            */
            XPathExpression<Object> xpathExpr = XPathFactory.instance().compile(parsingRule.getParsedExpression());
            /*
            XPathExpression<Object> xpathExpr = XPathFactory.instance().compile(parsingRule.getExpression(),
                    Filters.fpassthrough(), null, namespaces);
            */
            List evaluate;
            try {
                evaluate = xpathExpr.evaluate(element);
                for (Object o : evaluate) {
                    if (o instanceof Element) {
                        xmlOutputter.get().setFormat(Format.getPrettyFormat());
                        builder.multipleValue(xmlOutputter.get().outputString((Element) o));
                    } else if (o instanceof Text) {
                        builder.multipleValue(((org.jdom2.Content) o).getValue());
                    } else if (o instanceof Attribute) {
                        builder.multipleValue(((Attribute) o).getValue());
                    } else {
                        builder.multipleValue(o.toString());
                    }
                    if (!parsingRule.getMultiple()) {
                        break;
                    }
                }
                return builder.get();
            } catch (IllegalArgumentException e) {
                //let's write more informed message, which of parsing rules is dead
                throw new IllegalArgumentException(String.format(
                        "Failed applying xpath. Probably xPaths is incorrect. ParsingRule '%s', at %s",
                        parsingRule.getParamName(), parsingRule.getParsingRulePath()), e);
            }
        }
    }, REGEX_URI {
        @Override
        public String toString() {
            return "Regex URI";
        }

        @Override
        public MessageParameter parse(Message message, ParsingRule parsingRule) {
            MessageParameter.Builder builder = MessageParameter.build(parsingRule.getParamName(), parsingRule)
                    .multiple(parsingRule.getMultiple()).setAutosave(parsingRule.getAutosave());
            if (message.getConnectionProperties().get("uriParams") != null) {
                Pattern pattern = Pattern.compile(parsingRule.getParsedExpression());
                Matcher matcher = pattern.matcher((String) message.getConnectionProperties().get("uriParams"));
                if (matcher.find()) {
                    buildResult(parsingRule, matcher, builder);
                }
            }

            return builder.get();
        }
    }, JSON_PATH {
        @Override
        public String toString() {
            return "JSON Path";
        }

        @Override
        public MessageParameter parse(Message message, ParsingRule parsingRule) {
            MessageParameter.Builder builder = MessageParameter.build(parsingRule.getParamName(), parsingRule)
                    .multiple(parsingRule.getMultiple()).setAutosave(parsingRule.getAutosave());
            try {
                Object document = message.getContent().get();
                Object parsingResult = JsonPath.read(document, parsingRule.getParsedExpression());
                if (parsingRule.getMultiple()) {
                    if (parsingResult instanceof List) {
                        ((List<Object>) parsingResult).forEach(entry -> builder.multipleValue(jsonEntry2String(entry)));
                    } else {
                        builder.multipleValue(jsonEntry2String(parsingResult));
                    }
                } else {
                    String result;
                    if (parsingResult instanceof List) {
                        List<Object> listResult = (List<Object>) parsingResult;
                        if (listResult.isEmpty()) {
                            result = "";
                        } else {
                            // It's strange but parsingResult can be list of size=1
                            // but .get(0) == null (All elements are null)
                            Object obj = listResult.get(0);
                            result = Objects.isNull(obj) ? "" : jsonEntry2String(obj);
                        }
                    } else {
                        result = Objects.isNull(parsingResult) ? "" : jsonEntry2String(parsingResult);
                    }
                    builder.multipleValue(result);
                }
                return builder.get();
            } catch (InvalidJsonException ex) {
                // Currently each message is applicable for JsonPath (parsingRule.applicable(message)
                // always returns true for JsonPath rules)
                // So, we should silently ignore this exception or just inform about it. Not stop parsing.
                // Silently ignore it
                return builder.get();
            } catch (PathNotFoundException ex) {
                // Json message doesn't match this JsonPath expression.
                // It's not error really, so simply return empty parameter.
                return builder.get();
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format(
                        "Failed parsing JsonPath. Probably JsonPath expression is incorrect. ParsingRule '%s', at %s",
                        parsingRule.getParamName(), parsingRule.getParsingRulePath()), e);
            }
        }

        private String jsonEntry2String(Object entry) {
            if (entry instanceof List || entry instanceof Map) {
                return new Gson().toJson(entry);
            }
            return String.valueOf(entry);
        }
    }, REGEX_HEADER {
        @Override
        public String toString() {
            return "Regex Header";
        }

        @Override
        public MessageParameter parse(Message message, ParsingRule parsingRule) {
            MessageParameter.Builder builder = MessageParameter.build(parsingRule.getParamName(),
                    parsingRule).multiple(parsingRule.getMultiple()).setAutosave(parsingRule.getAutosave());
            Map<String, Object> headers = message.getHeaders();
            if (Objects.isNull(headers)) {
                return builder.get();
            }

            String parsedExpression = parsingRule.getParsedExpression();
            String requiredHeaderName;

            if (!parsedExpression.contains("/")) {
                requiredHeaderName = parsedExpression;
                if (requiredHeaderName.equals("*")) {
                    // Special case: All headers stored together, name1=value1\nname2=value2\n...
                    builder.multipleValue(headers2ConfigurationHeadersString(headers));
                } else {
                    Object headerValue = headers.get(requiredHeaderName);
                    if (Objects.nonNull(headerValue)) {
                        if (headerValue instanceof List) {
                            for (Object elem : (List)headerValue) {
                                builder.multipleValue(Objects.toString(elem));
                                if (!parsingRule.getMultiple()) {
                                    break;
                                }
                            }
                        } else {
                            builder.multipleValue(Objects.toString(headerValue));
                        }
                    }
                }
                return builder.get();
            }

            String[] splitExpression = parsedExpression.split("/", 2);
            requiredHeaderName = splitExpression[0];
            String expression = splitExpression[1];

            if (headers.get(requiredHeaderName) == null) {
                return builder.get();
            }

            Pattern pattern = Pattern.compile(expression);
            Matcher matcher = pattern.matcher(Objects.toString(headers.get(requiredHeaderName)));
            if (matcher.find()) {
                buildResult(parsingRule, matcher, builder);
            }
            return builder.get();
        }
    };

    @Override
    public abstract String toString();

    public abstract MessageParameter parse(Message message, ParsingRule parsingRule);

    /**
     * TODO: Add JavaDoc.
     */
    public static ParsingRuleType from(String value) {
        for (ParsingRuleType type : values()) {
            if (type.toString().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Undefined type of parsing rule '" + value
                + "' expected: " + Arrays.toString(values()));
    }

    protected void buildResult(ParsingRule parsingRule, Matcher matcher, MessageParameter.Builder builder) {
        if (parsingRule.getMultiple()) {
            for (int groupIndex = 1; groupIndex <= matcher.groupCount(); groupIndex++) {
                builder.multipleValue(matcher.group(groupIndex));
            }
        } else {
            if (matcher.groupCount() > 0) {
                builder.multipleValue(matcher.group(1));
            } else {
                builder.multipleValue("");
            }
        }
    }

    protected String headers2ConfigurationHeadersString(Map<String, Object> headers) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            if (!entry.getKey().startsWith("Camel")) {
                if (entry.getValue() instanceof List) {
                    for (Object elem : (List)entry.getValue()) {
                        sb.append(entry.getKey()).append("=").append(elem).append("\n");
                    }
                } else {
                    sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
                }
            }
        }
        return sb.toString();
    }
}
