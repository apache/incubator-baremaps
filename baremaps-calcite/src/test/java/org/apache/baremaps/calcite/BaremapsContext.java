package org.apache.baremaps.calcite;

import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.jdbc.CalcitePrepare;
import org.apache.calcite.jdbc.CalcitePrepare.SparkHandler;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.tools.RelRunner;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public class BaremapsContext implements CalcitePrepare.Context {

    private final JavaTypeFactory typeFactory;

    private final CalciteSchema rootSchema;

    private final RelRunner planner;

    public BaremapsContext(JavaTypeFactory typeFactory, CalciteSchema rootSchema, RelRunner planner) {
        this.typeFactory = typeFactory;
        this.rootSchema = rootSchema;
        this.planner = planner;
    }

    @Override
    public JavaTypeFactory getTypeFactory() {
        return typeFactory;
    }

    @Override
    public CalciteSchema getRootSchema() {
        return rootSchema;
    }

    @Override
    public CalciteSchema getMutableRootSchema() {
        return rootSchema;
    }

    @Override
    public List<String> getDefaultSchemaPath() {
        return List.of();
    }

    @Override
    public CalciteConnectionConfig config() {
        return null;
    }

    @Override
    public SparkHandler spark() {
        return null;
    }

    @Override
    public DataContext getDataContext() {
        return null;
    }

    @Override
    public @Nullable List<String> getObjectPath() {
        return null;
    }

    @Override
    public RelRunner getRelRunner() {
        return planner;
    }

}
