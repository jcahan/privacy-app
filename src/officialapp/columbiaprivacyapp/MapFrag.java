package officialapp.columbiaprivacyapp;

import officialapp.columbiaprivacyapp.R;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFrag extends SherlockFragment {
	private ListView assocListView;
	private View mapFragView;
	private String[] assocArrayItems; 
	private ArrayAdapter<String> mapAdapter;
	private MapView mapView; 

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

		//Checks if GooglePlayService is On--> Should take this out once first check forces user to have it 
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
			SupportMapFragment mapFrag= (SupportMapFragment)getSherlockActivity().getSupportFragmentManager().findFragmentById(R.id.map);
			if(mapFrag!=null)  {
				GoogleMap googleMap = mapFrag.getMap();
				if(googleMap!=null){
					LatLng theLatLng = makeNewLatLng();
					if(theLatLng!=null) {
						CameraUpdate update = CameraUpdateFactory.newLatLngZoom(theLatLng, 13);
						googleMap.animateCamera(update);
						googleMap.addMarker(new MarkerOptions().position(theLatLng).title("Most Recent Location"));
					}
				}
			}
		}		

		//Building list 
		String[] theList = buildList();
		assocListView = (ListView) mapFragView.findViewById(R.id.map_frag_view);

		if(theList==null) {
			theList = new String[0];
		}

		mapAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,  theList);
		assocListView.setAdapter(mapAdapter);
		((BaseAdapter) assocListView.getAdapter()).notifyDataSetChanged();


		if(mapFragView != null) { 
			return mapFragView;
		}

		((ViewGroup) assocListView.getParent()).removeView(assocListView);

		container.addView(assocListView);

		//		setMapTransparent(container);
		return container;
	}

	private LatLng makeNewLatLng() {
		//Creating MapFragment from SharedPreferences recently stored information 
		SharedPreferences tmpManager = MainActivity.getInstance().prefs;
		LatLng toReturnLatLng = null;
		String recLatitude = tmpManager.getString("recentLatitude", "default");
		String recLongitude = tmpManager.getString("recentLongitude", "default");
		if((!recLatitude.equals("default"))&&(!recLongitude.equals("default"))) {
			double remLat = Double.parseDouble(recLatitude);
			double remLong = Double.parseDouble(recLongitude);
			toReturnLatLng = new LatLng(remLat, remLong);
		}
		return toReturnLatLng;
	}

	private String[] buildList() {
		SharedPreferences tmpManager = MainActivity.getInstance().prefs;
		String recWordAssoc = tmpManager.getString("wordAssociations", "default");
		String[] theList = null; 
		if(!recWordAssoc.equals("default")) {
			if(recWordAssoc.charAt(1)!=']') {
				if(recWordAssoc.length()>0) {
					recWordAssoc = recWordAssoc.substring(1, recWordAssoc.length()-1);
					theList = recWordAssoc.split("\", ");
					for(int i=0; i< theList.length; i++) {
						theList[i] = theList[i].substring(1).toLowerCase();
						if(i==theList.length-1) theList[i]=theList[i].substring(0, theList[i].length()-1);
					}
				}
			}
		}
		if(theList==null) {
			theList = new String[]{"No recent locations have been acquired yet."};
		}
		return theList; 
	}

	//	private void setMapTransparent(ViewGroup group) {
	//		int childCount = group.getChildCount();
	//		for (int i = 0; i < childCount; i++) {
	//			View child = group.getChildAt(i);
	//			if (child instanceof ViewGroup) {
	//				setMapTransparent((ViewGroup) child);
	//			} else if (child instanceof SurfaceView) {
	//				child.setBackgroundColor(Color.TRANSPARENT);
	//			}
	//		}
	//	}
}

