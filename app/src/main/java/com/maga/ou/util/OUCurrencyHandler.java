package com.maga.ou.util;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OUCurrencyHandler implements InputFilter, View.OnFocusChangeListener
{
   private final String TAG = "ou." + getClass ().getSimpleName();

   private Pattern pattern = Pattern.compile("\\d*(\\.\\d{0,2})?");

   public OUCurrencyHandler()
   {

   }

   /**
    * This is a callback when text changes - Any addition or deletion.
    *
    * Starting index is inclusive and ending index is excluded.
    * "dest.subSequence(dstart, dend)" is removed and replaced with "source.subSequence(start, end)"
    *
    * We need to validate the resultant string which is
    *    - dest.subSequence(0, dstart) + source.subSequence(start, end) + dest.subSequence(dend, dest.length())
    */
   @Override
   public CharSequence filter (CharSequence source, int start, int end, Spanned dest, int dstart, int dend)
   {
      String strToTest = "" + dest.subSequence(0, dstart) + source.subSequence(start, end) + dest.subSequence(dend, dest.length());
      // Log.i(TAG, "ToTest=" + strToTest + " Source=" + source +  " Dest=" + dest + " Matches=" + pattern.matcher(strToTest).matches());

      Matcher matcher = pattern.matcher(strToTest);
      if (!matcher.matches())
         return "";
      return null;
   }

   /**
    * This is a callback when the focus is moved from the text.
    *
    * @param view
    * @param hasFocus
    */
   @Override
   public void onFocusChange (View view, boolean hasFocus)
   {
      EditText text = (EditText)view;
      double value = 0;
      try
      {
         value = Double.parseDouble(text.getText().toString());
         text.setText(String.format("%02.2f", value));
      }
      catch (NumberFormatException e)
      {
         Log.w(TAG, "Not a number. Value=" + text.getText());
      }
   }
}