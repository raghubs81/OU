package com.maga.ou.util;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import com.maga.ou.R;
import com.maga.ou.model.Item;
import com.maga.ou.model.TripGroup;
import com.maga.ou.model.TripUser;
import com.maga.ou.model.util.DBUtil;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupDetailFragment  extends Fragment implements View.OnClickListener
{
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
      inflater.inflate(R.menu.appbar_detail_edit, menu);
      super.onCreateOptionsMenu(menu, inflater);
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
   }

   private void inflateUIComponents ()
   {
      UIUtil.setAppBarTitle(activity, "Group Details");

      TextView textName = (TextView) viewRoot.findViewById(R.id.group_detail__name);
      textName.setText(group.getName());

      TextView textDetail = (TextView) viewRoot.findViewById(R.id.group_detail__detail);
      textDetail.setText(group.getDetail());

   }

   public void doAddGroupUserSegment (SQLiteDatabase db)
   {
      GridLayout layoutSegmentContainer  = (GridLayout)viewRoot.findViewById(R.id.group_detail__user_container);
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
      void groupEditClicked (int tripId, int userId);
   }
}