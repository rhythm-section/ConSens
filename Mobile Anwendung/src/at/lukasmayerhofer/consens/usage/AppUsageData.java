package at.lukasmayerhofer.consens.usage;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import at.lukasmayerhofer.consens.contentprovider.ConsensContentProvider;
import at.lukasmayerhofer.consens.database.AppUsageModel;


public class AppUsageData {

	private static final String TAG = AppUsageData.class.getSimpleName();

	private static int APP_USAGE_INTERVAL = 1000;		// 1 sec.
	
	private Context mContext;
	private SimpleDateFormat dateFormatShort, dateFormatLong;
	// private SimpleDateFormat dateFormat;
	private Timestamp timestampStart = null, timestampEnd = null, timestampDuration = null;
	private String appUsageStart = null, appUsageEnd = null, appUsageDuration = null;
	
	private ActivityManager mActivityManager;
	private ArrayList<AppUsageModel> appData = null;
	AppUsageModel previousAppDataItem = null;
	private Handler handler;
	private int mAppSessionId = -1;
	
	
	// constructor
	public AppUsageData(Context context) {
		Log.d(TAG, "constructor: AppUsageData");
		
		// set context
		mContext = context;
		
		// set time format
		// dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
		
		// set time format
		dateFormatLong = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
		dateFormatShort = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
		
		// handler for periodic app updates
		handler = new Handler();
		handler.post(topActivity);
	}
	
	
	// register
	public void register() {
		Log.d(TAG, "register");
		
		// initialize
		previousAppDataItem = null;
		
		// arraylist to save running apps
		appData = new ArrayList<AppUsageModel>();
		
		// handler for periodic app updates
		handler.post(topActivity);
	}

	
	// unregister
	public void unregister() {
		Log.d(TAG, "unregister");
		
		// remove handler updates
		handler.removeCallbacks(topActivity);
		
		// save data
		WriteAppUsage asyncTask = new WriteAppUsage();
		
		Log.d(TAG, "mAppSessionId = " + mAppSessionId);
		
		// pass arguments to AsyncTask "WriteUsage"
		if(mAppSessionId != -1) {
			asyncTask.mAppSessionId = mAppSessionId;
		}
		for(AppUsageModel appDataItem : appData) {
			asyncTask.appData.add(appDataItem);
		}
		
		// run AsyncTask "WriteUsage"
		asyncTask.execute();
		
		// after inserting, clear arraylist
		appData.clear();
	}
	
	
	// unregister with app session ID
	public void unregister(int appSessionId) {
		Log.d(TAG, "unregister with app session ID");
		
		// set appSessionId
		Log.d(TAG, "appSessionId = " + appSessionId);
		mAppSessionId = appSessionId;
		
		unregister();
	}
	
	
	
