package com.gyoliver.couponalert;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class Help extends TabActivity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_help);
            
            TabHost tabHost = getTabHost();
            
            // Tab for Photos
            TabSpec quickStart = tabHost.newTabSpec("Quick Start");
            // setting Title and Icon for the Tab
            quickStart.setIndicator("Quick Start", getResources().getDrawable(R.drawable.icon_tab_quickstart));
            Intent photosIntent = new Intent(this, HelpQuickStart.class);
            quickStart.setContent(photosIntent);
             
            // Tab for Songs
            TabSpec general = tabHost.newTabSpec("General");        
            general.setIndicator("General", getResources().getDrawable(R.drawable.icon_tab_general));
            Intent songsIntent = new Intent(this, HelpGeneral.class);
            general.setContent(songsIntent);
             
            // Tab for Videos
            TabSpec screenshots = tabHost.newTabSpec("Screenshots");
            screenshots.setIndicator("Screenshots", getResources().getDrawable(R.drawable.icon_tab_screenshots));
            Intent videosIntent = new Intent(this, HelpScreenshots.class);
            screenshots.setContent(videosIntent);
             
            // Adding all TabSpec to TabHost
            tabHost.addTab(quickStart); // Adding quick start tab
            tabHost.addTab(general); // Adding general tab
            tabHost.addTab(screenshots); // Adding screenshots tab
    }
	
	
	@Override
	protected void onPause(){
		super.onPause();
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
		    MenuItem item = menu.findItem(R.id.menu_settings);
	        item.setEnabled(false);
        return true;
	}
}
