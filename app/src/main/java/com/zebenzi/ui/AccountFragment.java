package com.zebenzi.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.zebenzi.users.Customer;

import static com.zebenzi.ui.FragmentsLookup.HISTORY;


/**
 * An Account screen that user's details.
 */
public class AccountFragment extends Fragment {

    private FragmentListener fragmentListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final View rootView = inflater.inflate(R.layout.fragment_account, container, false);

        TextView tvFirstName = (TextView) rootView.findViewById(R.id.account_first_name);
        TextView tvLastName = (TextView) rootView.findViewById(R.id.account_last_name);
        TextView tvMobileNumber = (TextView) rootView.findViewById(R.id.account_mobile_number);
        TextView tvAddress = (TextView) rootView.findViewById(R.id.account_address);
        TextView tvJobsComplete = (TextView) rootView.findViewById(R.id.account_jobs_done);
        TextView tvJobsInProgress = (TextView) rootView.findViewById(R.id.account_jobs_in_progress);

        tvFirstName.setText(Customer.getInstance().getCustomerFirstName());
        tvLastName.setText(Customer.getInstance().getCustomerLastName());
        tvMobileNumber.setText(Customer.getInstance().getCustomerMobileNumber());
        tvAddress.setText(Customer.getInstance().getCustomerAddress().toString());
        tvJobsComplete.setText("Jobs complete: 5");
        tvJobsInProgress.setText("Jobs in progress: 1");

        Button myJobsButton = (Button) rootView.findViewById(R.id.accounts_job_history_button);
        myJobsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentListener.changeFragment(HISTORY, null);
            }
        });

        Button changePasswordButton = (Button) rootView.findViewById(R.id.accounts_change_password_button);
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.getAppContext(), MainActivity.getAppContext().getString(R.string.implementation_coming_soon), Toast.LENGTH_LONG).show();
            }
        });

        ImageView img = (ImageView) rootView.findViewById(R.id.account_image);
        try {
            Picasso.with(MainActivity.getAppContext()).load(Customer.getInstance().getCustomerImageUrl()).into(img);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            Activity activity = (Activity) context;
            fragmentListener = (FragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement FragmentListener");
        }
    }

}



