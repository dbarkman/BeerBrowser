package com.realsimpleapps.beerbrowser;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockActivity;

public class BeerStylesActivity extends SherlockActivity {
	
	private final String tag = "MainActivity";

	private SharedPreferences apiResultStorage;

	private Context context;
	private ProgressDialog progress;

	private final String getAllStylesApiAction = "com.realsimpleapps.beerbrowser.getAllStyles";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beer_styles);
        
        context = this;
        
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        
        this.registerReceiver(getAllStylesReceiver, new IntentFilter(getAllStylesApiAction));

		fetchStyles();

		apiResultStorage = this.getSharedPreferences("displayPreferences", MODE_PRIVATE);
    }

	private void fetchStyles() {
		progress = ProgressDialog.show(this, null, "Fetching Beer Styles", true); 
		RestTask task = new RestTask(this, getAllStylesApiAction, "styles"); 
		task.execute();
	}

	private BroadcastReceiver getAllStylesReceiver = new BroadcastReceiver() { 
		@Override 
		public void onReceive(Context context, Intent intent) {

			if (progress != null) {
				progress.dismiss();
				progress = null;
			}

			String apiResult = intent.getStringExtra(RestTask.httpResponse);

			if (apiResult.equalsIgnoreCase("connectionFailed")) {
				Log.e(tag, "Connection Failed on Styles");
				String result = apiResultStorage.getString("getAllStyles", "");
				if (result.length() > 0) {
					updateBeerStyles(result);
				}
			} else {
				String result = apiResultStorage.getString("getAllStyles", "");
				updateBeerStyles(result);
			}
		}
	};
	
	private void updateBeerStyles(String apiResult)
	{
		try {
			JSONObject apiDataObject = (new JSONObject(apiResult));
			final JSONArray apiDataArray = apiDataObject.getJSONArray("data");
			
			ArrayList<String> beerStyleNameList = new ArrayList<String>();
			ArrayList<String> beerStyleCategoryNameList = new ArrayList<String>();
			beerStyleNameList.add("No Style Selected");
			beerStyleCategoryNameList.add("pick one");
			
			int apiDataArraySize = apiDataArray.length();
			for (int i = 0; i < apiDataArraySize; i++) {
				String name = apiDataArray.getJSONObject(i).getString("name");
				String category = apiDataArray.getJSONObject(i).getJSONObject("category").getString("name");
				beerStyleNameList.add(name);
				beerStyleCategoryNameList.add(category);
			}
			Spinner sp = (Spinner)findViewById(R.id.beerStylesSpinner);
			TitleSubtitleAdapter tstAdapter = new TitleSubtitleAdapter(this, R.layout.title_subtitle_item, beerStyleNameList, beerStyleCategoryNameList);
			sp.setAdapter(tstAdapter);
			
			sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
				@Override
				public void onItemSelected(AdapterView<?> adapter, View v, int position, long lng) {
					if (position > 0) {
						position--;
						try {
							int styleId = apiDataArray.getJSONObject(position).getInt("id");
							Intent intent = new Intent(context, BeersActivity.class);
							intent.putExtra("styleId", styleId);
							startActivity(intent);
						} catch (JSONException je) {
							Log.e(tag, "Could not get ID for style");
						}
					}
				}
				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
		} catch (JSONException je) {
			Log.e(tag, "Could not parse Styles API result");
		}
	}
}
