package com.maga.ou.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.maga.ou.model.util.DBUtil;
import com.maga.ou.model.util.ReportGenerator;
import com.maga.ou.util.OUCurrencyUtil;
import com.maga.ou.util.UIUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by rbseshad on 16-Aug-16.
 */
public class OUAmountDistribution
{
   private static final String TAG = "ou." + OUAmountDistribution.class.getSimpleName();

   /**
    * Map lender's userId to a list of UserAmount (Borrower userId and the amount owed by borrower)
    */
   private Map<Integer,List<UserAmount>> mapLenderToBorrowers = new TreeMap<> ();

   /**
    * Map borrower's userId to a list of UserAmount (Lenders userId and the amount owed to lender)
    */
   private Map<Integer,List<UserAmount>> mapBorrowerToLenders = new TreeMap<> ();

   /**
    * Map of lender's userId to list of ItemAmount (ItemId, summary and amount paid for the item)
    */
   private Map<Integer,List<ItemAmount>> mapLenderToItems = new TreeMap<>();

   /**
    * Map of userId to amount owed - A +ve number indicates amount owed to the user, -ve indicates amount owed by the user.
    */
   private Map<Integer,Integer> mapUserIdToOweAmount = new TreeMap<>();

   /**
    * List of all user IDs - Sorted by user name
    */
   private List<Integer> listAllUserId = new ArrayList<>();

   /**
    * List of all user names - Sorted
    */
   private List<String> listAllUserName = new ArrayList<>();

   /**
    * Current trip ID
    */
   private int tripId = DBUtil.UNSET_ID;

   /**
    * Reference to database
    */
   private SQLiteDatabase db;

   /**
    * App context
    */
   private Context context;

   /**
    * Utility for generating HTML report
    */
   private ReportGenerator reportGenerator;

   public OUAmountDistribution (Context context, int tripId)
   {
      this.context = context;
      this.db = DBUtil.getDB(context);
      this.tripId = tripId;

      Cursor cursorUser = TripUser.getTripUsers(db, tripId);
      listAllUserId   = DBUtil.getIdColumn(cursorUser, TripUser.Column._id);
      listAllUserName = DBUtil.getColumn  (cursorUser, TripUser.Column.NickName).get(0);

      this.reportGenerator = new ReportGenerator(context, tripId, listAllUserId, listAllUserName);
   }

   public static void main (String arg[])
   {
      OUAmountDistribution distrib = new OUAmountDistribution(null, 0);
      distrib.mapUserIdToOweAmount = genUserAmountList(new Integer[]{50078, 9921, 10001, -33354, -10121, -9921, -2104, -7000, -7500});
      distrib.doFindWhoOwesWhom();
      Log.i(TAG, "Lender    = " + distrib.getMapLenderToBorrowers());
      Log.i(TAG, "Borrowers = " + distrib.getMapBorrowerToLenders());
   }

   private static Map<Integer,Integer> genUserAmountList(Integer amount[])
   {
      Map<Integer,Integer> mapUserToOweAmount = new HashMap <> ();
      for (int i = 0; i < amount.length; ++i)
         mapUserToOweAmount.put(i, amount[i]);
      return mapUserToOweAmount;
   }

   public void doFindWhoOwesWhom ()
   {
      doMapUserIdToOweAmount();
      doMapLendersAndBorrowers();
   }

