package in.ezzie.gps.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by parminder on 9/2/16.
 */
public class GPSDatabase {
    private static final String TAG = GPSDatabase.class.getSimpleName();
    private Context context;
    private DbHelper dbHelper;
    public final String DBNAME="gps1";
    public final int DBVERSION=3;
    public SQLiteDatabase db;
    public final String COLUMN_LATITUDE="latitude";
    public final String COLUMN_LONGITUDE="longitude";
    public final String COLUMN_ID="id";
    public final String COLUMN_SESSION="session_id";
    public final String COLUMN_TIME="gpsTime";
    public final String TABLENAME="location";
    public final String CREATERDB="create table location(id integer primary key autoincrement," +
            "latitude text not null, longitude text not null, session_id text, " +
            "gpsTime text);";
    //const
    public GPSDatabase(Context context){
        this.context=context;
        dbHelper=new DbHelper(context);
    }
    public class DbHelper extends SQLiteOpenHelper {
        public DbHelper(Context context){
            super(context,DBNAME,null,DBVERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            // TODO Auto-generated method stub
            db.execSQL(CREATERDB);
            Log.d(TAG,"DB Created Successfully");
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub
            Log.d(TAG,"DB upgraded Successfully");
        }
    }
    public long insertRow(String latitude, String longitude, String session, String gpsTime){
        ContentValues value=new ContentValues();
        value.put(COLUMN_LATITUDE, latitude);
        value.put(COLUMN_LONGITUDE, longitude);
        value.put(COLUMN_SESSION, session);
        value.put(COLUMN_TIME, gpsTime);
        return db.insert(TABLENAME,null,value);
    }
    public Cursor getAllRows(){
        Cursor cursor=db.query(TABLENAME, new String[]{COLUMN_ID,COLUMN_LATITUDE,COLUMN_LONGITUDE,COLUMN_SESSION,COLUMN_TIME}, null,null, null, null, null);
        return cursor;
    }

    public void deleteAll() {
        db.execSQL("delete from "+ TABLENAME);
    }
    public void open() throws SQLException {
        db=dbHelper.getWritableDatabase();
        //return true;
    }
    public void close(){
        dbHelper.close();
        //return true;
    }
}
