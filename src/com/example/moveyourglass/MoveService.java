package com.example.moveyourglass;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;
import com.example.moveyourglass.ProcessFile;
import com.google.android.glass.content.Intents;

 
public class MoveService extends Service implements SensorEventListener{
    private static final String TAG = "MoveService";
    private static final String LIVE_CARD_ID = "move";
    private static final String LIVE_CARD_RESET = "restart";
    private static final String LIVE_CARD_STAT = "stats";

    private LiveCard mLiveCard;
    private LiveCard mStatsCard;
    private static LiveCard mRegistrationCard;
    Handler handler;
    Handler handler2;
	int PERIOD = 100; // read sensor data each 100 ms
	int UPDATEPERIOD = 30; //how many minutes till update daily stats
	int PROCESSBUFFERSIZE = 1000/PERIOD*60*UPDATEPERIOD;//size of the buffer before processing suggestions
	int MAXWALKINGPOINTS = PROCESSBUFFERSIZE/30; //max number of walking points allowed in buffer to not issue flag
	boolean flag = false; //flag for update sensors 
	boolean isHandlerLive = false;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private final IBinder mBinder = new MoveBinder();
	private RemoteViews mRemoteView;
	private int sittingCount = 0; 
	private int walkingCount = 0; 
	private int runningCount = 0; 
	public boolean registered;
	public static final String PREFS_NAME = "MyPrefsFile";
	public static String id; 
	private ArrayList<Integer> actionList = new ArrayList<Integer>();
	private ArrayList<Integer> copyOfactionList = new ArrayList<Integer>();
	static String ip_address = "http://moveserver.media.mit.edu/";
	static boolean suggestionFlag = false;
	private Intent dialogIntent;
	private int MODE;
	private boolean collectData = false; 
	Timer removeFlagTimer;
	Timer checkRegTimer;
	Timer writeDataTimer;
	private String dataString = ""; 
	ProcessFile process;
	
