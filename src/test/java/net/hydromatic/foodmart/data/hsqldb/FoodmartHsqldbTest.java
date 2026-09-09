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

import static java.lang.String.join;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.Matcher;
import org.junit.Test;

/** Kick the tires. */
public class FoodmartHsqldbTest {
  @Test
  public void test() throws SQLException {
    final Connection connection =
        DriverManager.getConnection(
            FoodmartHsqldb.URI, FoodmartHsqldb.USER, FoodmartHsqldb.PASSWORD);
    final Statement statement = connection.createStatement();
    foo(statement.executeQuery("select * from \"days\""), -1, is(7));
    foo(statement.executeQuery("select * from \"product\""), 1, is(1560));
    statement.close();
    connection.close();
  }

  @Test
  public void testTableNames() {
    final List<String> tableNames = FoodmartHsqldb.tableNames();
    assertEquals(25, tableNames.size());
    assertEquals("account", tableNames.get(0));
    assertEquals("category", tableNames.get(1));
    assertEquals("warehouse_class", tableNames.get(24));
  }

  @Test
  public void testTableUri() {
    List<String> uris =
        FoodmartHsqldb.tableNames().stream()
            .map(FoodmartHsqldb::tableUri)
            .collect(Collectors.toList());
    assertEquals(25, uris.size());
    assertEquals("/csv/account.csv", uris.get(0));
    assertEquals("/csv/category.csv", uris.get(1));
    assertEquals("/csv/warehouse_class.csv", uris.get(24));
  }

  @Test
  public void testRowCounts() throws SQLException {
    final Connection connection =
        DriverManager.getConnection(
            FoodmartHsqldb.URI, FoodmartHsqldb.USER, FoodmartHsqldb.PASSWORD);
    final Statement statement = connection.createStatement();

    // Expected row counts for each table and view
    checkRowCount(statement, "account", 11);
    checkRowCount(statement, "agg_c_10_sales_fact_1997", 12);
    checkRowCount(statement, "agg_c_14_sales_fact_1997", 86805);
    checkRowCount(statement, "agg_c_special_sales_fact_1997", 86805);
    checkRowCount(statement, "agg_g_ms_pcat_sales_fact_1997", 2637);
    checkRowCount(statement, "agg_l_03_sales_fact_1997", 20522);
    checkRowCount(statement, "agg_l_04_sales_fact_1997", 323);
    checkRowCount(statement, "agg_l_05_sales_fact_1997", 86154);
    checkRowCount(statement, "agg_lc_06_sales_fact_1997", 4464);
    checkRowCount(statement, "agg_lc_100_sales_fact_1997", 86602);
    checkRowCount(statement, "agg_ll_01_sales_fact_1997", 86829);
    checkRowCount(statement, "agg_pl_01_sales_fact_1997", 86829);
    checkRowCount(statement, "category", 4);
    checkRowCount(statement, "currency", 72);
    checkRowCount(statement, "customer", 10281);
    checkRowCount(statement, "days", 7);
    checkRowCount(statement, "department", 12);
    checkRowCount(statement, "employee", 1155);
    checkRowCount(statement, "employee_closure", 7179);
    checkRowCount(statement, "expense_fact", 2400);
    checkRowCount(statement, "inventory_fact_1997", 4070);
    checkRowCount(statement, "inventory_fact_1998", 7282);
    checkRowCount(statement, "position", 18);
    checkRowCount(statement, "product", 1560);
    checkRowCount(statement, "product_class", 110);
    checkRowCount(statement, "promotion", 1864);
    checkRowCount(statement, "region", 110);
    checkRowCount(statement, "reserve_employee", 143);
    checkRowCount(statement, "salary", 21252);
    checkRowCount(statement, "sales_fact_1997", 86837);
    checkRowCount(statement, "sales_fact_1998", 164558);
    checkRowCount(statement, "sales_fact_dec_1998", 18325);
    checkRowCount(statement, "store", 25);
    checkRowCount(statement, "store_ragged", 25);
    checkRowCount(statement, "time_by_day", 730);
    checkRowCount(statement, "warehouse", 24);
    checkRowCount(statement, "warehouse_class", 6);

    statement.close();
    connection.close();
  }

