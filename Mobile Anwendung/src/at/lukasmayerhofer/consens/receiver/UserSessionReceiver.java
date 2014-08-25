package at.lukasmayerhofer.consens.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class UserSessionReceiver extends BroadcastReceiver {

	private static final String TAG = UserSessionReceiver.class.getSimpleName();
	
	private boolean screenOn;
	private boolean userPresent;
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive()");
		
		if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			Log.d(TAG, "ACTION_SCREEN_OFF");
			screenOn = false;
			userPresent = false;
		} else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
			Log.d(TAG, "ACTION_SCREEN_ON");
			screenOn = true;
			userPresent = false;
		} else if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
			Log.d(TAG, "ACTION_USER_PRESENT");
			userPresent = true;
        }
		
		Intent userSession = new Intent(context, at.lukasmayerhofer.consens.services.UserSessionService.class);
		userSession.putExtra("screen_state", screenOn);
		userSession.putExtra("user_state", userPresent);
		context.startService(userSession);
	}

}
