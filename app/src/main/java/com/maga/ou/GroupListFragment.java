package com.maga.ou;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.maga.ou.model.TripGroup;
import com.maga.ou.model.TripUser;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.util.UIUtil;

/**
 * Created by rbseshad on 09-Aug-16.
 */
public class GroupListFragment extends ListFragment
{
   private final String TAG = "ou." + getClass().getSimpleName();

   /**
    * UI Base Objects
    * ___________________________________________________________________________________________________
    */

   private AppCompatActivity activity;

   private Context context;

   private View viewRoot;

   /**
    * Member variables
    * ___________________________________________________________________________________________________
    */

   private GroupListListener listener;

   private int tripId = DBUtil.UNSET_ID;

   /**
    * Constructor
    * ___________________________________________________________________________________________________
    */

   /**
    * <b>Parameters</b>
    * None
    */
   public GroupListFragment()
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
      return inflater.inflate(R.layout.fragment_group_list, container, false);
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
      this.listener = (GroupListListener)getActivity();

      initMembers();
   }

   /**
    * Invoked when an trip item is clicked.
    *
    * <br/>
    * <br/><b>Inherited Doc:</b>
    * <br/>{@inheritDoc}
    */
   @Override
   public void onListItemClick(ListView l, View v, int position, long id)
   {
      Log.d(TAG, "List item clicked. position=" + position + " id=" + id + " listenerExists=" + (listener != null));
      if (listener == null)
         return;
      listener.groupClicked(tripId, (int)id);
   }

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
      UIUtil.setAppBarTitle(activity, "Groups List");

      SQLiteDatabase db = DBUtil.getDB(context);
      Cursor cursor = TripGroup.getTripGroups(db, tripId);
      CursorAdapter cursorAdapter = new GroupCursorAdapter(cursor);
      setListAdapter(cursorAdapter);

      // Register for long click
      // getListView().setMultiChoiceModeListener(new GroupMultiChoiceListener());
   }


   /**
    * An interface that delegates following events to the activity that contains this fragment.
    */
   public interface GroupListListener
   {
      void groupClicked (int tripId, int groupId);
   }

   private class GroupCursorAdapter extends ResourceCursorAdapter
   {
      public GroupCursorAdapter(Cursor cursor)
      {
         this(cursor, 0);
      }

      public GroupCursorAdapter(Cursor cursor, int flags)
      {
         super(context, R.layout.segment_group_list, cursor, flags);
      }

      @Override
      public void bindView(View view, Context context, Cursor cursor)
      {
         TextView textName   = (TextView)view.findViewById(R.id.segment_group__name);
         textName.setText(DBUtil.getCell(cursor, TripGroup.Column.Name));

         TextView textDetail = (TextView)view.findViewById(R.id.segment_group__detail);
         textDetail.setText(DBUtil.getCell(cursor, TripGroup.Column.Detail));
      }
   }

}
