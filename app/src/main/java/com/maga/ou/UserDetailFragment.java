package com.maga.ou;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.maga.ou.model.TripUser;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.util.UIUtil;

public class UserDetailFragment  extends Fragment implements View.OnClickListener
{
   /**
    * Constants
    * ___________________________________________________________________________________________________
    */

   private final String TAG = "ou." + getClass ().getSimpleName();

   /**
    * UI Base Objects
    * ___________________________________________________________________________________________________
    */

   private Context context;

   private AppCompatActivity activity;

   private View viewRoot;

   /**
    * Fragment parameters
    * ___________________________________________________________________________________________________
    */

   private int userId = DBUtil.UNSET_ID;

   private int tripId = DBUtil.UNSET_ID;

   /**
    * Member variables
    * ___________________________________________________________________________________________________
    */

   private TripUser user = null;

   private UserDetailListener listener = null;

   /**
    * <b>Parameters</b>
    * <ul>
    *    <li>tripId : Mandatory - Used to display details of a trip</li>
    * </ul>
    */
   public UserDetailFragment ()
   {

   }

   /**
    * Setters
    * ___________________________________________________________________________________________________
    */

   public void setTripId (int id)
   {
      this.tripId = id;
   }

   public void setUserId (int id)
   {
      this.userId = id;
   }

   /**
    * Life cycle methods
    * ___________________________________________________________________________________________________
    */

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      // Inflate the layout for this fragment
      context = inflater.getContext();
      setHasOptionsMenu(true);
      return inflater.inflate(R.layout.fragment_user_detail, container, false);
   }

   @Override
   public void onActivityCreated (Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);
      this.viewRoot = getView();
      this.activity = (AppCompatActivity) getActivity();
      this.listener = (UserDetailListener)getActivity();
      initMembers();
   }

   @Override
   public void onCreateOptionsMenu (Menu menu, MenuInflater inflater)
   {
      inflater.inflate(R.menu.appbar_detail_edit, menu);
      super.onCreateOptionsMenu(menu, inflater);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      switch (item.getItemId())
      {
         case R.id.appbar_detail_edit:
            listener.userEditClicked(tripId, userId);
            return true;

         default:
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            return super.onOptionsItemSelected(item);
      }
   }

   /**
    * Event handlers
    * ___________________________________________________________________________________________________
    */

   @Override
   public void onClick(View view)
   {
      int id = view.getId();

      if (id == R.id.user_detail__contact)
         doViewContact ();

   }

   /**
    * Member functions
    * ___________________________________________________________________________________________________
    */

   public void initMembers ()
   {
      initMemberFromModel ();
      inflateUIComponents ();
   }

   private void initMemberFromModel()
   {
      DBUtil.assertSetId(tripId);
      DBUtil.assertSetId(userId);

      SQLiteDatabase db = DBUtil.getDB(context);
      user = TripUser.getInstance(db, userId);
   }

   private void inflateUIComponents ()
   {
      UIUtil.setAppBarTitle(activity, "Member Details");

      TextView textNickName = (TextView)viewRoot.findViewById(R.id.user_detail__nick_name);
      textNickName.setText(user.getNickName());

      TextView textFullName = (TextView)viewRoot.findViewById(R.id.user_detail__full_name);
      textFullName.setText(user.getFullName());

      TextView textContactHelp = (TextView)viewRoot.findViewById(R.id.user_detail__contact_help);
      textContactHelp.setText(getResources().getString(R.string.help_contact_with_id, user.getContactId()));

      Button buttonViewContact = (Button)viewRoot.findViewById(R.id.user_detail__contact);
      buttonViewContact.setOnClickListener(this);
   }

   private void doViewContact ()
   {
      Intent intent = new Intent (Intent.ACTION_VIEW);
      Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(user.getContactId()));
      intent.setData(uri);
      context.startActivity(intent);
   }

   interface UserDetailListener
   {
      void userEditClicked (int tripId, int userId);
   }

}
