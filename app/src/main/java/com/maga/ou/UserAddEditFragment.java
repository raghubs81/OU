package com.maga.ou;

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
import com.maga.ou.model.TripUser;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.util.UIUtil;

/**
 * Created by rbseshad on 10-Aug-16.
 */
public class UserAddEditFragment extends Fragment implements View.OnClickListener
{
   private final String TAG = "ou." + getClass ().getSimpleName();

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
      Add, Edit;
   }

   private OperationType operationType = OperationType.Add;

   private int userId = DBUtil.UNSET_ID;

   private int tripId = DBUtil.UNSET_ID;

   /**
    * <b>Parameters</b>
    * <ul>
    *    <li>operationType : (Add|Edit) Optional. Default is 'Add'</li>
    *    <li>tripId        : Mandatory. </li>
    *    <li>userId        : Optional if operationType is 'Add'   </li>
    * </ul>
    */
   public UserAddEditFragment()
   {

   }

   /**
    * Setters
    * ___________________________________________________________________________________________________
    */

   public void setOperationType (OperationType operationType)
   {
      this.operationType = operationType;
   }

   public void setUserId (int id)
   {
      this.userId = id;
   }

   public void setTripId (int id)
   {
      this.tripId = id;
   }

   /**
    * Lifecycle methods
    * ___________________________________________________________________________________________________
    */

   @Override
   public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      // Inflate the layout for this fragment
      context = inflater.getContext();
      return inflater.inflate(R.layout.fragment_user_add_edit, container, false);
   }

   @Override
   public void onActivityCreated (Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);
      this.viewRoot = getView();
      this.activity = (AppCompatActivity)getActivity();
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
      if (id == R.id.user_add_edit__save)
         doSave();
      else if (id == R.id.user_add_edit__cancel)
         doCancel();
   }

   /**
    * Instance Methods
    * ___________________________________________________________________________________________________
    */

   private void initMembers()
   {
      initMemberFromModel ();
      inflateUIComponents ();
      populateUIComponents();
   }

   private void initMemberFromModel()
   {
      DBUtil.assertSetId(tripId);
      if (operationType == OperationType.Edit)
         DBUtil.assertSetId(userId);
   }

   private void inflateUIComponents()
   {
      if (operationType == OperationType.Add)
         UIUtil.setAppBarTitle(activity, "Add User");
      else
         UIUtil.setAppBarTitle(activity, "Edit User");

      // Save
      Button buttonSave = (Button)viewRoot.findViewById(R.id.user_add_edit__save);
      buttonSave.setOnClickListener(this);

      // Cancel
      Button buttonCancel = (Button)viewRoot.findViewById(R.id.user_add_edit__cancel);
      buttonCancel.setOnClickListener(this);
   }

   private void populateUIComponents ()
   {
      if (operationType == OperationType.Add)
         return;

      SQLiteDatabase db = DBUtil.getDB(context);
      TripUser user = TripUser.getInstance(db, userId);

      EditText textName = (EditText)viewRoot.findViewById(R.id.user_add_edit__nick_name);
      textName.setText(user.getNickName());

      EditText textFirstName = (EditText)viewRoot.findViewById(R.id.user_add_edit__first_name);
      textFirstName.setText(user.getFirstName());

      EditText textLastName = (EditText)viewRoot.findViewById(R.id.user_add_edit__last_name);
      textLastName.setText(user.getLastName());

      EditText textMobile = (EditText)viewRoot.findViewById(R.id.user_add_edit__mobile);
      textMobile.setText(user.getMobile());

      EditText textEmail = (EditText)viewRoot.findViewById(R.id.user_add_edit__email);
      textEmail.setText(user.getEmail());
   }

   private void doSave ()
   {
      // Validate Input
      boolean valid = true;

      EditText textNickName = (EditText)viewRoot.findViewById(R.id.user_add_edit__nick_name);
      String nickName  = textNickName.getText().toString();

      String firstName = ((EditText)viewRoot.findViewById(R.id.user_add_edit__first_name)).getText().toString();

      String lastName  = ((EditText)viewRoot.findViewById(R.id.user_add_edit__last_name)).getText().toString();

      EditText textMobile   = (EditText)viewRoot.findViewById(R.id.user_add_edit__mobile);
      String mobile    = textMobile.getText().toString();

      String email     = ((EditText)viewRoot.findViewById(R.id.user_add_edit__email)).getText().toString();

      if (nickName.equals(""))
      {
         textNickName.setError("Nick name is required.");
         valid = false;
      }

      if (mobile.equals("") || mobile.length() != 10)
      {
         textMobile.setError("Mobile should have 10 digits.");
         valid = false;
      }

      // End processing if not valid
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
         TripUser user = (operationType == OperationType.Add) ? new TripUser() : TripUser.getLiteInstance(db, userId);

         user.setNickName(nickName);
         user.setFirstName(firstName);
         user.setLastName(lastName);
         user.setMobile(mobile);
         user.setEmail(email);

         if (operationType == OperationType.Add)
         {
            user.setTripId(tripId);
            user.add(db);
         }
         else if (operationType == OperationType.Edit)
            user.update(db);

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
      getActivity().onBackPressed();
   }

   private void doCancel ()
   {
      getActivity().onBackPressed();
   }

}
