package at.lukasmayerhofer.consens.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SystemSettingsModel {

	private static final String TAG = SystemSettingsModel.class.getSimpleName();
	
	// variables for constructor, getter and setter
	private long airplaneMode;
	private long bluetooth;
	private long dataRoaming;
	private long developmentSettingsEnabled;
	private String httpProxy;
	private String modeRinger;
	private int volumeAlarm;
	private int volumeMusic;
	private int volumeNotification;
	private int volumeRing;
	private int volumeSystem;
	private int volumeVoice;
	private String networkPreference;
	private String stayOnWhilePluggedIn;
	private long usbMassStorageEnabled;
	private long wifi;
	private String wifi_ssid;
	private String locationMode3;
	private long locationMode17;
	private boolean serverSent;

	// define table column names
	public static final String TABLE_SYSTEM_SETTINGS = "system_settings";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_SETTINGS_APP_SESSION_ID = "settings_app_session_id";
	public static final String COLUMN_SETTINGS_TIMESTAMP = "settings_timestamp";
	public static final String COLUMN_SETTINGS_AIRPLANE_MODE = "settings_airplane_mode";
	public static final String COLUMN_SETTINGS_BLUETOOTH = "settings_bluetooth";
	public static final String COLUMN_SETTINGS_DATA_ROAMING = "settings_data_roaming";
	public static final String COLUMN_SETTINGS_DEVELOPMENT_SETTINGS_ENABLED = "settings_development_settings_enabled";
	public static final String COLUMN_SETTINGS_HTTP_PROXY = "settings_http_proxy";
	public static final String COLUMN_SETTINGS_MODE_RINGER = "settings_mode_ringer";
	public static final String COLUMN_SETTINGS_VOLUME_ALARM = "settings_volume_alarm";
	public static final String COLUMN_SETTINGS_VOLUME_MUSIC = "settings_volume_music";
	public static final String COLUMN_SETTINGS_VOLUME_NOTIFICATION = "settings_volume_notification";
	public static final String COLUMN_SETTINGS_VOLUME_RING = "settings_volume_ring";
	public static final String COLUMN_SETTINGS_VOLUME_SYSTEM = "settings_volume_system";
	public static final String COLUMN_SETTINGS_VOLUME_VOICE = "settings_volume_voice";
	public static final String COLUMN_SETTINGS_NETWORK_PREFERENCE = "settings_networkPreference";
	public static final String COLUMN_SETTINGS_STAY_ON_WHILE_PLUGGED_IN = "settings_stay_on_while_plugged_in";
	public static final String COLUMN_SETTINGS_USB_MASS_STORAGE_ENABLED = "settings_usb_mass_storage_enabled";
	public static final String COLUMN_SETTINGS_WIFI = "settings_wifi";
	public static final String COLUMN_SETTINGS_WIFI_SSID = "settings_wifi_ssid";
	public static final String COLUMN_SETTINGS_LOCATION_MODE_3 = "settings_location_mode_3";
	public static final String COLUMN_SETTINGS_LOCATION_MODE_17 = "settings_location_mode_17";
	public static final String COLUMN_SERVER_SENT = "server_sent";
	
	// create databse SQL statement
	private static final String TABLE_CREATE = "create table " 
		+ TABLE_SYSTEM_SETTINGS
		+ "(" 
		+ COLUMN_ID + " integer primary key autoincrement, "
		+ COLUMN_SETTINGS_APP_SESSION_ID + " integer, "
		+ COLUMN_SETTINGS_TIMESTAMP + " text not null, "
		+ COLUMN_SETTINGS_AIRPLANE_MODE + " integer, "
		+ COLUMN_SETTINGS_BLUETOOTH + " integer, "
		+ COLUMN_SETTINGS_DATA_ROAMING + " integer, "
		+ COLUMN_SETTINGS_DEVELOPMENT_SETTINGS_ENABLED + " integer, "
		+ COLUMN_SETTINGS_HTTP_PROXY + " text, "
		+ COLUMN_SETTINGS_MODE_RINGER + " text, "
		+ COLUMN_SETTINGS_VOLUME_ALARM + " integer, "
		+ COLUMN_SETTINGS_VOLUME_MUSIC + " integer, "
		+ COLUMN_SETTINGS_VOLUME_NOTIFICATION + " integer, "
		+ COLUMN_SETTINGS_VOLUME_RING + " integer, "
		+ COLUMN_SETTINGS_VOLUME_SYSTEM + " integer, "
		+ COLUMN_SETTINGS_VOLUME_VOICE + " integer, "
		+ COLUMN_SETTINGS_NETWORK_PREFERENCE + " text, "
		+ COLUMN_SETTINGS_STAY_ON_WHILE_PLUGGED_IN + " text, "
		+ COLUMN_SETTINGS_USB_MASS_STORAGE_ENABLED + " integer, "
		+ COLUMN_SETTINGS_WIFI + " integer, "
		+ COLUMN_SETTINGS_WIFI_SSID + " text, "
		+ COLUMN_SETTINGS_LOCATION_MODE_3 + " text, "
		+ COLUMN_SETTINGS_LOCATION_MODE_17 + " integer, "
		+ COLUMN_SERVER_SENT + " integer not null"
		+ ");";

	
	// create or upgrade table
    public static void onCreate(SQLiteDatabase database) {
		Log.d(TAG, "onCreate()");
		
		database.execSQL(TABLE_CREATE);
	}
    	
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_SYSTEM_SETTINGS);
		onCreate(database);
	}

	
	// constructor
	public SystemSettingsModel(long airplaneMode, long bluetooth,
			long dataRoaming, long developmentSettingsEnabled,
			String httpProxy, String modeRinger, int volumeAlarm,
			int volumeMusic, int volumeNotification, int volumeRing,
			int volumeSystem, int volumeVoice, String networkPreference,
			String stayOnWhilePluggedIn, long usbMassStorageEnabled, long wifi,
			String wifi_ssid, String locationMode3, long locationMode17, boolean serverSent) {
		super();
		this.airplaneMode = airplaneMode;
		this.bluetooth = bluetooth;
		this.dataRoaming = dataRoaming;
		this.developmentSettingsEnabled = developmentSettingsEnabled;
		this.httpProxy = httpProxy;
		this.modeRinger = modeRinger;
		this.volumeAlarm = volumeAlarm;
		this.volumeMusic = volumeMusic;
		this.volumeNotification = volumeNotification;
		this.volumeRing = volumeRing;
		this.volumeSystem = volumeSystem;
		this.volumeVoice = volumeVoice;
		this.networkPreference = networkPreference;
		this.stayOnWhilePluggedIn = stayOnWhilePluggedIn;
		this.usbMassStorageEnabled = usbMassStorageEnabled;
		this.wifi = wifi;
		this.wifi_ssid = wifi_ssid;
		this.locationMode3 = locationMode3;
		this.locationMode17 = locationMode17;
		this.serverSent = serverSent;
	}

	
	// getter
	public long getAirplaneMode() {
		return airplaneMode;
	}

	public long getBluetooth() {
		return bluetooth;
	}

	public long getDataRoaming() {
		return dataRoaming;
	}

	public long getDevelopmentSettingsEnabled() {
		return developmentSettingsEnabled;
	}

	public String getHttpProxy() {
		return httpProxy;
	}

	public String getModeRinger() {
		return modeRinger;
	}

	public int getVolumeAlarm() {
		return volumeAlarm;
	}

	public int getVolumeMusic() {
		return volumeMusic;
	}

	public int getVolumeNotification() {
		return volumeNotification;
	}

	public int getVolumeRing() {
		return volumeRing;
	}

	public int getVolumeSystem() {
		return volumeSystem;
	}

	public int getVolumeVoice() {
		return volumeVoice;
	}

	public String getNetworkPreference() {
		return networkPreference;
	}

	public String getStayOnWhilePluggedIn() {
		return stayOnWhilePluggedIn;
	}

	public long getUsbMassStorageEnabled() {
		return usbMassStorageEnabled;
	}

	public long getWifi() {
		return wifi;
	}
	
	public String getWifiSSID() {
		return wifi_ssid;
	}

	public String getLocationMode3() {
		return locationMode3;
	}

	public long getLocationMode17() {
		return locationMode17;
	}

	public boolean isServerSent() {
		return serverSent;
	}

	
	// setter
	public void setAirplaneMode(long airplaneMode) {
		this.airplaneMode = airplaneMode;
	}

	public void setBluetooth(long bluetooth) {
		this.bluetooth = bluetooth;
	}

	public void setDataRoaming(long dataRoaming) {
		this.dataRoaming = dataRoaming;
	}

	public void setDevelopmentSettingsEnabled(long developmentSettingsEnabled) {
		this.developmentSettingsEnabled = developmentSettingsEnabled;
	}

	public void setHttpProxy(String httpProxy) {
		this.httpProxy = httpProxy;
	}

	public void setModeRinger(String modeRinger) {
		this.modeRinger = modeRinger;
	}

	public void setVolumeAlarm(int volumeAlarm) {
		this.volumeAlarm = volumeAlarm;
	}

	public void setVolumeMusic(int volumeMusic) {
		this.volumeMusic = volumeMusic;
	}

	public void setVolumeNotification(int volumeNotification) {
		this.volumeNotification = volumeNotification;
	}

	public void setVolumeRing(int volumeRing) {
		this.volumeRing = volumeRing;
	}

	public void setVolumeSystem(int volumeSystem) {
		this.volumeSystem = volumeSystem;
	}

	public void setVolumeVoice(int volumeVoice) {
		this.volumeVoice = volumeVoice;
	}

	public void setNetworkPreference(String networkPreference) {
		this.networkPreference = networkPreference;
	}

	public void setStayOnWhilePluggedIn(String stayOnWhilePluggedIn) {
		this.stayOnWhilePluggedIn = stayOnWhilePluggedIn;
	}

	public void setUsbMassStorageEnabled(long usbMassStorageEnabled) {
		this.usbMassStorageEnabled = usbMassStorageEnabled;
	}

	public void setWifi(long wifi) {
		this.wifi = wifi;
	}
	
	public void setWifiSSID(String wifi_ssid) {
		this.wifi_ssid = wifi_ssid;
	}

	public void setLocationMode3(String locationMode3) {
		this.locationMode3 = locationMode3;
	}

	public void setLocationMode17(long locationMode17) {
		this.locationMode17 = locationMode17;
	}

	public void setServerSent(boolean serverSent) {
		this.serverSent = serverSent;
	}
			
}
