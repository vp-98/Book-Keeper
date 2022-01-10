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
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationBarView;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    // Shared Preferences
    public static final String SHARED_PREFERENCES = "shelves";
    public static final String SHELVES = "shelf_names";
    public static final String VIEW = "layout_view";

    // Fragment Identifiers
    private final int APP_STATS_FRAGMENT = -2;
    private final int SEARCH_FRAGMENT    = -1;
    private final int BOOK_VIEW_FRAGMENT = 0;
    private final int ADD_BOOK_FRAGMENT  = 1;
    private final int SETTINGS_FRAGMENT  = 2;

    private static final String TAG = "MainActivity";
    private NavigationBarView bottomNav;
    private int currentPage;

    // ------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Main activity created.");
        // Get rid of the status bar and make activity fullscreen
        setContentView(R.layout.activity_main);

        // Attach the bottom navigation here and listener here
        bottomNav =findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        // Set default start page to be the book view page
        bottomNav.getMenu().findItem(R.id.nav_bookList).setChecked(true);

        getSupportFragmentManager().beginTransaction().replace(R.id.container_frags,
                new FragBookView()).commit();

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
                        selectedFrag = new FragBookView();
                        currentPage = BOOK_VIEW_FRAGMENT;
                    } else if (itemID == R.id.nav_search && currentPage != SEARCH_FRAGMENT) {
                        selectedFrag = new FragSearchBook();
                        currentPage = SEARCH_FRAGMENT;
                    } else if (itemID == R.id.nav_settings && currentPage != SETTINGS_FRAGMENT) {
                        selectedFrag = new FragSettings();
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
     *    int values that represent pages, the proper transiton is choosen. True indicates right,
     *    false indicates left.
     *
     * @param atPage  the current page/fragment that the user is on
     * @return        true or false
     */
    private boolean determineTransitionAnimation(int atPage) {
        boolean transitionRight = false;
        if (currentPage > atPage) { transitionRight = true; }
        return transitionRight;
    }
}


