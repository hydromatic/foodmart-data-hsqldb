# Foodmart data for hsqldb release history and change log

For a full list of releases, see
<a href="https://github.com/hydromatic/foodmart-data-hsqldb/releases">GitHub</a>.

## <a id="0.7" href="https://github.com/hydromatic/foodmart-data-hsqldb/releases/tag/foodmart-data-hsqldb-0.7">0.7</a> / 2026-09-09

This release converts the aggregate tables and `employee_closure`
into views, and adds a second database in which those views are
materialized. The repository has moved from `julianhyde` to
`hydromatic`; the Maven coordinates are unchanged.

The 11 `agg_*_sales_fact_1997` aggregate tables are now views that
aggregate `sales_fact_1997`, and `employee_closure` is a view that
computes the transitive closure of the employee hierarchy using a
recursive query. The views have the same contents and column types
as the tables they replace, but no indexes. If you query the
aggregate tables often, use the second database,
`jdbc:hsqldb:res:foodmart-mat` (constant
`FoodmartHsqldb.MATERIALIZED_URI`), in which the views are
materialized as indexed memory tables; it takes about 1.5 seconds
longer to open.

The CSV files and `foodmart_csv` text tables for the 12 views have
been removed, which reduces the size of the jar file from about 10MB
to about 4MB. `FoodmartHsqldb.tableNames()` and `generateInserts()`
now cover only the 25 base tables.

* Add a second database, `foodmart-mat`, that materializes the views
  ([#12](https://github.com/hydromatic/foodmart-data-hsqldb/issues/12))
* Convert the aggregate tables and `employee_closure` to views
  ([#11](https://github.com/hydromatic/foodmart-data-hsqldb/issues/11))
* Move repository from `julianhyde` to `hydromatic`
  ([#13](https://github.com/hydromatic/foodmart-data-hsqldb/issues/13))
* Bump `build-helper-maven-plugin` from 3.6.0 to 3.6.1,
  `central-publishing-maven-plugin` from 0.9.0 to 0.11.0,
  `maven-compiler-plugin` from 3.14.0 to 3.15.0,
  `maven-enforcer-plugin` from 3.0.0 to 3.6.3,
  `maven-javadoc-plugin` from 3.6.3 to 3.12.0

## <a id="0.6.1" href="https://github.com/hydromatic/foodmart-data-hsqldb/releases/tag/foodmart-data-hsqldb-0.6.1">0.6.1</a> / 2026-09-09

The previous release had a serious performance problem, and
therefore this is a patch release. We strongly recommend using this
release (0.6.1) rather than 0.6.

Database load was very slow because we were creating indexes on text
tables in a compressed Jar file. So in this revision, we go back to
using memory tables (with indexes) and the text tables are merely the
source of data. The text tables are in a new schema, "foodmart_csv",
but you should only use them for sequential scan. Also, as in 0.6,
the CSV files can be read directly from the jar file.

* Switch back to memory tables (but still load data from CSV files via
  text tables)
* Bump `maven-javadoc-plugin` from 2.9.1 to 3.6.3

## <a id="0.6" href="https://github.com/hydromatic/foodmart-data-hsqldb/releases/tag/foodmart-data-hsqldb-0.6">0.6</a> / 2025-10-18

This release moves the data from an HSQLDB `foodmart.script` file to a
`.csv` file for each table; consumers may now access those files from
the jar directly, if they wish.

Minimum HSQLDB version moves from 2.0.0 to 2.3.0; default HSQLDB
version is now 2.7.4.

* Bump Maven from 3.5.4 to 3.9.11,
  `build-helper-maven-plugin` to 3.6,
  `git-commit-id-plugin` to 4.9.10,
  `maven-compiler-plugin` to 3.14.0,
  `maven-enforcer-plugin` to 3.0,
  `maven-release-plugin` to 2.4.2;
  and add `maven-enforcer-plugin` version 3.0.0
* In Maven, add `central-publishing-maven-plugin`
* Bump HSQLDB from 2.5.1 to 2.7.4
* Add `googleformatter-maven-plugin` and reformat Java code
* Change git clone URL to HTTPS
* Add Javadoc badge to README

## <a id="0.5" href="https://github.com/hydromatic/foodmart-data-hsqldb/releases/tag/foodmart-data-hsqldb-0.5">0.5</a> / 2022-03-08

This release changes the file format from HSQLDB 1.8 to 2.0,
and therefore supports any HSQLDB version 2.0.0 or higher;
to use 2.6.1 and higher you will need Java 11.

* Bump HSQLDB from 2.3.1 to 2.5.1, and change HSQLDB file format from 1.8 to 2.0
* Add a GitHub workflow to build and test
* Add a unit test
* Add Apache Maven wrapper
* Enable Dependabot
* Schema diagram
  ([#1](https://github.com/hydromatic/foodmart-data-hsqldb/issues/1))

## <a id="0.4" href="https://github.com/hydromatic/foodmart-data-hsqldb/releases/tag/foodmart-data-hsqldb-0.4">0.4</a> / 2015-04-07

* Set initial schema to "foodmart", so you can use unqualified table names in SQL

## <a id="0.3" href="https://github.com/hydromatic/foodmart-data-hsqldb/releases/tag/foodmart-data-hsqldb-0.3">0.3</a> / 2015-03-05

* Publish releases to <a href="http://search.maven.org/">Maven Central</a>
* Sign jars
* Create, based upon Pentaho mondrian-data-foodmart-hsqldb version 0.2

