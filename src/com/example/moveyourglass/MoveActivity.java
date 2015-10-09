package com.example.moveyourglass;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.example.moveyourglass.MoveService.MoveBinder;
import com.google.android.glass.app.Card;

public class MoveActivity extends Activity{
	private static Context c;
	MoveService mService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("info", "Move Activity Started");
        Card card = new Card(this);
        card.setText("Move is running. You can continue working on other things. ");
        card.setFootnote("Move Your Glass");
        setContentView(card.getView());
        Intent intent = new Intent(this, MoveService.class);
   	    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
   	    startService(intent);
   	    c=this.getApplicationContext();
    }
    private ServiceConnection mConnection = new ServiceConnection() {
		
        @Override
        public void onServiceConnected(ComponentName className,IBinder service) {
        	if (service instanceof MoveService.MoveBinder){
        		MoveBinder binder = (MoveBinder) service;
        		mService = binder.getService();
        	}
            // We've bound to LocalService, cast the IBinder and get LocalService instance
        	unbindService(this);
        }
       
	    @Override
	    public void onServiceDisconnected(ComponentName arg0) {
	    	//unbindService(this);
	        //mService = null;
	    }
};

    
	  @Override
	  protected void onStop() {
	      super.onStop();
	        // Unbind from the service
	     	  }
	  
	 
	  
	  @Override
	  protected void onDestroy(){
		  super.onDestroy();
		  stopService(new Intent(MoveActivity.this, MoveService.class));
		
	  }
	  
	  public static void changeSuggestionState(){
		  //suggestionFlag = true;
		  //c.startActivity(new Intent(c, SuggestionActivity.class));
		  switchActivities(c);
	  }
	  
	  private static void switchActivities(Context c){
	    	Intent newintent = new Intent(c, SuggestionActivity.class);
	    	newintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	c.startActivity(newintent);
	    }
	  
}