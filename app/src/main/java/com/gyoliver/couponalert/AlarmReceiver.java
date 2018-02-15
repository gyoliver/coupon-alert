package com.gyoliver.couponalert;

import java.util.Calendar;
import java.util.Date;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context pContext, Intent pIntent) {
    	
    	String store = pIntent.getStringExtra("STORE");
        String product = pIntent.getStringExtra("PRODUCT");
        String amount = pIntent.getStringExtra("AMOUNT");
        long couponDate = pIntent.getLongExtra("DATE", 1000);
        long id = pIntent.getLongExtra("ID_CR",1000);
        
        String contentTitle = "";
        String contentText = "";
        
        Calendar calCoupon = Calendar.getInstance();
		calCoupon.setTimeInMillis(couponDate);
		
		// If amount is blank, we only want to display the product name.
		if (!amount.equals("")){
			amount = " (" + amount + ")";
		}
		contentText = product + amount;
		
        // Typical coupons have IDs set by the database in sequential order, will always be relatively small.
        if (id < 100000) {
        	contentTitle = "Last day: " + store + " coupon";
        }
        
        // Any notification with an ID over 1100000 will be the two-day notification for a coupon, and the notification
        // was not sent because the phone was off when the two-day notification date passed.
        
        else if (id > 1100000) {
        	contentTitle = "Alert 1 missed. Phone off.";
        	//contentText = "Check " + store + " coupon.";
        	contentText = store + ": " + product;
        	id = id - 1000000;  // adjust the id back to the actual coupon's id value so it can get deleted properly
        	// in stringOfCouponData and notifications will no longer be delivered.
        }
        
        // Any notification that is > 1000000 but <= 1100000 is a same-day notification missed because the phone was off
        
        else if (id > 1000000) {
        	contentTitle = "Final alert missed. Phone off.";
        	//contentText = "Check " + store + " coupon.";
        	contentText = store + ": " + product;
        	id = id - 1000000; // adjust the id back to the actual coupon's id value so it can get deleted properly
        	// in stringOfCouponData and notifications will no longer be delivered.
        }
        
        // All other notifications are the regular two-day notifications (100000 is added when
        // they're originally registered)
        else  if (id >= 100000 && id <= 1000000){
        	contentTitle = "2-day alert: " + store + " coupon";
        }
        
        Date nowTime = new Date(System.currentTimeMillis());
		long nowTimeMilli = nowTime.getTime();
        
        NotificationCompat.Builder notificationBuilder = 
        		new NotificationCompat.Builder(pContext)
        		.setSmallIcon(R.drawable.icon_notification)
        		.setContentTitle(contentTitle) 
        		.setContentText(contentText);
        Intent resultIntent = new Intent(pContext, ViewEdit.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(pContext);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(FrontPage.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
        notificationBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) pContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify((int) nowTimeMilli, notificationBuilder.build());
        
        // Rewrite the StringOfCouponData so that it removes the notification just sent.
        SharedPreferences sp = pContext.getSharedPreferences("Active Coupon Data", Context.MODE_PRIVATE);
        String stringOfCouponData = sp.getString("String of Coupon Data", "");
        deleteCouponDataInSharedPreferences(id, stringOfCouponData, pContext);
 	   
        
    }
    
    private void deleteCouponDataInSharedPreferences(long couponID, String stringOfData, Context pContext) {
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
				
			if (foundIt == false){
			}
			// update the value of String of Coupon Data in SharedPreferences
			SharedPreferences sp = pContext.getSharedPreferences("Active Coupon Data", Context.MODE_PRIVATE);
			SharedPreferences.Editor spe = sp.edit();
	 	    spe.putString("String of Coupon Data", stringOfDataOutput);
	 	    spe.commit();
			
		}
	}

}