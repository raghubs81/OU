package com.maga.ou.model;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.io.*;
import java.util.*;
import com.maga.ou.model.util.AbstractTable;

/**
 * Created by rbseshad on 27-Jun-16.
 */
public class OUDatabaseHelper extends SQLiteOpenHelper
{
   public static final String DB_NAME = "OUDB";

   public static final int DB_VERSION = 1;

   private final String TAG = "ou." + getClass ().getSimpleName();

   private Context context;

   public enum Table implements AbstractTable
   {
      Trip, TripUser, TripGroup, TripUserGroup, Item, TripItem, ItemPaidBy, ItemSharedBy
   }

   public OUDatabaseHelper (Context context)
   {
      super(context, DB_NAME, null, DB_VERSION);
      this.context = context;
      Log.d(TAG, "OUDatabaseHelper constuctor");
   }

   @Override
   public void onCreate (SQLiteDatabase db)
   {
      updateMyDatabase(db, 0, DB_VERSION);
   }

   @Override
   public void onConfigure(SQLiteDatabase db)
   {
      super.onConfigure(db);
      db.setForeignKeyConstraintsEnabled(true);
   }

   @Override
   public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion)
   {
      updateMyDatabase(db, oldVersion, newVersion);
   }

   private void updateMyDatabase (SQLiteDatabase db, int oldVersion, int newVersion)
   {
      Log.d(TAG, "DB upgrade. oldVersion=" + oldVersion + " newVersion=" + newVersion);

      // Only fresh installs will have oldVersion < 1
      if (oldVersion < 1)
         performDB1Changes(db);

      // Add methods to upgrade from DB1 to DB2
   }

   private void performDB1Changes (SQLiteDatabase db)
   {
      Log.d(TAG, "Started creating tables");
      createTables(db);
      Log.d(TAG, "Finished creating tables");
   }

   /**
    * Create and populate tables.
    *
    * @param db Database
    */
   private void createTables (SQLiteDatabase db)
   {
      String fileTableCreation = "/oudb.sql";
      try
      {
         BufferedReader in = new BufferedReader(new InputStreamReader(context.getAssets().open("oudb.sql")));
         StringBuilder builder = new StringBuilder();
         String line = null;
         while ((line = in.readLine()) != null)
         {
            line = line.trim().replaceAll("\\s+", " ");
            if (line.equals("") || line.startsWith("#"))
               continue;
            builder.append(line);
         }
         in.close();

         String sql[] = builder.toString().split(";");
         for (String currSql : sql)
         {
            currSql = currSql + ";";
            Log.d(TAG, "Executing SQL=" + currSql);
            db.execSQL(currSql);
         }
      }
      catch (IOException e)
      {
         throw new IllegalStateException("Error opening file. File=" + fileTableCreation);
      }
   }

   public ArrayList<Cursor> getData(String Query)
   {
      //get writable database
      SQLiteDatabase sqlDB = this.getWritableDatabase();
      String[] columns = new String[] { "mesage" };
      //an array list of cursor to save two cursors one has results from the query
      //other cursor stores error message if any errors are triggered
      ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
      MatrixCursor Cursor2= new MatrixCursor(columns);
      alc.add(null);
      alc.add(null);


      try{
         String maxQuery = Query ;
         //execute the query results will be save in Cursor c
         Cursor c = sqlDB.rawQuery(maxQuery, null);


         //add value to cursor2
         Cursor2.addRow(new Object[] { "Success" });

         alc.set(1,Cursor2);
         if (null != c && c.getCount() > 0) {


            alc.set(0,c);
            c.moveToFirst();

            return alc ;
         }
         return alc;
      } catch(SQLException sqlEx){
         Log.d("printing exception", sqlEx.getMessage());
         //if any exceptions are triggered save the error message to cursor an return the arraylist
         Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
         alc.set(1,Cursor2);
         return alc;
      } catch(Exception ex){

         Log.d("printing exception", ex.getMessage());

         //if any exceptions are triggered save the error message to cursor an return the arraylist
         Cursor2.addRow(new Object[] { ""+ex.getMessage() });
         alc.set(1,Cursor2);
         return alc;
      }
   }
}
