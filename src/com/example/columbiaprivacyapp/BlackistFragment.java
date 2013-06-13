package com.example.columbiaprivacyapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeSet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

public class BlackistFragment extends SherlockFragment {
	protected ArrayAdapter<String> adapter;
	protected View view; 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		view = inflater.inflate(R.layout.blacklist, container, false);

		ListView listView = (ListView) view.findViewById(R.id.listview);

		//Making BlackList 
		TreeSet<BlacklistWord> theSet =  MainActivity.getInstance().datasource.GetAllWords();
		ArrayList<String> list = new ArrayList<String>();
		for(BlacklistWord i :theSet){
			System.out.println(i.getWord());
			list.add(i.getWord());
		}
		Collections.sort(list);
		adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,  list);
		listView.setAdapter(adapter);
		((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();

		if(view != null) { return view;}

		((ViewGroup) listView.getParent()).removeView(listView);
		container.addView(listView);

		return view;
	}
}
