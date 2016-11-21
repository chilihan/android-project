package com.songhan.viralmix;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Anne Han on 2016-11-19.
 */

public class VideoDbAdapter {
    private static final String DATABASE_NAME = "VIDEO_DATABASE.db";
    private static final String TEMP_DATABASE_NAME = "TEMP_VIDEO_DATABASE.db";
    private static final String TABLE_NAME = "VIDEO_TABLE";
    private static final int DATABASE_VERSION = 8;
    public final Context Ctx;

    public static String TAG = VideoDbAdapter.class.getSimpleName();

    private DatabaseHelper DbHelper;
    SQLiteDatabase Db;

    public static final String KEY_ROWID="_id";
    public static final String VIDEO_ID="videoId";
    public static final String IMAGE_URL = "imageURL";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String CHANNEL = "channel";
    public static final String NEXTPAGE_ID = "nextpageId";
    public static final String STATE = "state";

    public static final String[] FIELDS = new String[]{
            KEY_ROWID,
            VIDEO_ID,
            IMAGE_URL,
            TITLE,
            DESCRIPTION,
            CHANNEL,
            NEXTPAGE_ID,
            STATE
    };

    private static final String CREATE_TABLE =
            "create table " + TABLE_NAME +"("
                    + KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + VIDEO_ID + " not null UNIQUE,"
                    + IMAGE_URL + " not null,"
                    + TITLE + " not null,"
                    + DESCRIPTION + " not null,"
                    + NEXTPAGE_ID + " TEXT,"
                    + STATE + " INTEGER not null default 0,"
                    + CHANNEL + " not null"
                    +");";

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context, String databaseName) {
            super(context, databaseName, null, context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE).getInt("DATABASE_VERSION", 10));
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    public VideoDbAdapter(Context ctx){
        this.Ctx = ctx;
    }

    public VideoDbAdapter open() throws SQLException {
        return open(DATABASE_NAME);
    }

    public VideoDbAdapter openTemp() throws SQLException {
        open(TEMP_DATABASE_NAME);
        Db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        Db.execSQL(CREATE_TABLE);
        return this;
    }

    public VideoDbAdapter open(String databaseName) throws SQLException {
        DbHelper = new DatabaseHelper(Ctx, databaseName);
        Db = DbHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        if(DbHelper!=null){
            DbHelper.close();
        }
    }

    public long insert(ContentValues initialValues){
        return Db.insertWithOnConflict(TABLE_NAME, null, initialValues, SQLiteDatabase.CONFLICT_FAIL);
    }

    public long insert(VideoData v){
        long id = insert(getContentValueFrom(v));
        v.id = (int)id;
        return id;
    }

    public boolean update(int id, ContentValues newValues){
        String[] selectionArgs = {String.valueOf(id)};
        return Db.update(TABLE_NAME, newValues, KEY_ROWID + "=?", selectionArgs) >0;
    }

    public boolean update(int id, VideoData v){
        return update(id, getContentValueFrom(v));
    }

    public boolean delete(int id){
        String[] selectionArgs = {String.valueOf(id)};
        return Db.delete(TABLE_NAME, KEY_ROWID + "=?", selectionArgs)>0;
    }

    public Cursor getAll(){
        return Db.query(TABLE_NAME, FIELDS, null, null, null, null, null);
    }

    public boolean videoExist(String videoId){
        return Db.query(TABLE_NAME, FIELDS,
                VIDEO_ID + " = ?",
                new String[] { videoId },
                null, null, null).getCount() != 0;
    }

    public Cursor getNormal(){
        return Db.query(TABLE_NAME, FIELDS,
                STATE + " = " +VideoData.STATE_NORMAL,
                null,
                null, null, null);
    }

    public Cursor getLiked(){
        return Db.query(TABLE_NAME, FIELDS,
                STATE + " = " +VideoData.STATE_LIKED,
                null,
                null, null, null);
    }

    public Cursor query(String match){
        return Db.query(TABLE_NAME, FIELDS,
                TITLE + " LIKE ? OR " + CHANNEL + " LIKE ? OR " + DESCRIPTION + " LIKE ?",
                new String[] { "%"+ match + "%", "%"+ match + "%", "%"+ match + "%" },
                null, null, null);
    }

    public VideoData getVideo(String videoId){
        Cursor c = Db.query(TABLE_NAME, FIELDS,
                VIDEO_ID + " = ?",
                new String[] { videoId },
                null, null, null);
        if (c.getCount() > 0){
            c.moveToFirst();
            return getVideoFromCursor(c);
        }
        return null;
    }

    public static VideoData getVideoFromCursor(Cursor cursor){
        VideoData v = new VideoData();
        v.id = cursor.getInt(cursor.getColumnIndex(KEY_ROWID));
        v.videoId = cursor.getString(cursor.getColumnIndex(VIDEO_ID));
        v.title = cursor.getString(cursor.getColumnIndex(TITLE));
        v.description = cursor.getString(cursor.getColumnIndex(DESCRIPTION));
        v.channel = cursor.getString(cursor.getColumnIndex(CHANNEL));
        v.imageUrl = cursor.getString(cursor.getColumnIndex(IMAGE_URL));
        v.nextPageId = cursor.getString(cursor.getColumnIndex(NEXTPAGE_ID));
        v.state = cursor.getInt(cursor.getColumnIndex(STATE));

        return(v);
    }

    public static ContentValues getContentValueFrom(VideoData v){
        ContentValues newValues = new ContentValues();
        newValues.put(TITLE, v.title);
        newValues.put(DESCRIPTION, v.description);
        newValues.put(CHANNEL, v.channel);
        newValues.put(VIDEO_ID, v.videoId);
        newValues.put(NEXTPAGE_ID, v.nextPageId);
        newValues.put(IMAGE_URL, v.imageUrl);
        newValues.put(STATE, v.state);
        return newValues;
    }
}
