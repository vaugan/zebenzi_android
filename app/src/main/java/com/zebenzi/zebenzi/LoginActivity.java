package com.zebenzi.zebenzi;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zebenzi.network.IAsyncTaskListener;
import com.zebenzi.network.LoginTask;
import com.zebenzi.users.Customer;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


/**
 * A login screen that offers login via mobile number and password.
 */
public class LoginActivity extends ActionBarActivity {


    String userLoginURL = "http://www.zebenzi.com/oauth/token";
    String userDetailsURL = "http://www.zebenzi.com/api/accounts/user/current";

    private Customer customer = null;

//    public final static String user = "0846676467";
//    public final static String password = "dolphin";

    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private AsyncTask<String, String, String>  mLoginTask = null;
    private UserDetailsTask mUserDetailsTask = null;

    // UI references.
    private EditText mMobileNumberView;
    private EditText mPasswordView;
    private TextView mLoginTokenView;
    private View mProgressView;
    private View mLoginFormView;
    private String oAuthToken;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_account:
                return true;
            case R.id.action_register:
                intent = new Intent(this, RegisterCustomerActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_login:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        // Set up the login form.
        mMobileNumberView = (EditText) findViewById(R.id.mobile_number);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    loginWithUsernamePassword();
                    return true;
                }
                return false;
            }
        });

        mLoginTokenView = (TextView) findViewById(R.id.login_token);

        Button mEmailSignInButton = (Button) findViewById(R.id.customer_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                loginWithUsernamePassword();
            }
        });


        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

