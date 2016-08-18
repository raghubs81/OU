package com.maga.ou.util;

import android.support.v7.app.AppCompatActivity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;

import com.maga.ou.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rbseshad on 11-Aug-16.
 */
public abstract class OUMultiChoiceListener<Result> implements AbsListView.MultiChoiceModeListener
{
   private AppCompatActivity activity;

   private ListView listView;

   private List<Integer> listId = new ArrayList<>();

   public OUMultiChoiceListener(AppCompatActivity activity, ListView listView)
   {
      this.activity = activity;
      this.listView = listView;
      this.listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
   }

   protected abstract Result doBackgroundTask();

   protected abstract void doAfterTaskCompletionBeforeRestoration(Result result);

   protected abstract void doAfterRestoration (Result result);

   public List<Integer> getListId ()
   {
      return listId;
   }

   @Override
   public boolean onCreateActionMode (ActionMode mode, Menu menu)
   {
      MenuInflater inflater = mode.getMenuInflater();
      inflater.inflate(R.menu.appbar_list_longtap, menu);
      mode.setTitle("Select Items");
      listId.clear();

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
      Integer itemId = (int)id;
      if (checked)
         listId.add(itemId);
      else
         listId.remove(itemId);
      mode.setSubtitle(listId.size() + " selected");
   }

   @Override
   public boolean onActionItemClicked(ActionMode mode, MenuItem item)
   {
      switch (item.getItemId())
      {
         case R.id.appbar_list_delete:
            getAsyncTask().execute();
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

   private OUAsyncTask<Void, Result> getAsyncTask()
   {
      return new OUAsyncTask<Void, Result>(activity)
      {
         @Override
         protected Result doInBackground (Void... params)
         {
            return OUMultiChoiceListener.this.doBackgroundTask();
         }

         @Override
         protected void onPostExecute(Result result)
         {
            OUMultiChoiceListener.this.doAfterTaskCompletionBeforeRestoration(result);
            super.onPostExecute(result);
            listId.clear();
            OUMultiChoiceListener.this.doAfterRestoration(result);
         }
      };
   }
}
