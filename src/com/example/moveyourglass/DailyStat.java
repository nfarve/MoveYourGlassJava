package com.example.moveyourglass;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;


public class DailyStat implements Serializable{
	private Calendar date;
	DateFormat dateformat = new SimpleDateFormat("MM/dd/yyyy");
	private int walkingTotal = 0;
	private int sittingTotal = 0;
	private int numberSuggestions; 
	boolean sent; 
	//constructor
	public DailyStat(int a, int b) {
		this.date = date.getInstance();
		this.walkingTotal = a; 
		this.sittingTotal = b; 
		this.numberSuggestions = 0;
		this.sent = false;
	}
	
	public void update(int a, int b){
		walkingTotal = a; 
		sittingTotal = b; 
	}
	
	public void updateSuggestions(int sugg){
		this.numberSuggestions = sugg;
	}
	
	public void updateSent(boolean Sent){
		this.sent = Sent;
	}
	
	public void addObject(List<DailyStat> list){
		list.add(this);
	}
	
	public void removeObject(List<DailyStat> list){
		list.remove(this);
	}
	
	public String getDate(){
		return dateformat.format(this.date.getTime());
	}
	
	
	public int getWalkingTotal (){
		return this.walkingTotal;
	}
	
	public int getSittingTotal (){
		return this.sittingTotal;
	}
	
	public int getSuggestions(){
		return this.numberSuggestions;
	}
	
	public boolean getSent(){
		return this.sent;
	}
	
	public void updateTotals(int w, int s){
		walkingTotal = w; 
		sittingTotal = s; 
	}
	
	public boolean compareDates(Calendar now){
		return (dateformat.format(date.getTime()).equals(dateformat.format(now.getTime())));
	}
	   
}

