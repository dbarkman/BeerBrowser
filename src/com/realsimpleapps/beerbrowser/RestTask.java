package com.realsimpleapps.beerbrowser;

import java.net.URI;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class RestTask extends AsyncTask<Map, Void, String> { 

	private static String tag = "RestTask";

	public static final String httpResponse = "httpResponse";

	private SharedPreferences apiResultStorage;
	private SharedPreferences.Editor apiResultStorageEditor;

	private Context context;
	private String action; 
	private String apiService;

	public RestTask(Context context, String action, String apiService) {
		this.context = context;
		this.action = action;
		this.apiService = apiService;

		apiResultStorage = context.getSharedPreferences("displayPreferences", Context.MODE_PRIVATE);
		apiResultStorageEditor = apiResultStorage.edit();
	} 

	@Override 
	protected String doInBackground(Map... params) {

		String bdbAPIKey = context.getString(R.string.bdbAPIKey);
		String apiUri = context.getString(R.string.apiURI) + apiService + "?key=" + bdbAPIKey;
		if (action.equals("com.realsimpleapps.beerbrowser.getBeersForStyle")) {
			apiUri = context.getString(R.string.apiURI) + apiService + "&key=" + bdbAPIKey;
		}
		
		return getAPIData(apiUri);
	}
	
	private String getAPIData(String apiUri) {
		Log.i(tag, "Processing: " + apiUri);

		String response = "";

		HttpClient client = new DefaultHttpClient();
		HttpGet apiRequest;
		try {
			apiRequest = new HttpGet(new URI(apiUri));

			HttpResponse serverResponse = client.execute(apiRequest); 
			BasicResponseHandler handler = new BasicResponseHandler(); 
			response = handler.handleResponse(serverResponse);

			String storageKey = "";
			if (action.equals("com.realsimpleapps.beerbrowser.getAllStyles")) {
				storageKey = "getAllStyles";
			} else if (action.equals("com.realsimpleapps.beerbrowser.getBeersForStyle")) {
				storageKey = "getBeersForStyle";
			}

			apiResultStorageEditor.putString(storageKey, response);
			apiResultStorageEditor.commit();
			response = "connectionSucceeded";
		} catch (Exception e) {
			Log.e(tag, "Couldn't make HTTP call: " + e.getMessage());
			response = "connectionFailed";
		}
		
		return response;
	}

	@Override 
	protected void onPostExecute(String result) {
		Intent intent = new Intent(action); 
		intent.putExtra(httpResponse, result); 
		//Broadcast the completion 
		context.sendBroadcast(intent); 
	} 
} 