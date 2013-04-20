package com.realsimpleapps.beerbrowser;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SectionedArrayAdapter extends ArrayAdapter<String> {

	private static final String tag = "SectionedArrayAdapter";

	private Context context;
	private int layoutResourceId;
	private ArrayList<String> beers;

	private static final int STATE_UNKNOWN = 0;
	private static final int STATE_SECTIONED_CELL = 1;
	private static final int STATE_REGULAR_CELL = 2;

	private int[] rowStates;

	public SectionedArrayAdapter(Context context, int layoutResourceId, ArrayList<String> beers) {
		super(context, layoutResourceId, beers);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
		this.beers = beers;

		this.rowStates = new int[beers.size()];
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		rowStates = new int[beers.size()];
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		StringHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new StringHolder();
			holder.separator = (TextView)row.findViewById(R.id.separator);
			holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);

			if (position == 2) holder.separator.setVisibility(View.GONE);

			row.setTag(holder);
		} else {
			holder = (StringHolder)row.getTag();
		}

		String filter;
		String beer;
		try {
			JSONObject beerData = (new JSONObject(beers.get(position)));
			filter = beerData.getString("filter");
			beer = beerData.getString("beerName");
		} catch (JSONException e) {
			Log.e(tag, "Couldn't parse JSON result: " + e.getMessage());
			filter = "";
			beer = "";
		}

		boolean needSeparator = false;

		switch (rowStates[position]) {
		case STATE_SECTIONED_CELL:
			needSeparator = true;
			break;

		case STATE_REGULAR_CELL:
			needSeparator = false;
			break;

		case STATE_UNKNOWN:
		default:
			if (position == 0) {
				needSeparator = true;
			} else {
				String lastFilter;
				try {
					JSONObject beerData = (new JSONObject(beers.get(position - 1)));
					lastFilter = beerData.getString("filter");
				} catch (JSONException e) {
					Log.e(tag, "Couldn't parse JSON result: " + e.getMessage());
					lastFilter = "";
				}
				if (!filter.equals(lastFilter)) needSeparator = true;
			}

			rowStates[position] = (needSeparator) ? STATE_SECTIONED_CELL : STATE_REGULAR_CELL;
			break;
		}

		if (needSeparator) {
			holder.separator.setVisibility(View.VISIBLE);
			holder.separator.setText(filter);
		} else {
			holder.separator.setVisibility(View.GONE);
		}

		holder.txtTitle.setText(beer);

		return row;
	}

	static class StringHolder {
		private TextView separator;
		private TextView txtTitle;
	}
}