	 private final BroadcastReceiver broadCastReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {

	            if (Intents.ACTION_ON_HEAD_STATE_CHANGED.equals(intent.getAction())) {
	                boolean onHead = intent.getBooleanExtra(Intents.EXTRA_IS_ON_HEAD,
	                        false);
	                if (onHead) {
	                	Log.i(TAG, "onhead Called");
	 
	                    if (!flag) {
	                       //re-enable sensors
	                        flag = true;
	                        registerSensor();
	                    }
	                } else {
	                    //disable sensor
	                	 flag = false;
	                	 unregisterSensor();
	                }
	            }
	        }
	    };
	
	
    @Override
    public void onCreate() {
        super.onCreate();
        process = new ProcessFile();
	    if (android.os.Build.VERSION.SDK_INT > 9) {
	        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	        StrictMode.setThreadPolicy(policy);
	      }
	    Log.d("debug", "onCreate method called");
	    
	    registerReceiver(broadCastReceiver, new IntentFilter(Intents.ACTION_ON_HEAD_STATE_CHANGED));
	   
	    
    }
    
    public final Runnable processSensors = new Runnable() {
		    @Override
		    public void run() {
		        // Do work with the sensor values.
		        flag = true;
		        // The Runnable is posted to run again here:
		        
		        handler.postDelayed(this, PERIOD);
		    }
		};
		
    public class MoveBinder extends Binder{
    	
    	MoveService getService(){
    		return MoveService.this;
    	}
    }
   
	 @Override
	  public final void onAccuracyChanged(Sensor sensor, int accuracy) {
  	    // Do something here if sensor accuracy changes.
  	  }
  	  
  	  
  	  
  	  public static class MovementParams{
  		  	String userid;
  		  	float x; 
  			float y; 
  			float z;
  			String url;
  			String flag;
  			
  			MovementParams(float x, float y, float z, String flag){
  				this.userid = id;
  				this.x = x; 
  				this.y= y; 
  				this.z= z;
  				this.url=ip_address+"process/";
  				this.flag = flag;
  			}
  			
  		}

  	@Override
  	  public final void onSensorChanged(SensorEvent event) {
  		//Log.d("debug",Boolean.toString(flag));
  		 if (flag) {
  	    // The light sensor returns a single value.
  	    // Many sensors return 3 values, one for each axis.
  			  float lux[] = event.values;
  			  if (collectData){
  				dataString = dataString + lux[0] + ',' + lux[1] + ',' + lux[2] + '\n';
//  				Log.i(TAG, "Sensor sting begun");
//  				Log.i(TAG, dataString);
  			  }
  			  else{
  				
    			String processResult  = process.processData(lux);
    			if (processResult == "walking"){
//    				Log.i("process", "walking");
    				addValuetoActionList(1);
    			}
    			else if(processResult == "sitting"){
//    				Log.i("process", "sitting");
    				addValuetoActionList(0);
    			}
  			  }
  			  
  			  //writeDataToFile(lux);
  			  //TextView t=(TextView)findViewById(R.id.text);
  			  //System.out.println(Arrays.toString(lux));
  			  //t.setText(Arrays.toString(lux));
  			  flag = false;
  		  }
  	  }
  	  
  	 
  	 
  	private void getID(){
	    String id_holder = "0";
	    String urlIs = ip_address + "id/";
		   try {
	  		  URL url = new URL(urlIs);
	  		  HttpURLConnection con = (HttpURLConnection) url
	  		    .openConnection();
	  		 id_holder = readStream(con.getInputStream());
	  		 Log.i("id", id_holder);
	  		  } catch (Exception e) {
	  		  e.printStackTrace();
	  		}
		 setId(id_holder);
		 Log.i("id", id_holder);
    }


    private String readStream(InputStream in) {
		  BufferedReader reader = null;
		  try {
		    reader = new BufferedReader(new InputStreamReader(in));
		    String line = "";
		    while ((line = reader.readLine()) != null) {
		      return (line);
		    }
		  } catch (IOException e) {
		    e.printStackTrace();
		  } finally {
		    if (reader != null) {
		      try {
		        reader.close();
		      } catch (IOException e) {
		        e.printStackTrace();
		        }
		    }
		  }
		 return "";
		} 

    
   public void setId(String id){
	   SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	   Editor editor = settings.edit();
	   editor.putString("user_id", id);
	   editor.commit();
	   MoveService.id=id;
   }
  
   public void setRegistration(boolean flag){
	   SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	   Editor editor = settings.edit();
	   editor.putBoolean("registerMode", flag);
	   editor.commit();
	   if (flag){
		   MainActivity.registeredFlag=true;
		   checkRegTimer.cancel();
		   Log.i("RegTimer", "Stopping Reg Timer");
	   }
	  
   }
 
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	if (collectData){
    		writeDataTimer = new Timer();
            writeDataTimer.schedule(new writeDataToFile(), 60*1000*5);
    	}
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
        id = settings.getString("user_id", "0");
        Log.i("Id", id);
        if (id == "0"){
        	Log.i("Id", "id is 0");
        }
        Log.i("Id", String.valueOf(id.length()));
        while (id.equals("0")){
        // First step if the id is 0 then it hasn't been set before and we need to get one from the server. 
        	getID();
        	id= settings.getString("user_id", "0");
        	Log.i("Id", id + "writing it for the second time inside loop");
        	Log.i("Id", "inside the loop!");
        }
        registerSensor();
       
 	    Log.d("Service", "onStartCommand Called");
        /**
         * For obvious reasons, you will probably only ever want ONE version of your
         * LiveCard running
         */
