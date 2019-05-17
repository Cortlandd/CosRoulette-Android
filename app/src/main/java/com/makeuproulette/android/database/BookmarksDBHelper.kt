package com.makeuproulette.android.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.makeuproulette.android.database.model.BookmarkModel

class BookmarksDBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?): SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "bookmarks.db"
        val TABLE_NAME = "bookmarks"
        val COLUMN_ID = "id"
        val COLUMN_TITLE = "title"
        val COLUMN_THUMBNAIL = "thumbnail"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN_ID + " TEXT PRIMARY KEY, " +
            COLUMN_TITLE + " TEXT, " +
            COLUMN_THUMBNAIL + " TEXT" + ");"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

    fun addBookmark(bookmarkModel: BookmarkModel) {
        val values = ContentValues()
        values.put(COLUMN_ID, bookmarkModel.id)
        values.put(COLUMN_TITLE, bookmarkModel.title)
        values.put(COLUMN_THUMBNAIL, bookmarkModel.thumbnail)
        val db = this.writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getAllVideoIds(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME", null)
    }

    fun removeBookmark(videoId: String) {
        val db = this.writableDatabase
        return db.execSQL("DELETE FROM $TABLE_NAME WHERE $COLUMN_ID='$videoId'")
    }

    // TODO: Potentially implement this in shared preferences as opposed to running SQL Query
    fun allBookmarksList(): List<BookmarkModel> {
        val bookmarks: ArrayList<BookmarkModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TABLE_NAME"
        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                var bookmark: BookmarkModel = BookmarkModel()
                bookmark.id = cursor.getString(cursor.getColumnIndex(COLUMN_ID))
                bookmark.title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE))
                bookmark.thumbnail = cursor.getString(cursor.getColumnIndex(COLUMN_THUMBNAIL))
                bookmarks.add(bookmark)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return bookmarks

    }

}