package com.maga.ou;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.maga.ou.model.Item;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.util.OUAsyncTask;
import com.maga.ou.util.OUCurrencyUtil;

import java.util.ArrayList;
import java.util.List;
import com.maga.ou.util.UIUtil;

public class ItemPaymentListFragment extends ListFragment
{
   private final String TAG = "ou." + getClass ().getSimpleName();

   /**
    * UI Base Objects
    * ___________________________________________________________________________________________________
    */

   private Context context;

   private AppCompatActivity activity;

   private View viewRoot;

   /**
    * Member variables
    * ___________________________________________________________________________________________________
    */

   private ItemPaymentListListener listener = null;

   private int tripId = DBUtil.UNSET_ID;

   /**
    * <b>Parameters</b>
    * <ul>
    *    <li>tripId : Mandatory - Display list of items belonging to the trip.</li>
    * </ul>
    */
   public ItemPaymentListFragment()
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
    * Life cycle methods
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
      return inflater.inflate(R.layout.fragment_item_payment_list, container, false);
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
   public void onActivityCreated (Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      this.viewRoot = getView();
      this.activity = (AppCompatActivity)getActivity();
      this.listener = (ItemPaymentListListener)getActivity();

      initMembers();
   }

   /**
    * Invoked when an item is clicked.
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
      listener.itemPaymentClicked(tripId, (int) id);
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
            listener.itemPaymentAddClicked(tripId);
            return true;

         default:
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            return super.onOptionsItemSelected(item);
      }
   }

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
      UIUtil.setAppBarTitle(activity, "Items");

      SQLiteDatabase db = DBUtil.getDB(context);
      Cursor cursor = Item.getItemPaymentSummary(db, tripId);
      CursorAdapter cursorAdapter = new ItemPaymentCursorAdapter(cursor);
      setListAdapter(cursorAdapter);

      // Register for long click
      getListView().setMultiChoiceModeListener(new ItemPaymentMultiChoiceListener());
   }

   /**
    * Handles long press events and allows mulitple selection.
    */
   private class ItemPaymentMultiChoiceListener implements  AbsListView.MultiChoiceModeListener
   {
      private ListView listView = getListView();

      private List<Integer> listItemId = new ArrayList<>();

      private SQLiteDatabase db = DBUtil.getDB(context);

      public ItemPaymentMultiChoiceListener ()
      {
         listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
      }

      @Override
      public boolean onCreateActionMode(ActionMode mode, Menu menu)
      {
         MenuInflater inflater = mode.getMenuInflater();
         inflater.inflate(R.menu.appbar_list_longtap, menu);
         mode.setTitle("Select Items");
         listItemId.clear();

         if (activity.getSupportActionBar() != null)
            activity.getSupportActionBar().hide();

         return true;
      }

      @Override
      public boolean onPrepareActionMode(ActionMode mode, Menu menu)
      {
         return false;
      }

      @Override
      public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked)
      {
         int itemId = (int)id;
         if (checked)
            listItemId.add(itemId);
         else
            listItemId.remove(itemId);
         mode.setSubtitle(listItemId.size() + " selected");
      }

      @Override
      public boolean onActionItemClicked(ActionMode mode, MenuItem item)
      {
         switch (item.getItemId())
         {
            case R.id.appbar_list_delete:
               new AsyncItemDeletionTask ().execute();
               mode.finish();
               return true;

            default:
               return false;
         }
      }

      @Override
      public void onDestroyActionMode(ActionMode mode)
      {
         if (activity.getSupportActionBar() != null)
            activity.getSupportActionBar().show();
      }

      /**
       * This class handles asynchronous deletion of list items.
       *    - The OUAsyncTask is an AsyncTask that shows a modal dialog
       */
      class AsyncItemDeletionTask extends OUAsyncTask<Void,Integer>
      {
         public AsyncItemDeletionTask ()
         {
            super(context);
         }

         @Override
         protected Integer doInBackground (Void... params)
         {
            if (listItemId.isEmpty())
               return 0;
            return Item.delete(db, listItemId);
         }

         @Override
         protected void onPostExecute(Integer result)
         {
            CursorAdapter cursorAdapter = (CursorAdapter)listView.getAdapter();
            cursorAdapter.changeCursor(Item.getItemPaymentSummary(db, tripId));
            super.onPostExecute(result);
            listItemId.clear();

            String strValue = (result == 0) ? "No" : String.valueOf(result);
            Toast.makeText(context, strValue + " items deleted", Toast.LENGTH_SHORT).show();
         }
      }
   }

   /**
    * An interface that delegates following events to the activity that contains this fragment.
    */
   public interface ItemPaymentListListener
   {
      void itemPaymentClicked (int tripId, int itemId);
      void itemPaymentAddClicked (int tripId);
   }

   /**
    * A cursor adapter that customizes a list item
    */
   private class ItemPaymentCursorAdapter extends ResourceCursorAdapter
   {
      public ItemPaymentCursorAdapter (Cursor cursor)
      {
         this(cursor, 0);
      }

      public ItemPaymentCursorAdapter (Cursor cursor, int flags)
      {
         super(context, R.layout.segment_item_payment_list, cursor, flags);
      }

      @Override
      public void bindView (View view, Context context, Cursor cursor)
      {
         TextView textSummary = (TextView)view.findViewById(R.id.segment_item_payment_list__summary);
         TextView textDetail  = (TextView)view.findViewById(R.id.segment_item_payment_list__detail);
         TextView textAmount  = (TextView)view.findViewById(R.id.segment_item_payment_list__amount);

         textSummary.setText(DBUtil.getCell(cursor, Item.Column.Summary));
         textDetail.setText(DBUtil.getCell(cursor, Item.Column.Detail));
         textAmount.setText(OUCurrencyUtil.format(Integer.valueOf(DBUtil.getCell(cursor, Item.ItemPaidBy.Column.Amount))));
      }
   }
}
