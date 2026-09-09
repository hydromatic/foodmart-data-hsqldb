<!--
{% comment %}
Licensed to Julian Hyde under one or more contributor license
agreements.  See the NOTICE file distributed with this work
for additional information regarding copyright ownership.
Julian Hyde licenses this file to you under the Apache
License, Version 2.0 (the "License"); you may not use this
file except in compliance with the License.  You may obtain a
copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied.  See the License for the specific
language governing permissions and limitations under the
License.
{% endcomment %}
-->
[![Build Status](https://github.com/hydromatic/foodmart-data-hsqldb/actions/workflows/main.yml/badge.svg?branch=main)](https://github.com/hydromatic/foodmart-data-hsqldb/actions?query=branch%3Amain)
[![Maven Central](https://img.shields.io/maven-central/v/net.hydromatic/foodmart-data-hsqldb)](https://central.sonatype.com/artifact/net.hydromatic/foodmart-data-hsqldb)
[![javadoc](https://javadoc.io/badge2/net.hydromatic/foodmart-data-hsqldb/javadoc.svg)](https://javadoc.io/doc/net.hydromatic/foodmart-data-hsqldb)

# foodmart-data-hsqldb
Foodmart data set in hsqldb format

This project contains the FoodMart data set as an embedded
HSQLDB database.

It originated as part of the test suite of the
<a href="https://mondrian.pentaho.org">Pentaho Mondrian OLAP engine</a>.

## Schema

Foodmart contains 25 tables (7 fact tables and 18 dimension tables)
and 12 views (11 summary views and a closure view):

| Name                            | Kind            |    Rows |
|---------------------------------|-----------------|--------:|
| `account`                       | dimension table |      11 |
| `agg_c_10_sales_fact_1997`      | summary view    |      12 |
| `agg_c_14_sales_fact_1997`      | summary view    |  86,805 |
| `agg_c_special_sales_fact_1997` | summary view    |  86,805 |
| `agg_g_ms_pcat_sales_fact_1997` | summary view    |   2,637 |
| `agg_l_03_sales_fact_1997`      | summary view    |  20,522 |
| `agg_l_04_sales_fact_1997`      | summary view    |     323 |
| `agg_l_05_sales_fact_1997`      | summary view    |  86,154 |
| `agg_lc_06_sales_fact_1997`     | summary view    |   4,464 |
| `agg_lc_100_sales_fact_1997`    | summary view    |  86,602 |
| `agg_ll_01_sales_fact_1997`     | summary view    |  86,829 |
| `agg_pl_01_sales_fact_1997`     | summary view    |  86,829 |
| `category`                      | dimension table |       4 |
| `currency`                      | dimension table |      72 |
| `customer`                      | dimension table |  10,281 |
| `days`                          | dimension table |       7 |
| `department`                    | dimension table |      12 |
| `employee`                      | dimension table |   1,155 |
| `employee_closure`              | closure view    |   7,179 |
| `expense_fact`                  | fact table      |   2,400 |
| `inventory_fact_1997`           | fact table      |   4,070 |
| `inventory_fact_1998`           | fact table      |   7,282 |
| `position`                      | dimension table |      18 |
| `product`                       | dimension table |   1,560 |
| `product_class`                 | dimension table |     110 |
| `promotion`                     | dimension table |   1,864 |
| `region`                        | dimension table |     110 |
| `reserve_employee`              | dimension table |     143 |
| `salary`                        | fact table      |  21,252 |
| `sales_fact_1997`               | fact table      |  86,837 |
| `sales_fact_1998`               | fact table      | 164,558 |
| `sales_fact_dec_1998`           | fact table      |  18,325 |
| `store`                         | dimension table |      25 |
| `store_ragged`                  | dimension table |      25 |
| `time_by_day`                   | dimension table |     730 |
| `warehouse`                     | dimension table |      24 |
| `warehouse_class`               | dimension table |       6 |

The summary views aggregate `sales_fact_1997` to various levels of
granularity, and the closure view computes the transitive closure of
the employee hierarchy using a recursive query. Because they are views,
they are recomputed on every query; they can be used to accelerate
queries provided that they are materialized as tables and indexed.
The database at `jdbc:hsqldb:res:foodmart-mat` (see
[below](#materialized-views)) does exactly that.

Its size is about 15MB uncompressed, 4MB compressed.

Here is a schema diagram:

![Foodmart schema diagram](foodmart-schema.png)

## Using the data set

The data set is packaged as a jar file that is published to
[Maven Central](https://search.maven.org/#search%7Cga%7C1%7Ca%3Afoodmart-data-hsqldb)
as a Maven artifact. To use the data in your Java application,
add the artifact to your project's dependencies,
as described [below](#from-maven).

## To connect and read data

Connect to the database using the URL, username and password
constants in the `FoodmartHsqldb` class:

```java
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import net.hydromatic.foodmart.data.hsqldb.FoodmartHsqldb;

Connection connection =
    DriverManager.getConnection(FoodmartHsqldb.URI,
        FoodmartHsqldb.USER, FoodmartHsqldb.PASSWORD);
Statement statement = connection.createStatement();
ResultSet resultSet =
    statement.executeQuery("select \"employee_id\", \"full_name\"\n"
        + "from \"foodmart\".\"employee\"");
while (resultSet.next()) {
  System.out.println(resultSet.getInt(1) + ":" + resultSet.getString(2));
}
resultSet.close();
statement.close();
connection.close();
```

## Materialized views

The jar contains a second database, `jdbc:hsqldb:res:foodmart-mat`
(constant `FoodmartHsqldb.MATERIALIZED_URI`), that is identical to
the first except that the summary views and the closure view are
materialized as memory tables, with the same indexes as the original
tables. Queries that use the aggregate tables are much faster
(a lookup by `customer_id` in `agg_c_14_sales_fact_1997` takes about
0.2 ms rather than 150 ms), at the cost of computing the tables when
the database is opened (about 2.6 seconds rather than 1.1 seconds).
The data (the CSV files) is shared between the two databases, so the
second database adds only a few kilobytes to the jar.

## Using SQLLine

You can also connect using a JDBC interface such as
[sqlline](https://github.com/julianhyde/sqlline).  Start `sqlline`:

```sql
$ ./sqlline
sqlline version 1.12.0
sqlline> !connect jdbc:hsqldb:res:foodmart FOODMART FOODMART
0: jdbc:hsqldb:res:foodmart> select count(*) from "sales_fact_1997";
+----------------------+
|          C1          |
+----------------------+
| 86837                |
+----------------------+
1 row selected (0.004 seconds)
0: jdbc:hsqldb:res:foodmart> !quit
```

You may need to edit the `sqlline` or `sqlline.bat` launcher script,
adding `foodmart-data-hsqldb.jar` to your class path.

## Get foodmart-data-hsqldb

### From Maven

Get foodmart-data-hsqldb from
<a href="https://search.maven.org/#search%7Cga%7C1%7Cg%3Anet.hydromatic%20a%3Afoodmart-data-hsqldb">Maven Central</a>:

```xml
<dependency>
  <groupId>net.hydromatic</groupId>
  <artifactId>foodmart-data-hsqldb</artifactId>
  <version>0.6.1</version>
</dependency>
```

### Download and build

Use Java version 11 or higher.

```bash
$ git clone https://github.com/hydromatic/foodmart-data-hsqldb.git
$ cd foodmart-data-hsqldb
$ ./mvnw install
```

On Windows, the last line is

```bash
> mvnw install
```

If you are using Java 8, you should add a parameter
`-Dhsqldb.version=2.5.1`, because HSQLDB 2.6.0 or higher
requires at least JDK 11.

## See also

Similar data sets:
* [chinook-data-hsqldb](https://github.com/julianhyde/chinook-data-hsqldb)
* [flight-data-hsqldb](https://github.com/julianhyde/flight-data-hsqldb)
* [foodmart-data](https://github.com/julianhyde/foodmart-data)
  (the same data set as CSV files, for Go and Rust)
* [foodmart-data-json](https://github.com/julianhyde/foodmart-data-json)
* [foodmart-data-mysql](https://github.com/julianhyde/foodmart-data-mysql)
* [foodmart-queries](https://github.com/julianhyde/foodmart-queries)
* [look-data-hsqldb](https://github.com/hydromatic/look-data-hsqldb)
* [sakila-data-hsqldb](https://github.com/hydromatic/sakila-data-hsqldb)
* [scott-data-hsqldb](https://github.com/julianhyde/scott-data-hsqldb)
* [steelwheels-data-hsqldb](https://github.com/julianhyde/steelwheels-data-hsqldb)

## More information

* **License:** Apache License, Version 2.0
* **Author:** [Julian Hyde](https://github.com/julianhyde)
  ([@julianhyde](https://twitter.com/julianhyde))
* **Blog:** http://blog.hydromatic.net
* **Source code:** https://github.com/hydromatic/foodmart-data-hsqldb
* **Developers list:**
  [dev@calcite.apache.org](mailto:dev@calcite.apache.org)
  ([archive](https://mail-archives.apache.org/mod_mbox/calcite-dev/),
  [subscribe](mailto:dev-subscribe@calcite.apache.org))
* **Issues:** https://github.com/hydromatic/foodmart-data-hsqldb/issues
* **Release notes:** [HISTORY.md](HISTORY.md)
