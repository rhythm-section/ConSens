package at.lukasmayerhofer.consens.usage;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import at.lukasmayerhofer.consens.contentprovider.ConsensContentProvider;
import at.lukasmayerhofer.consens.database.SystemSettingsModel;


public class SystemSettingsData {

	private static final String TAG = SystemSettingsData.class.getSimpleName();
	
	private Context mContext;
	SimpleDateFormat dateFormat;
	private int mAppSessionId = -1;
	
	private ContentResolver mSystemSettingsContentResolver = null;
	private SystemSettingsContentObserver mSystemSettingsContentObserver;
	
	private int apiLevel = 0;
	private long airplaneMode = -1, bluetooth = -1, dataRoaming = -1, developmentSettingsEnabled = -1, usbMassStorageEnabled = -1, wifi = -1, locationMode17 = -1;
	private String httpProxy = "-1", modeRinger = "-1", networkPreference = "-1", stayOnWhilePluggedIn = "-1", locationMode3 = "-1", wifiSSID = "-1";
	private int volumeAlarm = -1, volumeMusic = -1, volumeNotification = -1, volumeRing = -1, volumeSystem = -1, volumeVoice = -1;
	
	
	// constructor
	public SystemSettingsData(Context context) {
		Log.d(TAG, "constructor: SystemSettingsData");
		
		mContext = context;
		
		apiLevel = android.os.Build.VERSION.SDK_INT;
		
		mSystemSettingsContentResolver = mContext.getContentResolver();
		mSystemSettingsContentObserver = new SystemSettingsContentObserver(new Handler());
		
		// set time format
		dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
	}
	
	
	// register
	@SuppressLint("NewApi")
	public void register() {
		if(apiLevel >= 17) {
			Log.d(TAG, "register: API Level >=17");
			mSystemSettingsContentResolver.registerContentObserver(android.provider.Settings.Global.CONTENT_URI, true, mSystemSettingsContentObserver);
			mSystemSettingsContentResolver.registerContentObserver(android.provider.Settings.Secure.CONTENT_URI, true, mSystemSettingsContentObserver);
			mSystemSettingsContentResolver.registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mSystemSettingsContentObserver);
			setSystemSettingsApi17();
		} else {
			Log.d(TAG, "register: API Level >=3");
			mSystemSettingsContentResolver.registerContentObserver(android.provider.Settings.Secure.CONTENT_URI, true, mSystemSettingsContentObserver);
			mSystemSettingsContentResolver.registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mSystemSettingsContentObserver);
			setSystemSettingsApi3();
		}
	}
	
	
	// unregister
	public void unregister() {
		Log.d(TAG, "unregister");
		mSystemSettingsContentResolver.unregisterContentObserver(mSystemSettingsContentObserver);
	}
	
	// unregister with app session ID
	public void unregister(int appSessionId) {
		Log.d(TAG, "unregister with app session ID");
		
		// set appSessionId
		Log.d(TAG, "appSessionId = " + appSessionId);
		mAppSessionId = appSessionId;
		
		unregister();
	}
	
	
	// WifiReceiver sets wifiSSID if wifi connection established
	public boolean notifySettingsChange() {
		setSSID();
		
		if(writeSystemSettings()) {
			return true;
		}
		return false;
	}
	
	private void setSSID() {
		if(wifi != 0) {
			WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
	        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
	        wifiSSID = mWifiInfo.getSSID();
			Log.d(TAG, "wifiSSID");
		} else {
			wifiSSID = "-1";
		}
	}
	
	
	// getters
	public ContentResolver getSystemSettingsContentResolver() {
		if(mSystemSettingsContentResolver != null) {
			return mSystemSettingsContentResolver;
		}
		return null;
	}
	
	@SuppressWarnings("deprecation")
	private void setSystemSettingsApi3() {		
		try {
			// Settings: System
			airplaneMode = Settings.System.getInt(mSystemSettingsContentResolver, Settings.System.AIRPLANE_MODE_ON);
			modeRinger = Settings.System.getString(mSystemSettingsContentResolver, Settings.System.MODE_RINGER);
			stayOnWhilePluggedIn = Settings.System.getString(mSystemSettingsContentResolver, Settings.System.STAY_ON_WHILE_PLUGGED_IN);
			
        	// Settings: Secure
        	bluetooth = Settings.Secure.getInt(mSystemSettingsContentResolver, Settings.Secure.BLUETOOTH_ON);
        	dataRoaming = Settings.Secure.getInt(mSystemSettingsContentResolver, Settings.Secure.DATA_ROAMING);
        	developmentSettingsEnabled = -1;
        	httpProxy = Settings.Secure.getString(mSystemSettingsContentResolver, Settings.Secure.HTTP_PROXY);
        	networkPreference = Settings.Secure.getString(mSystemSettingsContentResolver, Settings.Secure.NETWORK_PREFERENCE);
        	usbMassStorageEnabled = Settings.Secure.getInt(mSystemSettingsContentResolver, Settings.Secure.USB_MASS_STORAGE_ENABLED);
        	wifi = Settings.Secure.getInt(mSystemSettingsContentResolver, Settings.Secure.WIFI_ON);
        	locationMode3 = Settings.Secure.getString(mSystemSettingsContentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        	
//        	Log.d(TAG, "---------------- API Level >=3 -----------------");
//        	Log.d(TAG, "airplaneMode: " + airplaneMode);
//        	Log.d(TAG, "bluetooth: " + bluetooth);
//        	Log.d(TAG, "dataRoaming: " + dataRoaming);
//        	Log.d(TAG, "developmentSettingsEnabled: " + developmentSettingsEnabled);
//        	Log.d(TAG, "httpProxy: " + httpProxy);
//        	Log.d(TAG, "modeRinger: " + modeRinger);
//        	Log.d(TAG, "volumeAlarm: " + volumeAlarm);
//        	Log.d(TAG, "volumeMusic: " + volumeMusic);
//        	Log.d(TAG, "volumeNotification: " + volumeNotification);
//        	Log.d(TAG, "volumeRing: " + volumeRing);
//        	Log.d(TAG, "volumeSystem: " + volumeSystem);
//        	Log.d(TAG, "volumeVoice: " + volumeVoice);
//        	Log.d(TAG, "networkPreference: " + networkPreference);
//        	Log.d(TAG, "stayOnWhilePluggedIn: " + stayOnWhilePluggedIn);
//        	Log.d(TAG, "usbMassStorageEnabled: " + usbMassStorageEnabled);
//        	Log.d(TAG, "wifi: " + wifi);
//        	Log.d(TAG, "locationMode: " + locationMode3);
//        	Log.d(TAG, "-------------------------------------------------");
        } catch(SettingNotFoundException error) {
        	error.printStackTrace();
        }
		
		writeSystemSettings();
	}
	
	@SuppressLint("NewApi")
	private void setSystemSettingsApi17() {		
        try {            	
        	// Settings: Global
        	airplaneMode = Settings.Global.getInt(mSystemSettingsContentResolver, Settings.Global.AIRPLANE_MODE_ON);
        	bluetooth = Settings.Global.getInt(mSystemSettingsContentResolver, Settings.Global.BLUETOOTH_ON);
        	dataRoaming = Settings.Global.getInt(mSystemSettingsContentResolver, Settings.Global.DATA_ROAMING);
        	developmentSettingsEnabled = Settings.Global.getInt(mSystemSettingsContentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED);
        	httpProxy = Settings.Global.getString(mSystemSettingsContentResolver, Settings.Global.HTTP_PROXY);
        	modeRinger = Settings.Global.getString(mSystemSettingsContentResolver, Settings.Global.MODE_RINGER);
        	networkPreference = Settings.Global.getString(mSystemSettingsContentResolver, Settings.Global.NETWORK_PREFERENCE);
        	stayOnWhilePluggedIn = Settings.Global.getString(mSystemSettingsContentResolver, Settings.Global.STAY_ON_WHILE_PLUGGED_IN);
        	usbMassStorageEnabled = Settings.Global.getInt(mSystemSettingsContentResolver, Settings.Global.USB_MASS_STORAGE_ENABLED);
        	wifi = Settings.Global.getInt(mSystemSettingsContentResolver, Settings.Global.WIFI_ON);
        	locationMode17 = Settings.Secure.getInt(mSystemSettingsContentResolver, Settings.Secure.LOCATION_MODE);
        	
//        	Log.d(TAG, "---------------- API Level >=17 ----------------");
//        	Log.d(TAG, "airplaneMode: " + airplaneMode);
//        	Log.d(TAG, "bluetooth: " + bluetooth);
//        	Log.d(TAG, "dataRoaming: " + dataRoaming);
//        	Log.d(TAG, "developmentSettingsEnabled: " + developmentSettingsEnabled);
//        	Log.d(TAG, "httpProxy: " + httpProxy);
//        	Log.d(TAG, "modeRinger: " + modeRinger);
//        	Log.d(TAG, "volumeAlarm: " + volumeAlarm);
//        	Log.d(TAG, "volumeMusic: " + volumeMusic);
//        	Log.d(TAG, "volumeNotification: " + volumeNotification);
//        	Log.d(TAG, "volumeRing: " + volumeRing);
//        	Log.d(TAG, "volumeSystem: " + volumeSystem);
//        	Log.d(TAG, "volumeVoice: " + volumeVoice);
//        	Log.d(TAG, "networkPreference: " + networkPreference);
//        	Log.d(TAG, "stayOnWhilePluggedIn: " + stayOnWhilePluggedIn);
//        	Log.d(TAG, "usbMassStorageEnabled: " + usbMassStorageEnabled);
//        	Log.d(TAG, "wifi: " + wifi);
//        	Log.d(TAG, "locationMode: " + locationMode17);
//        	Log.d(TAG, "-------------------------------------------------");
        } catch(SettingNotFoundException error) {
        	error.printStackTrace();
        }
        
        writeSystemSettings();
	}
	
	
	private boolean writeSystemSettings() {		
		Timestamp settingsTimeStamp = new Timestamp(System.currentTimeMillis());
        String settingsDateTime = dateFormat.format(settingsTimeStamp);
        
        ContentValues values = new ContentValues();
        
        // get current audio levels (volume)
        AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        volumeAlarm = audio.getStreamVolume(AudioManager.STREAM_ALARM);
        volumeMusic = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumeNotification = audio.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
    	volumeRing = audio.getStreamVolume(AudioManager.STREAM_RING);
    	volumeSystem = audio.getStreamVolume(AudioManager.STREAM_SYSTEM);
    	volumeVoice = audio.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        
    	// set SSID (wifi)
    	setSSID();
    	
        // create values for saving to database
		values.put(SystemSettingsModel.COLUMN_SETTINGS_APP_SESSION_ID, mAppSessionId);
		values.put(SystemSettingsModel.COLUMN_SETTINGS_TIMESTAMP, settingsDateTime);
		values.put(SystemSettingsModel.COLUMN_SETTINGS_AIRPLANE_MODE, airplaneMode);
		values.put(SystemSettingsModel.COLUMN_SETTINGS_BLUETOOTH, bluetooth);
		values.put(SystemSettingsModel.COLUMN_SETTINGS_DATA_ROAMING, dataRoaming);
		values.put(SystemSettingsModel.COLUMN_SETTINGS_DEVELOPMENT_SETTINGS_ENABLED, developmentSettingsEnabled);
		values.put(SystemSettingsModel.COLUMN_SETTINGS_HTTP_PROXY, httpProxy);
		values.put(SystemSettingsModel.COLUMN_SETTINGS_MODE_RINGER, modeRinger);
		values.put(SystemSettingsModel.COLUMN_SETTINGS_VOLUME_ALARM, volumeAlarm);
		values.put(SystemSettingsModel.COLUMN_SETTINGS_VOLUME_MUSIC, volumeMusic);
		values.put(SystemSettingsModel.COLUMN_SETTINGS_VOLUME_NOTIFICATION, volumeNotification);
		values.put(SystemSettingsModel.COLUMN_SETTINGS_VOLUME_RING, volumeRing);
		values.put(SystemSettingsModel.COLUMN_SETTINGS_VOLUME_SYSTEM, volumeSystem);
		values.put(SystemSettingsModel.COLUMN_SETTINGS_VOLUME_VOICE, volumeVoice);
		values.put(SystemSettingsModel.COLUMN_SETTINGS_NETWORK_PREFERENCE, networkPreference);
		values.put(SystemSettingsModel.COLUMN_SETTINGS_STAY_ON_WHILE_PLUGGED_IN, stayOnWhilePluggedIn);
		values.put(SystemSettingsModel.COLUMN_SETTINGS_USB_MASS_STORAGE_ENABLED, usbMassStorageEnabled);
		values.put(SystemSettingsModel.COLUMN_SETTINGS_WIFI, wifi);
		values.put(SystemSettingsModel.COLUMN_SETTINGS_WIFI_SSID, wifiSSID);
		values.put(SystemSettingsModel.COLUMN_SETTINGS_LOCATION_MODE_3, locationMode3);
		values.put(SystemSettingsModel.COLUMN_SETTINGS_LOCATION_MODE_17, locationMode17);
		values.put(SystemSettingsModel.COLUMN_SERVER_SENT, false);
		
		
//		Log.d(TAG, "airplaneMode: " + airplaneMode);
//    	Log.d(TAG, "bluetooth: " + bluetooth);
//    	Log.d(TAG, "dataRoaming: " + dataRoaming);
//    	Log.d(TAG, "developmentSettingsEnabled: " + developmentSettingsEnabled);
//    	Log.d(TAG, "httpProxy: " + httpProxy);
//    	Log.d(TAG, "modeRinger: " + modeRinger);
//    	Log.d(TAG, "volumeAlarm: " + volumeAlarm);
//    	Log.d(TAG, "volumeMusic: " + volumeMusic);
//    	Log.d(TAG, "volumeNotification: " + volumeNotification);
//    	Log.d(TAG, "volumeRing: " + volumeRing);
//    	Log.d(TAG, "volumeSystem: " + volumeSystem);
//    	Log.d(TAG, "volumeVoice: " + volumeVoice);
    	Log.d(TAG, "networkPreference: " + networkPreference);
//    	Log.d(TAG, "stayOnWhilePluggedIn: " + stayOnWhilePluggedIn);
//    	Log.d(TAG, "usbMassStorageEnabled: " + usbMassStorageEnabled);
    	Log.d(TAG, "wifi: " + wifi);
    	Log.d(TAG, "wifiSSID: " + wifiSSID);
//    	Log.d(TAG, "locationMode: " + locationMode3);
    	Log.d(TAG, "-------------------------------------------------");
		
		
		if(values.size() != 0) {
        	// save to database with ContentProvider
            ContentResolver mContentResolver = mContext.getContentResolver();
            mContentResolver.insert(ConsensContentProvider.SYSTEM_SETTINGS_CONTENT_URI, values);
            return true;
        } else {
        	Log.d(TAG, "no setting values to insert.");
        	return false;
        }
	}
	
	
	
	
		
	// content observer subclass
	// suppresslint because onChange(boolean) is just for older devices => ignore warnings
	@SuppressLint("New Api")
	class SystemSettingsContentObserver extends ContentObserver {
		
		private long lastTimeofCall = 0L;
		private long lastTimeofUpdate = 0L;
		private long threshold_time = 1000;
		
		// constructor
		public SystemSettingsContentObserver(Handler handler) {
		    super(handler);
		}
		
		
		// override methods
		@Override
		public boolean deliverSelfNotifications() {
			// return true if observer is interested receiving self-change notifications
		    return true;
		}
		
		@Override
		public void onChange(boolean selfChange) {
		    super.onChange(selfChange);
		    
		    lastTimeofCall = System.currentTimeMillis();
		    
			// because content observer often called multiple times, only update if there was enough time between two calls
			if(lastTimeofCall - lastTimeofUpdate > threshold_time) {
				// handler comes from UserSessionService => this runs in the UI thread (no long running processes!)
				// => quering ContentProvider in AsyncTask instead
				if(apiLevel >= 17) {
					setSystemSettingsApi17();
				} else {
					setSystemSettingsApi3();
				}
				
				lastTimeofUpdate = System.currentTimeMillis();
			}
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			super.onChange(selfChange, uri);
			
			lastTimeofCall = System.currentTimeMillis();
			
			// because content observer often called multiple times, only update if there was enough time between two calls
			if(lastTimeofCall - lastTimeofUpdate > threshold_time) {
				// handler comes from UserSessionService => this runs in the UI thread (no long running processes!)
				// => quering ContentProvider in AsyncTask instead
				if(apiLevel >= 17) {
					setSystemSettingsApi17();
				} else {
					setSystemSettingsApi3();
				}
				
				lastTimeofUpdate = System.currentTimeMillis();
			}
		}
	}
	
}
