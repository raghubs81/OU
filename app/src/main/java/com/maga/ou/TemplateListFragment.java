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
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.util.UIUtil;

/**
 * Created by rbseshad on 09-Aug-16.
 */
public class TemplateListFragment extends ListFragment
{
   /**
    * Constants
    * ___________________________________________________________________________________________________
    */

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

   private TemplateListListener listener;

   /**
    * Constructor
    * ___________________________________________________________________________________________________
    */

   /**
    * <b>Parameters</b>
    * None
    */
   public TemplateListFragment()
   {
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
      this.listener = (TemplateListListener)getActivity();

      UIUtil.setAppBarTitle(activity, "Members List");

      SQLiteDatabase db = DBUtil.getDB(context);
      Cursor cursor = null; // TODO: Get cursor
      CursorAdapter cursorAdapter = new TemplateCursorAdapter(cursor);
      setListAdapter(cursorAdapter);
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
      // TODO Invoke listener
   }

   /**
    * An interface that delegates following events to the activity that contains this fragment.
    */
   public interface TemplateListListener
   {
      // TODO: Listener method
   }

   private class TemplateCursorAdapter extends ResourceCursorAdapter
   {
      public TemplateCursorAdapter (Cursor cursor)
      {
         this(cursor, 0);
      }

      public TemplateCursorAdapter (Cursor cursor, int flags)
      {
         super(context, R.layout.segment_template_list, cursor, flags);
      }

      @Override
      public void bindView(View view, Context context, Cursor cursor)
      {

      }
   }
}
