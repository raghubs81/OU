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

import com.maga.ou.util.OUMultiChoiceListener;
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
    *    <li>tripId : Mandatory - Display list of thumb_items belonging to the trip.</li>
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

   private class ItemPaymentMultiChoiceListener extends OUMultiChoiceListener<Integer>
   {
      private SQLiteDatabase db = DBUtil.getDB(activity);

      public ItemPaymentMultiChoiceListener ()
      {
         super(activity, getListView());
      }

      @Override
      protected Integer doBackgroundTask()
      {
         return (listId.isEmpty()) ? 0 : Item.delete(db, listId);
      }

      @Override
      protected void doAfterTaskCompletionBeforeRestoration(Integer result)
      {
         if (result <= 0)
            return;
         CursorAdapter cursorAdapter = (CursorAdapter)getListView().getAdapter();
         cursorAdapter.changeCursor(Item.getItemPaymentSummary(db, tripId));
      }

      @Override
      protected void doAfterRestoration(Integer result)
      {
         String mesg = ((result == 0) ? "No" : String.valueOf(result)) + " thumb_items deleted";
         Toast.makeText(context, mesg, Toast.LENGTH_SHORT).show();
      }
   }
}
