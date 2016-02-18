package us.areaz.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ahmed on 2/15/2016.
 */
public class FavoriteMoviesSqlHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FavoriteMovies.db";
    public static final String TABLE_NAME = "FavoriteMovieTable";
    public static final String MOVIE_ID = "MovieID";
    public static final String FIND_IF_FAVORITE = "SELECT * FROM "
            +TABLE_NAME + " WHERE "+MOVIE_ID+" = '%s';";

    public static final String Delete_From_FAVORITE = "DELETE FROM "+TABLE_NAME +
                    " WHERE "+MOVIE_ID+" = '%s';";

    public static final String ADD_TO_FAVORITE = "INSERT INTO "+TABLE_NAME +
            "("+MOVIE_ID+") VALUES ('%s');";

    public String[] projection = {MOVIE_ID};

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    MOVIE_ID + " INTEGER PRIMARY KEY"+
            " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;



    public FavoriteMoviesSqlHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public List<Integer> getAll(){
        Cursor cursor = getReadableDatabase().rawQuery("select * from " + TABLE_NAME, null);
        if(cursor != null && cursor.getCount() > 0) {
            List<Integer> toReturn = new ArrayList<Integer>(cursor.getCount());
            if (cursor.moveToFirst()) {
                while (cursor.isAfterLast() == false) {
                    int id = cursor.getInt(cursor
                            .getColumnIndex(MOVIE_ID));
                    toReturn.add(id);
                    cursor.moveToNext();
                }
            }
            return toReturn;
        }
        return null;
    }

    public void delete(String MOVIE_ID){
        getWritableDatabase().execSQL(String.format(Delete_From_FAVORITE, MOVIE_ID));
    }

    public void add(String MOVIE_ID){
        getWritableDatabase().execSQL(String.format(ADD_TO_FAVORITE, MOVIE_ID));
    }

    public boolean isFavorite(String MOVIE_ID){
        Cursor cursor = getReadableDatabase().rawQuery(String.format(FIND_IF_FAVORITE, MOVIE_ID), null);
        return cursor==null?false:cursor.getCount()>0;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
