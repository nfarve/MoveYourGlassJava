package com.example.moveyourglass;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class DailyStat implements Serializable {

	  private Date date;
	    private int walkingTotal = 0;
	    private int sittingTotal = 0;
	    //constructor
	    public DailyStat(int a, int b) {
	    	this.date = new Date();
	    	this.walkingTotal = a; 
	    	this.sittingTotal = b; 
	    }
	    
	    public void update(int a, int b){
	    	walkingTotal = a; 
	    	sittingTotal = b; 
	    }
	    
	    public void addObject(List<DailyStat> list){
	    	list.add(this);
	    }
	    
	    public void removeObject(List<DailyStat> list){
	    	list.remove(this);
	    }
	    
	    public String getDate(){
	    	return this.date.toString();
	    }
	    
	    public int getWalkingTotal (){
	    	return this.walkingTotal;
	    }
		    
	    public int getSittingTotal (){
	    	return this.sittingTotal;
	    }
		   
}

