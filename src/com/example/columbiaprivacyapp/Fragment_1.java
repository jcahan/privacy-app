package com.example.columbiaprivacyapp;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.example.columbiaprivacyapp.MainActivity.BusProvider;
import com.squareup.otto.Subscribe;

public class Fragment_1 extends SherlockFragment {

	@Subscribe public void getList(ArrayList<String> theList) {
		System.out.println("18: " + theList.toString());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		
		
		//		ListView myView = MainActivity.getInstance().getListView();
		//		myView.setAdapter(MainActivity.getInstance().adapter);
		//		((BaseAdapter) myView.getAdapter()).notifyDataSetChanged();
		//		return inflater.inflate(R.layout.blacklist, myView, false);
		
		
		
		return inflater.inflate(R.layout.blacklist, container, false);
	}
	@Override public void onResume() {
		super.onResume();
		// Register ourselves so that we can provide the initial value.
		BusProvider.getInstance().register(this);
		Bundle bundle = this.getArguments();
		ArrayList<String> theList = bundle.getStringArrayList("theList");

	}

	@Override public void onPause() {
		super.onPause();

		// Always unregister when an object no longer should be on the bus.
		BusProvider.getInstance().unregister(this);
	}
}
