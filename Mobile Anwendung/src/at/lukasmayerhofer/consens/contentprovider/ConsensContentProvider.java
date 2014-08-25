package at.lukasmayerhofer.consens.contentprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import at.lukasmayerhofer.consens.database.ActivityModel;
import at.lukasmayerhofer.consens.database.AppSessionModel;
import at.lukasmayerhofer.consens.database.AppUsageModel;
import at.lukasmayerhofer.consens.database.ConsensDatabaseHelper;
import at.lukasmayerhofer.consens.database.LocationModel;
import at.lukasmayerhofer.consens.database.SystemSettingsModel;


public class ConsensContentProvider extends ContentProvider {

	private static final String TAG = ConsensContentProvider.class.getSimpleName();
	
	// database
	private ConsensDatabaseHelper mDatabaseHelper;
	
	// uri matcher
	private static final String AUTHORITY = "at.lukasmayerhofer.consens.contentprovider";
	
	private static final int APP_SESSION = 1;
	private static final int APP_SESSION_ID = 2;
	
	private static final int APP_USAGE = 3;
	private static final int APP_USAGE_ID = 4;
	
	private static final int SYSTEM_SETTINGS = 5;
	private static final int SYSTEM_SETTING_ID = 6;
	
	private static final int ACTIVITIES = 7;
	private static final int ACTIVITY_ID = 8;
	
