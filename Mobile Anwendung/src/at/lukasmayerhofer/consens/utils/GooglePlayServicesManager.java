package at.lukasmayerhofer.consens.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import at.lukasmayerhofer.consens.MainActivity;
import at.lukasmayerhofer.consens.services.LocationIntentService;
import at.lukasmayerhofer.consens.services.ActivityRecognitionIntentService;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

public class GooglePlayServicesManager implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

	private static final String TAG = MainActivity.class.getSimpleName();
	
	// broadcast intents for communicating with main activity
	public static final String GOOGLE_PLAY_SERVICES_ERROR = "error";
	public static final String CONNECTION_RESULT = "connection_result";
	
	private Context mContext;
	private PendingIntent mLocationPendingIntent;
	private PendingIntent mActivityRecognitionPendingIntent;
	private LocationClient mLocationClient = null;
	private ActivityRecognitionClient mActivityRecognitionClient = null;
	
	// Flags that indicate if a request is underway
	private boolean mLocationInProgress;
	private boolean mActivityRecognitionInProgress;
	
	private static final int LOCATION_REQUEST = 1000;
	private static final int ACTIVITY_REQUEST = 2000;
	
	// request code to send to google play services; the code is returned in Activity.onActivityResult
	public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	
	private static final int LOCATION_UPDATE_INTERVALL_MILLISECONDS = 300 * 1000;	// 5 min
	private static final int LOCATION_FASTEST_INTERVALL_MILLISECONDS = 2 * 1000;	// 2 sec.
	private static final int ACTIVITY_INTERVALL_MILLISECONDS = 60 * 1000;			// 1 min
	private LocationRequest mLocationRequest;
	
	
	// constructor
	public GooglePlayServicesManager(Context context) {
		Log.d(TAG, "GooglePlayServicesManager()");
		
		mContext = context;
		
		
		// LOCATION
		mLocationInProgress = false;
		// instantiate location client
		mLocationClient = new LocationClient(mContext, this, this);
		// create PendingIntent that location services uses to send location updates to this app
		Intent locationIntent = new Intent(mContext, LocationIntentService.class);
		// return a PendingIntent that starts the IntentService
		mLocationPendingIntent = PendingIntent.getService(mContext, LOCATION_REQUEST, locationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		// create location request object
		mLocationRequest = LocationRequest.create();
		mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		mLocationRequest.setInterval(LOCATION_UPDATE_INTERVALL_MILLISECONDS);
		mLocationRequest.setFastestInterval(LOCATION_FASTEST_INTERVALL_MILLISECONDS);
		
		
		// ACTIVITY
		mActivityRecognitionInProgress = false;
		mActivityRecognitionClient = new ActivityRecognitionClient(mContext, this, this);
		Intent activityIntent = new Intent(mContext, ActivityRecognitionIntentService.class);
		mActivityRecognitionPendingIntent = PendingIntent.getService(mContext, ACTIVITY_REQUEST, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
	}
	
	
	// override methods
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {		
		if(connectionResult.hasResolution()) {		
			Log.e(TAG, "Connection Failed: " + connectionResult);
		} else {
			Log.e(TAG, "Connection Failed (no resolution)");
		}
	}

	@Override
	public void onConnected(Bundle data) {
		// display connection status
		Toast.makeText(mContext, "ConSens: GooglePlay connected.", Toast.LENGTH_SHORT).show();
		Log.d(TAG, "onConnected()");
		
		if(mLocationClient.isConnected()) {
			mLocationClient.requestLocationUpdates(mLocationRequest, mLocationPendingIntent);
		}
		
		if(mActivityRecognitionClient.isConnected()) {
			mActivityRecognitionClient.requestActivityUpdates(ACTIVITY_INTERVALL_MILLISECONDS, mActivityRecognitionPendingIntent);
		}
	}

	@Override
	public void onDisconnected() {
		// display connection status
		Toast.makeText(mContext, "ConSens: GooglePlay disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
		
		mLocationInProgress = false;
		mLocationClient = null;
	}
	
	
	// check if google play services are connected
	private boolean servicesConnected() {
		// check that google play services is available
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
		
		// if available
		if(ConnectionResult.SUCCESS == resultCode) {
			Log.d(TAG, "Google Play services is available.");
			return true;
		} else {
			Log.e(TAG, "Google Play services not available.");
			return false;
		}
	}
	
	
	// start & stop updates
	public void startLocationUpdates() {
		if(!servicesConnected()) {
			return;
		}
		
		// if a request is not already underway
		if(!mLocationInProgress) {
			mLocationInProgress = true;
			mLocationClient.connect();
		} else {
			Log.d(TAG, "Request already underway: Disconnect client and retry the request");
			stopLocationUpdates();
			startLocationUpdates();
		}
	}
	
	public void stopLocationUpdates() {
		mLocationInProgress = false;
		
		if(mLocationClient.isConnected()) {
			Log.d(TAG, "Remove location updates");
			mLocationClient.removeLocationUpdates(mLocationPendingIntent);
		}
		mLocationClient.disconnect();
	}
	
	public void startActivityUpdates() {
		Log.d(TAG, "Start activity updates");
		
		if(!servicesConnected()) {
			return;
		}
		
		// if a request is not already underway
		if(!mActivityRecognitionInProgress) {
			mActivityRecognitionInProgress = true;
			mActivityRecognitionClient.connect();
		} else {
			Log.d(TAG, "Request already underway: Disconnect client and retry the request");
			stopActivityUpdates();
			startActivityUpdates();
		}
	}
	
	public void stopActivityUpdates() {
		Log.d(TAG, "Stop activity updates");
		
		mActivityRecognitionInProgress = false;
		
		if(mActivityRecognitionClient.isConnected()) {
			Log.d(TAG, "Remove activity updates");
			mActivityRecognitionClient.removeActivityUpdates(mActivityRecognitionPendingIntent);
		}
		mActivityRecognitionClient.disconnect();
	}
	
}
