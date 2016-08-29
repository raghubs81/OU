package com.maga.ou.model.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import com.maga.ou.model.OUDatabaseHelper;
import java.util.ArrayList;
import java.util.List;

public class DBUtil
{
   public static final String TAG = "ou." + DBUtil.class.getSimpleName();

   public static final String NEW_LINE = System.getProperty("line.separator");

   public static final int UNSET_ID = 0;

   private static SQLiteDatabase DB = null;

   /*
    * Core DB operations
    * ___________________________________________________________________________________________________
    */

   /**
    * Lazy DB instantiation - If not already present, create and return the DB connection.
    */
   public static SQLiteDatabase getDB (Context context)
   {
      if (DB == null)
      {
         synchronized (DBUtil.class)
         {
            if (DB == null)
               DB = new OUDatabaseHelper(context).getWritableDatabase();
         }
      }

      return DB;
   }

   /**
    * Close the database connection
    */
   public static synchronized void closeDB ()
   {
      if (DB != null)
      {
         DB.close();
         DB = null;
      }
   }

   /*
    * Cursor Operations
    * ___________________________________________________________________________________________________
    */

   /**
    * Get all rows, each row as an array of String, from the given cursor.
    */
   public static List<String[]> getRow (Cursor cursor)
   {
      List<String[]> listData = new ArrayList<>();
      boolean isDone;
      for (isDone = cursor.moveToFirst(); isDone; isDone = cursor.moveToNext())
      {
         String col[] = new String[cursor.getColumnCount()];
         for (int i = 0; i < col.length; ++i)
            col[i] = cursor.getString(i);
         listData.add(col);
      }
      return listData;
   }

   /* Vertical split of column(s) */

   /**
    * Extract the '_id' column from the cursor
    */
   public static List<Integer> getIdColumn (Cursor cursor, AbstractColumn columnName)
   {
      List<Integer> listId = new ArrayList<>();
      for (String currId : getColumn (cursor, columnName).get(0))
         listId.add(Integer.valueOf(currId));
      return listId;
   }

   /**
    * Each column from columnName array is vertically stripped and added to a List.
    * So, The first has entries in column columnName[0]
    */
   public static List<List<String>> getColumn (Cursor cursor, AbstractColumn... columnName)
   {
      List<List<String>> listData = new ArrayList<> ();

      for (int i = 0; i < columnName.length; ++i)
         listData.add(new ArrayList<String>());

      boolean isDone = false;
      for (isDone = cursor.moveToFirst(); isDone; isDone = cursor.moveToNext())
      {
         int i = 0;
         for (List listColumn : listData)
            listColumn.add (DBUtil.getCell(cursor, columnName[i++]));

      }
      return listData;
   }

   /**
    * Assuming the cursor is at the right row, fetches the cell at <b>column</b>
    */
   public static String getCell (Cursor cursor, AbstractColumn column)
   {
      String value = cursor.getString(cursor.getColumnIndexOrThrow(column.name()));
      return (value == null) ? "" : value;
   }

   /*
    * Asserts
    * ___________________________________________________________________________________________________
    */

   public static void assertEquals (int actual, int expected, String mesg)
   {
      if (actual != expected)
         die ("Actual is NOT same as expected. Actual=" + actual + " Expected=" + expected + " Error=" + mesg);
   }

   public static void assertNotEquals (int actual, int expected, String mesg)
   {
      if (actual == expected)
         die ("Actual is same as expected. Actual=Expected=" + actual + " Error=" + mesg);
   }

   public static void assertNonEmpty (Object value, String mesg)
   {
      boolean isEmpty = (value == null);

      if (!isEmpty && value instanceof String)
         isEmpty = ((String)value).isEmpty();

      if (isEmpty)
         die ("Non emtpy assertion failed. Value=" + value + " Error=" + mesg);
   }

   /**
    * Assert that {@code id == UNSET_ID}
    */
   public static void assertUnsetId (int id)
   {
      if (id != UNSET_ID)
         die ("ID already exists. ID=" + id);
   }

   /**
    * Assert that {@code id != UNSET_ID}
    */
   public static void assertSetId (int id)
   {
      if (id == UNSET_ID)
         die ("ID is not set. ID=" + id);
   }

   /*
    * Basic Insert, Delete and Update operations
    * ___________________________________________________________________________________________________
    */

   public static int addRow (SQLiteDatabase db, AbstractTable table, ContentValues values)
   {
      Log.d(TAG, "INSERT " + table + " " + values + " VALUES " + values);
      int result = (int) db.insert(table.name(), null, values);
      DBUtil.assertNotEquals(result, -1, "Table=" + table.name() + ", Addition failed");
      return result;
   }

   public static int updateRowById(SQLiteDatabase db, AbstractTable table, int id, ContentValues values)
   {
      String whereClause = "_id = ?";
      String whereValues[] = new String [] {String.valueOf(id)};
      Log.d(TAG, "UPDATE " + table + " " + values + " WHERE " + whereClause + " WHERE-VALUE " + TextUtils.join(", ", whereValues));
      int result = db.update(table.name(), values, whereClause, whereValues);
      DBUtil.assertNotEquals(result, 0, "Table=Item, No row updated");
      return result;
   }

   public static int deleteRowById (SQLiteDatabase db, AbstractTable table, List<Integer> listId)
   {
      if (listId == null || listId.size() == 0)
         return 0;

      String whereClause = "_id IN (" + TextUtils.join(",", listId) + ")";
      int result = db.delete(table.name(), whereClause, null);
      DBUtil.assertNotEquals(result, 0, "Table=Item, No row deleted");
      return result;
   }

   public static int deleteRowById(SQLiteDatabase db, AbstractTable table, int id)
   {
      String whereClause = "_id = ?";
      String whereValues[] = new String [] {String.valueOf(id)};
      int result = db.delete(table.name(), whereClause, whereValues);
      DBUtil.assertNotEquals(result, 0, "Table=Item, No row deleted");
      return result;
   }

   public static int deleteRowByReferenceId(SQLiteDatabase db, AbstractTable table, AbstractColumn refColumn, int id)
   {
      String whereClause = refColumn + " = ?";
      String whereValues[] = new String [] {String.valueOf(id)};
      Log.d(TAG, "DELETE " + table + " WHERE " + whereClause + " WHERE-VALUE " + TextUtils.join(", ", whereValues));
      return db.delete(table.name(), whereClause, whereValues);
   }

   public static int deleteRowByReferenceId(SQLiteDatabase db, AbstractTable table, AbstractColumn refColumn, List<Integer> listId)
   {
      if (listId == null || listId.size() == 0)
         return 0;

      String whereClause = refColumn + " IN (" + TextUtils.join(",", listId) + ")";
      Log.d(TAG, "DELETE " + table + " WHERE " + whereClause);
      return db.delete(table.name(), whereClause, null);
   }

   /*
    * DB Util
    * ___________________________________________________________________________________________________
    */

   public static String wrap (String value)
   {
      return "'" + value + "'";
   }

   /*
    * Fatal exit
    * ___________________________________________________________________________________________________
    */

   public static void die (String mesg)
   {
      die(mesg, null);
   }

   public static void die (String mesg, Throwable e)
   {
      Log.e(TAG, mesg, e);
      throw new IllegalStateException(mesg, e);
   }

}