	private static final int LOCATIONS = 9;
	private static final int LOCATION_ID = 10;
	
	
	private static final String APP_SESSION_BASE_PATH = "app_session";
	public static final Uri APP_SESSION_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + APP_SESSION_BASE_PATH);
	
	private static final String APP_USAGE_BASE_PATH = "app_usage";
	public static final Uri APP_USAGE_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + APP_USAGE_BASE_PATH);
	
	private static final String SYSTEM_SETTINGS_BASE_PATH = "system_settings";
	public static final Uri SYSTEM_SETTINGS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + SYSTEM_SETTINGS_BASE_PATH);
	
	private static final String ACTIVITIES_BASE_PATH = "activities";
	public static final Uri ACTIVITIES_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + ACTIVITIES_BASE_PATH);
	
	private static final String LOCATIONS_BASE_PATH = "locations";
	public static final Uri LOCATIONS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + LOCATIONS_BASE_PATH);
	
	
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {		
		sURIMatcher.addURI(AUTHORITY, APP_SESSION_BASE_PATH, APP_SESSION);
		sURIMatcher.addURI(AUTHORITY, APP_SESSION_BASE_PATH + "/#", APP_SESSION_ID);
		
		sURIMatcher.addURI(AUTHORITY, APP_USAGE_BASE_PATH, APP_USAGE);
		sURIMatcher.addURI(AUTHORITY, APP_USAGE_BASE_PATH + "/#", APP_USAGE_ID);
		
		sURIMatcher.addURI(AUTHORITY, SYSTEM_SETTINGS_BASE_PATH, SYSTEM_SETTINGS);
		sURIMatcher.addURI(AUTHORITY, SYSTEM_SETTINGS_BASE_PATH + "/#", SYSTEM_SETTING_ID);
		
		sURIMatcher.addURI(AUTHORITY, ACTIVITIES_BASE_PATH, ACTIVITIES);
		sURIMatcher.addURI(AUTHORITY, ACTIVITIES_BASE_PATH + "/#", ACTIVITY_ID);
		
		sURIMatcher.addURI(AUTHORITY, LOCATIONS_BASE_PATH, LOCATIONS);
		sURIMatcher.addURI(AUTHORITY, LOCATIONS_BASE_PATH + "/#", LOCATION_ID);
	}
	
	
	public static String getAuthority() {
		return AUTHORITY;
	}
	
	
	@Override
	public synchronized boolean onCreate() {
		mDatabaseHelper = new ConsensDatabaseHelper(getContext());
		return false;
	}

	@Override
	public synchronized Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {		
		// using SQLiteQueryBuilder instead of query() method & setting the table
	    SQLiteQueryBuilder mQueryBuilder = new SQLiteQueryBuilder();
	    
	    int uriType = sURIMatcher.match(uri);
	    switch (uriType) {
	    case APP_SESSION:
	    	Log.d(TAG, "query app session");
	    	mQueryBuilder.setTables(AppSessionModel.TABLE_APP_SESSION);
	    	break;
	    case APP_SESSION_ID:
	    	Log.d(TAG, "query app session id");
	    	// adding the ID to the original query
	    	mQueryBuilder.appendWhere(AppSessionModel.COLUMN_ID + "=" + uri.getLastPathSegment());
	    	break;
	    case APP_USAGE:
	    	Log.d(TAG, "query app usage");
	    	mQueryBuilder.setTables(AppUsageModel.TABLE_APP_USAGE);
	    	break;
	    case APP_USAGE_ID:
	    	Log.d(TAG, "query app usage id");
	    	// adding the ID to the original query
	    	mQueryBuilder.appendWhere(AppUsageModel.COLUMN_ID + "=" + uri.getLastPathSegment());
	    	break;
	    case SYSTEM_SETTINGS:
	    	Log.d(TAG, "query system settings");
	    	mQueryBuilder.setTables(SystemSettingsModel.TABLE_SYSTEM_SETTINGS);
	    	break;
	    case SYSTEM_SETTING_ID:
	    	Log.d(TAG, "query system settings id");
	    	// adding the ID to the original query
	    	mQueryBuilder.appendWhere(SystemSettingsModel.COLUMN_ID + "=" + uri.getLastPathSegment());
	    	break;
	    case ACTIVITIES:
	    	Log.d(TAG, "query activities");
	    	mQueryBuilder.setTables(ActivityModel.TABLE_ACTIVITY);
	    	break;
	    case ACTIVITY_ID:
	    	Log.d(TAG, "query activity");
	    	// adding the ID to the original query
	    	mQueryBuilder.appendWhere(ActivityModel.COLUMN_ID + "=" + uri.getLastPathSegment());
	    	break;
	    case LOCATIONS:
	    	Log.d(TAG, "query locations");
	    	mQueryBuilder.setTables(LocationModel.TABLE_LOCATION);
	    	break;
	    case LOCATION_ID:
	    	Log.d(TAG, "query location");
	    	// adding the ID to the original query
	    	mQueryBuilder.appendWhere(LocationModel.COLUMN_ID + "=" + uri.getLastPathSegment());
	    	break;
	    default:
	      throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    
	    SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
	    
	    Cursor cursor = mQueryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
	    
	    // make sure that potential listeners are getting notified
	    cursor.setNotificationUri(getContext().getContentResolver(), uri);
	    
		return cursor;
	}

	@Override
	public synchronized String getType(Uri uri) {
		return null;
	}

	@Override
	public synchronized Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
		long id = 0;
		String uriString = "";
		
		Log.d(TAG, "uriType: " + uriType);
		
		switch(uriType) {
			case APP_SESSION:
				Log.d(TAG, "insert app session");
				id = database.insertOrThrow(AppSessionModel.TABLE_APP_SESSION, null, values);
				Log.d(TAG, "app session id: " + id);
				Log.d(TAG, "values: " + values);
				uriString = APP_SESSION_BASE_PATH + "/" + id;
				break;
			case APP_USAGE:
				Log.d(TAG, "insert used apps");
				id = database.insert(AppUsageModel.TABLE_APP_USAGE, null, values);
				uriString = APP_USAGE_BASE_PATH + "/" + id;
				break;
			case SYSTEM_SETTINGS:
				Log.d(TAG, "insert system settings");
				Log.d(TAG, "Values: " + values);
				id = database.insert(SystemSettingsModel.TABLE_SYSTEM_SETTINGS, null, values);
				Log.d(TAG, "id: " + id);
				uriString = SYSTEM_SETTINGS_BASE_PATH + "/" + id;
				break;
			case ACTIVITIES:
				Log.d(TAG, "insert activity");
				id = database.insert(ActivityModel.TABLE_ACTIVITY, null, values);
				uriString = ACTIVITIES_BASE_PATH + "/" + id;
				break;
			case LOCATIONS:
				Log.d(TAG, "insert location");
				id = database.insert(LocationModel.TABLE_LOCATION, null, values);
				uriString = LOCATIONS_BASE_PATH + "/" + id;
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		
		return Uri.parse(uriString);
	}
//
//	@Override
//	public int bulkInsert(Uri uri, ContentValues[] values) {
//		final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
//		final int match = sURIMatcher.match(uri);
//		
//		switch(match) {
//			case APP_USAGE:
//				Log.d(TAG, "bulk insert activities");
//				
//				int numInserted = 0;
//				
//				database.beginTransaction();
//				try {
//					for(ContentValues value : values) {
//						database.insert(AppUsageModel.TABLE_APP_USAGE, null, value);
//					}
//				} finally {
//					database.endTransaction();
//				}
//		}
//		
//		return super.bulkInsert(uri, values);
//	}

	@Override
	public synchronized int delete(Uri uri, String selection, String[] selectionArgs) {		
		return -1;
	}

	@Override
	public synchronized int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
		int rowsUpdated = 0;
		String id;
		
		switch (uriType) {
		case APP_SESSION:
			rowsUpdated = database.update(AppSessionModel.TABLE_APP_SESSION, values, selection, selectionArgs);
			break;
		case APP_SESSION_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = database.update(AppSessionModel.TABLE_APP_SESSION, values, AppSessionModel.COLUMN_ID + "=" + id, null);
			} else {
				rowsUpdated = database.update(AppSessionModel.TABLE_APP_SESSION, values, AppSessionModel.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
			}
			break;
		case APP_USAGE:
			rowsUpdated = database.update(AppUsageModel.TABLE_APP_USAGE, values, selection, selectionArgs);
			break;
		case APP_USAGE_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = database.update(AppUsageModel.TABLE_APP_USAGE, values, AppUsageModel.COLUMN_ID + "=" + id, null);
			} else {
				rowsUpdated = database.update(AppUsageModel.TABLE_APP_USAGE, values, AppUsageModel.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
			}
			break;
		case SYSTEM_SETTINGS:
			rowsUpdated = database.update(SystemSettingsModel.TABLE_SYSTEM_SETTINGS, values, selection, selectionArgs);
			break;
		case SYSTEM_SETTING_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = database.update(SystemSettingsModel.TABLE_SYSTEM_SETTINGS, values, SystemSettingsModel.COLUMN_ID + "=" + id, null);
			} else {
				rowsUpdated = database.update(SystemSettingsModel.TABLE_SYSTEM_SETTINGS, values, SystemSettingsModel.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
			}
			break;
		case ACTIVITIES:
			Log.d(TAG, "Values: " + values + "Selection: " + selection);
			rowsUpdated = database.update(ActivityModel.TABLE_ACTIVITY, values, selection, selectionArgs);
			break;
		case ACTIVITY_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = database.update(ActivityModel.TABLE_ACTIVITY, values, ActivityModel.COLUMN_ID + "=" + id, null);
			} else {
				rowsUpdated = database.update(ActivityModel.TABLE_ACTIVITY, values, ActivityModel.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
			}
			break;
		case LOCATIONS:
			Log.d(TAG, "Values: " + values + "Selection: " + selection);
			rowsUpdated = database.update(LocationModel.TABLE_LOCATION, values, selection, selectionArgs);
			break;
		case LOCATION_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = database.update(LocationModel.TABLE_LOCATION, values, LocationModel.COLUMN_ID + "=" + id, null);
			} else {
				rowsUpdated = database.update(LocationModel.TABLE_LOCATION, values, LocationModel.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		
		return rowsUpdated;
	}

}
