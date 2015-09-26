package com.zebenzi.zebenzi;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zebenzi.network.HttpGetUserDetailsTask;
import com.zebenzi.network.HttpPostLoginTask;
import com.zebenzi.network.IAsyncTaskListener;
import com.zebenzi.users.Customer;
import org.json.JSONException;
import org.json.JSONObject;



//TODO: Revisit how the token is saved and managed via variables

/**
 * A login screen that offers login via mobile number and password.
 */
public class LoginFragment extends Fragment {

    // Keep track of the async tasks to ensure we can cancel it if requested.
    private AsyncTask<String, String, String>  mLoginTask = null;
    private AsyncTask<String, String, String>  mUserDetailsTask = null;

    // UI references.
    private EditText mMobileNumberView;
    private EditText mPasswordView;
    private TextView mLoginTokenView;
    private View mProgressView;
    private View mLoginFormView;
    private String oAuthToken;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        // Set up the login form.
        mMobileNumberView = (EditText) rootView.findViewById(R.id.mobile_number);
        mPasswordView = (EditText) rootView.findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    login();
                    return true;
                }
                return false;
            }
        });

        mLoginTokenView = (TextView) rootView.findViewById(R.id.login_token);

        Button mEmailSignInButton = (Button) rootView.findViewById(R.id.customer_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });


        mLoginFormView = rootView.findViewById(R.id.login_form);
        mProgressView = rootView.findViewById(R.id.login_progress);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.my_awesome_toolbar);
        login(Customer.getInstance().getToken());

        return rootView;
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void login() {
        if (mLoginTask != null) {
            return;
        }

        // Reset errors.
        mMobileNumberView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String mobileNumber = mMobileNumberView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid mobile number.
        if (TextUtils.isEmpty(mobileNumber)) {
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
            mLoginTask = new HttpPostLoginTask(MainActivity.getAppContext(), new LoginTaskCompleteListener()).execute(mobileNumber, password);
        }
    }

    /**
     * Attempts to sign in using stored oAuth token
     */
    private void login(String token) {

        if (token != null) {
            oAuthToken = token;
            mLoginTokenView.setText(oAuthToken);

            //Get the User details and display
            if (mUserDetailsTask == null) {
                showProgress(true);
                mUserDetailsTask = new HttpGetUserDetailsTask(MainActivity.getAppContext(), new UserDetailsTaskCompleteListener()).execute(oAuthToken);
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

    /**
     * Once the login task completes successfully, we should have a valid token and can
     * obtain the user details.
     * If unsuccessful, place focus on input text field.
     */
    public class LoginTaskCompleteListener implements IAsyncTaskListener<String> {
        @Override
        public void onAsyncTaskComplete(String result) {

            mLoginTask = null;
            JSONObject jsonResult = null;

            showProgress(false);

            try {
                jsonResult = new JSONObject(result);
                oAuthToken = (String) jsonResult.get(getString(R.string.api_rest_access_token));

                if (oAuthToken != null) {
                    mLoginTokenView.setText(oAuthToken);
                    login(oAuthToken);
                }
                else {
                    System.out.println("Error occurred with login: " + jsonResult.toString());
                    mMobileNumberView.setError(getString(R.string.error_incorrect_mobile_or_password));
                    mMobileNumberView.requestFocus();
                    mLoginTokenView.setText(jsonResult.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onAsyncTaskCancelled() {
            mLoginTask = null;
            showProgress(false);
        }
    }

    /**
     * Once user details task completes successfully, we should have a JSONObject with all the user
     * data from the server.
     * If unsuccessful, place focus on input field.
     */
    public class UserDetailsTaskCompleteListener implements  IAsyncTaskListener<String>
    {
        @Override
        public void onAsyncTaskComplete(String result) {
            mUserDetailsTask = null;
            JSONObject jsonResult;
            String UserName;

            showProgress(false);

            try {
                jsonResult = new JSONObject(result);
                UserName = jsonResult.get("fullName").toString();
                Customer.getInstance().setCustomerDetails(jsonResult, oAuthToken);

                if (UserName != null) {
                    mLoginTokenView.setText(UserName);
                    Intent resultIntent = new Intent();
                    //TODO: Fix this once user data fields are fixed to be consistent in core.
                    resultIntent.putExtra("Username", UserName);

                    //TODO: Fix return values for Fragment
//                    setResult(RESULT_OK, resultIntent);
//                    // Eventually, we should save the token and display the logged-in user's name in the app.
//                    finish();
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
        public void onAsyncTaskCancelled() {
            mUserDetailsTask = null;
            showProgress(false);
        }
    }

}


