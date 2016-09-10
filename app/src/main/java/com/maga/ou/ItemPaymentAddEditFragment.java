package com.maga.ou;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.maga.ou.model.Item;
import com.maga.ou.model.Trip;
import com.maga.ou.model.TripGroup;
import com.maga.ou.model.TripUser;
import com.maga.ou.model.util.DBQueryBuilder;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.model.OUDatabaseHelper.Table;
import com.maga.ou.util.OUCurrencyUtil;
import com.maga.ou.util.OUTextChangeListener;
import com.maga.ou.util.UIUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemPaymentAddEditFragment extends Fragment implements View.OnClickListener
{
   /**
    * Constants
    * ___________________________________________________________________________________________________
    */

   private final String TAG = "ou." + getClass ().getSimpleName();

   /**
    * UI Base Objects
    * ___________________________________________________________________________________________________
    */

   private Context context;

   private View viewRoot;

   private AppCompatActivity activity;

   /**
    * UI Components
    * ___________________________________________________________________________________________________
    */

   private EditText textItemSummary, textItemDetail;

   private TextView textTotalAmount;

   private LinearLayout layoutSegmentContainer;

   private SharedByDialogFragment dialogSharedBy;

   /**
    * List of index indicating the TripGroup and TripUser item that were selected.
    */
   private ArrayList<Integer> listSharedByIndex = new ArrayList<>();

   /**
    * Fragment Parameters
    * ___________________________________________________________________________________________________
    */

   public enum OperationType
   {
      Add, Edit;
   }

   private OperationType operationType = OperationType.Add;

   private int itemId = DBUtil.UNSET_ID;

   private int tripId = DBUtil.UNSET_ID;

   /**
    * Member variables
    * ___________________________________________________________________________________________________
    */

   private List<String> listTripUserName = new ArrayList<>();

   private List<String> listTripUserId = new ArrayList<>();

   private List<String> listTripGroupName = new ArrayList<>();

   private List<String> listTripGroupId = new ArrayList<>();

   private Item item = null;

   private int totalAmount = 0;

   /**
    * Constructor
    * ___________________________________________________________________________________________________
    */

   /**
    * <b>Parameters</b>
    * <ul>
    *    <li>operationType : (Add|Edit) Optional. Default is 'Add'</li>
    *    <li>itemId        : Optional if operationType is 'Add'   </li>
    *    <li>tripId        : Mandatory </li>
    * </ul>
    */
   public ItemPaymentAddEditFragment()
   {

   }

   /**
    * Setters
    * ___________________________________________________________________________________________________
    */

   public void setOperationType (OperationType operationType)
   {
      this.operationType = operationType;
   }

   public void setItemId (int id)
   {
      this.itemId = id;
   }

   public void setTripId (int id)
   {
      this.tripId = id ;
   }

   /**
    * Life cycle methods
    * ___________________________________________________________________________________________________
    */

   @Override
   public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      // Inflate the layout for this fragment
      context = inflater.getContext();
      return inflater.inflate(R.layout.fragment_item_payment_add_edit, container, false);
   }

   @Override
   public void onActivityCreated (Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);
      this.viewRoot = getView();
      this.activity = (AppCompatActivity)getActivity();
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

      if (id == R.id.item_payment_add_edit__paid_by_add)
         doAddSinglePaidBySegment();
      else if (id == R.id.segment_paid_by_add_edit__delete)
         doDeletePaidBySegment(view);
      else if (id == R.id.item_payment_add_edit__shared_by)
         doSelectSharedBy();
      else if (id == R.id.item_payment_add_edit__save)
         doSave ();
      else if (id == R.id.item_payment_add_edit__cancel)
         doCancel ();
   }

   /**
    * Member functions
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
      DBUtil.assertSetId(tripId);

      SQLiteDatabase db = DBUtil.getDB(context);
      if (operationType == OperationType.Add)
      {
         itemId = DBUtil.UNSET_ID;
         item = new Item();
      }
      else if (operationType == OperationType.Edit)
      {
         DBUtil.assertSetId(itemId);
         item = Item.getInstance(db, itemId);
      }

      List<List<String>> listData = null;
      Cursor cursor;

      // Populate the list of trip users
      cursor = new DBQueryBuilder(db)
            .select(TripUser.Column._id, TripUser.Column.NickName)
            .from(Table.TripUser)
            .orderBy(TripUser.Column.NickName)
            .where (TripUser.Column.TripId + " = " + tripId)
            .query();
      listData = DBUtil.getColumn(cursor, TripUser.Column._id, TripUser.Column.NickName);
      listTripUserId = listData.get(0);
      listTripUserName = listData.get(1);

      // Populate the list of trip groups
      cursor = new DBQueryBuilder(db)
            .select(TripGroup.Column._id, TripGroup.Column.Name)
            .from(Table.TripGroup)
            .where(TripGroup.Column.TripId + " = " + tripId)
            .query();
      listData = DBUtil.getColumn(cursor, TripGroup.Column._id, TripGroup.Column.Name);
      listTripGroupId = listData.get(0);
      listTripGroupName = listData.get(1);

      // By default select the first group (All) at index '0'
      if (operationType == OperationType.Add)
         listSharedByIndex.add(0);
   }

   private void inflateUIComponents()
   {
      if (operationType == OperationType.Add)
         UIUtil.setAppBarTitle(activity, "Add Item");
      else
         UIUtil.setAppBarTitle(activity, "Edit Item");

      // Summary Text
      textItemSummary   = (EditText)viewRoot.findViewById(R.id.item_payment_add_edit__summary);

      // Details Text
      textItemDetail    = (EditText)viewRoot.findViewById(R.id.item_payment_add_edit__detail);

      // Total Amount Text
      textTotalAmount   = (TextView)viewRoot.findViewById(R.id.item_payment_add_edit__total_amount);

      // Add paid by segments.
      layoutSegmentContainer  = (LinearLayout)viewRoot.findViewById(R.id.item_payment_add_edit__paid_by_container);
      if (operationType == OperationType.Add)
         doAddSinglePaidBySegment();

      // Add button for segment
      Button buttonAddUserAmount = (Button)viewRoot.findViewById(R.id.item_payment_add_edit__paid_by_add);
      buttonAddUserAmount.setOnClickListener(this);

      // Select shared by users
      Button buttonSharedBy = (Button)viewRoot.findViewById(R.id.item_payment_add_edit__shared_by);
      buttonSharedBy.setOnClickListener(this);

      // Save
      Button buttonSave = (Button)viewRoot.findViewById(R.id.item_payment_add_edit__save);
      buttonSave.setOnClickListener(this);

      // Cancel
      Button buttonCancel = (Button)viewRoot.findViewById(R.id.item_payment_add_edit__cancel);
      buttonCancel.setOnClickListener(this);

      // Ensure that the 'listSharedByIndex' are chosen even if user does not click on 'SharedBy' button.
      dialogSharedBy = new SharedByDialogFragment();
      Bundle bundle = new Bundle();
      bundle.putIntegerArrayList(SharedByDialogFragment.Arg.ChosenIndexList.name(), listSharedByIndex);
      dialogSharedBy.setArguments(bundle);
   }

   private void doSave ()
   {
      // Validate Input
      boolean valid = true;
      final String lineHR = "--------------------------------------------------------------------------------";

      StringBuilder builder = new StringBuilder();
      builder.append(DBUtil.NEW_LINE).append(lineHR);

      // Item Id
      if (operationType == OperationType.Edit)
         DBUtil.assertSetId(itemId);

      // Item summary
      String itemSummary = textItemSummary.getText().toString();
      if (itemSummary.equals(""))
      {
         textItemSummary.setError("Please fill item summmary");
         valid = false;
      }

      // Users
      if (listTripUserId.isEmpty())
      {
         valid = false;
      }

      builder.append("Summary=").append(itemSummary).append(DBUtil.NEW_LINE);

      // Item detail
      String itemDetail  = textItemDetail.getText().toString();
      if (itemDetail == null)
         itemDetail = "";
      builder.append("Detail=").append(itemDetail).append(DBUtil.NEW_LINE);

      // PaidBy Map - UserId to Amount
      Map<Integer,Integer> mapPaidByUserIdAmount = new HashMap<>();
      listSharedByIndex = dialogSharedBy.getChosenIndexList();

      // Iterate through all the PaidBy segments and populate PaidBy map.
      int count = layoutSegmentContainer.getChildCount();
      for (int i = 0; i < count; ++i)
      {
         ViewGroup childLayout = (ViewGroup)layoutSegmentContainer.getChildAt(i);

         Spinner  comboUser = (Spinner)childLayout.findViewById(R.id.segment_paid_by_add_edit__user);
         EditText txtAmount = (EditText) childLayout.findViewById(R.id.segment_paid_by_add_edit__amount);

         int userId = Integer.valueOf(listTripUserId.get(comboUser.getSelectedItemPosition()));
         int userAmount  = OUCurrencyUtil.valueOf(txtAmount.getText().toString());

         mapPaidByUserIdAmount.put(userId, userAmount);
         builder.append("UserId=").append(userId).append(" UserName=").append(listTripUserName.get(comboUser.getSelectedItemPosition())).append(" Amount").append(userAmount).append(DBUtil.NEW_LINE);

         if (userAmount == 0)
         {
            txtAmount.setError("Please fill amount more than zero");
            valid = false;
         }
      }

      // Use a set of SharedBy users to avoid duplicates.
      Set<Integer> setSharedByUserId = new HashSet<>();
      List<Integer> listSharedByGroupId = new ArrayList<>();
      builder.append("SharedBy=");
      for (int index : listSharedByIndex)
      {
         if (index < listTripGroupId.size())
         {
            // Index of a group - Get the groupId from listTripGroupId
            Integer groupId = Integer.valueOf(listTripGroupId.get(index));
            builder.append (listTripGroupName.get(index)).append(",");
            listSharedByGroupId.add(groupId);
         }
         else
         {
            // Index of a user - Get the userId from listTripUserId. (The index is actually index + group.size, so remove group.size)
            Integer userId = Integer.valueOf(listTripUserId.get(index - listTripGroupId.size()));
            builder.append (listTripUserName.get(index - listTripGroupId.size())).append(",");
            setSharedByUserId.add(userId);
         }
      }
      builder.append(lineHR).append(DBUtil.NEW_LINE);
      Log.d(TAG, builder.toString());

      // End processing if not valid
      if (!valid)
         return;

      // Update database
      SQLiteDatabase db = DBUtil.getDB(context);
      db.beginTransaction();
      try
      {
         // Update Item
         item.setSummary(itemSummary);
         item.setDetail(itemDetail);

         // Add or update item
         if (operationType == OperationType.Add)
         {
            // Add item
            item.add(db);

            // Add item to trip
            Trip trip = Trip.getLiteInstance(db, tripId);
            trip.addItem(db, item);
         }
         else
         {
            // Update item
            item.update(db);

            // Remove paid-by and shared-by entries for this item
            item.deletePaidBy(db);
            item.deleteSharedBy(db);
         }

         // Set users and amount paid by each user for the item.
         item.setPaidBy(db, mapPaidByUserIdAmount);

         // Get users from all SharedBy groups. Add this to set of SharedBy users.
         // We now have a distinct set of SharedBy users.
         setSharedByUserId.addAll(TripGroup.getUsersFromGroups(db, listSharedByGroupId));
         item.setSharedBy(db, setSharedByUserId);

         db.setTransactionSuccessful();
         Toast.makeText(context, "Saved successfully", Toast.LENGTH_SHORT).show();
      }
      catch (Throwable e)
      {
         Toast.makeText(context, "Error occurred during save", Toast.LENGTH_SHORT).show();
         Log.e(TAG, "Exception saving payment details", e);
      }
      finally
      {
         db.endTransaction();
      }
      getActivity().onBackPressed();
   }

   private void doCancel ()
   {
      getActivity().onBackPressed();
   }

   private void doSelectSharedBy ()
   {
      ArrayList<String> listGroupAndUser = new ArrayList<>();
      listGroupAndUser.addAll(listTripGroupName);
      listGroupAndUser.addAll(listTripUserName);

      Bundle bundle = new Bundle();
      bundle.putStringArrayList (SharedByDialogFragment.Arg.NameList.name(), listGroupAndUser);
      bundle.putIntegerArrayList(SharedByDialogFragment.Arg.ChosenIndexList.name(), listSharedByIndex);
      bundle.putInt(SharedByDialogFragment.Arg.TripGroupSize.name(), listTripGroupId.size());
      dialogSharedBy.setArguments(bundle);
      dialogSharedBy.show(getFragmentManager(), "dialog_shared_by");
   }

   private void populateUIComponents ()
   {
      if (operationType == OperationType.Add)
         return;

      SQLiteDatabase db = DBUtil.getDB(context);

      // Set summary
      textItemSummary.setText(item.getSummary());

      // Set detail
      textItemDetail.setText(item.getDetail());

      /* Add paid-by segments and populate it. */
      Map<TripUser,Integer> mapUserToAmount = item.getPaidByUsers(db);
      for (Map.Entry<TripUser,Integer> entry : mapUserToAmount.entrySet())
      {
         View segmentViewRoot = doAddSinglePaidBySegment ();
         TripUser user  = entry.getKey();
         Integer amount = entry.getValue();
         Log.d(TAG, "UserId=" + user.getId() + " UserName=" + user.getNickName() + " Amount=" + OUCurrencyUtil.format(amount) + " Index=" + listTripUserId.indexOf(String.valueOf(user.getId())) + " List=" + listTripUserId);

         Spinner     comboUser    = (Spinner)  segmentViewRoot.findViewById(R.id.segment_paid_by_add_edit__user);
         comboUser.setSelection(listTripUserId.indexOf(String.valueOf(user.getId())));

         EditText txtAmount    = (EditText) segmentViewRoot.findViewById(R.id.segment_paid_by_add_edit__amount);
         txtAmount.setText(OUCurrencyUtil.format(amount));
      }

      /* Set shared-by indexes */
      List<TripUser> listUser = item.getSharedByUsers(db);

      // Find the index of the already chosen user in list of all users
      // Add this index to group size the get the actual index in the 'shared-by' list
      int groupSize = listTripGroupId.size();
      for (TripUser user : listUser)
      {
         Log.d(TAG, "Shared Trip UserIndex=" + listTripUserId.indexOf(String.valueOf(user.getId())));
         listSharedByIndex.add(groupSize + listTripUserId.indexOf(String.valueOf(user.getId())));
      }
   }

   /**
    * Add PaidBy segment {@code R.layout.segment_paid_by_add_edit} to the segment container layout.
    *
    * @return The view root of the segment added.
    */
   private View doAddSinglePaidBySegment()
   {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      final View segmentViewRoot = inflater.inflate(R.layout.segment_paid_by_add_edit, layoutSegmentContainer, false);

      Spinner     comboUser    = (Spinner)  segmentViewRoot.findViewById(R.id.segment_paid_by_add_edit__user);
      UIUtil.setSpinnerList(context, comboUser, listTripUserName);

      final EditText txtAmount    = (EditText) segmentViewRoot.findViewById(R.id.segment_paid_by_add_edit__amount);
      UIUtil.setTextCurrencyHandler(txtAmount);
      txtAmount.addTextChangedListener(new OUTextChangeListener()
      {
         @Override
         public void onTextChanged(String oldText, String newText)
         {
            Log.d(TAG, " OldText=" + oldText + " NewText=" + newText);
            int oldValue = OUCurrencyUtil.valueOf(oldText);
            int newValue = OUCurrencyUtil.valueOf(newText);
            totalAmount = totalAmount - oldValue + newValue;
            textTotalAmount.setText(OUCurrencyUtil.format(totalAmount));
         }
      });

      ImageButton buttonDelete = (ImageButton) segmentViewRoot.findViewById(R.id.segment_paid_by_add_edit__delete);
      buttonDelete.setOnClickListener(this);
      layoutSegmentContainer.addView(segmentViewRoot);

      return segmentViewRoot;
   }

   private void doDeletePaidBySegment (View view)
   {
      Log.d(TAG, "Segment container child count. Count=" + layoutSegmentContainer.getChildCount());
      if (layoutSegmentContainer.getChildCount() == 1)
      {
         Toast.makeText(context, "Atleast one entry is required", Toast.LENGTH_SHORT).show();
         return;
      }

      final EditText txtAmount    = (EditText) ((LinearLayout)view.getParent()).findViewById(R.id.segment_paid_by_add_edit__amount);
      int value = OUCurrencyUtil.valueOf(txtAmount.getText().toString());
      totalAmount -= value;
      textTotalAmount.setText(OUCurrencyUtil.format(totalAmount));

      layoutSegmentContainer.removeView((View)view.getParent());
   }
}
