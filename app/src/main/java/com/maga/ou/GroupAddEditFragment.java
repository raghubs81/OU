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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.maga.ou.model.TripGroup;
import com.maga.ou.model.TripUser;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.util.UIUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class GroupAddEditFragment extends Fragment implements View.OnClickListener
{
   /**
    * Constants
    * ___________________________________________________________________________________________________
    */

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
    * Member variables
    * ___________________________________________________________________________________________________
    */

   private TripGroup group = null;

   private List<TripUser> listTripUser = new ArrayList<>();

   /**
    * Set of User Ids selected for the group.
    */
   private Set<Integer> setChosenUserId = new HashSet<>();

   /**
    * Constructor
    * ___________________________________________________________________________________________________
    */

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

      if (id == R.id.group_add_edit__save)
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
      SQLiteDatabase db = DBUtil.getDB(context);

      if (operationType == OperationType.Add)
      {
         groupId = DBUtil.UNSET_ID;
         group = new TripGroup();
      }
      else if (operationType == OperationType.Edit)
      {
         DBUtil.assertSetId(groupId);
         group = TripGroup.getInstance(db, groupId);
      }

      // All trip users
      listTripUser = TripUser.getLiteTripUsers(db, tripId);

      // Add trip users who are part of the group
      if (operationType == OperationType.Edit)
      {
         for (TripUser user : group.getLiteUsers(db))
            setChosenUserId.add(user.getId());
      }
   }

   private void inflateUIComponents()
   {
      if (operationType == OperationType.Add)
         UIUtil.setAppBarTitle(activity, "Add Group");
      else
         UIUtil.setAppBarTitle(activity, "Edit Group");

      // Add user segments
      addAllUserSegments ();

      // Save
      Button buttonSave = (Button) viewRoot.findViewById(R.id.group_add_edit__save);
      buttonSave.setOnClickListener(this);

      // Cancel
      Button buttonCancel = (Button) viewRoot.findViewById(R.id.group_add_edit__cancel);
      buttonCancel.setOnClickListener(this);
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
   }

   private void addAllUserSegments ()
   {
      ViewGroup layoutSharedByUsersContainer = (ViewGroup)viewRoot.findViewById(R.id.group_add_edit__users_container);
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      final int bgColor[] = context.getResources().getIntArray(R.array.bgRainbowDark);
      final int bgColorUnCheck = context.getResources().getColor(R.color.bgUnCheckUser);

      int index = -1;
      for (final TripUser user : listTripUser)
      {
         final int colorCheck = bgColor[++index % bgColor.length];
         final View segmentViewRoot = inflater.inflate(R.layout.segment_group_user_add_edit, layoutSharedByUsersContainer, false);

         CheckBox checkBox = (CheckBox) segmentViewRoot.findViewById(R.id.segment_group_user_add_edit__name);
         checkBox.setText(user.getNickName());
         checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
         {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
               if (isChecked)
               {
                  setChosenUserId.add(user.getId());
                  segmentViewRoot.setBackgroundColor(colorCheck);
               }
               else
               {
                  setChosenUserId.remove(Integer.valueOf(user.getId()));
                  segmentViewRoot.setBackgroundColor(bgColorUnCheck);
               }
            }
         });
         layoutSharedByUsersContainer.addView(segmentViewRoot);

         // Check the box if the user is already part of the group
         if(setChosenUserId.contains(user.getId()))
         {
            checkBox.setChecked(true);
            segmentViewRoot.setBackgroundColor(colorCheck);
         }
         else
         {
            checkBox.setChecked(false);
            segmentViewRoot.setBackgroundColor(bgColorUnCheck);
         }
      }
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

         // Add users with list of IDs to group
         group.addUsers(db, setChosenUserId);

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