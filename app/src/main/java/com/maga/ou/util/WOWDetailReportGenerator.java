package com.maga.ou.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.maga.ou.model.Item;
import com.maga.ou.model.OUAmountDistribution;
import com.maga.ou.model.Trip;
import com.maga.ou.model.TripUser;
import com.maga.ou.model.util.DBUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WOWDetailReportGenerator
{
   private static final String ROW_HEADING_PAID_BY = "Paid-by amount";

   private static final String ROW_HEADING_PAID_BY_BALANCE = "Balance after paid-by credit";

   private static final String ROW_HEADING_SHARED_BY = "Shared-by amount";

   private static final String ROW_HEADING_SHARED_BY_BALANCE = "Balance after shared-by debit";

   /**
    * File having HTML report prefix
    */
   private static final String FILENAME_REPORT_BEGIN = "report.detail.begin.txt";

   private static final String FILENAME_REPORT_CALCULATION = "report.detail.calculation.txt";

   private static final String FILENAME_REPORT_END = "report.detail.end.txt";

   private static final String TAB = ReportGeneratorUtil.TAB;

   /*
    * Member variables
    */

   /**
    * Current trip ID
    */
   private int tripId = DBUtil.UNSET_ID;

   /**
    * Reference to database
    */
   private SQLiteDatabase db;

   /**
    * List of all user IDs
    */
   private List<Integer> listAllUserId;

   /**
    * List of all user names
    */
   private List<String> listAllUserName;

   /**
    * Current tab
    */
   private String tab = TAB;

   /**
    * Utitlity for report generation
    */
   private ReportGeneratorUtil reportUtil = null;

   public WOWDetailReportGenerator(Context context, int tripId, List<Integer> listAllUserId, List<String> listAllUserName)
   {
      this.tripId  = tripId;
      this.listAllUserId = listAllUserId;
      this.listAllUserName = listAllUserName;
      this.db = DBUtil.getDB (context);
      this.reportUtil = new ReportGeneratorUtil(context, getHtmlReportName(context, tripId));
   }

   public static File getHtmlReport(Context context, int tripId)
   {
      return ReportGeneratorUtil.getExternalDocumentFile(context, getHtmlReportName(context, tripId));
   }

   private static String getHtmlReportName (Context context, int tripId)
   {
      String tripName = Trip.getInstance(DBUtil.getDB (context), tripId).getName();
      tripName = tripName.trim().replaceAll("\\s+", "_");
      return tripName + "_Detail.html";
   }

   public void doWriteTripHeading()
   {
      String tripName = Trip.getInstance(db, tripId).getName();
      reportUtil.writeln(tab, "<h1>Report - %s </h1>", tripName);
      reportUtil.writeln(tab, "<p/>");
   }

   public void doWriteTableExpenseBegin ()
   {
      reportUtil.writeln(tab, "<h2>Report of trip expenses");
      reportUtil.writeln(tab, "<span class='toggleText'><a href='#' onclick='toggle(this, \"reportExpense\");'>Hide</a></span>");
      reportUtil.writeln(tab, "</h2>");

      // Begin Div - TripExpense
      reportUtil.writeln(tab, "<div id='reportExpense'>");

      // Append the calculation file details
      reportUtil.appendReportFile (FILENAME_REPORT_CALCULATION);

      // Begin Table
      reportUtil.writeln(tab, "<table class='grid'>");
      tab = tab + TAB;

      // Begin Table Heading
      reportUtil.writeln(tab, "<tr>");
      tab = tab + TAB;

      reportUtil.writeln(tab, "<th>Item</th>");
      reportUtil.writeln(tab, "<th>Transaction</th>");
      for (String currUser : listAllUserName)
         reportUtil.writeln(tab, "<th>%s</th>", currUser);

      // End Table Heading
      tab = tab.substring(TAB.length());
      reportUtil.writeln(tab, "</tr>");
   }

   public void doWritePaidByAmount (Item item, Map<TripUser,Integer> mapPaidByUserToAmount)
   {
      reportUtil.writeln(tab, "<tr>");
      tab = tab + TAB;

      reportUtil.writeln(tab, "<td colspan='1' rowspan='4'>%s</td>", item.getSummary());

      // Write the amount for each user
      reportUtil.writeln(tab, "<td>%s</td>", ROW_HEADING_PAID_BY);
      for (int userAmount : getAmount(mapPaidByUserToAmount))
         doWriteCellAmount(userAmount);

      tab = tab.substring(TAB.length());
      reportUtil.writeln(tab, "</tr>");
   }

   public void doWritePaidByAmountBalance (Map<Integer,Integer> mapUserIdToOweAmount)
   {
      doWriteAmountBalance(mapUserIdToOweAmount, ROW_HEADING_PAID_BY_BALANCE);
   }

   public void doWriteSharedByAmount (List<TripUser>listSharedByUser, int amountSharePerUser, int remainderAfterShare)
   {
      reportUtil.writeln(tab, "<tr>");
      tab = tab + TAB;

      int amount[] = new int [listAllUserId.size()];
      for (TripUser currUser : listSharedByUser)
      {
         int userIndex  = listAllUserId.indexOf(currUser.getId());
         amount[userIndex] = (remainderAfterShare > 0) ? amountSharePerUser+1 : amountSharePerUser;
         if (remainderAfterShare > 0)
            --remainderAfterShare;
      }

      reportUtil.writeln(tab, "<td>%s</td>", ROW_HEADING_SHARED_BY);
      for (int currAmount : amount)
         doWriteCellAmount (currAmount);

      tab = tab.substring(TAB.length());
      reportUtil.writeln(tab, "</tr>");
   }

   public void doWriteSharedByAmountBalance (Map<Integer,Integer> mapUserIdToOweAmount)
   {
      doWriteAmountBalance(mapUserIdToOweAmount, ROW_HEADING_SHARED_BY_BALANCE);
   }

   private void doWriteAmountBalance (Map<Integer,Integer> mapUserIdToOweAmount, String rowHeading)
   {
      if (rowHeading.equals(ROW_HEADING_PAID_BY_BALANCE))
         reportUtil.writeln(tab, "<tr class='highlightCredit'>");
      else
         reportUtil.writeln(tab, "<tr class='highlightDebit'>");
      tab = tab + TAB;

      reportUtil.writeln(tab, "<td>%s</td>", rowHeading);
      for (int userId : listAllUserId)
         doWriteCellAmount(mapUserIdToOweAmount.get(userId), false);

      tab = tab.substring(TAB.length());
      reportUtil.writeln(tab, "</tr>");
   }

   public void doWriteTableExpenseEnd()
   {
      // End Table
      tab = tab.substring(TAB.length());
      reportUtil.writeln(tab, "</table>");

      // End Div - Trip Expense
      reportUtil.writeln(tab, "</div>");
   }

   public void doWriteTableWOW (Map<Integer,List<OUAmountDistribution.UserAmount>> mapLenderToBorrowers, Set<Integer> setBorrower)
   {
      List<Integer> listBorrower = new ArrayList<>(setBorrower);
      doWriteTableWOWBegin(listBorrower);

      for (Map.Entry<Integer,List<OUAmountDistribution.UserAmount>> entry : mapLenderToBorrowers.entrySet())
      {
         Integer lenderId = entry.getKey();
         List<OUAmountDistribution.UserAmount> listBorrowerAmount = entry.getValue();

         // Begin Table Row
         reportUtil.writeln(tab, "<tr>");
         tab = tab + TAB;

         // Lender Name
         reportUtil.writeln(tab, "<th>%s</th>", getUserName(lenderId));

         // Borrower Amount
         int amount[] = new int[listBorrower.size()];
         for (OUAmountDistribution.UserAmount borrowerAmount : listBorrowerAmount)
         {
            int borrowerIndex = listBorrower.indexOf(borrowerAmount.getId());
            amount[borrowerIndex] = borrowerAmount.getAmount();
         }

         int totalAmount = 0;
         for (int currAmount : amount)
         {
            doWriteCellAmount(currAmount);
            totalAmount += currAmount;
         }
         doWriteCellTotalAmount(totalAmount);

         // End Table Row
         tab = tab.substring(TAB.length());
         reportUtil.writeln(tab, "</tr>");
      }
      doWriteTableWOWEnd();
   }

   private void doWriteTableWOWBegin (List<Integer> listBorrower)
   {
      reportUtil.writeln(tab, "<h2> Report of who owes whom");
      reportUtil.writeln(tab, "<span class='toggleText'><a href='#' onclick='toggle(this, \"reportWOW\");'>Hide</a></span>");
      reportUtil.writeln(tab, "</h2>");

      // Begin Div - TripExpense
      reportUtil.writeln(tab, "<div id='reportWOW'>");
      tab = tab + TAB;

      // Begin Table
      reportUtil.writeln(tab, "<table class='grid'>");
      tab = tab + TAB;

      // Begin Table Row Heading- Top Row
      reportUtil.writeln(tab, "<tr>");
      tab = tab + TAB;

      // Emtpy Cell
      reportUtil.writeln(tab, "<th rowspan='2'></th>");

      // Span borrower names
      reportUtil.writeln(tab, "<th colspan='%d'>Borrowers - Members who owe</th>", listBorrower.size());

      // Total
      reportUtil.writeln(tab, "<th rowspan='2'>Total</th>");
      tab = tab.substring(TAB.length());

      // End Table Row Heading- Top Row
      reportUtil.writeln(tab, "</tr>");

      // Begin Table Row
      reportUtil.writeln(tab, "<tr>");
      tab = tab + TAB;

      // Table Row Heading - Borrower Names
      for (Integer currBorrowerId : listBorrower)
         reportUtil.writeln(tab, "<th>%s</th>", getUserName(currBorrowerId));

      // End Table Row
      tab = tab.substring(TAB.length());
      reportUtil.writeln(tab, "</tr>");
   }

   private void doWriteTableWOWEnd ()
   {
      // End Table
      tab = tab.substring(TAB.length());
      reportUtil.writeln(tab, "</table>");

      // End Div - WOW
      reportUtil.writeln(tab, "</div>");
   }

   private int[] getAmount (Map<TripUser,Integer> mapPaidByUserToAmount)
   {
      int amount[] = new int [listAllUserId.size()];

      for (Map.Entry<TripUser,Integer> entry : mapPaidByUserToAmount.entrySet())
      {
         int userIndex = listAllUserId.indexOf(entry.getKey().getId());
         int userAmount = entry.getValue();
         amount[userIndex] = userAmount;
      }
      return amount;
   }

   private void doWriteCellTotalAmount (int amount)
   {
      if (amount == 0)
         reportUtil.writeln(tab, "<th class='empty highlight'></th>");
      else
         reportUtil.writeln(tab, "<th class='amount highlight'>%s</th>", OUCurrencyUtil.format(amount));
   }

   private void doWriteCellAmount (int amount)
   {
      doWriteCellAmount (amount, true);
   }

   private void doWriteCellAmount (int amount, boolean isEmptyOnZero)
   {
      if (isEmptyOnZero && amount == 0)
         reportUtil.writeln(tab, "<td class='empty'></td>");
      else
         reportUtil.writeln(tab, "<td class='amount'>%s</td>", OUCurrencyUtil.format(amount));
   }

   private int getUserIndex(Integer userId)
   {
      return listAllUserId.indexOf(userId);
   }

   private String getUserName(Integer userId)
   {
      return listAllUserName.get(getUserIndex(userId));
   }

   public void openReportWriter()
   {
      reportUtil.openReportWriter(FILENAME_REPORT_BEGIN);
   }


   public void closeReportWriter()
   {
      reportUtil.closeReportWriter(FILENAME_REPORT_END);
   }
}
