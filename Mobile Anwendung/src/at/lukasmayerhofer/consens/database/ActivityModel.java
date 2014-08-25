package at.lukasmayerhofer.consens.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ActivityModel {

	private static final String TAG = ActivityModel.class.getSimpleName();
	
	// variables for constructor, getter and setter
	private String activityTimeStamp;
	private String activityName;
	private int activityType;
	private int activityConfidence;
	private boolean serverSent;
	
	// define table column names
	public static final String TABLE_ACTIVITY = "activities";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_ACTIVITY_TIMESTAMP = "activity_timestamp";
	public static final String COLUMN_ACTIVITY_NAME = "activity_name";
	public static final String COLUMN_ACTIVITY_TYPE = "activity_type";
	public static final String COLUMN_ACTIVITY_CONFIDENCE = "activity_confidence";
	public static final String COLUMN_SERVER_SENT = "server_sent";
	
	// create databse SQL statement
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_ACTIVITY
			+ "("
			+ COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_ACTIVITY_TIMESTAMP + " text not null, "
			+ COLUMN_ACTIVITY_NAME + " text not null, "
			+ COLUMN_ACTIVITY_TYPE + " integer not null, "
			+ COLUMN_ACTIVITY_CONFIDENCE + " integer not null, "
			+ COLUMN_SERVER_SENT + " integer not null"
			+ ");";
	
	
	// define table column names
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}
	
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITY);
		onCreate(database);
	}
	
	
	// constructor
	public ActivityModel(String activityTimeStamp,
					String activityName,
					int activityType,
					int activityConfidence,
					boolean serverSent) {
		this.activityTimeStamp = activityTimeStamp;
		this.activityName = activityName;
		this.activityType = activityType;
		this.activityConfidence = activityConfidence;
		this.serverSent = serverSent;
	}
	
	
	// getter	
	public String getActivityTimeStamp() {
		return this.activityTimeStamp;
	}

	public String getActivityName() {
		return this.activityName;
	}
	
	public int getActivityType() {
		return this.activityType;
	}
	
	public int getActivityConfidence() {
		return this.activityConfidence;
	}
	
	public boolean getServerSent() {
		return serverSent;
	}
	
	
	// setter
	public void setActivityTimeStamp(String activityTimeStamp) {
		this.activityTimeStamp = activityTimeStamp;
	}
	
	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}
	
	public void setActivityType(int activityType) {
		this.activityType = activityType;
	}
	
	public void setActivityConfidence(int activityConfidence) {
		this.activityConfidence = activityConfidence;
	}
	
	public void setServerSent(boolean serverSent) {
		this.serverSent = serverSent;
	}
	
}
