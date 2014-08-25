package at.lukasmayerhofer.consens.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;


public class NetworkConnectionManager {

	private static final String TAG = NetworkConnectionManager.class.getSimpleName();
	
	private Context mContext;
	
	public NetworkConnectionManager(Context context) {
		Log.d(TAG, "NetworkConnectionManager()");
		mContext = context;
	}
	
	// check if phone is connected
	public boolean isConnected() {
		Log.d(TAG, "isConnected()");
        ConnectivityManager coonectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = coonectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
            	return true;
            } else {
            	return false; 
            }   
    }
	
}
