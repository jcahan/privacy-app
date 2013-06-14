package com.example.columbiaprivacyapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.gms.maps.GoogleMap;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemLongClickListener;

public class MapFrag extends SherlockFragment {
	protected ListView assocListView;
	protected View mapFragView;
	protected String[] assocArrayItems; 
	protected ArrayAdapter<String> mapAdapter;
	protected List assocList;
	//TODO: Need to use SQLite, instead of just getInstance()....
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		//Creating MapFragment from recentLatitude and recentLongitude
		

		//Most Recent Items List

		mapFragView = inflater.inflate(R.layout.maptab, container, false);
		assocListView = (ListView) mapFragView.findViewById(R.id.map_frag_view);
		assocArrayItems = MainActivity.getInstance().recLocAssociations;
		
		if(assocArrayItems==null) return null; 
		assocList = Arrays.asList(assocArrayItems);

		mapAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,  assocList);

		assocListView.setAdapter(mapAdapter);
		((BaseAdapter) assocListView.getAdapter()).notifyDataSetChanged();

		if(mapFragView != null) { return mapFragView;}

		((ViewGroup) assocListView.getParent()).removeView(assocListView);
		container.addView(assocListView);

		return mapFragView;
	}

}
