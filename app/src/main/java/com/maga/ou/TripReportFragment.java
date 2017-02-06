package com.maga.ou;


import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.maga.ou.model.OUAmountDistribution;
import com.maga.ou.model.Trip;
import com.maga.ou.model.TripUser;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.util.WOWDetailReportGenerator;

import java.io.File;
import java.util.*;


/**
 * A simple {@link Fragment} subclass.
 */
public class TripReportFragment extends Fragment implements View.OnClickListener
{
   /*
    * Constants
    * ___________________________________________________________________________________________________
    */

   private final String TAG = "ou." + getClass().getSimpleName();

   /*
    * UI Base Objects
    * ___________________________________________________________________________________________________
    */

   private Context context;

   private View viewRoot;

   private AppCompatActivity activity;

   /*
    * Fragment Parameters
    * ___________________________________________________________________________________________________
    */

   private int tripId = DBUtil.UNSET_ID;

   /*
    * Member variables
    * ___________________________________________________________________________________________________
    */

   private List<TripUser> listTripUser = new ArrayList<>();

   /**
    * Set of User Ids selected for sharing.
    */
   private Set<Integer> setChosenUserId = new HashSet<>();

   /**
    * <b>Parameters set using setters</b>
    * <ul>
    *    <li>tripId        : Mandatory. </li>
    * </ul>
    */
   public TripReportFragment()
   {

   }

   /*
    * Setters
    * ___________________________________________________________________________________________________
    */
   public void setTripId (int id)
   {
      this.tripId = id;
   }

   /*
    * Lifecycle methods
    * ___________________________________________________________________________________________________
    */

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      // Inflate the layout for this fragment
      context = inflater.getContext();
      return inflater.inflate(R.layout.fragment_trip_report, container, false);
   }

   @Override
   public void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);
      this.viewRoot = getView();
      this.activity = (AppCompatActivity) getActivity();
      initMembers();
   }

   /*
    * Event Handler
    * ___________________________________________________________________________________________________
    */

   @Override
   public void onClick(View view)
   {
      int id = view.getId();

      if (id == R.id.trip_report__basic_show)
         doShowBasicReport();
      else if (id == R.id.trip_report__basic_share)
         doShareBasicReport();
      else if (id == R.id.trip_report__detail_show)
         doShowDetailReport();
      else if (id == R.id.trip_report__detail_share)
         doShareDetailReport();
   }


   /*
    * Members methods
    * ___________________________________________________________________________________________________
    */

   private void initMembers()
   {
      initMemberFromModel();
      inflateUIComponents ();
   }

   private void initMemberFromModel()
   {
      DBUtil.assertSetId(tripId);
      SQLiteDatabase db = DBUtil.getDB(context);

      // All trip users
      listTripUser = TripUser.getLiteTripUsers(db, tripId);

      // Initially all users are selected to recieve the report.
      for (TripUser user : listTripUser)
         setChosenUserId.add(user.getId());
   }

   private void inflateUIComponents()
   {
      SQLiteDatabase db = DBUtil.getDB(context);
      Trip trip = Trip.getInstance(db, tripId);

      // Summary text
      TextView textSummary = (TextView)viewRoot.findViewById(R.id.trip_report__summary);
      textSummary.setText("Report - " + trip.getName());

      // Show basic report
      Button buttonBasicShow = (Button)viewRoot.findViewById(R.id.trip_report__basic_show);
      buttonBasicShow.setOnClickListener(this);

      // Share basic report
      Button buttonBasicShare = (Button)viewRoot.findViewById(R.id.trip_report__basic_share);
      buttonBasicShare.setOnClickListener(this);

      // Show detail report
      Button buttonDetailShow = (Button)viewRoot.findViewById(R.id.trip_report__detail_show);
      buttonDetailShow.setOnClickListener(this);

      // Show detail report
      Button buttonDetailShare = (Button)viewRoot.findViewById(R.id.trip_report__detail_share);
      buttonDetailShare.setOnClickListener(this);

      OUAmountDistribution amountDistribution = new OUAmountDistribution(context, tripId);
      amountDistribution.doGenerateBasicReport();
   }

   private void doShowBasicReport()
   {
      File fileReport = OUAmountDistribution.getPDFReport(context, tripId);
      doShowReport(fileReport, "application/pdf", "Basic Report");
   }

   private void doShowDetailReport()
   {
      File fileReport = WOWDetailReportGenerator.getHtmlReport(context, tripId);
      doShowReport(fileReport, "text/html", "Detailed Report");
   }

   private void doShareBasicReport()
   {
      File fileReport = OUAmountDistribution.getPDFReport(context, tripId);
      doShareReport(fileReport, "application/pdf", "Basic Report");
   }

   private void doShareDetailReport()
   {
      File fileReport = WOWDetailReportGenerator.getHtmlReport(context, tripId);
      doShareReport(fileReport, "text/html", "Detailed Report");
   }

   private void doShowReport (File fileReport, String mimeType, String title)
   {
      Log.i(TAG, "File=" + fileReport.getAbsolutePath() + " Mime=" + mimeType + " Title=" + title);
      Intent intentOpen = new Intent(Intent.ACTION_VIEW)
            .setDataAndType(Uri.fromFile(fileReport), mimeType);

      if (mimeType.equals("text/html"))
      {
         intentOpen.addCategory(Intent.CATEGORY_BROWSABLE);
         intentOpen.setPackage("com.android.chrome");
      }

      startActivity(Intent.createChooser(intentOpen, title));
   }

   private void doShareReport(File fileReport, String mimeType, String title)
   {
      Intent intentShare = new Intent(Intent.ACTION_SEND)
         .setType(mimeType)
         .putExtra(Intent.EXTRA_TEXT, title)
         .putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileReport));

      startActivity(Intent.createChooser(intentShare, title));
   }
}
