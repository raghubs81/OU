package com.maga.ou.util;

import android.util.Log;

import java.io.*;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;

/**
 * Created by rbseshad on 31-Jan-17.
 */
public class PDFGenerator
{

   private static final String TAG = "ou." + PDFGenerator.class.getSimpleName();

   public static void main (String arg[])
   {
      File fileHtml = new File ("D:\\App\\OU\\utilities\\Republic_Basic.html");
      PDFGenerator.toPdf(fileHtml);
   }

   public static File toPdf (File fileHtml)
   {
      File filePdf = new File (fileHtml.getParent(), fileHtml.getName().replace(".html", ".pdf"));
      toPdf (fileHtml, filePdf);
      return filePdf;
   }

   public static void toPdf (File fileHtml, File filePdf)
   {
      try
      {
         Log.i(TAG, "HtmlReport=" + fileHtml.getAbsolutePath() + " PDFReport=" + filePdf.getAbsolutePath());

         OutputStream streamPdf = new FileOutputStream(filePdf);
         Document document = new Document();
         PdfWriter outPdf = PdfWriter.getInstance(document, streamPdf);
         document.open();

         XMLWorkerHelper.getInstance().parseXHtml(outPdf, document, new FileInputStream(fileHtml));

         document.close();
         streamPdf.close();
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed generating Pdf", e);
      }
   }

}
