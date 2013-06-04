package com.example.columbiaprivacyapp;

import java.util.TreeSet;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class TreeAdapter extends BaseAdapter{
	private TreeSet<String> mItems = new TreeSet<String>();
	private Context mContext;
	
	public TreeAdapter(Context context, TreeSet<String> items) {
	     mContext = context;
	     mItems = items;
	}
	
	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mItems; //TODO: Is this correct? 
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return null;
	}

}
