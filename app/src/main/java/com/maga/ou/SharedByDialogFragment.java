package com.maga.ou;

import java.util.*;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import com.maga.ou.util.UIUtil;

public class SharedByDialogFragment extends DialogFragment
{
   public enum Arg
   {
      NameList, ChosenIndexList;
   }

   /**
    * ArrayList of indexes that are chosen.
    */
   private ArrayList<Integer> listChosenIndex;

   /**
    * ArrayList of names with checkboxes to be displayed.
    */
   private ArrayList<String> listName;

   public SharedByDialogFragment ()
   {

   }

   @Override
   public void setArguments (Bundle bundle)
   {
      super.setArguments(bundle);
      listName        = bundle.getStringArrayList(Arg.NameList.name());
      listChosenIndex = bundle.getIntegerArrayList(Arg.ChosenIndexList.name());
   }

   public ArrayList<Integer> getChosenIndexList ()
   {
      return listChosenIndex;
   }

   @Override
   public Dialog onCreateDialog(Bundle savedInstanceState)
   {
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

      // Dialog Title
      builder.setTitle(R.string.shared_by_title);

      // Dialog Content - Items Displayed
      String name[] = listName.toArray(new String[0]);
      boolean checked[] = UIUtil.convertIndexListToCheckedItem(listChosenIndex, name.length);
      builder.setMultiChoiceItems(name, checked, new DialogInterface.OnMultiChoiceClickListener ()
      {
         @Override
         public void onClick(DialogInterface dialog, int which, boolean isChecked)
         {
            if (isChecked)
               listChosenIndex.add(which);
            else if (listChosenIndex.contains(which))
               listChosenIndex.remove(Integer.valueOf(which));
         }
      });

      // Dialog Buttons - OK
      builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener()
      {
         public void onClick(DialogInterface dialog, int id)
         {
            // FIRE ZE MISSILES!
         }
      });

      return builder.create();
   }
}
