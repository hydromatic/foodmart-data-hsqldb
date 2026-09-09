# Foodmart data for hsqldb release history and change log

For a full list of releases, see
<a href="https://github.com/hydromatic/foodmart-data-hsqldb/releases">GitHub</a>.

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
* [[FDH-1](https://github.com/hydromatic/foodmart-data-hsqldb/issues/1)] Schema diagram

## <a id="0.4" href="https://github.com/hydromatic/foodmart-data-hsqldb/releases/tag/foodmart-data-hsqldb-0.4">0.4</a> / 2015-04-07

* Set initial schema to "foodmart", so you can use unqualified table names in SQL

## <a id="0.3" href="https://github.com/hydromatic/foodmart-data-hsqldb/releases/tag/foodmart-data-hsqldb-0.3">0.3</a> / 2015-03-05

* Publish releases to <a href="http://search.maven.org/">Maven Central</a>
* Sign jars
* Create, based upon Pentaho mondrian-data-foodmart-hsqldb version 0.2

