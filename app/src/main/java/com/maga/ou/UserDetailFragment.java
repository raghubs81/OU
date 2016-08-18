package com.maga.ou;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.maga.ou.model.TripUser;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.util.UIUtil;

/**
 * Created by Pavithra on 09-Aug-16.
 */
public class UserDetailFragment  extends Fragment
{
   /**
    * UI Base Objects
    * ___________________________________________________________________________________________________
    */

   private Context context;

   private AppCompatActivity activity;

   private View viewRoot;

   /**
    * Member variables
    * ___________________________________________________________________________________________________
    */

   private int userId = DBUtil.UNSET_ID;

   private int tripId = DBUtil.UNSET_ID;

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


   public void initMembers ()
   {
      initMemberFromModel ();
      inflateUIComponents ();
   }

   private void initMemberFromModel()
   {
      DBUtil.assertSetId(tripId);
      DBUtil.assertSetId(userId);
   }

   private void inflateUIComponents ()
   {
      UIUtil.setAppBarTitle(activity, "Member Details");

      SQLiteDatabase db = DBUtil.getDB(context);
      TripUser user = TripUser.getInstance(db, userId);

      TextView textNickName = (TextView)viewRoot.findViewById(R.id.user_detail__nick_name);
      textNickName.setText(user.getNickName());

      TextView textFullName = (TextView)viewRoot.findViewById(R.id.user_detail__full_name);
      String firstName = user.getFirstName();
      String lastName  = user.getLastName();
      String fullName  = ((firstName == null) ? "" : firstName + " ") + ((lastName == null) ? "" : lastName);
      textFullName.setText(fullName);

      TextView textMobile   = (TextView)viewRoot.findViewById(R.id.user_detail__mobile);
      textMobile.setText(user.getMobile());

      TextView textEmail    = (TextView)viewRoot.findViewById(R.id.user_detail__email);
      textEmail.setText(user.getEmail());
   }

   interface UserDetailListener
   {
      void userEditClicked (int tripId, int userId);
   }

}
