package at.lukasmayerhofer.consens.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


public class AppSessionModel {

	private static final String TAG = AppSessionModel.class.getSimpleName();
	
	// variables for constructor, getter and setter
	private String timestampStart;
	private String timestampEnd;
	private String timestampDuration;
	private boolean serverSent;
	
	// define table column names
	public static final String TABLE_APP_SESSION = "app_session";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_APP_SESSION_TIMESTAMP_START = "app_session_timestamp_start";
	public static final String COLUMN_APP_SESSION_TIMESTAMP_END = "app_session_timestamp_end";
	public static final String COLUMN_APP_SESSION_TIMESTAMP_DURATION = "app_session_timestamp_duration";
	public static final String COLUMN_SERVER_SENT = "server_sent";
	
	// create databse SQL statement
	private static final String TABLE_CREATE = "create table " 
		+ TABLE_APP_SESSION
		+ "(" 
		+ COLUMN_ID + " integer primary key autoincrement, "
		+ COLUMN_APP_SESSION_TIMESTAMP_START + " text not null, "
		+ COLUMN_APP_SESSION_TIMESTAMP_END + " text not null, "
		+ COLUMN_APP_SESSION_TIMESTAMP_DURATION + " text, "
		+ COLUMN_SERVER_SENT + " integer not null"
		+ ");";

	// create table
    public static void onCreate(SQLiteDatabase database) {
		Log.d(TAG, "onCreate()");
		
		database.execSQL(TABLE_CREATE);
	}
    	
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_APP_SESSION);
		onCreate(database);
	}

	
	// constructor
	public AppSessionModel(String timestampStart, String timestampEnd, String timestampDuration, boolean serverSent) {
		this.timestampStart = timestampStart;
		this.timestampEnd = timestampEnd;
		this.timestampDuration = timestampDuration;
		this.serverSent = serverSent;
	}

	
	// getters
	public String getTimestampStart() {
		return timestampStart;
	}

	public String getTimestampEnd() {
		return timestampEnd;
	}

	public String getTimestampDuration() {
		return timestampDuration;
	}

	public boolean getServerSent() {
		return serverSent;
	}
	
	
	// setters
	public void setTimestampStart(String timestampStart) {
		this.timestampStart = timestampStart;
	}

	public void setTimestampEnd(String timestampEnd) {
		this.timestampEnd = timestampEnd;
	}

	public void setTimestampDuration(String timestampDuration) {
		this.timestampDuration = timestampDuration;
	}
	
	public void setServerSent(boolean serverSent) {
		this.serverSent = serverSent;
	}
	
}
