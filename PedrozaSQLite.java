package com.pedroza.PedrozaSQLite;

import android.content.Context;
import android.database.Cursor;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteProgram;
import android.database.SQLException;
import android.os.AsyncTask;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.collect.Sets;
import com.google.appinventor.components.runtime.util.BoundingBox;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.YailList;

import android.widget.Toast;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



/**Welcome to my second extension ever created.
* This extension provides access to the application SQlite database.
* @author Carlos Pedroza
*/

@DesignerComponent(
	version = 1,
	description = "Tool developed by Carlos Pedroza to access the application's SQlite database.",
	category = ComponentCategory.EXTENSION,
	nonVisible = true,
	iconName = "aiwebres/Untitled.png")

@SimpleObject(external = true)
public class PedrozaSQLite extends AndroidNonvisibleComponent implements Component {
	private ComponentContainer container;
	private Context context;
	private static final String LOG_TAG = "PedrozaSQLite Extension";
	public static final int VERSION = 1;
	
	private boolean suppressToast;
	private boolean returnHeader;
	
	private static String DB_NAME = "DATABASE.sqlite";
	private int DB_SCHEME_VERSION = 1;
	private SQLiteDatabase db;
	
	//These variables provides easy access when executing SQL Statements asynchronously
	private String SingleSQLStatement;
	private String[] MultipleSQLStatement;
	private boolean singleStatement;
	private String[] selectionArguments;
	
	//Very simple SQLiteHelper in order to help create and upgrade the database.
	private class DbHelper extends SQLiteOpenHelper {
		public DbHelper(Context context){
			super(context, DB_NAME, null, DB_SCHEME_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db){
			if (!suppressToast) { Toast.makeText(context, "Database created", Toast.LENGTH_SHORT).show(); }
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
			if (!suppressToast) { Toast.makeText(context, "Database upgraded", Toast.LENGTH_SHORT).show(); }
		}
	
	}
	
	//Class to execute SQL statements asynchronously
	private class ExecuteSQL extends AsyncTask<Void,Void,Void> {
		boolean wasSuccesful;
		
		@Override
		protected Void doInBackground(Void... voids){
			DbHelper helper = new DbHelper(context);
			SQLiteDatabase db = helper.getWritableDatabase();
			db.beginTransaction();
			try {
				
				if (singleStatement) {
					db.execSQL(SingleSQLStatement);				// Executing only one statement
				} else {
					for(int x = 0; x < MultipleSQLStatement.length; x++) {
						db.execSQL(MultipleSQLStatement[x]);	// Executing multiple statements
						}
				}
				wasSuccesful = true;
				db.setTransactionSuccessful();
			} catch (SQLException e) {
				//If there is an invalid SQL statement, this flag
				//helps with calling the ErrorOcurred event handler
				wasSuccesful = false;
			} finally {
				db.endTransaction();
			}
			db.close();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void aVoid){
			if (!suppressToast) { Toast.makeText(context,"Done",Toast.LENGTH_SHORT).show(); }
			if (wasSuccesful) {
				AfterExecution(wasSuccesful);	//In the AfterExecution event handler returns true
			} else {
				AfterExecution(wasSuccesful);	//In the AfterExecution event handler returns false
				ErrorOcurred("Invalid SQL Statement");	//Calls the ErrorOcurred event handler
			}
					
		}
	}
	
	//Class to execute a SQL rawQuery asynchronously
	private class RunQuery extends AsyncTask<Void,Void,Void> {
		List queryResult;
		int records;
		boolean wasSuccesful;
		
