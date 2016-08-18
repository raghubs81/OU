package com.maga.ou.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.maga.ou.model.util.AbstractColumn;
import com.maga.ou.model.util.DBQueryBuilder;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.model.OUDatabaseHelper.Table;
/**
 * Created by rbseshad on 27-Jun-16.
 */
public class Trip
{
   private static final String TAG = "ou." + Trip.class.getSimpleName();

   public enum Column implements AbstractColumn
   {
      _id, Name, Detail;

      @Override
      public String toString ()
      {
         return Table.Trip.name() + "." + this.name ();
      }
   }

   private int id;

   private String name, detail;

   public static Trip getLiteInstance (SQLiteDatabase db, int id)
   {
      Cursor cursor = new DBQueryBuilder(db)
            .select(Column._id, Column.Name)
            .from(Table.Trip)
            .where(Column._id + "= ?").whereValue(String.valueOf(id))
            .query();

      if (!cursor.moveToFirst())
         DBUtil.die("Could not get first row of trip cursor");

      Trip trip = new Trip ();
      trip.id = Integer.valueOf(DBUtil.getCell(cursor, Column._id));
      trip.setName(DBUtil.getCell(cursor, Column.Name));
      return trip;
   }

   public static Trip getInstance (SQLiteDatabase db, int id)
   {
      Cursor cursor = new DBQueryBuilder(db)
         .from(Table.Trip)
         .where(Column._id + "=?").whereValue(String.valueOf(id))
         .query();

      if (!cursor.moveToFirst())
         DBUtil.die("Could not get first row of trip cursor");

      Trip trip = new Trip ();
      trip.id = Integer.valueOf(DBUtil.getCell(cursor, Column._id));
      trip.setName(DBUtil.getCell(cursor, Column.Name));
      trip.setDetail(DBUtil.getCell(cursor, Column.Detail));

      return trip;
   }

   /**
    * Returns a cursor of all the trips.
    */
   public static Cursor getTrips (SQLiteDatabase db)
   {
      return new DBQueryBuilder(db)
         .from (Table.Trip)
         .orderByDesc(Column._id)
         .query();
   }

   /**
    * Add <b>item</b> to the current trip (in context).
    */
   public void addItem (SQLiteDatabase db, Item item)
   {
      DBUtil.assertSetId(this.id);
      DBUtil.assertSetId(item.getId());

      ContentValues values = new ContentValues ();
      values.put(TripItem.Column.TripId.name(), this.id);
      values.put(TripItem.Column.ItemId.name(), item.getId());

      int newId = (int)db.insert(Table.TripItem.name(), null, values);
      DBUtil.assertNotEquals (newId, -1, "Table=Item, Addition failed");
   }

   public int add (SQLiteDatabase db)
   {
      DBUtil.assertUnsetId(id);
      validate();

      ContentValues values = getPopulatedContentValues ();
      Log.d(TAG, "INSERT " + Table.Trip + " " + values + " VALUES " + values);
      int result = (int) db.insert(Table.Trip.name(), null, values);
      DBUtil.assertNotEquals(result, -1, "Table=Trip, Addition failed");
      this.id = result;
      return result;
   }

   public int update (SQLiteDatabase db)
   {
      DBUtil.assertSetId(id);
      validate();

      ContentValues values = getPopulatedContentValues ();
      return DBUtil.updateRowById(db, Table.Trip, id, values);
   }


   private ContentValues getPopulatedContentValues ()
   {
      ContentValues values = new ContentValues();
      values.put(Column.Name.name() , name);
      values.put(Column.Detail.name() , detail);
      return values;
   }

   private void validate ()
   {
      DBUtil.assertNonEmpty(name, "Table=Trip, Name is mandatory.");
   }

   // Setters and Getters

   public void setName(String name)
   {
      if (name == null || name.isEmpty())
         DBUtil.die("Trip name is empty");
      this.name = name;
   }

   public void setDetail(String detail)
   {
      this.detail = detail;
   }

   public int getId ()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }


   public String getDetail()
   {
      return detail;
   }

   static class TripItem
   {
      public enum Column implements AbstractColumn
      {
         _id, TripId, ItemId;

         @Override
         public String toString ()
         {
            return Table.TripItem.name() + "." + this.name ();
         }
      }
   }

}
