package com.maga.ou;

import android.content.Context;
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
import com.maga.ou.model.util.DBUtil;
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
   /*
    * Constants
    * ___________________________________________________________________________________________________
    */

   private final String TAG = "ou." + getClass ().getSimpleName();

   /*
    * UI Base Objects
    * ___________________________________________________________________________________________________
    */

   private Context context;

   private View viewRoot;

   private AppCompatActivity activity;

   /*
    * UI Components
    * ___________________________________________________________________________________________________
    */

   private EditText textItemSummary, textItemDetail;

   private TextView textTotalAmount;

   private LinearLayout layoutPaidBySegmentContainer;

   /**
    * List of index indicating the TripUsers that were selected.
    */
   private Set<Integer> setSharedByUserId = new HashSet<>();

   /**
    * Map of userId to the checkbox object
    */
   private Map<Integer,CheckBox> mapUserIdToCheckBox = new HashMap<>();

   /*
    * Fragment Parameters
    * ___________________________________________________________________________________________________
    */

   public enum OperationType
   {
      Add, Edit
   }

   private OperationType operationType = OperationType.Add;

   private int itemId = DBUtil.UNSET_ID;

   private int tripId = DBUtil.UNSET_ID;

   /*
    * Member variables
    * ___________________________________________________________________________________________________
    */
   private List<TripUser> listTripUser = null;

   private List<String> listTripUserName = new ArrayList<>();

   private List<TripGroup> listTripGroup = null;

   private Map<Integer,List<TripUser>> mapGroupIdToUsers = new HashMap<>();

   private Item item = null;

   private int totalAmount = 0;

   private int idGroupOfAll = 0;

   /*
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

   /*
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

   /*
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

   /*
    * Event handlers
    * ___________________________________________________________________________________________________
    */

   @Override
   public void onClick (View view)
   {
      int id = view.getId();

      if (id == R.id.item_payment_add_edit__paid_by_add)
         doAddSinglePaidBySegment();
      else if (id == R.id.segment_item_paid_by_add_edit__delete)
         doDeletePaidBySegment(view);
      else if (id == R.id.item_payment_add_edit__save)
         doSave ();
      else if (id == R.id.item_payment_add_edit__cancel)
         doCancel ();
   }

   /*
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

      // All trip users
      listTripUser = TripUser.getLiteTripUsers(db, tripId);

      // List of trip user names
      for (TripUser user : listTripUser)
         listTripUserName.add(user.getNickName());

      // All trip groups
      listTripGroup = TripGroup.getLiteTripGroups(db, tripId);

      // ID of group 'All'
      idGroupOfAll = TripGroup.getIdOfGroupOfAll(db, tripId);

      // Generate a map of GroupId to Users in the group
      for (TripGroup group : listTripGroup)
         mapGroupIdToUsers.put(group.getId(), group.getLiteUsers(db));

      // Add trip users who share the item
      if (operationType == OperationType.Edit)
      {
         for (TripUser currSharedByUser : item.getSharedByUsers(db))
            setSharedByUserId.add(currSharedByUser.getId());
      }
   }

   private void inflateUIComponents()
   {
      if (operationType == OperationType.Add)
         UIUtil.setAppBarTitle(activity, R.string.item_title_add);
      else
         UIUtil.setAppBarTitle(activity, R.string.item_title_edit);

      // Summary Text
      textItemSummary   = (EditText)viewRoot.findViewById(R.id.item_payment_add_edit__summary);

      // Details Text
      textItemDetail    = (EditText)viewRoot.findViewById(R.id.item_payment_add_edit__detail);

      // Total Amount Text
      textTotalAmount   = (TextView)viewRoot.findViewById(R.id.item_payment_add_edit__total_amount);

      // Add paid by segments.
      layoutPaidBySegmentContainer = (LinearLayout)viewRoot.findViewById(R.id.item_payment_add_edit__paid_by_container);

      if (operationType == OperationType.Add)
         doAddSinglePaidBySegment();

      // Add button for segment
      Button buttonAddUserAmount = (Button)viewRoot.findViewById(R.id.item_payment_add_edit__paid_by_add);
      buttonAddUserAmount.setOnClickListener(this);

      // Add shared by segments
      doAddAllSharedBySegments();

      // Save
      Button buttonSave = (Button)viewRoot.findViewById(R.id.item_payment_add_edit__save);
      buttonSave.setOnClickListener(this);

      // Cancel
      Button buttonCancel = (Button)viewRoot.findViewById(R.id.item_payment_add_edit__cancel);
      buttonCancel.setOnClickListener(this);
   }

   private void doSave ()
   {
      // Validate Input
      boolean valid = true;

      // Item Id
      if (operationType == OperationType.Edit)
         DBUtil.assertSetId(itemId);

      // Item summary
      String itemSummary = textItemSummary.getText().toString();
      if (itemSummary.equals(""))
      {
         textItemSummary.setError(UIUtil.getResourceString(context, R.string.item_validation_summary));
         valid = false;
      }

      // Users
      if (listTripUser.isEmpty())
      {
         valid = false;
      }

      // Item detail
      String itemDetail  = textItemDetail.getText().toString();

      // PaidBy Map - UserId to Amount
      Map<Integer,Integer> mapPaidByUserIdAmount = new HashMap<>();

      // Iterate through all the PaidBy segments and populate PaidBy map.
      int count = layoutPaidBySegmentContainer.getChildCount();
      for (int i = 0; i < count; ++i)
      {
         ViewGroup childLayout = (ViewGroup) layoutPaidBySegmentContainer.getChildAt(i);

         Spinner  comboUser = (Spinner)childLayout.findViewById(R.id.segment_item_paid_by_add_edit__user);
         EditText txtAmount = (EditText) childLayout.findViewById(R.id.segment_item_paid_by_add_edit__amount);

         int userId = listTripUser.get(comboUser.getSelectedItemPosition()).getId();
         int userAmount  = OUCurrencyUtil.valueOf(txtAmount.getText().toString());

         mapPaidByUserIdAmount.put(userId, userAmount);

         if (userAmount == 0)
         {
            txtAmount.setError(UIUtil.getResourceString(context, R.string.item_validation_paid_by_amount));
            valid = false;
         }
      }

      if (setSharedByUserId.isEmpty())
      {
         UIUtil.doToastError(context, R.string.item_validation_shared_by);
         valid = false;
      }

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

         // Set of all the users who share the items
         item.setSharedBy(db, setSharedByUserId);

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
      getActivity().onBackPressed();
   }

   private void doCancel ()
   {
      getActivity().onBackPressed();
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
         Log.d(TAG, "UserId=" + user.getId() + " UserName=" + user.getNickName() + " Amount=" + OUCurrencyUtil.format(amount));

         Spinner     comboUser    = (Spinner)  segmentViewRoot.findViewById(R.id.segment_item_paid_by_add_edit__user);
         comboUser.setSelection(getIndexOfUser(user.getId()));

         EditText txtAmount    = (EditText) segmentViewRoot.findViewById(R.id.segment_item_paid_by_add_edit__amount);
         txtAmount.setText(OUCurrencyUtil.format(amount));
      }
   }

   private int getIndexOfUser (int userId)
   {
      int index = -1;
      for (TripUser user : listTripUser)
      {
         ++index;
         if (userId == user.getId())
            return index;
      }
      return index;
   }

   private void doAddAllSharedBySegments ()
   {
      doAddAllSharedByUserSegments();
      doAddAllSharedByGroupSegments();
   }

   private void doAddAllSharedByGroupSegments ()
   {
      ViewGroup layoutSharedByGroupsContainer = (ViewGroup)viewRoot.findViewById(R.id.item_payment_add_edit__groups_container);
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

      for (final TripGroup group : listTripGroup)
      {
         final View segmentViewRoot = inflater.inflate(R.layout.segment_item_shared_by_group_add_edit, layoutSharedByGroupsContainer, false);
         CheckBox checkBox = (CheckBox) segmentViewRoot.findViewById(R.id.segment_item_shared_by_group_add_edit__name);
         checkBox.setText(group.getName());

         checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
         {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
               for (TripUser user : mapGroupIdToUsers.get(group.getId()))
               {
                  CheckBox checkBox = mapUserIdToCheckBox.get(user.getId());
                  checkBox.setChecked(isChecked);
               }
            }
         });

         if (operationType == OperationType.Add && idGroupOfAll == group.getId())
            checkBox.setChecked(true);

         layoutSharedByGroupsContainer.addView(segmentViewRoot);
      }
   }

   private void doAddAllSharedByUserSegments ()
   {
      ViewGroup layoutSharedByUsersContainer = (ViewGroup)viewRoot.findViewById(R.id.item_payment_add_edit__users_container);
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      final int bgColor[] = context.getResources().getIntArray(R.array.bgRainbowDark);
      final int bgColorUnCheck = context.getResources().getColor(R.color.bgUnCheckUser);

      int index = -1;
      for (final TripUser user : listTripUser)
      {
         final int colorCheck   = bgColor[++index % bgColor.length];
         final View segmentViewRoot = inflater.inflate(R.layout.segment_item_shared_by_user_add_edit, layoutSharedByUsersContainer, false);

         CheckBox checkBox = (CheckBox) segmentViewRoot.findViewById(R.id.segment_item_shared_by_user_add_edit__name);
         checkBox.setText(user.getNickName());
         checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
         {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
               if (isChecked)
               {
                  setSharedByUserId.add(user.getId());
                  segmentViewRoot.setBackgroundColor(colorCheck);
               }
               else
               {
                  setSharedByUserId.remove(Integer.valueOf(user.getId()));
                  segmentViewRoot.setBackgroundColor(bgColorUnCheck);
               }
            }
         });
         layoutSharedByUsersContainer.addView(segmentViewRoot);
         mapUserIdToCheckBox.put(user.getId(), checkBox);

         // Check the box if the user has shared the item
         if(setSharedByUserId.contains(user.getId()))
         {
            checkBox.setChecked(true);
            segmentViewRoot.setBackgroundColor(colorCheck);
         }
         else
         {
            checkBox.setChecked(false);
            segmentViewRoot.setBackgroundColor(bgColorUnCheck);
         }
      }
   }


   /**
    * Add PaidBy segment {@code R.layout.segment_item_paid_by_add_edit} to the segment container layout.
    *
    * @return The view root of the segment added.
    */
   private View doAddSinglePaidBySegment()
   {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      final View segmentViewRoot = inflater.inflate(R.layout.segment_item_paid_by_add_edit, layoutPaidBySegmentContainer, false);

      Spinner     comboUser    = (Spinner)  segmentViewRoot.findViewById(R.id.segment_item_paid_by_add_edit__user);
      UIUtil.setSpinnerList(context, comboUser, listTripUserName);

      final EditText txtAmount    = (EditText) segmentViewRoot.findViewById(R.id.segment_item_paid_by_add_edit__amount);
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

      ImageButton buttonDelete = (ImageButton) segmentViewRoot.findViewById(R.id.segment_item_paid_by_add_edit__delete);
      buttonDelete.setOnClickListener(this);
      layoutPaidBySegmentContainer.addView(segmentViewRoot);

      return segmentViewRoot;
   }

   private void doDeletePaidBySegment (View view)
   {
      Log.d(TAG, "Segment container child count. Count=" + layoutPaidBySegmentContainer.getChildCount());
      if (layoutPaidBySegmentContainer.getChildCount() == 1)
      {
         UIUtil.doToastError(context, R.string.item_validation_paid_by_count);
         return;
      }

      final EditText txtAmount    = (EditText) ((LinearLayout)view.getParent()).findViewById(R.id.segment_item_paid_by_add_edit__amount);
      int value = OUCurrencyUtil.valueOf(txtAmount.getText().toString());
      totalAmount -= value;
      textTotalAmount.setText(OUCurrencyUtil.format(totalAmount));

      layoutPaidBySegmentContainer.removeView((View) view.getParent());
   }
}
