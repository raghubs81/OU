package com.maga.ou.model.util;

import android.util.Log;

import java.io.*;
import java.nio.channels.FileChannel;

/**
 * Created by rbseshad on 07-Oct-16.
 */
public class CoreUtil
{
   /**
    * Copy contents from input stream to output stream.
    */
   public static void copy (InputStream in, OutputStream out) throws IOException
   {
      // Transfer bytes from in to out
      byte[] buffer = new byte[1024];

      int length;
      while ((length = in.read(buffer)) > 0)
      {
         out.write(buffer, 0, length);
      }

      in.close();
      out.close();
   }

   /*
    * Fatal exit
    * ___________________________________________________________________________________________________
    */

   public static void die (String mesg)
   {
      die(mesg, null);
   }

   public static void die (String mesg, Throwable e)
   {
      Log.e(DBUtil.TAG, mesg, e);
      throw new IllegalStateException(mesg, e);
   }
}
