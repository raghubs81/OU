package com.maga.ou.model.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.maga.ou.R;
import com.maga.ou.model.Item;
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
      doWrite (tab + "<hr/>");
      doWrite (tab + "<p/>");
   }

   public void doWriteTableExpenseBegin ()
   {
      doWrite ("<h2> Report of expense computation</h2>");
      doWrite ("<table class='grid'>");
      tab = tab + TAB;

      // Heading
      doWrite ("<tr>");
      tab = tab + TAB;

      doWrite ("<th>Item</th>");
      doWrite ("<th>Transaction</th>");
      for (String currUser : listAllUserName)
         doWrite ("<th>%s</th>", currUser);

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
         doWrite("<td class='amount'>%s</td>", (userAmount == 0) ? "" : OUCurrencyUtil.format(userAmount));

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
         doWrite("<td class='amount'>%s</td>", (currAmount == 0) ? "" : OUCurrencyUtil.format(currAmount));

      tab = tab.substring(TAB.length());
      doWrite("</tr>");
   }

   public void doWriteSharedByAmountBalance (Map<Integer,Integer> mapUserIdToOweAmount)
   {
      doWriteAmountBalance(mapUserIdToOweAmount, ROW_HEADING_SHARED_BY_BALANCE);
   }

   private void doWriteAmountBalance (Map<Integer,Integer> mapUserIdToOweAmount, String rowHeading)
   {
      doWrite ("<tr class='highlight'>");
      tab = tab + TAB;

      doWrite("<td>%s</td>", rowHeading);
      for (int userId : listAllUserId)
      {
         int userAmount = mapUserIdToOweAmount.get(userId);
         doWrite("<td class='amount'>%s</td>", OUCurrencyUtil.format(userAmount));
      }

      tab = tab.substring(TAB.length());
      doWrite("</tr>");
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

   public void doWriteTableExpenseEnd()
   {
      tab = tab.substring(TAB.length());
      doWrite ("</table>");
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
