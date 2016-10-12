package com.maga.ou.model.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.maga.ou.R;
import com.maga.ou.model.Item;
import com.maga.ou.model.OUAmountDistribution;
import com.maga.ou.model.Trip;
import com.maga.ou.model.TripUser;
import com.maga.ou.util.OUCurrencyUtil;
import com.maga.ou.util.UIUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by rbseshad on 07-Oct-16.
 */
public class ReportGenerator
{
   private static final String TAG = "ou." + ReportGenerator.class.getSimpleName();

   private static final String ROW_HEADING_PAID_BY = "Paid-by amount";

   private static final String ROW_HEADING_PAID_BY_BALANCE = "Balance after paid-by credit";

   private static final String ROW_HEADING_SHARED_BY = "Shared-by amount";

   private static final String ROW_HEADING_SHARED_BY_BALANCE = "Balance after shared-by debit";

   /**
    * File having HTML report prefix
    */
   private static final String fileReportBegin = "report.begin.txt";

   private static final String fileReportEnd   = "report.end.txt";

   private static final String TAB = "   ";

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
    * App context
    */
   private Context context;

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
    * The report file
    */
   private File fileReport;

   /**
    * Writer of the report
    */
   private PrintWriter out = null;

   public ReportGenerator (Context context, int tripId, List<Integer> listAllUserId, List<String> listAllUserName)
   {
      this.context = context;
      this.tripId  = tripId;
      this.listAllUserId = listAllUserId;
      this.listAllUserName = listAllUserName;
      this.db = DBUtil.getDB(context);

      String filename = "TripReport_" + Trip.getLiteInstance(db, tripId).getName().replaceAll(" ", "_") + ".html";
      fileReport = new File (context.getFilesDir(), filename);
   }

   public void doWriteTripHeading()
   {
      String tripName = Trip.getInstance(db, tripId).getName();
      doWrite ("<h1>Payment Report - %s </h1>", tripName);
      doWrite (tab + "<p/>");
   }

   public void doWriteTableExpenseBegin ()
   {
      doWrite ("<h2> Report of expense computation</h2>");

      // Begin Table
      doWrite ("<table class='grid'>");
      tab = tab + TAB;

      // Begin Table Heading
      doWrite ("<tr>");
      tab = tab + TAB;

      doWrite ("<th>Item</th>");
      doWrite ("<th>Transaction</th>");
      for (String currUser : listAllUserName)
         doWrite("<th>%s</th>", currUser);

      // End Table Heading
      tab = tab.substring(TAB.length());
      doWrite ("</tr>");
   }

   public void doWritePaidByAmount (Item item, Map<TripUser,Integer> mapPaidByUserToAmount)
   {
      doWrite ("<tr>");
      tab = tab + TAB;

      doWrite("<td colspan='1' rowspan='4'>%s</td>", item.getSummary());

      // Write the amount for each user
      doWrite("<td>%s</td>", ROW_HEADING_PAID_BY);
      for (int userAmount : getAmount(mapPaidByUserToAmount))
         doWriteCellAmount(userAmount);

      tab = tab.substring(TAB.length());
      doWrite("</tr>");
   }

   public void doWritePaidByAmountBalance (Map<Integer,Integer> mapUserIdToOweAmount)
   {
      doWriteAmountBalance(mapUserIdToOweAmount, ROW_HEADING_PAID_BY_BALANCE);
   }

   public void doWriteSharedByAmount (List<TripUser>listSharedByUser, int amountSharePerUser, int remainderAfterShare)
   {
      doWrite ("<tr>");
      tab = tab + TAB;

      int amount[] = new int [listAllUserId.size()];
      for (TripUser currUser : listSharedByUser)
      {
         int userIndex  = listAllUserId.indexOf(currUser.getId());
         int userAmount = amountSharePerUser;
         amount[userIndex] = (remainderAfterShare > 0) ? userAmount+1 : userAmount;
         if (remainderAfterShare > 0)
            --remainderAfterShare;
      }

      doWrite("<td>%s</td>", ROW_HEADING_SHARED_BY);
      for (int currAmount : amount)
         doWriteCellAmount (currAmount);

      tab = tab.substring(TAB.length());
      doWrite("</tr>");
   }

