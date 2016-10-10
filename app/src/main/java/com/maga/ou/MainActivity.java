package com.maga.ou;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.maga.ou.model.*;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.util.DieDialogFragment;
import com.maga.ou.util.UIUtil;
import com.vstechlab.easyfonts.EasyFonts;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
   /*
    * Constants
    * ___________________________________________________________________________________________________
    */
   public static String TAG = "ou." + MainActivity.class.getSimpleName();

   public static final int PERMISSION_REQUEST_READ_CONTACTS = 101;

   public static final List<Integer> listRequestCode = Arrays.asList(PERMISSION_REQUEST_READ_CONTACTS);

   private static final int REQUEST_CODE_TRIP_LIST = 201;

   private static final int REQUEST_CODE_TRIP_ADD  = 202;


   /*
    * Member variables
    * ___________________________________________________________________________________________________
    */
   private enum GrantType
   {
      UNSET, ALLOW, DENY;
   }

   private GrantType grantStatus = GrantType.UNSET;

   /*
    * Lifecycle methods
    * ___________________________________________________________________________________________________
    */

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      Log.i(TAG, "Init members");
      initMembers();
   }

   @Override
   public void onRequestPermissionsResult(int requestCode, String permission[], int grantResult[])
   {
      if (grantStatus == grantStatus.DENY)
         return;

      if (listRequestCode.contains(requestCode))
      {
         // Check grant status of each permission request for a given requestCode
         // If any permission request is denied then the overall grant staus is denied - break
         for (int currGrantResult : grantResult)
         {
            if (currGrantResult == PackageManager.PERMISSION_GRANTED)
               grantStatus = GrantType.ALLOW;
            else
            {
               grantStatus = GrantType.DENY;
               break;
            }
         }
      }

      // Got all grants
      if (grantStatus == GrantType.ALLOW)
         UIUtil.doToastSuccess(this, R.string.main_permissions_grant_success);
   }

   @Override
   public void onResume ()
   {
      super.onResume();
      if (grantStatus == GrantType.DENY)
      {
         DieDialogFragment fragment = new DieDialogFragment();
         Bundle bundle = new Bundle();
         bundle.putInt(DieDialogFragment.Arg.MESSAGE_ID.name(), R.string.main_permissions_grant_error);
         fragment.setArguments(bundle);
         fragment.show(getSupportFragmentManager(), "Permission Dialog Fragment");
      }
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
   public void onDestroy()
   {
      super.onDestroy();
      DBUtil.closeDB();
   }


   /*
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

   /*
    * Member functions
    * ___________________________________________________________________________________________________
    */

   private void initMembers ()
   {
      inflateUIComponents();
      doRequestPermissions();
      TripGroup.logGroups(DBUtil.getDB(this));
   }

   private void inflateUIComponents ()
   {
      Button buttonTripAdd = (Button)findViewById(R.id.main_trip_add);
      buttonTripAdd.setTypeface(EasyFonts.cac_champagne(this), Typeface.BOLD);
      buttonTripAdd.setOnClickListener(this);

      Button buttonTripList = (Button)findViewById(R.id.main_trip_list);
      buttonTripList.setTypeface(EasyFonts.cac_champagne(this), Typeface.BOLD);
      buttonTripList.setOnClickListener(this);
   }

   private void doRequestPermissions ()
   {
      if (grantStatus != GrantType.UNSET || isPermissionGranted())
         return;

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
      {
         Log.i(TAG, "Invoke request permission");
         requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST_READ_CONTACTS);
         //After this point we wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
      }
   }

   /**
    * @return true if all permissions are granted
    */
   private boolean isPermissionGranted ()
   {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
         return true;

      boolean permissionGranted = true;
      permissionGranted = permissionGranted && (checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED);
      return permissionGranted;
   }


   private void goToFragment (Class classFragment, int requestCode)
   {
      Intent intent = new Intent(this, MainFragmentManagerActivity.class);
      intent.putExtra(MainFragmentManagerActivity.Arg.ON_START_FRAGMENT.name(), classFragment.getSimpleName());
      startActivityForResult(intent, requestCode);
   }
}
