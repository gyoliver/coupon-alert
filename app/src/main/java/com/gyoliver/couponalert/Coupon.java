package com.gyoliver.couponalert;

import java.text.NumberFormat;

public class Coupon {
	
	private long id;
	private String store, amount, product, notes;
	private long date;
	
	public Coupon (long id, long date, String store, String amount, String product, String notes) {
		
		this.id = id;
		this.date = date;
		this.store = store;
		this.amount = formatAmount(amount);
		this.product = product;
		this.notes = notes;
		
	}
	
	public Coupon (String store, String amount) {
		this.store = store;
		this.amount = formatAmount(amount);
		this.date = 0;
		this.product = "";
		this.notes = "";
		
	}
	
	public Coupon() {
		this.store = "";
		this.amount = "";
		this.date = 0;
		this.product = "";
		this.notes = "";
	}

	
	// Getters
	
	public long getId(){
		return this.id;
	}
	
	public long getDate(){
		return this.date;
	}
	
	public String getStore(){
		return this.store;
	}
	
	public String getAmount(){
		return this.amount;
	}
	
	public String getProduct(){
		return this.product;
	}
	
	public String getNotes(){
		return this.notes;
	}
	
	
	// Setters
	public void setId(long id){
		this.id = id;
	}
	
	public void setStore(String store){
		this.store = store;
	}
	
	public void setAmount(String amount){
		this.amount = formatAmount(amount);
	}
	
	public void setDate(long date){
		this.date = date + 18000000; // Add this value so that the coupon arrives at 5am, not midnight  CHANGE TO TEST DATE
	}
	
	public void setProduct(String product){
		this.product = product;
	}
	
	public void setNotes(String notes){
		this.notes= notes;
	}
	
	public String formatAmount(String amount) {
		
		String formattedAmount = "";
		// check if the first character is already $ and if the following value is numeric
        
        // add a $ if necessary
        // first deal with the case where "amount" is blank or is already a $
        if (amount.equals("") || amount.equals("$")) {
        	formattedAmount = amount;
        }
        
        
        // if "amount" isn't blank or $, then need to figure out if it's length 1, $+numeric, $+non-numeric, or just numeric
        else {
        	
        	// deal with length 1 strings
        	// deal with length 1 strings that aren't "$" only
        	boolean amountNumeric = isNumeric(amount);
        	
        	// if length 1 non-numeric, just add as is
        	if (amount.length()==1 && !amountNumeric) {
            	formattedAmount = amount;
            }
            
        	// deal strings that are either > length 1 or length 1 and numeric
        	else {
        		
        		// for strings > length 1, check for $
        		if (amount.length()!=1) {
		        	String firstChar = amount.substring(0,1);
		            String restOfValue = amount.substring(1);
		        	boolean restOfValueNumeric = isNumeric(restOfValue);
        		
		        	// if $+numeric, remove the $
		        	if (firstChar.equals("$") && restOfValueNumeric){
		        		amount = restOfValue;
		        	}
        		}
		        	
	        	// all other cases of non-numerics are not of form $+numeric, and we know they're non-numeric, just set
	        	// just changed amount, run isNumeric on amount again to reflect the change
        		amountNumeric = isNumeric(amount);
        		if (!amountNumeric){
	        		formattedAmount = amount;
	        	}
	        	
	        	// all other cases are numeric and can be dealt with with a NumberFormat CurrencyInstance
	        	else {
	        		NumberFormat fmt1 = NumberFormat.getCurrencyInstance();
	        		formattedAmount = fmt1.format(Double.parseDouble(amount));
	        	}
        	}
        }
        		
        return formattedAmount;
        
	}
        	
	public static boolean isNumeric(String str) {
		
		// this helper method is used in the method formatAmount to check if the string is numeric, and specifically matches a numeric
		// US currency amount
		return str.matches("-?\\d*(\\.\\d{1,2})?");  //match a number with optional '-' and decimal.
	}

}
