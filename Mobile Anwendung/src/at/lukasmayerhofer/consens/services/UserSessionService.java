package at.lukasmayerhofer.consens.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import at.lukasmayerhofer.consens.usage.AppSessionData;
import at.lukasmayerhofer.consens.usage.AppUsageData;
import at.lukasmayerhofer.consens.usage.SystemSettingsData;


public class UserSessionService extends Service {

	private static final String TAG = UserSessionService.class.getSimpleName();
	
	private IntentFilter userPresent;
	private BroadcastReceiver userSessionReceiver;
	
	// data handlers
	private SystemSettingsData mSystemSettings;
	private AppSessionData mAppSession;
	private AppUsageData mAppUsage;
	
	private boolean appSessionStarted = false;
	
	
	// override methods
	@Override
	public void onCreate() {
		super.onCreate();
		
		// create intents and start receiver to listen for screen and user status
		userPresent = new IntentFilter(Intent.ACTION_SCREEN_ON);
		userPresent.addAction(Intent.ACTION_SCREEN_OFF);
		userPresent.addAction(Intent.ACTION_USER_PRESENT);
		
		userSessionReceiver = new at.lukasmayerhofer.consens.receiver.UserSessionReceiver();
		registerReceiver(userSessionReceiver, userPresent);
	
		// SYSTEM SETTINGS
		mSystemSettings = new SystemSettingsData(this);
//    	mSystemSettings.register();
		
		// APP SESSION
		mAppSession = new AppSessionData(this);

		// APP USAGE
		mAppUsage = new AppUsageData(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy()");
		
		// clean up
		unregisterReceiver(userSessionReceiver);
	
		if(appSessionStarted) {
			// APP SESSION
			mAppSession.unregister();
			
			// APP USAGE
			mAppUsage.unregister(mAppSession.getAppSessionId());
		}
		
		// SYSTEM SETTINGS
    	mSystemSettings.unregister(mAppSession.getAppSessionId());
		
		// clean up
		mAppSession = null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		boolean screenOn = intent.getBooleanExtra("screen_state", true);
		boolean userPresent = intent.getBooleanExtra("user_state", true);
		
		if (screenOn && userPresent) {
        	Log.d(TAG, "Screen ON, User PRESENT");
    		
        	// APP SESSION
        	mAppSession.register();
        	
        	// APP USAGE
        	mAppUsage.register();
        	
        	appSessionStarted = true;
        } else if(screenOn && !userPresent) {
        	Log.d(TAG, "Screen ON, User NOT PRESENT");
     	
        	appSessionStarted = false;
        	
        	// SYSTEM SETTINGS
        	mSystemSettings.register();
        } else {
        	Log.d(TAG, "Screen OFF, User NOT PRESENT");
        	
        	if(appSessionStarted) {
        		// APP SESSION
        		mAppSession.unregister();
        		
        		// APP USAGE
            	mAppUsage.unregister(mAppSession.getAppSessionId());
        	}
    		
        	// SYSTEM SETTINGS
        	mSystemSettings.unregister(mAppSession.getAppSessionId());
        }
		
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	
	// return this instance of UserSessionService so clients can call public methods
	public class LocalBinder extends Binder {
		UserSessionService getService() {
			return UserSessionService.this;
		}
	}
	
}
