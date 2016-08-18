package com.maga.ou.util;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Created by rbseshad on 01-Aug-16.
 */
public abstract class OUTextChangeListener implements TextWatcher
{
   private String oldText = "";

   private String newText = "";

   public abstract void onTextChanged (String oldText, String newText);

   @Override
   public void beforeTextChanged(CharSequence s, int start, int count, int after)
   {
      oldText = s.toString();
   }

   @Override
   public void onTextChanged(CharSequence s, int start, int before, int count)
   {

   }

   @Override
   public void afterTextChanged(Editable s)
   {
      newText = s.toString();
      onTextChanged(oldText, newText);
   }
}
