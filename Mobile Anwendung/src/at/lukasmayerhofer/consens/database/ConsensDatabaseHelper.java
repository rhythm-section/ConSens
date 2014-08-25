package at.lukasmayerhofer.consens.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class ConsensDatabaseHelper extends SQLiteOpenHelper {

	private static final String TAG = ConsensDatabaseHelper.class.getSimpleName();
	
	private static final String DATABASE_NAME = "consens.db";
	private static final int DATABASE_VERSION = 2;		// added app usage timestamps
	
	
	public ConsensDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		Log.d(TAG, "ConsensDatabaseHelper()");
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		Log.d(TAG, "onCreate()");
		AppSessionModel.onCreate(database);
		AppUsageModel.onCreate(database);
		SystemSettingsModel.onCreate(database);
		ActivityModel.onCreate(database);
		LocationModel.onCreate(database);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.d(TAG, "onUpgrade()");
		AppSessionModel.onUpgrade(database, oldVersion, newVersion);
		AppUsageModel.onUpgrade(database, oldVersion, newVersion);
		SystemSettingsModel.onUpgrade(database, oldVersion, newVersion);
		ActivityModel.onUpgrade(database, oldVersion, newVersion);
		LocationModel.onUpgrade(database, oldVersion, newVersion);
	}
	
	
	public String getDatabaseName() {
		return DATABASE_NAME;
	}
}
