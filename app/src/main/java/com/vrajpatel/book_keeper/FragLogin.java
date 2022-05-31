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

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class FragLogin extends Fragment {
    private RelativeLayout loginHolder;
    private EditText username_field;
    private EditText password_field;
    private SwitchCompat remember_switch;
    private Button login_btn;
    private ProgressBar progressBar;
    private TextView signupLink;
    private FragmentInteractionListener fragmentListener;
    private static final String TAG = "FragLogin";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_frag_login, container, false);
        loginHolder = (RelativeLayout) view.findViewById(R.id.login_holder);
        username_field = (EditText) view.findViewById(R.id.login_username_field);
        password_field = (EditText) view.findViewById(R.id.login_password_field);
        signupLink = (TextView) view.findViewById(R.id.login_create_btn);
        login_btn = (Button) view.findViewById(R.id.login_btn);
        remember_switch = (SwitchCompat) view.findViewById(R.id.login_remember_switch);
        progressBar = (ProgressBar) view.findViewById(R.id.login_progress_bar);
        progressBar.setVisibility(View.GONE);

        // Load data if anything is stored
        loadData();

        // Button actions
        login_btn.setOnClickListener(v -> {
            Log.d(TAG, "onCreateView: login submitted");
            String name_str = username_field.getText().toString();
            String pass_str = password_field.getText().toString();

            if (validInputs(name_str, pass_str)) {
                loginHolder.setAlpha(0.2f);
                progressBar.setVisibility(View.VISIBLE);
                loginUser(name_str, pass_str);
            }
        });

        signupLink.setOnClickListener(v -> {
            fragmentListener.changeFrag(MainActivity.SIGNUP_FRAGMENT);
        });

        return view;
    }
    //==============================================================================================

    private boolean validInputs(String name_str, String pass_str) {
        boolean valid = false;
        if (name_str.isEmpty() || pass_str.isEmpty()) {
            password_field.setText("");
            Toast.makeText(getContext(), "Invalid Inputs", Toast.LENGTH_SHORT).show();
        } else { valid = true; }
        return valid;
    }

    //==============================================================================================

    private void loginUser(String name_str, String pass_str) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, MainActivity.URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    // Reset the view and hide the progress bar
                    loginHolder.setAlpha(1f);
                    progressBar.setVisibility(View.GONE);

                    JSONObject responseOBJ = new JSONObject(response);
                    boolean error = responseOBJ.getBoolean("error");
                    Log.d(TAG, "onResponse: " + response);

                    if (!error) {
                        Log.d(TAG, "onResponse: Login completed without errors");

                        // Get user information from stored table
                        String id = responseOBJ.getString("id");
                        int userID = Integer.parseInt(id);
                        JSONObject user = responseOBJ.getJSONObject("user");
                        String obj_name = user.getString("name");
                        String obj_username = user.getString("username");
                        String obj_email = user.getString("email");

                        // Save data into shared preferences for future login
                        saveData(obj_name, obj_email, obj_username, userID);
                        fragmentListener.changeFrag(MainActivity.LOGIN_COMPLETE);
                    } else {
                        // Error handling
                        Toast.makeText(getContext(), "Invalid login", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loginHolder.setAlpha(1f);
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "onErrorResponse: " + error.getMessage());
                String connection_drop = "java.net.ConnectException: Failed to connect to /192.168.0.14:80";
                if (error != null || (error != null && error.getMessage().equals(connection_drop))) {
                    Toast.makeText(getContext(), "Connection Timed Out", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "login");
                params.put("username", name_str);
                params.put("password", pass_str);
                return params;
            }
        };
        Volley.newRequestQueue(getContext()).add(stringRequest);
    }

    //==============================================================================================

    private void saveData(String name_stored, String email_stored, String username_stored, int userID) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(MainActivity.USER_SHARED_PREFERENCES,
                MainActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(MainActivity.USER_NAME, name_stored);
        editor.putString(MainActivity.USER_USERNAME, username_stored);
        editor.putString(MainActivity.USER_EMAIL, email_stored);
        editor.putInt(MainActivity.USER_ID, userID);
        editor.putBoolean(MainActivity.USER_REMEMBER, remember_switch.isChecked());
        editor.apply();
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(MainActivity.USER_SHARED_PREFERENCES,MainActivity.MODE_PRIVATE);
        String username = sharedPreferences.getString(MainActivity.USER_USERNAME, "");
        boolean remembered = sharedPreferences.getBoolean(MainActivity.USER_REMEMBER, false);
        if (!remembered && !username.isEmpty()) { username_field.setText(username);}
    }

    //==============================================================================================
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            fragmentListener = (FragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentListener = null;
    }
}