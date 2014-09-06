package com.letv.statistics;

import com.letv.upnpControl.tools.LetvLog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = "record.db";
	/* 搜索输入的历史记录 */
	private static final String TBL_STORE = "storeTbl";
	private static final String TBL_VIDEO = "videoTbl";

	private static final String CREATE_STORE_TBL = " create table if not exists "
			+ " storeTbl(_id integer primary key autoincrement,key text) ";
	private static final String CREATE_VIDEO_TBL = " create table if not exists "
			+ " videoTbl(_id integer primary key autoincrement,key text) ";

	/* 统计上报 */
//	private static final String TBL_STARTUPS = "startupsTbl";
//	private static final String TBL_LOGINS = "loginsTbl";
//	private static final String TBL_NAVCLICKS = "navclicksTbl";
//	private static final String TBL_PUSHES = "pushesTbl";
//	private static final String TBL_VSHOWS = "vshowsTbl";
//	private static final String TBL_VPLAYS = "vplaysTbl";
//	private static final String TBL_APPSHOWS = "appshowsTbl";
//	private static final String TBL_APPINSTALLS = "appinstallsTbl";
//	private static final String TBL_OPENDURS = "opendursTbl";

	private static final String CREATE_STARTUPS_TBL = " create table if not exists "
			+ " startupsTbl(_id integer primary key autoincrement,at text) ";
	private static final String CREATE_LOGINS_TBL = " create table if not exists "
			+ " loginsTbl(_id integer primary key autoincrement,at text,tp integer,dur integer) ";
	private static final String CREATE_NAVCLICKS_TBL = " create table if not exists "
			+ " navclicksTbl(_id integer primary key autoincrement,at text,navid text) ";
	private static final String CREATE_PUSHES_TBL = " create table if not exists "
			+ " pushesTbl(_id integer primary key autoincrement,at text,tp integer) ";
	private static final String CREATE_VSHOWS_TBL = " create table if not exists "
			+ " vshowsTbl(_id integer primary key autoincrement,at text,vid text,vn text) ";
	private static final String CREATE_VPLAYS_TBL = " create table if not exists "
			+ " vplaysTbl(_id integer primary key autoincrement,at text,vid text,vn text) ";
	private static final String CREATE_APPSHOWS_TBL = " create table if not exists "
			+ " appshowsTbl(_id integer primary key autoincrement,at text,apn text,an text) ";
	private static final String CREATE_APPINSTALLS_TBL = " create table if not exists "
			+ " appinstallsTbl(_id integer primary key autoincrement,at text,apn text,an text) ";
	private static final String CREATE_OPENDURS_TBL = " create table if not exists "
			+ " opendursTbl(_id integer primary key autoincrement,at text,dur integer,fdur integer,bdur integer) ";

	/* 下載視頻存儲 */
	private static final String TBL_DOWNLOAD = "info";

	private static final String CREATE_DOWNLOAD_TBL = " create table if not exists "
			+ " info(_id integer primary key autoincrement,path text,done integer,name text,pic_url text,html text,filelen integer) ";

	private static final String TBL_FINISHED_DOWNLOAD = "finished_info";

	private static final String CREATE_FINISHED_DOWNLOAD_TBL = " create table if not exists "
			+ " finished_info(_id integer primary key autoincrement,path text,filelen integer,name text) ";
	// private static final String CREATE_DOWNLOAD_TBL =
	// " create table if not exists "
	// +
	// " info(path VARCHAR(1024),done INTEGER,name VARCHAR(1024),pic_url VARCHAR(1024) PRIMARY KEY(path, done,name,pic_url)) ";

	private SQLiteDatabase db;

	public DBHelper(Context c) {
		super(c, DB_NAME, null, 2);
	}

	public void onCreate(SQLiteDatabase db) {
		this.db = db;
		db.execSQL(CREATE_STORE_TBL);
		db.execSQL(CREATE_VIDEO_TBL);
		db.execSQL(CREATE_STARTUPS_TBL);
		db.execSQL(CREATE_LOGINS_TBL);
		db.execSQL(CREATE_NAVCLICKS_TBL);
		db.execSQL(CREATE_PUSHES_TBL);
		db.execSQL(CREATE_VSHOWS_TBL);
		db.execSQL(CREATE_VPLAYS_TBL);
		db.execSQL(CREATE_APPSHOWS_TBL);
		db.execSQL(CREATE_APPINSTALLS_TBL);
		db.execSQL(CREATE_OPENDURS_TBL);
		db.execSQL(CREATE_DOWNLOAD_TBL);
		db.execSQL(CREATE_FINISHED_DOWNLOAD_TBL);
		// System.currentTimeMillis();
	}

	public void insert(ContentValues values, String table) {
		SQLiteDatabase db = getWritableDatabase();
		db.insert(table, null, values);
		db.close();
	}

	public Cursor query(String table) {
		SQLiteDatabase db = getWritableDatabase();

		Cursor c = db.query(table, null, null, null, null, null, null);

		return c;
	}

	public Boolean isEmpty(String table) {
		
		try {
			SQLiteDatabase db = getWritableDatabase();
			Cursor c = db.query(table, null, null, null, null, null, null);
			int count = c.getCount();
			if (c != null) {
				c.close();
			}
			// if (db != null) {
			// db.close();
			// }
			if (count > 0) {
				return false;
			} else {
				return true;
			}
		} catch (SQLiteDiskIOException e) {
			return true;
		} catch (SQLiteException e1) {
			return true;
		}
	}

	public void del(int id, String table) {
		// if (db == null) {
		db = getWritableDatabase();
		// }
		db.delete(table, "_id=?", new String[] { String.valueOf(id) });

		// if (db != null) {
		// db.close();
		// }
	}

	public void delAll(String table) {
		// if (db == null) {
		db = getWritableDatabase();
		// }
		if (!isEmpty(table)) {
			LetvLog.i("logpost", "table:" + table);
			db.delete(table, null, null);
		}
		// if (db != null) {
		// db.close();
		// }
	}

	public void close() {
		if (db != null) {
			db.close();
		}
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
}
