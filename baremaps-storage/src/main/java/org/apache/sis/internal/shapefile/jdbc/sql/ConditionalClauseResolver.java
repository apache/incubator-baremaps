/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sis.internal.shapefile.jdbc.sql;

/**
 * Conditional Clause resolver : resolves a single part of an expression :
 * <br>- Only resolve comparison of simple statements like F = 2, not those involving intermediate calculations (F + 4 = 6).
 * <br>- Only resolve a part of an expression : in "A = 5 and (B = 3 or C = 6)" will treat A = 5, for example.
 * <br>- If many clauseResolvers are chained, operator precedence checking has to be done by the caller.
 * <br>- This class is only here temporary, and will be replaced by a good SQL parser as soon as possible.
 * @author Marc LE BIHAN
 */
public class ConditionalClauseResolver extends ClauseResolver
{
    /**
     * Construct a where clause resolver.
     * @param comparand1 The first comparand that might be a primitive or a Field.
     * @param comparand2 The second comparand that might be a primitive or a Field.
     * @param operator The operator to apply.
     */
    public ConditionalClauseResolver(Object comparand1, Object comparand2, String operator) {
        super(comparand1, comparand2, operator);
    }
}
