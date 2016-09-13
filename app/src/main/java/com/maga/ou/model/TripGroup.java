package com.maga.ou.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

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

   /*
    * Get Instance
    * ___________________________________________________________________________________________________
    */

   public static TripGroup getInstance (SQLiteDatabase db, int id)
   {
      Cursor cursor = new DBQueryBuilder(db)
         .from(Table.TripGroup)
         .where(Column._id + "= ?").whereValue(String.valueOf(id))
         .query();

      if (!cursor.moveToFirst())
         DBUtil.die("Query empty. Id=" + id + " Table=" + Table.TripGroup);

      TripGroup group = new TripGroup();
      group.id = id;
      group.setName(DBUtil.getCell(cursor, Column.Name));
      group.setDetail(DBUtil.getCell(cursor, Column.Detail));
      group.setTripId(Integer.valueOf(DBUtil.getCell(cursor, Column.TripId)));

      return group;
   }

   public static TripGroup getLiteInstance (String id, String name, int tripId)
   {
      TripGroup group = new TripGroup();
      group.id = Integer.valueOf(id);
      group.setName(name);
      group.setTripId(tripId);
      return group;
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

      this.id = DBUtil.addRow (db, Table.TripGroup, getPopulatedContentValues());
      return this.id;
   }

   public void addUsers (SQLiteDatabase db, List<Integer> listUserId)
   {
      DBUtil.assertSetId(id);
      DBUtil.assertSetId(tripId);

      for (Integer userId : listUserId)
      {
         ContentValues values = new ContentValues();
         values.put (TripUserGroup.Column.UserId.name(), userId);
         values.put (TripUserGroup.Column.GroupId.name(), id);
         DBUtil.addRow(db, Table.TripUserGroup, values);
      }
   }

   public int update (SQLiteDatabase db)
   {
      DBUtil.assertSetId(id);
      DBUtil.assertSetId(tripId);
      validate();

      ContentValues values = getPopulatedContentValues ();
      return DBUtil.updateRowById(db, Table.TripGroup, id, values);
   }

   /**
    * Delete all groups present in <b>listGroupId</b> - All users of the group are automatically deleted due to cascade constraint in DB.
    *
    * @return The number of rows affected, 0 if deletion failed.
    */
   public static int delete (SQLiteDatabase db, List<Integer> listGroupId)
   {
      return DBUtil.deleteRowById(db, Table.TripGroup, listGroupId);
   }

   public int deleteAllUsers (SQLiteDatabase db)
   {
      DBUtil.assertSetId(id);
      DBUtil.assertSetId(tripId);

      return DBUtil.deleteRowByReferenceId(db, Table.TripUserGroup, TripUserGroup.Column.GroupId, id);
   }

   public List<TripUser> getLiteUsers(SQLiteDatabase db)
   {
      Cursor cursor = new DBQueryBuilder(db)
         .select(TripUserGroup.Column.UserId, TripUser.Column.NickName)
         .from(Table.TripUserGroup, Table.TripUser)
         .whereAND
         (
            TripUserGroup.Column.UserId + " = " + TripUser.Column._id,
            TripUserGroup.Column.GroupId + " = " + id
         )
         .orderBy(TripUser.Column.NickName)
         .query();

      List<String[]> listData = DBUtil.getRow(cursor);
      List<TripUser> listTripUser = new ArrayList<>();
      for (String col[] : listData)
         listTripUser.add(TripUser.getLiteInstance(col[0], col[1], tripId));

      return listTripUser;
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

   /*
    * Static Methods
    * ___________________________________________________________________________________________________
    */

   /**
    * Return a cursor of all trip groups for this trip <b>tripId</b>.
    */
   public static Cursor getTripGroups (SQLiteDatabase db, int tripId)
   {
      return new DBQueryBuilder(db)
         .from(Table.TripGroup)
         .where(Column.TripId + " = " + tripId)
         .orderBy(Column._id)
         .query();
   }

   /**
    * Return the list of all trip groups (lite) ordered by ID - This ensures the 'All' group tops the list.
    */
   public static List<TripGroup> getLiteTripGroups(SQLiteDatabase db, int tripId)
   {
      Cursor cursor = new DBQueryBuilder(db)
            .select(Column._id, Column.Name)
            .from(Table.TripGroup)
            .where(Column.TripId + " = " + tripId)
            .orderBy(Column._id)
            .query();

      List<String[]> listData = DBUtil.getRow(cursor);
      List<TripGroup> listGroup = new ArrayList<>();
      for (String col[] : listData)
         listGroup.add(TripGroup.getLiteInstance(col[0], col[1], tripId));

      return listGroup;
   }

   public static void addUserToGroupOfAll (SQLiteDatabase db, int tripId, TripUser user)
   {
      int userId = user.getId();
      DBUtil.assertSetId(userId);
      int groupIdOfAll = TripGroup.getIdOfGroupOfAll(db, tripId);

      // Check if the group 'All' already contains the userId
      // Get groupId for group by name 'All'
      Cursor cursorUserInAll = new DBQueryBuilder(db)
         .select(TripUserGroup.Column._id)
         .from (Table.TripUserGroup)
         .whereAND
         (
           TripUserGroup.Column.UserId   + " = " + user.getId(),
           TripUserGroup.Column.GroupId  + " = " + groupIdOfAll
         )
         .query();

      // This user already exists in 'All'
      if (cursorUserInAll.getCount() > 0)
      {
         Log.d(TAG, "User is already a part of Group All with Id=" + groupIdOfAll + ". Not adding to TripUserGroup table again!");
         return;
      }

      // Add user to group 'All' of this trip
      ContentValues values = new ContentValues();
      values.put(TripUserGroup.Column.GroupId.name(), groupIdOfAll);
      values.put(TripUserGroup.Column.UserId.name(), userId);
      DBUtil.addRow(db, Table.TripUserGroup, values);
   }

   public static int getIdOfGroupOfAll (SQLiteDatabase db, int tripId)
   {
      // Get groupId for group by name 'All'
      Cursor cursorAllGroupId = new DBQueryBuilder(db)
         .select(Column._id)
            .from(Table.TripGroup)
            .whereAND
         (
               Column.TripId + " = " + tripId,
               Column.Name + " = " + DBUtil.wrap(TripGroup.All)
         )
            .query();

      if (!cursorAllGroupId.moveToFirst())
         DBUtil.die("Could not get Id of TripGroup cursor for 'All' group");

      return cursorAllGroupId.getInt(0);
   }

   public static List<Integer> getUsersFromGroups (SQLiteDatabase db, Collection<Integer> listGroupId)
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
            .select(TripUser.Column.FullName, TripGroup.Column.Name)
            .from(Table.TripUserGroup, Table.TripUser, Table.TripGroup)
            .whereAND
            (
               TripUserGroup.Column.UserId  + " = " + TripUser.Column._id,
               TripUserGroup.Column.GroupId + " = " + TripGroup.Column._id
            )
            .query();

      for (boolean isDone = cursor.moveToFirst(); isDone; isDone = cursor.moveToNext())
         Log.d(TAG, "FullName=" + cursor.getString(0) + " Name=" + cursor.getString(1));
   }

   /*
    * Instance variable setters and getters
    * ___________________________________________________________________________________________________
    */

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
