package myapp.columbiaprivacyapp;

import java.util.List;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;

public class MapFrag extends SherlockFragment {
	protected ListView assocListView;
	protected View mapFragView;
	protected String[] assocArrayItems; 
	protected ArrayAdapter<String> mapAdapter;
	protected List assocList;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

		boolean ifPlay = MainActivity.getInstance().checkIfGooglePlay();

		if(ifPlay) {
			if (mapFragView != null) {
				ViewGroup parent = (ViewGroup) mapFragView.getParent();
				if (parent != null)
					parent.removeView(mapFragView);
			}
			try {
				mapFragView = inflater.inflate(R.layout.maptab, null, false);
			} catch (InflateException e) {
				/* map is already there, just return view as it is */
			}
		}


		

		//Creating MapFragment from SharedPreferences recently stored information 
		SharedPreferences tmpManager = MainActivity.getInstance().prefs;

		System.out.println("now within the Map Fragment...");
		String recLatitude = tmpManager.getString("recentLatitude", "default");
		String recLongitude = tmpManager.getString("recentLongitude", "default");
		String recWordAssoc = tmpManager.getString("wordAssociations", "default");

		//Testing 
		System.out.println(recLatitude);
		System.out.println(recLongitude);
		System.out.println(recWordAssoc);
		//Most Recent Items List
		String[] theList = null; 

		//Building list 
		buildList(theList, recWordAssoc);

		//TODO: ERROR HERE !!!!!!!!ClassCastException NoSaveStateFrameLayout 
		assocListView = (ListView) mapFragView.findViewById(R.id.map_frag_view);

		if(theList==null) {
			theList = new String[0];
		}
		mapAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,  theList);

		assocListView.setAdapter(mapAdapter);
		((BaseAdapter) assocListView.getAdapter()).notifyDataSetChanged();

		if(mapFragView != null) { return mapFragView;}

		setMapTransparent((ViewGroup)mapFragView);

		((ViewGroup) assocListView.getParent()).removeView(assocListView);

		container.addView(assocListView);
		container.addView(mapFragView);

		//Now need to add Google API's Map Fragment 

		return container;
	}

	
	private String[] buildList(String[] theList, String recWordAssoc) {
		if(!recWordAssoc.equals("default")) {
			if(recWordAssoc.charAt(1)!=']') {
				if(recWordAssoc.length()>0) {
					recWordAssoc = recWordAssoc.substring(1, recWordAssoc.length()-1);
					System.out.println(recWordAssoc);
					theList = recWordAssoc.split("\", ");
					for(int i=0; i< theList.length; i++) {
						theList[i] = theList[i].substring(1).toLowerCase();
						if(i==theList.length-1) theList[i]=theList[i].substring(0, theList[i].length()-1);
					}
				}
			}
		}
		return theList; 
	}

	private void setMapTransparent(ViewGroup group) {
		int childCount = group.getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = group.getChildAt(i);
			if (child instanceof ViewGroup) {
				setMapTransparent((ViewGroup) child);
			} else if (child instanceof SurfaceView) {
				child.setBackgroundColor(Color.TRANSPARENT);
			}
		}
	}
}