// 	    Log.d("debug", "SuggestionFlag: "+ String.valueOf(suggestionFlag));
// 	    Log.d("debug", "mLiveCard: "+ String.valueOf(mLiveCard));
	    if (suggestionFlag) {
	    	Log.i("info","Suggestion flag up");
	    	startCard();
        	suggestionFlag = false;
        	
	    }
 	 
 	    else{
 	    	clearCards();
 	    	mRegistrationCard = new LiveCard(this, LIVE_CARD_ID);
            Log.d(TAG, "Publishing Registration LiveCard");
            mRemoteView =new RemoteViews(this.getPackageName(), R.layout.card_registration);
            mRemoteView.setTextViewText(R.id.registration_number, id);
            mRegistrationCard.setViews(mRemoteView);
            Intent menuIntent = new Intent(this, MainActivity.class);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
           // Log.i("id",MainActivity.id);
            mRegistrationCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
            mRegistrationCard.attach(this);
            mRegistrationCard.publish(PublishMode.REVEAL);
            Log.d(TAG, "Done publishing Registration LiveCard ");
            checkRegTimer = new Timer();
            checkRegTimer.scheduleAtFixedRate(new checkRegistration(), 0, 10*1000);
            while (!MainActivity.registeredFlag){
            	
            }
            Log.i("info","device registered");
            startCard();
            if (collectData){
            	handler=new Handler();
    			handler.postAtTime(processSensors, System.currentTimeMillis());
    			handler.postDelayed(processSensors, PERIOD);
            }
 	    }
 
        return START_STICKY;
    }
    private void clearCards(){
    	if (mLiveCard != null){
    		if (mLiveCard.isPublished()){
    			mLiveCard.unpublish();
    		}
    		
    	}
    	if (mStatsCard != null){
    		if(mStatsCard.isPublished()){
    			mStatsCard.unpublish();
    		}
    
    	}
    	
    }
    
    private void clearAllButStart(){
    	if (mStatsCard != null){
    		if(mStatsCard.isPublished()){
    			mStatsCard.unpublish();
    		}
    
    	}
    	
    	if (mRegistrationCard != null){
    		if (mRegistrationCard.isPublished()){
    			mRegistrationCard.unpublish();
    		}
    	}
    }
    
    private void setMode(String mode){
       SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
  	   Editor editor = settings.edit();
  	   editor.putInt("mode", Integer.parseInt(mode));
  	   editor.commit();
  	   MODE = Integer.parseInt(mode);
  	   Log.i("info", "Mode = "+ mode);
    }
    
   class checkRegistration extends TimerTask{
	   public void run(){
    	boolean status= false;
    	String holder = "";
    	String urlIs = ip_address +"register/"+id;
    	//Log.i("info", "url is:"+urlIs);
    	try {
	  		  URL url = new URL(urlIs);
	  		  HttpURLConnection con = (HttpURLConnection) url
	  		    .openConnection();
	  		 holder = readStream(con.getInputStream());
	  		  } catch (Exception e) {
	  		  e.printStackTrace();
	  	}
    	String[] parts = holder.split(",");
    	if (parts.length>1){
	    	Log.i("info","response is: "+holder);
	    	if (parts[0].contains("True")){
	    		status = true; 
	    	}
	    	setMode(parts[1]);
	    	setRegistration(status);
    	}
    	Log.i("Waiting", "waiting...");
	   }
    }
    
    private String pingStatus(){
    	String holder = "";
    	//Log.i("info", "url is:"+urlIs);
    	try {
	  		  URL url = new URL(ip_address +"summary/"+id+ "/glass");
	  		  HttpURLConnection con = (HttpURLConnection) url
	  		    .openConnection();
	  		 con.setConnectTimeout(3000);
	  		 holder = readStream(con.getInputStream());
	  		  } catch (Exception e) {
	  		  e.printStackTrace();
	  		} 
    	Log.i("info","response is: "+holder);
    	
    	return holder;
    }
    
    class writeDataToFile extends TimerTask{
    	public void run(){
    		 handler.removeCallbacks(processSensors);
    		 unregisterSensor();
    		 File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    		 String name = "Walking_data.txt";
    		 File file = new File(directory, name);
    		 try {
    		      FileOutputStream outputStream = new FileOutputStream(file);
    		      outputStream.write(dataString.getBytes());
    		      outputStream.close(); 
    		      Log.i(TAG, "Wrote data");
    		      return;
    		    }catch (Exception e) {
    		      Log.e(TAG, "Could not save data: " + e);
    		      return;
    		    }
    		
    	}
    }
    
    public void startCard(){
    	if (mLiveCard != null){
    		if (mLiveCard.isPublished()){
    			mLiveCard.navigate();
    		}
    		else{
    			mLiveCard.publish(PublishMode.REVEAL);
    		}
    		
    	}
    	else{
	    	mLiveCard = new LiveCard(this, LIVE_CARD_ID);
	        Log.d(TAG, "Publishing LiveCard");
	        mLiveCard.setViews(new RemoteViews(this.getPackageName(), R.layout.card_move));
	        Intent menuIntent = new Intent(this, MainActivity.class);
	        menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
	        mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
	        mLiveCard.attach(this);
	        mLiveCard.publish(PublishMode.REVEAL);
	        Log.d(TAG, "Done publishing LiveCard ");
	        
    	}
    	clearAllButStart();
    }

    private void addValuetoActionList(int action){
    	int size = actionList.size();
    	if (suggestionFlag & MODE==2){
    		if (action ==1){
    			walkingCount+=1;
    		}
    		if (walkingCount>=25){
    			sendBroadcast(new Intent("xyz"));
    			
    		}
    	}
    	else if (!suggestionFlag){
	    	if (size <=PROCESSBUFFERSIZE){
	    		actionList.add(action);
	    	}
	    	else{
	    		actionList.remove(0);
	    		actionList.add(action);
	    		//TODO: Add a call to update daily state object
	    		int walkingOccurrences = Collections.frequency(actionList, 1);
	    		if (walkingOccurrences <MAXWALKINGPOINTS){
	    			cardGetUp();
	    		}
	    		else{
	    			actionList.clear();
	    		}
	    	}
    	}
    }
    public void cardLive(){
    	MainActivity.startFlag = true;
    	mRemoteView = new RemoteViews(getPackageName(), R.layout.card_live);
    	mLiveCard.setViews(mRemoteView);
    }
    
    public void cardDead(){
    	mRemoteView = new RemoteViews(getPackageName(), R.layout.card_move);
    	mRemoteView.setTextViewText(R.id.seconds_view,String.valueOf("Move Stopped"));
    	mLiveCard.setViews(mRemoteView);
    }
    
    private void cardGetUp(){
    	actionList.clear();
    	copyOfactionList.clear();
    	sittingCount=0; 
    	flag=false;
    	
    	if (MODE == 2){
    		dialogIntent = new Intent(getBaseContext(), SuggestionActivity.class);
        	dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	getApplication().startActivity(dialogIntent);
        	suggestionFlag=true;
        	MainActivity.changeSuggestionState();
    	}
    	
    	else if (MODE == 1){
    		MediaPlayer mediaPlayer = MediaPlayer.create(getBaseContext(), R.raw.sound);
        	mediaPlayer.start(); // no need to call prepare(); create() does that for you
        	suggestionFlag=true;
        	removeFlagTimer = new Timer();
        	removeFlagTimer.schedule(new RemoveFlag(), 60*1000);
    	}
    	
    	//mLiveCard.unpublish();
    	
    	//handler.removeCallbacks(processSensors);
    	
    	//stopSelf();
    }
    
    class RemoveFlag extends TimerTask{
    	public void run(){
    		Log.i("Timer", "Changed back to false!");
    		suggestionFlag= false;
    		removeFlagTimer.cancel();
    	}
    }
    
    public void getStats(){
    	String stats = pingStatus();
    	String[] parts = stats.split(",");
    	mStatsCard = new LiveCard(this, LIVE_CARD_STAT);
        Log.d(TAG, "Publishing Stats StatCard");
        int image;
        if ((Integer.parseInt(parts[1])+Integer.parseInt(parts[2]))>120){
        	image = R.drawable.average_stick2;
        }
        else if((Integer.parseInt(parts[1])+Integer.parseInt(parts[2]))>180){
        	 image = R.drawable.fit_stick;
        }
        else{
        	image = R.drawable.fat_stick2;
        }
        
        RemoteViews view2 = new CardBuilder(getBaseContext(), CardBuilder.Layout.COLUMNS)
        .setText("Time spent sitting: "+parts[0] + " minutes\nTime spent walking: "+parts[1] + " minutes\nTime spent running: "+parts[2] + " minutes")
        .setFootnote("Move Your Glass")
        .setTimestamp("just now")
        .setIcon(image)
        .getRemoteViews();
        /*mRemoteView =new RemoteViews(this.getPackageName(), R.layout.card_stats);
        mRemoteView.setTextViewText(R.id.sitting,"Time spent sitting: "+parts[0] + " minutes");
        mRemoteView.setTextViewText(R.id.walking,"Time spent walking: "+parts[1] + " minutes");
        mRemoteView.setTextViewText(R.id.running,"Time spent running: "+parts[2] + " minutes");*/
        mStatsCard.setViews(view2);
        Intent menuIntent = new Intent(this, MainActivity.class);
        menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
       // Log.i("id",MainActivity.id);
        mStatsCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
        mStatsCard.attach(this);
        mStatsCard.publish(PublishMode.SILENT);
        Log.d(TAG, "Done publishing Registration StatCard ");
        mLiveCard.unpublish();
    }
    
    private void registerSensor(){
    	mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 	    mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 	    mSensorManager.registerListener(this,mAccelerometer, 100000);
    }
    
    private void unregisterSensor(){
    	mSensorManager.unregisterListener(this);
    	mAccelerometer = null;
    }
    
    @Override
    public void onDestroy() {
        if (mLiveCard != null && mLiveCard.isPublished()) {
         
            Log.d(TAG, "Unpublishing LiveCard");
            mLiveCard.unpublish();
            mLiveCard = null;
        }
        if (mStatsCard != null && mStatsCard.isPublished()) {
            
            Log.d(TAG, "Unpublishing StatCard");
            mStatsCard.unpublish();
            mStatsCard = null;
        }
        flag = false;
        unregisterSensor();
        if (handler != null){
        	handler.removeCallbacks(processSensors);
        }
        if (handler2 !=null){
        	handler2.removeCallbacks(processSensors);
        }
      
        super.onDestroy();
        unregisterReceiver(broadCastReceiver);
    }
 
    @Override
    public IBinder onBind(Intent intent) {
    	
        return mBinder;
    }
}

