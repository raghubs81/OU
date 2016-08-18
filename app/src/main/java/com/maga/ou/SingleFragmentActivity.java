package com.maga.ou;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

public abstract class SingleFragmentActivity extends AppCompatActivity
{
   public String TAG = "ou." + getClass().getSimpleName();

   public abstract Fragment getFragment ();

   @Override
   protected void onCreate (Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      if (savedInstanceState == null)
      {
         Fragment fragment = getFragment();
         FragmentManager fragmentManager = getSupportFragmentManager();
         FragmentTransaction transaction = fragmentManager.beginTransaction();
         transaction.replace(R.id.single_fragment_frame, fragment);
         transaction.commit();
      }
      setContentView(R.layout.activity_single_fragment);
      Toolbar toolbar = (Toolbar) findViewById(R.id.single_fragment_toolbar);
      setSupportActionBar(toolbar);
   }

   @Override
   public void onBackPressed()
   {
      // Log.d(TAG, "BackStackEntryCount=" + getFragmentManager().getBackStackEntryCount());
      if (getFragmentManager().getBackStackEntryCount() == 0)
      {
         super.onBackPressed();
      }
      else
      {
         getFragmentManager().popBackStackImmediate();
      }
   }

   public static void replaceFrameWithFragment (Fragment fragment, AppCompatActivity activity)
   {
      FragmentManager fragmentManager = activity.getSupportFragmentManager();
      FragmentTransaction transaction = fragmentManager.beginTransaction();
      transaction.replace(R.id.single_fragment_frame, fragment);
      transaction.addToBackStack(null);
      transaction.commit();
   }
}
