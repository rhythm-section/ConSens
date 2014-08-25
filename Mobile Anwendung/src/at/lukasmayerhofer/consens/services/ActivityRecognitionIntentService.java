package at.lukasmayerhofer.consens.services;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;
import at.lukasmayerhofer.consens.contentprovider.ConsensContentProvider;
import at.lukasmayerhofer.consens.database.ActivityModel;


public class ActivityRecognitionIntentService extends IntentService {

	private static final String TAG = ActivityRecognitionIntentService.class.getSimpleName();
	
	SimpleDateFormat dateFormat;
	
	
	public ActivityRecognitionIntentService() {
		super("ActivityRecognitionService");

		// set time format
		dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
	}
	
	private String getReadableVersion(int type) {
		switch(type) {
			case DetectedActivity.UNKNOWN:
				return "Unkown";
			case DetectedActivity.IN_VEHICLE:
				return "In Vehicle";
			case DetectedActivity.ON_BICYCLE:
				return "On Bicycle";
			case DetectedActivity.ON_FOOT:
				return "On Foot";
			case DetectedActivity.STILL:
				return "Still";
			case DetectedActivity.TILTING:
				return "Tilting";
			default:
				break;
		}
		
		return "";
	}

	@Override
	protected void onHandleIntent(Intent intent) {		
		if(ActivityRecognitionResult.hasResult(intent)){    		
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            Timestamp activityTimeStamp = new Timestamp(result.getTime());
            String activityDateTime = dateFormat.format(activityTimeStamp);
            
            // create values for saving to database
            ContentValues values = new ContentValues();
			values.put(ActivityModel.COLUMN_ACTIVITY_TIMESTAMP, activityDateTime);
			values.put(ActivityModel.COLUMN_ACTIVITY_NAME, getReadableVersion(result.getMostProbableActivity().getType()));
			values.put(ActivityModel.COLUMN_ACTIVITY_TYPE, result.getMostProbableActivity().getType());
			values.put(ActivityModel.COLUMN_ACTIVITY_CONFIDENCE, result.getMostProbableActivity().getConfidence());
			values.put(ActivityModel.COLUMN_SERVER_SENT, false);
            
			// save to database with ContentProvider
            ContentResolver mContentResolver = getContentResolver();
            mContentResolver.insert(ConsensContentProvider.ACTIVITIES_CONTENT_URI, values);
        } else {
        	Log.d(TAG, "No activity updates");
        }
	}

}
