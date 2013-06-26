package officialapp.columbiaprivacyapp;

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
import officialapp.columbiaprivacyapp.R;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;

public class BlackistFragment extends SherlockFragment {
	protected ArrayAdapter<String> adapter;
	protected View view; 
	protected ArrayList<String> theList; 
	protected ListView listView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		view = inflater.inflate(R.layout.blacklist, container, false);
		listView = (ListView) view.findViewById(R.id.listview);
		//Making BlackList 
		TreeSet<BlacklistWord> theSet =  MainActivity.getInstance().datasource.GetAllWords();
		
		theList = new ArrayList<String>();
		
		if(theSet.size()==0) {
			theList.add("Please navigate to the \"Add to List\" Tab to update your \"Blacklist.\"");
		}
		else {
			for(BlacklistWord i :theSet){
				theList.add(i.getWord());
			}
			Collections.sort(theList);
		}
		
		adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,  theList);
		listView.setAdapter(adapter);
		((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
		
		Toast.makeText(getSherlockActivity(), "To delete an item, press down on it for several seconds. To add an item, navigate to the \"Add to List\" tab.", Toast.LENGTH_SHORT).show();
		
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				String theWord = theList.get(position);

				MainActivity.getInstance().deleteFromBlackList((theWord));

				//Attempt to delete from TreeMenuFragment to keep consistent
				MainActivity.getInstance().removeFromMenu(theWord);

				//Refresh
				theList.remove(theWord);
				adapter.notifyDataSetChanged();

				return true; 
			}
		});

		if(view != null) { return view;}

		((ViewGroup) listView.getParent()).removeView(listView);
		container.addView(listView);

		return view;
	}
	public void refresh() {
		if(listView!=null) {
			((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
		}
	}
}
