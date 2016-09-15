package com.maga.ou;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.maga.ou.model.TripUser;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.util.UIUtil;

public class UserEditFragment extends Fragment implements View.OnClickListener
{
   /**
    * Constants
    * ___________________________________________________________________________________________________
    */

   private final String TAG = "ou." + getClass ().getSimpleName();

   private static final int PICK_CONTACT = 101;

   /**
    * UI Base Objects
    * ___________________________________________________________________________________________________
    */

   private Context context;

   private View viewRoot;

   private AppCompatActivity activity;

   /**
    * UI Components
    * ___________________________________________________________________________________________________
    */

   private TextView textContactHelp;

   /**
    * Fragment Parameters
    * ___________________________________________________________________________________________________
    */

   private int userId = DBUtil.UNSET_ID;

   private int tripId = DBUtil.UNSET_ID;

   /**
    * Member variables
    * ___________________________________________________________________________________________________
    */

   private TripUser user;

   /**
    * <b>Parameters</b>
    * <ul>
    *    <li>operationType : (Add|Edit) Optional. Default is 'Add'</li>
    *    <li>tripId        : Mandatory. </li>
    *    <li>userId        : Optional if operationType is 'Add'   </li>
    * </ul>
    */
   public UserEditFragment()
   {

   }

   /**
    * Setters
    * ___________________________________________________________________________________________________
    */

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
      return inflater.inflate(R.layout.fragment_user_edit, container, false);
   }

   @Override
   public void onActivityCreated (Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);
      this.viewRoot = getView();
      this.activity = (AppCompatActivity)getActivity();
      initMembers();
   }

   public void onActivityResult(int reqCode, int resultCode, Intent data)
   {
      super.onActivityResult(reqCode, resultCode, data);

      switch (reqCode)
      {
         case (PICK_CONTACT) :
            if (resultCode == Activity.RESULT_OK)
            {
               Uri uriContact = data.getData();
               String contactId = uriContact.getLastPathSegment();
               user.setContactId(contactId);
               textContactHelp.setText(getResources().getString(R.string.help_contact_with_id, user.getContactId()));
            }
         break;
      }
   }

   /**
    * Event Handler
    * ___________________________________________________________________________________________________
    */

   @Override
   public void onClick(View view)
   {
      int id = view.getId();
      if (id == R.id.user_edit__save)
         doSave();
      else if (id == R.id.user_edit__cancel)
         doCancel();
      else if (id == R.id.user_edit__relink_contact)
         doRelinkContact ();
   }

   /**
    * Instance Methods
    * ___________________________________________________________________________________________________
    */

   private void initMembers()
   {
      initMemberFromModel ();
      inflateUIComponents();

      SQLiteDatabase db = DBUtil.getDB(context);
      user = TripUser.getInstance(db, userId);

      populateUIComponents();
   }

   private void initMemberFromModel()
   {
      DBUtil.assertSetId(tripId);
      DBUtil.assertSetId(userId);

      SQLiteDatabase db = DBUtil.getDB(context);
      user = TripUser.getInstance(db, userId);
   }

   private void inflateUIComponents()
   {
      UIUtil.setAppBarTitle(activity, R.string.user_title_edit);

      // Relink contact
      Button buttonRelinkContact = (Button)viewRoot.findViewById(R.id.user_edit__relink_contact);
      buttonRelinkContact.setOnClickListener(this);

      // Contact Id
      textContactHelp = (TextView)viewRoot.findViewById(R.id.user_detail__contact_help);
      textContactHelp.setText(getResources().getString(R.string.help_contact_with_id, user.getContactId()));

      // Save
      Button buttonSave = (Button)viewRoot.findViewById(R.id.user_edit__save);
      buttonSave.setOnClickListener(this);

      // Cancel
      Button buttonCancel = (Button)viewRoot.findViewById(R.id.user_edit__cancel);
      buttonCancel.setOnClickListener(this);
   }

   private void populateUIComponents ()
   {
      EditText textName = (EditText)viewRoot.findViewById(R.id.user_edit__nick_name);
      textName.setText(user.getNickName());

      EditText textFirstName = (EditText)viewRoot.findViewById(R.id.user_edit__full_name);
      textFirstName.setText(user.getFullName());
   }

   private void doRelinkContact ()
   {
      Intent intent = new Intent(Intent.ACTION_PICK);
      intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
      startActivityForResult(intent, PICK_CONTACT);
   }

   private void doSave ()
   {
      // Validate Input
      boolean valid = true;

      EditText textNickName = (EditText)viewRoot.findViewById(R.id.user_edit__nick_name);
      String nickName  = textNickName.getText().toString();

      String fullName = ((EditText)viewRoot.findViewById(R.id.user_edit__full_name)).getText().toString();

      if (nickName.equals(""))
      {
         textNickName.setError(UIUtil.getResourceString(context, R.string.user_validation_name));
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
         user.setNickName(nickName);
         user.setFullName(fullName);
         user.update(db);

         db.setTransactionSuccessful();
         UIUtil.doToastSaveSuccess(context);
      }
      catch (Throwable e)
      {
         Log.e(TAG, "Exception saving payment details", e);
         UIUtil.doToastSaveFailure(context);
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
