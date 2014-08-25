package at.lukasmayerhofer.consens.usage;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import at.lukasmayerhofer.consens.contentprovider.ConsensContentProvider;
import at.lukasmayerhofer.consens.database.AppSessionModel;

public class AppSessionData {

	private static final String TAG = AppSessionData.class.getSimpleName();

	private Context mContext;
	private SimpleDateFormat dateFormatShort, dateFormatLong;
	
	private Timestamp timestampStart = null, timestampEnd = null, timestampDuration = null;
	private String appSessionStart = null, appSessionEnd = null, appSessionDuration = null;
	private int appSessionId = -1;
	
	
	// constructor
	public AppSessionData(Context context) {
		Log.d(TAG, "constructor: AppSessionData");
		
		// set context
		mContext = context;
		
		// set time format
		dateFormatLong = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
		dateFormatShort = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
	}

	
	// getters	
	public int getAppSessionId() {
		Log.d(TAG, "appSessionId: " + appSessionId);
		
		return appSessionId;
	}
	
	
	// register
	public void register() {
		Log.d(TAG, "register");
		
		// set start time (if user starts logging with start-button. after that: SCREEN_ON is triggered)
		timestampStart = new Timestamp(System.currentTimeMillis());
		appSessionStart = dateFormatLong.format(timestampStart);
	}
	
	
	// unregister
	public void unregister() {
		Log.d(TAG, "unregister");
		
		timestampEnd = new Timestamp(System.currentTimeMillis());
		appSessionEnd = dateFormatLong.format(timestampEnd);
		
		timestampDuration = new Timestamp(timestampEnd.getTime() - timestampStart.getTime());
		appSessionDuration = dateFormatShort.format(timestampDuration);
		
		// save data
		writeAppSession();
	}
	
	
	// save data in SQLite database (with ContentProvider)
	private void writeAppSession() {	
		// create values
        ContentValues values = new ContentValues();
		values.put(AppSessionModel.COLUMN_APP_SESSION_TIMESTAMP_START, appSessionStart);
		values.put(AppSessionModel.COLUMN_APP_SESSION_TIMESTAMP_END, appSessionEnd);
		values.put(AppSessionModel.COLUMN_APP_SESSION_TIMESTAMP_DURATION, appSessionDuration);
		values.put(AppSessionModel.COLUMN_SERVER_SENT, false);
        
		// save
        ContentResolver mSessionContentResolver = mContext.getContentResolver();
        Uri uri = mSessionContentResolver.insert(ConsensContentProvider.APP_SESSION_CONTENT_URI, values);
        appSessionId = Integer.parseInt(uri.getLastPathSegment());
        
	    Log.d(TAG, "appSessionId: " + appSessionId);
	}
	
}