//        loginWithToken(Customer.getInstance().getToken());
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void loginWithUsernamePassword() {
        if (mLoginTask != null) {
            return;
        }

        // Reset errors.
        mMobileNumberView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mMobileNumberView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mMobileNumberView.setError(getString(R.string.error_field_required));
            focusView = mMobileNumberView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mLoginTask = new LoginTask(this, new LoginTaskCompleteListener()).execute(email, password);
        }
    }

    /**
     * Attempts to sign in using stored oAuth token
     */
    public void loginWithToken(String token) {

        if (token != null) {
            oAuthToken = token;
            mLoginTokenView.setText(oAuthToken);

            //Get the User details and display
            if (mUserDetailsTask == null) {
                showProgress(true);
                mUserDetailsTask = new UserDetailsTask();
                mUserDetailsTask.execute((String) null);
            }
        }
        else
        {
            //Do nothing, but wait for login
            System.out.println("The oAuth token is null!");
        }
    }
    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    public class LoginTaskCompleteListener implements IAsyncTaskListener<String> {
        @Override
        public void onAsyncTaskComplete(String result) {
            mLoginTask = null;
            showProgress(false);


            JSONObject jsonResult = null;
            try {
                jsonResult = new JSONObject(result);
                oAuthToken = (String) jsonResult.get(getString(R.string.api_access_token));
            } catch (JSONException e) {
                e.printStackTrace();
            }



            if (oAuthToken != null) {
                mLoginTokenView.setText(oAuthToken);
                loginWithToken(oAuthToken);
            }
            else {
                System.out.println("Error occurred with login: " + jsonResult.toString());
                mMobileNumberView.setError(getString(R.string.error_incorrect_mobile_or_password));
                mMobileNumberView.requestFocus();
                mLoginTokenView.setText(jsonResult.toString());

            }
        }

        @Override
        public void onAsyncTaskCancelled() {
            mLoginTask = null;
            showProgress(false);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<String, String, String> {

        private final String mMobileNumber;
        private final String mPassword;
        String resultToDisplay = null;

        UserLoginTask(String email, String password) {
            mMobileNumber = email;
            mPassword = password;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(userLoginURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod(getString(R.string.api_post));
                conn.setDoInput(true);
                conn.setDoOutput(true);

                List<NameValuePair> local_params = new ArrayList<NameValuePair>();
                local_params.add(new BasicNameValuePair(getString(R.string.api_username), mMobileNumber));
                local_params.add(new BasicNameValuePair(getString(R.string.api_password), mPassword));
                local_params.add(new BasicNameValuePair(getString(R.string.api_grant_type), getString(R.string.api_password)));


                //Send params via output stream
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, getString(R.string.api_utf8)));
                writer.write(getQuery(local_params));
                writer.flush();
                writer.close();
                os.close();

                conn.connect();

                if (conn.getResponseCode() / 100 == 2) { // 2xx code means success
                        //Read data from input stream
                        StringBuilder sb = new StringBuilder();
                        String line = "";
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        reader.close();

                        resultToDisplay = sb.toString();
                    }
                    else
                    {

                        StringBuilder sb = new StringBuilder();
                        String line = "";

                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        reader.close();
                        resultToDisplay = sb.toString();
                    }

            } catch (Exception e) {
                System.out.println(e.getMessage());
                return null;
            }

            return resultToDisplay;
        }

        //Encode the login params in UTF-8
        private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
        {
            StringBuilder result = new StringBuilder();
            boolean first = true;

            for (NameValuePair pair : params)
            {
                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(pair.getName(), getString(R.string.api_utf8)));
                result.append("=");
                result.append(URLEncoder.encode(pair.getValue(), getString(R.string.api_utf8)));
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(final String result) {
            mLoginTask = null;
            showProgress(false);


            JSONObject jsonResult = null;
            try {
                jsonResult = new JSONObject(result);
                oAuthToken = (String) jsonResult.get(getString(R.string.api_access_token));
            } catch (JSONException e) {
                e.printStackTrace();
            }



            if (oAuthToken != null) {
                mLoginTokenView.setText(oAuthToken);
                loginWithToken(oAuthToken);
            }
            else {
                System.out.println("Error occurred with login: " + jsonResult.toString());
                mMobileNumberView.setError(getString(R.string.error_incorrect_mobile_or_password));
                mMobileNumberView.requestFocus();
                mLoginTokenView.setText(jsonResult.toString());

            }
        }

        @Override
        protected void onCancelled() {
            mLoginTask = null;
            showProgress(false);
        }
    }




    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserDetailsTask extends AsyncTask<String, String, String> {

        UserDetailsTask() {

        }

        @Override
        protected String doInBackground(String... params) {
            String resultToDisplay;
            try {
                URL url = new URL(userDetailsURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod(getString(R.string.api_get));
//                conn.setDoInput(true);
//                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "bearer " + oAuthToken);


                conn.connect();

                if (conn.getResponseCode() / 100 == 2) { // 2xx code means success
                    //Read data from input stream
                    StringBuilder sb = new StringBuilder();
                    String line = "";
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();

                    resultToDisplay = sb.toString();
                }
                else
                {

                    StringBuilder sb = new StringBuilder();
                    String line = "";

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();
                    resultToDisplay = sb.toString();
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
                return null;
            }

            return resultToDisplay;
        }


        @Override
        protected void onPostExecute(final String result) {
            mUserDetailsTask = null;
            showProgress(false);
            String UserName = null;


            JSONObject jsonResult = null;
            try {
                jsonResult = new JSONObject(result);
                UserName = (String) jsonResult.get("fullName");
                Customer.getInstance().setCustomerDetails(jsonResult, oAuthToken);
//                customer.setCustomerDetails(jsonResult, oAuthToken);

                if (UserName != null) {
                    mLoginTokenView.setText(UserName);

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("Username", UserName);
                    setResult(RESULT_OK, resultIntent);

//                Eventually, we should save the token and display the logged-in user's name in the app.
                    finish();
                } else {
                    System.out.println("Error occurred with login: " + jsonResult.toString());
                    mMobileNumberView.setError(getString(R.string.error_incorrect_mobile_or_password));
                    mMobileNumberView.requestFocus();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }



        }

        @Override
        protected void onCancelled() {
            mUserDetailsTask = null;
            showProgress(false);
        }
    }

}



