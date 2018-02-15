package com.gyoliver.couponalert;

import java.io.File;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class FrontPage extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_front_page);
		
		SharedPreferences sp = getSharedPreferences("EULA Accepted", MODE_PRIVATE);
		String eulaAccepted = sp.getString("EULA Accepted", "false");
		
		// Only bring the EULA up the first time (i.e., when the SP string EULA Accepted is "false"
		if (eulaAccepted.equals("false")){
 	   
			// Create the Alert Dialog that will be used to show the EULA
		    AlertDialog.Builder builder = new AlertDialog.Builder(FrontPage.this);
		    builder.setMessage(R.string.eula_body);
		    
			// Add the buttons
			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	               // User clicked OK button
	        	   
	        	   // get the string of coupon IDs saved in SharedPrefs in case the phone is turned off and alarms cancelled
	        	   SharedPreferences sp = getSharedPreferences("EULA Accepted", MODE_PRIVATE);
	        	   SharedPreferences.Editor spe = sp.edit();
	        	   spe.putString("EULA Accepted", "true");
	   	 	       spe.commit();
	           }
		    });
			
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	               // User cancelled the dialog
	        	   Intent intent = new Intent(Intent.ACTION_MAIN);
	        	   intent.addCategory(Intent.CATEGORY_HOME);
	        	   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        	   startActivity(intent);
	        	   finish();
	           }
	        });
	
			AlertDialog alert = builder.create();
	        alert.show();
	        alert.getWindow().getAttributes();
	
	        TextView textView = (TextView) alert.findViewById(android.R.id.message);
	        textView.setTextSize(12);
	        Button btn1 = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
	        btn1.setTextSize(16);
	        Button btn2 = alert.getButton(DialogInterface.BUTTON_POSITIVE);
	        btn2.setTextSize(16);
		}
		
		boolean dbExists = doesDatabaseExist(this, "CouponDB");
		
		// if the database doesn't yet exist, add records
		DBOpenHelper dboh = new DBOpenHelper(this);
		
		if (dbExists == false){
			SQLiteDatabase db = dboh.getWritableDatabase(); 
			db.close();
		}
    	
		Button button1 = (Button) findViewById(R.id.button1);
		Button button2 = (Button) findViewById(R.id.button2);
		Button button3 = (Button) findViewById(R.id.button3);
		
		button1.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View vw){
				Intent myIntent = new Intent(vw.getContext(), NewCoupon.class);
				startActivity(myIntent);
			}
		});	
		
		button2.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View vw){
				Intent myIntent = new Intent(vw.getContext(), ViewEdit.class);
				startActivity(myIntent);
			}
		});
		
		button3.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View vw){
				Intent myIntent = new Intent(vw.getContext(), Help.class);
				startActivity(myIntent);
			}
		});
		
	}
	

	@Override
	protected void onPause(){
		super.onPause();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
	}
	
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	public boolean doesDatabaseExist(Context context, String dbName) {
	    File dbFile=context.getDatabasePath(dbName);
	    return dbFile.exists();
	}

}
