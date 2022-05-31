//****************************************************************************************
//* Copyright (c) 2021 Vraj Patel <vrajpatel098@gmail.com>                               *
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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class FragSettings extends Fragment implements ListViewAdapter.onDeleteIconPressListener {

    private static final String TAG = "Settings Fragment";
    // View for this fragment
    private View settingsView;

    // For layout preference card
    private Button refreshLayoutBTN;
    private Spinner layoutChoiceSpinner;

    // For shelf naming card
    private EditText shelfName;
    private Button addShelfBTN;
    private ListView shelves;
    private ArrayList<String> shelfNames;
    private ListViewAdapter listViewAdapter;

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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        settingsView = inflater.inflate(R.layout.frag_settings_layout, container, false);
        Log.d(TAG, "onCreateView: creating view of settings page");
        initLayoutPrefCard();
        initShelfNameCard();
        return settingsView;
    }
    //==============================================================================================

    /**
     * initLayoutPrefCard:
     *  Initializes the components inside the fragment and sets up respective functions and
     *   accepts user's sort preference for the recycler view.
     */
    private void initLayoutPrefCard() {
        Log.d(TAG, "initLayoutPrefCard:  Initialized objects in the layout preference card");
        layoutChoiceSpinner = settingsView.findViewById(R.id.settings_layout_spinner);
        refreshLayoutBTN = settingsView.findViewById(R.id.settings_refresh_btn);

        ArrayAdapter<String> options = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.settings_layout_option));
        options.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        layoutChoiceSpinner.setAdapter(options);
        loadViewChoice();

        refreshLayoutBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = getItemPosition(layoutChoiceSpinner.getSelectedItem().toString());
                saveViewChoice(pos);
                String selected = "Saved: ";
                selected += layoutChoiceSpinner.getSelectedItem().toString();
                Log.d(TAG, "onClick: " + selected);
                Toast.makeText(getContext(), "Layout Refreshed!", Toast.LENGTH_LONG).show();
            }
        });
    }
    //==============================================================================================

    /**
     * initShelfNameCard:
     *  Initializes the components inside the fragment and sets up respective functions and
     *   accepts/deletes shelf names.
     */
    private void initShelfNameCard() {
        Log.d(TAG, "initShelfNameCard: Initialized objects in the naming card");
        shelfName = settingsView.findViewById(R.id.settings_shelf_et);
        addShelfBTN = settingsView.findViewById(R.id.settings_add_btn);
        shelves = settingsView.findViewById(R.id.settings_shelf_names_lv);
        loadShelfNames();

        listViewAdapter = new ListViewAdapter(getContext(), R.layout.listview_single_item, shelfNames, this);
        shelves.setAdapter(listViewAdapter);

        addShelfBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shelfName.getText().toString().length() != 0) {
                    String newName = shelfName.getText().toString();
                    if (!shelfNames.contains(newName)) {
                        shelfNames.add(shelfName.getText().toString());
                        saveShelfName();
                    }
                    shelfName.setText("");
                } else {
                    Toast.makeText(getContext(), "Must add shelf name!", Toast.LENGTH_LONG).show();
                }
                listViewAdapter.notifyDataSetChanged();
            }
        });
    }
    //==============================================================================================

    /**
     * saveViewChoice:
     *  Saves the sort preference in the shared-preferences.
     * @param pos
     */
    private void saveViewChoice(int pos) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(MainActivity.SHARED_PREFERENCES,
                MainActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(MainActivity.VIEW, pos);
        editor.apply();
    }

    //==============================================================================================

    /**
     * loadViewChoice:
     *  Loads the sort preference in the shared-preferences and prefills it into the spinner.
     */
    private void loadViewChoice() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(MainActivity.SHARED_PREFERENCES,
                MainActivity.MODE_PRIVATE);
        int selectedChoice = sharedPreferences.getInt(MainActivity.VIEW, -1);
        if (selectedChoice == -1) {
            selectedChoice = 0;
        }
        layoutChoiceSpinner.setSelection(selectedChoice);
    }
    //==============================================================================================

    /**
     * getItemPosition:
     *  Gets the index at which the sort preference is located in the spinner.
     * @param selectedChoice
     * @return int, the index of sort preference
     */
    private int getItemPosition(String selectedChoice) {
        int pos = 0;
        switch (selectedChoice) {
            case "Alphabetical by Title":
                pos = 0; break;
            case "Alphabetical by Author":
                pos = 1; break;
            case "Sort by Shelf":
                pos = 2; break;
            default:
                pos = -1;
        }
        return pos;
    }
    //==============================================================================================

    /**
     * saveShelfName:
     *  Saves all the shelf names present in the list view into a single string and then saves into
     *   a shared-preference.
     */
    private void saveShelfName() {
        StringBuilder newNames = new StringBuilder();
        for (String name : shelfNames) {
            newNames.append(name);
            newNames.append("@");
        }
        newNames.deleteCharAt(newNames.length()-1);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(MainActivity.SHARED_PREFERENCES,
                MainActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(MainActivity.SHELVES, newNames.toString());
        editor.apply();
    }
    //==============================================================================================

    /**
     * loadShelfNames:
     *  Loads all teh shelf names from the shared-preference and breaks apart the string.
     */
    private void loadShelfNames() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(MainActivity.SHARED_PREFERENCES,
                MainActivity.MODE_PRIVATE);

        String storedNames = sharedPreferences.getString(MainActivity.SHELVES, "");
        if (storedNames.length() == 0) { storedNames = "Default";}

        String[] namesArr = storedNames.split("@",-1);
        shelfNames = new ArrayList<String>();

        for (String name : namesArr) { shelfNames.add(name);}
    }
    //==============================================================================================

    /**
     * deleteItem: (overridden method)
     *  Deletes the selected shelf name and removes it from the arraylist.
     * @param position
     */
    @Override
    public void deleteItem(int position) {
        String message = "Removed: " + shelfNames.get(position).toString();
        Log.d(TAG, "onItemClick-> removing: " + shelfNames.get(position).toString());
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        shelfNames.remove(position);
        listViewAdapter.notifyDataSetChanged();
        saveShelfName();
    }
}