   private void doMapLendersAndBorrowers()
   {
      List<UserAmount> listLenderUA   = new ArrayList<>();
      List<UserAmount> listBorrowerUA = new ArrayList<>();

      Integer sum = 0;
      for (Map.Entry<Integer, Integer> entry : mapUserIdToOweAmount.entrySet())
      {
         Integer userId = entry.getKey();
         Integer amount = entry.getValue();
         sum += amount;

         if (amount > 0)
         {
            mapLenderToBorrowers.put(userId, new ArrayList<UserAmount>());
            listLenderUA.add(new UserAmount (userId, amount));
         }
         else
         {
            mapBorrowerToLenders.put(userId, new ArrayList<UserAmount>());
            listBorrowerUA.add(new UserAmount (userId, -1 * amount));
         }
      }
      Collections.sort(listLenderUA);
      Collections.sort(listBorrowerUA);
      Log.i(TAG, "Lenders=" + listLenderUA);
      Log.i(TAG, "Borrowers=" + listBorrowerUA);

      if (sum != 0)
         throw new IllegalArgumentException("The lender and borrower amounts don't add up to zero. Sum=" + sum + " UserAmount=" + mapUserIdToOweAmount);

      for (UserAmount currLenderUA : listLenderUA)
      {
         int lenderId = currLenderUA.id;
         Integer lenderAmount = currLenderUA.amount;

         int borrowerId = -1;
         Integer borrowerAmount = -1;

         Log.i(TAG, UIUtil.LOG_HR);
         Log.i(TAG, "Lender   : Id=" + lenderId + " Amount=" + OUCurrencyUtil.format(lenderAmount));

         for (Iterator<UserAmount> iter = listBorrowerUA.iterator(); iter.hasNext(); )
         {
            UserAmount borrowerQA = iter.next();
            borrowerId = borrowerQA.id;
            borrowerAmount = Math.abs(borrowerQA.amount);
            Log.i(TAG, "Borrower : Id=" + borrowerId + " Amount=" + OUCurrencyUtil.format(borrowerAmount));
            iter.remove();

            if (lenderAmount > borrowerAmount)
            {
               mapLenderToBorrowers.get(lenderId).add(new UserAmount(borrowerId, borrowerAmount));
               mapBorrowerToLenders.get(borrowerId).add(new UserAmount(lenderId, borrowerAmount));
               lenderAmount -= borrowerAmount;
               Log.i(TAG, "Borrower : Id=" + borrowerId + " Amount=" + OUCurrencyUtil.format(borrowerAmount) + " RemainingLenderAmount=" + OUCurrencyUtil.format(lenderAmount));
               borrowerAmount = 0;
            }
            else
            {
               mapLenderToBorrowers.get(lenderId).add(new UserAmount(borrowerId, lenderAmount));
               mapBorrowerToLenders.get(borrowerId).add (new UserAmount(lenderId, lenderAmount));

               borrowerAmount -= lenderAmount;
               Log.i(TAG, "Borrower : Id=" + borrowerId + " Amount=" + OUCurrencyUtil.format(lenderAmount) + " RemainingBorrowerAmount=" + OUCurrencyUtil.format(borrowerAmount));
               lenderAmount = 0;
               break;
            }
         }

         if (lenderAmount > 0)
            throw new IllegalStateException("The lender Id=" + lenderId + " could not be paid even after going throuh all borrowers");

         if (borrowerAmount > 0)
         {
            listBorrowerUA.add(new UserAmount(borrowerId, borrowerAmount));
            Collections.sort(listBorrowerUA);
            Log.i(TAG, "Revised BorrowerSet=" + listBorrowerUA);
         }
      }
   }

   /**
    * Create a Map of userIds to the amount each user owes.
    *
    * <pre>
    * If the amount is
    *    Positive number : The user has a +ve balance. He needs to get amount from others.
    *    Negative number : The user has a -ve balance. He needs to pay amount to   others.
    *
    * Note:
    * The sum of all positive and negative user balances must add up to zero!
    *
    * For each item that exists for the trip
    *    - For each user who has paid for the item ==> Add the amount paid by the user to his balance.
    *    - Find 'totalAmountPaid'      ==> Amount by all the users who paid for the item.
    *    - Find 'amountSharePerUser'   ==> Amount that needs to be paid by users who share the item.
    *    - Find 'remainderAfterShare'  ==> When 'x' amount is divided among 'n' users there can be a non-zero remainder 'r' (r < n).
    *                                      This will be shared by a subset of n users.
    * </pre>
    */
   private void doMapUserIdToOweAmount()
   {
      // Map UserId to Amount (+ve is amount to get, -ve is amount to be paid)
      for (Integer userId : listAllUserId)
         mapUserIdToOweAmount.put(userId, 0);

      Log.i(TAG, "Start stage. User and owe amount :" + mapUserIdToOweAmount);

      List<Item> listItem = Item.getItems (db, tripId);
      Log.i(TAG, "Items :" + listItem);

      reportGenerator.openReportWriter();
      reportGenerator.doWriteTripHeading();
      reportGenerator.doWriteTableExpenseBegin();

      for (Item currItem : listItem)
      {
         Log.i(TAG, UIUtil.LOG_HR);
         Log.i(TAG, "Item=" + currItem);

         int totalAmountPaid = 0;

         // For each user who has paid for the item ==> Add the amount paid by the user to his balance.
         Map<TripUser,Integer> mapPaidByUserToAmount = currItem.getPaidByUsers(db);

         // Report: Row having amount paid by each user
         reportGenerator.doWritePaidByAmount (currItem, mapPaidByUserToAmount);

         for (Map.Entry<TripUser,Integer> entry : mapPaidByUserToAmount.entrySet())
         {
            Integer userId = entry.getKey().getId();
            int  amountPaid = entry.getValue();
            totalAmountPaid += amountPaid;

            Log.i(TAG, "User=" + userId + " Amount=" + OUCurrencyUtil.format(amountPaid) + " NickName=" + listAllUserName.get(listAllUserId.indexOf(userId)));

            int amountExisting = mapUserIdToOweAmount.get(userId);
            int amountResult   = amountExisting + amountPaid;
            mapUserIdToOweAmount.put(userId, amountResult);

            // Populate mapLenderToItems
            List<ItemAmount> list = mapLenderToItems.get(userId);
            if (list == null)
               list = new ArrayList<>();
            list.add(new ItemAmount(currItem.getSummary(), amountPaid));
            mapLenderToItems.put(userId, list);
         }

         // Report: Balance after Paid-by credit
         Log.i(TAG, "After Credit :" + getOweMap(mapUserIdToOweAmount, listAllUserId, listAllUserName) + " TotalAmountPaid=" + OUCurrencyUtil.format(totalAmountPaid));
         reportGenerator.doWritePaidByAmountBalance(mapUserIdToOweAmount);

         List<TripUser> listSharedByUser = currItem.getSharedByUsers(db);
         int sharedByUserCount =  listSharedByUser.size();
         int amountSharePerUser = totalAmountPaid / sharedByUserCount;
         int remainderAfterShare = totalAmountPaid % sharedByUserCount;

         Log.i(TAG, "Shared By    :" + listSharedByUser + " AmountSharePerUser=" + OUCurrencyUtil.format(amountSharePerUser) + " remainderAfterShare=" + remainderAfterShare);
         reportGenerator.doWriteSharedByAmount(listSharedByUser, amountSharePerUser, remainderAfterShare);

         for (TripUser currUser : listSharedByUser)
         {
            Integer userId = currUser.getId();
            int amountExisting = mapUserIdToOweAmount.get(userId);
            int amountResult = amountExisting - amountSharePerUser;
            if (remainderAfterShare > 0)
            {
               --remainderAfterShare;
               --amountResult;
            }
            mapUserIdToOweAmount.put(userId, amountResult);
         }

         Log.i(TAG, "After Debit :" + getOweMap(mapUserIdToOweAmount, listAllUserId, listAllUserName));
         reportGenerator.doWriteSharedByAmountBalance(mapUserIdToOweAmount);
      }

      reportGenerator.doWriteTableExpenseEnd();
      reportGenerator.closeReportWriter();
   }

