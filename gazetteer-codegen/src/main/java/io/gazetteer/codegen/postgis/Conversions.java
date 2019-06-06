package io.gazetteer.codegen.postgis;

import com.google.common.base.CaseFormat;

public class Conversions {

    public static String className(String tableName) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableName);
    }

    public static String variableName(String columnName) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, columnName);
    }

}
