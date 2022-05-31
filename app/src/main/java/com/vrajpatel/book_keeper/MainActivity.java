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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements FragmentInteractionListener{

    // Shared Preferences
    public static final String SHARED_PREFERENCES = "shelves";
    public static final String SHELVES = "shelf_names";
    public static final String VIEW = "layout_view";

    // Shared Preferences to hold user info
    public static final String USER_SHARED_PREFERENCES = "user";
    public static final String USER_ID = "userID";
    public static final String USER_NAME = "name";
    public static final String USER_EMAIL = "email";
    public static final String USER_USERNAME = "username";
    public static final String USER_REMEMBER = "remember";

    // Fragment Identifiers
    private final int APP_STATS_FRAGMENT = -2;
    private final int SEARCH_FRAGMENT    = -1;
    private final int BOOK_VIEW_FRAGMENT = 0;
    private final int ADD_BOOK_FRAGMENT  = 1;
    private final int SETTINGS_FRAGMENT  = 2;

    // Login and Sign up fragment Identifiers
    public static final int LOGIN_COMPLETE = 0;
    public static final int LOGIN_FRAGMENT = 3;
    public static final int SIGNUP_FRAGMENT = 4;

    private static final String TAG = "MainActivity";
    public static final String URL = "http://192.168.0.14/includes/androidAPI.inc.php";
    private NavigationBarView bottomNav;
    private ProgressBar progressBar;
    private RelativeLayout activityHolder;
    private boolean serverConnected;
    private int currentPage;

    //==============================================================================================
    /**
     * onCreate: (overridden method)
     *  Creates the view of the fragment and binds all the components in the fragment for further
     *   use. Sets up the bottom navigation and prepares the fragments when navigating.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate: Main activity created.");
        // Get rid of the status bar and make activity fullscreen
        setContentView(R.layout.activity_main);

        // Attach the bottom navigation here and listener here
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);
        activityHolder = findViewById(R.id.main_activity_holder);
        progressBar = findViewById(R.id.main_progress_bar);
        progressBar.setVisibility(View.GONE);

        // Set default start page to be the book view page
        bottomNav.getMenu().findItem(R.id.nav_bookList).setChecked(true);
        
        // Check to see if we are logged in
        if (loadUser()) {
            Log.d(TAG, "onCreate: user is remembered...logging in");

            checkConnection();

            getSupportFragmentManager().beginTransaction().replace(R.id.container_frags,
                    new FragBookView(serverConnected)).commit();
        } else {
            Log.d(TAG, "onCreate: user not remembered");
            // Temporary set bottom nav to invisible
            bottomNav.setVisibility(View.GONE);
            getSupportFragmentManager().beginTransaction().replace(R.id.container_frags,
                    new FragLogin()).commit();
        }

    }
    //==============================================================================================

    /**
     * navListener
     *  This is the onclick listener used for the bottom navigation menu. The selected fragment
     *   is determined by obtaining the id of the selected icon and then using that to launch
     *   and/or set the fragment to the selected one. There is no default case since the app
     *   initially starts out with a provided fragment selected in the onCreate function above.
     */
    private NavigationBarView.OnItemSelectedListener navListener =
            new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFrag = null;
                    int previousPage = currentPage;
                    // get the next fragment
                    int itemID = item.getItemId();
                    if (itemID == R.id.nav_add && currentPage != ADD_BOOK_FRAGMENT) {
                        selectedFrag = new FragAddBook();
                        currentPage = ADD_BOOK_FRAGMENT;
                    } else if (itemID == R.id.nav_appstats && currentPage != APP_STATS_FRAGMENT) {
                        selectedFrag = new FragAppStats();
                        currentPage = APP_STATS_FRAGMENT;
                    } else if (itemID == R.id.nav_bookList && currentPage != BOOK_VIEW_FRAGMENT) {
                        selectedFrag = new FragBookView(serverConnected);
                        currentPage = BOOK_VIEW_FRAGMENT;
                    } else if (itemID == R.id.nav_search && currentPage != SEARCH_FRAGMENT) {
                        selectedFrag = new FragSearchBook();
                        currentPage = SEARCH_FRAGMENT;
                    } else if (itemID == R.id.nav_settings && currentPage != SETTINGS_FRAGMENT) {
                        selectedFrag = new FragSettings(serverConnected);
                        currentPage = SETTINGS_FRAGMENT;
                    } else {
                        Log.e(TAG, "onNavigationItemSelected: Invalid navigation item was selected [ID = " + Integer.toString(itemID));
                        return false;
                    }

                    // Start the next Fragment with the correct animation
                    Log.d(TAG, "onNavigationItemSelected: Transition to new fragment.");
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    if (determineTransitionAnimation(previousPage)) {
                        transaction.setCustomAnimations(R.anim.anim_enter_from_right, R.anim.anim_exit_to_right);
                    } else {
                        transaction.setCustomAnimations(R.anim.anim_enter_from_left, R.anim.anim_exit_to_left);
                    }
                    transaction.add(R.id.container_frags, selectedFrag).commit();
                    return true;
                }
            };
    //==============================================================================================

    /**
     * determineTransitionAnimation
     *   This function is used to determine which directions to set the animations. Using the
     *    int values that represent pages, the proper transition is chosen. True indicates right,
     *    false indicates left.
     *
     * @param atPage    the current page/fragment that the user is on
     * @return boolean  true if navigating right, else false
     */
    private boolean determineTransitionAnimation(int atPage) {
        boolean transitionRight = false;
        if (currentPage > atPage) { transitionRight = true; }
        return transitionRight;
    }

    //==============================================================================================

    @Override
    public void changeFrag(int id) {
        Fragment selectedFrag = null;
        switch (id) {
            case LOGIN_FRAGMENT:
                selectedFrag = new FragLogin();
                break;
            case SIGNUP_FRAGMENT:
                selectedFrag = new FragSignUp();
                break;
            case LOGIN_COMPLETE:
                bottomNav.setVisibility(View.VISIBLE);
                checkConnection();
                selectedFrag = new FragBookView(serverConnected);
                break;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.container_frags, selectedFrag).commit();
    }

    //==============================================================================================

    private boolean loadUser() {
        boolean remembered = false;
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.USER_SHARED_PREFERENCES,MainActivity.MODE_PRIVATE);
        String name = sharedPreferences.getString(MainActivity.USER_NAME, "");
        String username = sharedPreferences.getString(MainActivity.USER_USERNAME, "");
        String email = sharedPreferences.getString(MainActivity.USER_EMAIL, "");
        remembered = sharedPreferences.getBoolean(MainActivity.USER_REMEMBER, false);
        return remembered && name.length() > 0 && username.length() > 0 && email.length() > 0;
    }

    //==============================================================================================

    private void checkConnection() {
        activityHolder.setAlpha(0.5f);
        progressBar.setVisibility(View.VISIBLE);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    // Reset the view and hide the progress bar
                    activityHolder.setAlpha(1f);
                    progressBar.setVisibility(View.GONE);

                    JSONObject responseOBJ = new JSONObject(response);
                    boolean error = responseOBJ.getBoolean("error");
                    Log.d(TAG, "onResponse: " + response);
                    Log.d(TAG, "onResponse: [CONNECTION] " + serverConnected);
                    serverConnected = !error;


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                activityHolder.setAlpha(1f);
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "onErrorResponse: " + error.getMessage());
                Log.d(TAG, "onResponse: [CONNECTION] " + serverConnected);
                serverConnected = false;
                Toast.makeText(getApplicationContext(), "Currently Offline", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "connection");
                return params;
            }
        };
        Volley.newRequestQueue(getApplicationContext()).add(stringRequest);
    }
}
