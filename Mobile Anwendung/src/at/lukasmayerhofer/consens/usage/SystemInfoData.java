package at.lukasmayerhofer.consens.usage;

import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.util.Log;


public class SystemInfoData {

	private static final String TAG = SystemInfoData.class.getSimpleName();
	private static final String SYSTEM_PREFERENCES = "at.lukasmayerhofer.consens.SYSTEM_PREFERENCES";
	
	private static Context mContext;
	private static String uuid = null;
	
	
	public synchronized static String getUUID(Context context) {
		mContext = context;
		SharedPreferences systemPreferences = null;
		
		// get uuid from shared preferences if available
		systemPreferences = mContext.getSharedPreferences(SYSTEM_PREFERENCES, Context.MODE_PRIVATE);
		uuid = systemPreferences.getString("UUID", null);
		Log.d(TAG, "UUID: " + uuid);
		
		// app was started for the very first time => create UUID
		if (uuid == null) {
        	uuid = UUID.randomUUID().toString();
        	
        	systemPreferences = mContext.getSharedPreferences(SYSTEM_PREFERENCES, Context.MODE_PRIVATE);
        	SharedPreferences.Editor mEditor = systemPreferences.edit();
        	mEditor.putString("UUID", uuid);
        	mEditor.commit();
        }
        
        return uuid;
	}
	
	public synchronized static boolean isUUID() {
		return (uuid != null) ? true : false;
	}
	
	public static String getSystemInfoOsVersion() {
		return System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
	}
	
	public static String getSystemInfoDevice() {
		return android.os.Build.DEVICE;
	}
	
	public static String getSystemInfoModel() {
		return android.os.Build.MODEL;
	}
	
	public static String getSystemInfoProduct() {
		return android.os.Build.PRODUCT;
	}
	
	public static String getSystemInfoManufacturer() {
		return android.os.Build.MANUFACTURER;
	}
	
	public static String getSystemInfoBrand() {
		return android.os.Build.BRAND;
	}
	
	public static String getSystemInfoAndroidId() {
		return Secure.getString(mContext.getContentResolver(), Secure.ANDROID_ID);
	}
	
}