  @Test
  public void testGenerateInserts() throws IOException {
    final Iterable<String> statements = FoodmartHsqldb.generateInserts();
    int count = 0;
    int accountCount = 0;
    int customerCount = 0;
    int salesCount = 0;
    boolean foundWarehouse17 = false;
    boolean foundTimeByDay738 = false;
    boolean foundStoreRagged1 = false;

    for (String stmt : statements) {
      count++;
      // Verify each statement is an INSERT
      assertEquals(
          "Statement should start with INSERT INTO",
          true,
          stmt.startsWith("INSERT INTO"));

      // Count statements by table
      if (stmt.contains("\"account\"")) {
        accountCount++;
      } else if (stmt.contains("\"customer\"")) {
        customerCount++;
      } else if (stmt.contains("\"sales_fact_1997\"")) {
        salesCount++;
      }
      if (stmt.equals(
          "INSERT INTO \"warehouse\" VALUES(17,4,17,'Jorge Garcia, Inc.',"
              + "'4364 Viera Avenue',NULL,NULL,NULL,'Tacoma','WA','55555','USA',"
              + "NULL,'200-555-1310','442-555-5874')")) {
        foundWarehouse17 = true;
      }
      if (stmt.equals(
          "INSERT INTO \"time_by_day\" VALUES(738,'1998-01-07 00:00:00.0',"
              + "'Wednesday','January',1998,7,4,1,'Q1',NULL)")) {
        foundTimeByDay738 = true;
      }
      if (stmt.equals(
          "INSERT INTO \"store_ragged\" VALUES(1,'Supermarket',28,'Store 1',1,'2853 "
              + "Bailey Rd','Acapulco','Guerrero','55555','Mexico','Jones','262-555-5124',"
              + "'262-555-5121','1982-01-09 00:00:00.0','1990-12-05 00:00:00.0',23593,17475,3671,"
              + "2447,FALSE,FALSE,FALSE,FALSE,FALSE)")) {
        foundStoreRagged1 = true;
      }
    }

    // Verify total count and counts for some tables
    // Views (the aggregate tables and employee_closure) have no CSV file
    // and therefore generate no INSERT statements.
    int expectedCount =
        11 + 4 + 72 + 10281 + 7 + 12 + 1155 + 2400 + 4070 + 7282 + 18 + 1560
            + 110 + 1864 + 110 + 143 + 21252 + 86837 + 164558 + 18325 + 25 + 25
            + 730 + 24 + 6;
    assertEquals(expectedCount, count);
    assertEquals(11, accountCount);
    assertEquals(10281, customerCount);
    assertEquals(86837, salesCount);

    // Verify specific tricky case: comma in name and multiple NULLs
    assertTrue("Should contain warehouse row 17", foundWarehouse17);
    assertTrue("Should contain store_ragged row 1", foundStoreRagged1);
    assertTrue("Should contain time_by_day row 738", foundTimeByDay738);
  }

  private void checkRowCount(
      Statement statement, String tableName, int expectedCount)
      throws SQLException {
    ResultSet rs =
        statement.executeQuery(
            "select count(*) from \"foodmart\".\"" + tableName + "\"");
    rs.next();
    int actualCount = rs.getInt(1);
    rs.close();
    assertThat(
        "Row count for table " + tableName, actualCount, is(expectedCount));
  }

  /** Tests {@link CompositeIterator#concat}. */
  @Test
  public void testMultipleIterables() {
    List<String> empty = Collections.emptyList();
    List<String> ab = Arrays.asList("a", "b");
    List<String> cde = Arrays.asList("c", "d", "e");
    List<String> f = Collections.singletonList("f");

    assertConcat("a,b,c,d,e,f", ab, cde, f);
    assertConcat("a,b,c,d,e,f", empty, ab, cde, f);
    assertConcat("a,b,c,d,e,f", ab, empty, cde, f);
    assertConcat("", empty, empty);
    assertConcat("", empty);
    assertConcat("f", empty, f);
  }

  @SafeVarargs
  private static void assertConcat(String expected, List<String>... lists) {
    Iterable<String> composite = CompositeIterator.concat(Arrays.asList(lists));
    List<String> result = new ArrayList<>();
    for (String s : composite) {
      result.add(s);
    }
    assertEquals(expected, join(",", result));
  }

  private void foo(
      ResultSet resultSet, int printLimit, Matcher<Integer> rowCountMatcher)
      throws SQLException {
    final ResultSetMetaData metaData = resultSet.getMetaData();
    final int columnCount = metaData.getColumnCount();
    int row = 0;
    while (resultSet.next()) {
      if (row++ >= printLimit && printLimit >= 0) {
        continue;
      }
      for (int i = 0; i < columnCount; i++) {
        if (i > 0) {
          System.out.print(", ");
        }
        System.out.print(metaData.getColumnLabel(i + 1));
        System.out.print(": ");
        System.out.print(resultSet.getObject(i + 1));
      }
      System.out.println();
    }
    assertThat(row, rowCountMatcher);
  }
}

// End FoodmartHsqldbTest.java
