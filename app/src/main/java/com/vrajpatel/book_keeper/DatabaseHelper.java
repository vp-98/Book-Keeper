/****************************************************************************************
 * Copyright (c) 2021 Vraj Patel <vrajpatel098@gmail.com>                               *
 *                                                                                      *
 * This program is free software; you can redistribute it and/or modify it under        *
 * the terms of the GNU General Public License as published by the Free Software        *
 * Foundation; either version 3 of the License, or (at your option) any later           *
 * version.                                                                             *
 *                                                                                      *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY      *
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A      *
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.             *
 *                                                                                      *
 * You should have received a copy of the GNU General Public License along with         *
 * this program.  If not, see <http://www.gnu.org/licenses/>.                           *
 ****************************************************************************************/

package com.vrajpatel.book_keeper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    // Database Information
    private static final int DATABASE_VERSION_ON_MOBILE_DEVICE = 2;  // Modifications
    private static final int DATABASE_VERSION_ON_SIMULATOR = 2;      // Modifications

    // Static strings for the table columns
    private static final String TABLE_NAME = "book_table";
    private static final String COL_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_TITLE_LOWERCASE = "lowercase_title";
    private static final String COL_SHELF_LOCATION = "shelf_location";
    private static final String COL_AUTHOR = "author";
    private static final String COL_IS_READ = "is_read";

    //==============================================================================================
    /**
     * DatabaseHelper (constructor):
     * Before pushing update to the phone, make sure that the database version matches that of
     *   the actual user's phone, otherwise make sure to switch back to the correct one for testing.
     * @param context
     */
    public DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null,2);
        Log.d(TAG, "DatabaseHelper: Database version simulator: " + DATABASE_VERSION_ON_SIMULATOR);
        Log.d(TAG, "DatabaseHelper: Database version mobile device: " + DATABASE_VERSION_ON_MOBILE_DEVICE);
    }
    //==============================================================================================

    /**
     * onCreate: (overridden method)
     * Creates a new data table if needed with the columns stated.
     * @param sqLiteDatabase
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(TAG, "onCreate: Creating Database " + TABLE_NAME);
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " TEXT, " + COL_TITLE_LOWERCASE + " TEXT, " + COL_AUTHOR + " TEXT, "
                + COL_IS_READ + " INTEGER, " + COL_SHELF_LOCATION + " TEXT)";

        sqLiteDatabase.execSQL(createTable);
    }
    //==============================================================================================

    /**
     * onUpgrade: (overridden method)
     *  Upgrades data table when a new version is detected.
     * @param sqLiteDatabase
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            Log.d(TAG, "onUpgrade: Creating the database table");
            sqLiteDatabase.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " +
                    COL_SHELF_LOCATION + " TEXT DEFAULT 'Default'");
            Log.d(TAG, "onUpgrade: Added new column");
            //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            //onCreate(sqLiteDatabase);
        }
    }
    //==============================================================================================

    /**
     * itemPresent:
     *   Checks to see if the item is already present in the database in the given column.
     * @param titleLowerCase         Title that is about to be added
     * @param author                 Author that is about to be added
     * @return boolean               True if book present, false otherwise
     */
    private boolean itemPresent(String titleLowerCase, String author) {
        Log.d(TAG, "itemPresent: Checking to see if: " + titleLowerCase + " exists in col: " + COL_TITLE_LOWERCASE);
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_TITLE_LOWERCASE +
                " = ? AND " + COL_AUTHOR + " =?";
        Cursor cursor = db.rawQuery(query, new String[]{titleLowerCase, author});
        int count = cursor.getCount();
        cursor.close();
        Log.d(TAG, "itemPresent: located " + count + " item(s) in the db with matching info");
        return (count > 0);
    }
    //==============================================================================================

    /**
     * addData:
     *  Taking the provided item, add it to the database if it does not exist.
     * @param title
     * @param author
     * @param titleLowerCase
     * @param readStatus
     * @param shelfLocation
     * @return boolean         True if book was saved, else false
     */
    public boolean addData(String title, String author, String titleLowerCase, boolean readStatus,
                           String shelfLocation) {
        Log.d(TAG, "addData: Adding a new Book to the database");
        SQLiteDatabase db = this.getWritableDatabase();

        // Check to make sure that the same book is not already there
        if (!(itemPresent(titleLowerCase, author))){

            ContentValues contentValues = new ContentValues();
            contentValues.put(COL_TITLE, title);
            contentValues.put(COL_TITLE_LOWERCASE,titleLowerCase);
            contentValues.put(COL_AUTHOR, author);
            contentValues.put(COL_IS_READ, readStatus? 1 : 0);
            contentValues.put(COL_SHELF_LOCATION, shelfLocation);

            // Get status of the insertion
            long result = db.insert(TABLE_NAME, null, contentValues);
            db.close();
            return result != -1;
        }
        return false;
    }
    //==============================================================================================

    /**
     * deleteBookWithID:
     *  Removes the book using the provided book. The ID is extracted from the Book class directly.
     * @param book
     * @return boolean    True by default
     */
    public boolean deleteBookWithID(BookModel book) {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.d(TAG, "deleteBookWithID: removing by item id: " + book.getID() + " title: " + book.getTitle());
        db.delete(TABLE_NAME, COL_ID + " =?" , new String[]{Long.toString(book.getID())});
        db.close();
        return true;
    }
    //==============================================================================================

    /**
     * getStoredBooks:
     *   Converts the contents of the database into an arraylist. Contents are
     *   sorted alphabetically.
     * @return ArrayList of stored books
     */
    public ArrayList<BookModel> getStoredBooks() {
        ArrayList<BookModel> books = new ArrayList<>();
        String queryContent = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_TITLE_LOWERCASE;
        SQLiteDatabase db = this.getReadableDatabase();

        // Obtain the data from db
        Cursor data = db.rawQuery(queryContent, null);

        if (data.moveToFirst()) {
            // Create the book using the provided information
            do {
                // Get the book information here
                int ID = data.getInt(0);
                String title = data.getString(1);
                String titleLower = data.getString(2);
                String author = data.getString(3);
                boolean readStatus = data.getInt(4) == 1;
                String shelfLocation = data.getString(5);

                // Create a new book with that
                books.add(new BookModel(title, titleLower, author, readStatus, ID, shelfLocation));
            } while(data.moveToNext());

        } else {
            Log.d(TAG, "getStoredBooks: There was nothing to fetch");
        }
        data.close();
        db.close();
        return books;
    }
    //==============================================================================================

    /**
     * generateStatsReport:
     *  Obtains the statistics of stored books.
     * @return FragAppStats.BookListInformation holding the stats
     */
    public FragAppStats.BookListInformation generateStatsReport() {

        String queryContent = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_TITLE_LOWERCASE;
        SQLiteDatabase db = this.getReadableDatabase();

        // general counters
        int bookCount = 0;
        int readCount = 0;
        int notReadCount = 0;

        // Obtain the data from db
        Cursor data = db.rawQuery(queryContent, null);

        if (data.moveToFirst()) {
            // Create the book using the provided information
            do {
                // Get the book information here
                if(data.getInt(4) == 1) {
                    readCount++;
                } else {notReadCount++; }

                bookCount++;
            } while(data.moveToNext());

        } else {
            Log.d(TAG, "getStoredBooks: There was nothing to fetch");
        }
        data.close();
        db.close();
        return new FragAppStats.BookListInformation(bookCount,
                notReadCount, readCount);
    }
    //==============================================================================================

    /**
     * updateCol:
     *  Updates the columns for a book that was edited.
     * @param book   Book with edited information.
     */
    public void updateCol(BookModel book) {

        int id = book.getID();  // Get ID from the class directly
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TITLE, book.getTitle());
        cv.put(COL_AUTHOR, book.getAuthor());
        cv.put(COL_TITLE_LOWERCASE, book.getTitleLowerCase());
        cv.put(COL_IS_READ, book.getReadStatus());
        cv.put(COL_SHELF_LOCATION, book.getShelfLocation());

        db.update(TABLE_NAME, cv, "ID = ?", new String[]{Integer.toString(id)});
        db.close();
    }
}
