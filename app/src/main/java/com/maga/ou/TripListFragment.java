package com.maga.ou;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Fragment;
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
import com.maga.ou.model.Trip;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.util.UIUtil;


/**
 * A simple {@link Fragment} subclass.
 */
public class TripListFragment extends ListFragment
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

   private TripListListener listener;

   /**
    * <b>Parameters</b>
    * None
    */
   public TripListFragment()
   {
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
      return inflater.inflate(R.layout.fragment_trip_list, container, false);
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
      this.listener = (TripListListener)getActivity();
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
      listener.tripClicked((int) id);
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
            listener.tripAddClicked();
            return true;

         default:
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            return super.onOptionsItemSelected(item);
      }
   }

   private void initMembers ()
   {
      inflateUIComponents();
   }

   private void inflateUIComponents ()
   {
      UIUtil.setAppBarTitle(activity, "Trips");

      SQLiteDatabase db = DBUtil.getDB(context);
      Cursor cursor = Trip.getTrips(db);
      CursorAdapter cursorAdapter = new TripCursorAdapter(cursor);
      setListAdapter(cursorAdapter);
   }

   /**
    * An interface that delegates following events to the activity that contains this fragment.
    */
   public interface TripListListener
   {
      void tripClicked (int tripId);
      void tripAddClicked ();
   }

   private class TripCursorAdapter extends ResourceCursorAdapter
   {
      public TripCursorAdapter (Cursor cursor)
      {
         this(cursor, 0);
      }

      public TripCursorAdapter (Cursor cursor, int flags)
      {
         super(context, R.layout.segment_trip_list, cursor, flags);
      }

      @Override
      public void bindView(View view, Context context, Cursor cursor)
      {
         TextView textName   = (TextView)view.findViewById(R.id.segment_trip_list__name);
         TextView textDetail = (TextView)view.findViewById(R.id.segment_trip_list__detail);

         textName.setText(DBUtil.getCell(cursor, Trip.Column.Name));
         textDetail.setText(DBUtil.getCell(cursor, Trip.Column.Detail));
      }
   }
}



















