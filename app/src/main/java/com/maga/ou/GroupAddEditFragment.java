package com.maga.ou;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.maga.ou.model.TripGroup;
import com.maga.ou.model.TripUser;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.util.UIUtil;

import java.util.ArrayList;
import java.util.List;


public class GroupAddEditFragment extends Fragment implements View.OnClickListener
{
   private final String TAG = "ou." + getClass().getSimpleName();

   /**
    * UI Base Objects
    * ___________________________________________________________________________________________________
    */

   private Context context;

   private View viewRoot;

   private AppCompatActivity activity;

   /**
    * Fragment Parameters
    * ___________________________________________________________________________________________________
    */

   public enum OperationType
   {
      Add, Edit
   }

   private OperationType operationType = OperationType.Add;

   private int groupId = DBUtil.UNSET_ID;

   private int tripId = DBUtil.UNSET_ID;

   /**
    * UI Components
    * ___________________________________________________________________________________________________
    */

   private MembersDialogFragment dialogMembers;

   /**
    * Instance variables
    * ___________________________________________________________________________________________________
    */

   private ArrayList<Integer> listTripUserId = new ArrayList<>();

   private ArrayList<String> listTripUserName = new ArrayList<>();

   /**
    * List of index indicating the TripGroup and TripUser item that were selected.
    */
   private ArrayList<Integer> listChosenUserIndex = new ArrayList<>();

   /**
    * <b>Parameters</b>
    * <ul>
    * <li>operationType : (Add|Edit) Optional. Default is 'Add'</li>
    * <li>tripId        : Mandatory. </li>
    * <li>userId        : Optional if operationType is 'Add'   </li>
    * </ul>
    */
   public GroupAddEditFragment()
   {

   }

   /**
    * Setters
    * ___________________________________________________________________________________________________
    */

   public void setOperationType(OperationType operationType)
   {
      this.operationType = operationType;
   }

   public void setTripId(int id)
   {
      this.tripId = id;
   }

   public void setGroupId(int id)
   {
      this.groupId = id;
   }