   private String getOweMap (Map<Integer,Integer> mapUserAmount, List<Integer> listAllUserId, List<String>  listAllUserName)
   {
      StringBuilder builder = new StringBuilder();
      for (Map.Entry<Integer,Integer> entry : mapUserAmount.entrySet())
      {
         int index = listAllUserId.indexOf(entry.getKey());
         String userName = listAllUserName.get(index);
         String amount   = OUCurrencyUtil.format (entry.getValue());
         builder.append(userName + "=" + amount).append(" ");
      }
      return builder.toString();
   }

   public Map<Integer,Integer> getMapUserIdToOweAmount ()
   {
      return mapUserIdToOweAmount;
   }

   public Map<Integer,List<ItemAmount>> getMapLenderToItems ()
   {
      return mapLenderToItems;
   }

   public Map<Integer,List<UserAmount>> getMapBorrowerToLenders ()
   {
      return mapBorrowerToLenders;
   }

   public Map<Integer,List<UserAmount>> getMapLenderToBorrowers ()
   {
      return mapLenderToBorrowers;
   }

   public List<Integer> getListAllUserId ()
   {
      return listAllUserId;
   }

   public List<String> getListAllUserName ()
   {
      return listAllUserName;
   }

   public static class ItemAmount
   {
      private int amount;

      private String summary;

      public ItemAmount (String summary, int amount)
      {
         this.summary = summary;
         this.amount = amount;
      }

      public String getSummary()
      {
         return summary;
      }

      public int getAmount()
      {
         return amount;
      }
   }

   public static class UserAmount implements Comparable<UserAmount>
   {
      private static final int HASH_PRIME = 31;

      private int id;

      private int amount;

      public UserAmount (int id, int amount)
      {
         this.id = id;
         this.amount = amount;
      }

      public int getId ()
      {
         return id;
      }

      public int getAmount ()
      {
         return amount;
      }

      @Override
      public int hashCode()
      {
         int result = 1;
         result = result * HASH_PRIME + Integer.valueOf(id).hashCode();
         result = result * HASH_PRIME + Integer.valueOf(amount).hashCode();
         return result;
      };

      @Override
      public boolean equals (Object obj)
      {
         if (this == obj)
            return true;

         if (!(obj instanceof UserAmount))
            return false;

         UserAmount user = (UserAmount)obj;
         return (this.id == user.id && this.amount == user.amount);
      }

      @Override
      public int compareTo (UserAmount userAmount)
      {
         if (this.equals(userAmount))
            return 0;

         int amountSortOrder = -1 * (int)(Math.abs(this.amount) - Math.abs(userAmount.amount));
         return (amountSortOrder == 0) ? (this.id - userAmount.id) : amountSortOrder;
      }

      @Override
      public String toString ()
      {
         return "(" + id + "," + OUCurrencyUtil.format(amount) + ")";
      }
   }

}


