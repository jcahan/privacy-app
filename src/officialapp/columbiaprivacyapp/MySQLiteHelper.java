package officialapp.columbiaprivacyapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

	public static final String TABLE_WORDS = "words";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_WORD = "word";

	private static final String DATABASE_NAME = "words.db";
	private static final int DATABASE_VERSION = 1;

	private static MySQLiteHelper instance; 

	public static synchronized MySQLiteHelper getHelper(Context context)
	{
		if (instance == null) {
			System.out.println("22 MYSQLiteHelper");
			instance = new MySQLiteHelper(context);
		}
		if(instance.getWritableDatabase()==null) {
			System.out.println("the instance writable is null");
		}
		System.out.println("25 MYSQLiteHelper");
		return instance;
	}

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_WORDS + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_WORD
			+ " text not null);";

	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(MySQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORDS);
		onCreate(db);
	}

} 