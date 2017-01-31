package com.maga.ou.model.util;

import java.io.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;

/**
 * Created by rbseshad on 31-Jan-17.
 */
public class PDFGenerator
{
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

   public static void main()
   {

   }
}
