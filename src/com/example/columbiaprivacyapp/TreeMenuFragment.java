package com.example.columbiaprivacyapp;

import com.actionbarsherlock.app.SherlockFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class TreeMenuFragment extends SherlockFragment{

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.treemenu, container, false);
		//Auto-Complete
		AutoCompleteTextView autoView = (AutoCompleteTextView) view.findViewById(R.id.edit_message);
		String[] itemOptions = getResources().getStringArray(R.array.edit_message);
		
		
		ArrayAdapter<String> theAdapter = 
				new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, itemOptions);
		autoView.setAdapter(theAdapter);
		((BaseAdapter) autoView.getAdapter()).notifyDataSetChanged();
		((ViewGroup) autoView.getParent()).removeView(autoView);
		
		container.addView(autoView);

		return view;

		
	}

}
