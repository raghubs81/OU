package com.maga.ou.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import com.maga.ou.R;
import com.maga.ou.model.OUAmountDistribution;
import com.maga.ou.model.util.CoreUtil;
import java.io.*;
import java.util.List;

/**
 * Created by rbseshad on 02-Feb-17.
 */
public class ReportGeneratorUtil
{
   private static final String TAG = "ou." + ReportGeneratorUtil.class.getSimpleName();

   public static final String TAB = "   ";

   private Context context;

   private File fileReport;

   /**
    * Writer of the report
    */
   private PrintWriter out = null;


   public static void main (String arg[])
   {
      String tab = ReportGeneratorUtil.TAB;
      String format = "</p>";
      fun (tab, format);
      fun (tab, format);
   }

   public static void fun (String tab, String format, Object... arg)
   {
      System.out.println (String.format(tab + format, arg));
   }

   public ReportGeneratorUtil (Context context, String filename)
   {
      this.context = context;
      this.fileReport = ReportGeneratorUtil.getExternalDocumentFile(context, filename);
   }

   public File getReportFile ()
   {
      return fileReport;
   }

   public static File getExternalDocumentFile (Context context, String filename)
   {
      File file = null;
      try
      {
         if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            throw new IOException("External storage not available");
         file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), filename);
         Log.i(TAG, "ReportGeneratorUtil ReportFile=" + file.getAbsolutePath());
      }
      catch (Exception e)
      {
         UIUtil.doToastError(context, R.string.wow_error_report_gen);
         Log.i(TAG, "Error generating report", e);
      }
      return file;
   }

   public void openReportWriter (String fileToPrefix)
   {
      if (out != null)
         return;

      try
      {
         if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            throw new IOException("External storage not available");

         if (fileToPrefix != null)
         {
            InputStream streamSrc = context.getAssets().open(fileToPrefix);
            OutputStream streamDest = new FileOutputStream(fileReport, false);
            CoreUtil.copy(streamSrc, streamDest);
         }

         out = new PrintWriter(new FileWriter(fileReport, true), true);
         Log.i(TAG, "Report File Path = " + fileReport.getAbsolutePath());
      }
      catch (IOException e)
      {
         Log.i(TAG, "Error generating report", e);
         UIUtil.doToastError(context, R.string.wow_error_report_gen);
      }
   }

   public void appendReportFile (String fileToAppend)
   {
      try
      {
         out.close();
         CoreUtil.copy(context.getAssets().open(fileToAppend), new FileOutputStream(fileReport, true));
         out = new PrintWriter(new FileWriter(fileReport, true), true);
      }
      catch (IOException e)
      {
         Log.i(TAG, "Error generating report", e);
         UIUtil.doToastError(context, R.string.wow_error_report_gen);
      }
   }

   public void closeReportWriter (String fileToAppendAtLast)
   {
      try
      {
         out.flush();
         out.close();
         Log.i(TAG, "Closing the report writer");
         CoreUtil.copy(context.getAssets().open(fileToAppendAtLast), new FileOutputStream(fileReport, true));
      }
      catch (IOException e)
      {
         Log.i(TAG, "Error generating report", e);
         UIUtil.doToastError(context, R.string.wow_error_report_gen);
      }
   }

   public void writeln (String tab, String format, Object... arg)
   {
      out.println (String.format(tab + format, arg));
   }

   public static String toNoWrap (String str)
   {
      return str.replaceAll(" ", "&nbsp;");
   }
}
