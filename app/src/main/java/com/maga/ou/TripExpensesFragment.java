package com.maga.ou;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.maga.ou.model.OUAmountDistribution;
import com.maga.ou.model.OUAmountDistribution.ItemAmount;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.util.OUCurrencyUtil;
import com.maga.ou.util.UIUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class TripExpensesFragment extends Fragment
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
    * UI Components
    * ___________________________________________________________________________________________________
    */

   ExpandableListView accordianListView;

   /**
    * Fragment Parameters
    * ___________________________________________________________________________________________________
    */

   private int tripId = DBUtil.UNSET_ID;

   /**
    * Constructor
    * ___________________________________________________________________________________________________
    */

   /**
    * <b>Parameters</b>
    * <ul>
    * <li>tripId        : Mandatory. </li>
    * </ul>
    */
   public TripExpensesFragment()
   {
      // Required empty public constructor
   }

   /**
    * Setters
    * ___________________________________________________________________________________________________
    */

   public void setTripId(int id)
   {
      this.tripId = id;
   }

   /**
    * Lifecycle methods
    * ___________________________________________________________________________________________________
    */

   /**
    * <br/>
    * <br/><b>Inherited Doc:</b>
    * <br/>{@inheritDoc}
    */
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      // Inflate the layout for this fragment
      context = inflater.getContext();
      return inflater.inflate(R.layout.fragment_trip_expenses, container, false);
   }

   /**
    * Called when the fragment's activity has been created and this fragment's view hierarchy instantiated.
    * Used to retrieving views or restoring state.
    * <p/>
    * <br/>
    * <br/><b>Inherited Doc:</b>
    * <br/>{@inheritDoc}
    */
   @Override
   public void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      this.viewRoot = getView();
      this.activity = (AppCompatActivity) getActivity();

      initMembers();
   }

   /**
    * Member functions
    * ___________________________________________________________________________________________________
    */

   private void initMembers ()
   {
      initMemberFromModel();
      inflateUIComponents();
   }

   private void initMemberFromModel()
   {
      DBUtil.assertSetId(tripId);
   }

   private void inflateUIComponents ()
   {
      UIUtil.setAppBarTitle(activity, R.string.trip_expenses_title);

      accordianListView = (ExpandableListView) viewRoot.findViewById(R.id.trip_expenses__accordion);
      SQLiteDatabase db = DBUtil.getDB(context);
      OUAmountDistribution amountDistribution = new OUAmountDistribution(db, tripId);
      amountDistribution.doFindWhoOwesWhom();

      accordianListView.setAdapter(new TripExpensesAdapter(amountDistribution));
   }

   class TripExpensesAdapter extends BaseExpandableListAdapter
   {
      private List<Integer> listAllUserId;

      private List<String> listAllUserName;

      private Map<Integer,List<ItemAmount>> mapLenderToItems;

      private Map<Integer,Integer> mapUserIdToOweAmount;

      private Map<Integer,Integer[]> mapUserIdToGroupMeta = new TreeMap<>();

      private static final String SummaryAmountBeforeSettlement = "You Spent";

      private static final String SummaryAmountAfterSettlement = "Your Trip Expense";

      private static final String SummaryAmountToPay = "You Owe Others";

      private static final String SummaryAmountToGet = "Others Owe You";

      private int colorBeforeSettlement, colorToPay, colorToGet, colorAfterSettlement;

      public TripExpensesAdapter(OUAmountDistribution amountDistribution)
      {
         this.mapLenderToItems     = amountDistribution.getMapLenderToItems();
         this.mapUserIdToOweAmount = amountDistribution.getMapUserIdToOweAmount();
         this.listAllUserId   = amountDistribution.getListAllUserId();
         this.listAllUserName = amountDistribution.getListAllUserName();

         colorBeforeSettlement = ContextCompat.getColor(context, R.color.textExpenseBeforeSettlementDark);
         colorToPay            = ContextCompat.getColor(context, R.color.textExpenseToPayDark);
         colorToGet            = ContextCompat.getColor(context, R.color.textExpenseToGetDark);
         colorAfterSettlement  = ContextCompat.getColor(context, R.color.textExpenseAfterSettlementDark);

         populateMetaData();
      }

      private String getUserName (int userId)
      {
         return listAllUserName.get(listAllUserId.indexOf(userId));
      }

      private int getTotalAmount (List<ItemAmount> listItemAmount)
      {
         if (listItemAmount == null)
            return  0;

         int sum = 0;
         for (ItemAmount itemAmount : listItemAmount)
            sum += itemAmount.getAmount();
         return sum;
      }

      private void populateMetaData()
      {
         for (Integer userId : listAllUserId)
         {
            List<ItemAmount> listItemAmount = mapLenderToItems.get(userId);
            if (listItemAmount == null)
               listItemAmount = new ArrayList<>();

            int beforeSettlement = getTotalAmount(listItemAmount);
            listItemAmount.add(new ItemAmount(SummaryAmountBeforeSettlement, beforeSettlement));

            int amountOthersOU = mapUserIdToOweAmount.get(userId);
            if (amountOthersOU >= 0)
               listItemAmount.add(new ItemAmount(SummaryAmountToGet, amountOthersOU));
            else
               listItemAmount.add(new ItemAmount(SummaryAmountToPay, -1 * amountOthersOU));
            int afterSettlement = beforeSettlement - amountOthersOU;

            listItemAmount.add(new ItemAmount(SummaryAmountAfterSettlement, afterSettlement));

            mapLenderToItems.put(userId, listItemAmount);
            mapUserIdToGroupMeta.put(userId, new Integer[]{beforeSettlement, afterSettlement});
         }

         Log.d(TAG, "MapUserIdToGroupMeta=" + mapUserIdToGroupMeta);
         Log.d(TAG, "MapLenderToItems=" + mapLenderToItems);
      }

      @Override
      public ItemAmount getChild(int groupPosition, int childPosition)
      {
         int userId = listAllUserId.get(groupPosition);
         return mapLenderToItems.get(userId).get(childPosition);
      }

      @Override
      public long getChildId (int groupPosition, int childPosition)
      {
         return childPosition;
      }

      @Override
      public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
      {
         LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         int childCount = getChildrenCount(groupPosition);
         final ItemAmount itemAmount = getChild(groupPosition, childPosition);
         String summary = itemAmount.getSummary();
         int amount = itemAmount.getAmount();

         int layoutId = R.layout.segment_trip_expenses_child;
         if (childPosition >= childCount - 3)
         {
            if (childPosition == childCount - 3)
               layoutId = R.layout.segment_trip_expenses_child_before;
            else if (childPosition == childCount - 2)
            {
               if (summary.equals(SummaryAmountToGet))
                  layoutId = R.layout.segment_trip_expenses_child_to_get;
               else
                  layoutId = R.layout.segment_trip_expenses_child_to_pay;
            }
            else if (childPosition == childCount - 1)
               layoutId = R.layout.segment_trip_expenses_child_after;
         }
         convertView = inflater.inflate(layoutId, null);

         TextView textUserName   = (TextView) convertView.findViewById(R.id.trip_expenses_child__name);
         textUserName.setText(summary);

         TextView textUserAmount = (TextView) convertView.findViewById(R.id.trip_expenses_child__amount);
         textUserAmount.setText(OUCurrencyUtil.format(amount));

         return convertView;
      }

      @Override
      public int getChildrenCount(int groupPosition)
      {
         int userId = listAllUserId.get(groupPosition);
         return mapLenderToItems.get(userId).size();
      }

      @Override
      public Integer getGroup(int groupPosition)
      {
         return listAllUserId.get(groupPosition);
      }

      @Override
      public int getGroupCount()
      {
         return listAllUserId.size();
      }

      @Override
      public long getGroupId(int groupPosition)
      {
         return groupPosition;
      }

      @Override
      public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
      {
         Integer userId = getGroup(groupPosition);

         if (convertView == null)
         {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.segment_trip_expenses_group, null);
         }

         TextView textUserName   = (TextView) convertView.findViewById(R.id.trip_expenses_group__name);
         textUserName.setText(getUserName(userId));

         Integer meta [] = mapUserIdToGroupMeta.get(userId);

         TextView textBefore   = (TextView) convertView.findViewById(R.id.trip_expenses_group__before_amount);
         textBefore.setText(OUCurrencyUtil.format(meta[0]));

         TextView textAfter   = (TextView) convertView.findViewById(R.id.trip_expenses_group__after_amount);
         textAfter.setText(OUCurrencyUtil.format(meta[1]));

         return convertView;
      }

      @Override
      public boolean hasStableIds()
      {
         return false;
      }

      @Override
      public boolean isChildSelectable(int groupPosition, int childPosition)
      {
         return true;
      }
   }

}