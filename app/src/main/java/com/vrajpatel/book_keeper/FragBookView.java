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

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class FragBookView extends Fragment implements RecyclerViewAdapter.onDeleteCallListener, RecyclerViewAdapter.onEditCallListener {

    private static final String TAG = "BookListFragment";
    private RecyclerView recyclerView;
    private ArrayList<BookModel> books;
    private RecyclerViewAdapter adapter;
    private DatabaseHelper mDatabaseHelper;
    private Context mContext;

    // To create the popup menu---------------------
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private Button cancelBTN, updateBTN;
    private SwitchCompat readSwitch;
    private EditText titleField, authorField;
    //----------------------------------------------

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_bookview_layout, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_holder);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        books = new ArrayList<>();
        mContext = getContext();
        return view;
    }

    //==============================================================================================
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: FirstFragment has started");
        mDatabaseHelper = new DatabaseHelper(getContext());
        // Get the list view layout here

        // Populate the list
        books = mDatabaseHelper.getStoredBooks();
        // Create and set the adapter using the list of books
        adapter = new RecyclerViewAdapter(books,mContext, this, this);
        recyclerView.setAdapter(adapter);
        registerForContextMenu(recyclerView);
    }
    //==============================================================================================
    /*
     * onCreateContextMenu:
     *  Generates an option menu for each item present in the list view.
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.item_context_menu, menu);
    }

    @Override
    public void deleteItem(int position) {
        if (mDatabaseHelper.deleteBookWithID(books.get(position))) {
            Log.e(TAG, "deleteItem: Should be reset??");
            books.remove(position);
            adapter.notifyDataSetChanged();
        } else {
            Log.e(TAG, "deleteItem: Book Could not be removed");
        }
    }

    @Override
    public void openEditFragment(int position) {
        generatePopup(books.get(position));
    }
    //==============================================================================================
    /*
     * generatePopup:
     *  This function will create a small popup window to the screen. This will allow the user to
     *   edit a selected book.
     */
    public void generatePopup(BookModel book) {
        Log.d(TAG, "generatePopup: Generating A new Popup Option");
        dialogBuilder = new AlertDialog.Builder(mContext);
        final View popupView = getLayoutInflater().inflate(R.layout.popup_editbook_layout, null);

        // Get all the widgets attached from the layout
        titleField = (EditText) popupView.findViewById(R.id.pop_bookTitleET);
        authorField = (EditText) popupView.findViewById(R.id.pop_bookAuthorET);
        cancelBTN = (Button) popupView.findViewById(R.id.pop_cancel_btn);
        updateBTN = (Button) popupView.findViewById(R.id.pop_update_btn);
        readSwitch = (SwitchCompat) popupView.findViewById(R.id.pop_read_switch);

        // Set fields to current book information
        titleField.setText(book.getTitle());
        authorField.setText(book.getAuthor());
        readSwitch.setChecked(book.getReadStatus());

        // Create the popup view
        dialogBuilder.setView(popupView);
        dialog = dialogBuilder.create();
        dialog.show();

        readSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (readSwitch.isChecked()) {
                    readSwitch.setText(R.string.pop_is_read_switch_true);
                } else {
                    readSwitch.setText(R.string.pop_is_read_switch_false);
                }
            }
        });
        cancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // Close dialog
            }
        });
        updateBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve data from the text field and update book
                String title = titleField.getText().toString();
                String author = authorField.getText().toString();
                boolean newStatus = readSwitch.isChecked();

                book.setAuthor(author);
                book.setTitle(title);
                book.setTitleLowerCase(title.toLowerCase());
                book.setReadStatus(newStatus);

                mDatabaseHelper.updateCol(book);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
    }
}