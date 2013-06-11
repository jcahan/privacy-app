package com.example.columbiaprivacyapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;

public class Fragment_1 extends SherlockFragment{
//	private ListView listView;

	//	@Override 
	//	public void onCreate(Bundle savedInstanceState) {
	//		super.onCreate(savedInstanceState);
	//	}
	//	@Override
	//	public void onActivityCreated(Bundle savedInstanceState) {
	//		super.onActivityCreated(savedInstanceState);
	////		setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, list));
	//		
	//	}




	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		return inflater.inflate(R.layout.blacklist, container, false);
	}

}

