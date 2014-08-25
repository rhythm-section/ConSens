package at.lukasmayerhofer.consens.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import at.lukasmayerhofer.consens.utils.HttpSend;


public class DatabaseBackupService extends Service {

	private static final String TAG = DatabaseBackupService.class.getSimpleName();
	private HttpSend sendDataToServer = null;
	
	
	// override methods
	@Override
	public void onCreate() {
		super.onCreate();
		
		sendDataToServer = new HttpSend(this);
		
		Log.d(TAG, "onCreate()");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if(sendDataToServer != null) {
    		Log.d(TAG, "sendDataToServer: " + sendDataToServer);
    		sendDataToServer.unregister();
    		sendDataToServer = null;
    	}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand(): " + intent + " / " + flags + " / " + startId);
				
    	sendDataToServer.register();
    	if(sendDataToServer.isConnected()) {
    		new HttpSend(this).execute();
    	}
		
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	
	public static void copyDatabaseToSdCard(Context context, File originalDatabaseFile, File sdDatabaseFile, String dbName) {
		Log.e(TAG, "*** Copy Database to public folder: START ***");
		
		try {
			originalDatabaseFile = new File(context.getDatabasePath(dbName).getPath());
			Toast.makeText(context, "Original:" + originalDatabaseFile, Toast.LENGTH_LONG).show();
			Log.d(TAG, "File-Path 1: " + originalDatabaseFile);

			if (originalDatabaseFile.exists()) {
				Log.d(TAG, "File 2 erstellen");
				sdDatabaseFile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/" + dbName);
				Toast.makeText(context, "Kopiert:" + sdDatabaseFile, Toast.LENGTH_LONG).show();
				Log.d(TAG, "File-Path 2: " + sdDatabaseFile);
				sdDatabaseFile.createNewFile();
				InputStream in = new FileInputStream(originalDatabaseFile);
				OutputStream out = new FileOutputStream(sdDatabaseFile);
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
			}
		} catch (FileNotFoundException error) {
			System.out.println(error.getMessage() + " in the specified directory.");
			System.exit(0);
		} catch (IOException error) {
			error.printStackTrace();
			System.out.println(error.getMessage());
		}
				
		Log.e(TAG, "*** Copy Database to public folder: END  ***");
	}

}
