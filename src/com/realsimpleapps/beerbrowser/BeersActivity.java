package com.realsimpleapps.beerbrowser;

import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListActivity;

public class BeersActivity extends SherlockListActivity {
	
	private final String tag = "MainActivity";

	private SharedPreferences apiResultStorage;

	private ProgressDialog progress;
	
	private String styleId;

	private final String getBeersForStyleApiAction = "com.realsimpleapps.beerbrowser.getBeersForStyle";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        
		this.registerReceiver(getBeersForStyleReceiver, new IntentFilter(getBeersForStyleApiAction));

		Bundle extras = this.getIntent().getExtras();
		if (extras != null) {
			styleId = Integer.toString(extras.getInt("styleId"));
		}		

		fetchBeers();

		apiResultStorage = this.getSharedPreferences("displayPreferences", MODE_PRIVATE);
    }

	private void fetchBeers() {
		progress = ProgressDialog.show(this, null, "Fetching Beers!", true); 
		RestTask task = new RestTask(this, getBeersForStyleApiAction, "beers?styleId=" + styleId); 
		task.execute();
	}

	private BroadcastReceiver getBeersForStyleReceiver = new BroadcastReceiver() { 
		@Override 
		public void onReceive(Context context, Intent intent) {

			if (progress != null) {
				progress.dismiss();
				progress = null;
			}

			String apiResult = intent.getStringExtra(RestTask.httpResponse);

			if (apiResult.equalsIgnoreCase("connectionFailed")) {
				Log.e(tag, "Connection Failed on Beers");
				String result = apiResultStorage.getString("getBeersForStyle", "");
				if (result.length() > 0) {
					updateBeers(result);
				}
			} else {
				String result = apiResultStorage.getString("getBeersForStyle", "");
				updateBeers(result);
			}
		}
	};

	private void updateBeers(String apiResult)
	{
		ArrayList<String> beersToSort = new ArrayList<String>();
		ArrayList<String> beers = new ArrayList<String>();

		try {
			JSONObject apiDataObject = (new JSONObject(apiResult));
			final JSONArray apiDataArray = apiDataObject.getJSONArray("data");
			
			int apiDataArraySize = apiDataArray.length();
			for (int i = 0; i < apiDataArraySize; i++) {
				String name = apiDataArray.getJSONObject(i).getString("name");
				beersToSort.add(name);
			}
			
			Collections.sort(beersToSort);
			
			for (String beer : beersToSort) {
				String jsonString = "{\"filter\":\"" + beer.substring(0, 1) + "\",\"beerName\":\"" + beer + "\"}";
				beers.add(jsonString);
			}
			
			SectionedArrayAdapter adapter = new SectionedArrayAdapter(this, R.layout.sectioned_list_item, beers);
			setListAdapter(adapter);

			ListView lv = getListView();
			lv.setDivider(new ColorDrawable(this.getResources().getColor(R.color.darkGray)));
			lv.setDividerHeight(1);
		} catch (JSONException je) {
			Log.e(tag, "Could not parse Beers API result");
		}
	}
}
