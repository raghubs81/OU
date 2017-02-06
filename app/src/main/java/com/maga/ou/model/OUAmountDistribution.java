package com.maga.ou.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.maga.ou.model.util.DBUtil;
import com.maga.ou.util.PDFGenerator;
import com.maga.ou.util.ReportGeneratorUtil;
import com.maga.ou.util.WOWDetailReportGenerator;
import com.maga.ou.util.OUCurrencyUtil;
import com.maga.ou.util.UIUtil;

import java.io.File;
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
    * Utility for generating basic HTML report
    */
   private WOWBasicReportGenerator basicReportGenerator;

   /**
    * Utility for generating HTML report
    */
   private WOWDetailReportGenerator detailReportGenerator;

   public OUAmountDistribution (Context context, int tripId)
   {
      this.context = context;
      this.db = DBUtil.getDB(context);
      this.tripId = tripId;

      Cursor cursorUser = TripUser.getTripUsers(db, tripId);
      listAllUserId   = DBUtil.getIdColumn(cursorUser, TripUser.Column._id);
      listAllUserName = DBUtil.getColumn  (cursorUser, TripUser.Column.NickName).get(0);

      this.detailReportGenerator = new WOWDetailReportGenerator(context, tripId, listAllUserId, listAllUserName);
      this.basicReportGenerator = new WOWBasicReportGenerator();
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

   /**
    * Find who owes whom (WOW), populate maps and generate detailed HTML report.
    *
    * Following maps populated.
    * <ul>
    *    <li>mapLenderToBorrowers</li>
    *    <li>mapBorrowerToLenders</li>
    *    <li>mapLenderToItems</li>
    *    <li>mapUserIdToOweAmount</li>
    * </ul>
    */
   public void doFindWhoOwesWhom ()
   {
      detailReportGenerator.openReportWriter();
      detailReportGenerator.doWriteTripHeading();
      doMapUserIdToOweAmount();
      doMapLendersAndBorrowers();
      detailReportGenerator.closeReportWriter();
   }

   /**
    * Invoke {@code doFindWhoOwesWhom()} and also generate basic PDF report.
    */
   public void doGenerateBasicReport()
   {
      doFindWhoOwesWhom ();
      basicReportGenerator.doGenerateReport();
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

      // Generate report of who owes whom
      detailReportGenerator.doWriteTableWOW(mapLenderToBorrowers, mapBorrowerToLenders.keySet());
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

      List<Item> listItem = Item.getItems(db, tripId);
      Log.i(TAG, "Items :" + listItem);

      detailReportGenerator.doWriteTableExpenseBegin();

      for (Item currItem : listItem)
      {
         Log.i(TAG, UIUtil.LOG_HR);
         Log.i(TAG, "Item=" + currItem);

         int totalAmountPaid = 0;

         // For each user who has paid for the item ==> Add the amount paid by the user to his balance.
         Map<TripUser,Integer> mapPaidByUserToAmount = currItem.getPaidByUsers(db);

         // Report: Row having amount paid by each user
         detailReportGenerator.doWritePaidByAmount(currItem, mapPaidByUserToAmount);

         for (Map.Entry<TripUser,Integer> entry : mapPaidByUserToAmount.entrySet())
         {
            Integer userId = entry.getKey().getId();
            int  amountPaid = entry.getValue();
            totalAmountPaid += amountPaid;

            Log.i(TAG, "User=" + userId + " Amount=" + OUCurrencyUtil.format(amountPaid) + " NickName=" + getNickName(userId));

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
         detailReportGenerator.doWritePaidByAmountBalance(mapUserIdToOweAmount);

         List<TripUser> listSharedByUser = currItem.getSharedByUsers(db);
         int sharedByUserCount =  listSharedByUser.size();
         int amountSharePerUser = totalAmountPaid / sharedByUserCount;
         int remainderAfterShare = totalAmountPaid % sharedByUserCount;

         Log.i(TAG, "Shared By    :" + listSharedByUser + " AmountSharePerUser=" + OUCurrencyUtil.format(amountSharePerUser) + " remainderAfterShare=" + remainderAfterShare);
         detailReportGenerator.doWriteSharedByAmount(listSharedByUser, amountSharePerUser, remainderAfterShare);

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
         detailReportGenerator.doWriteSharedByAmountBalance(mapUserIdToOweAmount);
      }

      detailReportGenerator.doWriteTableExpenseEnd();
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

      public static int getTotalAmount (List<ItemAmount> listItemAmount)
      {
         if (listItemAmount == null)
            return  0;

         int sum = 0;
         for (ItemAmount itemAmount : listItemAmount)
            sum += itemAmount.getAmount();
         return sum;
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

   public String getNickName (int userId)
   {
      return listAllUserName.get(listAllUserId.indexOf(userId));
   }

   public static File getPDFReport(Context context, int tripId)
   {
      File htmlFile = ReportGeneratorUtil.getExternalDocumentFile(context, getHtmlReportName(context, tripId));
      return PDFGenerator.toPdf(htmlFile);
   }

   private static String getHtmlReportName (Context context, int tripId)
   {
      String tripName = Trip.getInstance(DBUtil.getDB (context), tripId).getName();
      tripName = tripName.trim().replaceAll("\\s+", "_");
      return tripName + "_Basic.html";
   }

   private class WOWBasicReportGenerator
   {
      private static final String FILENAME_REPORT_BEGIN = "report.basic.begin.txt";

      private static final String FILENAME_REPORT_END = "report.basic.end.txt";

      private static final String TAB = ReportGeneratorUtil.TAB;

      private String tab = TAB;

      private Map<Integer,String[]> mapUserToExpenseSummary = new HashMap<>();

      /**
       * Html report name
       */
      private String htmlReportName = null;

      /**
       * Utitlity for report generation
       */
      private ReportGeneratorUtil reportUtil = null;

      public WOWBasicReportGenerator ()
      {
         htmlReportName = getHtmlReportName(context, tripId);
         this.reportUtil = new ReportGeneratorUtil(context, htmlReportName);
      }

      public File doGenerateReport()
      {
         doGenerateHtmlReport();
         return PDFGenerator.toPdf(reportUtil.getReportFile());
      }

      private void doGenerateHtmlReport ()
      {
         populateSummary ();

         reportUtil.openReportWriter(FILENAME_REPORT_BEGIN);
         writeSummaryTable();
         writeDetails();
         reportUtil.closeReportWriter(FILENAME_REPORT_END);
      }

      private void writeSummaryTable ()
      {
         final String HEADING_SUMMARY = "Summary";
         final String INFO_SUMMARY = "<div>A positive OU indicates the amount your friends owe you. A negative value indicates the amount you owe. </div>";

         reportUtil.writeln(tab, "<h1>%s</h1>", HEADING_SUMMARY);
         reportUtil.writeln(tab, INFO_SUMMARY);
         reportUtil.writeln(tab, "<p/>");

         // Begin Table
         reportUtil.writeln(tab, "<table class='grid'>");
         tab = tab + TAB;

         // Heading Row
         reportUtil.writeln(tab, "<tr>");
         tab = tab + TAB;
         String heading[] = new String[] {"Name", "Amount Spent<br/>(A)", "Trip Expense<br/>(B)", "Owe You<br/>(OU=A-B)"};
         for (String currHeading : heading)
            reportUtil.writeln(tab, "<th>%s</th>", ReportGeneratorUtil.toNoWrap(currHeading));
         tab = tab.substring(TAB.length());
         reportUtil.writeln(tab, "</tr>");

         // A row for how much each user OU amount
         for (int userId : listAllUserId)
         {
            reportUtil.writeln(tab, "<tr>");
            tab = tab + TAB;
            reportUtil.writeln(tab, "<td>%s</td>", getNickName(userId));
            for (String summary : mapUserToExpenseSummary.get(userId))
               reportUtil.writeln(tab, "<td class='amount'>%s</td>", summary);
            tab = tab.substring(TAB.length());
            reportUtil.writeln(tab, "</tr>");
         }

         // End Table
         tab = tab.substring(TAB.length());
         reportUtil.writeln(tab, "</table>");
      }

      private void writeDetails ()
      {
         final String HEADING_DETAIL = "Details";

         reportUtil.writeln(tab, "<h1>%s</h1>", HEADING_DETAIL);
         reportUtil.writeln(tab, "<p/>");
         for (int userId : listAllUserId)
            writeDetails(userId);
      }

      private void writeDetails (int userId)
      {
         final String INFO_LENDER   = "<div>The trip costed you <b>%s</b>. However, you spent <b>%s</b>. Friends owe you <b>%s</b></div>";
         final String INFO_BORROWER = "<div>The trip costed you <b>%s</b>. However, you spent <b>%s</b>. You owe your friends <b>%s</b></div>";

         reportUtil.writeln(tab, "");
         reportUtil.writeln(tab, "<h2>%s</h2>", getNickName(userId));

         int ouAmount = mapUserIdToOweAmount.get(userId);
         String summary[] = mapUserToExpenseSummary.get(userId);
         if (summary[2].startsWith("-"))
            summary[2] = OUCurrencyUtil.format(-1 * ouAmount);

         if (ouAmount < 0)
            reportUtil.writeln(tab, INFO_BORROWER, summary);
         else
            reportUtil.writeln(tab, INFO_LENDER, summary);

         writeWOWTable (userId);
         writeExpensesTable(userId);
      }

      private void writeWOWTable (int userId)
      {
         final String HEADING_WOW = "Who owes whom";
         final String TH_NAME = "Name";
         final String TH_OU_GET = ReportGeneratorUtil.toNoWrap("Amount owed to you");
         final String TH_OU_PAY = ReportGeneratorUtil.toNoWrap("Amount you owe");
         final String TH_TOTAL = "Total";

         boolean isLender = mapLenderToBorrowers.containsKey(userId);
         List<UserAmount> listUserAmount = (isLender) ? mapLenderToBorrowers.get(userId) : mapBorrowerToLenders.get(userId);
         int ouAmount = mapUserIdToOweAmount.get(userId);

         // Heading
         reportUtil.writeln(tab, "<h3>%s</h3>", HEADING_WOW);

         // Begin Table
         reportUtil.writeln(tab, "<table class='grid'>");
         tab = tab + TAB;

         // Table heading
         reportUtil.writeln(tab, "<tr><th>%s</th><th>%s</th></tr>", TH_NAME, (ouAmount >= 0) ? TH_OU_GET : TH_OU_PAY);

         // User and amount owed
         for (UserAmount userAmount : listUserAmount)
            reportUtil.writeln(tab, "<tr><td>%s</td><td class='amount'>%s</td></tr>", getNickName(userAmount.getId()), OUCurrencyUtil.format(userAmount.getAmount()));
         reportUtil.writeln(tab, "<tr><th>%s</th><th class='amount'>%s</th></tr>", TH_TOTAL, OUCurrencyUtil.format(Math.abs(ouAmount)));

         // End Table
         tab = tab.substring(TAB.length());
         reportUtil.writeln(tab, "</table>");
      }

      private void writeExpensesTable (int userId)
      {
         List<ItemAmount> listItemAmount = mapLenderToItems.get(userId);
         if (listItemAmount == null)
            return;

         final String HEADING_EXPRENSES = "Expenses";
         final String TH_NAME = "Item";
         final String TH_TOTAL = "Total";
         final String TH_OU_EXPENSE = ReportGeneratorUtil.toNoWrap("Amount spent");

         // Heading
         reportUtil.writeln(tab, "<h3>%s</h3>", HEADING_EXPRENSES);

         // Begin Table
         reportUtil.writeln(tab, "<table class='grid'>");
         tab = tab + TAB;

         // Table heading
         reportUtil.writeln(tab, "<tr><th>%s</th><th>%s</th></tr>", TH_NAME, TH_OU_EXPENSE);

         // User and amount owed
         for (ItemAmount itemAmount : listItemAmount)
            reportUtil.writeln(tab, "<tr><td>%s</td><td class='amount'>%s</td></tr>", itemAmount.getSummary(), OUCurrencyUtil.format(itemAmount.getAmount()));
         reportUtil.writeln(tab, "<tr><th>%s</th><th class='amount'>%s</th></tr>", TH_TOTAL, mapUserToExpenseSummary.get(userId)[0]);

         // End Table
         tab = tab.substring(TAB.length());
         reportUtil.writeln(tab, "</table>");
      }

      /**
       * Create a map of userId to array of {@code <AmountSpent> <TripExpense> <OU Amount>}
       */
      private void populateSummary ()
      {
         for (int userId : listAllUserId)
         {
            String summary[] = new String [3];
            int totalAmountSpent = ItemAmount.getTotalAmount(mapLenderToItems.get(userId));
            int ouAmount = mapUserIdToOweAmount.get(userId);
            int tripExpense = totalAmountSpent - ouAmount;

            int index = 0;
            for (int amount : new int[]{totalAmountSpent, tripExpense, ouAmount})
               summary[index++] = OUCurrencyUtil.format(amount);
            mapUserToExpenseSummary.put(userId, summary);
         }
      }
   }

}


