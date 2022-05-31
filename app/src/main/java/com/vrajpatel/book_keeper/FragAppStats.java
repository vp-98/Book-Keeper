//****************************************************************************************
//* Copyright (c) 2022 Vraj Patel <vrajpatel098@gmail.com>                               *
//*                                                                                      *
//* This program is free software; you can redistribute it and/or modify it under        *
//* the terms of the GNU General Public License as published by the Free Software        *
//* Foundation; either version 3 of the License, or (at your option) any later           *
//* version.                                                                             *
//*                                                                                      *
//* This program is distributed in the hope that it will be useful, but WITHOUT ANY      *
//* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A      *
//* PARTICULAR PURPOSE. See the GNU General Public License for more details.             *
//*                                                                                      *
//* You should have received a copy of the GNU General Public License along with         *
//* this program.  If not, see <http://www.gnu.org/licenses/>.                           *
//****************************************************************************************/

package com.vrajpatel.book_keeper;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FragAppStats extends Fragment {
    private static final String TAG = "FragAppStats";

    /**
     * BookListInformation
     *  Static class that holds information used on the stats page. Considers read books,
     *   books that have not yet been read, and the total book count.
     */
    public static class BookListInformation {
        private final int bookCount;
        private final int notReadCount;
        private final int readCount;

        public BookListInformation(int bookCount, int notReadCount, int readCount) {
            this.bookCount = bookCount;
            this.notReadCount = notReadCount;
            this.readCount = readCount;
        }

        // public void setBookCount(int bookCount) {this.bookCount = bookCount;}
        public int getBookCount() {return bookCount;}
        // public void setNotReadCount(int notReadCount) {this.notReadCount = notReadCount;}
        public int getNotReadCount() {return notReadCount;}
        // public void setReadCount(int readCount) {this.readCount = readCount;}
        public int getReadCount() {return readCount;}
    }

    private TextView hashmapDebug;
    private ArrayList<BookModel> books;

    //==============================================================================================
    /**
     * onCreateView:
     *  Creates the view of the fragment and binds all the components in the fragment for further
     *   use.
     * @param inflater           inflater which will inflate the menu
     * @param container          Container which will have the view inflated in
     * @param savedInstanceState Saved state
     * @return View of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Creating initial stats view");

        View view = inflater.inflate(R.layout.frag_appstats_layout, container, false);
        DatabaseHelper mDatabaseHelper = new DatabaseHelper(getContext());
        BookListInformation bookListInformation = mDatabaseHelper.generateStatsReport();
        books = mDatabaseHelper.getStoredBooks();

        TextView bookCountField = view.findViewById(R.id.appstats_numbooks);
        TextView readCountField = view.findViewById(R.id.appstats_readbooks);
        TextView notReadCountField = view.findViewById(R.id.appstats_notreadbooks);

        hashmapDebug = view.findViewById(R.id.appstats_hashmap_debug);
        genMap();

        // Set the counts for each criteria
        bookCountField.setText(String.format(Locale.getDefault(), "%d",
                bookListInformation.getBookCount()));
        readCountField.setText(String.format(Locale.getDefault(), "%d",
                bookListInformation.getReadCount()));
        notReadCountField.setText(String.format(Locale.getDefault(), "%d",
                bookListInformation.getNotReadCount()));

        return view;
    }

    private void genMap() {
        HashMap<String, ArrayList<BookModel>> shelfBooks = new HashMap<>();
        for (BookModel book : books) {
            ArrayList<BookModel> currList = shelfBooks.get(book.getShelfLocation());
            if (currList == null) { currList = new ArrayList<>();}
            currList.add(book);
            shelfBooks.put(book.getShelfLocation(), currList);
        }
        StringBuilder message = new StringBuilder("Shelf and book count\n");
        for (Map.Entry<String, ArrayList<BookModel>> entry : shelfBooks.entrySet()) {
            message.append(entry.getKey()).append(": ").append(entry.getValue().size()).append("\n");
        }

        hashmapDebug.setText(message.toString());
    }
}
