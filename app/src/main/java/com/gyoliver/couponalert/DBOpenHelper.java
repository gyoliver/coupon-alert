 package com.gyoliver.couponalert;

import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper {

	// Coupon table variables/constants
	private static final int DATABASE_VERSION = 2;
	private static final String DATABASE_NAME = "CouponDB";
	private static final String TABLE_NAME = "COUPON";
	private static final String ID = "_id";
	private static final String STORE = "STORE";
	private static final String AMOUNT = "AMOUNT";
	private static final String DATE = "DATE";
	private static final String PRODUCT = "PRODUCT";
	private static final String NOTES = "NOTES";
	private static final String TABLE_CREATE =
	            "CREATE TABLE " + TABLE_NAME + " (" +
	            ID + " INTEGER PRIMARY KEY, " +
	            STORE + " TEXT, " +
	            AMOUNT + " TEXT, " +
	            DATE + " INTEGER, " +
	            PRODUCT + " REAL, " +
	            NOTES + " TEXT, " +
	            " INTEGER);";
	
	
	public DBOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_CREATE);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        
        // Create tables again
        onCreate(db);
	}
	
	
	// Coupon table methods
	public long addCoupon(String store, String amount, long date, String product,
			String notes, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
        
		values.put(STORE, store);
		values.put(AMOUNT, amount);	// note that the amount should always be processed to be formatted before writing to the db
        values.put(DATE, date);
        values.put(PRODUCT , product);
        values.put(NOTES, notes);
        
        // Inserting Row
        long x = db.insert(TABLE_NAME, null, values);
	        
	    return x;
	}
	
	public int editCoupon(long id, String store, String amount, long date, String product,
			String notes, SQLiteDatabase db){
		
		String strFilter = "_id=" + id;
		ContentValues args = new ContentValues();
		args.put(STORE, store);
		args.put(AMOUNT,  amount);	// note that the amount should always be processed to be formatted before writing to the db
		args.put(DATE, date);
		args.put(PRODUCT, product);
		args.put(NOTES, notes);
		
		int x = db.update(TABLE_NAME, args, strFilter, null);
		
		return x;
	}
	
	public void deleteCoupon(long id, SQLiteDatabase db) {
		db.delete(TABLE_NAME, "_id = " + id, null);
	}
	
	public String getCoupon(int record_id, String columns, SQLiteDatabase db) {
		Cursor cursor = db.query(TABLE_NAME, new String[] { columns }, 
	    		ID + "=" + record_id, null, null, null, null);
	    if (cursor != null)
	        cursor.moveToFirst();
	    String response = cursor.getString(0);
	    return response;
	}
	
	public long getCount(){
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
 
        int count = cursor.getCount();
        cursor.close();
        
        return count;
	}
	
	// This method creates spCouponArray, which will be used to create the string value written to SharedPreferences
	// that concatenates all info
	// written to Shared Preferences, allowing coupon alarms to be re-registered after the phone is turned off
	public String[][] getSpCouponArray() {
		String[][] spCouponArray;
		SQLiteDatabase db = this.getReadableDatabase();
		
		Calendar currentDate = Calendar.getInstance();
		currentDate.get(Calendar.MILLISECOND);
		
		// retrieve only those coupons whose due dates are greater than or equal to the current date and whose "PRODUCT"
		// field values don't start with "USED"
		Cursor cursor = db.query(TABLE_NAME, new String[] {ID, STORE, AMOUNT, DATE, PRODUCT}, 
				DATE + ">=" + currentDate.getTimeInMillis() + " AND " + PRODUCT + " NOT LIKE 'USED%'" , null, null, null, null);
		if (cursor != null)
	        cursor.moveToFirst();
		// get the length of the cursor so can specify the length of the array when initializing
		// the first dimension of the cursor is coupons, the second is the various details of each coupon
		int ct = cursor.getCount();
	    spCouponArray = new String[ct][5];
	    int i = 0;
	    int j = 0;
	    if (cursor.moveToFirst()){
	    	do {
	    		while (j < 5) {
	    			
	    			// use a switch block because some data is stored as numeric, some as text, but they all need to be written as text
	    			switch (j) {
	    				case 0:
	    					String case0 = String.valueOf(cursor.getInt(j));
	    					spCouponArray[i][j] = case0;
	    					j += 1;
	    					break;
	    				case 3:
	    					String case3 = String.valueOf(cursor.getLong(j));
	    					spCouponArray[i][j] = case3;
	    					j += 1;
	    					break;
	    				default:
	    					spCouponArray[i][j] = cursor.getString(j);
	    					j += 1;
	    			}
	    		}
	    	i += 1;
	    	j = 0;
	    	} while (cursor.moveToNext());
	    }
	    return spCouponArray;
	}	
}