   public void doWriteSharedByAmountBalance (Map<Integer,Integer> mapUserIdToOweAmount)
   {
      doWriteAmountBalance(mapUserIdToOweAmount, ROW_HEADING_SHARED_BY_BALANCE);
   }

   private void doWriteAmountBalance (Map<Integer,Integer> mapUserIdToOweAmount, String rowHeading)
   {
      if (rowHeading.equals(ROW_HEADING_PAID_BY_BALANCE))
         doWrite ("<tr class='highlightCredit'>");
      else
         doWrite ("<tr class='highlightDebit'>");
      tab = tab + TAB;

      doWrite("<td>%s</td>", rowHeading);
      for (int userId : listAllUserId)
         doWriteCellAmount(mapUserIdToOweAmount.get(userId));

      tab = tab.substring(TAB.length());
      doWrite("</tr>");
   }

   public void doWriteTableExpenseEnd()
   {
      // End Table
      tab = tab.substring(TAB.length());
      doWrite ("</table>");
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
         doWrite ("<tr>");
         tab = tab + TAB;

         // Lender Name
         doWrite ("<th>%s</th>", getUserName(lenderId));

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
         doWrite("</tr>");
      }
      doWriteTableWOWEnd();
   }

   private void doWriteTableWOWBegin (List<Integer> listBorrower)
   {
      doWrite ("<h2> Report of who owes whom</h2>");

      // Begin Table
      doWrite ("<table class='grid'>");
      tab = tab + TAB;

      // Begin Table Row Heading- Top Row
      doWrite ("<tr>");
      tab = tab + TAB;

      // Emtpy Cell
      doWrite ("<th rowspan='2'></th>");

      // Span borrower names
      doWrite ("<th colspan='%d'>Borrowers - Members who owe</th>", listBorrower.size());

      // Total
      doWrite ("<th rowspan='2'>Total</th>");
      tab = tab.substring(TAB.length());

      // End Table Row Heading- Top Row
      doWrite ("</tr>");

      // Begin Table Row
      doWrite ("<tr>");
      tab = tab + TAB;

      // Table Row Heading - Borrower Names
      for (Integer currBorrowerId : listBorrower)
         doWrite("<th>%s</th>", getUserName(currBorrowerId));

      // End Table Row
      tab = tab.substring(TAB.length());
      doWrite ("</tr>");
   }

   private void doWriteTableWOWEnd ()
   {
      // End Table
      tab = tab.substring(TAB.length());
      doWrite("</table>");
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
         doWrite("<th class='empty'></th>");
      else
         doWrite("<th class='amount highlight'>%s</th>", OUCurrencyUtil.format(amount));
   }

   private void doWriteCellAmount (int amount)
   {
      if (amount == 0)
         doWrite("<td class='amount empty'></td>");
      else
         doWrite("<td class='amount'>%s</td>", OUCurrencyUtil.format(amount));
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
      if (out != null)
         return;

      try
      {
         CoreUtil.copy (context.getAssets().open(fileReportBegin), new FileOutputStream(fileReport, false));
         out = new PrintWriter(new FileWriter(fileReport, true));
         Log.i(TAG, "Report File Path = " + fileReport.getAbsolutePath());
      }
      catch (IOException e)
      {
         UIUtil.doToastError(context, R.string.wow_error_report_gen);
         Log.e(TAG, "Error generating report", e);
      }
   }

   public void closeReportWriter ()
   {
      try
      {
         out.close();
         CoreUtil.copy(context.getAssets().open(fileReportEnd), new FileOutputStream(fileReport, true));
      }
      catch (IOException e)
      {
         UIUtil.doToastError(context, R.string.wow_error_report_gen);
         Log.e(TAG, "Error generating report", e);
      }
   }

   private void doWrite (String format, Object... arg)
   {
      out.println (String.format(tab + format, arg));
   }
}
