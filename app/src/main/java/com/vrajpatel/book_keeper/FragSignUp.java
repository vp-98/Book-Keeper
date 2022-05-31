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
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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

public class FragSignUp extends Fragment {

    private RelativeLayout signUpHolder;
    private EditText name;
    private EditText username;
    private EditText email;
    private EditText password;
    private EditText password_conf;
    private CheckBox termsBox;
    private Button signupBTN;
    private TextView loginLink;
    private ProgressBar progressBar;
    private FragmentInteractionListener fragmentListener;
    private static final String TAG = "FragSignUp";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_frag_sign_up, container, false);
        signUpHolder = (RelativeLayout) view.findViewById(R.id.signup_holder);
        name = (EditText) view.findViewById(R.id.signup_name_field);
        username = (EditText) view.findViewById(R.id.signup_username_field);
        email = (EditText) view.findViewById(R.id.signup_email_field);
        password = (EditText) view.findViewById(R.id.signup_password_field);
        password_conf = (EditText) view.findViewById(R.id.signup_password_conf_field);
        termsBox = (CheckBox) view.findViewById(R.id.signup_terms_box);
        loginLink = (TextView) view.findViewById(R.id.signup_login_btn);
        signupBTN = (Button) view.findViewById(R.id.signup_btn);
        progressBar = (ProgressBar) view.findViewById(R.id.signup_progress_bar);
        progressBar.setVisibility(View.GONE);

        // Button actions
        signupBTN.setOnClickListener(v -> {
            Log.d(TAG, "onCreateView: signup submitted");
            String name_str = name.getText().toString();
            String username_str = username.getText().toString();
            String email_str = email.getText().toString();
            String pass_str = password.getText().toString();
            String pass_conf_str = password_conf.getText().toString();
            boolean checkedTerms = termsBox.isChecked();

            if (validInputs(name_str, username_str, email_str, pass_str, pass_conf_str, checkedTerms)) {
                signUpHolder.setAlpha(0.2f);
                progressBar.setVisibility(View.VISIBLE);
                signupUser(name_str, username_str, email_str, pass_str);
            }

        });

        loginLink.setOnClickListener(v -> {
            fragmentListener.changeFrag(MainActivity.LOGIN_FRAGMENT);
        });

        return view;
    }

    //==============================================================================================

    private void signupUser(String name_str, String username_str, String email_str, String pass_str) {
        StringRequest stringRequest =  new StringRequest(Request.Method.POST, MainActivity.URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    // Reset the view and hide progress bar
                    progressBar.setVisibility(View.GONE);
                    signUpHolder.setAlpha(1f);

                    JSONObject obj = new JSONObject(response);
                    boolean error = obj.getBoolean("error");
                    Log.d(TAG, "onResponse: " + response);
                    
                    if (!error) {
                        Log.d(TAG, "onResponse: Signup completed without errors");
                        
                        // Get user information to store from response
                        String id = obj.getString("id");
                        int userID = Integer.parseInt(id);
                        JSONObject user = obj.getJSONObject("user");
                        String obj_name = user.getString("name");
                        String obj_username = user.getString("username");
                        String obj_email = user.getString("email");

                        // Save data into shared preferences for future login
                        saveData(obj_name, obj_email, obj_username, userID);
                        fragmentListener.changeFrag(MainActivity.LOGIN_COMPLETE);

                    } else {
                        if (obj.getString("error-val").equals("user-or-email-exists")) {
                            Toast.makeText(getContext(), "Username/Email is in use", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Reset the view and hide progress bar
                progressBar.setVisibility(View.GONE);
                signUpHolder.setAlpha(1f);
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
                Map<String, String> params = new HashMap<>();
                params.put("tag", "signup");
                params.put("fname", name_str);
                params.put("username", username_str);
                params.put("email", email_str);
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
        editor.putBoolean(MainActivity.USER_REMEMBER, false);
        editor.apply();
    }

    //==============================================================================================


    private boolean validInputs(String name_str, String username_str, String email_str,
                                String pass_str, String pass_conf_str, boolean checkedTerms) {
        boolean valid = false;

        if (!checkedTerms) {
            Toast.makeText(getContext(), "Please accepts terms of service", Toast.LENGTH_SHORT).show();
        } else if (name_str.isEmpty() || username_str.isEmpty() || email_str.isEmpty() ||
                pass_str.isEmpty() || pass_conf_str.isEmpty()) {
            password.setText("");
            password_conf.setText("");
            Toast.makeText(getContext(), "Inputs are invalid", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email_str).matches()) {
            Toast.makeText(getContext(), "Email is not valid", Toast.LENGTH_SHORT).show();
        } else if (!pass_str.equals(pass_conf_str)) {
            password.setText("");
            password_conf.setText("");
            password.requestFocus();
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
        } else { valid = true; }
        return valid;
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