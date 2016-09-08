package com.maga.ou;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import com.maga.ou.model.TripGroup;
import com.maga.ou.model.TripUser;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.util.UIUtil;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupDetailFragment  extends Fragment implements View.OnClickListener
{
   private static final String TAG = "ou." + TripGroup.class.getSimpleName();

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

   private int groupId = DBUtil.UNSET_ID;

   private int tripId = DBUtil.UNSET_ID;

   /**
    * Member variables
    * ___________________________________________________________________________________________________
    */

   private TripGroup group = null;

   private GroupDetailListener listener = null;

   private int idOfGroupOfAll = DBUtil.UNSET_ID;

   /**
    * Constructor
    * ___________________________________________________________________________________________________
    */

   public GroupDetailFragment()
   {
      // Required empty public constructor
   }

   /**
    * Setters
    * ___________________________________________________________________________________________________
    */

   public void setTripId  (int id)
   {
      this.tripId = id;
   }

   public void setGroupId (int id)
   {
      this.groupId = id;
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
      return inflater.inflate(R.layout.fragment_group_detail, container, false);
   }

   @Override
   public void onActivityCreated (Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);
      this.viewRoot = getView();
      this.activity = (AppCompatActivity) getActivity();
      this.listener = (GroupDetailListener)getActivity();
      initMembers();
   }

   @Override
   public void onCreateOptionsMenu (Menu menu, MenuInflater inflater)
   {
      Log.d(TAG, "GroupId=" + groupId + " IdGroupOfAll=" + idOfGroupOfAll);
      if (groupId != idOfGroupOfAll)
         inflater.inflate(R.menu.appbar_detail_edit, menu);
      super.onCreateOptionsMenu(menu, inflater);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      switch (item.getItemId())
      {
         case R.id.appbar_detail_edit:
            listener.groupEditClicked(tripId, groupId);
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

   }

   /**
    * Instance methods
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
      DBUtil.assertSetId(groupId);

      SQLiteDatabase db = DBUtil.getDB(context);
      group = TripGroup.getInstance(db, groupId);
      idOfGroupOfAll = TripGroup.getIdOfGroupOfAll(db, tripId);
   }

   private void inflateUIComponents ()
   {
      UIUtil.setAppBarTitle(activity, "Group Details");

      TextView textName = (TextView) viewRoot.findViewById(R.id.group_detail__name);
      textName.setText(group.getName());

      TextView textDetail = (TextView) viewRoot.findViewById(R.id.group_detail__detail);
      textDetail.setText(group.getDetail());

      SQLiteDatabase db = DBUtil.getDB(context);
      doAddGroupUserSegment (db);
   }

   public void doAddGroupUserSegment (SQLiteDatabase db)
   {
      GridLayout layoutSegmentContainer  = (GridLayout)viewRoot.findViewById(R.id.group_detail__user_container);
      List<TripUser> listUser = group.getLiteUsers(db);

      int bgColor[] = context.getResources().getIntArray(R.array.bgRainbowDark);
      int rowCount = -1;
      for (final TripUser user : listUser)
      {
         LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         final View segmentViewRoot = inflater.inflate(R.layout.segment_group_detail, layoutSegmentContainer, false);

         int color = bgColor[++rowCount % bgColor.length];
         segmentViewRoot.setBackgroundColor(color);

         TextView textUser   = (TextView)segmentViewRoot.findViewById(R.id.segment_group_detail__user);
         textUser.setText(user.getNickName());

         textUser.setOnClickListener
         (
            new View.OnClickListener()
            {
               @Override
               public void onClick(View view)
               {
                  listener.goToUserDetailClicked(tripId, user.getId());
               }
            }
         );

         layoutSegmentContainer.addView(segmentViewRoot);
      }
   }

   interface GroupDetailListener
   {
      void groupEditClicked (int tripId, int groupId);
      void goToUserDetailClicked (int tripId, int userId);
   }
}
