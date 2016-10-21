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
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.model.OUAmountDistribution;
import com.maga.ou.util.OUCurrencyUtil;
import com.maga.ou.model.OUAmountDistribution.UserAmount;
import com.maga.ou.util.UIUtil;

import java.util.*;


/**
 * A simple {@link Fragment} subclass.
 */
public class WhoOwesWhomFragment extends Fragment implements View.OnClickListener
{
   private final String TAG = "ou." + getClass ().getSimpleName();

   /*
    * UI Base Objects
    * ___________________________________________________________________________________________________
    */

   private Context context;

   private AppCompatActivity activity;

   private View viewRoot;

   /*
    * Fragment Parameters
    * ___________________________________________________________________________________________________
    */

   private int tripId = DBUtil.UNSET_ID;

   /*
    * UI Components
    * ___________________________________________________________________________________________________
    */

   private ExpandableListView accordianListView;

   private WhoOwesWhomListAdapter adapterLenderToBorrowers, adapterBorrowerToLenders;

   /*
    * Member variables
    * ___________________________________________________________________________________________________
    */

   private WhoOwesWhomListener listener;

   /*
    * Constructor
    * ___________________________________________________________________________________________________
    */

   /**
    * <b>Parameters</b>
    * <ul>
    *    <li>tripId        : Mandatory. </li>
    * </ul>
    */
   public WhoOwesWhomFragment()
   {
      // Required empty public constructor
   }

   /*
    * Setters
    * ___________________________________________________________________________________________________
    */

   public void setTripId (int id)
   {
      this.tripId = id;
   }

   /*
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
      setHasOptionsMenu(true);
      return inflater.inflate(R.layout.fragment_who_owes_whom, container, false);
   }

   /**
    * Called when the fragment's activity has been created and this fragment's view hierarchy instantiated.
    * Used to retrieving views or restoring state.
    *
    * <br/>
    * <br/><b>Inherited Doc:</b>
    * <br/>{@inheritDoc}
    */
   @Override
   public void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      this.viewRoot = getView();
      this.activity = (AppCompatActivity)getActivity();
      this.listener = (WhoOwesWhomListener)getActivity();

