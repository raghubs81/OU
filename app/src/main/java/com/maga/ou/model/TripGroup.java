package com.maga.ou.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.maga.ou.OUContext;
import com.maga.ou.model.util.AbstractColumn;
import com.maga.ou.model.util.DBQueryBuilder;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.model.OUDatabaseHelper.*;

import java.util.*;

/**
 * Created by rbseshad on 24-Jun-16.
 */
public class TripGroup
{
   private static final String TAG = "ou." + TripGroup.class.getSimpleName();

   public static final String All = "All";

   public static final String ALL_DESCRIPTION = "A group with all members";

   public enum Column implements AbstractColumn
   {
      _id, Name, Detail, TripId;

      @Override
      public String toString ()
      {
         return Table.TripGroup.name() + "." + this.name ();
      }
   }

   private int id, tripId;

   private String name, detail;

   private static TripGroup getLiteInstance (SQLiteDatabase db, int id)
   {
      Cursor cursor = new DBQueryBuilder(db)
         .select(Column._id, Column.Name)
         .from(Table.TripGroup)
         .where(Column._id + "= ?").whereValue(String.valueOf(id))
         .query();

      if (!cursor.moveToFirst())
         DBUtil.die("Query empty. Id=" + id + " Table=" + Table.TripGroup);

      TripGroup group = new TripGroup();
      group.id = id;
      group.setName(DBUtil.getCell(cursor, Column.Name));
      group.setTripId(Integer.valueOf(DBUtil.getCell(cursor, Column.TripId)));

      return group;
   }

   public int add (SQLiteDatabase db)
   {
      DBUtil.assertUnsetId(id);
      DBUtil.assertSetId(tripId);
      validate();

      ContentValues values = getPopulatedContentValues();
      int result = (int) db.insert(Table.TripGroup.name(), null, values);

      DBUtil.assertNotEquals(result, -1, "Table=TripGroup, Addition failed");
      this.id = result;
      return result;
   }

   public int update (SQLiteDatabase db)
   {
      DBUtil.assertSetId(id);
      DBUtil.assertSetId(tripId);
      validate();

      ContentValues values = getPopulatedContentValues ();
      return DBUtil.updateRowById(db, Table.TripUser, id, values);
   }

   private ContentValues getPopulatedContentValues ()
   {
      ContentValues values = new ContentValues();
      values.put(Column.Name.name(), name);
      values.put(Column.Detail.name(), detail);
      values.put(Column.TripId.name(), tripId);
      return values;
   }

   private void validate ()
   {
      DBUtil.assertNonEmpty(name, "Table=TripGroup, name is mandatory.");
      DBUtil.assertNonEmpty(tripId, "Table=TripGroup, tripId is mandatory.");
   }

   public static void addUserGroupOfAll (SQLiteDatabase db, TripUser user)
   {

      int userId = user.getId();
      DBUtil.assertSetId(id);
      DBUtil.assertSetId(userId);
   }


   public static List<Integer> getUsersFromGroups (SQLiteDatabase db, List<Integer> listGroupId)
   {
      Cursor cursor = new DBQueryBuilder(db)
         .distinct(true)
         .select(TripUserGroup.Column.UserId)
         .from(Table.TripUserGroup)
         .where(TripUserGroup.Column.GroupId + " IN (" + TextUtils.join(",", listGroupId) + ")")
         .query();
      return DBUtil.getIdColumn(cursor, TripUserGroup.Column.UserId);
   }

   public static void logGroups (SQLiteDatabase db)
   {
      Cursor cursor = new DBQueryBuilder(db)
            .distinct(true)
            .select(TripUser.Column.FirstName, TripUser.Column.LastName, TripGroup.Column.Name)
            .from(Table.TripUserGroup, Table.TripUser, Table.TripGroup)
            .where("TripUserGroup.UserId = TripUser._id AND TripUserGroup.GroupId = TripGroup._id")
            .query();

      String sql = "SELECT FirstName, LastName, Name " +
                   "FROM   TripUserGroup, TripUser, TripGroup " +
                   "WHERE  TripUserGroup.UserId = TripUser._id AND TripUserGroup.GroupId = TripGroup._id";

      for (boolean isDone = cursor.moveToFirst(); isDone; isDone = cursor.moveToNext())
         Log.d(TAG, "FirstName=" + cursor.getString(0) + " LastName=" + cursor.getString(1) + " Name=" + cursor.getString(2));
   }

   public int getId ()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getDetail()
   {
      return detail;
   }

   public void setDetail(String detail)
   {
      this.detail = detail;
   }

   public int getTripId()
   {
      return tripId;
   }

   public void setTripId(int tripId)
   {
      this.tripId = tripId;
   }

   static class TripUserGroup
   {
      public enum Column implements AbstractColumn
      {
         _id, UserId, GroupId;

         @Override
         public String toString ()
         {
            return Table.TripUserGroup.name() + "." + this.name ();
         }
      }
   }
}
