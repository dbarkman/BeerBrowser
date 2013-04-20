package com.realsimpleapps.beerbrowser;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class TitleSubtitleAdapter extends BaseAdapter implements SpinnerAdapter {

	private static final String tag = "TitleSubtitleArrayAdapter";

	private Context context;
	private int layoutResourceId;
	private ArrayList<String> titles;
	private ArrayList<String> subTitles;

	public TitleSubtitleAdapter(Context context, int layoutResourceId, ArrayList<String> titles, ArrayList<String> subTitles) {
		super();
		this.context = context;
		this.layoutResourceId = layoutResourceId;
		this.titles = titles;
		this.subTitles = subTitles;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		StringHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new StringHolder();
			holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);
			holder.txtSubTitle = (TextView)row.findViewById(R.id.txtSubTitle);

			row.setTag(holder);
		} else {
			holder = (StringHolder)row.getTag();
		}

		String title = titles.get(position);
		String subTitle = subTitles.get(position);

		holder.txtTitle.setText(title);
		holder.txtSubTitle.setText(subTitle);

		return row;
	}

	static class StringHolder {
		private TextView txtTitle;
		private TextView txtSubTitle;
	}

	@Override
	public int getCount() {
		return titles.size();
	}

	@Override
	public Object getItem(int position) {
		return titles.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
