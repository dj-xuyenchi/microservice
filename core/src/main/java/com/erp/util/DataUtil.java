package com.erp.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import com.erp.constant.Constant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.thoughtworks.xstream.XStream;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;


public class DataUtil {

    private static final XStream xstream = new XStream();
    private static final Gson gson = new Gson();

    static {
        XStream.setupDefaultSecurity(xstream);
        xstream.allowTypesByWildcard(new String[]{"**"});  // Cho phép tất cả class, hoặc chỉ whitelist nếu cần
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    public static boolean isNull(final Object obj) {
        return obj == null;
    }

    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNumber(String str) {
        return str != null && str.matches("\\d+");
    }

    public static boolean isNullOrEmpty(CharSequence cs) {
        int strLen;
        if (cs != null && (strLen = cs.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isNullOrEmpty(Object[] collection) {
        return collection == null || collection.length == 0;
    }

    public static boolean isNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isEmail(String email) {
        if (email == null) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    public static String objectToXml(Object obj) {
        if (isNull(obj)) return "<null/>";
        return "\n" + xstream.toXML(obj);
    }

    public static String objectToJson(Object object) throws Exception {
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
    public static String objectToJsonNoException(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return e.getMessage();
        }
    }
    public static <T> T objectToClass(Object object, Class<T> targetClass) throws Exception {
        try {
            String json = mapper.writeValueAsString(object);
            return mapper.readValue(json, targetClass);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public static String safeToString(Object obj1, String defaultValue) {
        return obj1 == null ? defaultValue : obj1.toString().trim();
    }

    public static String safeToString(Object obj1) {
        return safeToString(obj1, "");
    }

    public static Long safeToLong(Object obj1, Long defaultValue) {
        if (obj1 != null && (!(obj1 instanceof String) || !isNullOrEmpty(safeToString(obj1)))) {
            if (obj1 instanceof BigDecimal) {
                return ((BigDecimal) obj1).longValue();
            } else if (obj1 instanceof BigInteger) {
                return ((BigInteger) obj1).longValue();
            } else if (obj1 instanceof Double) {
                return ((Double) obj1).longValue();
            } else {
                try {
                    return Long.parseLong(obj1.toString());
                } catch (NumberFormatException nfe) {
                    return defaultValue;
                }
            }
        } else {
            return defaultValue;
        }
    }

    public static Long safeToLong(Object obj1) {
        return safeToLong(obj1, 0L);
    }

    public static Double safeToDouble(Object obj1, Double defaultValue) {
        if (obj1 != null && (!(obj1 instanceof String) || !isNullOrEmpty(safeToString(obj1)))) {
            try {
                return Double.parseDouble(obj1.toString());
            } catch (NumberFormatException nfe) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public static Double safeToDouble(Object obj1) {
        return safeToDouble(obj1, (double) 0.0F);
    }

    public static Short safeToShort(Object obj1, Short defaultValue) {
        if (obj1 != null && (!(obj1 instanceof String) || !isNullOrEmpty(safeToString(obj1)))) {
            try {
                return Short.parseShort(obj1.toString());
            } catch (NumberFormatException nfe) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public static Short safeToShort(Object obj1) {
        return safeToShort(obj1, Short.valueOf((short) 0));
    }

    public static Integer safeToInt(Object obj1, Integer defaultValue) {
        if (obj1 != null && (!(obj1 instanceof String) || !isNullOrEmpty(safeToString(obj1)))) {
            try {
                String s = obj1.toString();
                if (s.contains(".")) {
                    s = s.substring(0, s.lastIndexOf("."));
                }

                return Integer.parseInt(s);
            } catch (NumberFormatException nfe) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public static Integer safeToInt(Object obj1) {
        return safeToInt(obj1, 0);
    }

    public static BigDecimal safeToBigDecimal(Object obj1, BigDecimal defaultValue) {
        if (obj1 != null && (!(obj1 instanceof String) || !isNullOrEmpty(safeToString(obj1)))) {
            try {
                return new BigDecimal(obj1.toString());
            } catch (NumberFormatException nfe) {
                return BigDecimal.ZERO;
            }
        } else {
            return defaultValue;
        }
    }

    public static BigDecimal safeToBigDecimal(Object obj1) {
        return safeToBigDecimal(obj1, BigDecimal.ZERO);
    }

    public static BigInteger safeToBigInteger(Object obj1) {
        return safeToBigInteger(obj1, BigInteger.ZERO);
    }

    public static BigInteger safeToBigInteger(Object obj1, BigInteger defaultValue) {
        if (obj1 != null && (!(obj1 instanceof String) || !isNullOrEmpty(safeToString(obj1)))) {
            try {
                return new BigInteger(obj1.toString());
            } catch (NumberFormatException nfe) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public static Boolean safeToBoolean(Integer data) {
        return safeToBoolean(data, false);
    }

    public static Boolean safeToBoolean(Integer data, Boolean defaultValue) {
        return isNullObject(data) ? defaultValue : data.equals(1);
    }

    public static Boolean safeToBoolean(String data) {
        return safeToBoolean(data, false);
    }

    public static Boolean safeToBoolean(String data, Boolean defaultValue) {
        return isNullOrEmpty(data) ? defaultValue : data.equals("true") || data.equals("1");
    }

    public static Boolean safeToBoolean(Double data) {
        return safeToBoolean(data, false);
    }

    public static Boolean safeToBoolean(Double data, Boolean defaultValue) {
        return isNullObject(data) ? defaultValue : data == (double) 1.0F;
    }

    public static Boolean safeToBoolean(Long data) {
        return safeToBoolean(data, false);
    }

    public static Boolean safeToBoolean(Long data, Boolean defaultValue) {
        return isNullObject(data) ? defaultValue : data == 1L;
    }

    public static Boolean safeToBoolean(Boolean data) {
        return safeToBoolean(data, false);
    }

    public static Boolean safeToBoolean(Boolean data, Boolean defaultValue) {
        return isNullObject(data) ? defaultValue : data;
    }

    public static <T> List<T> safeToList(List<T> data, List<T> defaultValue) {
        return isNullOrEmpty((Collection) data) ? defaultValue : data;
    }

    public static <T> List<T> safeToList(List<T> data) {
        return safeToList(data, new ArrayList());
    }

    public static <T> Set<T> safeToSet(Set<T> data, Set<T> defaultValue) {
        return isNullOrEmpty((Collection) data) ? defaultValue : data;
    }

    public static <T> Set<T> safeToSet(Set<T> data) {
        return safeToSet(data, new HashSet());
    }

    public static <T1, T2> Map<T1, T2> safeToMap(Map<T1, T2> data, Map<T1, T2> defaultValue) {
        return isNullOrEmpty(data) ? defaultValue : data;
    }

    public static <T1, T2> Map<T1, T2> safeToMap(Map<T1, T2> data) {
        return safeToMap(data, new HashMap());
    }

    public static boolean isNullObject(Object obj1) {
        if (obj1 == null) {
            return true;
        } else {
            return obj1 instanceof String ? isNullOrEmpty(obj1.toString()) : false;
        }
    }

    @SafeVarargs
    public static <T> List<T> tupleToObject(
            List<Tuple> listSource,
            Class<T> classTarget,
            Map<String, String> dateFormats,
            Map<String, Object>... options) {

        List<T> result = new ArrayList<>();
        if (listSource == null || listSource.isEmpty()) return result;

        try {
            for (Tuple sourceItem : listSource) {
                T target = classTarget.getDeclaredConstructor().newInstance();
                Field[] targetFields = classTarget.getDeclaredFields();

                // build alias map
                Map<String, TupleElement<?>> aliasMap = sourceItem.getElements()
                        .stream()
                        .collect(Collectors.toMap(
                                item -> item.getAlias().replace("_", "").toLowerCase(),
                                item -> item,
                                (a, b) -> a // trường hợp duplicate
                        ));

                for (Field field : targetFields) {
                    field.setAccessible(true);
                    TupleElement<?> tupleElement = aliasMap.get(field.getName().toLowerCase());

                    if (tupleElement != null) {
                        try {
                            setFieldData(sourceItem, tupleElement.getAlias(), field, target, dateFormats, options);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                result.add(target);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @SafeVarargs
    public static <T> List<T> tupleToObject(List<Tuple> listSource, Class<T> classTarget, String dateFormat, Map<String, Object>... options) {
        Map<String, String> dateFormats = new HashMap();
        dateFormats.put("ALL", dateFormat);
        return tupleToObject(listSource, classTarget, dateFormats, options);
    }

    @SafeVarargs
    public static <T> List<T> tupleToObject(List<Tuple> listSource, Class<T> classTarget, Map<String, Object>... options) {
        return tupleToObject(listSource, classTarget, "yyyy-MM-dd", options);
    }

    public static <T> T tupleToObject(Tuple source, Class<T> classTarget, Map<String, String> dateFormats) {
        try {
            Constructor<?> cons = classTarget.getConstructor();
            Object target = cons.newInstance();
            Field[] targetFields = target.getClass().getDeclaredFields();

            for (Field targetFieldItem : targetFields) {
                String fieldName = targetFieldItem.getName().toLowerCase();
                List<TupleElement<?>> sourceFields = source.getElements();

                try {
                    List<TupleElement<?>> collect = (List) sourceFields.stream().filter((item) -> {
                        String sourceFieldNameRemoveUnderscore = item.getAlias().replace("_", "");
                        return fieldName.equalsIgnoreCase(sourceFieldNameRemoveUnderscore);
                    }).collect(Collectors.toList());
                    if (!isNullOrEmpty((Collection) collect)) {
                        String sourceFieldName = ((TupleElement) collect.get(0)).getAlias();
                        setFieldData(source, sourceFieldName, targetFieldItem, target, dateFormats);
                    }
                } catch (Exception e) {
                    String message = e.getMessage();
                    if (!safeEqual(message, "Unknown alias []")) {
                    }
                }
            }

            return (T) target;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T tupleToObject(Tuple source, Class<T> classTarget, String dateFormat) {
        Map<String, String> dateFormats = new HashMap();
        dateFormats.put("ALL", dateFormat);
        return (T) tupleToObject(source, classTarget, dateFormats);
    }

    public static <T> T tupleToObject(Tuple source, Class<T> classTarget) {
        return (T) tupleToObject(source, classTarget, "yyyy-MM-dd");
    }

    @SafeVarargs
    private static <T> void setFieldData(Tuple sourceItem, String sourceFieldName, Field targetFieldItem, Object target, Map<String, String> dateFormats, Map<String, Object>... options) throws IllegalAccessException {
        Integer digitsRound = null;
        if (options.length > 0) {
            Map<String, Object> option = options[0];
            digitsRound = isNullObject(option.get("digitsRound")) ? null : safeToInt(option.get("digitsRound"));
        }

        targetFieldItem.setAccessible(true);
        Class<?> targetFieldType = targetFieldItem.getType();
        String targetFieldTypeName = targetFieldType.getName();
        String dateFormat;
        if (dateFormats.size() == 1) {
            dateFormat = (String) dateFormats.get("ALL");
        } else {
            dateFormat = (String) dateFormats.get(sourceFieldName);
        }

        Object sourceItemData = sourceItem.get(sourceFieldName);
        if (sourceItemData != null) {
            Constant.DataTypeClassName targetFieldClassName = Constant.DataTypeClassName.get(targetFieldTypeName);
            if (isNullObject(targetFieldClassName)) {
                if (Object.class.isAssignableFrom(targetFieldType)) {
                    targetFieldItem.set(target, jsonToObject(safeToString(sourceItemData), targetFieldType));
                }

            } else {
                switch (targetFieldClassName) {
                    case STRING:
                        if (sourceItemData instanceof Date) {
                            targetFieldItem.set(target, DateUtil.dateToString(DateUtil.safeToDate(sourceItemData), dateFormat));
                        } else {
                            targetFieldItem.set(target, safeToString(sourceItemData));
                        }
                        break;
                    case LONG:
                    case PRIMITIVE_LONG:
                        targetFieldItem.set(target, safeToLong(sourceItemData));
                        break;
                    case DOUBLE:
                    case PRIMITIVE_DOUBLE:
                        if (isNullOrZero(digitsRound)) {
                            targetFieldItem.set(target, safeToDouble(sourceItemData));
                        } else {
                            targetFieldItem.set(target, round(safeToDouble(sourceItemData), digitsRound));
                        }
                        break;
                    case BOOLEAN:
                    case PRIMITIVE:
                        targetFieldItem.set(target, "true".equalsIgnoreCase(safeToString(sourceItemData)) || "1".equalsIgnoreCase(safeToString(sourceItemData)));
                        break;
                    case DATE:
                        targetFieldItem.set(target, DateUtil.safeToDate(sourceItemData));
                        break;
                    case BIG_DECIMAL:
                        targetFieldItem.set(target, safeToBigDecimal(sourceItemData));
                        break;
                    case INTEGER:
                    case INT:
                        targetFieldItem.set(target, safeToInt(sourceItemData));
                        break;
                    case LIST:
                        Type genericType = targetFieldItem.getGenericType();
                        if (genericType instanceof ParameterizedType) {
                            ParameterizedType parameterizedType = (ParameterizedType) genericType;
                            Type[] typeArguments = parameterizedType.getActualTypeArguments();
                            if (typeArguments.length > 0 && typeArguments[0] instanceof Class) {
                                Class<T> type = (Class) typeArguments[0];
                                targetFieldItem.set(target, jsonToObject(safeToString(sourceItemData), type));
                            }
                        }
                }

            }
        }
    }

    public static <T> T jsonToObject(String json, Class<T> classTarget) {
        if (isNullOrEmpty(json)) return null;
        try {
            return gson.fromJson(json, classTarget);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    public static Double round(Double number, Integer digits) {
        if (isNullObject(number)) {
            return null;
        } else {
            number = safeToDouble(number);
            digits = safeToInt(digits);
            if (digits < 0) {
                throw new IllegalArgumentException("Số chữ số sau dấu thập phân phải là số nguyên dương.");
            } else {
                BigDecimal bd = BigDecimal.valueOf(number);
                bd = bd.setScale(digits, RoundingMode.HALF_UP);
                return bd.doubleValue();
            }
        }
    }

    public static boolean safeEqual(Integer obj1, Integer obj2) {
        if (Objects.equals(obj1, obj2)) {
            return true;
        } else {
            return obj1 != null && obj2 != null && obj1.compareTo(obj2) == 0;
        }
    }

    public static boolean safeEqual(Double obj1, Double obj2) {
        if (Objects.equals(obj1, obj2)) {
            return true;
        } else {
            return obj1 != null && obj2 != null && obj1.compareTo(obj2) == 0;
        }
    }

    public static boolean safeEqual(Long obj1, Long obj2) {
        if (Objects.equals(obj1, obj2)) {
            return true;
        } else {
            return obj1 != null && obj2 != null && obj1.compareTo(obj2) == 0;
        }
    }

    public static boolean safeEqual(BigInteger obj1, BigInteger obj2) {
        if (Objects.equals(obj1, obj2)) {
            return true;
        } else {
            return obj1 != null && obj1.equals(obj2);
        }
    }

    public static boolean safeEqual(Short obj1, Short obj2) {
        if (Objects.equals(obj1, obj2)) {
            return true;
        } else {
            return obj1 != null && obj2 != null && obj1.compareTo(obj2) == 0;
        }
    }

    public static boolean safeEqualCaseSensitive(String obj1, String obj2) {
        return Objects.equals(obj1, obj2);
    }

    public static boolean safeEqual(String obj1, String obj2) {
        if (Objects.equals(obj1, obj2)) {
            return true;
        } else {
            return obj1 != null && obj1.equalsIgnoreCase(obj2);
        }
    }

    public static boolean safeEqual(Object obj1, Object obj2) {
        try {
            if (obj1 == null && obj2 == null) {
                return true;
            } else if (obj1 != null && obj2 != null) {
                if (obj1 instanceof String) {
                    return String.valueOf(obj1).equals(String.valueOf(obj2));
                } else if (obj1 instanceof Long) {
                    return safeToLong(obj1).equals(safeToLong(obj2));
                } else if (obj1 instanceof Integer) {
                    return safeToInt(obj1).equals(safeToInt(obj2));
                } else {
                    return obj1 == obj2;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean safeEqual(Boolean obj1, Boolean obj2) {
        return Objects.equals(obj1, obj2);
    }

    public static boolean isNullOrZero(Long value) {
        return value == null || value.equals(0L);
    }

    public static boolean isNullOrZero(Double value) {
        return value == null || value == (double) 0.0F;
    }

    public static boolean isNullOrZero(String value) {
        return value == null || safeToLong(value).equals(0L);
    }

    public static boolean isNullOrZero(Integer value) {
        return value == null || value.equals(0);
    }

    public static boolean isNullOrZero(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) == 0;
    }
}
