package com.maga.ou;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.maga.ou.model.*;
import com.maga.ou.model.util.AndroidDatabaseManager;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.util.UIUtil;
import com.vstechlab.easyfonts.EasyFonts;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
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
      initMembers();
   }

   /**
    * Event handlers
    * ___________________________________________________________________________________________________
    */

   @Override
   public void onClick (View view)
   {
      int id = view.getId();

      if (id == R.id.main_trip_add)
         goToFragment(TripAddEditFragment.class, REQUEST_CODE_TRIP_ADD);
      else if (id == R.id.main_trip_list)
         goToFragment(TripListFragment.class, REQUEST_CODE_TRIP_LIST);

      // To start DB
      // startActivity(new Intent(v.getContext(), AndroidDatabaseManager.class));
   }

   /**
    * Member functions
    * ___________________________________________________________________________________________________
    */

   private void initMembers ()
   {
      inflateUIComponents();
      TripGroup.logGroups(DBUtil.getDB(this));
   }

   private void inflateUIComponents ()
   {
      Typeface fontAlexBrush = Typeface.createFromAsset(getAssets(), "fonts/AlexBrush-Regular.ttf");

      Button buttonTripAdd = (Button)findViewById(R.id.main_trip_add);
      buttonTripAdd.setTypeface(EasyFonts.cac_champagne(this), Typeface.BOLD);
      buttonTripAdd.setOnClickListener(this);

      Button buttonTripList = (Button)findViewById(R.id.main_trip_list);
      buttonTripList.setTypeface(EasyFonts.cac_champagne(this), Typeface.BOLD);
      buttonTripList.setOnClickListener(this);
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
