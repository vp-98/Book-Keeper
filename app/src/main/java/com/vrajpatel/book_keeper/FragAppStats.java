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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.w3c.dom.Text;

public class FragAppStats extends Fragment {

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_appstats_layout, container, false);
        mDatabaseHelper = new DatabaseHelper(getContext());
        bookListInformation = mDatabaseHelper.generateStatsReport();

        bookCountField = view.findViewById(R.id.appstats_numbooks);
        readCountField = view.findViewById(R.id.appstats_readbooks);
        notReadCountField = view.findViewById(R.id.appstats_notreadbooks);

        // Set the counts for each criteria
        bookCountField.setText(Integer.toString(bookListInformation.getBookCount()));
        readCountField.setText(Integer.toString(bookListInformation.getReadCount()));
        notReadCountField.setText(Integer.toString(bookListInformation.getNotReadCount()));

        return view;
    }
}
