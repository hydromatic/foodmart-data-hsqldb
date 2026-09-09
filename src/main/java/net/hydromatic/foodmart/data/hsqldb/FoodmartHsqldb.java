/*
 * Licensed to Julian Hyde under one or more contributor license
 * agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership. Julian Hyde
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hydromatic.foodmart.data.hsqldb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Foodmart data set in hsqldb format. */
public class FoodmartHsqldb {
  /**
   * URI of the hsqldb database.
   *
   * <p>In this database, the aggregate tables and {@code employee_closure} are
   * views, computed on demand from the base tables.
   */
  public static final String URI = "jdbc:hsqldb:res:foodmart";

  /**
   * URI of the hsqldb database in which the aggregate tables and {@code
   * employee_closure} are materialized as indexed memory tables.
   *
   * <p>The contents are identical to those of {@link #URI}. Queries that use
   * the aggregate tables are faster, at the cost of about 1.5 seconds to
   * compute the tables when the database is opened.
   */
  public static final String MATERIALIZED_URI = "jdbc:hsqldb:res:foodmart-mat";

  public static final String USER = "FOODMART";
  public static final String PASSWORD = "FOODMART";

  /** Consumer interface for processing table metadata. */
  public interface TableConsumer {
    void accept(String tableName, String... quotedColumns);
  }

  /**
   * Invokes the consumer for each base table in the Foodmart schema.
   *
   * <p>Views (the aggregate tables and {@code employee_closure}) are not
   * included, because they have no CSV file; their contents are derived from
   * the base tables.
   *
   * @param consumer Consumer to invoke for each table with its name and quoted
   *     column names
   */
  public static void forEachTable(TableConsumer consumer) {
    consumer.accept(
        "account",
        "account_description",
        "account_type",
        "account_rollup",
        "Custom_Members");
    consumer.accept(
        "category",
        "category_id",
        "category_parent",
        "category_description",
        "category_rollup");
    consumer.accept("currency", "date", "currency");
    consumer.accept(
        "customer",
        "lname",
        "fname",
        "mi",
        "address1",
        "address2",
        "address3",
        "address4",
        "city",
        "state_province",
        "postal_code",
        "country",
        "phone1",
        "phone2",
        "birthdate",
        "marital_status",
        "yearly_income",
        "gender",
        "education",
        "date_accnt_opened",
        "member_card",
        "occupation",
        "houseowner",
        "fullname");
    consumer.accept("days", "week_day");
    consumer.accept("department", "department_description");
    consumer.accept(
        "employee",
        "full_name",
        "first_name",
        "last_name",
        "position_title",
        "birth_date",
        "hire_date",
        "end_date",
        "education_level",
        "marital_status",
        "gender",
        "management_role");
    consumer.accept("expense_fact", "exp_date", "category_id");
    consumer.accept("inventory_fact_1997");
    consumer.accept("inventory_fact_1998");
    consumer.accept(
        "position", "position_title", "pay_type", "management_role");
    consumer.accept("product", "brand_name", "product_name");
    consumer.accept(
        "product_class",
        "product_subcategory",
        "product_category",
        "product_department",
        "product_family");
    consumer.accept(
        "promotion", "promotion_name", "media_type", "start_date", "end_date");
    consumer.accept(
        "region",
        "sales_city",
        "sales_state_province",
        "sales_district",
        "sales_region",
        "sales_country");
    consumer.accept(
        "reserve_employee",
        "full_name",
        "first_name",
        "last_name",
        "position_title",
        "birth_date",
        "hire_date",
        "end_date",
        "education_level",
        "marital_status",
        "gender");
    consumer.accept("salary", "pay_date");
    consumer.accept("sales_fact_1997");
    consumer.accept("sales_fact_1998");
    consumer.accept("sales_fact_dec_1998");
    consumer.accept(
        "store",
        "store_type",
        "store_name",
        "store_street_address",
        "store_city",
        "store_state",
        "store_postal_code",
        "store_country",
        "store_manager",
        "store_phone",
        "store_fax",
        "first_opened_date",
        "last_remodel_date",
        "florist");
    consumer.accept(
        "store_ragged",
        "store_type",
        "store_name",
        "store_street_address",
        "store_city",
        "store_state",
        "store_postal_code",
        "store_country",
        "store_manager",
        "store_phone",
        "store_fax",
        "first_opened_date",
        "last_remodel_date",
        "florist");
    consumer.accept(
        "time_by_day",
        "the_date",
        "the_day",
        "the_month",
        "the_year",
        "day_of_month",
        "week_of_year",
        "month_of_year",
        "quarter",
        "fiscal_period");
    consumer.accept(
        "warehouse",
        "warehouse_name",
        "wa_address1",
        "wa_address2",
        "wa_address3",
        "wa_address4",
        "warehouse_city",
        "warehouse_state_province",
        "warehouse_postal_code",
        "warehouse_country",
        "warehouse_owner_name",
        "warehouse_phone",
        "warehouse_fax");
    consumer.accept("warehouse_class", "description");
  }

  /** Returns the list of base table names in the Foodmart schema. */
  public static List<String> tableNames() {
    List<String> names = new ArrayList<>();
    forEachTable((tableName, quotedColumns) -> names.add(tableName));
    return names;
  }

  /**
   * Converts a table name to a CSV file URI relative to the jar file root.
   *
   * <p>For example, {@code tableUri("customer")} returns {@code
   * "/csv/customer.csv"}.
   */
  public static String tableUri(String tableName) {
    return "/csv/" + tableName.toLowerCase() + ".csv";
  }

  /**
   * Returns the INSERT statements for all Foodmart schema data.
   *
   * <p>Generates statements by reading from the CSV files lazily using an
   * iterator.
   *
   * @return an iterable of INSERT statements
   */
  public static Iterable<String> generateInserts() {
    final Map<String, List<Integer>> columnTypes = new HashMap<>();
    try (Connection c = DriverManager.getConnection(URI, USER, PASSWORD);
        ResultSet x = c.getMetaData().getColumns(null, "foodmart", "%", "%")) {
      while (x.next()) {
        String tableName = x.getString(3);
        int dataType = x.getInt(5);
        columnTypes
            .computeIfAbsent(tableName, t -> new ArrayList<>())
            .add(dataType);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    List<Iterable<String>> iterables = new ArrayList<>();
    forEachTable(
        (tableName, quotedColumns) ->
            iterables.add(
                () ->
                    new StatementGenerator(
                        FoodmartHsqldb.class,
                        tableName,
                        columnTypes.get(tableName))));
    return CompositeIterator.concat(iterables);
  }
}

// End FoodmartHsqldb.java
