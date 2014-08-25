package at.lukasmayerhofer.consens.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import at.lukasmayerhofer.consens.usage.SystemSettingsData;

public class WifiReceiver extends BroadcastReceiver {
	private static final String TAG = WifiReceiver.class.getSimpleName();
	
	static String wifi_ssid = "-1";
	
	@Override
	public void onReceive(Context mContext, Intent intent) {
		Log.d(TAG, "onReceive()");
		
		NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
		if(info != null) {
			if(info.isConnected()) {
	            SystemSettingsData mSystemSettings = new SystemSettingsData(mContext);
	            mSystemSettings.register();
	            if(mSystemSettings.notifySettingsChange()) {
	            	mSystemSettings.unregister();
	            }
			}
	    }
	}
}
