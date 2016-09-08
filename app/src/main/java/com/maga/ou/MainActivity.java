package com.maga.ou;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import com.maga.ou.model.*;
import com.maga.ou.model.util.AndroidDatabaseManager;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.util.UIUtil;

public class MainActivity extends AppCompatActivity
{
   /**
    * Constants
    * ___________________________________________________________________________________________________
    */
   public static String TAG = "ou." + MainActivity.class.getSimpleName();

   private static final int REQUEST_CODE_TRIP_ADD  = 101;

   private static final int REQUEST_CODE_TRIP_LIST = 100;

   /**
    * Lifecycle methods
    * ___________________________________________________________________________________________________
    */

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      Toolbar toolbar = (Toolbar) findViewById(R.id.single_fragment_toolbar);
      toolbar.setTitle("Owe yoU");
      toolbar.setSubtitle("Track what you owe and who owes you");
      setSupportActionBar(toolbar);

      findViewById(R.id.main_trip_add).setOnClickListener(new View.OnClickListener()
      {
         public void onClick(View v)
         {
            goToFragment(TripAddEditFragment.class, REQUEST_CODE_TRIP_ADD);
         }
      });


      findViewById(R.id.main_trip_list).setOnClickListener(new View.OnClickListener()
      {
         public void onClick(View v)
         {
            goToFragment(TripListFragment.class, REQUEST_CODE_TRIP_LIST);
         }
      });

//      findViewById(R.id.main_database).setOnClickListener(new View.OnClickListener()
//      {
//         public void onClick(View v)
//         {
//            startActivity(new Intent(v.getContext(), AndroidDatabaseManager.class));
//         }
//      });

      TripGroup.logGroups(DBUtil.getDB(this));
  }

   private void goToFragment (Class classFragment, int requestCode)
   {
      Intent intent = new Intent(this, MainFragmentManagerActivity.class);
      intent.putExtra(MainFragmentManagerActivity.Arg.ON_START_FRAGMENT.name(), classFragment.getSimpleName());
      startActivityForResult(intent, requestCode);
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data)
   {
      super.onActivityResult(requestCode, resultCode, data);

      if (requestCode == REQUEST_CODE_TRIP_ADD)
      {
         if (resultCode == RESULT_OK)
            goToFragment(TripListFragment.class, REQUEST_CODE_TRIP_LIST);
      }
   }

   @Override
   public void onDestroy ()
   {
      super.onDestroy();
      DBUtil.closeDB();
   }
}
