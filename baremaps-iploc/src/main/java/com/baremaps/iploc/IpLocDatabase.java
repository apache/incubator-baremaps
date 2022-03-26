/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.iploc;

import java.sql.*;

public class IpLocDatabase {

    /**
     * Create the database
     * @param fileName
     */
    public static void createNewDatabase(String fileName) {

        String url = "JDBC:sqlite:" + fileName;

        // SQL statement for creating a new table
        String dropSql = "DROP TABLE IF EXISTS employees;";
        String createSql = "CREATE TABLE IF NOT EXISTS employees (\n"
                + " id integer PRIMARY KEY,\n"
                + " name text NOT NULL,\n"
                + " capacity real\n"
                + ");";

        try {
            Connection conn = DriverManager.getConnection(url);
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("Using driver " + meta.getDriverName());
                System.out.println("A new database has been created.");

                Statement stmt = conn.createStatement();
                stmt.execute(dropSql);
                stmt.execute(createSql);
                System.out.println("A new table has been created.");

            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Open the database
     */
    public static void openDatabase(String fileName) {
        Connection conn = null;
        try {
            // db parameters
            String url = "JDBC:sqlite:" + fileName;
            // create a connection to the database
            conn = DriverManager.getConnection(url);

            System.out.println("Connection to SQLite has been established.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}
