package com.gyoliver.couponalert;

import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class ViewEdit extends Activity {

	// Coupon table variables/constants
	private static final String TABLE_NAME = "COUPON";
	private static final String COLUMN0 = "_id";
	private static final String COLUMN1 = "STORE";
	private static final String COLUMN2 = "AMOUNT";
	private static final String COLUMN3 = "DATE";
	private static final String COLUMN4 = "PRODUCT";
	private static final String COLUMN5 = "NOTES";
	private static final String TAG = "CouponHelper_ViewEdit";
	
	DBOpenHelper dboh = new DBOpenHelper(this);
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	protected void onStart() {
		
		super.onStart();
		setContentView(R.layout.activity_view_edit);
		
		// Create database items and SQL query to populate the ListView
		final SQLiteDatabase db = dboh.getReadableDatabase(); 
		
		Cursor cursor = db.rawQuery("SELECT " + COLUMN0 + " AS _id, " + COLUMN1 + ", " + COLUMN2 + ", " + COLUMN3 + ", " + COLUMN4 + 
				", " + COLUMN5 + " FROM " + TABLE_NAME + " ORDER BY " + COLUMN3 + " DESC", null);
	    if (cursor != null) {
	        cursor.moveToFirst();
	    }
	    
	    final ViewCouponCursorAdapter adapter = new ViewCouponCursorAdapter(this, cursor, 0);
	            
	    final ListView listView = (ListView) findViewById(R.id.listView1);
	    listView.setAdapter(adapter);
	    
	    // If a ListView item is clicked 
	    listView.setOnItemClickListener(new OnItemClickListener() {
			
			
			public void onItemClick(AdapterView<?> parent, View vw, int position, long id) {
			    
				// When clicked, open up the EditCoupon activity.
				Cursor c = (Cursor) parent.getAdapter().getItem(position);
			    long couponID = c.getLong(c.getColumnIndex("_id"));
			    
				Intent myIntent = new Intent(vw.getContext(), EditCoupon.class);
				myIntent.putExtra("com.gyoliver.couponalert.CouponID", couponID);
				startActivity(myIntent);
			}
		});	
	    
	    listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			
			public boolean onItemLongClick(final AdapterView<?> parent, View vw, final int position, long id) {
			    
				// When long clicked, delete the coupon.
				Cursor c = (Cursor) parent.getAdapter().getItem(position);
				final long couponID = c.getLong(c.getColumnIndex("_id"));
				
				// Create the Alert Dialog that will be used to confirm category deletion
			    AlertDialog.Builder builder = new AlertDialog.Builder(ViewEdit.this);
			    builder.setMessage("Delete this coupon?");
			    
				// Add the buttons
				builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			               // User clicked OK button
			        	   
			        	   // create a dummy date that's going to be fed to deleteNotification (need to create a notification
			        	   // and delete in order to cancel an existing notification...)
			        	   Date nowTime = new Date(System.currentTimeMillis());
			       		   long nowTimeMilli = 2*nowTime.getTime();
			        	   
			        	   
			        	   deleteNotification(nowTimeMilli, couponID);
			        	   deleteNotification(nowTimeMilli, couponID + 100000);
			        	   
			        	   // get the string of coupon IDs saved in SharedPrefs in case the phone is turned off and alarms cancelled
			        	   SharedPreferences sp = getSharedPreferences("Active Coupon Data", MODE_PRIVATE);
			        	   String stringOfCouponData = sp.getString("String of Coupon Data", "");
			        	   deleteCouponDataInSharedPreferences(couponID, stringOfCouponData);
			        	   dboh.deleteCoupon(couponID, db);
			        	   sp = getSharedPreferences("Active Coupon Data", MODE_PRIVATE);
			        	   stringOfCouponData = sp.getString("String of Coupon Data", "");
			        	   
			        	   // Refresh the current view to remove the deleted coupon
			        	   final Cursor cursor2 = db.rawQuery("SELECT " + COLUMN0 + " AS _id, " + COLUMN1 + ", " + COLUMN2 + ", " + 
			        			   COLUMN3 + ", " + COLUMN4 + ", " + COLUMN5 + " FROM " + TABLE_NAME +
			        			   " ORDER BY " + COLUMN3 + " DESC", null);
						   if (cursor2 != null) {
						        cursor2.moveToFirst();
						   }
						   
						   adapter.changeCursor(cursor2);
						   Toast.makeText(ViewEdit.this, "Coupon deleted.", Toast.LENGTH_SHORT).show();
			           }
				      });
				builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				               // User cancelled the dialog
				        	   Toast.makeText(ViewEdit.this, "Delete canceled.", Toast.LENGTH_SHORT).show();
				           }
				       });
				
				builder.setNeutralButton(R.string.mark_as_used, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User chose to gray out the coupon.
							
						// When clicked, read the coupon values, change the Notes field, then re-write the values to the database.
						Cursor c1 = (Cursor) parent.getAdapter().getItem(position);
					    long couponID = c1.getLong(c1.getColumnIndex("_id"));
					    String store = c1.getString(c1.getColumnIndex("STORE"));
					    String amount = c1.getString(c1.getColumnIndex("AMOUNT"));
					    long date = c1.getLong(c1.getColumnIndex("DATE"));
					    String product = c1.getString(c1.getColumnIndex("PRODUCT"));
					    String notes = c1.getString(c1.getColumnIndex("NOTES"));
					    
					    product = "USED " + product;
					    
					    dboh.editCoupon(couponID, store, amount, date, product, notes, db);
						
						// create a dummy date that's going to be fed to deleteNotification (need to create a notification
		        	    // and delete in order to cancel an existing notification...)
		        	    Date nowTime = new Date(System.currentTimeMillis());
		       		    long nowTimeMilli = 2*nowTime.getTime();
		        	    
		        	    deleteNotification(nowTimeMilli, couponID);
		        	    deleteNotification(nowTimeMilli, couponID + 100000);
		        	    
		        	    // get the string of coupon IDs saved in SharedPrefs in case the phone is turned off and alarms cancelled
		        	    SharedPreferences sp = getSharedPreferences("Active Coupon Data", MODE_PRIVATE);
		        	    String stringOfCouponData = sp.getString("String of Coupon Data", "");
		        	    deleteCouponDataInSharedPreferences(couponID, stringOfCouponData);
		        	    sp = getSharedPreferences("Active Coupon Data", MODE_PRIVATE);
		        	    stringOfCouponData = sp.getString("String of Coupon Data", "");
		        	    
		        	    // Refresh the current view to update the coupon marked as used
			        	final Cursor cursor3 = db.rawQuery("SELECT " + COLUMN0 + " AS _id, " + COLUMN1 + ", " + COLUMN2 + ", " + 
			        			COLUMN3 + ", " + COLUMN4 + ", " + COLUMN5 + " FROM " + TABLE_NAME +
			        			" ORDER BY " + COLUMN3 + " DESC", null);
			        	if (cursor3 != null) {
			        		cursor3.moveToFirst();
			        	}
						   
						adapter.changeCursor(cursor3);
		        	    
						Toast.makeText(ViewEdit.this, "Coupon marked as used.", Toast.LENGTH_SHORT).show();
					}
				});
	
				builder.show();
				
				return true;
			}
		});		
	}
	
	
	@Override
	protected void onPause(){
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		SQLiteDatabase db = dboh.getWritableDatabase();
		db.close();
	}
	
	
	private void deleteNotification(long millisecondDate, long id) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this.getApplicationContext(), (int) id, intent, 0);
        alarmManager.cancel(alarmIntent);
        alarmIntent.cancel();
    }
	
	private void deleteCouponDataInSharedPreferences(long couponID, String stringOfData) {
		if (stringOfData.length() != 0) {
			
			String[] couponArray = stringOfData.split("®", -1);
			int couponArrayLength = couponArray.length;
			String stringOfDataOutput = "";
			
			boolean foundIt = false;
			int i = 0;
			
			// Set up the string that will identify the coupon to be deleted.
			String couponIDstring = String.valueOf(couponID) + "±";
			int couponIDlength = couponIDstring.length();
			
			// Set up the string that will identify the second alarm registration (if it exists) to be deleted.
			String couponID2string = String.valueOf(couponID + 100000) + "±";
			int couponID2length = couponID2string.length();
			
			// This loop finds the coupon information that needs to be deleted, and rewrites the stringOfDataOutput string
			// without the coupon to be deleted.
			while (i < couponArrayLength) {
				if (couponArray[i].substring(0,couponIDlength).equals(couponIDstring)) {
					foundIt = true;
				}
				else if (couponArray[i].substring(0,couponID2length).equals(couponID2string)) {
					;
				}
				else {
					stringOfDataOutput += couponArray[i] + "®";
				}
				i += 1;	
			}
			i = 0;
			
			if (stringOfDataOutput.length() != 0) {
				stringOfDataOutput = stringOfDataOutput.substring(0,stringOfDataOutput.length() - 1); // strip the final "®"
			}
				
			//if (foundIt == false){
			//	Log.w(TAG, "Coupon ID " + couponID + "'s entry in SharedPreferences was not found.");
			//}
			// update the value of String of Coupon Data in SharedPreferences
			SharedPreferences sp = getSharedPreferences("Active Coupon Data", MODE_PRIVATE);
			SharedPreferences.Editor spe = sp.edit();
	 	    spe.putString("String of Coupon Data", stringOfDataOutput);
	 	    spe.commit();
		}
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.options_menu, menu);
		return true;
	}
	
	
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		    case R.id.menu_new_coupon:
		    startActivity(new Intent(this, NewCoupon.class));
		    return true;
	    case R.id.menu_view_or_edit:
		    startActivity(new Intent(this, ViewEdit.class));
		    return true;
	    case R.id.menu_settings:
		    startActivity(new Intent(this, Help.class));
		    return true;
	    default:
	        return super.onOptionsItemSelected(item);
		}
	}
	
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		    MenuItem item = menu.findItem(R.id.menu_view_or_edit);
	        item.setEnabled(false);
        return true;
	}
}
