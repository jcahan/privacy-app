package myapp.columbiaprivacyapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import com.actionbarsherlock.app.SherlockFragment;
import myapp.columbiaprivacyapp.R;
import com.google.android.gms.maps.GoogleMap;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
		mapFragView = inflater.inflate(R.layout.maptab, container, false);
		
		//Creating MapFragment from SharedPreferences recently stored information 
		SharedPreferences tmpManager = MainActivity.getInstance().prefs;


		System.out.println("now within the Map Fragment...");
		String recLatitude = tmpManager.getString("recentLatitude", "default");
		String recLongitude = tmpManager.getString("recentLongitude", "default");
		String recWordAssoc = tmpManager.getString("wordAssociations", "default");

		System.out.println(recLatitude);
		System.out.println(recLongitude);
		System.out.println(recWordAssoc);

		//Most Recent Items List
		String[] theList; 

		//TODO:See if you can abstract out method from MainActivity
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
				else return null; 
			}
			else return null; 
		}
		else {
			return null; 
		}

		assocListView = (ListView) mapFragView.findViewById(R.id.map_frag_view);

		mapAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,  theList);

		assocListView.setAdapter(mapAdapter);
		((BaseAdapter) assocListView.getAdapter()).notifyDataSetChanged();

		if(mapFragView != null) { return mapFragView;}

		((ViewGroup) assocListView.getParent()).removeView(assocListView);
		container.addView(assocListView);

		//Now need to add Google API's Map Fragment 
		



		return mapFragView;
	}

}
