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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;

import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FragSearchBook extends Fragment implements PopupMenu.OnMenuItemClickListener,
        RecyclerViewAdapter.onDeleteCallListener, RecyclerViewAdapter.onEditCallListener {

    // To create the popup menu---------------------
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private Button cancelBTN, updateBTN;
    private SwitchCompat readSwitch;
    private EditText titleField, authorField;
    private Spinner spinner;
    //----------------------------------------------

    private static final String TAG = "FragSearchBook";
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private ArrayList<BookModel> allBooks;
    private ImageView filterIcon;

    private SearchView searchBar;
    private DatabaseHelper mDatabaseHelper;

    // Filter options and popup menu for filters
    private PopupMenu popupMenu;
    private View popupMenuLocationView;
    private boolean read;
    private boolean notRead;
    private String searchQuery;
    private Context mContext;
    private RecyclerViewAdapter.onDeleteCallListener myDeleteListener;
    private RecyclerViewAdapter.onEditCallListener myEditListener;

    //==============================================================================================
    /**
     * onCreateView: (overridden method)
     *  Creates the view of the fragment and binds all the components in the fragment for further
     *   use.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return View of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {

        // generate view and attach all private members
        View view = inflater.inflate(R.layout.frag_search_layout, container, false);
        recyclerView = view.findViewById(R.id.search_results);
        searchBar = view.findViewById(R.id.search_bar);
        searchBar.setImeOptions(EditorInfo.IME_ACTION_DONE);
        filterIcon = view.findViewById(R.id.filter_icon);

        //noteListener = this;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        allBooks = new ArrayList<>();
        searchQuery = "";
        mContext = getContext();
        myDeleteListener = this;
        myEditListener = this;

        // Create the shared preference

        // Default filter options
        read = notRead = true;

        return view;
    }
    //==============================================================================================

    /**
     * onViewCreated: (overridden method)
     *  Sets up the fragment and initializes the page for searching. Initializes components of
     *   the fragment and sets up a recycler view with a custom adapter. Filters recycler view
     *   depending on the user's search.
     * @param view
     * @param savedInstanceState
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: FirstFragment has started");
        mDatabaseHelper = new DatabaseHelper(getContext());

        // Populate the list
        allBooks = mDatabaseHelper.getStoredBooks();
        searchQuery = "";
        // Create and set the adapter using the list of books
        adapter = new RecyclerViewAdapter(allBooks, mContext, myDeleteListener, myEditListener);
        adapter.provideFilters(true, true, "");
        recyclerView.setAdapter(adapter);
        registerForContextMenu(recyclerView);

        // Allows the search bar to be opened fully
        searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBar.setIconified(false);
            }
        });
        // Modify results as characters are inserted into the search
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {return false;}
            @Override
            public boolean onQueryTextChange(String newText) {
                searchQuery = newText;
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        // Icon for filter listener
        filterIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenuLocationView = v;
                showFilterMenu(v);
            }
        });
    }
    //==============================================================================================

    /**
     * onCreateContextMenu:
     *  Generates an option menu for each item present in the recycler view.
     * @param menu
     * @param v
     * @param menuInfo
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.item_context_menu, menu);
    }
    //==============================================================================================

    /**
     * showFilterMenu:
     *  Creates the filter menu that pops up for the filter options and accepts the user's selected
     *   filter options.
     * @param view
     */
    private void showFilterMenu(View view) {
        popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.inflate(R.menu.filter_menu);
        popupMenu.setOnMenuItemClickListener(this);
        Menu menu = popupMenu.getMenu();
        menu.findItem(R.id.filter_not_read_books).setChecked(notRead);
        menu.findItem(R.id.filter_read_books).setChecked(read);
        popupMenu.show();
    }
    //==============================================================================================

    /**
     * onMenuClick: (overridden method)
     *  Registers the menu clicks from the filter menu. The selected options from the menu will are
     *   then used to filter the results.
     * @param item
     * @return
     */
    @Override
    public boolean onMenuItemClick(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.filter_not_read_books:
                notRead = !item.isChecked();
                break;
            case R.id.filter_read_books:
                read = !item.isChecked();
                break;
        }
        adapter.provideFilters(read, notRead, searchQuery);
        item.setChecked(!item.isChecked());
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        item.setActionView(new View(getContext()));
        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return false;
            }
        });
        return false;
    }
    //==============================================================================================

    /**
     * deleteItem: (overridden method)
     *  Deletes the item that is selected on the recycler view.
     * @param position
     */
    @Override
    public void deleteItem(int position) {
        if (mDatabaseHelper.deleteBookWithID(allBooks.get(position))) {
            allBooks.remove(position);
            adapter.notifyDataSetChanged();
        } else {
            Log.e(TAG, "deleteItem: Book Could not be removed");
        }
    }
    //==============================================================================================

    /**
     * openEditFragment: (overridden method)
     *  Generates a popup dialog with fields pre-filled with the selected book's information.
     * @param position
     */
    @Override
    public void openEditFragment(int position) {
        generatePopup(allBooks.get(position));
    }
    //==============================================================================================

    /**
     * generatePopup:
     *  Creates a small popup dialog/window allowing the user to edit any of the book's fields.
     * @param book
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
        spinner = (Spinner) popupView.findViewById(R.id.popup_spinner);

        // Add shelves the spinner
        ArrayList<String> storedNames = loadShelfNames();
        ArrayAdapter<String> dropDownArrayAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, storedNames);
        spinner.setAdapter(dropDownArrayAdapter);

        // Set fields to current book information
        titleField.setText(book.getTitle());
        authorField.setText(book.getAuthor());
        readSwitch.setChecked(book.getReadStatus());
        if (storedNames.contains(book.getShelfLocation())) {
            spinner.setSelection(dropDownArrayAdapter.getPosition(book.getShelfLocation()));
        } else { spinner.setSelection(0);}

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
                String shelfLocation = spinner.getSelectedItem().toString();
                boolean newStatus = readSwitch.isChecked();

                book.setAuthor(author);
                book.setTitle(title);
                book.setTitleLowerCase(title.toLowerCase());
                book.setReadStatus(newStatus);
                book.setShelfLocation(shelfLocation);

                mDatabaseHelper.updateCol(book);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
    }
    //==============================================================================================

    /**
     * loadShelfNames:
     *  Extracts the saved shelf names that the user has defined from the shared-preferences.
     * @return Arraylist of shelf names
     */
    private ArrayList<String> loadShelfNames() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(MainActivity.SHARED_PREFERENCES,
                MainActivity.MODE_PRIVATE);

        String storedNames = sharedPreferences.getString(MainActivity.SHELVES, "");
        if (storedNames.length() == 0) { storedNames = "Default";}

        String[] namesArr = storedNames.split("@",-1);
        ArrayList<String> shelfNames = new ArrayList<String>();

        for (String name : namesArr) { shelfNames.add(name);}
        return shelfNames;
    }
}
