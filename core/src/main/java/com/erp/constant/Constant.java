package com.erp.constant;

import java.math.BigDecimal;
import java.util.*;

public final class Constant {
    public interface StatusType {
        String ACTIVE = "O";
        String CLOSE = "C";
        String DELETE = "D";
    }

    public interface ConstType {
        String APPLY_EFFECT = "E";
        String NO_APPLY_EFFECT = "NE";
    }

    public interface API_CONSTANT {
        String OK = "OK";
        String ERROR_AUTHORIZE = "ERROR_AUTHORIZE";
    }

    public interface GlobalConfig {
        String GLOBAL = "GLOBAL";
        String GLOBAL_DATA = "GLOBAL_DATA";
    }

    public interface FilterGatewayParams {
        String X_USER_ID = "X-User-Id";
        String X_USER_NAME = "X-User-Name";
        String X_USER_ROLES = "X-User-Roles";
    }

    public static enum DataTypeClassName {
        STRING(String.class.getName()),
        LONG(Long.class.getName()),
        PRIMITIVE_LONG(Long.TYPE.getName()),
        DOUBLE(Double.class.getName()),
        PRIMITIVE_DOUBLE(Double.TYPE.getName()),
        BOOLEAN(Boolean.class.getName()),
        PRIMITIVE(Boolean.TYPE.getName()),
        DATE(Date.class.getName()),
        BIG_DECIMAL(BigDecimal.class.getName()),
        INTEGER(Integer.class.getName()),
        INT(Integer.TYPE.getName()),
        OBJECT(Objects.class.getName()),
        LIST(List.class.getName());

        public final String value;
        private static final Map<String, DataTypeClassName> lookup = new HashMap<>();

        public static DataTypeClassName get(String value) {
            return (DataTypeClassName) lookup.get(value);
        }

        private DataTypeClassName(String value) {
            this.value = value;
        }

        static {
            for (DataTypeClassName d : values()) {
                lookup.put(d.value, d);
            }

        }
    }
}

