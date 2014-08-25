package at.lukasmayerhofer.consens;

import java.io.File;

import com.google.android.gms.location.LocationClient;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningServiceInfo;
//import android.content.ContentResolver;
//import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
//import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.Menu;
//import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
//import android.widget.TextView;
//import at.lukasmayerhofer.consens.contentprovider.ConsensContentProvider;
//import at.lukasmayerhofer.consens.database.ConsensDatabaseHelper;
import at.lukasmayerhofer.consens.services.ActivityRecognitionIntentService;
import at.lukasmayerhofer.consens.services.DatabaseBackupService;
import at.lukasmayerhofer.consens.services.LocationIntentService;
import at.lukasmayerhofer.consens.services.UserSessionService;
import at.lukasmayerhofer.consens.usage.SystemInfoData;
import at.lukasmayerhofer.consens.utils.GooglePlayServicesManager;
import at.lukasmayerhofer.consens.utils.HttpSend;


public class MainActivity extends Activity implements OnClickListener {

	private static final String TAG = MainActivity.class.getSimpleName();
	private static final String SYSTEM_PREFERENCES = "at.lukasmayerhofer.consens.SYSTEM_PREFERENCES";
	
	private GooglePlayServicesManager mGooglePlayServicesManager;
	private HttpSend sendDataToServer = null;
	
	private Intent locationUpdates;
	private Intent activityUpdates;
	private Intent userSessionUpdates;
	private Intent makeBackup;

	private EditText enterName;
	private Button startButton;
	private Button stopButton;
	private Button saveButton;
	
//	private ConsensDatabaseHelper mDatabaseHelper;
	
	boolean alreadyStarted;
	
	LocationClient mLocationClient = null;
	
	
	File originalDatabaseFile;
	File sdDatabaseFile;

	// database
	private PendingIntent makeBackupAlarm;
	private AlarmManager backupAlarmManager = null;
	
	
	// override methods
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		this.findViewById(R.id.textview_enter_name);
		enterName = (EditText) this.findViewById(R.id.edit_text_enter_name);
		enterName.requestFocus();

		startButton = (Button) this.findViewById(R.id.service_start);
		stopButton = (Button) this.findViewById(R.id.service_stop);
		saveButton = (Button) this.findViewById(R.id.db_save);
		
		startButton.setOnClickListener(this);
		stopButton.setOnClickListener(this);
		saveButton.setOnClickListener(this);
		
		// setting state (buttons, textfields, ...)
		setState(false, false);
		
		locationUpdates = new Intent(this, LocationIntentService.class);
		activityUpdates = new Intent(this, ActivityRecognitionIntentService.class);
		userSessionUpdates = new Intent(this, UserSessionService.class);
		mGooglePlayServicesManager = new GooglePlayServicesManager(this);
	}
	
	

//	@Override
//	protected void onRestoreInstanceState(Bundle savedInstanceState) {
//		super.onRestoreInstanceState(savedInstanceState);
//
//		if ((savedInstanceState != null) && (savedInstanceState.getSerializable("serviceState") != null)) {
//			Log.d(TAG, "saved instance available");
//			startButton.setEnabled(false);
//			stopButton.setEnabled(true);
//			enterName.setClickable(false);
//			enterName.setEnabled(false);
//		} else {
//			Log.d(TAG, "saved instance not available");
//			startButton.setEnabled(true);
//			stopButton.setEnabled(false);
//			enterName.setClickable(true);
//			enterName.setEnabled(true);
//		}
//	}
//
//	@Override
//	protected void onSaveInstanceState(Bundle outState) {
//		super.onSaveInstanceState(outState);
//				
//		if(isServiceRunning("at.lukasmayerhofer.consens.services.UserSessionService") || isServiceRunning("at.lukasmayerhofer.consens.services.ActivityRecognitionIntentService") || isServiceRunning("at.lukasmayerhofer.consens.services.LocationIntentService") || isServiceRunning("at.lukasmayerhofer.consens.services.DatabaseBackupService")) {
//			outState.putBoolean("serviceState", true);
//		} else {
//			outState.putBoolean("serviceState", false);
//		}
//	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.service_start:
				Log.d(TAG, "start logging data");
				
				if(isInputValid()) {					
					SharedPreferences systemPreferences = null;
					
					// set user_name
					systemPreferences = getSharedPreferences(SYSTEM_PREFERENCES, Context.MODE_PRIVATE);
					String userName = systemPreferences.getString("user_name", null);
					
					if (userName == null) {
			        	userName = enterName.getText().toString();
			        	
			        	systemPreferences = getSharedPreferences(SYSTEM_PREFERENCES, Context.MODE_PRIVATE);
			        	SharedPreferences.Editor mEditor = systemPreferences.edit();
			        	mEditor.putString("user_name", userName);
			        	mEditor.commit();
			        }
					
					// setting state (buttons, textfields, ...)
					setState(true, false);
					
					mGooglePlayServicesManager = new GooglePlayServicesManager(this);
					
					// start logging location
		        	startService(locationUpdates);
		        	mGooglePlayServicesManager.startLocationUpdates();
		        	
		        	// start logging activities
		        	startService(activityUpdates);
		        	mGooglePlayServicesManager.startActivityUpdates();
		            
		        	// start logging user session
		        	startService(userSessionUpdates);
		        	
		        	// start database backup service
		        	makeBackup = new Intent(this, at.lukasmayerhofer.consens.services.DatabaseBackupService.class);
		        	makeBackupAlarm = PendingIntent.getService(this, 0, makeBackup, 0);
		        	backupAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		        	backupAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, 1000*60*1, 1000*60*60*2, makeBackupAlarm);
