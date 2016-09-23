package com.maga.ou.util;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import com.maga.ou.R;

/**
 * Created by rbseshad on 22-Sep-16.
 */
public class DieDialogFragment extends DialogFragment
{
   public static enum Arg
   {
      MESSAGE_ID;
   }

   private int messageId = 0;

   public DieDialogFragment ()
   {
   }

   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      messageId = getArguments().getInt(Arg.MESSAGE_ID.name());
   }

   @Override
   public Dialog onCreateDialog (Bundle savedInstanceState)
   {
      // Use the Builder class for convenient dialog construction
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder
         .setMessage(messageId)
         .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
         {
            public void onClick(DialogInterface dialog, int id)
            {
               getActivity().finishAffinity();
               getActivity().onBackPressed();
            }
         });
      // Create the AlertDialog object and return it
      return builder.create();
   }
}
