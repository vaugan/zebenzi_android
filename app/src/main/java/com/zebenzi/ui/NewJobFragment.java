package com.zebenzi.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.zebenzi.service.Services;
import com.zebenzi.job.Quote;
import com.zebenzi.users.Customer;
import com.zebenzi.utils.DatePickerFragment;
import com.zebenzi.utils.TimePickerFragment;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static com.zebenzi.ui.FragmentsLookup.SEARCH;


//TODO: Revisit how the token is saved and managed via variables

/**
 * A login screen that offers login via mobile number and password.
 */
public class NewJobFragment extends Fragment {

    private FragmentListener fragmentListener;
    private Button jobDate;
    private int mYear;
    public int mMonth;
    private int mDay;
    private int mHour;
    private int mMin;
    private Button jobTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_new_job, container, false);

        final Spinner spinnerService = (Spinner) rootView.findViewById(R.id.new_job_service_name);
        ArrayList<String> spinnerArray= new ArrayList<String>() {
            {
                //TODO: Make this build dynamically from the enum
                add(Services.GARDENER.getName());
                add(Services.TILER.getName());
                add(Services.PAINTER.getName());
                add(Services.CLADDER.getName());
                add(Services.PAVER.getName());
                add(Services.PLASTERER.getName());
                add(Services.PLUMBER.getName());
            }};

        //Select date via datepicker fragment
        jobDate = (Button)rootView.findViewById(R.id.new_job_date);
        jobTime = (Button)rootView.findViewById(R.id.new_job_time);

        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy");
        String date = df.format(Calendar.getInstance().getTime());
        jobDate.setText(date);
        jobDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        df = new SimpleDateFormat("HH:mm");
        String time = df.format(Calendar.getInstance().getTime());
        jobTime.setText(time);
        jobTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(MainActivity.getAppContext(), R.layout.spinner_item, spinnerArray);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerService.setAdapter(spinnerArrayAdapter);

        final EditText units = (EditText) rootView.findViewById(R.id.new_job_units);
        units.setText("1");

        Button buttonGetQuote = (Button)  rootView.findViewById(R.id.new_job_get_quote);
        buttonGetQuote.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                String item = spinnerService.getSelectedItem().toString();
                Services quoteSvc = Services.of(item);
                int quoteUnits = Integer.parseInt(units.getText().toString());
                Quote q = new Quote(quoteSvc, quoteUnits);
                System.out.println("Quote price = " + q.getPrice());
                Customer.getInstance().setCurrentQuote(q);
                fragmentListener.changeFragment(SEARCH);
            }
        });


        return rootView;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            fragmentListener = (FragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentListener");
        }
    }

    /**
     * Handle the selection of date via dialog
     */
    private void showDatePicker() {
        //To show current date in the datepicker
        Calendar mcurrentDate = Calendar.getInstance();
        mYear = mcurrentDate.get(Calendar.YEAR);
        mMonth = mcurrentDate.get(Calendar.MONTH);
        mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

        DatePickerFragment mDatePicker = new DatePickerFragment();
        mDatePicker.setCallBack(mDateSetListener);
        mDatePicker.show(getFragmentManager(), "datePicker");
    }

    /**
     * Handle the selection of date via dialog
     */
    private void showTimePicker() {
        //To show current time in the timepicker
        Calendar mcurrentDate = Calendar.getInstance();
        mHour = mcurrentDate.get(Calendar.HOUR);
        mMin = mcurrentDate.get(Calendar.MINUTE);

        TimePickerFragment mTimePicker = new TimePickerFragment();
        mTimePicker.setCallBack(mTimeSetListener);
        mTimePicker.show(getFragmentManager(), "timePicker");

    }

    DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year,
                              int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            updateDate();
        }
    };

    TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hour,
                              int min) {
            mHour = hour;
            mMin = min;
            updateTime();
        }
    };

    private void updateDate() {
        GregorianCalendar c = new GregorianCalendar(mYear, mMonth, mDay);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy");
        jobDate.setText(sdf.format(c.getTime()));
    }

    private void updateTime() {
        GregorianCalendar c = new GregorianCalendar(mHour, mMonth, mDay, mHour, mMin);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        jobTime.setText(sdf.format(c.getTime()));
    }
}



