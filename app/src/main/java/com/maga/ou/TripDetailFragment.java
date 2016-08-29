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
import android.widget.Button;
import android.widget.TextView;

import com.maga.ou.model.Trip;
import com.maga.ou.model.TripUser;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.util.UIUtil;

public class TripDetailFragment extends Fragment implements View.OnClickListener
{
   /**
    * UI Base Objects
    * ___________________________________________________________________________________________________
    */

   private Context context;

   private AppCompatActivity activity;

   private View viewRoot;

   private TripDetailListener listener;

   /**
    * Fragment parameters
    * ___________________________________________________________________________________________________
    */

   private int tripId = DBUtil.UNSET_ID;

   /**
    * <b>Parameters</b>
    * <ul>
    *    <li>tripId : Mandatory - Used to display details of a trip</li>
    * </ul>
    */
   public TripDetailFragment ()
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
      return inflater.inflate(R.layout.fragment_trip_detail, container, false);
   }

   @Override
   public void onActivityCreated (Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);
      this.viewRoot = getView();
      this.activity = (AppCompatActivity)getActivity();
      this.listener = (TripDetailListener)getActivity();
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
            listener.tripEditClicked(tripId);
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
   public void onClick (View view)
   {
      int id = view.getId();

      if (id == R.id.trip_detail__items)
         onClickTripItems();
      else if (id == R.id.trip_detail__users)
         onClickTripUsers();
      else if (id == R.id.trip_detail__wow)
         onClickTripWOW ();
      else if (id == R.id.trip_detail__expenses)
         onClickTripExpenses ();
   }

   /**
    * Member functions
    * ___________________________________________________________________________________________________
    */

   private void initMembers()
   {
      initMemberFromModel ();
      inflateUIComponents();
   }

   private void initMemberFromModel()
   {
      DBUtil.assertSetId(tripId);
   }

   private void inflateUIComponents ()
   {
      UIUtil.setAppBarTitle(activity, "Trip Details");

      SQLiteDatabase db = DBUtil.getDB(context);
      Trip trip = Trip.getInstance(db, tripId);

      TextView textName = (TextView)viewRoot.findViewById(R.id.trip_detail__name);
      textName.setText(trip.getName());

      TextView textDetail = (TextView)viewRoot.findViewById(R.id.trip_detail__detail);
      textDetail.setText(trip.getDetail());

      Button buttonTripItems = (Button)viewRoot.findViewById(R.id.trip_detail__items);
      buttonTripItems.setOnClickListener(this);

      Button buttonTripUsers = (Button)viewRoot.findViewById(R.id.trip_detail__users);
      buttonTripUsers.setOnClickListener(this);

      Button buttonTripWOW = (Button)viewRoot.findViewById(R.id.trip_detail__wow);
      buttonTripWOW.setOnClickListener(this);

      Button buttonTripExpenses = (Button)viewRoot.findViewById(R.id.trip_detail__expenses);
      buttonTripExpenses.setOnClickListener(this);

      if (TripUser.getTripUserCount(db, tripId) == 0)
      {
         buttonTripItems.setEnabled(false);
         buttonTripWOW.setEnabled(false);
         buttonTripExpenses.setEnabled(false);
      }
   }

   private void onClickTripItems()
   {
      listener.goToTripItemsClicked(tripId);
   }

   private void onClickTripUsers()
   {
      listener.goToTripUsersClicked(tripId);
   }

   private void onClickTripWOW ()
   {
      listener.goToTripWOWClicked(tripId);
   }

   private void onClickTripExpenses ()
   {
      listener.gotoTripExpenses (tripId);
   }

   public interface TripDetailListener
   {
      void tripEditClicked (int tripId);
      void goToTripItemsClicked(int tripId);
      void goToTripUsersClicked(int tripId);
      void goToTripWOWClicked  (int tripId);
      void gotoTripExpenses    (int tripId);
   }

}
