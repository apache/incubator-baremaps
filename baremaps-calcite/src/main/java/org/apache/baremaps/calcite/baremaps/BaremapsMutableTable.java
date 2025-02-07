/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.calcite.baremaps;

import org.apache.baremaps.calcite.*;
import org.apache.baremaps.calcite.DataColumn.Cardinality;
import org.apache.baremaps.data.collection.AppendOnlyLog;
import org.apache.baremaps.data.collection.DataCollection;
import org.apache.baremaps.data.memory.Memory;
import org.apache.baremaps.data.memory.MemoryMappedDirectory;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.linq4j.QueryProvider;
import org.apache.calcite.linq4j.Queryable;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.prepare.Prepare;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.TableModify;
import org.apache.calcite.rel.logical.LogicalTableModify;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelProtoDataType;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.schema.ModifiableTable;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Schemas;
import org.apache.calcite.schema.Wrapper;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.schema.impl.AbstractTableQueryable;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.sql2rel.InitializerExpressionFactory;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Type;
import java.nio.MappedByteBuffer;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static java.util.Objects.requireNonNull;


class BaremapsMutableTable extends AbstractTable implements ModifiableTable, Wrapper {

    private final String name;

    private final InitializerExpressionFactory initializerExpressionFactory;

    private final RelProtoDataType protoRowType;

    private final RelDataType rowType;

    private final DataSchema schema;

    final DataCollection<DataRow> rows;

    BaremapsMutableTable(String name,
                         RelProtoDataType protoRowType,
                         InitializerExpressionFactory initializerExpressionFactory,
                         RelDataTypeFactory typeFactory) {
        super();
        this.name = requireNonNull(name, "name");
        this.initializerExpressionFactory =
                requireNonNull(initializerExpressionFactory, "initializerExpressionFactory");
        this.protoRowType = requireNonNull(protoRowType, "protoRowType");
        this.rowType = this.protoRowType.apply(typeFactory);

        // Create the schema
        List<DataColumn> columns = rowType.getFieldList().stream().map(field -> {
            String columnName = field.getName();
            RelDataType relDataType = field.getType();
            DataColumn.Cardinality columnCardinality = cardinalityFromRelDataType(relDataType);
            DataColumn.Type columnType = typeFromRelDataType(relDataType);
            return (DataColumn) new DataColumnFixed(columnName, columnCardinality, columnType);
        }).toList();
        this.schema = new DataSchema(name, columns);

        // Create the collection
        DataRowType dataRowType = new DataRowType(schema);
        Memory<MappedByteBuffer> memory = new MemoryMappedDirectory(Paths.get(this.name));
        this.rows = new AppendOnlyLog<>(dataRowType, memory);
    }

    @Override
    public TableModify toModificationRel(
            RelOptCluster cluster,
            RelOptTable table,
            Prepare.CatalogReader catalogReader,
            RelNode child,
            TableModify.Operation operation,
            @Nullable List<String> updateColumnList,
            @Nullable List<RexNode> sourceExpressionList,
            boolean flattened) {
        return LogicalTableModify.create(table, catalogReader, child, operation,
                updateColumnList, sourceExpressionList, flattened);
    }

    private DataColumn.Cardinality cardinalityFromRelDataType(RelDataType columnType) {
        if (columnType.getSqlTypeName() == SqlTypeName.ARRAY) {
            return DataColumn.Cardinality.REPEATED;
        } else if (columnType.isNullable()) {
            return Cardinality.OPTIONAL;
        } else {
            return Cardinality.REQUIRED;
        }
    }

    public static DataColumn.Type typeFromRelDataType(RelDataType relDataType) {
        SqlTypeName sqlTypeName = relDataType.getSqlTypeName();
        switch (sqlTypeName) {
            case BOOLEAN:
                return DataColumn.Type.BOOLEAN;
            case TINYINT:
                return DataColumn.Type.BYTE;
            case SMALLINT:
                return DataColumn.Type.SHORT;
            case INTEGER:
                return DataColumn.Type.INTEGER;
            case BIGINT:
                return DataColumn.Type.LONG;
            case FLOAT:
            case REAL:
                return DataColumn.Type.FLOAT;
            case DOUBLE:
            case DECIMAL:
                return DataColumn.Type.DOUBLE;
            case CHAR:
            case VARCHAR:
                return DataColumn.Type.STRING;
            case BINARY:
            case VARBINARY:
                return DataColumn.Type.BINARY;
            case DATE:
                return DataColumn.Type.LOCAL_DATE;
            case TIME:
                return DataColumn.Type.LOCAL_TIME;
            case TIMESTAMP:
                return DataColumn.Type.LOCAL_DATE_TIME;
            case MAP:
                return DataColumn.Type.NESTED;
            case GEOMETRY:
                return DataColumn.Type.GEOMETRY;
            case ARRAY:
                RelDataType componentType = requireNonNull(relDataType.getComponentType());
                return typeFromRelDataType(componentType);
            default:
                throw new IllegalArgumentException("Unsupported Calcite type: " + sqlTypeName);
        }
    }

    @Override
    public Collection getModifiableCollection() {
        return new CollectionAdapter();
    }

    @Override
    public <T> Queryable<T> asQueryable(QueryProvider queryProvider, SchemaPlus schema,
                                        String tableName) {
        return new AbstractTableQueryable<T>(queryProvider, schema, this,
                tableName) {
            @Override
            public Enumerator<T> enumerator() {
                return (Enumerator<T>) Linq4j.enumerator(new CollectionAdapter());
            }
        };
    }

    @Override
    public Type getElementType() {
        return Object[].class;
    }

    @Override
    public Expression getExpression(SchemaPlus schema, String tableName, Class clazz) {
        return Schemas.tableExpression(schema, getElementType(), tableName, clazz);
    }

    @Override
    public RelDataType getRowType(final RelDataTypeFactory typeFactory) {
        return rowType;
    }

    @Override
    public <C extends Object> @Nullable C unwrap(Class<C> aClass) {
        if (aClass.isInstance(initializerExpressionFactory)) {
            return aClass.cast(initializerExpressionFactory);
        }
        return super.unwrap(aClass);
    }

    private class CollectionAdapter implements Collection<Object[]> {

        private final int size;

        public CollectionAdapter() {
            this.size = (int) Math.min(rows.size(), Integer.MAX_VALUE);
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public boolean isEmpty() {
            return rows.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return rows.contains(o);
        }

        @Override
        public Iterator<Object[]> iterator() {
            return rows.stream().map(row -> row.values().toArray()).iterator();
        }

        @Override
        public Object[] toArray() {
            return rows.stream().map(row -> row.values().toArray()).toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return (T[]) rows.stream().map(row -> row.values().toArray()).toArray();
        }

        @Override
        public boolean add(Object[] objects) {
            return rows.add(new DataRow(schema, List.of(objects)));
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return rows.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends Object[]> c) {
            return rows.addAll(c.stream().map(objects -> new DataRow(schema, List.of(objects))).toList());
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            rows.clear();
        }
    }
}
