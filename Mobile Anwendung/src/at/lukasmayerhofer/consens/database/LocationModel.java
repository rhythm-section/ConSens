package at.lukasmayerhofer.consens.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


public class LocationModel {

	private static final String TAG = LocationModel.class.getSimpleName();
	
	// variables for constructor, getter and setter
	private String locationTimeStamp;
	private long locationElapsedRealTimeNanos;
	private String locationProvider;
	private double locationLatitude;
	private double locationLongitude;
	private double locationAltitude;
	private float locationSpeed;
	private float locationAccuracy;
	private boolean serverSent;
	
	// define table column names
	public static final String TABLE_LOCATION = "locations";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_LOCATION_TIMESTAMP = "location_timestamp";
	public static final String COLUMN_LOCATION_ELAPSED_REALTIME_NANOS = "location_elapsed_realtime_nanos";
	public static final String COLUMN_LOCATION_PROVIDER = "location_provider";
	public static final String COLUMN_LOCATION_LATITUDE = "location_latitude";
	public static final String COLUMN_LOCATION_LONGITUDE = "location_longitude";
	public static final String COLUMN_LOCATION_ALTITUDE = "location_altitude";
	public static final String COLUMN_LOCATION_SPEED = "location_speed";
	public static final String COLUMN_LOCATION_ACCURACY = "location_accuracy";
	public static final String COLUMN_SERVER_SENT = "server_sent";
	
	// create databse SQL statement
	private static final String TABLE_CREATE = "create table " 
		+ TABLE_LOCATION
		+ "(" 
		+ COLUMN_ID + " integer primary key autoincrement, "
		+ COLUMN_LOCATION_TIMESTAMP + " text not null, "
		+ COLUMN_LOCATION_ELAPSED_REALTIME_NANOS + " integer not null, "
		+ COLUMN_LOCATION_PROVIDER + " text not null, "
		+ COLUMN_LOCATION_LATITUDE + " real not null, "
		+ COLUMN_LOCATION_LONGITUDE + " real not null, "
		+ COLUMN_LOCATION_ALTITUDE + " real not null, "
		+ COLUMN_LOCATION_SPEED + " real not null, "
		+ COLUMN_LOCATION_ACCURACY + " real not null, "
		+ COLUMN_SERVER_SENT + " integer not null"
		+ ");";
    
    
    // Create table
    public static void onCreate(SQLiteDatabase database) {
		Log.d(TAG, "onCreate()");
		
		database.execSQL(TABLE_CREATE);
	}
    	
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);
		onCreate(database);
	}

	
	// constructor
	public LocationModel(String locationTimeStamp,
					 long locationElapsedRealTimeNanos,
					 String locationProvider,
					 double locationLatitude,
					 double locationLongitude,
					 double locationAltitude,
					 float locationSpeed,
					 float locationAccuracy,
					 boolean serverSent) {
		this.locationTimeStamp = locationTimeStamp;
		this.locationElapsedRealTimeNanos = locationElapsedRealTimeNanos;
		this.locationProvider = locationProvider;
		this.locationLatitude = locationLatitude;
		this.locationLongitude = locationLongitude;
		this.locationAltitude = locationAltitude;
		this.locationSpeed = locationSpeed;
		this.locationAccuracy = locationAccuracy;
		this.serverSent = serverSent;
	}

	
	// getters
	public String getLocationTimeStamp() {
		return locationTimeStamp;
	}

	public long getLocationElapsedRealTimeNanos() {
		return locationElapsedRealTimeNanos;
	}

	public String getLocationProvider() {
		return locationProvider;
	}

	public double getLocationLatitude() {
		return locationLatitude;
	}

	public double getLocationLongitude() {
		return locationLongitude;
	}

	public double getLocationAltitude() {
		return locationAltitude;
	}

	public float getLocationSpeed() {
		return locationSpeed;
	}

	public float getLocationAccuracy() {
		return locationAccuracy;
	}
	
	public boolean getServerSent() {
		return serverSent;
	}

	
	// setters
	public void setLocationTimeStamp(String locationTimeStamp) {
		this.locationTimeStamp = locationTimeStamp;
	}

	public void setLocationElapsedRealTimeNanos(long locationElapsedRealTimeNanos) {
		this.locationElapsedRealTimeNanos = locationElapsedRealTimeNanos;
	}

	public void setLocationProvider(String locationProvider) {
		this.locationProvider = locationProvider;
	}

	public void setLocationLatitude(double locationLatitude) {
		this.locationLatitude = locationLatitude;
	}

	public void setLocationLongitude(double locationLongitude) {
		this.locationLongitude = locationLongitude;
	}

	public void setLocationAltitude(double locationAltitude) {
		this.locationAltitude = locationAltitude;
	}

	public void setLocationSpeed(float locationSpeed) {
		this.locationSpeed = locationSpeed;
	}

	public void setLocationAccuracy(float locationAccuracy) {
		this.locationAccuracy = locationAccuracy;
	}
	
	public void setServerSent(boolean serverSent) {
		this.serverSent = serverSent;
	}
	
}