		@Override
		protected Void doInBackground(Void... voids){
			DbHelper helper = new DbHelper(context);
			SQLiteDatabase db = helper.getReadableDatabase();
			db.beginTransaction();
			try {
				Cursor cursor = db.rawQuery(SingleSQLStatement, selectionArguments);
				queryResult = CursorManagement(cursor); //Translate Cursor into an ArrayList
				records = queryResult.size();
				wasSuccesful = true;	//Using this flag to know if the Transaction was succesful in onPostExecute()
				db.setTransactionSuccessful();
			} catch (SQLException e) {
				wasSuccesful = false;	//Using this flag to know if the Transaction wasn't succesful in onPostExecute()
			} finally {
				db.endTransaction();
			}
			db.close();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void aVoid){
			if (!suppressToast) { Toast.makeText(context,"Done",Toast.LENGTH_SHORT).show(); }
			if (wasSuccesful) {
				AfterQuery (queryResult, records);	//In the event handler, it returns a YailList and the number of records
			} else {
				ErrorOcurred("Invalid SQL Statement");
			}
		}
	}
	
	//Function to translate Cursor into ArrayList
	private List CursorManagement(Cursor c){
		String[] columnNames;
		YailList result;
		List queryResult  = new ArrayList<String>();
		boolean wasSuccesful;
		
		try {
		columnNames = c.getColumnNames();
		//If the cursor has more than one column this uses an ArrayList<ArrayList<String>>
		if (columnNames.length > 1){
			queryResult = new ArrayList<ArrayList<String>>();
			if (returnHeader){
				ArrayList<String> columns = new ArrayList<String>();
					for(int x = 0; x < columnNames.length; x++) {
						columns.add(columnNames[x]);
					}
				queryResult.add(columns);
			}
			try {
				while (c.moveToNext()) {
					ArrayList<String> ROW = new ArrayList<String>();
					for(int x = 0; x < columnNames.length; x++) {
						ROW.add(c.getString(x));
					}
					queryResult.add(ROW);
				}
			} finally {
				c.close();
			}
		} else {
		//If the c has only one column this uses an ArrayList<String>
			queryResult = new ArrayList<String>();
			if (returnHeader){
				queryResult.add(columnNames[0]);
			}
			try {
				while (c.moveToNext()) {
					queryResult.add(c.getString(0));
				}
			} finally {
				c.close();
			}
		}
		return queryResult;
		} catch (SQLException e) {
		ErrorOcurred("Error during managing the cursor");
		return queryResult;
	}
	}
	
	public PedrozaSQLite (ComponentContainer container) {
		super(container.$form());
		this.container = container;
		context = (Context) container.$context();
		Log.d(LOG_TAG, "SQLite created");
	}
	
	/**
	* Returns whether the header row should be returned in the result of a Select statement.
	*/
	@SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Returns whether the header row should be returned in the result of a Select statement.")
	public boolean ReturnHeader() { return returnHeader; }
  
	/**
	* Specifies whether the header row should be returned in the result of a Select statement.
	*/
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
		defaultValue = "false")
	@SimpleProperty
	public void ReturnHeader(boolean returnHeader) { this.returnHeader = returnHeader; }
	
	/**
	* Returns whether Success Toast should be suppressed.
	*/
	@SimpleProperty(category = PropertyCategory.BEHAVIOR,
		description = "Returns whether Success Toast should be suppressed.")
	public boolean SuppressToast() { return suppressToast; }
  
