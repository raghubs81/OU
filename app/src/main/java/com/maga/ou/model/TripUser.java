package com.maga.ou.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import com.maga.ou.model.util.*;
import com.maga.ou.model.OUDatabaseHelper.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by rbseshad on 24-Jun-16.
 */
public class TripUser
{
   private static final String TAG = "ou." + TripUser.class.getSimpleName();

   public enum Column implements AbstractColumn
   {
      _id, NickName, FullName, ContactId, TripId;

      @Override
      public String toString ()
      {
         return Table.TripUser.name() + "." + this.name ();
      }
   }

   private int id, tripId;

   private String nickName;

   private String fullName;

   private String contactId;

   /*
    * Get Instance
    * ___________________________________________________________________________________________________
    */

   public static TripUser getLiteInstance (SQLiteDatabase db, int id)
   {
      Cursor cursor = new DBQueryBuilder(db)
         .select(Column._id, Column.NickName, Column.TripId)
         .from(Table.TripUser)
         .where(Column._id + "=" + id)
         .query();

      if (!cursor.moveToFirst())
         DBUtil.die("Query empty. Id=" + id + " Table=" + Table.TripUser);

      TripUser user = new TripUser();
      user.id = Integer.valueOf(DBUtil.getCell(cursor, Column._id));
      user.setNickName(DBUtil.getCell(cursor, Column.NickName));
      user.setTripId(Integer.valueOf(DBUtil.getCell(cursor, Column.TripId)));

      return user;
   }

   public static TripUser getLiteInstance (String id, String nickName, int tripId)
   {
      TripUser user = new TripUser();
      user.id = Integer.valueOf(id);
      user.setNickName(nickName);
      user.setTripId(tripId);
      return user;
   }

   public static TripUser getInstance (SQLiteDatabase db, int id)
   {
      Cursor cursor = new DBQueryBuilder(db)
            .from(Table.TripUser)
            .where(Column._id + "=?").whereValue(String.valueOf(id))
            .query();

      if (!cursor.moveToFirst())
         DBUtil.die("Could not get first row of TripUser cursor");

      TripUser user = new TripUser();
      user.id = Integer.valueOf(DBUtil.getCell(cursor, Column._id));
      user.setNickName(DBUtil.getCell(cursor, Column.NickName));
      user.setFullName(DBUtil.getCell(cursor, Column.FullName));
      user.setContactId(DBUtil.getCell(cursor, Column.ContactId));
      user.setTripId(Integer.valueOf(DBUtil.getCell(cursor, Column.TripId)));

      return user;
   }

   /*
    * CURD Operations
    * ___________________________________________________________________________________________________
    */

   public int add (SQLiteDatabase db)
   {
      DBUtil.assertUnsetId(id);
      DBUtil.assertSetId(tripId);
      validate();

      this.id = DBUtil.addRow (db, Table.TripUser, getPopulatedContentValues());
      return this.id;
   }

   public int update (SQLiteDatabase db)
   {
      DBUtil.assertSetId(id);
      DBUtil.assertSetId(tripId);
      validate();

      ContentValues values = getPopulatedContentValues ();
      return DBUtil.updateRowById(db, Table.TripUser, id, values);
   }

   /*
    * Static methods
    * ___________________________________________________________________________________________________
    */

   /**
    * Delete all users in <b>listUserId</b> if they do not owe OR are owed by for any items.
    * <br><b>NOTE :</b> The table schema syntax 'on delete cascade' takes care of auto removing the thumb_users from all groups.
    *
    * @return The number of rows affected, 0 if deletion failed.
    */
   public static int delete (SQLiteDatabase db, int tripId, List<Integer> listUserId)
   {
      try
      {
//         String whereClause =  Column.TripId + " = "   + tripId + " AND " +
//                               Column._id    + " IN (" + TextUtils.join(",", listUserId) + ")";
//         return db.delete(Table.TripUser.name(), whereClause, null);
         return DBUtil.deleteRowById(db, Table.TripUser, listUserId);
      }
      catch (SQLiteConstraintException e)
      {
         Log.d (TAG, "Constraint Violated - " + e.getMessage());
         return 0;
      }
   }

   /**
    * Return a cursor of all trip users for this trip <b>tripId</b>.
    */
   public static Cursor getTripUsers (SQLiteDatabase db, int tripId)
   {
      return new DBQueryBuilder(db)
         .from (Table.TripUser)
         .where(Column.TripId + " = " + tripId)
         .orderBy(TripUser.Column.NickName)
         .query();
   }

   public static List<TripUser> getLiteTripUsers(SQLiteDatabase db, int tripId)
   {
      Cursor cursor = new DBQueryBuilder(db)
         .select(Column._id, Column.NickName)
         .from(Table.TripUser)
         .where(Column.TripId + " = " + tripId)
         .orderBy(TripUser.Column.NickName)
         .query();

      List<String[]> listData = DBUtil.getRow(cursor);
      List<TripUser> listUser = new ArrayList<>();
      for (String col[] : listData)
         listUser.add(TripUser.getLiteInstance(col[0], col[1], tripId));

      return listUser;
   }


   public static List<String> getTripUserContactIds (SQLiteDatabase db, int tripId)
   {
      Cursor cursor =  new DBQueryBuilder(db)
         .select(Column.ContactId)
         .from(Table.TripUser)
         .where(Column.TripId + " = " + tripId)
         .query();

      return DBUtil.getColumn(cursor, Column.ContactId).get(0);
   }

   /**
    * Return the count of trip thumb_users
    */
   public static int getTripUserCount (SQLiteDatabase db, int tripId)
   {
      Cursor cursor = new DBQueryBuilder(db)
         .selectRaw("COUNT(*)")
         .from (Table.TripUser)
         .where(Column.TripId + " = " + tripId)
         .query();

      if (!cursor.moveToFirst())
         DBUtil.die("Could not get first row of TripUser counting cursor");

      return cursor.getInt(0);
   }

   private ContentValues getPopulatedContentValues ()
   {
      ContentValues values = new ContentValues ();
      values.put(Column.NickName.name() , nickName);
      values.put(Column.FullName.name(), fullName);
      values.put(Column.ContactId.name(), contactId);
      values.put(Column.TripId.name(), tripId);
      return values;
   }

   private void validate ()
   {
      DBUtil.assertNonEmpty(nickName, "Table=TripUser, NickName is mandatory.");
   }

   /*
    * Instance variable setters and getters
    * ___________________________________________________________________________________________________
    */

   public int getId ()
   {
      return id;
   }

   public String getFullName()
   {
      return fullName;
   }

   public void setNickName(String nickName)
   {
      this.nickName = nickName;
   }

   public String getNickName()
   {
      return nickName;
   }

   public void setFullName(String fullName)
   {
      this.fullName = fullName;
   }

   public int getTripId()
   {
      return tripId;
   }

   public void setTripId(int tripId)
   {
      this.tripId = tripId;
   }

   public String getContactId()
   {
      return contactId;
   }

   public void setContactId(String contactId)
   {
      this.contactId = contactId;
   }


   @Override
   public String toString ()
   {
      return getNickName();
   }
}