	// handler to periodically get the top activity (=active app the user currently uses)
	private final Runnable topActivity = new Runnable() {
	    public void run() {
	    	getTopActivity();	    	
	    	handler.postDelayed(this, APP_USAGE_INTERVAL);
	    }
	};
	
	
	// get top activity (=active app the user currently uses)
	private void getTopActivity() {
		if(mActivityManager == null) {
			mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		}
		
		try {
			// the first in the list of RunningTasks is always the foreground task.
			RunningTaskInfo foregroundTaskInfo = mActivityManager.getRunningTasks(1).get(0);
	        
			// get starting activity (baseActivity) and current foreground task (topActivity)
			ComponentName baseActivity = foregroundTaskInfo.baseActivity;
			ComponentName topActivity = foregroundTaskInfo.topActivity;
			
			// get package info of current foreground task
			PackageManager packageManager = (PackageManager) mContext.getPackageManager();
			PackageInfo appPackageInfo = packageManager.getPackageInfo(topActivity.getPackageName(), 0);
			
			String appName = appPackageInfo.applicationInfo.loadLabel(packageManager).toString();
			
			Timestamp firstInstallTimeMilliseconds = new Timestamp(appPackageInfo.firstInstallTime);
			String firstInstallTime = dateFormatLong.format(firstInstallTimeMilliseconds);
			
			Timestamp lastUpdateTimeMilliseconds = new Timestamp(appPackageInfo.lastUpdateTime);
			String lastUpdateTime = dateFormatLong.format(lastUpdateTimeMilliseconds);
			
			String packageName = appPackageInfo.packageName;
			String versionName = appPackageInfo.versionName;
			int versionCode = appPackageInfo.versionCode;
			
			// get last entry
			if (!appData.isEmpty()) {		// TODO: check if table is empty
				previousAppDataItem = appData.get(appData.size() - 1);
				
				// TODO: ContentProvider: getAs[Type]
			}
			
			// check if previous entry is null or is not the same as current entry
			if(previousAppDataItem == null || !previousAppDataItem.getTopActivity().equals(topActivity.toString())) {
				// set start time
				timestampStart = new Timestamp(System.currentTimeMillis());
				appUsageStart = dateFormatLong.format(timestampStart);
				
				timestampEnd = new Timestamp(System.currentTimeMillis());
				appUsageEnd = dateFormatLong.format(timestampEnd);
				
				timestampDuration = new Timestamp(timestampEnd.getTime() - timestampStart.getTime());
				appUsageDuration = dateFormatShort.format(timestampDuration);
				
				// Log.d(TAG, "start: " + appUsageStart);
				// Log.d(TAG, "end: " + appUsageEnd);
				// Log.d(TAG, "duration: " + appUsageDuration);
				// Log.d(TAG, "------------------------------");
				
				AppUsageModel dataItem = new AppUsageModel(
											appUsageStart,
											appUsageEnd,
											appUsageDuration,
											mAppSessionId,
											appName,
											packageName,
											versionName,
											versionCode,
											baseActivity.toString(),
											topActivity.toString(),
											firstInstallTime,
											lastUpdateTime,
											false);
				appData.add(dataItem);
			} else {
				// set end and duration time
				appUsageStart = previousAppDataItem.getTimestampStart();
				
				timestampEnd = new Timestamp(System.currentTimeMillis());
				appUsageEnd = dateFormatLong.format(timestampEnd);
				
				timestampDuration = new Timestamp(timestampEnd.getTime() - timestampStart.getTime());
				appUsageDuration = dateFormatShort.format(timestampDuration);
				
//				Log.d(TAG, "start: " + appUsageStart);
//				Log.d(TAG, "end: " + appUsageEnd);
//				Log.d(TAG, "duration: " + appUsageDuration);
//				Log.d(TAG, "------------------------------");
				Log.d(TAG, "app: " + packageName + " / " + appName + " / " + topActivity);
				
				AppUsageModel dataItem = new AppUsageModel(
						appUsageStart,
						appUsageEnd,
						appUsageDuration,
						mAppSessionId,
						appName,
						packageName,
						versionName,
						versionCode,
						baseActivity.toString(),
						topActivity.toString(),
						firstInstallTime,
						lastUpdateTime,
						false);
				appData.set(appData.size() - 1, dataItem);
			}
		} catch (Exception error) {
	        error.printStackTrace();
	    }
	}
	
	
	// save data in SQLite database (with ContentProvider)
	private class WriteAppUsage extends AsyncTask<Void, Void, Void> {

//		private ContentProviderResult[] insertResults;
		public int mAppSessionId;
		public ArrayList<AppUsageModel> appData = new ArrayList<AppUsageModel>();
	
		
		// override methods
		@Override
		protected Void doInBackground(Void... params) {
			Log.e(TAG, "-------- AppUsageInsert --------");

			ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
			
			// app usage data
			for(AppUsageModel appDataItem : appData) {
				ContentValues values = new ContentValues();

				Log.d(TAG, "App Session ID: " + mAppSessionId);
				
				values.put(AppUsageModel.COLUMN_APP_SESSION_ID, mAppSessionId);
				values.put(AppUsageModel.COLUMN_TIMESTAMP_START, appDataItem.getTimestampStart());
				values.put(AppUsageModel.COLUMN_TIMESTAMP_END, appDataItem.getTimestampEnd());
				values.put(AppUsageModel.COLUMN_TIMESTAMP_DURATION, appDataItem.getTimestampDuration());
				values.put(AppUsageModel.COLUMN_APP_NAME, appDataItem.getAppName());
				values.put(AppUsageModel.COLUMN_PACKAGE_NAME, appDataItem.getPackageName());
				values.put(AppUsageModel.COLUMN_VERSION_NAME, appDataItem.getVersionName());
				values.put(AppUsageModel.COLUMN_VERSION_CODE, appDataItem.getVersionCode());
				values.put(AppUsageModel.COLUMN_BASE_ACTIVITY, appDataItem.getBaseActivity());
				values.put(AppUsageModel.COLUMN_TOP_ACTIVITY, appDataItem.getTopActivity());
				values.put(AppUsageModel.COLUMN_FIRST_INSTALL_TIME, appDataItem.getFirstInstallTime());
				values.put(AppUsageModel.COLUMN_LAST_UPDATE_TIME, appDataItem.getLastUpdateTime());
				values.put(AppUsageModel.COLUMN_SERVER_SENT, false);
				
				operations.add(ContentProviderOperation.newInsert(ConsensContentProvider.APP_USAGE_CONTENT_URI).withValues(values).build());
			}
			
			
			try {
				ContentResolver mUsageContentResolver = mContext.getContentResolver();
				mUsageContentResolver.applyBatch(ConsensContentProvider.getAuthority(), operations);
			} catch(RemoteException error) {
				Log.e(TAG, error.toString());
			} catch(OperationApplicationException error) {
				Log.e(TAG, error.toString());
			}

			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}
	}
}
