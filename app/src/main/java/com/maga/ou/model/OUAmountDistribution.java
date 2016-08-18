package com.maga.ou.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.maga.ou.model.util.DBUtil;
import com.maga.ou.util.OUCurrencyUtil;
import com.maga.ou.util.UIUtil;

import java.util.*;

/**
 * Created by rbseshad on 16-Aug-16.
 */
public class OUAmountDistribution
{
   private static final String TAG = "ou." + OUAmountDistribution.class.getSimpleName();

   private Map<Integer,List<UserAmount>> mapLenderToBorrowers = new TreeMap<> ();

   private Map<Integer,List<UserAmount>> mapBorrowerToLenders = new TreeMap<> ();

   private Map<Integer,Integer> mapUserIdToOweAmount = new TreeMap<>();

   private List<Integer> listAllUserId = new ArrayList<>();

   private List<String> listAllUserName = new ArrayList<>();

   private int tripId = DBUtil.UNSET_ID;

   private SQLiteDatabase db;

   public OUAmountDistribution (SQLiteDatabase db, int tripId)
   {
      this.db = db;
      this.tripId = tripId;
   }

   public static void main (String arg[])
   {
      OUAmountDistribution distrib = new OUAmountDistribution(null, 0);
      distrib.mapUserIdToOweAmount = genUserAmountList(new Integer[]{50078, 9921, 10001, -33354, -10121, -9921, -2104, -7000, -7500});
      distrib.doFindWhoOwesWhom();
      Log.d(TAG, "Lender    = " + distrib.getMapLenderToBorrowers());
      Log.d(TAG, "Borrowers = " + distrib.getMapBorrowerToLenders());
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
      Cursor cursorUser = TripUser.getTripUsers(db, tripId);
      listAllUserId   = DBUtil.getIdColumn(cursorUser, TripUser.Column._id);
      listAllUserName = DBUtil.getColumn  (cursorUser, TripUser.Column.NickName).get(0);

      doMapUserIdToOweAmount();
      doMapLendersToBorrowers();
   }

   public void doMapLendersToBorrowers()
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
      Log.d(TAG, "Lenders=" + listLenderUA);
      Log.d(TAG, "Borrowers=" + listBorrowerUA);

      if (sum != 0)
         throw new IllegalArgumentException("The lender and borrower amounts don't add up to zero. Sum=" + sum + " UserAmount=" + mapUserIdToOweAmount);

      for (UserAmount currLenderUA : listLenderUA)
      {
         int lenderId = currLenderUA.id;
         Integer lenderAmount = currLenderUA.amount;

         int borrowerId = -1;
         Integer borrowerAmount = -1;

         Log.d(TAG, UIUtil.LOG_HR);
         Log.d(TAG, "Lender   : Id=" + lenderId + " Amount=" + OUCurrencyUtil.format(lenderAmount));

         for (Iterator<UserAmount> iter = listBorrowerUA.iterator(); iter.hasNext(); )
         {
            UserAmount borrowerQA = iter.next();
            borrowerId = borrowerQA.id;
            borrowerAmount = Math.abs(borrowerQA.amount);
            Log.d(TAG, "Borrower : Id=" + borrowerId + " Amount=" + OUCurrencyUtil.format(borrowerAmount));
            iter.remove();

            if (lenderAmount > borrowerAmount)
            {
               // 500 -300
               mapLenderToBorrowers.get(lenderId).add(new UserAmount(borrowerId, borrowerAmount));
               mapBorrowerToLenders.get(borrowerId).add(new UserAmount(lenderId, borrowerAmount));
               lenderAmount -= borrowerAmount;
               Log.d(TAG, "Borrower : Id=" + borrowerId + " Amount=" + OUCurrencyUtil.format(borrowerAmount) + " RemainingLenderAmount=" + OUCurrencyUtil.format(lenderAmount));
               borrowerAmount = 0;
            }
            else
            {
               // -300 500
               mapLenderToBorrowers.get(lenderId).add(new UserAmount(borrowerId, lenderAmount));
               mapBorrowerToLenders.get(borrowerId).add (new UserAmount(lenderId, lenderAmount));

               borrowerAmount -= lenderAmount;
               Log.d(TAG, "Borrower : Id=" + borrowerId + " Amount=" + OUCurrencyUtil.format(lenderAmount) + " RemainingBorrowerAmount=" + OUCurrencyUtil.format(borrowerAmount));
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
            Log.d(TAG, "Revised BorrowerSet=" + listBorrowerUA);
         }
      }
   }

   private void doMapUserIdToOweAmount()
   {
      // Map UserId to Amount (+ve is amount to get, -ve is amount to be paid)
      for (Integer userId : listAllUserId)
         mapUserIdToOweAmount.put(userId, 0);

      Log.d(TAG, "Start stage. User and owe amount :" + mapUserIdToOweAmount);

      List<Item> listItem = Item.getItems (db, tripId);
      Log.d(TAG, "Items :" + listItem);

      for (Item currItem : listItem)
      {
         Log.d(TAG, UIUtil.LOG_HR);
         Log.d(TAG, "Item=" + currItem);
         int totalAmountPaid = 0;
         Map<TripUser,Integer> mapPaidByUserToAmount = currItem.getPaidByUsers(db);
         for (Map.Entry<TripUser,Integer> entry : mapPaidByUserToAmount.entrySet())
         {
            Integer userId = entry.getKey().getId();
            int  amountPaid = entry.getValue();
            totalAmountPaid += amountPaid;

            Log.d(TAG, "User=" + userId + " Amount=" + OUCurrencyUtil.format(amountPaid) + " NickName=" + listAllUserName.get(listAllUserId.indexOf(userId)));

            int amountExisting = mapUserIdToOweAmount.get(userId);
            int amountResult   = amountExisting + amountPaid;
            mapUserIdToOweAmount.put(userId, amountResult);
         }

         Log.d(TAG, "After Credit :" + getOweMap(mapUserIdToOweAmount, listAllUserId, listAllUserName) + " TotalAmountPaid=" + OUCurrencyUtil.format(totalAmountPaid));

         List<TripUser> listSharedByUser = currItem.getSharedByUsers(db);
         int sharedByUserCount =  listSharedByUser.size();
         int amountSharePerUser = totalAmountPaid / sharedByUserCount;
         int remainderAfterShare = totalAmountPaid % sharedByUserCount;
         Log.d(TAG, "Shared By    :" + listSharedByUser + " AmountSharePerUser=" + OUCurrencyUtil.format(amountSharePerUser) + " remainderAfterShare=" + remainderAfterShare);


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
         Log.d(TAG, "After Debit :" + getOweMap(mapUserIdToOweAmount, listAllUserId, listAllUserName));
      }
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


   public static class UserAmount implements Comparable<UserAmount>
   {
      private static final int HASH_PRIME = 31;

      private int id;

      private Integer amount;

      public UserAmount (int id, Integer amount)
      {
         this.id = id;
         this.amount = amount;
      }

      public Integer getId ()
      {
         return id;
      }

      public Integer getAmount ()
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


