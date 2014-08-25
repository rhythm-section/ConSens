package at.lukasmayerhofer.consens.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import at.lukasmayerhofer.consens.services.ActivityRecognitionIntentService;
import at.lukasmayerhofer.consens.services.LocationIntentService;
import at.lukasmayerhofer.consens.utils.GooglePlayServicesManager;


public class BootReceiver extends BroadcastReceiver {

	private static final String TAG = BootReceiver.class.getSimpleName();
	
	private PendingIntent makeBackupAlarm;
	private AlarmManager backupAlarmManager;
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive()");
		
		if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Log.d(TAG, "ACTION_BOOT_COMPLETED");
			
			GooglePlayServicesManager mGooglePlayServicesManager = new GooglePlayServicesManager(context.getApplicationContext());
			
			// start logging location
			context.startService(new Intent(context, LocationIntentService.class));
        	mGooglePlayServicesManager.startLocationUpdates();
        	
        	// start logging activities
        	context.startService(new Intent(context, ActivityRecognitionIntentService.class));
        	mGooglePlayServicesManager.startActivityUpdates();
        	
        	// start logging user session
        	context.startService(new Intent(context, at.lukasmayerhofer.consens.services.UserSessionService.class));

        	// start database backup service (send data to server)
        	Intent makeBackup = new Intent(context, at.lukasmayerhofer.consens.services.DatabaseBackupService.class);
        	makeBackupAlarm = PendingIntent.getService(context, 0, makeBackup, 0);        	
        	backupAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        	backupAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, 1000*60*1, 1000*60*60*2, makeBackupAlarm);
//        	backupAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, 1000*60*1, 1000*60*1, makeBackupAlarm);
		}
	}
	
}