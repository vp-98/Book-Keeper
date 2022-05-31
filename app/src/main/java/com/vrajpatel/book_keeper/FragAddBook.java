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
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FragAddBook extends Fragment {

    private static final String TAG = "FragAddBook";
    private DatabaseHelper mDatabaseHelper;
    private Button submitBTN, resetBTN;
    private SwitchCompat readStatus;
    private EditText authorField;
    private EditText titleField;
    private Spinner spinner;
    private int userID;

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
        View view = inflater.inflate(R.layout.frag_addbook_layout, container, false);
        loadUser();

        titleField = view.findViewById(R.id.addBook_title_edit_text);
        authorField = view.findViewById(R.id.addBook_author_edit_text);
        submitBTN = view.findViewById(R.id.button);
        readStatus = view.findViewById(R.id.my_switch);
        resetBTN = view.findViewById(R.id.addbook_resetBTN);
        spinner = view.findViewById(R.id.addBook_spinner);

        return view;
    }
    //==============================================================================================

    /**
     * onViewCreated: (overridden method)
     *  Sets up the fragment and initializes the page. Functions dealing with resetting fields,
     *   adding books, and user input are initialized here.
     * @param view
     * @param savedInstanceState
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDatabaseHelper = new DatabaseHelper(getContext());

        ArrayList<String> storedNames = loadShelfNames();

        ArrayAdapter<String> dropDownArrayAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, storedNames);
        spinner.setAdapter(dropDownArrayAdapter);
        spinner.setSelection(0);

        resetBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readStatus.setChecked(false);
                readStatus.setText(R.string.read_status_false);
                titleField.setText("");
                authorField.setText("");
            }
        });

        readStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (readStatus.isChecked()) {
                    readStatus.setText(R.string.read_status_true);
                } else {
                    readStatus.setText(R.string.read_status_false);
                }
            }
        });

        submitBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleField.getText().toString();
                String author = authorField.getText().toString();
                String shelfLocation = spinner.getSelectedItem().toString();
                boolean readBook = readStatus.isChecked();
                BookModel book = new BookModel(title, title.toLowerCase(), author, readBook, -1, shelfLocation);
                if (title.length() > 0) {
                    // Attempt to add to DB and report success/failure
                    if (mDatabaseHelper.addData(title, author, title.toLowerCase(), readBook, shelfLocation)) {
                        String message = "Title: " + title + ", Author: " + author;
                        Log.d(TAG, "onClick: Retrieved Information: " + message);
                        displayMessageMaker("Book Added!", 0, null);
                        if (userID != -999) { addBookToServer(book); }
                        resetAllFields();
                    } else {
                        Log.e(TAG, "onClick: Book was not able to be added: " + title + " by: " + author);
                        displayMessageMaker("Book could not be added!", 0, null);
                    }
                } else {
                    displayMessageMaker("Please enter valid title!",0, null);
                }
                titleField.requestFocus();
            }
        });
    }
    //==============================================================================================

    /**
     * resetAllFields:
     *  Clears all the fields and sets them to default values.
     */
    private void resetAllFields() {
        CharSequence empty = "";
        titleField.setText(empty);
        authorField.setText(empty);
        readStatus.setChecked(false);
        readStatus.setText(R.string.read_status_false);
    }
    //==============================================================================================

    /**
     * displayMessageMaker:
     *  Generate a popup toast message onto the screen. Primarily to show
     *   the successful completion of a given task or to inform the user
     *   what is currently happening (debugging purposes too).
     *   Snackbar or Toasts can be made. Option 1: Snackbar, requires view.
     *   other default options: Toasts, only requires message.
     * @param message  Message to print
     * @param type     type of message, snack bar or toast
     * @param view     view in message is going to appear in
     */
    public void displayMessageMaker(String message, int type, View view) {
        switch (type) {
            case 1:
                if (view != null) {
                    Snackbar.make(view, message, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                } else {
                    Snackbar.make(view, "**ERROR**", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                }
                break;
            default:
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                break;
        }
    }
    //==============================================================================================

    /**
     * loadShelfNames:
     *  Extracts the saved shelf names that the user has defined from the shared-preferences.
     * @return ArrayList of strings holding the shelf names.
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

    //==============================================================================================

    private void addBookToServer(BookModel book) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, MainActivity.URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
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

    private void loadUser() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(MainActivity.USER_SHARED_PREFERENCES,MainActivity.MODE_PRIVATE);
        userID = sharedPreferences.getInt(MainActivity.USER_ID, -1);
    }
}
