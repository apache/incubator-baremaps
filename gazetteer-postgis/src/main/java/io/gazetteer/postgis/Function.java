package io.gazetteer.postgis;

import com.google.common.base.MoreObjects;

public class Function {

    private final String functionCatalog;
    private final String functionSchema;
    private final String functionName;
    private final String remarks;
    private final Short functionType;
    private final String specificName;

    public Function(String functionCatalog, String functionSchema, String functionName, String remarks, Short functionType, String specificName) {
        this.functionCatalog = functionCatalog;
        this.functionSchema = functionSchema;
        this.functionName = functionName;
        this.remarks = remarks;
        this.functionType = functionType;
        this.specificName = specificName;
    }

    public String getFunctionCatalog() {
        return functionCatalog;
    }

    public String getFunctionSchema() {
        return functionSchema;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getRemarks() {
        return remarks;
    }

    public short getFunctionType() {
        return functionType;
    }

    public String getSpecificName() {
        return specificName;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("functionCatalog", functionCatalog)
                .add("functionSchema", functionSchema)
                .add("functionName", functionName)
                .add("remarks", remarks)
                .add("functionType", functionType)
                .add("specificName", specificName)
                .toString();
    }
}
