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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.maga.ou.model.Trip;
import com.maga.ou.model.TripUser;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.model.util.ReportGenerator;
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

      if (id == R.id.trip_report__show)
         doShowReport();
      else if (id == R.id.trip_report__share)
         doShareReport();
   }


   /*
    * Members methods
    * ___________________________________________________________________________________________________
    */

   private void initMembers()
   {
      initMemberFromModel ();
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

      // Show button
      Button buttonShowReport = (Button)viewRoot.findViewById(R.id.trip_report__show);
      buttonShowReport.setOnClickListener(this);

      // Add user segments
      addAllUserSegments();

      // Save
      Button buttonShare = (Button) viewRoot.findViewById(R.id.trip_report__share);
      buttonShare.setOnClickListener(this);
   }

   private void addAllUserSegments ()
   {
      ViewGroup layoutSharedByUsersContainer = (ViewGroup)viewRoot.findViewById(R.id.trip_report__users_container);
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      final int bgColor[] = context.getResources().getIntArray(R.array.bgRainbowDark);
      final int bgColorUnCheck = context.getResources().getColor(R.color.bgUnCheckUser);

      int index = -1;
      for (final TripUser user : listTripUser)
      {
         final int bgColorCheck = bgColor[++index % bgColor.length];
         final View segmentViewRoot = inflater.inflate(R.layout.segment_report_user_add_edit, layoutSharedByUsersContainer, false);

         CheckBox checkBox = (CheckBox) segmentViewRoot.findViewById(R.id.segment_report_user_add_edit__name);
         checkBox.setText(user.getNickName());
         checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
         {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
               if (isChecked)
               {
                  setChosenUserId.add(user.getId());
                  segmentViewRoot.setBackgroundColor(bgColorCheck);
               }
               else
               {
                  setChosenUserId.remove(Integer.valueOf(user.getId()));
                  segmentViewRoot.setBackgroundColor(bgColorUnCheck);
               }
            }
         });
         layoutSharedByUsersContainer.addView(segmentViewRoot);

         // Check the box if the user is already part of the group
         if(setChosenUserId.contains(user.getId()))
         {
            checkBox.setChecked(true);
            segmentViewRoot.setBackgroundColor(bgColorCheck);
         }
         else
         {
            checkBox.setChecked(false);
            segmentViewRoot.setBackgroundColor(bgColorUnCheck);
         }
      }
   }

   private void doShowReport ()
   {
      Log.i(TAG, "Users selected for share == " + setChosenUserId);
      File fileReport = ReportGenerator.getPDFReportFile(context, tripId);

      Intent intentOpenPdf = new Intent(Intent.ACTION_VIEW)
         .setType("application/pdf")
         .setData(Uri.fromFile(fileReport));

      startActivity(Intent.createChooser(intentOpenPdf, "Open PDF Report"));
   }

   private void doShareReport()
   {
      File fileReport = ReportGenerator.getPDFReportFile(context, tripId);

      Intent intentShare = new Intent()
         .setAction(Intent.ACTION_SEND)
         .setType("application/pdf")
            .putExtra(Intent.EXTRA_TEXT, "Share Report")
         .putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileReport));

      startActivity(Intent.createChooser(intentShare, "Share PDF Report"));
   }
}
