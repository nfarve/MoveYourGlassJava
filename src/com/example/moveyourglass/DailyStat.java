package com.example.moveyourglass;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public class DailyStat implements Serializable {
	private LocalDate date;

	private int walkingTotal = 0; //total time spent walking in seconds
	private int sittingTotal = 0; //total time spent to sitting in seconds
	//constructor
	public DailyStat(int a, int b) {
		this.date = LocalDate.now();
		this.walkingTotal = a; 
		this.sittingTotal = b; 
	}
	
	public void update(int a, int b){
		//update the walking and sitting totals
		walkingTotal = a; 
		sittingTotal = b; 
	}
	
	public void addObject(List<DailyStat> list){
		//add DailyStat object to list
		list.add(this);
	}
	
	public void removeObject(List<DailyStat> list){
		//remove DailyStat object from list
		list.remove(this);
	}
	
	public String getDate(){
		//get Date for this DailyStat object
		return this.date.toString();
	}
	
	public int getWalkingTotal (){
		//get walking total in seconds for this object
		return this.walkingTotal;
	}
	
	public int getSittingTotal (){
		//get sitting total in seconds for this object; 
		return this.sittingTotal;
	}
	
	public void updateTotals(int w, int s){
		//update the walking and sitting totals
		walkingTotal = w; 
		sittingTotal = s; 
	}
	
	public boolean compareDates(LocalDate now){
		//compare the date given date with the date that Daily Object was created
		return this.date.equals(now);
	}
}

