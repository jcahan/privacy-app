package officialapp.columbiaprivacyapp;

import java.util.ArrayList;
import java.util.TreeSet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class BlacklistWordDataSource {

	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.COLUMN_WORD };

	public BlacklistWordDataSource(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public BlacklistWord CreateBlacklistWord(String word){
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_WORD, word);
		long insertId = database.insert(MySQLiteHelper.TABLE_WORDS, null,
				values);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_WORDS,
				allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		BlacklistWord newWord = cursorToWord(cursor);
		cursor.close();
		return newWord;
	}

	private BlacklistWord cursorToWord(Cursor cursor) {
		BlacklistWord newWord = new BlacklistWord();
		newWord.setId(cursor.getLong(0));
		newWord.setWord(cursor.getString(1));
		return newWord;
	}
	
	//TODO: Ask Chris to glance at this method 
	public void deleteStringWord(String word) {
		word = DatabaseUtils.sqlEscapeString(word);
		database.delete(MySQLiteHelper.TABLE_WORDS, MySQLiteHelper.COLUMN_WORD + " = " + word, null);
	}
	public void DeleteWord(BlacklistWord word) {
		long id = word.getId();
		database.delete(MySQLiteHelper.TABLE_WORDS, MySQLiteHelper.COLUMN_ID + " = " + id, null);
	}

	public void DeleteAll() {
		database.delete(MySQLiteHelper.TABLE_WORDS, null, null);
	}

	public TreeSet<BlacklistWord> GetAllWords() {
		ArrayList<BlacklistWord> allWords = new ArrayList<BlacklistWord>();
		Cursor cursor = database.query(MySQLiteHelper.TABLE_WORDS,
				this.allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			BlacklistWord word = cursorToWord(cursor);
			allWords.add(word);
			cursor.moveToNext();
		}
		cursor.close();
		TreeSet<BlacklistWord> myTreeSet = new TreeSet<BlacklistWord>(new MyComparator());
		myTreeSet.addAll(allWords);
		return myTreeSet;
	}
}
