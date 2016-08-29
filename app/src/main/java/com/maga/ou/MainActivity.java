package com.maga.ou;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import com.maga.ou.model.*;
import com.maga.ou.model.util.AndroidDatabaseManager;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.util.UIUtil;

public class MainActivity extends AppCompatActivity
{
   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      Toolbar toolbar = (Toolbar) findViewById(R.id.single_fragment_toolbar);
      toolbar.setTitle("OU");
      setSupportActionBar(toolbar);

      findViewById(R.id.main_trip_add).setOnClickListener(new View.OnClickListener()
      {
         public void onClick(View v)
         {
            Intent intent = new Intent(v.getContext(), MainFragmentManagerActivity.class);
            intent.putExtra(MainFragmentManagerActivity.Arg.ON_START_FRAGMENT.name(), TripAddEditFragment.class.getSimpleName());
            startActivity(intent);
         }
      });


      findViewById(R.id.main_trip_list).setOnClickListener(new View.OnClickListener()
      {
         public void onClick(View v)
         {
            Intent intent = new Intent(v.getContext(), MainFragmentManagerActivity.class);
            intent.putExtra(MainFragmentManagerActivity.Arg.ON_START_FRAGMENT.name(), TripListFragment.class.getSimpleName());
            startActivity(intent);
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

   @Override
   public void onDestroy ()
   {
      super.onDestroy();
      DBUtil.closeDB();
   }
}
