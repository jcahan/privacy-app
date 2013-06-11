//package com.example.columbiaprivacyapp;
//
//import java.util.ArrayList;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.view.Menu;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemLongClickListener;
//import android.widget.ArrayAdapter;
//import android.widget.EditText;
//import android.widget.ListView;
//import android.widget.Toast;
//
//public class BlacklistActivity extends Activity {
//
//	private ArrayAdapter adapter;
//	private ArrayList<String> blacklist_strs; 
//	private ArrayList<BlacklistWord> blacklist_words;
//	//private SimpleCursorAdapter dataAdapter;
//	
//	private BlacklistWordDataSource datasource;
//	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_blacklist);
//		
//		// Get datasource for the blacklist
//		datasource = new BlacklistWordDataSource(this);
//		datasource.open();
//		
//		this.blacklist_words = datasource.GetAllWords();
//		
//		this.adapter = new  ArrayAdapter<BlacklistWord>(this, 
//		        android.R.layout.simple_list_item_1, this.blacklist_words);
//		ListView visibleBlacklist = (ListView) findViewById(R.id.blacklist);
//		visibleBlacklist.setAdapter(this.adapter);		
//		
//		visibleBlacklist.setOnItemLongClickListener(new OnItemLongClickListener() {
//	          public boolean onItemLongClick(AdapterView<?> parent, View view,
//	              int position, long id) {
//	        	  BlacklistActivity.this.removeFromBlacklist(position);
//	        	  return true;
//	          }
//			});
//
//		Toast.makeText(this, "Press and hold a word to remove it", Toast.LENGTH_LONG).show();
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.activity_blacklist, menu);
//		return true;
//	}
//	
//	/** Called when the user clicks the Send button */
//	public void addToBlacklist(View view) {
//	    // Add new blacklist item to the blacklist
//		EditText editText = (EditText) findViewById(R.id.new_blacklist);
//		BlacklistWord newWord = this.datasource.CreateBlacklistWord(editText.getText().toString());
//		this.blacklist_words.add(0, newWord);
//		this.adapter.notifyDataSetChanged();
//	}
//	
//	/** Called when the user clicks on an item in the blacklist */
//	public void removeFromBlacklist(int position) {
//		BlacklistWord removedWord = this.blacklist_words.remove(position);
//		this.datasource.DeleteWord(removedWord);
//		this.adapter.notifyDataSetChanged();
//		Toast.makeText(getBaseContext(), "Removed " + removedWord.getWord(), Toast.LENGTH_SHORT).show();
//	}
//	
//	@Override
//	protected void onResume() {
//		datasource.open();
//		super.onResume();
//	}
//
//	@Override
//	protected void onPause() {
//		datasource.close();
//		super.onPause();
//	}
//}
