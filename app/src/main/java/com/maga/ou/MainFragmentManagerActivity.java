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
import com.maga.ou.GroupListFragment.GroupListListener;
import com.maga.ou.GroupDetailFragment.GroupDetailListener;

public class MainFragmentManagerActivity extends SingleFragmentActivity implements
      ItemPaymentListListener, ItemPaymentDetailListener,
      TripListListener, TripDetailListener,
      UserListListener, UserDetailListener,
      GroupListListener, GroupDetailListener
{

   public enum Arg
   {
      ON_START_FRAGMENT;
   }

   private Fragment onStartFragment;

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      String fragmentName = getIntent().getStringExtra(Arg.ON_START_FRAGMENT.name());
      Log.d(TAG, "OnStartFragment=" + fragmentName);

      if (fragmentName == null || fragmentName.equals(TripListFragment.class.getSimpleName()))
         onStartFragment = new TripListFragment();
      else
      {
         TripAddEditFragment fragment = new TripAddEditFragment();
         fragment.setOperationType(TripAddEditFragment.OperationType.Add);
         onStartFragment = fragment;
      }
      super.onCreate(savedInstanceState);
   }

   @Override
   public Fragment getFragment()
   {
      return onStartFragment;
   }

   /*  Item Payment List Fragment */

   @Override
   public void itemPaymentClicked (int tripId, int itemId)
   {
      Log.d(TAG, "ItemPaymentList - ItemClicked. Activity to invoke next fragment. Received TripId=" + tripId + "ItemId=" + itemId);
      ItemPaymentDetailFragment fragment = new ItemPaymentDetailFragment();
      fragment.setTripId(tripId);
      fragment.setItemId(itemId);
      SingleFragmentActivity.replaceFrameWithFragment(fragment, this);
   }

   @Override
   public void itemPaymentAddClicked(int tripId)
   {
      Log.d(TAG, "ItemPaymentList - AppBarIcon - AddClicked. Activity to invoke next fragment. Received TripId=" + tripId);
      ItemPaymentAddEditFragment fragment = new ItemPaymentAddEditFragment();
      fragment.setTripId(tripId);
      SingleFragmentActivity.replaceFrameWithFragment(fragment, this);
   }

   /*  Item Payment Detail Fragment */

   @Override
   public void itemPaymentEditClicked(int tripId, int itemId)
   {
      Log.d(TAG, "ItemPaymentDetail - AppBarIcon - EditClicked. Activity to invoke next fragment. Received TripId=" + tripId + "ItemId=" + itemId);
      ItemPaymentAddEditFragment fragment = new ItemPaymentAddEditFragment();
      fragment.setTripId(tripId);
      fragment.setItemId(itemId);
      fragment.setOperationType(ItemPaymentAddEditFragment.OperationType.Edit);
      SingleFragmentActivity.replaceFrameWithFragment(fragment, this);
   }

   @Override
   public void goToUserDetailClicked(int tripId, int userId)
   {
      Log.d(TAG, "ItemPaymentDetail - TextView - SharedByUserClicked. Activity to invoke next fragment. Received TripId=" + tripId + " UserId=" + userId);
      userClicked(tripId, userId);
   }

   /* Trip List Fragment */

   @Override
   public void tripClicked(int tripId)
   {
      Log.d(TAG, "TripList - ItemClicked. Activity to invoke next fragment. Received TripId=" + tripId);
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
   public void goToTripUsersClicked(int tripId)
   {
      Log.d(TAG, "TripDetails - Button - TripUsersClicked. Activity to invoke next fragment. Received TripId=" + tripId);
      UserListFragment fragment = new UserListFragment();
      fragment.setTripId(tripId);
      SingleFragmentActivity.replaceFrameWithFragment(fragment, this);
   }

   @Override
   public void goToTripGroupsClicked(int tripId)
   {
      Log.d(TAG, "TripDetails - Button - TripGroupsClicked. Activity to invoke next fragment. Received TripId=" + tripId);
      GroupListFragment fragment = new GroupListFragment();
      fragment.setTripId(tripId);
      SingleFragmentActivity.replaceFrameWithFragment(fragment, this);
   }

   @Override
   public void goToTripItemsClicked(int tripId)
   {
      Log.d(TAG, "TripDetails - Button - TripItemsClicked. Activity to invoke next fragment. Received TripId=" + tripId);
      ItemPaymentListFragment fragment = new ItemPaymentListFragment();
      fragment.setTripId(tripId);
      SingleFragmentActivity.replaceFrameWithFragment(fragment, this);
   }

   @Override
   public void gotoTripExpenses(int tripId)
   {
      Log.d(TAG, "TripDetails - Button - TripWOWClicked. Activity to invoke next fragment. Received TripId=" + tripId);
      TripExpensesFragment fragment = new TripExpensesFragment();
      fragment.setTripId(tripId);
      SingleFragmentActivity.replaceFrameWithFragment(fragment, this);
   }

   @Override
   public void goToTripWOWClicked(int tripId)
   {
      Log.d(TAG, "TripDetails - Button - TripWOWClicked. Activity to invoke next fragment. Received TripId=" + tripId);
      WhoOwesWhomFragment fragment = new WhoOwesWhomFragment();
      fragment.setTripId(tripId);
      SingleFragmentActivity.replaceFrameWithFragment(fragment, this);
   }

   @Override
   public void tripEditClicked(int tripId)
   {
      Log.d(TAG, "TripDetails - AppBarIcon - EditClicked. Activity to invoke next fragment Received TripId=" + tripId);
      TripAddEditFragment fragment = new TripAddEditFragment();
      fragment.setOperationType(TripAddEditFragment.OperationType.Edit);
      fragment.setTripId(tripId);
      SingleFragmentActivity.replaceFrameWithFragment(fragment, this);
   }

   /* User List Fragment */

   @Override
   public void userClicked(int tripId, int userId)
   {
      Log.d(TAG, "UserList - ItemClicked. Activity to invoke next fragment. Received TripId=" + tripId + " UserId=" + userId);
      UserDetailFragment fragment = new UserDetailFragment();
      fragment.setTripId(tripId);
      fragment.setUserId(userId);
      SingleFragmentActivity.replaceFrameWithFragment(fragment, this);
   }

   @Override
   public void userAddClicked(int tripId)
   {
      Log.d(TAG, "UserList - AppBarIcon - AddClicked. Activity to invoke next fragment. Received TripId=" + tripId);
      ContactListFragment fragment = new ContactListFragment();
      fragment.setTripId(tripId);
      SingleFragmentActivity.replaceFrameWithFragment(fragment, this);
   }

   /* User Detail Fragment */

   @Override
   public void userEditClicked(int tripId, int userId)
   {
      Log.d(TAG, "UserDetails - AppBarIcon - EditClicked. Activity to invoke next fragment. Received TripId=" + tripId + " UserId=" + userId);
      UserEditFragment fragment = new UserEditFragment();
      fragment.setTripId(tripId);
      fragment.setUserId(userId);
      SingleFragmentActivity.replaceFrameWithFragment(fragment, this);
   }

   /* Group List Fragment */

   @Override
   public void groupClicked(int tripId, int groupId)
   {
      Log.d(TAG, "UserList - ItemClicked. Activity to invoke next fragment. Received TripId=" + tripId + " GroupId=" + groupId);
      GroupDetailFragment fragment = new GroupDetailFragment();
      fragment.setTripId(tripId);
      fragment.setGroupId(groupId);
      SingleFragmentActivity.replaceFrameWithFragment(fragment, this);
   }

   /* Group Details Fragment */

   @Override
   public void groupEditClicked(int tripId, int userId)
   {

   }



}
