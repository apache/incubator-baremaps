package io.gazetteer.postgis;

public class StatementParameter {

  private final String parameterClassName;

  private final int parameterMode;

  private final int parameterType;

  private final String parameterTypeName;

  private final int precision;

  private final int scale;

  private final int isNullable;

  private final boolean isSigned;

  public StatementParameter(String parameterClassName, int parameterMode, int parameterType, String parameterTypeName,
      int precision, int scale, int isNullable, boolean isSigned) {
    this.parameterClassName = parameterClassName;
    this.parameterMode = parameterMode;
    this.parameterType = parameterType;
    this.parameterTypeName = parameterTypeName;
    this.precision = precision;
    this.scale = scale;
    this.isNullable = isNullable;
    this.isSigned = isSigned;
  }

  public String getParameterClassName() {
    return parameterClassName;
  }

  public int getParameterMode() {
    return parameterMode;
  }

  public int getParameterType() {
    return parameterType;
  }

  public String getParameterTypeName() {
    return parameterTypeName;
  }

  public int getPrecision() {
    return precision;
  }

  public int getScale() {
    return scale;
  }

  public int getIsNullable() {
    return isNullable;
  }

  public boolean isSigned() {
    return isSigned;
  }
}
