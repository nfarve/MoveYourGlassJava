package com.example.moveyourglass;

import com.example.moveyourglass.MoveService.MoveBinder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;




public class MainActivity extends Activity{


	 private final Handler mhandler = new Handler();
	 int PERIOD = 500; // read sensor data each 500 ms
	 boolean flag = false;
	 boolean isHandlerLive = false;
	 MoveService mService;
	 boolean mBound = false;
	 private boolean mOptionsMenuOpen;
	 private boolean mAttachedToWindow;
	 static boolean suggestionFlag = false;
	
	 TextView t;
	 public static boolean registeredFlag;
	 public static String id; 
	 public static boolean startFlag = false;
	 
	
    String regid;
    private static Context c;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("debug", "Main Activity onCreate called");
        Intent intent = new Intent(this, MoveService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	    if (android.os.Build.VERSION.SDK_INT > 9) {
	        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	        StrictMode.setThreadPolicy(policy);
	     }
        
	   }
    private ServiceConnection mConnection = new ServiceConnection() {
		
        @Override
        public void onServiceConnected(ComponentName className,IBinder service) {
        	if (service instanceof MoveService.MoveBinder){
        		MoveBinder binder = (MoveBinder) service;
        		mService = binder.getService();
        		//Log.i("mService", String.valueOf(mService));
        		openOptionsMenu();
        		Log.i("info", "Binding to Local Service");
        	}
            // We've bound to LocalService, cast the IBinder and get LocalService instance
        	unbindService(this);
        	//Log.i("info", "Bound to Local Service");
        }
       
	    @Override
	    public void onServiceDisconnected(ComponentName arg0) {
	    	//unbindService(this);
	        //mService = null;
	    }
};
//    private boolean checkRegistration(){
//    	boolean status= false;
//    	String holder = "";
//    	try {
//	  		  URL url = new URL("http://18.85.59.15:8000/register/"+id);
//	  		  HttpURLConnection con = (HttpURLConnection) url
//	  		    .openConnection();
//	  		 holder = readStream(con.getInputStream());
//	  		  } catch (Exception e) {
//	  		  e.printStackTrace();
//	  		}
//    	if (holder =="True"){
//    		status = true; 
//    	}
//    	setRegisteredFlag(status);
//    	return status;
//    }
//    
    
    
    public static void setRegisteredFlag(boolean status){
    	registeredFlag = status;
    }
       
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
//        	mAudioManager.playSoundEffect(Sounds.TAP);
//        	startMove();
//            //openOptionsMenu();
//            return true;
//        }
//        return false;
//  }
//
//    
//    private void startMove(){
//    	startActivity(new Intent (this, MoveActivity.class));
//    	finish();
//    }
    
   
	  @Override
	   protected void onStart() {
	      super.onStart();
	     if(suggestionFlag){
	    	 Log.i("info", "suggestionFlag");
	    	 startService(new Intent(MainActivity.this, MoveService.class));
	    	 suggestionFlag = false;
	    	 MoveService.suggestionFlag = true;
	     }
			
//	     if (!registered){
//	    	 
//		     while (!checkRegistration()){
//		    	 Log.i("registration", "waiting for user to register");
//		     }
//		    
//	     }
	      Log.d("debug", "Start Method Started");
	      // Bind to LocalService
	     
	  }
	  
	    
	  @Override
	  protected void onStop() {
	      super.onStop();
	        // Unbind from the service
//	      if (mBound) {
//	          unbindService(mConnection);
//	          mBound = false;
//	      }
	  }
	  
	  @Override 
	  protected void onPause(){
		  super.onPause();
		  Log.d("debug", "On Pause called in Main Activity");
	  }
	  
	  @Override 
	  protected void onResume(){
		  super.onResume();
//		  if (suggestionFlag){
//			  Log.d("debug", "restarting processSensors");
//			  startService(intent);
//			  mService = binder.getService();
//			  mService.handler.postAtTime(mService.processSensors, System.currentTimeMillis());
//			  mService.handler.postDelayed(mService.processSensors, PERIOD);
//  			  mService.cardLive();
//  			  suggestionFlag = false;
//		  }
		  
		  Log.d("debug", "On Resume called in Main Activity");
	  }
	  
	  @Override 
	  protected void onDestroy(){
		  super.onDestroy();
	  }
	
	  public static void changeSuggestionState(){
		  suggestionFlag = true;

		  //c.startActivity(new Intent(c, SuggestionActivity.class));
		  //switchActivities(c);
	  }
		  
	  @Override
	  public void onAttachedToWindow(){
		c=this.getApplicationContext();
	    super.onAttachedToWindow();
	    Log.d("debug", "onAttachedToWindow Called");
	    mAttachedToWindow=true;
	    openOptionsMenu();
	  }

	  @Override
	  public void onDetachedFromWindow(){
		  super.onDetachedFromWindow();
		  mAttachedToWindow=false;
	  }
	  
	  @Override
	  public void openOptionsMenu(){
		  if (!mOptionsMenuOpen && mAttachedToWindow && registeredFlag && mService!=null){
			  mOptionsMenuOpen= true;
			  super.openOptionsMenu();
		  }
	  }
	  
	  @Override
	  public void onOptionsMenuClosed(Menu menu){
		  mOptionsMenuOpen = false;
		  finish();
	  }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.move_start, menu);
        return true;
    }
    
    @Override 
    public boolean onOptionsItemSelected(MenuItem item){
    	switch (item.getItemId()){
    		case R.id.start:
    			//startFlag = true;
    			mService.handler=new Handler();
				mService.handler.postAtTime(mService.processSensors, System.currentTimeMillis());
				mService.handler.postDelayed(mService.processSensors, PERIOD);
    			mService.cardLive();
    			//Log.i("Menu", "start selected");
    			
    			return true;
    		case R.id.stop:
    			startFlag=false;
    			mService.cardDead();
    			mhandler.post(new Runnable(){
    				@Override 
    				public void run(){
    					stopService(new Intent(MainActivity.this, MoveService.class));
    				}
    			});
    			mService.handler.removeCallbacks(mService.processSensors);
    			
    			return true;
    		case R.id.stats:
    			mService.getStats();
    			return true;
    		default:
    			return false;//super.onOptionsItemSelected(item);
    	}
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
    	super.onPrepareOptionsMenu(menu);
    	Log.i("startFlag", String.valueOf(startFlag));
    	menu.findItem(R.id.start).setVisible(!startFlag);
    	menu.findItem(R.id.stop).setVisible(startFlag);
    	return true;
    }
	  private static void switchActivities(Context c){
    	Intent newintent = new Intent(c, SuggestionActivity.class);
    	newintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	c.startActivity(newintent);
    }
}