/***
Trash code
 public final void writeDataToFile(float data[]){
  		  	//t=(TextView)findViewById(R.id.text);
  		  
  		    //String dataToWrite = Float.toString(data[0])+","+Float.toString(data[1])+","+Float.toString(data[2]);
  		  	String checkbox; 
  		  	if (suggestionFlag){
  		  		checkbox = "on";
  		  	}
  		  	else checkbox = "off";
  		    MovementParams params = new MovementParams(data[0], data[1], data[2], checkbox);
  		   // Log.i("Sending", "Sending sensor data");
  		    new HttpAsyncTask().execute(params);
  		    
  		    
  	  }
  	  
  	 
  	  public static String postData(float x, float y, float z, String url, String flag) {
  		    // Create a new HttpClient and Post Header
 
  		  	String result = "";
  		  	HttpClient httpclient = new DefaultHttpClient();
		    	HttpPost httpPost = new HttpPost(url);
		    	try{
		    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		    	nameValuePairs.add(new BasicNameValuePair("userid", id));
		        nameValuePairs.add(new BasicNameValuePair("x", Float.toString(x)));
		        nameValuePairs.add(new BasicNameValuePair("y", Float.toString(y)));
		        nameValuePairs.add(new BasicNameValuePair("z", Float.toString(z)));
		        nameValuePairs.add(new BasicNameValuePair("flag",flag));
		     
		        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		        
		         ResponseHandler<String> responseHandler=new BasicResponseHandler();
		         String responseBody = httpclient.execute(httpPost, responseHandler);
		         return responseBody;

  	  	} catch (ClientProtocolException e) {
  	        // TODO Auto-generated catch block
  	    } catch (IOException e) {
  	        // TODO Auto-generated catch block
  	    }
  		    return  result; 
  		}
  	  
  	  private class HttpAsyncTask extends AsyncTask<MovementParams, Void, String>{
  	  
  		  @Override
  		  protected String doInBackground(MovementParams... data) {
  			  float x = data[0].x;
  			  float y = data[0].y;
  			  float z = data[0].z;
  			  String url = data[0].url;
  			  String flag = data[0].flag;
  			  String userid = data[0].userid;
  			 
  			  return postData(x,y,z,url, flag);
  		  }	
  		  @Override
  		  protected void onPostExecute(String result){

  			  //Log.d("Response", "result");
  			 
  			  //Log.d("Response", result);
  			  if (result.contains("running")){
  				// t=(TextView)findViewById(R.id.text);
  				addValuetoActionList(2);
  		    	//runningCount = runningCount+1;
  		    	//t.setText("Running");
  			  }
  			  else if (result.contains("walking")){
   				 //t=(TextView)findViewById(R.id.text);
  				addValuetoActionList(1);
  				//walkingCount = walkingCount+1;
   		    	//t.setText("Walking");
   			  }
  			  else if (result.contains("sitting")){
   				 //t=(TextView)findViewById(R.id.text);
  				addValuetoActionList(0);
  				//sittingCount = sittingCount+1;
   		    	//t.setText("Sitting");
   			  }
  			  
  	
  		  }
  	  }
  	  
***/