//		        	backupAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, 1000*60*1, 1000*60*1, makeBackupAlarm);
				}
					
				break;
				
			case R.id.service_stop:
				Log.d(TAG, "stop logging data");
	        	
//				startButton.setEnabled(true);
//				stopButton.setEnabled(false);
//				
//				enterName.setClickable(false);
//				enterName.setEnabled(false);
				
				// setting state (buttons, textfields, ...)
				setState(false, true);
				
	        	// stop logging location
	        	mGooglePlayServicesManager.stopLocationUpdates();
	        	stopService(getIntent());
	        	
	        	// stop logging activities
	        	mGooglePlayServicesManager.stopActivityUpdates();
	        	stopService(getIntent());
	        	
	        	// stop logging user session
	        	stopService(userSessionUpdates);
	        	
	        	// clear alarm manager & stop database logger
	        	if(backupAlarmManager != null) {
	        		backupAlarmManager.cancel(makeBackupAlarm);
	        	}
	        	
	        	// clear HttpSend
	        	if(sendDataToServer != null) {
	        		Log.d(TAG, "sendDataToServer: " + sendDataToServer);
	        		sendDataToServer.unregister();
	        		sendDataToServer = null;
	        	}
				
				break;
			case R.id.db_save:
				Log.d(TAG, "save db to sd card");
				Log.d(TAG, SystemInfoData.getSystemInfoOsVersion());

				// copy database to sd card
	        	DatabaseBackupService.copyDatabaseToSdCard(getApplicationContext(), originalDatabaseFile, sdDatabaseFile, "consens.db");
	        	
	        	Log.d(TAG, "send database");
	        	
	        	sendDataToServer = new HttpSend(this);
	        	sendDataToServer.register();
	        	if(sendDataToServer.isConnected()) {
	        		new HttpSend(this).execute();
	        	}
	        	
	            break;
			default:
				break;
		}
	}
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {		
//		switch(item.getItemId()){
//	        case R.id.action_start:
//	        	Log.d(TAG, "start logging data");
//	        	
//	        	// start logging location
//	        	startService(locationUpdates);
//	        	mGooglePlayServicesManager.startLocationUpdates();
//	        	
//	        	// start logging activities
//	        	startService(activityUpdates);
//	        	mGooglePlayServicesManager.startActivityUpdates();
//	            
//	        	// start logging user session
//	        	startService(userSessionUpdates);
//	        	
//	        	// start database backup service
//	        	makeBackup = new Intent(this, at.lukasmayerhofer.consens.services.DatabaseBackupService.class);
//	        	makeBackupAlarm = PendingIntent.getService(this, 0, makeBackup, 0);
//	        	backupAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//	        	backupAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, 1000*60*60*1, 1000*60*60*1, makeBackupAlarm);
//	        	
//	        	return true;
//	        case R.id.action_stop:
//	        	Log.d(TAG, "stop logging data");
//	        	
//	        	// stop logging location
//	        	mGooglePlayServicesManager.stopLocationUpdates();
//	        	stopService(getIntent());
//	        	
//	        	// stop logging activities
//	        	mGooglePlayServicesManager.stopActivityUpdates();
//	        	stopService(getIntent());
//	        	
//	        	// stop logging user session
//	        	stopService(userSessionUpdates);
//	        	
//	        	// clear alarm manager
//	        	if(backupAlarmManager != null) {
//	        		backupAlarmManager.cancel(makeBackupAlarm);
//	        	}
//	        	
//	        	// clear HttpSend
//	        	if(sendDataToServer != null) {
//	        		Log.d(TAG, "sendDataToServer: " + sendDataToServer);
//	        		sendDataToServer.unregister();
//	        		sendDataToServer = null;
//	        	}
//	        
//	            return true;
//	        case R.id.action_save:
//	        	Log.d(TAG, "save database");
	        	
	        	// copy database to sd card
//	        	DatabaseBackupService.copyDatabaseToSdCard(getApplicationContext(), originalDatabaseFile, sdDatabaseFile, "consens.db");
	        	
	        	// delete tables from original database file in "data/data..."
//	        	mDatabaseHelper = new ConsensDatabaseHelper(this);
//	        	SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
	        	
//	        	database.delete(ActivityModel.TABLE_ACTIVITY, null, null);
//	        	database.delete(AppSessionModel.TABLE_APP_SESSION, null, null);
//	        	database.delete(AppUsageModel.TABLE_APP_USAGE, null, null);
//	        	database.delete(LocationModel.TABLE_LOCATION, null, null);
	        	
//	            return true;
////	        case R.id.action_clear:
////	        	// set all items to server_sent = 0
////	    		ContentResolver clearResolver = getContentResolver();
////	    		ContentValues clearValues = new ContentValues();
////	    		clearValues.put("server_sent", false);
////	    		
////	    		clearResolver.update(ConsensContentProvider.APP_SESSION_CONTENT_URI, clearValues, null, null);
////	    		clearResolver.update(ConsensContentProvider.APP_USAGE_CONTENT_URI, clearValues, null, null);
////	    		clearResolver.update(ConsensContentProvider.SYSTEM_SETTINGS_CONTENT_URI, clearValues, null, null);
////	    		clearResolver.update(ConsensContentProvider.ACTIVITIES_CONTENT_URI, clearValues, null, null);
////	    		clearResolver.update(ConsensContentProvider.LOCATIONS_CONTENT_URI, clearValues, null, null);
////	        	
////	        	return true;
//	        case R.id.action_send:
//	        	Log.d(TAG, "send database");
//	        	
//	        	sendDataToServer = new HttpSend(this);
//	        	sendDataToServer.register();
//	        	if(sendDataToServer.isConnected()) {
//	        		new HttpSend(this).execute();
//	        	}
//	        	
//	        	return true;
////	        case R.id.action_check:
////	        	Log.d(TAG, "check if everything is ok");
////	        	
////	        	if(isServiceRunning("at.lukasmayerhofer.consens.services.UserSessionService")) {
////	        		Toast.makeText(this, "ConSens: Everything is OK!", Toast.LENGTH_LONG).show();
////	        	} else {
////	        		Toast.makeText(this, "ConSens: User Session Service NOT RUNNING!", Toast.LENGTH_LONG).show();
////	        	}
//	        	
//	        default:
//	            return super.onOptionsItemSelected(item);
//	    }
//	}
	
	private boolean isInputValid() {
		if(enterName.getText().toString().trim().equals("")) {
			return false;
		} else {
        	return true;
        }
	}
	
	private boolean isServiceRunning(String serviceName) {		
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    
		for (RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceName.equals(service.service.getClassName())) {
				return true;
		    }
		}
	    return false;
	}
	
	private void setState(boolean start, boolean stop) {
		SharedPreferences systemPreferences = null;
		
		// set already_started to indicate if services are running
		systemPreferences = getSharedPreferences(SYSTEM_PREFERENCES, Context.MODE_PRIVATE);
		alreadyStarted = systemPreferences.getBoolean("already_started", false);
		
		Log.d(TAG, "alreadyStarted: " + alreadyStarted);
		
		if (!alreadyStarted) { 
			if(start || isServiceRunning("at.lukasmayerhofer.consens.services.UserSessionService") || isServiceRunning("at.lukasmayerhofer.consens.services.ActivityRecognitionIntentService") || isServiceRunning("at.lukasmayerhofer.consens.services.LocationIntentService") || isServiceRunning("at.lukasmayerhofer.consens.services.DatabaseBackupService")) {
				Log.d(TAG, "Service(s) running");
	        	SharedPreferences.Editor mEditor = systemPreferences.edit();
	        	mEditor.putBoolean("already_started", true);
	        	mEditor.commit();
	        	alreadyStarted = systemPreferences.getBoolean("already_started", false);
			}
        }
		
//		if ((savedInstanceState != null) && (savedInstanceState.getSerializable("serviceState") != null)) {
		if(alreadyStarted && !stop) {
			Log.d(TAG, "saved state available");
			startButton.setEnabled(false);
			stopButton.setEnabled(true);
			
			enterName.setClickable(false);
			enterName.setEnabled(false);
			
			String userName = systemPreferences.getString("user_name", "Max Mustermann");
			enterName.setText(userName);
		} else {
			Log.d(TAG, "saved state not available");
			startButton.setEnabled(true);
			stopButton.setEnabled(false);
		}
	}

}
