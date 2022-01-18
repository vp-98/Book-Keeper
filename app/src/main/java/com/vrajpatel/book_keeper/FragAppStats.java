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

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class FragAppStats extends Fragment {
    private static final String TAG = "FragAppStats";
    // Public class to hold statistics of stored books
    public static class BookListInformation {
        private int bookCount;
        private int notReadCount;
        private int readCount;

        public BookListInformation() {
            this.bookCount = this.readCount = this.notReadCount = 0;
        }

        public BookListInformation(int bookCount, int notReadCount, int readCount) {
            this.bookCount = bookCount;
            this.notReadCount = notReadCount;
            this.readCount = readCount;
        }

        public void setBookCount(int bookCount) {this.bookCount = bookCount;}
        public int getBookCount() {return bookCount;}
        public void setNotReadCount(int notReadCount) {this.notReadCount = notReadCount;}
        public int getNotReadCount() {return notReadCount;}
        public void setReadCount(int readCount) {this.readCount = readCount;}
        public int getReadCount() {return readCount;}
    }

    private BookListInformation bookListInformation;
    private DatabaseHelper mDatabaseHelper;
    private TextView bookCountField;
    private TextView readCountField;
    private TextView notReadCountField;

    private TextView hashmapDebug;
    private ArrayList<BookModel> books;

    //==============================================================================================
    /**
     * onCreateView:
     *  Creates the view of the fragment and binds all the components in the fragment for further
     *   use.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return View of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_appstats_layout, container, false);
        mDatabaseHelper = new DatabaseHelper(getContext());
        bookListInformation = mDatabaseHelper.generateStatsReport();
        books = mDatabaseHelper.getStoredBooks();

        bookCountField = view.findViewById(R.id.appstats_numbooks);
        readCountField = view.findViewById(R.id.appstats_readbooks);
        notReadCountField = view.findViewById(R.id.appstats_notreadbooks);

        hashmapDebug = view.findViewById(R.id.appstats_hashmap_debug);
        genMap();

        // Set the counts for each criteria
        bookCountField.setText(Integer.toString(bookListInformation.getBookCount()));
        readCountField.setText(Integer.toString(bookListInformation.getReadCount()));
        notReadCountField.setText(Integer.toString(bookListInformation.getNotReadCount()));

        return view;
    }

    private void genMap() {
        HashMap<String, ArrayList<BookModel>> shelfBooks = new HashMap<>();
        if (shelfBooks == null) {
            Log.e(TAG, "genMap: the map is null");
        }
        for (BookModel book : books) {
            ArrayList<BookModel> currList = shelfBooks.get(book.getShelfLocation());
            if (currList == null) { currList = new ArrayList<BookModel>();}
            currList.add(book);
            shelfBooks.put(book.getShelfLocation(), currList);
        }
        String message = "Shelf and book count\n";
        for (Map.Entry<String, ArrayList<BookModel>> entry : shelfBooks.entrySet()) {
            message += entry.getKey() + ": " + Integer.toString(entry.getValue().size()) + "\n";
        }

        hashmapDebug.setText(message);
    }
}
