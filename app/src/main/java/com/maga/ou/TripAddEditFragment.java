package com.maga.ou;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.maga.ou.model.Trip;
import com.maga.ou.model.TripGroup;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.util.UIUtil;


/**
 * A simple {@link Fragment} subclass.
 */
public class TripAddEditFragment extends Fragment implements View.OnClickListener
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

   private Context context;

   private View viewRoot;

   private AppCompatActivity activity;

   /**
    * Fragment Parameters
    * ___________________________________________________________________________________________________
    */

   public enum OperationType
   {
      Add, Edit
   }

   private OperationType operationType = OperationType.Add;

   private int tripId = DBUtil.UNSET_ID;

   /**
    * Constructor
    * ___________________________________________________________________________________________________
    */

   /**
    * <b>Parameters</b>
    * <ul>
    * <li>operationType : (Add|Edit) Optional. Default is 'Add'</li>
    * <li>tripId        : Mandatory. </li>
    * <li>userId        : Optional if operationType is 'Add'   </li>
    * </ul>
    */
   public TripAddEditFragment()
   {

   }

   /**
    * Setters
    * ___________________________________________________________________________________________________
    */

   public void setOperationType(OperationType operationType)
   {
      this.operationType = operationType;
   }

   public void setTripId(int id)
   {
      this.tripId = id;
   }

   /**
    * Lifecycle methods
    * ___________________________________________________________________________________________________
    */

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      // Inflate the layout for this fragment
      context = inflater.getContext();
      return inflater.inflate(R.layout.fragment_trip_add_edit, container, false);
   }

   @Override
   public void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);
      this.viewRoot = getView();
      this.activity = (AppCompatActivity) getActivity();
      initMembers();
   }

   /**
    * Event Handler
    * ___________________________________________________________________________________________________
    */

   @Override
   public void onClick(View view)
   {
      int id = view.getId();
      if (id == R.id.trip_add_edit__save)
         doSave();
      else if (id == R.id.trip_add_edit__cancel)
         doCancel();
   }

   /**
    * Members methods
    * ___________________________________________________________________________________________________
    */

   private void initMembers()
   {
      initMemberFromModel();
      inflateUIComponents();
      populateUIComponents();
   }

   private void initMemberFromModel()
   {
      if (operationType == OperationType.Edit)
         DBUtil.assertSetId(tripId);
   }

   private void inflateUIComponents()
   {
      if (operationType == OperationType.Add)
         UIUtil.setAppBarTitle(activity, R.string.trip_title_add);
      else
         UIUtil.setAppBarTitle(activity, R.string.trip_title_edit);

      // Save
      Button buttonSave = (Button) viewRoot.findViewById(R.id.trip_add_edit__save);
      buttonSave.setOnClickListener(this);

      // Cancel
      Button buttonCancel = (Button) viewRoot.findViewById(R.id.trip_add_edit__cancel);
      buttonCancel.setOnClickListener(this);
   }

   private void populateUIComponents ()
   {
      if (operationType == OperationType.Add)
         return;

      SQLiteDatabase db = DBUtil.getDB(context);

      Trip trip = Trip.getInstance(db, tripId);

      EditText textName = (EditText)viewRoot.findViewById(R.id.trip_add_edit__summary);
      textName.setText(trip.getName());

      EditText textDetail = (EditText)viewRoot.findViewById(R.id.trip_add_edit__detail);
      textDetail.setText(trip.getDetail());
   }

   private void doSave()
   {
      // Validate Input
      boolean valid = true;

      EditText textName = (EditText)viewRoot.findViewById(R.id.trip_add_edit__summary);
      String name = textName.getText().toString();

      EditText textDetail = (EditText)viewRoot.findViewById(R.id.trip_add_edit__detail);
      String detail = textDetail.getText().toString();

      if (name.equals(""))
      {
         textName.setError(UIUtil.getResourceString(context, R.string.trip_validation_name));
         valid = false;
      }

      if (!valid)
      {
         Log.d(TAG, "Validation failed");
         return;
      }

      // Update database
      SQLiteDatabase db = DBUtil.getDB(context);
      db.beginTransaction();
      try
      {
         Trip trip = (operationType == OperationType.Add) ? new Trip() : Trip.getInstance(db, tripId);

         trip.setName(name);
         trip.setDetail(detail);

         if (operationType == OperationType.Add)
         {
            trip.add(db);

            TripGroup group = new TripGroup();
            group.setName(TripGroup.All);
            group.setDetail(TripGroup.ALL_DESCRIPTION);
            group.setTripId(trip.getId());
            group.add(db);
         }
         else if (operationType == OperationType.Edit)
            trip.update(db);

         db.setTransactionSuccessful();
         UIUtil.doToastSaveSuccess(context);
      }
      catch (Throwable e)
      {
         Log.e(TAG, "Exception saving payment details", e);
         UIUtil.doToastSaveFailure(context);
      }
      finally
      {
         db.endTransaction();
      }
      getActivity().setResult(Activity.RESULT_OK);
      getActivity().onBackPressed();
   }

   private void doCancel()
   {
      getActivity().setResult(Activity.RESULT_CANCELED);
      getActivity().onBackPressed();
   }

}
