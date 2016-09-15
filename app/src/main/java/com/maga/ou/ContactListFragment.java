package com.maga.ou;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.maga.ou.model.TripGroup;
import com.maga.ou.model.TripUser;
import com.maga.ou.model.util.DBUtil;
import com.maga.ou.util.OUTextChangeListener;
import com.maga.ou.util.UIUtil;
import java.util.*;

/**
 * Created by rbseshad on 25-Aug-16.
 */
public class ContactListFragment extends ListFragment
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

   private AppCompatActivity activity;

   private Context context;

   private View viewRoot;

   /**
    * Fragment Parameters
    * ___________________________________________________________________________________________________
    */

   private int tripId = DBUtil.UNSET_ID;

   /**
    * Member variables
    * ___________________________________________________________________________________________________
    */

   private Map<Integer,Contact> mapPositionToContact = new TreeMap<>();

   /**
    * Constructor
    * ___________________________________________________________________________________________________
    */

   /**
    * <b>Parameters</b>
    * None
    */
   public ContactListFragment()
   {
   }

   /**
    * Setters
    * ___________________________________________________________________________________________________
    */

   public void setTripId (int id)
   {
      this.tripId = id;
   }

   /**
    * Lifecycle methods
    * ___________________________________________________________________________________________________
    */

   /**
    * <br/>
    * <br/><b>Inherited Doc:</b>
    * <br/>{@inheritDoc}
    */
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      // Inflate the layout for this fragment
      context = inflater.getContext();
      setHasOptionsMenu(true);
      return inflater.inflate(R.layout.fragment_contact_list, container, false);
   }

   /**
    * Called when the fragment's activity has been created and this fragment's view hierarchy instantiated.
    * Used to retrieving views or restoring state.
    *
    * <br/>
    * <br/><b>Inherited Doc:</b>
    * <br/>{@inheritDoc}
    */
   @Override
   public void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      this.viewRoot = getView();
      this.activity = (AppCompatActivity)getActivity();

      initMembers();
   }

   /**
    * Invoked when an trip item is clicked.
    *
    * <br/>
    * <br/><b>Inherited Doc:</b>
    * <br/>{@inheritDoc}
    */
   @Override
   public void onListItemClick(ListView l, View view, int position, long contactId)
   {
      String nickName = ((TextView)view.findViewById(R.id.segment_contact_list__nickname)).getText().toString();
      String fullName = ((TextView)view.findViewById(R.id.segment_contact_list__fullname)).getText().toString();

      Contact contact = null;
      Integer objPosition = position;
      if (mapPositionToContact.keySet().contains(objPosition))
         contact = mapPositionToContact.remove(objPosition);
      else
      {
         contact = new Contact ();
         contact.contactId = String.valueOf(contactId);
         contact.fullName  = fullName;
         contact.nickName  = nickName;
         contact.mobile    = Contact.getMobile(activity, String.valueOf(contactId));
         mapPositionToContact.put(objPosition, contact);
      }
      UIUtil.setAppBarTitle(activity, R.string.contact_title_add, mapPositionToContact.size());
      Log.d(TAG, "Contact = " + contact);
      ((CursorAdapter)l.getAdapter()).notifyDataSetChanged();
   }

   /**
    * Use custom app bar - {@code R.menu.appbar_list_add}
    *
    * <br/>
    * <br/><b>Inherited Doc:</b>
    * <br/>{@inheritDoc}
    */
   @Override
   public void onCreateOptionsMenu (Menu menu, MenuInflater inflater)
   {
      inflater.inflate(R.menu.appbar_list_done, menu);
      super.onCreateOptionsMenu(menu, inflater);
   }

   /**
    * Handle app bar actions - When app bar buttons are tapped.
    *
    * <br/>
    * <br/><b>Inherited Doc:</b>
    * <br/>{@inheritDoc}
    */
   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      switch (item.getItemId())
      {
         case R.id.appbar_list_done:
            doSaveContacts();
            return true;

         default:
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            return super.onOptionsItemSelected(item);
      }
   }

   /**
    * Member functions
    * ___________________________________________________________________________________________________
    */

   private void initMembers ()
   {
      initMemberFromModel();
      inflateUIComponents();
   }

   private void initMemberFromModel()
   {
      DBUtil.assertSetId(tripId);
   }

   private void inflateUIComponents ()
   {
      UIUtil.setAppBarTitle(activity, R.string.contact_title_add, 0);

      Cursor cursor = getContactsCursor();

      final CursorAdapter cursorAdapter = new ContactListAdapter(cursor);
      setListAdapter(cursorAdapter);

      EditText textSearch = (EditText)viewRoot.findViewById(R.id.contact_list__search);
      textSearch.addTextChangedListener(new OUTextChangeListener()
      {
         @Override
         public void onTextChanged(String oldText, String newText)
         {
            cursorAdapter.getFilter().filter(newText);
         }
      });

      cursorAdapter.setFilterQueryProvider(new FilterQueryProvider()
      {
         @Override
         public Cursor runQuery(CharSequence constraint)
         {
            return getContactsCursor(constraint.toString());
         }
      });
   }

   private void doSaveContacts()
   {
      if (mapPositionToContact.size() == 0)
      {
         Toast.makeText(context, "No contact added", Toast.LENGTH_SHORT).show();
         return;
      }

      int contactCountToAdd = mapPositionToContact.size();
      SQLiteDatabase db = DBUtil.getDB(context);
      db.beginTransaction();
      try
      {
         List<String> listContactId = TripUser.getTripUserContactIds(db, tripId);

         for (Map.Entry<Integer,Contact> entry : mapPositionToContact.entrySet())
         {
            Contact contact = entry.getValue();
            if (listContactId.contains(contact.contactId))
            {
               contactCountToAdd--;
               Log.d(TAG, "Contact " + contact + " already exists");
               continue;
            }
            TripUser user = new TripUser ();
            user.setNickName(contact.nickName);
            user.setFullName(contact.fullName);
            user.setContactId(contact.contactId);
            user.setTripId(tripId);
            user.add(db);

            TripGroup.addUserToGroupOfAll(db, tripId, user);
         }

         db.setTransactionSuccessful();
         Toast.makeText(context, String.format("Added %d new contacts", contactCountToAdd), Toast.LENGTH_SHORT).show();
      }
      catch (Throwable e)
      {
         Toast.makeText(context, "Error occurred during save", Toast.LENGTH_SHORT).show();
         Log.e(TAG, "Exception saving contact details", e);
      }
      finally
      {
         db.endTransaction();
      }
      getActivity().onBackPressed();
   }

   private Cursor getContactsCursor ()
   {
      return getContactsCursor(null);
   }

   private Cursor getContactsCursor (String filterName)
   {
      Uri contentURI   = ContactsContract.Contacts.CONTENT_URI;
      String select [] = new String[] {ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME};
      String where     = ContactsContract.Contacts.DISPLAY_NAME + " NOT LIKE ? AND " +
                         ContactsContract.Contacts.HAS_PHONE_NUMBER + " = ?";
      List<String> listWhereArg = new ArrayList<>(Arrays.asList("%@%", "1"));
      String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

      if (filterName != null && !filterName.isEmpty())
      {
         where = where + " AND " + ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?";
         listWhereArg.add("%" + filterName + "%");
      }

      ContentResolver contentResolver = activity.getContentResolver();
      return contentResolver.query(contentURI, select, where, listWhereArg.toArray(new String[0]), sortOrder);
   }

   /**
    * Cursor Adapter for ListView
    */
   private class ContactListAdapter extends ResourceCursorAdapter implements Filterable
   {
      public ContactListAdapter (Cursor cursor)
      {
         this(cursor, 0);
      }

      public ContactListAdapter (Cursor cursor, int flags)
      {
         super(context, R.layout.segment_contact_list, cursor, flags);
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent)
      {
         View view = super.getView(position, convertView, parent);

         if (mapPositionToContact.keySet().contains(Integer.valueOf(position)))
            view.setBackgroundColor(getResources().getColor(R.color.bgListItemSelected));
         else
            view.setBackgroundColor(getResources().getColor(R.color.bgLightPrimary));

         return view;
      }

      @Override
      public void bindView(View view, Context context, Cursor cursor)
      {
         // Nick name
         TextView textNickName   = (TextView)view.findViewById(R.id.segment_contact_list__nickname);

         // Full name
         TextView textFullName = (TextView)view.findViewById(R.id.segment_contact_list__fullname);

         // Mobile
         TextView textMobile = (TextView)view.findViewById(R.id.segment_contact_list__mobile);

         // Get contactId from Contacts
         String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

         // Get full name from Contacts
         String fullName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)).trim();

         textNickName.setText(Contact.getNickName(fullName));
         textFullName.setText(fullName);
         textMobile.setText(Contact.getMobile(activity, contactId));
      }
   }

   public static class Contact
   {
      public String contactId, nickName, fullName, mobile;

      public static String getNickName (String fullName)
      {
         String nickName = "Anonymous";
         String token[] = fullName.split("\\s+");
         if (token.length > 0)
         {
            if (token.length == 1)
               nickName = token[0];
            else
               nickName = (token[0].length() > 3) ?  token[0] : token[1];
         }
         return nickName;
      }

      private static String getMobile (Activity activity, String contactId)
      {
         ContentResolver contentResolver = activity.getContentResolver();
         Uri phoneContentURI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
         String select[]   = new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER};
         String where      = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
         String whereArg[] = new String[] {contactId };
         Cursor cursorPhone = contentResolver.query (phoneContentURI, select, where, whereArg, null);

         String mobile = "";
         if (cursorPhone != null)
         {
            if (cursorPhone.moveToFirst())
            {
               int index = cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
               mobile = (index != -1) ? cursorPhone.getString(index) : "";
            }
            cursorPhone.close();
         }
         return mobile;
      }

      @Override
      public String toString ()
      {
         return "[" + contactId + ", " + fullName + ", " + mobile + "]";
      }
   }

}
