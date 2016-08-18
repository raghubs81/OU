package com.maga.ou.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.maga.ou.OUContext;
import com.maga.ou.model.util.AbstractColumn;
import com.maga.ou.model.util.DBQueryBuilder;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.model.OUDatabaseHelper.Table;
import com.maga.ou.model.Trip.TripItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An item that is shared by a group of users.
 */
public class Item
{
   public enum Column implements AbstractColumn
   {
      _id, Summary, Detail;

      @Override
      public String toString()
      {
         return Table.Item.name() + "." + this.name();
      }
   }

   private int id = DBUtil.UNSET_ID;

   private String summary, detail;

   /*
    * Get Instance
    * ___________________________________________________________________________________________________
    */

   public static Item getLiteInstance(SQLiteDatabase db, int id)
   {
      Cursor cursor = new DBQueryBuilder(db)
            .select(Column._id, Column.Summary)
            .from(Table.Item)
            .where(Column._id + "= ?").whereValue(String.valueOf(id))
            .query();

      if (!cursor.moveToFirst())
         DBUtil.die("Could not get first row of Item cursor");

      Item Item = new Item();
      Item.setId(DBUtil.getCell(cursor, Column._id));
      Item.setSummary(DBUtil.getCell(cursor, Column.Summary));
      return Item;
   }

   public static Item getInstance(SQLiteDatabase db, int id)
   {
      Cursor cursor = new DBQueryBuilder(db)
            .from(Table.Item)
            .where(Column._id + "=?").whereValue(String.valueOf(id))
            .query();

      if (!cursor.moveToFirst())
         DBUtil.die("Could not get first row of Item cursor");

      return createItemFromCursor(cursor);
   }

   /*
    * CURD Operations
    * ___________________________________________________________________________________________________
    */

   public int add(SQLiteDatabase db)
   {
      DBUtil.assertUnsetId(id);
      validate();

      this.id = DBUtil.addRow (db, Table.Item, getPopulatedContentValues());
      return this.id;
   }

   public int update(SQLiteDatabase db)
   {
      DBUtil.assertSetId(id);
      validate();

      ContentValues values = getPopulatedContentValues ();
      return DBUtil.updateRowById(db, Table.Item, id, values);
   }

   public static int delete (SQLiteDatabase db, List<Integer> listItemId)
   {
      return DBUtil.deleteRowById(db, Table.Item, listItemId);
   }

   private void validate()
   {
      DBUtil.assertNonEmpty(summary, "Table=Item, Summary is mandatory.");
   }

   private ContentValues getPopulatedContentValues ()
   {
      ContentValues values = new ContentValues();
      values.put(Column.Summary.name(), summary);
      values.put(Column.Detail.name(), detail);
      return values;
   }

   /*
    * CURD Operations - Sub elements
    * ___________________________________________________________________________________________________
    */

   public Map<TripUser, Integer> getPaidByUsers(SQLiteDatabase db)
   {
      Cursor cursor = new DBQueryBuilder(db)
            .select(ItemPaidBy.Column.UserId, ItemPaidBy.Column.Amount)
            .from(Table.ItemPaidBy, Table.TripUser)
            .where(ItemPaidBy.Column.ItemId + " = ?").whereValue(String.valueOf(this.id)).where("AND")
            .where(ItemPaidBy.Column.UserId + " = " + TripUser.Column._id)
            .orderBy(TripUser.Column.FirstName)
            .query();
      List<String[]> listData = DBUtil.getRow(cursor);

      Map<TripUser, Integer> mapUserAmount = new LinkedHashMap<>();
      for (String col[] : listData)
      {
         TripUser user = TripUser.getLiteInstance(db, Integer.valueOf(col[0]));
         Integer amount = Integer.valueOf(col[1]);
         mapUserAmount.put(user, amount);
      }
      return mapUserAmount;
   }

   public List<TripUser> getSharedByUsers(SQLiteDatabase db)
   {
      Cursor cursor = new DBQueryBuilder(db)
            .select(ItemSharedBy.Column.UserId)
            .from(Table.ItemSharedBy, Table.TripUser)
            .where(ItemSharedBy.Column.ItemId + " = ?").whereValue(String.valueOf(this.id)).where("AND")
            .where(ItemSharedBy.Column.UserId + " = " + TripUser.Column._id)
            .orderBy(TripUser.Column.NickName)
            .query();
      List<String[]> listData = DBUtil.getRow(cursor);

      List<TripUser> listUser = new ArrayList<>();
      for (String col[] : listData)
      {
         TripUser user = TripUser.getLiteInstance(db, Integer.valueOf(col[0]));
         listUser.add(user);
      }
      return listUser;
   }