      initMembers();
   }

   @Override
   public void onCreateOptionsMenu (Menu menu, MenuInflater inflater)
   {
      inflater.inflate(R.menu.appbar_report, menu);
      super.onCreateOptionsMenu(menu, inflater);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      switch (item.getItemId())
      {
         case R.id.appbar_report_share:
            listener.goToTripReportClicked(tripId);
            return true;

         default:
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            return super.onOptionsItemSelected(item);
      }
   }

   /*
    * Instance Methods
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
   }

   private void inflateUIComponents ()
   {
      UIUtil.setAppBarTitle(activity, R.string.wow_title);

      accordianListView = (ExpandableListView) viewRoot.findViewById(R.id.who_owes_whom__accordion);

      ToggleButton toggleButton = (ToggleButton)viewRoot.findViewById(R.id.who_owes_whom__toggle);
      toggleButton.setOnClickListener(this);

      OUAmountDistribution amountDistribution = new OUAmountDistribution(context, tripId);
      amountDistribution.doFindWhoOwesWhom();
      adapterLenderToBorrowers = new WhoOwesWhomListAdapter(amountDistribution.getMapLenderToBorrowers(), amountDistribution.getListAllUserId(), amountDistribution.getListAllUserName());
      adapterBorrowerToLenders = new WhoOwesWhomListAdapter(amountDistribution.getMapBorrowerToLenders(), amountDistribution.getListAllUserId(), amountDistribution.getListAllUserName());
      accordianListView.setAdapter(adapterLenderToBorrowers);
   }

   /*
    * Event Handler
    * ___________________________________________________________________________________________________
    */

   @Override
   public void onClick(View view)
   {
      int id = view.getId();
      if (id == R.id.who_owes_whom__toggle)
         doToggleLenderBorrower(((ToggleButton)view).isChecked());
   }

   /*
    * Instance Methods
    * ___________________________________________________________________________________________________
    */

   private void doToggleLenderBorrower (boolean isOn)
   {
      int titleId  = (isOn) ? R.string.wow_borrowers_title : R.string.wow_lenders_title;
      int detailId = (isOn) ? R.string.wow_borrowers_detail : R.string.wow_lenders_detail;

      if (isOn)
         accordianListView.setAdapter(adapterBorrowerToLenders);
      else
         accordianListView.setAdapter(adapterLenderToBorrowers);

      TextView textTitle = (TextView)viewRoot.findViewById(R.id.who_owes_whom__title);
      textTitle.setText(getResources().getText(titleId));

      TextView textDetail = (TextView)viewRoot.findViewById(R.id.who_owes_whom__detail);
      textDetail.setText(getResources().getText(detailId));
   }

   class WhoOwesWhomListAdapter extends BaseExpandableListAdapter
   {
      private Map<Integer,List<UserAmount>> mapIdToUserAmountList;

      private List<Integer> listAllUserId;

      private List<String> listAllUserName;

      private List<Integer> listGroupUserId = new ArrayList<>();

      public WhoOwesWhomListAdapter (Map<Integer,List<UserAmount>> mapIdToUserAmountList, List<Integer> listAllUserId, List<String> listAllUserName)
      {
         this.mapIdToUserAmountList = mapIdToUserAmountList;
         this.listAllUserId = listAllUserId;
         this.listAllUserName = listAllUserName;

         for (int userId : mapIdToUserAmountList.keySet())
            listGroupUserId.add(userId);
      }

      private String getUserName (int userId)
      {
         return listAllUserName.get(listAllUserId.indexOf(userId));
      }

      @Override
      public UserAmount getChild(int groupPosition, int childPosititon)
      {
         int userId = listGroupUserId.get(groupPosition);
         return mapIdToUserAmountList.get(userId).get(childPosititon);
      }

      @Override
      public long getChildId (int groupPosition, int childPosition)
      {
         return childPosition;
      }

      @Override
      public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
      {
         final UserAmount childUserAmount = getChild(groupPosition, childPosition);

         if (convertView == null)
         {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.segment_who_owes_whom_child, null);
         }

         TextView textUserName   = (TextView) convertView.findViewById(R.id.who_owes_whom_child__name);
         textUserName.setText(getUserName(childUserAmount.getId()));

         TextView textUserAmount = (TextView) convertView.findViewById(R.id.who_owes_whom_child__amount);
         textUserAmount.setText(OUCurrencyUtil.format(childUserAmount.getAmount()));

         return convertView;
      }

      @Override
      public int getChildrenCount(int groupPosition)
      {
         int userId = listGroupUserId.get(groupPosition);
         return mapIdToUserAmountList.get(userId).size();
      }

      @Override
      public Integer getGroup(int groupPosition)
      {
         return listGroupUserId.get(groupPosition);
      }

      @Override
      public int getGroupCount()
      {
         return listGroupUserId.size();
      }

      @Override
      public long getGroupId(int groupPosition)
      {
         return groupPosition;
      }

      @Override
      public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
      {
         int userId = getGroup(groupPosition);
         int totalAmount = 0;
         for (UserAmount currUserAmount : mapIdToUserAmountList.get(userId))
            totalAmount += currUserAmount.getAmount();

         if (convertView == null)
         {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.segment_who_owes_whom_group, null);
         }

         TextView textUserName   = (TextView) convertView.findViewById(R.id.who_owes_whom_group__name);
         textUserName.setText(getUserName(userId));

         TextView textUserAmount = (TextView) convertView.findViewById(R.id.who_owes_whom_group__amount);
         textUserAmount.setText(OUCurrencyUtil.format(totalAmount));

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

   public interface WhoOwesWhomListener
   {
      void goToTripReportClicked  (int tripId);
   }
}
