package com.gyoliver.couponalert;

import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class BootReceiver extends BroadcastReceiver {
	
    @Override
    public void onReceive(Context pContext, Intent pIntent) {
    	
    	SharedPreferences sp1 = pContext.getSharedPreferences("Active Coupon Data", Context.MODE_PRIVATE);
    	String stringOfCouponData = sp1.getString("String of Coupon Data", "");
    	
    	int countCoupons = 0;
    	int lenStringOfCouponData = stringOfCouponData.length();
    	// If stringOfCouponData has data in it, then parse it and register notifications accordingly
    	if (lenStringOfCouponData > 0) {
    		countCoupons = (stringOfCouponData.length() - stringOfCouponData.replace("®", "").length()) + 1;
	    	
    		countCoupons = (stringOfCouponData.length() - stringOfCouponData.replace("®", "").length()) + 1;
    		String[] couponArray = stringOfCouponData.split("®", -1);
			
			String[] couponSubArray;
			int i2 = 0;
			while (i2 < countCoupons) {
				couponSubArray = couponArray[i2].split("±", -1);
				long id = Long.valueOf(couponSubArray[ 0]);
				String store = couponSubArray[1];
				String amount = couponSubArray[2];
				long millisecondDate = Long.valueOf(couponSubArray[3]);
				String product = couponSubArray[4];
				
				Date nowTime = new Date(System.currentTimeMillis());
				long nowTimeMilli = nowTime.getTime();
				
				// if the millisecondDate is before the current time, the notification should've been sent
				// while the phone was off.  In such cases the user should be notified that their phone was off.
				// AlarmReceiver differentiates these coupons by the id value registered to them.
				if (millisecondDate < nowTimeMilli) {
					id += 1000000;
					registerNotification(millisecondDate, store, product, amount, id, millisecondDate, pContext);
					// set both the notification time and the coupon expiration date to millisecondDate.
					// The second millisecondDate won't get used for these coupons.
				}
				
				else if (millisecondDate > nowTimeMilli) {
					registerNotification(millisecondDate, store, product, amount, id, millisecondDate, pContext);
					//  For those coupons whose dates are still in the future, they just need to be re-registered to the
					//  dates saved in their coupons.
				}
				
				i2 += 1;
			}
    	}
    }
    
    private void registerNotification(long millisecondDate, String store, String product, String amount, long id, long couponDate, Context pContext) {
        AlarmManager alarmManager = (AlarmManager) pContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(pContext, AlarmReceiver.class);
        
        // Note that this registerNotification method is different from the ones in NewCoupon and EditCoupon.  Here the coupon date is also written as a bundle
        // extra since it's used for notifications coming from BootReceiver  . 

        Bundle bundle = new Bundle();
        bundle.putString("STORE", store);
        bundle.putString("PRODUCT", product);
        bundle.putString("AMOUNT", amount);
        bundle.putLong("DATE", couponDate);
        bundle.putLong("ID_CR", id);
        intent.putExtras(bundle);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(pContext, (int) id, intent, 0);
        
//      alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 60 * 1000, alarmIntent);
        alarmManager.set(AlarmManager.RTC_WAKEUP, millisecondDate, alarmIntent);
	}
    
}