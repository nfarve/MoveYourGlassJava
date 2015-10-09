package com.example.moveyourglass;

import com.example.moveyourglass.MoveService.MoveBinder;
import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardBuilder;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;


public class SuggestionActivity extends Activity{
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	MediaPlayer mediaPlayer = MediaPlayer.create(getBaseContext(), R.raw.sound);
    	mediaPlayer.start(); // no need to call prepare(); create() does that for you
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                + WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                + WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        Log.i("Suggestion", "Suggestion Card Started");
        
        View view2 = new CardBuilder(getBaseContext(), CardBuilder.Layout.COLUMNS)
        .setText("Ready to get up and move?")
        .setFootnote("Move Your Glass")
        .setTimestamp("just now")
        .setIcon(R.drawable.fit_stick)
        .getView();
        
        
       
        //Intent intent = new Intent(this, MoveService.class);
	    //bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        setContentView(view2);
        registerReceiver(abcd, new IntentFilter("xyz"));
    }
    
    
	  @Override
	  protected void onStop() {
	      super.onStop();
	        // Unbind from the service
	      Log.d("Suggestion", "Suggestion Stopped");
	  }
	  
	  public boolean onKeyDown(int keycode, KeyEvent event) {
	        if (keycode == KeyEvent.KEYCODE_BACK) {
	            Log.d("info", "Swipe down");
	            //mService.onMoveStartCommand();
	            Intent intent = new Intent (this, MainActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        	startActivity(intent);
	        	unregisterReceiver(abcd);
	            finish();
	            return true;
	        }
	    
	        return false;
	    }
	  
	  
	  public void end(){
		  Intent intentb = new Intent (this, MainActivity.class);
          intentb.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      	  startActivity(intentb);
          finish(); 
		  
	  }
	  @Override
	  protected void onDestroy(){
		  super.onDestroy();
		  Log.d("Suggestion", "Suggestion on destroy called");
		
	  }
	  
	  private final BroadcastReceiver abcd = new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
        	  	end();
                unregisterReceiver(abcd);
          }
	  };
}