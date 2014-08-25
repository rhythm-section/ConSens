package at.lukasmayerhofer.consens.services;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import at.lukasmayerhofer.consens.contentprovider.ConsensContentProvider;
import at.lukasmayerhofer.consens.database.LocationModel;


public class LocationIntentService extends IntentService {
	
	private int apiLevel = 0;

	private static final String TAG = LocationIntentService.class.getSimpleName();
	
	SimpleDateFormat dateFormat;
	
	
	public LocationIntentService() {
		super("Location Service");
		
		// set time format
		dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
		
		apiLevel = android.os.Build.VERSION.SDK_INT;
	}

	@Override
	protected void onHandleIntent(Intent intent) {		
		if(intent.getExtras() != null) {
			Location location = (Location) intent.getExtras().get("com.google.android.location.LOCATION");
			
			Timestamp locationTimeStamp = new Timestamp(location.getTime());
            String locationDateTime = dateFormat.format(locationTimeStamp);;
            
            // create values for saving to database
            ContentValues values = new ContentValues();
			values.put(LocationModel.COLUMN_LOCATION_TIMESTAMP, locationDateTime);
			if(apiLevel >= 17) {
				values.put(LocationModel.COLUMN_LOCATION_ELAPSED_REALTIME_NANOS, location.getElapsedRealtimeNanos());
			} else {
				values.put(LocationModel.COLUMN_LOCATION_ELAPSED_REALTIME_NANOS, -1);
			}
			values.put(LocationModel.COLUMN_LOCATION_PROVIDER, location.getProvider());
			values.put(LocationModel.COLUMN_LOCATION_LATITUDE, location.getLatitude());
			values.put(LocationModel.COLUMN_LOCATION_LONGITUDE, location.getLongitude());
			values.put(LocationModel.COLUMN_LOCATION_ALTITUDE, location.getAltitude());
			values.put(LocationModel.COLUMN_LOCATION_SPEED, location.getSpeed());
			values.put(LocationModel.COLUMN_LOCATION_ACCURACY, location.getAccuracy());
			values.put(LocationModel.COLUMN_SERVER_SENT, false);
            
			// save to database with ContentProvider
            ContentResolver mContentResolver = getContentResolver();
            mContentResolver.insert(ConsensContentProvider.LOCATIONS_CONTENT_URI, values);
		} else {
			Log.d(TAG, "No location updates.");
		}
	}

}
