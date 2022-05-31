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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FragSettings extends Fragment implements ListViewAdapter.onDeleteIconPressListener {

    private static final String TAG = "Settings Fragment";
    // View for this fragment
    private View settingsView;

    // For layout preference card
    private RelativeLayout settingsHolder;
    private ProgressBar progressBar;
    private Button refreshLayoutBTN;
    private Spinner layoutChoiceSpinner;
    private boolean onlineStatus;
    private int userID;

    // For shelf naming card
    private EditText shelfName;
    private Button addShelfBTN;
    private ListView shelves;
    private ArrayList<String> shelfNames;
    private ListViewAdapter listViewAdapter;

    // Handling account preferences
    private TextView accountName;
    private SwitchCompat remember_switch;
    private Button transferBTN;

    // Handling the transfer of books and shelves
    private DatabaseHelper mDatabaseHelper;

    //==============================================================================================

    public FragSettings(boolean onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

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
        settingsHolder = settingsView.findViewById(R.id.settings_holder);
        progressBar = settingsView.findViewById(R.id.settings_progress_bar);
        progressBar.setVisibility(View.GONE);

        Log.d(TAG, "onCreateView: creating view of settings page");
        initLayoutPrefCard();
        initShelfNameCard();
        initAccountCard();
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

    private void initAccountCard() {
        Log.d(TAG, "initAccountCard: Initialized objects in the account card");
        accountName = settingsView.findViewById(R.id.settings_user_name);
        remember_switch = settingsView.findViewById(R.id.settings_account_remember);
        transferBTN = settingsView.findViewById(R.id.settings_transfer_btn);
        loadUserData();

        remember_switch.setOnClickListener(v -> {
            saveUserData(remember_switch.isChecked());
        });

        transferBTN.setOnClickListener(v -> {
            if (onlineStatus) {
                // transfer content
                settingsHolder.setAlpha(0.2f);
                progressBar.setVisibility(View.VISIBLE);
                if (transferShelves()) {
                    transferBooks();
                }
            } else {
                Toast.makeText(getContext(), "Not Connected to Server", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean transferShelves() {
        boolean transfered = false;
        for (String shelf : shelfNames) {
            transfered = addShelfToServer(shelf);
        }
        return transfered;
    }

    private void transferBooks() {
        mDatabaseHelper = new DatabaseHelper(getContext());
        // Populate the list
        ArrayList<BookModel> books = mDatabaseHelper.getStoredBooks();
        for (BookModel book : books) {
            addBookToServer(book);
        }
    }

    private boolean addShelfToServer(String shelf) {
        final boolean[] success = {true};
        StringRequest stringRequest = new StringRequest(Request.Method.POST, MainActivity.URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    // Reset the view and hide the progress bar
                    JSONObject responseOBJ = new JSONObject(response);
                    boolean error = responseOBJ.getBoolean("error");
                    Log.d(TAG, "onResponse: " + response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: " + error.getMessage());
                settingsHolder.setAlpha(1f);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Conenction Lost", Toast.LENGTH_SHORT).show();
                success[0] = false;
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "add-shelf");
                params.put("userID", Integer.toString(userID));
                params.put("shelfName", shelf);
                return params;
            }
        };
        Volley.newRequestQueue(getContext()).add(stringRequest);
        return success[0];
    }

    private void addBookToServer(BookModel book) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, MainActivity.URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    // Reset the view and hide the progress bar
                    settingsHolder.setAlpha(1f);
                    progressBar.setVisibility(View.GONE);

                    JSONObject responseOBJ = new JSONObject(response);
                    boolean error = responseOBJ.getBoolean("error");
                    Log.d(TAG, "onResponse: " + response);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                settingsHolder.setAlpha(1f);
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "onErrorResponse: " + error.getMessage());

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "add-book");
                params.put("userID", Integer.toString(userID));
                params.put("title", book.getTitle());
                params.put("author", book.getAuthor());
                params.put("shelf", book.getShelfLocation());
                params.put("status", book.getReadStatus()? "on" : "none");
                return params;
            }
        };
        Volley.newRequestQueue(getContext()).add(stringRequest);
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

    private void loadUserData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(MainActivity.USER_SHARED_PREFERENCES,
                MainActivity.MODE_PRIVATE);
        String username = sharedPreferences.getString(MainActivity.USER_USERNAME, "");
        userID = sharedPreferences.getInt(MainActivity.USER_ID, -999);
        boolean remember = sharedPreferences.getBoolean(MainActivity.USER_REMEMBER, false);

        accountName.setText(username);
        remember_switch.setChecked(remember);
    }

    private void saveUserData(boolean remember) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(MainActivity.USER_SHARED_PREFERENCES,
                MainActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(MainActivity.USER_REMEMBER, remember);
        editor.apply();
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
