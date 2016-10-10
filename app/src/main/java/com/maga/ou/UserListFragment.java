package com.maga.ou;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.maga.ou.util.UIUtil;
import com.maga.ou.model.TripUser;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.util.OUMultiChoiceListener;

/**
 * Created by rbseshad on 09-Aug-16.
 */
public class UserListFragment extends ListFragment
{
   /*
    * Constants
    * ___________________________________________________________________________________________________
    */

   private final String TAG = "ou." + getClass().getSimpleName();

   /*
    * UI Base Objects
    * ___________________________________________________________________________________________________
    */

   private AppCompatActivity activity;

   private Context context;

   private View viewRoot;

   /*
    * Member variables
    * ___________________________________________________________________________________________________
    */

   private UserListListener listener;

   private int tripId = DBUtil.UNSET_ID;

   /*
    * Constructor
    * ___________________________________________________________________________________________________
    */

   /**
    * <b>Parameters</b>
    * None
    */
   public UserListFragment()
   {
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
      return inflater.inflate(R.layout.fragment_user_list, container, false);
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
      this.listener = (UserListListener)getActivity();

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
      Log.i(TAG, "List item clicked. position=" + position + " id=" + id + " listenerExists=" + (listener != null));
      if (listener == null)
         return;
      listener.userClicked(tripId, (int) id);
   }

   /**
    * Use custom app bar - {@code R.menu.appbar_list_add}
    *
    * <br/>
    * <br/><b>Inherited Doc:</b>
    * <br/>{@inheritDoc}
    */
   @Override
   public void onCreateOptionsMenu (Menu menu, MenuInflater inflater)
   {
      inflater.inflate(R.menu.appbar_list_add, menu);
      super.onCreateOptionsMenu(menu, inflater);
   }

   /**
    * Handle app bar actions - When app bar buttons are tapped.
    *
    * <br/>
    * <br/><b>Inherited Doc:</b>
    * <br/>{@inheritDoc}
    */
   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      switch (item.getItemId())
      {
         case R.id.appbar_list_add:
            listener.userAddClicked(tripId);
            return true;

         default:
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            return super.onOptionsItemSelected(item);
      }
   }

   /*
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
      UIUtil.setAppBarTitle(activity, R.string.user_title_list);

      SQLiteDatabase db = DBUtil.getDB(context);
      Cursor cursor = TripUser.getTripUsers(db, tripId);
      CursorAdapter cursorAdapter = new UserCursorAdapter(cursor);
      setListAdapter(cursorAdapter);

      // Register for long click
      getListView().setMultiChoiceModeListener(new UserMultiChoiceListener());
   }

   /**
    * An interface that delegates following events to the activity that contains this fragment.
    */
   public interface UserListListener
   {
      void userClicked    (int tripId, int userId);
      void userAddClicked (int tripId);
   }

   private class UserMultiChoiceListener extends OUMultiChoiceListener<Integer>
   {
      private SQLiteDatabase db = DBUtil.getDB(activity);

      public UserMultiChoiceListener ()
      {
         super(activity, getListView());
      }

      @Override
      protected Integer doBackgroundTask()
      {
         return listId.isEmpty() ? -1 : TripUser.delete(db, tripId, listId);
      }

      @Override
      protected void doAfterTaskCompletionBeforeRestoration(Integer result)
      {
         if (result <= 0)
            return;
         CursorAdapter cursorAdapter = (CursorAdapter)getListView().getAdapter();
         cursorAdapter.changeCursor(TripUser.getTripUsers(db, tripId));
      }

      @Override
      protected void doAfterRestoration(Integer result)
      {
         String mesg = null;
         if (result == -1)
            UIUtil.doToastError(context, R.string.list_item_del_none, "members");
         else if (result  == 0)
            UIUtil.doToastError(context, R.string.user_del_constraint);
         else
            UIUtil.doToastError(context, R.string.list_item_del, result, "members");
      }
   }

   /**
    * Cursor Adapter for ListView
    */
   private class UserCursorAdapter extends ResourceCursorAdapter
   {
      public UserCursorAdapter (Cursor cursor)
      {
         this(cursor, 0);
      }

      public UserCursorAdapter (Cursor cursor, int flags)
      {
         super(context, R.layout.segment_user_list, cursor, flags);
      }

      @Override
      public void bindView(View view, Context context, Cursor cursor)
      {
         // Set nick name
         TextView textName   = (TextView)view.findViewById(R.id.segment_user_list__nickname);
         textName.setText(DBUtil.getCell(cursor, TripUser.Column.NickName));

         // Set full name
         TextView textDetail = (TextView)view.findViewById(R.id.segment_user_list__fullname);
         String fullName = DBUtil.getCell(cursor, TripUser.Column.FullName);
         textDetail.setText(fullName);
      }
   }

}
