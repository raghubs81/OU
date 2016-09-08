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
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.maga.ou.model.Item;
import com.maga.ou.model.TripUser;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.util.OUCurrencyUtil;
import com.maga.ou.util.UIUtil;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class ItemPaymentDetailFragment extends Fragment
{
   /**
    * UI Base Objects
    * ___________________________________________________________________________________________________
    */

   private Context context;

   private AppCompatActivity activity;

   private View viewRoot;

   /**
    * Fragment Parameters
    * ___________________________________________________________________________________________________
    */

   private int itemId = DBUtil.UNSET_ID;

   private int tripId = DBUtil.UNSET_ID;

   /**
    * Member variables
    * ___________________________________________________________________________________________________
    */

   private ItemPaymentDetailListener listener = null;


   /**
    * Constructor
    * ___________________________________________________________________________________________________
    */

   /**
    * <b>Parameters</b>
    * <ul>
    *    <li>itemId : Mandatory - Used to display item details. </li>
    *    <li>tripId : Mandatory - Used to edit the item. Only current trip users needs to be displayed.</li>
    * </ul>
    */
   public ItemPaymentDetailFragment()
   {

   }

   /**
    * Setters
    * ___________________________________________________________________________________________________
    */

   public void setItemId (int id)
   {
      this.itemId = id;
   }

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
      return inflater.inflate(R.layout.fragment_item_payment_detail, container, false);
   }

   @Override
   public void onActivityCreated (Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);
      this.viewRoot = getView();
      this.activity = (AppCompatActivity)getActivity();
      this.listener = (ItemPaymentDetailListener)getActivity();
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
            listener.itemPaymentEditClicked(tripId, itemId);
            return true;

         default:
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            return super.onOptionsItemSelected(item);
      }
   }

   /**
    * Member functions
    * ___________________________________________________________________________________________________
    */

   private void initMembers ()
   {
      initMemberFromModel ();
      inflateUIComponents();
   }

   private void initMemberFromModel()
   {
      DBUtil.assertSetId(tripId);
      DBUtil.assertSetId(itemId);
   }

   private void inflateUIComponents()
   {
      UIUtil.setAppBarTitle(activity, "Item Details");

      SQLiteDatabase db = DBUtil.getDB(context);
      Item item = Item.getInstance(db, itemId);

      TextView textSummary = (TextView)viewRoot.findViewById(R.id.item_payment_detail__summary);
      textSummary.setText(item.getSummary());

      TextView textDetail  = (TextView)viewRoot.findViewById(R.id.item_payment_detail__detail);
      textDetail.setText(item.getDetail());

      doAddPaidBySegment(db, item);
      doAddSharedBySegment(db, item);
   }

   public void doAddPaidBySegment (SQLiteDatabase db, Item item)
   {
      LinearLayout layoutSegmentContainer  = (LinearLayout)viewRoot.findViewById(R.id.item_payment_detail__paid_by_container);

      Map<TripUser,Integer> mapUserAmount = item.getPaidByUsers(db);

      int totalAmount = 0;
      for (TripUser user : mapUserAmount.keySet())
      {
         LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         final View segmentViewRoot = inflater.inflate(R.layout.segment_paid_by_detail, layoutSegmentContainer, false);

         // Set paid-by user
         TextView textUser   = (TextView)segmentViewRoot.findViewById(R.id.segment_paid_by_detail__user);
         textUser.setText(user.getNickName());

         // Set paid-by amount
         TextView textAmount = (TextView)segmentViewRoot.findViewById(R.id.segment_paid_by_detail__amount);
         int currUserAmount =  mapUserAmount.get(user);
         textAmount.setText(OUCurrencyUtil.format(currUserAmount));

         // Calculate total
         totalAmount += currUserAmount;

         // Add segment to container
         layoutSegmentContainer.addView(segmentViewRoot);
      }
      TextView textTotalAmount = (TextView)viewRoot.findViewById(R.id.item_payment_detail__total_amount);
      textTotalAmount.setText(OUCurrencyUtil.format(totalAmount));
   }

   public void doAddSharedBySegment (SQLiteDatabase db, Item item)
   {
      GridLayout layoutSegmentContainer  = (GridLayout)viewRoot.findViewById(R.id.item_payment_detail__shared_by_container);
      List<TripUser> listUser = item.getSharedByUsers(db);

      int bgColor[] = context.getResources().getIntArray(R.array.bgRainbowDark);
      int rowCount = -1;
      for (final TripUser user : listUser)
      {
         LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         final View segmentViewRoot = inflater.inflate(R.layout.segment_shared_by_detail, layoutSegmentContainer, false);

         int color = bgColor[++rowCount % bgColor.length];
         segmentViewRoot.setBackgroundColor(color);

         TextView textUser   = (TextView)segmentViewRoot.findViewById(R.id.segment_shared_by_detail__user);
         textUser.setText(user.getNickName());

         textUser.setOnClickListener
         (
            new View.OnClickListener()
            {
               @Override
               public void onClick (View view)
               {
                  listener.goToUserDetailClicked(tripId, user.getId());
               }
            }
         );

         layoutSegmentContainer.addView(segmentViewRoot);
      }
   }

   /**
    * An interface that delegates following events to the activity that contains this fragment.
    */
   public interface ItemPaymentDetailListener
   {
      void itemPaymentEditClicked (int tripId, int itemId);
      void goToUserDetailClicked  (int tripId, int userId);
   }
}
