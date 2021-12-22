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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FragSettings extends Fragment {

    private static final String TAG = "Settings Fragment";

    private EditText shelfName;
    private Button addShelfBTN;
    private ListView shelfs;
    private ArrayList<String> shelfNames;
    private ArrayAdapter<String> arrayAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_settings_layout, container, false);

        shelfName = view.findViewById(R.id.settings_shelf_et);
        addShelfBTN = view.findViewById(R.id.settings_add_btn);
        shelfs = view.findViewById(R.id.settings_shelf_names_lv);

        shelfNames = new ArrayList<String>();
        shelfNames.addAll(loadShelfNames());

        arrayAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1, shelfNames);
        shelfs.setAdapter(arrayAdapter);

        addShelfBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shelfName.getText().toString().length() != 0) {
                    saveShelfName(shelfName.getText().toString());
                } else {
                    Toast.makeText(getContext(), "Must add shelf name!", Toast.LENGTH_LONG).show();
                }
            }
        });

        // remove temporarily
        shelfs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getItemAtPosition(i).toString() != "Default") {
                    shelfNames.remove(i);
                    arrayAdapter.notifyDataSetChanged();
                    saveShelfName(null);
                } else {
                    Toast.makeText(getContext(), "Cannot remove 'Default' shelf", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

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
        if (!set.contains("Default")) {
            set.add("Default");
        }
        return set;
    }
}
