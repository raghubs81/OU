package com.maga.ou;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import com.maga.ou.ItemPaymentDetailFragment.ItemPaymentDetailListener;
import com.maga.ou.ItemPaymentListFragment.ItemPaymentListListener;
import com.maga.ou.TripListFragment.TripListListener;
import com.maga.ou.TripDetailFragment.TripDetailListener;
import com.maga.ou.UserListFragment.UserListListener;
import com.maga.ou.UserDetailFragment.UserDetailListener;

public class OUMainActivity extends SingleFragmentActivity implements
      ItemPaymentListListener, ItemPaymentDetailListener,
      TripListListener, TripDetailListener,
      UserListListener, UserDetailListener
{
   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
   }

   @Override
   public Fragment getFragment()
   {
      return new TripListFragment();
   }

   /*  Item Payment List Fragment */

   @Override
   public void itemPaymentClicked (int tripId, int itemId)
   {
      Log.d(TAG, "ItemPaymentList - ItemClicked. Activity to invoke next fragment. Received Id=" + itemId);
      ItemPaymentDetailFragment fragment = new ItemPaymentDetailFragment();
      fragment.setTripId(tripId);
      fragment.setItemId(itemId);
      SingleFragmentActivity.replaceFrameWithFragment(fragment, this);
   }

   @Override
   public void itemPaymentAddClicked(int tripId)
   {
      Log.d(TAG, "ItemPaymentList - AppBarIcon - AddClicked. Activity to invoke next fragment");
      ItemPaymentAddEditFragment fragment = new ItemPaymentAddEditFragment();
      fragment.setTripId(tripId);
      SingleFragmentActivity.replaceFrameWithFragment(fragment, this);
   }

   /*  Item Payment Detail Fragment */

   @Override
   public void itemPaymentEditClicked(int tripId, int itemId)
   {
      Log.d(TAG, "ItemPaymentList - AppBarIcon - EditClicked. Activity to invoke next fragment. Received Id=" + itemId);
      ItemPaymentAddEditFragment fragment = new ItemPaymentAddEditFragment();
      fragment.setTripId(tripId);
      fragment.setItemId(itemId);
      fragment.setOperationType(ItemPaymentAddEditFragment.OperationType.Edit);
      SingleFragmentActivity.replaceFrameWithFragment(fragment, this);
   }

   /* Trip List Fragment */

   @Override
   public void tripClicked(int tripId)
   {
      Log.d(TAG, "TripList - ItemClicked. Activity to invoke next fragment. Received Id=" + tripId);
      TripDetailFragment fragment = new TripDetailFragment();
      fragment.setTripId(tripId);
      SingleFragmentActivity.replaceFrameWithFragment(fragment, this);
   }

   @Override
   public void tripAddClicked()
   {
      Log.d(TAG, "TripList - AppBarIcon - AddClicked. Activity to invoke next fragment");
      TripAddEditFragment fragment = new TripAddEditFragment();
      fragment.setOperationType(TripAddEditFragment.OperationType.Add);
      SingleFragmentActivity.replaceFrameWithFragment(fragment, this);
   }

   /* Trip Detail Fragment */

   @Override
   public void tripEditClicked(int tripId)
   {
      Log.d(TAG, "TripDetails - AppBarIcon - EditClicked. Activity to invoke next fragment");
      TripAddEditFragment fragment = new TripAddEditFragment();
      fragment.setOperationType(TripAddEditFragment.OperationType.Edit);
      fragment.setTripId(tripId);
      SingleFragmentActivity.replaceFrameWithFragment(fragment, this);
   }

   @Override
   public void goToTripItemsClicked(int tripId)
   {
      Log.d(TAG, "TripDetails - Button - TripItemsClicked. Activity to invoke next fragment. Received Id=" + tripId);
      ItemPaymentListFragment fragment = new ItemPaymentListFragment();
      fragment.setTripId(tripId);
      SingleFragmentActivity.replaceFrameWithFragment(fragment, this);
   }

   @Override
   public void goToTripUsersClicked(int tripId)
   {
      Log.d(TAG, "TripDetails - Button - TripUsersClicked. Activity to invoke next fragment. Received Id=" + tripId);
      UserListFragment fragment = new UserListFragment();
      fragment.setTripId(tripId);
      SingleFragmentActivity.replaceFrameWithFragment(fragment, this);
   }

   @Override
   public void goToTripWOWClicked(int tripId)
   {
      Log.d(TAG, "TripDetails - Button - TripWOWClicked. Activity to invoke next fragment. Received Id=" + tripId);
      WhoOwesWhomFragment fragment = new WhoOwesWhomFragment();
      fragment.setTripId(tripId);
      SingleFragmentActivity.replaceFrameWithFragment(fragment, this);
   }

   /* User List Fragment */

   @Override
   public void userClicked(int tripId, int userId)
   {
      Log.d(TAG, "UserList - ItemClicked. Activity to invoke next fragment. Received Id=" + userId);
      UserDetailFragment fragment = new UserDetailFragment();
      fragment.setTripId(tripId);
      fragment.setUserId(userId);
      SingleFragmentActivity.replaceFrameWithFragment(fragment, this);
   }

   @Override
   public void userAddClicked(int tripId)
   {
      Log.d(TAG, "UserList - AppBarIcon - AddClicked. Activity to invoke next fragment. Received Id=" + tripId);
      UserAddEditFragment fragment = new UserAddEditFragment();
      fragment.setOperationType(UserAddEditFragment.OperationType.Add);
      fragment.setTripId(tripId);
      SingleFragmentActivity.replaceFrameWithFragment(fragment, this);
   }

   /* User Edit Fragment */

   @Override
   public void userEditClicked(int tripId, int userId)
   {
      Log.d(TAG, "UserDetails - AppBarIcon - EditClicked. Activity to invoke next fragment. Received Id=" + tripId);
      UserAddEditFragment fragment = new UserAddEditFragment();
      fragment.setOperationType(UserAddEditFragment.OperationType.Edit);
      fragment.setTripId(tripId);
      fragment.setUserId(userId);
      SingleFragmentActivity.replaceFrameWithFragment(fragment, this);
   }
}
