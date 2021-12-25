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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FragSettings extends Fragment {

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
    private ArrayAdapter<String> arrayAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        settingsView = inflater.inflate(R.layout.frag_settings_layout, container, false);
        Log.d(TAG, "onCreateView: creating view of settings page");
        initLayoutPrefCard();
        initShelfNameCard();
        return settingsView;
    }
    //==================-Functions for Initializing objects in the View-==================
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
            }
        });
    }

    private void initShelfNameCard() {
        Log.d(TAG, "initShelfNameCard: Initialized objects in the naming card");
        shelfName = settingsView.findViewById(R.id.settings_shelf_et);
        addShelfBTN = settingsView.findViewById(R.id.settings_add_btn);
        shelves = settingsView.findViewById(R.id.settings_shelf_names_lv);
        shelfNames = new ArrayList<String>();
        shelfNames.addAll(loadShelfNames());
        arrayAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1, shelfNames);
        shelves.setAdapter(arrayAdapter);

        addShelfBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shelfName.getText().toString().length() != 0) {
                    String newName = shelfName.getText().toString();
                    if (!shelfNames.contains(newName)) {
                        shelfNames.add(shelfName.getText().toString());
                        saveShelfName(shelfName.getText().toString());
                    }
                    shelfName.setText("");
                } else {
                    Toast.makeText(getContext(), "Must add shelf name!", Toast.LENGTH_LONG).show();
                }
                arrayAdapter.notifyDataSetChanged();
            }
        });

        // remove temporarily
        shelves.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (shelfNames.get(i) != "Default") {
                    Log.d(TAG, "onItemClick-> removing: " + adapterView.getItemAtPosition(i).toString());
                    shelfNames.remove(i);
                    arrayAdapter.notifyDataSetChanged();
                    saveShelfName(null);
                } else { Log.e(TAG, "onItemClick -> attempted to remove 'Default'");}
            }
        });
    }

    //==================-Functions for Choosing view and applying that=-==================
    private void saveViewChoice(int pos) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(MainActivity.SHARED_PREFERENCES,
                MainActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(MainActivity.VIEW, pos);
        editor.apply();
    }

    private void loadViewChoice() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(MainActivity.SHARED_PREFERENCES,
                MainActivity.MODE_PRIVATE);
        int selectedChoice = sharedPreferences.getInt(MainActivity.VIEW, -1);
        if (selectedChoice == -1) {
            selectedChoice = 0;
        }
        layoutChoiceSpinner.setSelection(selectedChoice);
    }

    private int getItemPosition(String selectedChoice) {
        int pos = 0;
        switch (selectedChoice) {
            case "Alphabetical by Title":
                pos = 0; break;
            case "Alphabetical by Author":
                pos = 1; break;
            case "Alphabetical by Shelf Name":
                pos = 2; break;
            default:
                pos = -1;
        }
        return pos;
    }

    //==================-Functions for Naming Shelves and Storing Names-==================
    private Set<String> getNames() {
        Set<String> names = new HashSet<String>();
        for (int i = 0; i < arrayAdapter.getCount(); i++) {
            names.add(arrayAdapter.getItem(i).toString());
        }
        names.add(shelfName.getText().toString());
        return names;
    }

    private void saveShelfName(String name) {
        Set<String> names = getNames();
        if (name != null) {names.add(name);}
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(MainActivity.SHARED_PREFERENCES,
                MainActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(MainActivity.SET, names);
        editor.apply();
    }

    private Set<String> loadShelfNames() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(MainActivity.SHARED_PREFERENCES,
                MainActivity.MODE_PRIVATE);
        Set<String> set = sharedPreferences.getStringSet(MainActivity.SET, new HashSet<String>());
        set.remove("");
        if (!set.contains("Default")) { set.add("Default");}
        return set;
    }
}
