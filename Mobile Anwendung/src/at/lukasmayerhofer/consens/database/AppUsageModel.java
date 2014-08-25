package at.lukasmayerhofer.consens.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


public class AppUsageModel {

	private static final String TAG = AppUsageModel.class.getSimpleName();
	
	// variables for constructor, getter and setter
	private String timestampStart;
	private String timestampEnd;
	private String timestampDuration;
	private int appSessionid;
	private String appName;
	private String packageName;
	private String versionName;
	private int versionCode;
	private String baseActivity;
	private String topActivity;
	private String firstInstallTime;
	private String lastUpdateTime;
	private boolean serverSent;

	// define table column names
	public static final String TABLE_APP_USAGE = "app_usage";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TIMESTAMP_START = "timestamp_start";
	public static final String COLUMN_TIMESTAMP_END = "timestamp_end";
	public static final String COLUMN_TIMESTAMP_DURATION = "timestamp_duration";
	public static final String COLUMN_APP_SESSION_ID = "app_session_id";
	public static final String COLUMN_APP_NAME = "app_name";
	public static final String COLUMN_PACKAGE_NAME = "package_name";
	public static final String COLUMN_VERSION_NAME = "version_name";
	public static final String COLUMN_VERSION_CODE = "version_code";
	public static final String COLUMN_BASE_ACTIVITY = "base_activity";
	public static final String COLUMN_TOP_ACTIVITY = "top_activity";
	public static final String COLUMN_FIRST_INSTALL_TIME = "first_install_time";
	public static final String COLUMN_LAST_UPDATE_TIME = "last_update_time";
	public static final String COLUMN_SERVER_SENT = "server_sent";
	
	// create databse SQL statement
	private static final String TABLE_CREATE = "create table " 
		+ TABLE_APP_USAGE
		+ "(" 
		+ COLUMN_ID + " integer primary key autoincrement, "
		+ COLUMN_TIMESTAMP_START + " text not null, "
		+ COLUMN_TIMESTAMP_END + " text not null, "
		+ COLUMN_TIMESTAMP_DURATION + " text, "
		+ COLUMN_APP_SESSION_ID + " integer, "
		+ COLUMN_APP_NAME + " text not null, "
		+ COLUMN_PACKAGE_NAME + " text not null, "
		+ COLUMN_VERSION_NAME + " text not null, "
		+ COLUMN_VERSION_CODE + " integer not null, "
		+ COLUMN_BASE_ACTIVITY + " text not null, "
		+ COLUMN_TOP_ACTIVITY + " text not null, "
		+ COLUMN_FIRST_INSTALL_TIME + " text not null, "
		+ COLUMN_LAST_UPDATE_TIME + " text not null, "
		+ COLUMN_SERVER_SENT + " integer not null"
		+ ");";

	
	// create or upgrade table
    public static void onCreate(SQLiteDatabase database) {
		Log.d(TAG, "onCreate()");
		
		database.execSQL(TABLE_CREATE);
	}
    	
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_APP_USAGE);
		onCreate(database);
	}
	
	
	// constructor
	public AppUsageModel(String timestampStart,
						String timestampEnd,
						String timestampDuration,
						int appSessionId,
						String appName,
						String packageName,
						String versionName,
						int versionCode,
						String baseActivity,
						String topActivity,
						String firstInstallTime,
						String lastUpdateTime,
						boolean serverSent) {
			this.timestampStart = timestampStart;
			this.timestampEnd = timestampEnd;
			this.timestampDuration = timestampDuration;
			this.appSessionid = appSessionId;
			this.appName = appName;
			this.packageName = packageName;
			this.versionName = versionName;
			this.versionCode = versionCode;
			this.baseActivity = baseActivity;
			this.topActivity = topActivity;
			this.firstInstallTime = firstInstallTime;
			this.lastUpdateTime = lastUpdateTime;
			this.serverSent = serverSent;
	}
	
	
	// getter
	public String getTimestampStart() {
		return timestampStart;
	}

	public String getTimestampEnd() {
		return timestampEnd;
	}

	public String getTimestampDuration() {
		return timestampDuration;
	}
	
	public int getAppSessionId() {
		return this.appSessionid;
	}
	
	public String getAppName() {
		return this.appName;
	}
	
	public String getPackageName() {
		return this.packageName;
	}
	
	public String getVersionName() {
		return this.versionName;
	}
	
	public int getVersionCode() {
		return this.versionCode;
	}
	
	public String getBaseActivity() {
		return this.baseActivity;
	}
	
	public String getTopActivity() {
		return topActivity;
	}
	
	public String getFirstInstallTime() {
		return this.firstInstallTime;
	}
	
	public String getLastUpdateTime() {
		return this.lastUpdateTime;
	}
	
	public boolean getServerSent() {
		return serverSent;
	}
	
	
	// setter
	public void setTimestampStart(String timestampStart) {
		this.timestampStart = timestampStart;
	}

	public void setTimestampEnd(String timestampEnd) {
		this.timestampEnd = timestampEnd;
	}

	public void setTimestampDuration(String timestampDuration) {
		this.timestampDuration = timestampDuration;
	}
	
	public void setAppSessionId(int appSessionId) {
		this.appSessionid = appSessionId;
	}
	
	public void setAppName(String appName) {
		this.appName = appName;
	}
	
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}
	
	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}
	
	public void setBaseActivity(String baseActivity) {
		this.baseActivity = baseActivity;
	}
	
	public void setTopActivity(String topActivity) {
		this.topActivity = topActivity;
	}
	
	public void setFirstInstallTime(String firstInstallTime) {
		this.firstInstallTime = firstInstallTime;
	}
	
	public void setLastUpdateTime(String lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
	
	public void setServerSent(boolean serverSent) {
		this.serverSent = serverSent;
	}
	
}
