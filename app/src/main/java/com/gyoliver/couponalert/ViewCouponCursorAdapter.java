package com.gyoliver.couponalert;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ViewCouponCursorAdapter extends CursorAdapter {
    private LayoutInflater mLayoutInflater;
    int flags;
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ViewCouponCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context); 
    }
    
    @SuppressWarnings("deprecation")
	public ViewCouponCursorAdapter(Context context, Cursor c) {
        super(context, c);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context); 
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = mLayoutInflater.inflate(R.layout.listview_item, parent, false);
        return v;
    }

    
    @Override
    public void bindView(View v, Context context, Cursor c) {
        String store = c.getString(c.getColumnIndexOrThrow("STORE"));
        String amount = c.getString(c.getColumnIndexOrThrow("AMOUNT"));
        long millisecondDate = c.getLong(c.getColumnIndexOrThrow("DATE"));
        String product = c.getString(c.getColumnIndexOrThrow("PRODUCT"));
        String notes = c.getString(c.getColumnIndexOrThrow("NOTES"));
        
        TextView storeTV = (TextView) v.findViewById(R.id.store);
        TextView amountTV = (TextView) v.findViewById(R.id.amount);
        TextView dateTV = (TextView) v.findViewById(R.id.date);
        TextView productTV = (TextView) v.findViewById(R.id.product);
        TextView notesTV = (TextView) v.findViewById(R.id.notes);
        
        // check to see if the notes field begins with "USED".  If so, the user has already used the coupon.
        boolean used = false;
        if (product.length() >= 4) {
        	String productFirstFour = product.substring(0, 4);
        	if (productFirstFour.equals("USED")){
        		used = true;
        	}
        }
        
        // set the coupon color to gray if the date is before today or if the notes field begins with "USED"
        setItemColor(v, millisecondDate, used);
        
        Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millisecondDate);
		
		// CHANGE TO TEST DATE
		SimpleDateFormat sdf = new SimpleDateFormat("M-d-yy", Locale.US);
		String printDate = sdf.format(cal.getTime());
		
        storeTV.setText(store);
        amountTV.setText(amount);
        dateTV.setText(printDate);
        productTV.setText(product);
        notesTV.setText(notes);	
        
        if (notes.equals("")||notes.equals(null)||notes.equals(" ")){
        	notesTV.setVisibility(View.GONE);
        }
    }
    
    
    @SuppressLint("NewApi")
	private void setItemColor(View v, long millisecondDate, boolean used){
    	Calendar currentDate = Calendar.getInstance();
		currentDate.get(Calendar.MILLISECOND);
		
		Calendar couponDate = Calendar.getInstance();
		couponDate.setTimeInMillis(millisecondDate + 68400000); // 68400000 = 86400000 - 18000000: Adding 86400000 ensures that the
			// coupon shows up as active on the date it's expiring.  The 18000000 is to compensate for the fact that the coupons are
			// added with the date entered plus 6 hours so that the notifications appear at 6am.  
        
		
		int compare = couponDate.compareTo(currentDate);
		
	    if (compare < 0 || used == true){
			
	    	// Apparently, the 'deprecated' warning is just because the name of the method changed.  
	    	// The deprecated method will work fine in old versions of Android.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				v.setBackground(v.getResources().getDrawable(R.drawable.coupon_background_expired)) ;
			}
			else {
				v.setBackgroundDrawable(v.getResources().getDrawable(R.drawable.coupon_background_expired));
			}	
		}
	    
	    if (compare >= 0 && used != true) {
	    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				v.setBackground(v.getResources().getDrawable(R.drawable.coupon_background_current)) ;
			}
			else {
				v.setBackgroundDrawable(v.getResources().getDrawable(R.drawable.coupon_background_current));
			}
	    }	
    }
}
