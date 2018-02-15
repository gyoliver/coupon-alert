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
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NewCoupon extends Activity {
	
	public static Context newCouponContext;  //  add this in order to enable getting the context in BootReceiver
	DBOpenHelper dboh = new DBOpenHelper(this);
	
	// instantiate a coupon to save coupon values to
	final Coupon c = new Coupon(); 
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_coupon);
		
		// If NewCoupon is just getting created, there is no need to read any values from SharedPreferences into the
		// EditTexts.  To prevent any values from getting written to the EditTexts, write the Coupon values (all empty
		// strings except the date, which is 0) to SharedPreferences so the EditTexts will start with these blank values.
		// The date is just added as an empty string.
		
		writeInstanceState(this, c, "");
	}
	
	@Override
	protected void onStart(){
		super.onStart();
	}
	
	
	@Override
	protected void onResume(){
		super.onResume();
		
		// Read the current Coupon (c) values so they can be displayed in the EditTexts.  If this Activity has just
		// gotten created, then the values will have been set to empty values in onCreate().  If the Activity had been paused,
		// the last values in the EditTexts will have been saved to SharedPrefences in onPause() .  
		String stringDate = readInstanceState(this, c);
		
		// get a writable database to which user-inputted coupon data will be saved
		final SQLiteDatabase db = dboh.getWritableDatabase();
		
		// instantiate all EditTexts
		final EditText etStore = (EditText) findViewById(R.id.editText01);
		final EditText etAmount = (EditText) findViewById(R.id.editText02);
		final EditText etDate = (EditText) findViewById(R.id.editText03);
		final EditText etProduct = (EditText) findViewById(R.id.editText04);
		final EditText etNotes = (EditText) findViewById(R.id.editText07);
		
		// Set EditText values to values saved in Coupon.  If the Activity has *just* gone through onCreate, then the values
		// will have been set to blank values.  If the Activity had been paused or stopped, then the last EditText values
		// would've been written in onPause.
		// set EditTexts' text to the correct values.
		etStore.setText(c.getStore());
		etAmount.setText(c.getAmount());
		etProduct.setText(c.getProduct());
		etNotes.setText(c.getNotes());
		etDate.setText(stringDate);
		
		// instantiate buttons
		final Button addButton = (Button) findViewById(R.id.button1);
		final Button viewButton = (Button) findViewById(R.id.button2);
		
		// The below code implements the OnClickListener for the "add" button, which adds a new coupon to the database,
        // as well as registering the notifications for the coupons expiration date.
        
		addButton.setOnClickListener(new View.OnClickListener() {
		
			@Override
			public void onClick(View vw)
			{
				// Copy the data inputted by the user into the Coupon  object
				c.setStore(etStore.getText().toString());
				c.setAmount(etAmount.getText().toString());
				c.setProduct(etProduct.getText().toString());
				c.setNotes(etNotes.getText().toString());
				
				// get date from EditText and convert to millisecond format, then store to Coupon objects
				String stringDate = etDate.getText().toString();
				
				// Validate that the entered data is in a format that can be parsed
				boolean valid;
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
					Toast.makeText(NewCoupon.this, "The characters ± and ® cannot be used.", Toast.LENGTH_LONG).show();
					if (valid == false){
						Toast.makeText(NewCoupon.this, "Date or date format is invalid.\nPlease use the format m-d-yy.", Toast.LENGTH_SHORT).show();	
					}
				}
				
				else if (valid == false){
					Toast.makeText(NewCoupon.this, "Date or date format is invalid.\nPlease use the format m-d-yy.", Toast.LENGTH_SHORT).show();	
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
						
						if (before == true){
							Toast.makeText(NewCoupon.this, "Coupon date entered is today or earlier.\nTo change the date,"  + 
									" click 'View Coupons'\nand click the coupon to edit it.", Toast.LENGTH_LONG).show();
						}
						
						// Copy the data saved to the Coupon object into a new coupon record in the database.
						long newCouponId = dboh.addCoupon(c.getStore(), c.getAmount(), c.getDate(), c.getProduct(), c.getNotes(), db);
						
						// CHANGE TO TEST DATE
						/*if (dtToday == dtCoupon){
							before = false;
						}*/
						
						// Check if the Product text begins with "USED"
						String product = c.getProduct();
						
						boolean used = false;
				        if (product.length() >= 4) {
				        	String productFirstFour = product.substring(0, 4);
				        	if (productFirstFour.equals("USED")){
				        		used = true;
				        	}
				        }
				        
				        // Register a notification if the date isn't today or earlier, and if the Product doesn't begin with "USED"
						if (before != true && used == false){
							registerNotification(c.getDate(), c.getStore(), c.getProduct(), c.getAmount(), newCouponId);
							
							// Code to set up the second notification two seconds before the coupon expires
							//long twoDayNoteTimeMillis = c.getDate() - 10000;
							
							// CHANGE TO TEST DATE
							long twoDayNoteTimeMillis = c.getDate() - 172800000;
							
							long nowTimeMillis = calToday.getTimeInMillis();
							long secondRegistrationId = newCouponId + 100000;
							
							if (nowTimeMillis < twoDayNoteTimeMillis) {
								//registerNotification(c.getDate() - 10000, c.getStore(), c.getProduct(), c.getAmount(), secondRegistrationId);
								// CHANGE TO TEST DATE
								registerNotification(c.getDate() - 172800000, c.getStore(), c.getProduct(), c.getAmount(), secondRegistrationId);
							}
							
							String[][] spCouponArray = dboh.getSpCouponArray();
							
							// set the stringOfCouponData String to the first value, 
							// and then use a loop to concatenate the others with dividers
							// Example string for a coupon;
							// 9±Free±Garnett52±1403456367701±6/23/14®10±Free±Garnett52±1403456368413±6/23/14

							String stringOfCouponData = "";
							
							if (spCouponArray.length != 0) {
								int i = 0;
								int j = 0;
								boolean needSecond = false;		// used for checking whether a second notification is needed
								
								while (i < spCouponArray.length) {
									while (j < 5) {
										stringOfCouponData += spCouponArray[i][j] + "±";
										j += 1;
										
										// Check to see if the coupon's millisecond date is at least two days away
										if (j == 3){
											if (Long.valueOf(spCouponArray[i][j]) > nowTimeMillis + 172800000) {
											// CHANGE TO TEST DATE
											// if (Long.valueOf(spCouponArray[i][j]) > nowTimeMillis + 2000);
												needSecond = true;
											}
										}
									}
									stringOfCouponData = stringOfCouponData.substring(0,stringOfCouponData.length() - 1) + "®";
									j = 0;
									
									if (needSecond == true){
										while (j < 5) {
											if (j != 0 && j != 3) {
												stringOfCouponData += spCouponArray[i][j] + "±";
											}
											
											else if (j == 0) {
												long couponId = Long.valueOf(spCouponArray[i][j]);
												long couponId2 = couponId + 100000;
												stringOfCouponData += couponId2 + "±";
											}
											
											else {
												long couponDate2 = Long.valueOf(spCouponArray[i][j]);
												long firstAlarmDate = couponDate2 - 172800000;
												// CHANGE TO TEST DATE
												// long firstAlarmDate = couponDate2 - 2000;
												stringOfCouponData += firstAlarmDate + "±";
											}
											j += 1;
										}
										stringOfCouponData = stringOfCouponData.substring(0,stringOfCouponData.length() - 1) + "®";
										j = 0;
									}
									
									i += 1;
									needSecond = false;
								}
								stringOfCouponData = stringOfCouponData.substring(0,stringOfCouponData.length() - 1); // get rid of last "®"
							}
							
							//write stringOfCouponData to SharedPreferences so coupons' alarms can be re-created if the phone is turned off
							SharedPreferences sp = getSharedPreferences("Active Coupon Data", MODE_PRIVATE);
							SharedPreferences.Editor spe = sp.edit();
							spe.putString("String of Coupon Data", stringOfCouponData);
							spe.commit();
						}
						
						Toast.makeText(NewCoupon.this, "New coupon added.", Toast.LENGTH_SHORT).show();	

						// Reset edit texts so another coupon can be added.  This also prevents a user from accidentally adding a
						// duplicate coupon, since an empty date field will cause the program to reject the addition of the coupon.
						etStore.setText("");
						etAmount.setText("");
						etProduct.setText("");
						etNotes.setText("");
						etDate.setText("");
						
						// 
						
					}
					
					catch (ParseException exception) {
						Toast.makeText(NewCoupon.this, "Date or date format is invalid.\nPlease use the format m-d-yy.", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
		
		

		viewButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View vw)
			{
				db.close();
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

	
	protected void onStop(){
		super.onStop();
		SQLiteDatabase db = dboh.getWritableDatabase();
		db.close();
	}
	
	
	public static long daysBetween(Calendar startDate, Calendar endDate) {  
		  
		Calendar date = (Calendar) startDate.clone();  
		long daysBetween = 0;  
		while (date.before(endDate)) {  
			date.add(Calendar.DAY_OF_MONTH, 1);  
			daysBetween++;  
		} 
		
		return daysBetween;  
	}
	
	
	// this method validates all dates until 2035
	public boolean validateDate(String dateString) {
		
		boolean valid;
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
		boolean xx = !str.matches(".*(±|®).*");
		return xx;   
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
	
	
	private void registerNotification(long millisecondDate, String store, String product, String amount, long id) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);

        Bundle bundle = new Bundle();
        bundle.putString("STORE", store);
        bundle.putString("PRODUCT", product);
        bundle.putString("AMOUNT", amount);
        bundle.putLong("DATE", millisecondDate);
        bundle.putLong("ID_CR", id);
        intent.putExtras(bundle);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this.getApplicationContext(), (int) id, intent, 0);
        
//      alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 60 * 1000, alarmIntent);
        alarmManager.set(AlarmManager.RTC_WAKEUP, millisecondDate, alarmIntent);
        
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
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		    MenuItem item = menu.findItem(R.id.menu_new_coupon);
	        item.setEnabled(false);
        return true;
	}

}