	/**
	* Specifies whether Success Toast should be suppressed.
	*/
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
		defaultValue = "false")
	@SimpleProperty
	public void SuppressToast(boolean suppressToast) { this.suppressToast = suppressToast; }
	
	/**
	* Returns the path to the database.
	*/
	@SimpleFunction(description = "Returns the path to the database")
	public String GetPath() {
		DbHelper helper = new DbHelper(context);
		db = helper.getReadableDatabase();
		return db.getPath();
	}
	
		
	/**
	* Clears the database to version 1. Use only while developing, this shouldn't be used on production.
	*/
	@SimpleFunction(description = "Clears the database to version 1. Use only while developing, this shouldn't be use on production.")
	public void ClearDatabase() {
		context.deleteDatabase(DB_NAME);
		Toast.makeText(context, "Database cleared", Toast.LENGTH_SHORT).show();
	}
	
	/**
	* Execute a Single SQL Statement asynchronously and returns whether the transaction was succesful in the AfterExecution Event Handler.
	* @param sql: The SQL statement
	*/
	@SimpleFunction(description = "Execute a Single SQL Statement asynchronously and returns"
	+ " whether the transaction was succesful in the AfterExecution Event Handler."
	+ " Use it when returned data isn't needed. Parameter: 1) String sql.")
	public void SingleSQL (String sql) {
		SingleSQLStatement = sql;
		singleStatement = true;
		new ExecuteSQL().execute();
	}

	/**
	* Execute Multiple SQL Statement asynchronously and returns whether the transaction was succesful in the AfterExecution Event Handler.
	* @param YailList: List with SQL Statement 
	*/
	@SimpleFunction(description = "Execute Multiple SQL Statement asynchronously and returns"
	+ " whether the transaction was succesful in the AfterExecution Event Handler."
	+ " Use it when returned data isn't needed. Parameter: 1 ) List of SQL.")
	public void MultipleSQL (YailList list) {
		MultipleSQLStatement = list.toStringArray();
		singleStatement = false;
		new ExecuteSQL().execute();
	}

	/**
	* Event handler after the SQL statement is executed, returns whether the execution was succesful.
	*/
	@SimpleEvent
	public void AfterExecution(boolean wasExecuted) {
		EventDispatcher.dispatchEvent(this, "AfterExecution", wasExecuted);
	}
	
	
	/**
	* Executes the provided rawQuery Statement asynchronously and returns a YailList with the selected data and number of records in the AfterQuery Event.
	* @param String sql: The SQL statement
	*/
	@SimpleFunction(description = "Executes the provided rawQuery Statement asynchronously."
	+ " Returns a YailList with the selected data and number of records in the AfterQuery Event."
	+ " Parameter: 1) String sql. 2) YailList selectionArgs: List with the arguments that will replace '?' in where clause in the query, to prevent SQL injections")
	public void RawQuery (String sql, YailList selectionArgs) {
		selectionArguments = selectionArgs.toStringArray();
		SingleSQLStatement = sql;
		new RunQuery().execute();
	}
	
	/**
	* Event handler after the ExecuteRawQuery or Query is executed and returns a list with the selected data and number of records.
	*/
	@SimpleEvent
	public void AfterQuery(List result, int numberOfRecords) {
		EventDispatcher.dispatchEvent(this, "AfterQuery", result, numberOfRecords);
	}
		
	/**
	* Executes pre-compiled DELETE statement with specified parameters.
	* @param String table: Name of the table.
	* @param String whereClause: the optional WHERE clause to apply when deleting. Passing null will delete all rows.
	* @param YailList whereArgs: List with arguments for the WHERE clause.
	* @return the number of rows affected if a whereClause is passed in, 0 otherwise. To remove all rows and get a count pass "1" as the whereClause. 
	*/
	@SimpleFunction(description = "Executes pre-compiled DELETE statement with specified parameters."
	+ " Parameters: 1) String table - Name of the table. 2) String whereClause - Optional WHERE clause to apply when deleting (Example:"
	+ " 'ID = ?'), pasing an empty a string will delete all rows."
	+ " 3) List whereArgs - List with arguments for the WHERE clause. These arguments will be replaced by '?' in the whereClause."
	+ " Returns the number of rows affected if a whereClause is passed in, 0 otherwise.")
	public int Delete(String table, String whereClause, YailList whereArgs) {
		DbHelper helper = new DbHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();
		String[] deleteWhereArgs = whereArgs.toStringArray();
		int result = -1;
		whereClause = (whereClause == "" ? null : whereClause);
			
		try {
			result = db.delete(table, whereClause, deleteWhereArgs);
		} catch (SQLException e) {
			ErrorOcurred("Something went wrong deleting");
		}
		db.close();
		return result;
	}
	
	/**
	* Executes pre-compiled INSERT statement with specified parameters.
	* @param String table: Name of the table.
	* @param YailList columns: List with the columns that will contain the data to be inserted in the database.
	* @param YailList values: List with the data to be inserted in the database.
	* @return the row ID of the newly inserted row, or -1 if an error occurred.
	*/
	@SimpleFunction(description = "Executes pre-compiled INSERT statement with specified parameters."
	+ " Parameters: 1) String table - Name of the table."
	+ " 2) YailList columns - List with the columns that will contain the data to be inserted in the database."
	+ " 3) YailList values - List with the data to be inserted in the database."
	+ " Returns the row ID of the newly inserted row, or -1 if an error occurred.")
	public long Insert(String table, YailList columns, YailList values) {
		DbHelper helper = new DbHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();
		long result = -1;
		String[] insertColumns = columns.toStringArray();
		String[] insertValues = values.toStringArray();
		ContentValues Values = new ContentValues();
		for (int i = 0; i < insertColumns.length; i++) {
			Values.put(insertColumns[i],insertValues[i]);
		}
		try {
			result = db.insert(table, null, Values);
		} catch (SQLException e) {
			ErrorOcurred("Something went wrong inserting");
		}
		db.close();
		return result;
	}
	
	/**
	* Executes pre-compiled REPLACE OR INSERT INTO statement with specified parameters.
	* @param String table: Name of the table.
	* @param YailList columns: List with the columns that will contain the data to be replaced in the database.
	* @param YailList values: List with the data to be replaced in the database.
	* @return the row ID of the newly replaced row, or -1 if an error occurred.
	*/
	@SimpleFunction(description = "Executes pre-compiled REPLACE OR INSERT INTO statement with specified parameters."
	+ " Parameters: 1) String table - Name of the table."
	+ " 2) YailList columns - List with the columns that will contain the data to be replaced in the database."
	+ " 3) YailList values - List with the data to be replaced in the database."
	+ " Returns the row ID of the newly replaced row, or -1 if an error occurred.")
	public long Replace(String table, YailList columns, YailList values) {
		DbHelper helper = new DbHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();
		long result = -1;
		String[] replaceColumns = columns.toStringArray();
		String[] replaceValues = values.toStringArray();
		ContentValues Values = new ContentValues();
		for (int i = 0; i < replaceColumns.length; i++) {
			Values.put(replaceColumns[i],replaceValues[i]);
		}
		try {
			result = db.replace(table, null, Values);
		} catch (SQLException e) {
			ErrorOcurred("Something went wrong replacing");
		}
		db.close();
		return result;
	}
	
	/**
	* Executes pre-compiled UPDATE statement with specified parameters.
	* @param String table: Name of the table.
	* @param YailList columns: List with the columns that will contain the data to be updated in the database.
	* @param YailList values: List with the data to be updated in the database.
	* @param String whereClause: the optional WHERE clause to apply when updating, leave an empty string to update all rows. Include ?s, which will be updated by the values from whereArgs.
	* @param YailList whereArgs: List with the columns that will contain the data to be updated in the database.
	* @return the number of rows affected.
	*/
	@SimpleFunction(description = "Executes pre-compiled UPDATE statement with specified parameters."
	+ " Parameters: 1) String table - Name of the table."
	+ " 2) YailList columns - List with the columns that will contain the data to be inserted in the database."
	+ " 3) YailList values - List with the data to be inserted in the database."
	+ " 4) String whereClause - optional WHERE clause to apply when updating, leave an empty string to update all rows. Include ?s, which will be updated by the values from whereArgs."
	+ " 5) YailList whereArgs - List with the columns that will contain the data to be updated in the database."
	+ " Returns the row ID of the newly inserted row, or -1 if an error occurred.")
	public int Update(String table, YailList columns, YailList values, String whereClause, YailList whereArgs) {
		DbHelper helper = new DbHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();
		String[] updateColumns = columns.toStringArray();
		String[] updateValues = values.toStringArray();
		ContentValues Values = new ContentValues();
		int result = -1;
		
		for (int i = 0; i < updateColumns.length; i++) {
			Values.put(updateColumns[i],updateValues[i]);
		}
		String[] updateWhereArgs = whereArgs.toStringArray();
		whereClause = (whereClause == "" ? null : whereClause);
		try {
			result = db.update(table, Values, whereClause, updateWhereArgs);
		} catch (SQLException e) {
			ErrorOcurred("Something went wrong updating");
		}
		db.close();
		return result;
	}
	
	/**
	* Executes pre-compiled QUERY statement with specified parameters.
	* @param String table: Name of the table.
	* @param YailList columns: List of which columns to return, passing an empty list will return all columns.
	* @param String selection: Filter declaring which rows to return, formatted as an SQL WHERE clause, passing an empty string.
	* @param YailList selectionArgs: List with the arguments that will replace onto "?" in the selection filter.
	* @param String groupBy: A filter declaring how to group rows, formatted as an SQL GROUP BY clause (excluding the GROUP BY itself), passing an empty string will cause the row to not be grouped.
	* @param String having: A filter declare which row groups to include if row grouping is being used, passing an empty string will cause all row groups to be included.
	* @param String orderBy: How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself), passing an empty string will use the default sort order (unordered).
	* @param String limit: Limits the number of rows returned by the query, formatted as LIMIT clause, passing an empty string denotes no LIMIT clause.
	*/
	@SimpleFunction(description = "Executes pre-compiled QUERY statement with specified parameters."
	+ " Parameters: 1) String table: Name of the table."
	+ " 2) YailList columns: List of which columns to return, passing an empty list will return all columns."
	+ " 3) String selection: Filter declaring which rows to return, formatted as an SQL WHERE clause, passing an empty string will return all rows."
	+ " 4) YailList selectionArgs: List with the arguments that will replace onto '?' in the selection filter."
	+ " 5) String groupBy: A filter declaring how to group rows, formatted as an SQL GROUP BY clause (excluding the GROUP BY itself), passing an empty string will cause the row to not be grouped."
	+ " 6) String having: A filter declare which row groups to include if row grouping is being used, passing an empty string will cause all row groups to be included."
	+ " 7) String orderBy: How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself), passing an empty string will use the default sort order (unordered)."
	+ " 8) String limit: Limits the number of rows returned by the query, formatted as LIMIT clause, passing an empty string denotes no LIMIT clause."
	+ " The result query is available in the AfterQuery event handler")
	public void Query(String table, YailList columns, String selection, YailList selectionArgs, String groupBy, String having, String orderBy) {
		DbHelper helper = new DbHelper(context);
		SQLiteDatabase db = helper.getReadableDatabase();
		String[] queryColumns = columns.toStringArray();
		String[] querySelectionArgs = selectionArgs.toStringArray();
		
		selection = (selection == "" ? null : selection);
		groupBy = (groupBy == "" ? null : groupBy);
		having = (having == "" ? null : having);
		orderBy = (orderBy == "" ? null : orderBy);
		
		List queryResult = new ArrayList<String>();
		int records = 0;
		boolean wasSuccesful;
		db.beginTransaction();
		try {
			Cursor cursor = db.query(table, queryColumns, selection, querySelectionArgs, groupBy, having, orderBy);
			queryResult = CursorManagement(cursor); //Translate Cursor into an ArrayList
			records = queryResult.size();
			wasSuccesful = true;
			db.setTransactionSuccessful();
			} catch (SQLException e) {
				wasSuccesful = false;
			} finally {
				db.endTransaction();
			}
		db.close();
	
		if (!suppressToast) { Toast.makeText(context,"Done",Toast.LENGTH_SHORT).show(); }
		if (wasSuccesful) {
			AfterQuery (queryResult, records);	
		} else {
			ErrorOcurred("Something went wrong querying");	
		}
	}
	
	/**
	* Event handler when an error ocurred, returns a string with a message from the error.
	*/
	@SimpleEvent
	public void ErrorOcurred(String message) {
		EventDispatcher.dispatchEvent(this, "ErrorOcurred", message);
	}
}
