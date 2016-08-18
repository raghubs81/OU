package com.maga.ou;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.maga.ou.model.*;
import com.maga.ou.model.util.AndroidDatabaseManager;
import com.maga.ou.model.util.DBUtil;

public class MainActivity extends AppCompatActivity
{

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      findViewById(R.id.main_trip_list).setOnClickListener(new View.OnClickListener()
      {
         public void onClick(View v)
         {
            Intent intent = new Intent();
            startActivity(new Intent(v.getContext(), OUMainActivity.class));
         }
      });

      findViewById(R.id.main_database).setOnClickListener(new View.OnClickListener()
      {
         public void onClick(View v)
         {
            startActivity(new Intent(v.getContext(), AndroidDatabaseManager.class));
         }
      });

      TripGroup.logGroups(DBUtil.getDB(this));
  }

   @Override
   public void onDestroy ()
   {
      super.onDestroy();
      DBUtil.closeDB();
   }
}
