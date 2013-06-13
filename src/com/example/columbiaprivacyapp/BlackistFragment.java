package com.example.columbiaprivacyapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeSet;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;

public class BlackistFragment extends SherlockFragment {
	protected ArrayAdapter<String> adapter;
	protected View view; 
	protected ArrayList<String> list; 
	protected ListView listView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		view = inflater.inflate(R.layout.blacklist, container, false);

		listView = (ListView) view.findViewById(R.id.listview);

		//Making BlackList 
		TreeSet<BlacklistWord> theSet =  MainActivity.getInstance().datasource.GetAllWords();
		list = new ArrayList<String>();
		for(BlacklistWord i :theSet){
			System.out.println(i.getWord());
			list.add(i.getWord());
		}
		Collections.sort(list);
		adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,  list);
		listView.setAdapter(adapter);
		((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();

		//TODO: Pick up here 
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.i("ItemListener","Item should be deleted");
				String theWord = list.get(position);
				MainActivity.getInstance().datasource.deleteStringWord(theWord);
				list.remove(theWord);
				adapter.notifyDataSetChanged();
				
				return true; 
			}

		});

		if(view != null) { return view;}

		((ViewGroup) listView.getParent()).removeView(listView);
		container.addView(listView);

		return view;
	}
}
