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

package org.apache.baremaps.calcite.ddl;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Parse tree for SqlAttributeDefinition, which is part of a {@link SqlCreateType}.
 */
public class SqlAttributeDefinition extends SqlCall {
  private static final SqlSpecialOperator OPERATOR =
      new SqlSpecialOperator("ATTRIBUTE_DEF", SqlKind.ATTRIBUTE_DEF);

  public final SqlIdentifier name;
  public final SqlDataTypeSpec dataType;
  final @Nullable SqlNode expression;
  final @Nullable SqlCollation collation;

  /** Creates a SqlAttributeDefinition; use {@link SqlDdlNodes#attribute}. */
  SqlAttributeDefinition(SqlParserPos pos, SqlIdentifier name,
      SqlDataTypeSpec dataType, @Nullable SqlNode expression, @Nullable SqlCollation collation) {
    super(pos);
    this.name = name;
    this.dataType = dataType;
    this.expression = expression;
    this.collation = collation;
  }

  @Override
  public SqlOperator getOperator() {
    return OPERATOR;
  }

  @Override
  public List<SqlNode> getOperandList() {
    return ImmutableList.of(name, dataType);
  }

  @Override
  public void unparse(SqlWriter writer, int leftPrec, int rightPrec) {
    name.unparse(writer, 0, 0);
    dataType.unparse(writer, 0, 0);
    if (collation != null) {
      writer.keyword("COLLATE");
      collation.unparse(writer);
    }
    if (Boolean.FALSE.equals(dataType.getNullable())) {
      writer.keyword("NOT NULL");
    }
    SqlNode expression = this.expression;
    if (expression != null) {
      writer.keyword("DEFAULT");
      SqlColumnDeclaration.exp(writer, expression);
    }
  }
}
