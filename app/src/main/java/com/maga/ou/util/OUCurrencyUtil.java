package com.maga.ou.util;

/**
 * Created by rbseshad on 16-Aug-16.
 */
public class OUCurrencyUtil
{
   public static void main(String arg[])
   {
      System.out.println(valueOf(""));
   }


   public static int valueOf(String num)
   {
      String token[] = num.split("\\.");

      int value = token[0].equals("") ? 0 : Integer.valueOf(token[0]);

      if (token.length == 2)
      {
         if (token[1].length() == 1)
            token[1] = token[1] + "0";
         value = value * 100 + Integer.valueOf(token[1]);
      }
      return value;
   }

   public static String format(int num)
   {
      String format = (num < 0) ? "-%d.%02d" : "%d.%02d";
      if (num < 0)
         num = -num;
      return String.format(format, num / 100, num % 100);
   }
}
