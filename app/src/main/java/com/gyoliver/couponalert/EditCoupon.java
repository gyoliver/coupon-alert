package com.gyoliver.couponalert;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditCoupon extends Activity {

	// Coupon table variables/constants
	private static final String TABLE_NAME = "COUPON";
	private static final String COLUMN0 = "_ID";
	private static final String COLUMN1 = "STORE";
	private static final String COLUMN2 = "AMOUNT";
	private static final String COLUMN3 = "DATE";
	private static final String COLUMN4 = "PRODUCT";
	private static final String COLUMN5 = "NOTES";
	
	// Instantiate a coupon object to write coupon values to.
	private final Coupon c = new Coupon(); 		
	long couponID = 0;
	
	DBOpenHelper dboh = new DBOpenHelper(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_coupon);	
		
		// get writable database
		final SQLiteDatabase db = dboh.getWritableDatabase();
		
		// Instantiate EditTexts here so they can be accessed by onStart (typical workflow) or onResume (when interrupted).
		final EditText etStore = (EditText) findViewById(R.id.editText01);
		final EditText etAmount = (EditText) findViewById(R.id.editText02);
		final EditText etDate = (EditText) findViewById(R.id.editText03);
		final EditText etProduct = (EditText) findViewById(R.id.editText04);
		final EditText etNotes = (EditText) findViewById(R.id.editText07);

		// setup for retrieving data for the coupon that is to be edited
		// First, get the coupon ID, so the correct coupon's information can be accessed.  The coupon ID was sent with the intent.
		Intent myIntent = getIntent();
	    myIntent.getExtras();
	    couponID = myIntent.getLongExtra("com.gyoliver.couponalert.CouponID", 0); 
	    
	    // Next, create and use the SQL cursor to access the coupon data.
		final Cursor cursor = db.rawQuery("SELECT " + COLUMN0 + " AS _id, " + COLUMN1 + ", " + COLUMN2 + ", " + COLUMN3 + ", " + COLUMN4 + 
				", " + COLUMN5 + " FROM " + TABLE_NAME + " WHERE " + COLUMN0 + " = " + couponID, null);
		
		// instantiate stringDate for writing to instance state below
		String stringDate = "";
		
	    if (cursor.getCount()==0) {
	    	
	    	Toast.makeText(EditCoupon.this, "There are no coupons in the database currently.",Toast.LENGTH_LONG).show();
	    	Toast.makeText(EditCoupon.this, "You may have navigated back to the Edit Coupon " +
	    			"screen on mistake.  Returning you to the app's first screen.", Toast.LENGTH_LONG).show();
	    	cursor.close();
			db.close();
			dboh.close();
			Intent myIntent2 = new Intent(this.getBaseContext(), FrontPage.class);
			startActivity(myIntent2);	
	    }
	    
	    else {
	    	cursor.moveToFirst();
		    
		    String store = cursor.getString(1);
		    String amount = cursor.getString(2);
		    long millisecondDate = cursor.getLong(3);  
		    String product = cursor.getString(4);
		    String notes = cursor.getString(5);

			// set EditTexts' text to the correct values.
			etStore.setText(store);
			etAmount.setText(amount);
			etProduct.setText(product);
			etNotes.setText(notes);
			
			// need to translate the millisecond value of date stored in the db to the text version
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(millisecondDate);
			
			// CHANGE TO TEST DATE
			SimpleDateFormat sdf = new SimpleDateFormat("M-d-yy", Locale.US);
			stringDate = sdf.format(cal.getTime());
			etDate.setText(stringDate);
	    }
	    
	    c.setStore(etStore.getText().toString());
	    c.setAmount(etAmount.getText().toString());
	    c.setProduct(etProduct.getText().toString());
	    c.setNotes(etNotes.getText().toString());
	    
	    // Write EditText values to the instance state in SharedPreferences so in onResume, the database values for the
	    // coupon being edited are retrieved in coming directly from onCreate.  If, onResume is coming from onPause,
	    // the values retrieved will be those written to the instance state in onPause. 
	    
	    writeInstanceState(this, c, stringDate);
	    cursor.close();
	}
	
	
	@Override
	protected void onStart(){
		super.onStart();
	}
 
	
	@Override
	protected void onResume(){
		super.onResume();
		
		String stringDate = readInstanceState(this, c);
		
		// Instantiate EditTexts here so they can be accessed by onStart (typical workflow) or onResume (when interrupted).
		final EditText etStore = (EditText) findViewById(R.id.editText01);
		final EditText etAmount = (EditText) findViewById(R.id.editText02);
		final EditText etDate = (EditText) findViewById(R.id.editText03);
		final EditText etProduct = (EditText) findViewById(R.id.editText04);
		final EditText etNotes = (EditText) findViewById(R.id.editText07);
		
		// instantiate buttons
		final Button saveButton = (Button) findViewById(R.id.button1);
		final Button viewButton = (Button) findViewById(R.id.button2);
		
		// set EditText values to those retrieved from the coupon and stringDate.  This will be what was saved in onCreate
		// or what was saved in onPause.
		
		etStore.setText(c.getStore());
		etAmount.setText(c.getAmount());
		etProduct.setText(c.getProduct());
		etNotes.setText(c.getNotes());
		etDate.setText(stringDate);
		
		// The below code sets up the OnClickListener for the "Save" button, which commits any edits to the appropriate coupon in the db
	    saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View vw)
			{
				
				// Retrieve the current date for comparison to the edited date to figure out what notifications need to be deleted.
				long millisecondDate_orig = c.getDate();
				
				// Copy the data inputted by the user into the Coupon object
				c.setStore(etStore.getText().toString());
				c.setAmount(etAmount.getText().toString());
				c.setProduct(etProduct.getText().toString());
				c.setNotes(etNotes.getText().toString());
				
				// get date from EditText and convert to millisecond format, then store to Coupon objects
				boolean valid;
				String stringDate = etDate.getText().toString();
				
				// Validate that the entered data is in a format that can be parsed
				valid = validateDate(stringDate);
				//valid = true;
				
				// Make sure that ± and ® aren't entered.  These are used in the StringOfCouponData strings, which would get
				// corrupted if these string sequences were in coupon data.
				boolean validStore, validAmount, validProduct, validNotes;
				validStore = checkInvalidStrings(etStore.getText().toString());
				validAmount = checkInvalidStrings(etAmount.getText().toString());
				validProduct = checkInvalidStrings(etProduct.getText().toString());
				validNotes = checkInvalidStrings(etNotes.getText().toString());
				
				if (validStore == false || validAmount == false || validProduct == false || validNotes == false) {
					Toast.makeText(EditCoupon.this, "The characters � and � cannot be used.", Toast.LENGTH_LONG).show();
					if (valid == false){
						Toast.makeText(EditCoupon.this, "Date or date format is invalid.\nPlease use the format m-d-yy.", Toast.LENGTH_SHORT).show();	
					}
				}
				
				else if (valid == false){
					Toast.makeText(EditCoupon.this, "Date or date format is invalid.\nPlease use the format m-d-yy.", Toast.LENGTH_SHORT).show();	
				}
				
				else {
					
					// CHANGE TO TEST DATE
					SimpleDateFormat sdf = new SimpleDateFormat("M-d-yy", Locale.US);
					
					try {
						
						// Parse the entered date and convert it to millisecond format for writing to the Coupon object/the database.
						Date couponDate = sdf.parse(stringDate);
						long millisecondDate = couponDate.getTime();
						
						// Set the date in the Coupon object
						c.setDate(millisecondDate);	
						
						
						// Tell the user if the time chosen is for the same day or earlier
						
						Calendar calCoupon = Calendar.getInstance();
						calCoupon.setTimeInMillis(millisecondDate);
						int yrCoupon = calCoupon.get(Calendar.YEAR);
						int moCoupon = calCoupon.get(Calendar.MONTH);
						int dtCoupon = calCoupon.get(Calendar.DATE);
						int hrCoupon = calCoupon.get(Calendar.HOUR_OF_DAY);
						int minCoupon = calCoupon.get(Calendar.MINUTE);
						int secCoupon = calCoupon.get(Calendar.SECOND);
						
						Calendar calToday = Calendar.getInstance();
						int yrToday = calToday.get(Calendar.YEAR);
						int moToday = calToday.get(Calendar.MONTH);
						int dtToday = calToday.get(Calendar.DATE);
						int hrToday = calToday.get(Calendar.HOUR_OF_DAY);
						int minToday = calToday.get(Calendar.MINUTE);
						int secToday = calToday.get(Calendar.SECOND);
						
						
						boolean before = false;
						if (yrCoupon < yrToday){
							before = true;
						}
						
						else if (yrCoupon == yrToday){
							if (moCoupon < moToday) {
								before = true;
							}
							else if (moCoupon == moToday){
								if (dtCoupon < dtToday) {
									before = true;
								}
								
								else if (dtCoupon == dtToday){
									if (hrCoupon < hrToday) {
										before = true;
									}
									
									else if (hrCoupon == hrToday){
										if (minCoupon < minToday) {
											before = true;
										}
										
										else if (minCoupon == minToday){
											if (secCoupon < secToday) {
												before = true;
											}
											else {
												before = false;
											}
										}
									}	
								}
							}
						}
						
						
						// EDIT COUPON ONLY: Copy the data saved to the Coupon object into the coupon record in the database.
						// get writable database
						final SQLiteDatabase db = dboh.getWritableDatabase();
						dboh.editCoupon(couponID, c.getStore(), c.getAmount(), c.getDate(), c.getProduct(),  
								c.getNotes(), db);
						
						
						// Check if the Product text begins with "USED"
						String product = c.getProduct();
						
						boolean used = false;
				        if (product.length() >= 4) {
				        	String productFirstFour = product.substring(0, 4);
				        	if (productFirstFour.equals("USED")){
				        		used = true;
				        	}
				        }
						
						
						if (before == true){
							Toast.makeText(EditCoupon.this, "Coupon date entered is today or earlier.\nTo change the date,"  + 
									" click 'View Coupons'\nand click the coupon to edit it.", Toast.LENGTH_LONG).show();
							
							// EDIT COUPON ONLY:  need to un-register one or both notifications if the new date used is the same day or
							// earlier
							
							// First deal with the same-day notification for the coupon 
							deleteNotification(c.getDate(), couponID);
							SharedPreferences sp = getSharedPreferences("Active Coupon Data", MODE_PRIVATE);
				        	String stringOfCouponData = sp.getString("String of Coupon Data", "");
							deleteCouponDataInSharedPreferences(couponID, stringOfCouponData);
							sp = getSharedPreferences("Active Coupon Data", MODE_PRIVATE);
				        	stringOfCouponData = sp.getString("String of Coupon Data", "");
							
							// Next, check if there is still a two-day notification, and unregister it if so.
							long nowTimeMillis = calToday.getTimeInMillis();
							long dateDiff = millisecondDate_orig - nowTimeMillis;
							if (dateDiff < 172800000) {  // changes this value to 172800000 for the day version of the app
								deleteNotification(c.getDate(), couponID + 100000); // Two-day notifications have IDs 100,000 greater than
									// the original same-day notification for that coupon
								sp = getSharedPreferences("Active Coupon Data", MODE_PRIVATE);
					        	stringOfCouponData = sp.getString("String of Coupon Data", "");
							}
							
							
							
						}
						
						
				        // Register a notification if the date isn't today or earlier, and if the Product doesn't begin with "USED"
						if (before != true && used == false){
							registerNotification(c.getDate(), c.getStore(), c.getProduct(), c.getAmount(), couponID);  // in EDIT COUPON use couponID, not newCouponId
							// CHANGE TO TEST DATE
							
							// Code to set up the second notification two days before the coupon expires
							//long twoDayNoteTimeMillis = c.getDate() - 10000;
							
							// CHANGE TO TEST DATE
							long twoDayNoteTimeMillis = c.getDate() - 172800000;
							
							long nowTimeMillis = calToday.getTimeInMillis();
							long secondRegistrationId = couponID + 100000;
							
							if (nowTimeMillis < twoDayNoteTimeMillis) {
								//registerNotification(c.getDate() - 10000, c.getStore(), c.getProduct(), c.getAmount(), secondRegistrationId);
								// CHANGE TO TEST DATE
								registerNotification(c.getDate() - 172800000, c.getStore(), c.getProduct(), c.getAmount(), secondRegistrationId);
							}
							
							// EDIT COUPON ONLY:  need to un-register the second registration if it's date is less than the current time
							if (twoDayNoteTimeMillis < nowTimeMillis) {
								//deleteNotification(c.getDate() - 10000, secondRegistrationId);
								deleteNotification(c.getDate() - 172800000, secondRegistrationId);
								SharedPreferences sp = getSharedPreferences("Active Coupon Data", MODE_PRIVATE);
					        	String stringOfCouponData = sp.getString("String of Coupon Data", "");
								deleteCouponDataInSharedPreferences(secondRegistrationId, stringOfCouponData);
								sp = getSharedPreferences("Active Coupon Data", MODE_PRIVATE);
					        	stringOfCouponData = sp.getString("String of Coupon Data", "");
							}
							
							String[][] spCouponArray = dboh.getSpCouponArray();
							
							// set the stringOfCouponData String to the first value, 
							// and then use a loop to concatenate the others with dividers
							// Example string for a coupon;
							// 9�Free�Garnett52�1403456367701�6/23/14�10�Free�Garnett52�1403456368413�6/23/14

							String stringOfCouponData = "";
							
							if (spCouponArray.length != 0) {
								int i = 0;
								int j = 0;
								boolean needSecond = false;		// used for checking whether a second notification is needed
								
								while (i < spCouponArray.length) {
									while (j < 5) {
										stringOfCouponData += spCouponArray[i][j] + "�";
										j += 1;
										
										// Check to see if the coupon's millisecond date is at least two days away
										if (j == 3){
											// if (Long.valueOf(spCouponArray[i][j]) > nowTimeMillis + 10000) {
											// CHANGE TO TEST DATE
											if (Long.valueOf(spCouponArray[i][j]) > nowTimeMillis + 172800000) {
												needSecond = true;
											}
										}
									}
									stringOfCouponData = stringOfCouponData.substring(0,stringOfCouponData.length() - 1) + "�";
									j = 0;
									
									if (needSecond == true){
										while (j < 5) {
											if (j != 0 && j != 3) {
												stringOfCouponData += spCouponArray[i][j] + "�";
											}
											
											else if (j == 0) {
												long couponId = Long.valueOf(spCouponArray[i][j]);
												long couponId2 = couponId + 100000;
												stringOfCouponData += couponId2 + "�";
											}
											
											else {
												long couponDate2 = Long.valueOf(spCouponArray[i][j]);
												//long firstAlarmDate = couponDate2 - 10000;
												// CHANGE TO TEST DATE
												long firstAlarmDate = couponDate2 - 172800000;
												stringOfCouponData += firstAlarmDate + "�";
											}
											j += 1;
										}
										stringOfCouponData = stringOfCouponData.substring(0,stringOfCouponData.length() - 1) + "�";
										j = 0;
									}
									
									i += 1;
									needSecond = false;
								}
								stringOfCouponData = stringOfCouponData.substring(0,stringOfCouponData.length() - 1); // get rid of last "�"  
							}
					
							//write stringOfCouponData to SharedPreferences so coupons' alarms can be re-created if the phone is turned off
							SharedPreferences sp = getSharedPreferences("Active Coupon Data", MODE_PRIVATE);
							SharedPreferences.Editor spe = sp.edit();
							spe.putString("String of Coupon Data", stringOfCouponData);
							spe.commit();
						}
						
						Toast.makeText(EditCoupon.this, "Coupon edited.", Toast.LENGTH_SHORT).show();
						
						db.close();
					}
					
					catch (ParseException exception) {
						Toast.makeText(EditCoupon.this, "Date or date format is invalid.\nPlease use the format m-d-yy.", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
		
		viewButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View vw)
			{
				dboh.close();
				Intent myIntent = new Intent(vw.getContext(), ViewEdit.class);
				startActivity(myIntent);
			}
		});
		
	}
	
	
	@Override
	protected void onPause(){
		super.onPause();
		
		// The onPause method is used to store current EditText values (the instance state) 
		// in case the Activity is interrupted.
		
		// instantiate EditTexts so their current values can be retrieved
		final EditText etStore = (EditText) findViewById(R.id.editText01);
		final EditText etAmount = (EditText) findViewById(R.id.editText02);
		final EditText etDate = (EditText) findViewById(R.id.editText03);
		final EditText etProduct = (EditText) findViewById(R.id.editText04);
		final EditText etNotes = (EditText) findViewById(R.id.editText07);
		
		// save values in the UI's EditText fields in case Activity is interrupted
		c.setStore(etStore.getText().toString());
		c.setAmount(etAmount.getText().toString());
		c.setProduct(etProduct.getText().toString());
		c.setNotes(etNotes.getText().toString());
		String stringDate = etDate.getText().toString();  // only the date isn't saved to the Coupon since that would require
		// converting it to the long equivalent.  Instead just keep it as text since it'll be written back as text in onResume.
		
		// save values in the UI's EditText fields in case Activity is interrupted
		writeInstanceState(this, c, stringDate);
	}
	
	
	@Override
	protected void onStop(){
		super.onStop();
		SQLiteDatabase db = dboh.getWritableDatabase();
		db.close();	
	}
	
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
	}
	
	
	// If the user navigates to the ViewEdit screen by using the back button, need to launch the event again so that the SQLite database
	// gets opened again (I think!)
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if( (keyCode==KeyEvent.KEYCODE_BACK) ) {
			Intent myIntent = new Intent(this, ViewEdit.class);
			startActivity(myIntent);
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
	
	
	
	// this method validates all dates until 2035
	public boolean validateDate(String dateString) {
		boolean valid = false;
		String pattern = ("(\\d{1,2})-(\\d{1,2})-(\\d{2}|20\\d{2})");
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(dateString);
		boolean matches = m.matches();
		
		if (matches){
			valid = true;
			long month = Long.valueOf(m.group(1));
			long day = Long.valueOf(m.group(2));
			long year = Long.valueOf(m.group(3));
			
			if ((year > 99 && year < 2013) || year > 2099 || year < 13) {
				valid = false;
			}
			
			else if (month > 12 || month < 1) {
				valid = false;
			}
			
			else if (day < 1 || day > 31) {
				valid = false;
			}
				
			else if (month==4||month==6||month==9||month==11){
				if (day > 30) {
					valid = false;
				}
			}
			
			else if (month==2) {
				if (year==2016||year==2020||year==2024||year==2028||year==2032) {
					if (day > 29) {
						valid = false;
					}
				}
				else {
					if (day > 28) {
						valid = false;
					}
				}
			}
		}
		else {
			valid = false;
		}
		return valid;
	}
	
	public boolean checkInvalidStrings(String str){
	    return !str.matches(".*(�|�).*");
	}
	
	private void registerNotification(long millisecondDate, String store, String product, String amount, long id) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        
        Bundle bundle = new Bundle();
        bundle.putString("STORE", store);
        bundle.putString("PRODUCT", product);
        bundle.putString("AMOUNT", amount);
        bundle.putLong("ID_CR", id);
        bundle.putLong("DATE", millisecondDate);
        intent.putExtras(bundle);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, (int) id, intent, 0);
        
