package com.maga.ou.util;

import android.content.Context;
import android.text.InputFilter;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

/**
 * Created by rbseshad on 07-Jul-16.
 */
public class UIUtil
{
   private UIUtil ()
   {

   }

   public static final String LOG_HR = "____________________________________________________________________________________________________";

   public static final OUCurrencyHandler OU_CURRENCY_HANDLER = new OUCurrencyHandler();

   public static void setAppBarTitle (AppCompatActivity activity, String title)
   {
      if (activity.getSupportActionBar() != null)
         activity.getSupportActionBar().setTitle(title);
   }

   public static boolean[] convertIndexListToCheckedItem (List<Integer> listIndex, int itemCount)
   {
      boolean checked[] = new boolean[itemCount];
      for (int index : listIndex)
         checked[index] = true;
      return checked;
   }

   public static void setSpinnerList (Context context, Spinner combo, List<String> list)
   {
      ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, list);
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      combo.setAdapter(adapter);
   }

   public static void setTextCurrencyHandler (EditText editText)
   {
      editText.setText("0.00");
      editText.setFilters(new InputFilter[]{OU_CURRENCY_HANDLER});
      editText.setOnFocusChangeListener(OU_CURRENCY_HANDLER);
   }

   public static void sleep (int sec)
   {
      try
      {
         Thread.sleep(sec * 1000);
      }
      catch (InterruptedException e)
      {
         e.printStackTrace();
      }
   }
}