   /**
    * Lifecycle methods
    * ___________________________________________________________________________________________________
    */

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      // Inflate the layout for this fragment
      context = inflater.getContext();
      return inflater.inflate(R.layout.fragment_group_add_edit, container, false);
   }

   @Override
   public void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);
      this.viewRoot = getView();
      this.activity = (AppCompatActivity) getActivity();
      initMembers();
   }

   /**
    * Event Handler
    * ___________________________________________________________________________________________________
    */

   @Override
   public void onClick(View view)
   {
      int id = view.getId();
      if (id == R.id.group_add_edit__members)
         doSelectMembers ();
      else if (id == R.id.group_add_edit__save)
         doSave();
      else if (id == R.id.group_add_edit__cancel)
         doCancel();
   }


   /**
    * Members methods
    * ___________________________________________________________________________________________________
    */

   private void initMembers()
   {
      initMemberFromModel();
      inflateUIComponents();
      populateUIComponents();
   }

   private void initMemberFromModel()
   {
      DBUtil.assertSetId(tripId);
      if (operationType == OperationType.Edit)
         DBUtil.assertSetId(groupId);

      SQLiteDatabase db = DBUtil.getDB(context);
      List<TripUser> listUser = TripUser.getLiteUsers(db, tripId);
      for (TripUser user : listUser)
      {
         listTripUserId.add(user.getId());
         listTripUserName.add(user.getNickName());
      }
   }

   private void inflateUIComponents()
   {
      if (operationType == OperationType.Add)
         UIUtil.setAppBarTitle(activity, "Add Group");
      else
         UIUtil.setAppBarTitle(activity, "Edit Group");

      // Select shared by users
      Button buttonSharedBy = (Button)viewRoot.findViewById(R.id.group_add_edit__members);
      buttonSharedBy.setOnClickListener(this);

      // Save
      Button buttonSave = (Button) viewRoot.findViewById(R.id.group_add_edit__save);
      buttonSave.setOnClickListener(this);

      // Cancel
      Button buttonCancel = (Button) viewRoot.findViewById(R.id.group_add_edit__cancel);
      buttonCancel.setOnClickListener(this);

      // Ensure that the 'listChosenUserIndex' are chosen even if user does not click on 'SharedBy' button.
      dialogMembers = new MembersDialogFragment();
      Bundle bundle = new Bundle();
      bundle.putIntegerArrayList(SharedByDialogFragment.Arg.ChosenIndexList.name(), listChosenUserIndex);
      dialogMembers.setArguments(bundle);
   }

   private void populateUIComponents()
   {
      if (operationType == OperationType.Add)
         return;

      SQLiteDatabase db = DBUtil.getDB(context);
      TripGroup group = TripGroup.getInstance(db, groupId);

      EditText textName = (EditText)viewRoot.findViewById(R.id.group_add_edit__name);
      textName.setText(group.getName());

      EditText textDetail = (EditText)viewRoot.findViewById(R.id.group_add_edit__detail);
      textDetail.setText(group.getDetail());

      List<TripUser> listUserFromGroup = group.getLiteUsers(db);
      for (TripUser user : listUserFromGroup)
      {
         int indexOfUser = listTripUserId.indexOf(user.getId());
         listChosenUserIndex.add(indexOfUser);
      }
   }

   private void doSelectMembers ()
   {
      Bundle bundle = new Bundle();
      bundle.putStringArrayList (MembersDialogFragment.Arg.NameList.name(), listTripUserName);
      bundle.putIntegerArrayList(MembersDialogFragment.Arg.ChosenIndexList.name(), listChosenUserIndex);
      dialogMembers.setArguments(bundle);
      dialogMembers.show(getFragmentManager(), "dialog_members");
   }

   private void doSave()
   {
      // Validate Input
      boolean valid = true;

      EditText textName = (EditText) viewRoot.findViewById(R.id.group_add_edit__name);
      String name = textName.getText().toString();

      EditText textDetail = (EditText) viewRoot.findViewById(R.id.group_add_edit__detail);
      String detail = textDetail.getText().toString();

      if (name.equals(""))
      {
         textName.setError("Please name the group.");
         valid = false;
      }

      if (!valid)
      {
         Log.d(TAG, "Validation failed");
         return;
      }

      // Update database
      SQLiteDatabase db = DBUtil.getDB(context);
      db.beginTransaction();
      try
      {
         TripGroup group = (operationType == OperationType.Add) ? new TripGroup() : TripGroup.getInstance(db, groupId);
         group.setName(name);
         group.setDetail(detail);

         if (operationType == OperationType.Add)
         {
            group.setTripId(tripId);
            group.add(db);
         }
         else if (operationType == OperationType.Edit)
         {
            group.update(db);
            group.deleteAllUsers(db);
         }

         // Convert chosen index to Id
         listChosenUserIndex = dialogMembers.getChosenIndexList();
         List<Integer> listChosenUserId = new ArrayList<>();
         for (Integer currUserIndex : listChosenUserIndex)
            listChosenUserId.add(listTripUserId.get(currUserIndex));

         // Add users with list of IDs to group
         group.addUsers(db, listChosenUserId);

         db.setTransactionSuccessful();
         Toast.makeText(context, "Saved successfully", Toast.LENGTH_SHORT).show();
      }
      catch (Throwable e)
      {
         Toast.makeText(context, "Error occurred during save", Toast.LENGTH_SHORT).show();
         Log.e(TAG, "Exception saving payment details", e);
      }
      finally
      {
         db.endTransaction();
      }
      getActivity().setResult(Activity.RESULT_OK);
      getActivity().onBackPressed();
   }

   private void doCancel()
   {
      getActivity().setResult(Activity.RESULT_CANCELED);
      getActivity().onBackPressed();
   }


}