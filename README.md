# PedrozaSQLite
Tool developed by Carlos Pedroza to access the application's SQlite database based on [SQLite Android Developer's Reference]

## Background

I know there is already a few extensions with this functionality, but here are the reasons why I made it:

* First of all, **thanks to AppInventor and all its distributions I took my first steps on how to code**. Later, I thought about creating extensions and I found that I really like Java and the whole process of thinking and trying to run it correctly. So I took my chances and made my first extension that I published it [here in Thunkable (Polyline Tools)](https://community.thunkable.com/t/new-update-polylinetools-extension/7211?u=carlos_pedroza).
* I knew that I wanted more, and decided to make one of the extensions I always wanted to have but couldn't afford it.
* My goal is to let other people like me use this  extension, but always remembering the true meaning of AppInventor that is learning how to code. So if you feel like you can take the next step, I encourage you to try it. It's really fun!

So this is it! I hope you find this useful and if you find how to make it better, please let me know so I can learn from it!

For reference on how to write SQLite statements, I recommend for a good quick start [TutorialPoint.com](https://www.tutorialspoint.com/sqlite/) and [SoloLearn's SQL Fundamentals](https://www.sololearn.com/Course/SQL/).

For other options availables:

* Taifun's SQLite Extension - [Link](http://puravidaapps.com/sqlite.php) (since Aug 11th, 2016).
* Andrés Cotes' SQLite Extension - [Link](https://community.thunkable.com/t/sqlite-extension-paid/8496?u=carlos_pedroza) (since Oct 2nd, 2017).
* Juan Ruvalcaba's SQLite Extension - [Link](http://mitextensions.esy.es/) (since September 8th, 2017).

## Features

* SQL Statements executes asynchronously, very useful in large databases.
* Good practices to avoid as possible SQL Injections.
* Opportunity to use rawQuery and write SQL statements.
* Compile a statement to reuse it later (on next release).
* Query result in a list for easy use.

**DISADVANTAGES**

* Not able to export and import databases as a file .sqlite (I don't know how to do it, yet).

----------


## Methods
* **AfterExecution**: Event handler after the SQL statement is executed, **returns** whether the execution was succesfully executed.
* **AfterQuery**: Event handler after the ExecuteRawQuery or Query is executed and **returns** a list with the selected data and number of records.
* **ErrorOccurred**: Event handler when an error occurred, **returns** a string with a message from the error.
* **Query**: Executes pre-compiled QUERY statement with specified parameters. **Parameters**: 1) _String table_: Name of the table. 2) _YailList columns_: List of which columns to return, passing an empty list will return all columns. 3) _String selection_: Filter declaring which rows to return, formatted as an SQL WHERE clause, passing an empty string will return all rows. 4) _YailList selectionArgs_: List with the arguments that will replace onto '?' in the selection filter. 5) _String groupBy_: A filter declaring how to group rows, formatted as an SQL GROUP BY clause (excluding the GROUP BY itself), passing an empty string will cause the row to not be grouped. 6) _String having_: A filter declare which row groups to include if row grouping is being used, passing an empty string will cause all row groups to be included. 7) _String orderBy_: How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself), passing an empty string will use the default sort order (unordered). 8) _String limit_: Limits the number of rows returned by the query, formatted as LIMIT clause, passing an empty string denotes no LIMIT clause. **Return**: The result query is available in the AfterQuery event handler.
* **Insert**: Executes pre-compiled INSERT statement with specified parameters. **Parameters**: 1) _String table_: Name of the table. 2) _YailList columns_: List with the columns that will contain the data to be inserted in the database. 3) _YailList values_: List with the data to be inserted in the database. **Returns** the row ID of the newly inserted row, or -1 if an error occurred.
* **Replace**: Executes pre-compiled REPLACE OR INSERT INTO statement with specified parameters. **Parameters**: 1) _String table_: Name of the table. 2) _YailList columns_: List with the columns that will contain the data to be replaced in the database. 3) _YailList values_ List with the data to be replaced in the database. **Returns** the row ID of the newly replaced row, or -1 if an error occurred.
* **Update**: "Executes pre-compiled UPDATE statement with specified parameters. **Parameters**: 1) _String table_: Name of the table. 2) _YailList columns_ List with the columns that will contain the data to be inserted in the database. 3) _YailList values_ List with the data to be inserted in the database. 4) _String whereClause_: optional WHERE clause to apply when updating, leave an empty string to update all rows. Include ?s, which will be updated by the values from whereArgs. 5) _YailList whereArgs_: List with the columns that will contain the data to be updated in the database. **Returns** the row ID of the newly inserted row, or -1 if an error occurred.
* **Delete**: Executes pre-compiled DELETE statement with specified parameters. **Parameters**: 1) _String table_: Name of the table. 2) _String whereClause_: Optional WHERE clause to apply when deleting (Example: 'ID = ?'), pasing an empty a string will delete all rows. 3) _List whereArgs_: List with arguments for the WHERE clause. These arguments will be replaced by '?' in the whereClause. **Returns** the number of rows affected if a whereClause is passed in, 0 otherwise.
* **SingleSQL**: Execute a Single SQL Statement asynchronously and returns whether the transaction was succesful in the AfterExecution Event Handler. Use it when returned data isn't needed. **Parameters**: _String sql_.
* **MultipleSQL**: Execute Multiple SQL Statement asynchronously and returns whether the transaction was succesful in the AfterExecution Event Handler. Use it when returned data isn't needed. **Parameters**: _List of SQL statements_.
* **RawQuery**: Executes the provided rawQuery Statement asynchronously. Returns a YailList with the selected data and number of records in the AfterQuery Event. **Parameters**: 1)_String SQL statement_. 2) _List selectionArgs_: List with the arguments that will replace '?' in where clause in the query, to prevent SQL injections.
* **GetPath**: Returns the path to the database.
* **ClearDatabase**: Clears the database to version 1. Use only while developing, this shouldn't be used on production.
* **ReturnHeader**: Returns or specifies whether the header row should be returned in the result of a Select statement.
* **SupressToast**: Returns or specifies whether Success Toast should be suppressed.

## Best Practices

In order to avoid SQL Injections and use pre-compiled statements, it is a good practice to use this blocks to SELECT, INSERT, UPDATE, REPLACE and DELETE.
On the other hand, these block are useful for executing a single SQL statement that is NOT a SELECT or any other SQL statement that returns data (INSERT, UPDATE, REPLACE or DELETE). It has no means to return any data (such as the number of affected rows). Statements like CREATE or DROP table / trigger / view / index / virtual table, ALTER TABLE, REINDEX, RELEASE, SAVEPOINT and PRAGMA are very good options to use these block for.
When using rawQuery (using it a lot will cause performance issues because it compiles it over and over again) use the selectionArgs to provide the arguments that will replace '?' in the where clause to prevent SQL injections. For example: `SELECT * FROM table_name WHERE ID=?`, the arguments in selectionArgs will replace all `?`.

This extension has been sponsored by Peter Mathijssen and Diego Marino, so now it's free.

## Changelog
* October 9, 2017 - Initial release.
