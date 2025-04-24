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

package org.qubership.automation.itf.core.util.helper;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.reflections.Reflections;
import org.springframework.util.ReflectionUtils;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.ObjectArrays;
import com.google.common.reflect.TypeToken;

public final class Reflection {

    private static final Reflections REFLECTIONS = new Reflections("org.qubership.automation.itf");
    private static final Pattern SEPARATOR_PATTERN = Pattern.compile("\n|;");

    public static Reflections getReflections() {
        return REFLECTIONS;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static Object get(Field field, Object owner) {
        boolean accessible = field.isAccessible();
        try {
            field.setAccessible(true);
            return field.get(owner);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            field.setAccessible(accessible);
        }
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static List<Field> getFieldsAnnotatedBy(Class clazz, Class<? extends Annotation> annotationClass) {
        Field[] declaredFields = clazz.getDeclaredFields();
        List<Field> annotatedFields = Lists.newArrayListWithExpectedSize(declaredFields.length / 2 + 5);
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(annotationClass)) {
                annotatedFields.add(field);
            }
        }
        return annotatedFields;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static void setFieldValue(Class<?> clazz, String name, Object owner, Object value) {
        setFieldValue(ReflectionUtils.findField(clazz, name), owner, value);
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static void setFieldValue(Field field, Object owner, Object value) {
        boolean accessible = field.isAccessible();
        try {
            field.setAccessible(true);
            field.set(owner, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            field.setAccessible(accessible);
        }
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static String convertToString(Object object) {
        if (object != null) {
            PropertyEditor editor = PropertyEditorManager.findEditor(object.getClass());
            editor.setValue(object);
            editor.getAsText();
            return editor.getAsText();
        }
        return ""; //TODO throw error
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static String fromMap(Object object) {
        if (object != null) {
            return Joiner.on('\n').withKeyValueSeparator("=").join((Map<?, ?>) object);
        }
        return ""; //TODO throw error
    }

    public static String fromArray(Object array) {
        return Joiner.on('\n').skipNulls().join((Object[]) array);
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static String fromCollection(Object collection) {
        return Joiner.on('\n').skipNulls().join((Iterable<?>) collection);
    }

    /**
     * TODO: Add JavaDoc.
     */
    @SuppressWarnings("unchecked")
    public static <T> T convertFromString(Class<T> targetType, String text) {
        if (!Strings.isNullOrEmpty(text)) {
            PropertyEditor editor = PropertyEditorManager.findEditor(targetType);
            editor.setAsText(text);
            return (T) editor.getValue();
        }
        return null;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static <T> T[] toArray(Class<T> targetType, String values) {
        if (!Strings.isNullOrEmpty(values)) {
            List<String> list = Splitter.on('\n').omitEmptyStrings().trimResults().splitToList(values);
            T[] array = ObjectArrays.newArray(targetType, list.size());
            int i = 0;
            for (String value : list) {
                array[i++] = convertFromString(targetType, value);
            }
            return array;
        }
        return null;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static Map<String, Object> toStringMap(String text) {
        if (!Strings.isNullOrEmpty(text)) {
            Map<String, Object> map = Maps.newHashMap();
            Splitter splitter = Splitter.on('=').trimResults().limit(2);
            for (String string : Splitter.on('\n').omitEmptyStrings().trimResults().split(text)) {
                String[] array = Iterables.toArray(splitter.split(string), String.class);
                if (array.length == 2) {
                    if (map.containsKey(array[0])) {
                        Object oldValue = map.get(array[0]);
                        List<String> list;
                        if (oldValue instanceof List) {
                            list = (List<String>)oldValue;
                        } else {
                            list = new ArrayList<>();
                            list.add(oldValue.toString());
                        }
                        list.add(array[1]);
                        map.replace(array[0], list);
                    } else {
                        map.put(array[0], array[1]);
                    }
                }
            }
            return map;
        }
        return null;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static Collection toCollection(final Class<?> targetType, String value) {
        if (!Strings.isNullOrEmpty(value)) {
            return Lists.newArrayList(Collections2.transform(Splitter.on(SEPARATOR_PATTERN)
                    .omitEmptyStrings().trimResults().splitToList(value), new Function<String, Object>() {
                public Object apply(String s) {
                    return convertFromString(targetType, s);
                }
            }).iterator());
        }
        return null;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static Object detectAndConvertFromString(Class targetClass, String value) {
        value = Strings.nullToEmpty(value);
        Object converted;
        if (Collection.class.isAssignableFrom(targetClass)) {
            converted = Reflection.toCollection(String.class, value);
        } else if (Map.class.isAssignableFrom(targetClass)) {
            converted = Reflection.toStringMap(value);
        } else if (TypeToken.of(targetClass).isArray()) {
            converted = Reflection.toArray(TypeToken.of(targetClass).getComponentType().getRawType(), value);
        } else if ("java.lang.String".equals(targetClass.getName())) {
            converted = value;
        } else if ("java.lang.Integer".equals(targetClass.getName())) {
            converted = Integer.valueOf(value);
        } else if ("java.lang.Boolean".equals(targetClass.getName())) {
            converted = Boolean.parseBoolean(value);
        } else {
            converted = Reflection.convertFromString(targetClass, value);
        }
        return converted;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static Object detectAndConvertFromString(Field field, String value) {
        return detectAndConvertFromString(field.getType(), value);
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static String detectAndConvertToString(Object object) {
        if (object == null) {
            return "";
        }
        if (Collection.class.isAssignableFrom(object.getClass())) {
            return Reflection.fromCollection(object);
        } else if (Map.class.isAssignableFrom(object.getClass())) {
            return Reflection.fromMap(object);
        } else if (TypeToken.of(object.getClass()).isArray()) {
            return Reflection.fromArray(object);
        } else {
            return Reflection.convertToString(object);
        }
    }

    public static String detectAndConvertToString(Field field, Object object) {
        return detectAndConvertToString(get(field, object));
    }

}
