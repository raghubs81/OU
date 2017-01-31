package com.maga.ou.model.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
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
import java.io.InputStream;
import java.io.OutputStream;
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
   private static final String FILENAME_REPORT_BEGIN = "report.begin.txt";

   private static final String FILENAME_REPORT_CALCULATION = "report.calculation.txt";

   private static final String FILENAME_REPORT_END = "report.end.txt";

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
      this.db = DBUtil.getDB (context);

      fileReport = getHtmlReportFile(context, tripId);
   }

   public static File getPDFReportFile (Context context, int tripId)
   {
      return PDFGenerator.toPdf(getHtmlReportFile(context, tripId));
   }

   /**
    * Return file that shall contain the HTML report.
    *
    * @param tripId TripId of the current trip.
    * @return filename that shall contain the HTML report.
    */
   public static File getHtmlReportFile(Context context, int tripId)
   {
      File file = null;
      try
      {
         if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            throw new IOException("External storage not available");

         String tripName = Trip.getInstance(DBUtil.getDB (context), tripId).getName();
         tripName = tripName.trim().replaceAll("\\s+", "_");
         String filename = tripName + ".html";

         file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), filename);
      }
      catch (Exception e)
      {
         UIUtil.doToastError(context, R.string.wow_error_report_gen);
         Log.i(TAG, "Error generating report", e);
      }
      return file;
   }

   public void doWriteTripHeading()
   {
      String tripName = Trip.getInstance(db, tripId).getName();
      writeln("<h1>Report - %s </h1>", tripName);
      writeln(tab + "<p/>");
   }

   public void doWriteTableExpenseBegin ()
   {
      writeln("<h2>Report of trip expenses");
      writeln("<span class='toggleText'><a href='#' onclick='toggle(this, \"reportExpense\");'>Hide</a></span>");
      writeln("</h2>");

      // Begin Div - TripExpense
      writeln("<div id='reportExpense'>");

      // Append the calculation file details
      appendReportFile (FILENAME_REPORT_CALCULATION);

      // Begin Table
      writeln("<table class='grid'>");
      tab = tab + TAB;

      // Begin Table Heading
      writeln("<tr>");
      tab = tab + TAB;

      writeln("<th>Item</th>");
      writeln("<th>Transaction</th>");
      for (String currUser : listAllUserName)
         writeln("<th>%s</th>", currUser);

      // End Table Heading
      tab = tab.substring(TAB.length());
      writeln("</tr>");
   }

   public void doWritePaidByAmount (Item item, Map<TripUser,Integer> mapPaidByUserToAmount)
   {
      writeln("<tr>");
      tab = tab + TAB;

      writeln("<td colspan='1' rowspan='4'>%s</td>", item.getSummary());

      // Write the amount for each user
      writeln("<td>%s</td>", ROW_HEADING_PAID_BY);
      for (int userAmount : getAmount(mapPaidByUserToAmount))
         doWriteCellAmount(userAmount);

      tab = tab.substring(TAB.length());
      writeln("</tr>");
   }

   public void doWritePaidByAmountBalance (Map<Integer,Integer> mapUserIdToOweAmount)
   {
      doWriteAmountBalance(mapUserIdToOweAmount, ROW_HEADING_PAID_BY_BALANCE);
   }

   public void doWriteSharedByAmount (List<TripUser>listSharedByUser, int amountSharePerUser, int remainderAfterShare)
   {
      writeln("<tr>");
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

      writeln("<td>%s</td>", ROW_HEADING_SHARED_BY);
      for (int currAmount : amount)
         doWriteCellAmount (currAmount);

      tab = tab.substring(TAB.length());
      writeln("</tr>");
   }

   public void doWriteSharedByAmountBalance (Map<Integer,Integer> mapUserIdToOweAmount)
   {
      doWriteAmountBalance(mapUserIdToOweAmount, ROW_HEADING_SHARED_BY_BALANCE);
   }

   private void doWriteAmountBalance (Map<Integer,Integer> mapUserIdToOweAmount, String rowHeading)
   {
      if (rowHeading.equals(ROW_HEADING_PAID_BY_BALANCE))
         writeln("<tr class='highlightCredit'>");
      else
         writeln("<tr class='highlightDebit'>");
      tab = tab + TAB;

      writeln("<td>%s</td>", rowHeading);
      for (int userId : listAllUserId)
         doWriteCellAmount(mapUserIdToOweAmount.get(userId), false);

      tab = tab.substring(TAB.length());
      writeln("</tr>");
   }

   public void doWriteTableExpenseEnd()
   {
      // End Table
      tab = tab.substring(TAB.length());
      writeln("</table>");

      // End Div - Trip Expense
      writeln("</div>");
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
         writeln("<tr>");
         tab = tab + TAB;

         // Lender Name
         writeln("<th>%s</th>", getUserName(lenderId));

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
         writeln("</tr>");
      }
      doWriteTableWOWEnd();
   }

   private void doWriteTableWOWBegin (List<Integer> listBorrower)
   {
      writeln("<h2> Report of who owes whom");
      writeln("<span class='toggleText'><a href='#' onclick='toggle(this, \"reportWOW\");'>Hide</a></span>");
      writeln("</h2>");

      // Begin Div - TripExpense
      writeln("<div id='reportWOW'>");
      tab = tab + TAB;

      // Begin Table
      writeln("<table class='grid'>");
      tab = tab + TAB;

      // Begin Table Row Heading- Top Row
      writeln("<tr>");
      tab = tab + TAB;

      // Emtpy Cell
      writeln("<th rowspan='2'></th>");

      // Span borrower names
      writeln("<th colspan='%d'>Borrowers - Members who owe</th>", listBorrower.size());

      // Total
      writeln("<th rowspan='2'>Total</th>");
      tab = tab.substring(TAB.length());

      // End Table Row Heading- Top Row
      writeln("</tr>");

      // Begin Table Row
      writeln("<tr>");
      tab = tab + TAB;

      // Table Row Heading - Borrower Names
      for (Integer currBorrowerId : listBorrower)
         writeln("<th>%s</th>", getUserName(currBorrowerId));

      // End Table Row
      tab = tab.substring(TAB.length());
      writeln("</tr>");
   }

   private void doWriteTableWOWEnd ()
   {
      // End Table
      tab = tab.substring(TAB.length());
      writeln("</table>");

      // End Div - WOW
      writeln("</div>");
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
         writeln("<th class='empty highlight'></th>");
      else
         writeln("<th class='amount highlight'>%s</th>", OUCurrencyUtil.format(amount));
   }

   private void doWriteCellAmount (int amount)
   {
      doWriteCellAmount (amount, true);
   }

   private void doWriteCellAmount (int amount, boolean isEmptyOnZero)
   {
      if (isEmptyOnZero && amount == 0)
         writeln("<td class='empty'></td>");
      else
         writeln("<td class='amount'>%s</td>", OUCurrencyUtil.format(amount));
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
         if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            throw new IOException ("External storage not available");

         InputStream  streamSrc  = context.getAssets().open(FILENAME_REPORT_BEGIN);
         OutputStream streamDest = new FileOutputStream(fileReport, false);
         CoreUtil.copy (streamSrc, streamDest);

         out = new PrintWriter(new FileWriter(fileReport, true), true);
         Log.i(TAG, "Report File Path = " + fileReport.getAbsolutePath());
      }
      catch (IOException e)
      {
         Log.i(TAG, "Error generating report", e);
         UIUtil.doToastError(context, R.string.wow_error_report_gen);
      }
   }

   private void appendReportFile (String fileReportMid)
   {
      try
      {
         out.close();
         CoreUtil.copy(context.getAssets().open(fileReportMid), new FileOutputStream(fileReport, true));
         out = new PrintWriter(new FileWriter(fileReport, true), true);
      }
      catch (IOException e)
      {
         Log.i(TAG, "Error generating report", e);
         UIUtil.doToastError(context, R.string.wow_error_report_gen);
      }
   }

   public void closeReportWriter ()
   {
      try
      {
         out.flush();
         out.close();
         Log.i(TAG, "Closing the report writer");
         CoreUtil.copy(context.getAssets().open(FILENAME_REPORT_END), new FileOutputStream(fileReport, true));
      }
      catch (IOException e)
      {
         Log.i(TAG, "Error generating report", e);
         UIUtil.doToastError(context, R.string.wow_error_report_gen);
      }
   }

   private void writeln (String format, Object... arg)
   {
      out.println (String.format(tab + format, arg));
   }
}
