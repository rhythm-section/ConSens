package at.lukasmayerhofer.consens.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class _SystemInfoModel {

	private static final String TAG = _SystemInfoModel.class.getSimpleName();
	
	// variables for constructor, getter and setter
	private String systemInfoOsVersion;
	private String systemInfoApiLevel;
	private String systemInfoDevice;
	private String systemInfoModel;
	private String systemInfoProduct;
	private String systemInfoAndroidId;
	
	// define table column names
	public static final String TABLE_ACTIVITY = "activities";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_SYSTEM_INFO_OS_VERSION = "system_info_os_version";
	public static final String COLUMN_SYSTEM_INFO_API_LEVEL = "system_info_api_level";
	public static final String COLUMN_SYSTEM_INFO_DEVICE = "system_info_device";
	public static final String COLUMN_SYSTEM_INFO_MODEL = "system_info_model";
	public static final String COLUMN_SYSTEM_INFO_PRODUCT = "system_info_product";
	public static final String COLUMN_SYSTEM_INFO_ANDROID_ID = "system_info_android_id";
	
	// create databse SQL statement
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_ACTIVITY
			+ "("
			+ COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_SYSTEM_INFO_OS_VERSION + " text not null, "
			+ COLUMN_SYSTEM_INFO_API_LEVEL + " text not null, "
			+ COLUMN_SYSTEM_INFO_DEVICE + " text not null, "
			+ COLUMN_SYSTEM_INFO_MODEL + " text not null, "
			+ COLUMN_SYSTEM_INFO_PRODUCT + " text not null, "
			+ COLUMN_SYSTEM_INFO_ANDROID_ID + " text not null, "
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
	public _SystemInfoModel(String systemInfoOsVersion,
			String systemInfoApiLevel, String systemInfoDevice,
			String systemInfoModel, String systemInfoProduct,
			String systemInfoAndroidId) {
		this.systemInfoOsVersion = systemInfoOsVersion;
		this.systemInfoApiLevel = systemInfoApiLevel;
		this.systemInfoDevice = systemInfoDevice;
		this.systemInfoModel = systemInfoModel;
		this.systemInfoProduct = systemInfoProduct;
		this.systemInfoAndroidId = systemInfoAndroidId;
	}

	
	// getters
	public String getSystemInfoOsVersion() {
		return systemInfoOsVersion;
	}

	public String getSystemInfoApiLevel() {
		return systemInfoApiLevel;
	}

	public String getSystemInfoDevice() {
		return systemInfoDevice;
	}

	public String getSystemInfoModel() {
		return systemInfoModel;
	}

	public String getSystemInfoProduct() {
		return systemInfoProduct;
	}

	public String getSystemInfoAndroidId() {
		return systemInfoAndroidId;
	}

	
	// setters
	public void setSystemInfoOsVersion(String systemInfoOsVersion) {
		this.systemInfoOsVersion = systemInfoOsVersion;
	}

	public void setSystemInfoApiLevel(String systemInfoApiLevel) {
		this.systemInfoApiLevel = systemInfoApiLevel;
	}

	public void setSystemInfoDevice(String systemInfoDevice) {
		this.systemInfoDevice = systemInfoDevice;
	}

	public void setSystemInfoModel(String systemInfoModel) {
		this.systemInfoModel = systemInfoModel;
	}

	public void setSystemInfoProduct(String systemInfoProduct) {
		this.systemInfoProduct = systemInfoProduct;
	}

	public void setSystemInfoAndroidId(String systemInfoAndroidId) {
		this.systemInfoAndroidId = systemInfoAndroidId;
	}
	
}
