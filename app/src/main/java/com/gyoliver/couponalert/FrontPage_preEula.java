package com.gyoliver.couponalert;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FrontPage_preEula extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_front_page);
		

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
