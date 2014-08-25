package at.lukasmayerhofer.consens.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.AbstractWindowedCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.CursorWrapper;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import at.lukasmayerhofer.consens.contentprovider.ConsensContentProvider;
import at.lukasmayerhofer.consens.usage.SystemInfoData;


public class HttpSend extends AsyncTask<String, Void, String> {
	
	private static final String TAG = HttpSend.class.getSimpleName();
	private static final String SYSTEM_PREFERENCES = "at.lukasmayerhofer.consens.SYSTEM_PREFERENCES";
	
//	private static final String DEVICES_URL = "http://10.0.0.2:3000/api/devices.json";
	private static final String DEVICES_URL = "http://consens.herokuapp.com/api/devices.json";
	private static final int UPDATE_ITEM_COUNT = 300;
	
	private Context mContext;
	private JSONObject postData = null;
	private NetworkConnectionManager mNetworkConnectionManager;
	
	
	// constructor
	public HttpSend(Context context) {
		mContext = context;
	}
	
	public void register() {
		mNetworkConnectionManager = new NetworkConnectionManager(mContext);
		
		if(mNetworkConnectionManager.isConnected()) {
			Toast.makeText(mContext, "ConSens: Connected. Sending Data.", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(mContext, "ConSens: Internet disconnected. Please re-connect.", Toast.LENGTH_LONG).show();
		}
	}
	
	public void unregister() {
		mNetworkConnectionManager = null;
	}
	
	public boolean isConnected() {
		return mNetworkConnectionManager.isConnected();
	}
	
	
	// override methods
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		// get activity data as JSONArray
		ContentResolver mContentResolver = mContext.getContentResolver();
		
		postData = new JSONObject();
		JSONObject device = new JSONObject();
		
		try {
			Log.d(TAG, "Set System Info Data (only one time)");
			device.put("uuid", SystemInfoData.getUUID(mContext));
			
			// get and save system info
			JSONObject system_info = new JSONObject();
			system_info.put("os_version", SystemInfoData.getSystemInfoOsVersion());
			system_info.put("model", SystemInfoData.getSystemInfoModel());
			system_info.put("product", SystemInfoData.getSystemInfoProduct());
			system_info.put("manufacturer", SystemInfoData.getSystemInfoManufacturer());
			system_info.put("brand", SystemInfoData.getSystemInfoBrand());
			system_info.put("android_id", SystemInfoData.getSystemInfoAndroidId());
			// get and save user name
			SharedPreferences systemPreferences = null;
			systemPreferences = mContext.getSharedPreferences(SYSTEM_PREFERENCES, Context.MODE_PRIVATE);
			system_info.put("user_name", systemPreferences.getString("user_name", "-1"));
			device.put("system_info", system_info);
			
			
			Cursor appSessionCursor = mContentResolver.query(ConsensContentProvider.APP_SESSION_CONTENT_URI, null, null, null, "_id ASC ");
			getJSON(postData, device, appSessionCursor, "app_sessions");
			appSessionCursor.close();
			
			Cursor appUsageCursor = mContentResolver.query(ConsensContentProvider.APP_USAGE_CONTENT_URI, null, null, null, "_id ASC ");
			getJSON(postData, device, appUsageCursor, "app_usages");
			appUsageCursor.close();
			
			Cursor systemSettingsCursor = mContentResolver.query(ConsensContentProvider.SYSTEM_SETTINGS_CONTENT_URI, null, null, null, "_id ASC ");
			getJSON(postData, device, systemSettingsCursor, "system_settings");
			systemSettingsCursor.close();
			
			Cursor activityCursor = mContentResolver.query(ConsensContentProvider.ACTIVITIES_CONTENT_URI, null, null, null, "_id ASC ");
			getJSON(postData, device, activityCursor, "user_activities");
			activityCursor.close();
			
			Cursor locationCursor = mContentResolver.query(ConsensContentProvider.LOCATIONS_CONTENT_URI, null, null, null, "_id ASC ");
			getJSON(postData, device, locationCursor, "user_locations");
			locationCursor.close();
			
			longInfo(device.toString());
			
			postData.accumulate("device", device);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static void longInfo(String str) {
	    if(str.length() > 4000) {
	        Log.i(TAG, str.substring(0, 4000));
	        longInfo(str.substring(4000));
	    } else
	        Log.i(TAG, str);
	}
	
	@Override
	protected String doInBackground(String... str) {		
		return sendData(DEVICES_URL, postData);
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		ContentResolver mContentResolver = mContext.getContentResolver();
		
		Log.d(TAG, "Result: " + result);
				
		if(result.equals("data successfully added")) {			
			markAsSent(mContentResolver.query(ConsensContentProvider.APP_SESSION_CONTENT_URI, null, null, null, null), "app_session");
			markAsSent(mContentResolver.query(ConsensContentProvider.APP_USAGE_CONTENT_URI, null, null, null, null), "app_usage");
			markAsSent(mContentResolver.query(ConsensContentProvider.SYSTEM_SETTINGS_CONTENT_URI, null, null, null, null), "system_settings");
			markAsSent(mContentResolver.query(ConsensContentProvider.LOCATIONS_CONTENT_URI, null, null, null, null), "locations");
			markAsSent(mContentResolver.query(ConsensContentProvider.ACTIVITIES_CONTENT_URI, null, null, null, null), "activities");
		}
		
		Toast.makeText(mContext, "ConSens: " + result, Toast.LENGTH_LONG).show();
	}
	
	
	// save SQLite data to JSONObject
	@SuppressWarnings("deprecation")
	private JSONObject getJSON(JSONObject postData, JSONObject device, Cursor cursor, String tableName) {
		JSONArray outer = new JSONArray();
		JSONObject inner = null;
		int jsonArrayCounter = 0;
		int serverSentColumnIndex = -1;
		int serverSent = 0;
		int startingPoint = 0;
		int itemCounter = 0;
		boolean everythingSent = false;
		
		if(cursor == null) {
			Log.w(TAG, "cursor=null / no data found (SQLite consens.db)");
			return null;
		}
		
//		String[] columnNames = cursor.getColumnNames();
//		for(String columnName : columnNames) {
//			Log.d(TAG, "Column Name: " + columnName);
//		}
//		Log.d(TAG, "Count: " + cursor.getColumnCount());
		
		// iterate over all items until server_sent = 0
		while (cursor.moveToNext()) {
			serverSentColumnIndex = cursor.getColumnIndex("server_sent");
			if(serverSentColumnIndex != -1) {
				serverSent = cursor.getInt(serverSentColumnIndex);
				if(serverSent == 0) {
					startingPoint = cursor.getPosition() - 1;
					break;
				} else {
					if(cursor.isLast()) {
						// check if cursor is already on last position
						everythingSent = true;
					}
				}
			}
		}
		
		
		cursor.moveToPosition(startingPoint);
		while (!everythingSent && cursor.moveToNext() && itemCounter < UPDATE_ITEM_COUNT) {
			Log.d(TAG, tableName);
			inner = new JSONObject();
			
		    for(int i = 1; i < cursor.getColumnCount(); i++) {
		    	int type = 0;
		    	
		    	// get column types for devices with SDK < 11
				CursorWindow cursorWindow = null;
		        int cursorPosition = 0;
		        if (android.os.Build.VERSION.SDK_INT < 11) {
		        	CursorWrapper cw = new CursorWrapper(cursor);
		            Class<?> cursorWrapper = CursorWrapper.class;
		            Field mCursor = null;
		            try {
		            	mCursor = cursorWrapper.getDeclaredField("mCursor");
		                mCursor.setAccessible(true);
		                AbstractWindowedCursor abstractWindowedCursor = (AbstractWindowedCursor)mCursor.get(cw);
		                cursorWindow = abstractWindowedCursor.getWindow();
		                cursorPosition = abstractWindowedCursor.getPosition();
		                
		                int pos = abstractWindowedCursor.getPosition();
		                for ( int j = 0; j < cursor.getColumnCount(); j++ ) {
		                    if (cursorWindow.isNull(pos, i)) {
		                        type = 0;									// Cursor.FIELD_TYPE_NULL
		                    } else if (cursorWindow.isLong(pos, i)) {
		                        type = 1;									// Cursor.FIELD_TYPE_INTEGER
		                    } else if (cursorWindow.isFloat(pos, i)) {
		                        type = 2;									// Cursor.FIELD_TYPE_FLOAT
		                    } else if (cursorWindow.isString(pos, i)) {
		                        type = 3;									// Cursor.FIELD_TYPE_STRING
		                    } else if (cursorWindow.isBlob(pos, i)) {
		                        type = 4;									// Cursor.FIELD_TYPE_BLOB
		                    }
		                }
		            } catch (NoSuchFieldException e) {
		                e.printStackTrace();
		            } catch (IllegalArgumentException e) {
		                e.printStackTrace();
		            } catch (IllegalAccessException e) {
		                e.printStackTrace();
		            }
		        } else {
		        	type = cursor.getType(i);
		        }
		    	
		    	try {
			    	switch(type) {
			    		case Cursor.FIELD_TYPE_NULL:
							inner.accumulate(cursor.getColumnName(i), null);
			    			break;
			    		case Cursor.FIELD_TYPE_INTEGER:
			    			inner.accumulate(cursor.getColumnName(i), cursor.getInt(i));
			    			break;
			    		case Cursor.FIELD_TYPE_FLOAT:
			    			inner.accumulate(cursor.getColumnName(i), cursor.getFloat(i));
			    			break;
			    		case Cursor.FIELD_TYPE_STRING:
			    			inner.accumulate(cursor.getColumnName(i), cursor.getString(i));
			    			break;
			    		case Cursor.FIELD_TYPE_BLOB:
			    			inner.accumulate(cursor.getColumnName(i), cursor.getBlob(i));
			    			break;
			    	}
			    	
		    	} catch (JSONException e) {
					e.printStackTrace();
				}
		    }
		    
		    try {
		    	outer.put(jsonArrayCounter, inner);
		    	jsonArrayCounter++;
		    } catch (JSONException e) {
				e.printStackTrace();
			}
		    
		    itemCounter++;
		}
		
		if(inner != null) {
			try {
				device.put(tableName, outer);
			} catch (JSONException error) {
				error.printStackTrace();
			}
		}
		
		return postData;
	}
	
	
	// send post data to server
	private String sendData(String url, JSONObject postData) {
		InputStream mInputStream = null;
		String result = "";
		
		try {
			HttpClient mHttpClient = new DefaultHttpClient();
			HttpPost mHttpPost = new HttpPost(url);
			
			// create and get StringEntity
			mHttpPost.setEntity(getStringEntity(postData));

			// set http headers
			mHttpPost.setHeader("Accept", "application/at.lukasmayerhofer.consens-v1");
			mHttpPost.setHeader("Content-type", "application/json");
			
			// execute post request
			HttpResponse mHttpResponse = mHttpClient.execute(mHttpPost);
			mInputStream = mHttpResponse.getEntity().getContent();
			
			if(mInputStream != null) {
				result = convertInputStreamToString(mInputStream);
			} else {
				result = "Error";
			}
		} catch(Exception error) {
			Log.d(TAG, error.getLocalizedMessage());
		}
		
		return result;
	}
	
	
	// create HTTPPostRequest
	private StringEntity getStringEntity(JSONObject mData) {
		String jsonString;
		StringEntity mStringEntity = null;
		
		try {
			if(mData != null) {
				Log.d(TAG, "String Entity: " + mData);
				jsonString = mData.toString();
			} else {
				JSONObject jsonObject = new JSONObject();
				jsonObject.accumulate("error", "There was no SQLite data");
				jsonString = jsonObject.toString();
			}
			
			// set httpPost entitiy
			mStringEntity = new StringEntity(jsonString);
		} catch(Exception error) {
			Log.d(TAG, error.getLocalizedMessage());
		}
		
		return mStringEntity;
	}
	
	
	// convert InputStrem to String
	private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        
        String line = "";
        String result = "";
        
        while((line = bufferedReader.readLine()) != null) {
        	result += line;
        }
 
        inputStream.close();
        
        return result;
    }
	
	private void markAsSent(Cursor cursor, String type) {
		int serverSentColumnIndex = -1;
		int serverSent = 0;
		int startingPoint = 0;
		
		// iterate over all items until server_sent = 0
		while (cursor.moveToNext()) {
			serverSentColumnIndex = cursor.getColumnIndex("server_sent");
			if(serverSentColumnIndex != -1) {
				serverSent = cursor.getInt(serverSentColumnIndex);
				if(serverSent == 0) {
					startingPoint = cursor.getPosition();
//					Log.w(TAG, "STARTING POINT: " + startingPoint);
					break;
				}
			}
		}
		
		// check sended items as server_sent  = 1
		ContentResolver mContentResolver = mContext.getContentResolver();
		ContentValues values = new ContentValues();
		values.put("server_sent", true);
		
		if(type.compareTo("app_session") == 0) {
			mContentResolver.update(ConsensContentProvider.APP_SESSION_CONTENT_URI, values, "_id BETWEEN " + startingPoint + " AND " + (startingPoint + UPDATE_ITEM_COUNT), null);
		} else if(type.compareTo("app_usage") == 0) {
			mContentResolver.update(ConsensContentProvider.APP_USAGE_CONTENT_URI, values, "_id BETWEEN " + startingPoint + " AND " + (startingPoint + UPDATE_ITEM_COUNT), null);
		} else if(type.compareTo("system_settings") == 0) {
			mContentResolver.update(ConsensContentProvider.SYSTEM_SETTINGS_CONTENT_URI, values, "_id BETWEEN " + startingPoint + " AND " + (startingPoint + UPDATE_ITEM_COUNT), null);
		} else if(type.compareTo("locations") == 0) {
			mContentResolver.update(ConsensContentProvider.LOCATIONS_CONTENT_URI, values, "_id BETWEEN " + startingPoint + " AND " + (startingPoint + UPDATE_ITEM_COUNT), null);
		} else if(type.compareTo("activities") == 0) {
			mContentResolver.update(ConsensContentProvider.ACTIVITIES_CONTENT_URI, values, "_id BETWEEN " + startingPoint + " AND " + (startingPoint + UPDATE_ITEM_COUNT), null);
		}
	}
	
}