//      alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 60 * 1000, alarmIntent);
        alarmManager.cancel(alarmIntent);
        alarmIntent.cancel();
        PendingIntent alarmIntent2 = PendingIntent.getBroadcast(this.getApplicationContext(), (int) id, intent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, millisecondDate, alarmIntent2);
    }
	
	private void deleteNotification(long millisecondDate, long id) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this.getApplicationContext(), (int) id, intent, 0);
        //alarmManager.set(AlarmManager.RTC_WAKEUP, millisecondDate, alarmIntent);
        alarmManager.cancel(alarmIntent);
        alarmIntent.cancel();
    }
	
	private void deleteCouponDataInSharedPreferences(long couponID, String stringOfData) {
		if (stringOfData.length() != 0) {
			String[] couponArray = stringOfData.split("�", -1);
			int couponArrayLength = couponArray.length;
			String stringOfDataOutput = "";
			
			boolean foundIt = false;
			int i = 0;
			
			// Set up the string that will identify the coupon to be deleted.
			String couponIDstring = String.valueOf(couponID) + "�";
			int couponIDlength = couponIDstring.length();
			
			// Set up the string that will identify the second alarm registration (if it exists) to be deleted.
			String couponID2string = String.valueOf(couponID + 100000) + "�";
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
					stringOfDataOutput += couponArray[i] + "�";
				}
				i += 1;	
			}
			i = 0;
			
			if (stringOfDataOutput.length() != 0) {
				stringOfDataOutput = stringOfDataOutput.substring(0,stringOfDataOutput.length() - 1); // strip the final "�"
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
	
	
	
	public boolean writeInstanceState(Context cx, Coupon c, String stringDate) {
		SharedPreferences sp =cx.getSharedPreferences("Instance State", MODE_PRIVATE);
		SharedPreferences.Editor e = sp.edit();
		e.putString("Store", c.getStore());
		e.putString("Amount", c.getAmount());
		e.putString("Date",  stringDate);
		e.putString("Product", c.getProduct());
		e.putString("Notes", c.getNotes());
	    
		return (e.commit());
	}
	
	
	public String readInstanceState(Context cx, Coupon c){
		
		// this method returns the values set for the Coupon in either onCreate or onPause.  
		// Only the date value isn't written to the Coupon, since it would have to be validated and translated to a
		// numeric before doing so.  That is unnecessary since that process would happen in onResume.  All we're doing
		// here is copying temporarily what the user had input into the EditTexts and populating them again.
		SharedPreferences sp =cx.getSharedPreferences("Instance State", MODE_PRIVATE);
		c.setStore(sp.getString("Store", ""));
		c.setAmount(sp.getString("Amount", ""));
		c.setProduct(sp.getString("Product", ""));
		c.setNotes(sp.getString("Notes", ""));
		String stringDate = sp.getString("Date", "");
		
		return stringDate;
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

	
}