   public void setPaidBy (SQLiteDatabase db, Map<Integer, Integer> mapUserIdAmount)
   {
      for (Map.Entry<Integer, Integer> entry : mapUserIdAmount.entrySet())
      {
         Integer userId = entry.getKey();
         Integer amount = entry.getValue();

         ContentValues values = new ContentValues();
         values.put(ItemPaidBy.Column.Amount.name(), amount);
         values.put(ItemPaidBy.Column.UserId.name(), userId);
         values.put(ItemPaidBy.Column.ItemId.name(), this.id);

         int newId = (int) db.insert(Table.ItemPaidBy.name(), null, values);
         DBUtil.assertNotEquals(newId, -1, "Table=ItemPaidBy, Addition failed");
      }
   }

   public int deletePaidBy (SQLiteDatabase db)
   {
      return DBUtil.deleteRowById(db, Table.ItemPaidBy, ItemPaidBy.Column.ItemId, id);
   }

   public void setSharedBy (SQLiteDatabase db, Collection<Integer> setUserId)
   {
      for (Integer userId : setUserId)
      {
         ContentValues values = new ContentValues();
         values.put(ItemSharedBy.Column.UserId.name(), userId);
         values.put(ItemPaidBy.Column.ItemId.name(), this.id);

         int newId = (int) db.insert(Table.ItemSharedBy.name(), null, values);
         DBUtil.assertNotEquals(newId, -1, "Table=ItemSharedBy, Addition failed");
      }
   }

   public int deleteSharedBy (SQLiteDatabase db)
   {
      return DBUtil.deleteRowById(db, Table.ItemSharedBy, ItemSharedBy.Column.ItemId, id);
   }

   /*
    * Static Methods
    * ___________________________________________________________________________________________________
    */

   /**
    * Get the list of Items for this trip <b>tripId</b>.
    */
   public static List<Item> getItems(SQLiteDatabase db, int tripId)
   {
      Cursor cursor = new DBQueryBuilder(db)
            .from(Table.TripItem, Table.Item)
            .whereAND
                  (
                        TripItem.Column.ItemId + " = " + Column._id,
                        TripItem.Column.TripId + " = " + tripId
                  )
            .query();

      List<Item> listItem = new ArrayList<>();
      boolean isDone = false;
      for (isDone = cursor.moveToFirst(); isDone; isDone = cursor.moveToNext())
         listItem.add(createItemFromCursor(cursor));

      return listItem;
   }

   private static Item createItemFromCursor(Cursor cursor)
   {
      Item item = new Item();
      item.setId(DBUtil.getCell(cursor, Column._id));
      item.setSummary(DBUtil.getCell(cursor, Column.Summary));
      item.setDetail(DBUtil.getCell(cursor, Column.Detail));
      return item;
   }

   public static Cursor getItemPaymentSummary(SQLiteDatabase db, int tripId)
   {
      String whereJoin_Item_TripItem = Column._id + " = " + TripItem.Column.ItemId;
      String whereJoin_Item_PaidBy = Column._id + " = " + ItemPaidBy.Column.ItemId;
      String where_CurrentTripRows = TripItem.Column.TripId + " = " + tripId;

      return new DBQueryBuilder(db)
            .select(Column._id, Column.Summary, Column.Detail).selectRaw("SUM(Amount) as Amount")
            .from(Table.Item, Table.TripItem, Table.ItemPaidBy)
            .whereAND(whereJoin_Item_TripItem, whereJoin_Item_PaidBy, where_CurrentTripRows)
            .groupBy(ItemPaidBy.Column.ItemId)
            .query();
   }

   /*
    * Instance variable setters and getters
    * ___________________________________________________________________________________________________
    */

   public String getSummary()
   {
      return summary;
   }

   public void setSummary(String summary)
   {
      this.summary = summary;
   }

   public String getDetail()
   {
      return detail;
   }

   public void setDetail(String detail)
   {
      this.detail = detail;
   }

   public int getId()
   {
      return id;
   }

   private void setId(String Id)
   {
      this.id = Integer.valueOf(Id);
   }

   public String toString()
   {
      return summary;
   }

   public static class ItemSharedBy
   {
      public enum Column implements AbstractColumn
      {
         _id, UserId, ItemId;

         @Override
         public String toString ()
         {
            return Table.ItemSharedBy.name() + "." + this.name ();
         }
      }
   }

   public static class ItemPaidBy
   {
      public enum Column implements AbstractColumn
      {
         _id, Amount, UserId, ItemId;

         @Override
         public String toString ()
         {
            return Table.ItemPaidBy.name() + "." + this.name ();
         }
      }
   }
}
