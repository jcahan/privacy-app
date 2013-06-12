package com.example.columbiaprivacyapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import com.actionbarsherlock.app.SherlockFragment;

public class TreeMenuFragment extends SherlockFragment{
	protected View view; 
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		MainActivity.getInstance().invalidateOptionsMenu();
		view = inflater.inflate(R.layout.treemenu, container, false);
		//Auto-Complete
		AutoCompleteTextView autoView = (AutoCompleteTextView) view.findViewById(R.id.edit_message);
		String[] itemOptions = getResources().getStringArray(R.array.edit_message);

		ArrayAdapter<String> theAdapter = 
				new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, itemOptions);
		autoView.setAdapter(theAdapter);
		((BaseAdapter) autoView.getAdapter()).notifyDataSetChanged();
		//		((ViewGroup) autoView.getParent()).removeView(autoView);
		Button b = (Button) view.findViewById(R.id.post_blacklist_button);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				AutoCompleteTextView editText=(AutoCompleteTextView) view.findViewById(R.id.edit_message);
				String blackListItem = editText.getText().toString();
				editText.setText("");
				MainActivity.getInstance().postBlackListItem(blackListItem);
				//refreshes the rest 
				MainActivity.getInstance().invalidateOptionsMenu();
			}
		});

		if(view != null) { return view; }
		container.addView(autoView);

		return view;
	}
}
