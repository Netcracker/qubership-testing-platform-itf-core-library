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

package org.qubership.automation.itf.core.model.jpa.context;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.qubership.automation.itf.core.model.extension.Extendable;
import org.qubership.automation.itf.core.model.extension.ExtendableImpl;
import org.qubership.automation.itf.core.model.extension.Extension;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@SuppressWarnings("unchecked")
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class JsonContext extends JSONObject implements IJsonContext, Extendable {
    private static final long serialVersionUID = 20240812L;

    private static final Splitter SPLITTER = Splitter.on('.');
    private static final Pattern ARRAY_PATTERN = Pattern.compile("(\\w+)\\s*\\[\\s*(\\d+)\\s*\\]");
    private static final Pattern MAP_PATTERN = Pattern.compile("(\\w+)\\s*\\[\\s*(\"|')(\\w+)(\\2)\\s*\\]");

    private Object version;

    private List<String> labels = Lists.newArrayListWithExpectedSize(10);
    private ExtendableImpl extendable = new ExtendableImpl();

    private transient WeakHashMap<Object, Pair<Object, Object>> history;
    private boolean collectHistory = false;

    public JsonContext() {
        super();
        this.history = new WeakHashMap<>();
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static <T extends JsonStorable> T fromJson(String jsonString, Class<T> clazz)
            throws ParseException, IllegalAccessException, InstantiationException {
        T instance = clazz.newInstance();
        instance.setJsonString(jsonString);
        return instance;
    }

    @Override
    public Object get(Object key) {
        if (key instanceof String) {
            return iterateForGet((String) key, false, true);
        } else {
            return super.get(key);
        }
    }

    /**
     * TODO: Add JavaDoc.
     */
    public <T> T get(String key, Class<T> castTo) {
        try {
            return getCast(key, castTo);
        } catch (ClassCastException e) {
            return null;
        }
    }

    @Override
    public void putAll(Map m) {
        if (collectHistory) {
            Set<Map.Entry> set = m.entrySet();
            for (Map.Entry entry : set) {
                if (this.containsKey(entry.getKey())) {
                    history.put(entry.getKey(), ImmutablePair.of(super.get(entry.getKey()), entry.getValue()));
                }
            }
        }
        super.putAll(m);
    }

    @Override
    public Object put(Object key, Object value) {
        Object obj = key instanceof String ? iterateForPut((String) key, value) : super.put(key, value);
        if (collectHistory) {
            history.put(key, ImmutablePair.of(obj, value));
        }
        return obj;
    }

    private void put(Map to, Object fromKey, Object fromValue, boolean addNewOnly) {
        if (addNewOnly) {
            to.putIfAbsent(fromKey, fromValue);
            return;
        }
        to.put(fromKey, fromValue);
    }

    public Map<Object, Pair<Object, Object>> getHistory() {
        return Maps.newHashMap(history);
    }

    @Override
    public boolean containsKey(Object key) {
        return key instanceof String
                ? iterateForGet((String) key, false, false) != null
                : super.containsKey(key);
    }

    public Object create(Object key, boolean list) {
        Object o = list ? new JSONArray() : new JsonContext();
        return put(key, o);
    }

    public Object create(Object key) {
        return create(key, false);
    }

    private Object iterateForGet(String stringKey, boolean checkLast, boolean doThrow) {
        Object tmpObject = this;
        Iterator<String> iterator = SPLITTER.split(stringKey).iterator();
        Matcher arrayMatcher = ARRAY_PATTERN.matcher("");
        Matcher mapMatcher = MAP_PATTERN.matcher("");
        do {
            String keyPart = iterator.next();
            if (checkLast && !iterator.hasNext()) {
                break;
            }
            if (tmpObject == null) {
                if (doThrow) {
                    throw new IllegalStateException(String.format("Full key is '%s'. Cannot take property \"%s\" "
                            + "(may be there are dots in the full key?).", stringKey, keyPart));
                } else {
                    return null;
                }
            } else if (tmpObject instanceof Map) {
                if (arrayMatcher.reset(keyPart).matches()) {
                    String varName = arrayMatcher.group(1);
                    int index = Integer.parseInt(arrayMatcher.group(2));
                    Object o = ((Map) tmpObject).get(varName);
                    if (o instanceof List) {
                        if (((List) o).size() > index) {
                            tmpObject = ((List) o).get(index);
                        } else {
                            return null;
                        }
                    } else {
                        if (doThrow) {
                            throw new IllegalStateException(String.format("Cannot take index \"%s\" from object [%s], "
                                            + "class is [%s]. Object is not list. Full key is '%s'", index, tmpObject,
                                    tmpObject.getClass().getSimpleName(), stringKey));
                        } else {
                            return null;
                        }
                    }
                } else if (mapMatcher.reset(keyPart).matches()) {
                    String varName = mapMatcher.group(1);
                    String subVarName = mapMatcher.group(3);
                    Object o = ((Map) tmpObject).get(varName);
                    if (o instanceof Map) {
                        tmpObject = ((Map) o).get(subVarName);
                    } else {
                        if (doThrow) {
                            throw new IllegalStateException(String.format("Cannot take property \"%s\" from "
                                            + "object [%s], class is [%s]. Object is not map. Full key is '%s'",
                                    subVarName, tmpObject, tmpObject.getClass().getSimpleName(), stringKey));
                        } else {
                            return null;
                        }
                    }
                } else {
                    tmpObject = (tmpObject == this) ? super.get(keyPart) : ((Map) tmpObject).get(keyPart);
                }
            } else {
                if (doThrow) {
                    throw new IllegalStateException(String.format("Cannot take property \"%s\" from object [%s], "
                                    + "class is [%s]. Object is not map. Full key is '%s'", keyPart, tmpObject,
                            tmpObject.getClass().getSimpleName(), stringKey));
                } else {
                    return null;
                }
            }
        } while (iterator.hasNext());
        return tmpObject;
    }

    private Object iterateForPut(String key, Object value) {
        Object tmpObject = iterateForGet(key, true, true);
        Iterator<String> iterator = SPLITTER.split(key).iterator();
        String lastKey;
        do {
            lastKey = iterator.next();
        } while (iterator.hasNext());
        Matcher arrayMatcher = ARRAY_PATTERN.matcher(lastKey);
        Matcher mapMatcher = MAP_PATTERN.matcher(lastKey);
        String simpleName = (tmpObject == null) ? "null" : tmpObject.getClass().getSimpleName();
        if (arrayMatcher.matches()) {
            String varName = arrayMatcher.group(1);
            int index = Integer.parseInt(arrayMatcher.group(2));
            if (tmpObject instanceof Map) {
                Object o = ((Map) tmpObject).get(varName);
                if (o == null) {
                    o = new JSONArray();
                    ((Map) tmpObject).put(varName, o);
                }
                if (o instanceof List) {
                    List list = (List) o;
                    if (index == list.size()) {
                        list.add(value);
                        return null;
                    } else {
                        if (list.size() < index) {
                            ((JSONArray) list).ensureCapacity(index + 1); // it is need, if I want to put
                            // first key[2], key[1], key[0].
                            for (int number = list.size(); number < index + 1; number++) {
                                list.add(null);
                            }
                        }
                        Object toReturn = list.get(index);
                        list.set(index, value);
                        return toReturn;
                    }
                } else {
                    throw new IllegalStateException(String.format("Cannot set value by index \"%s\" to object [%s], "
                                    + "class is [%s]. Object is not list. Full key is '%s'", index, o,
                            o.getClass().getSimpleName(), key));
                }
            } else {
                throw new IllegalStateException(String.format("Cannot take property \"%s\" from object [%s], "
                        + "class is [%s]. Object is not map. Full key is '%s'", varName, tmpObject, simpleName, key));
            }
        } else if (mapMatcher.matches()) {
            String varName = mapMatcher.group(1);
            String subVarName = mapMatcher.group(3);
            if (tmpObject instanceof Map) {
                Object o = ((Map) tmpObject).get(varName);
                if (o == null) {
                    o = new JsonContext();
                    ((Map) tmpObject).put(varName, o);
                }
                if (o instanceof Map) {
                    return ((Map) o).put(subVarName, value);
                } else {
                    throw new IllegalStateException(String.format("Cannot set property \"%s\" to object [%s], "
                                    + "class is [%s]. Object is not map. Full key is '%s'", subVarName, o,
                            o.getClass().getSimpleName(), key));
                }
            } else {
                throw new IllegalStateException(String.format("Cannot take property \"%s\" from object [%s], "
                        + "class is [%s]. Object is not map. Full key is '%s'", varName, tmpObject, simpleName, key));
            }
        } else {
            if (tmpObject instanceof Map) {
                return (tmpObject == this) ? super.put(lastKey, value) : ((Map) tmpObject).put(lastKey, value);
            } else {
                throw new IllegalStateException(String.format("Cannot set property \"%s\" to object [%s], "
                        + "class is [%s]. Object is not map. Full key is '%s'", lastKey, tmpObject, simpleName, key));
            }
        }
    }

    public <T> T getCast(String key, Class<T> castTo) throws ClassCastException {
        Object o = get(key);
        return o != null ? castTo.cast(o) : null;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public void merge(Map from) {
        if (from == null || from.isEmpty()) {
            return;
        }
        mergeMap(this, from, false);
    }

    private void merge(Map to, Object fromKey, Object fromValue, boolean addNewOnly) {
        Object toValue = to.get(fromKey);
        if (toValue instanceof Map && fromValue instanceof Map) {
            mergeMap((Map) toValue, (Map) fromValue, addNewOnly);
        } else if (toValue instanceof List && fromValue instanceof List) {
            mergeList((List) toValue, (List) fromValue, addNewOnly);
        } else {
            put(to, fromKey, fromValue, addNewOnly);
        }
    }

    /**
     * Put ONLY new variables to context. This method uses mergeMap method, but it doesn't replace any existing
     * variable values.
     * Possible cases:
     * 1. if the value of a key contains map, it will be iterated and new key-value pairs will be added.
     * 2. if the value of some key contains a list - only new values will be added to the end of the list
     * if the list contains:
     * 1. maps, then it will be iterated and new key-value pairs will be added
     * 2. lists, then we iterate recursively (we return to the beginning of the mergeList method
     * to check the values of these lists).
     * 3. strings\numbers and other non-collection types, then the values that are in the “to” list will be removed
     * from the “from” list, and then if anything remains in “from” it will be added to the end of the “to” list)
     *
     * @param from - map; only new key-values from this map will be added to this (JsonContext/Map).
     */
    @SuppressWarnings("unused")
    public void putIfAbsent(Map from) {
        mergeMap(this, from, true);
    }

    /**
     * TODO: Add JavaDoc.
     */
    public void mergeMap(Map to, Map from, boolean addNewOnly) {
        for (Object o : from.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            Object fromValue = entry.getValue();
            Object fromKey = entry.getKey();
            if (to.containsKey(fromKey)) {
                merge(to, fromKey, fromValue, addNewOnly);
            } else {
                add(to, fromKey, fromValue);
            }
        }
    }

    private void add(Map to, Object fromKey, Object fromValue) {
        String[] split = fromKey.toString().split("\\.");
        String group = split[0];
        if (!to.containsKey(group)) {
            to.put(group, new JSONObject());
        }
        if (fromValue instanceof Map) {
            JSONObject toChild = new JsonContext();
            for (Object val : ((Map) fromValue).entrySet()) {
                Entry valEntry = (Entry) val;
                Object valValue = valEntry.getValue();
                Object valKey = valEntry.getKey();
                if (valKey instanceof String && ((String) valKey).contains(".")) {
                    checkOrCreateParents(toChild, (String) valKey, valValue);
                } else {
                    toChild.put(valKey, valValue);
                }
            }
            to.put(fromKey, toChild);
        } else {
            to.put(fromKey, fromValue);
        }
    }

    private void checkOrCreateParents(JSONObject to, String key, Object value) {
        String[] names = key.split("\\.");
        JSONObject curObj = to;
        for (int i = 0; i < names.length; i++) {
            if (!curObj.containsKey(names[i])) {
                if (i == names.length - 1) {
                    curObj.put(names[i], value);
                    break;
                } else {
                    curObj.put(names[i], new JSONObject());
                }
            }
            curObj = (JSONObject) curObj.get(names[i]);
        }
    }

    private void mergeList(List to, List from, boolean addNewOnly) {
        int toSize = to.size();
        int fromSize = from.size();
        for (int i = 0; i < toSize && i < fromSize; i++) {
            mergeListElement(to, from, addNewOnly, i);
        }
        if (addNewOnly) {
            addNewListElementsIfNotContains(to, from, fromSize);
            return;
        }
        addListElementsAfterReplaceExisting(to, from, fromSize, toSize);
    }

    private void mergeListElement(List to, List from, boolean addNewOnly, int elementIndex) {
        Object toValue = to.get(elementIndex);
        Object fromValue = from.get(elementIndex);
        if (toValue instanceof Map && fromValue instanceof Map) {
            mergeMap((Map) toValue, (Map) fromValue, addNewOnly);
        } else if (toValue instanceof List && fromValue instanceof List) {
            mergeList((List) toValue, (List) fromValue, addNewOnly);
        } else {
            if (!addNewOnly) {
                to.set(elementIndex, fromValue);
            }
        }
    }

    private void addListElementsAfterReplaceExisting(List to, List from, int fromSize, int toSize) {
        if (fromSize > toSize) {
            for (int i = toSize; i < fromSize; i++) {
                to.add(i, from.get(i));
            }
        }
    }

    private void addNewListElementsIfNotContains(List to, List from, int fromSize) {
        for (int i = 0; i < fromSize; i++) {
            Object element = from.get(i);
            if (!to.contains(element)) {
                to.add(element);
            }
        }
    }

    public boolean isCollectHistory() {
        return collectHistory;
    }

    public JsonContext setCollectHistory(boolean collectHistory) {
        this.collectHistory = collectHistory;
        return this;
    }

    @Override
    public boolean extend(Extension extension) {
        return extendable.extend(extension);
    }

    @Override
    public <T extends Extension> T getExtension(Class<T> extensionClass) {
        return extendable.getExtension(extensionClass);
    }

    @Override
    public String getExtensionsJson() {
        return extendable.getExtensionsJson();
    }

    @Override
    public void setExtensionsJson(String extensionsJson) {
        extendable.setExtensionsJson(extensionsJson);
    }

    public String getJsonString() {
        return toJSONString();
    }

    protected void setJsonString(String jsonString) throws ParseException {
        JSONParser parser = new JSONParser();
        if (Strings.isNullOrEmpty(jsonString)) {
            return;
        }
        Object parse = parser.parse(jsonString);
        if (parse instanceof Map) {
            putAll((Map) parse);
        } else {
            put("parsed", parse);
        }
    }
}
