package students.college.freefood;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Calendar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

/**
 * Created by Robert Bradshaw on 10/7/2017.
 */

public class AddEvent extends UserActivity
{
    private FreeFoodEvent ffe;
    private TextView tvStartDate,tvEndDate, tvStartTime, tvEndTime, tvAddress;
    private Calendar mCal;

    private int startDay,startMonth,startYear,endDay,endMonth,endYear;
    private int startHour,startMin, endHour, endMin;
    private double lat, lng;

    private String name, description, location, category;
    private static final int REQUEST_PLACE_PICKER = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_event);

        //Set Time Spinner
        Spinner spinner = (Spinner) findViewById(R.id.spCategory);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.category_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        setUpCalendarVars();
        setUpTheListeners();

    }

    public void addThisEvent(View view)
    {

        EditText etName = ((EditText)findViewById(R.id.etName));
        EditText etDescription = ((EditText)findViewById(R.id.etDescription));

        name = etName.getText().toString();
        description = etDescription.getText().toString();
        location =  tvAddress.getText().toString();

        String startTime = startYear+"-"+(startMonth+1)+"-"+ startDay +"%20"+ startHour +":"+ startMin +":00";
        String endTime = endYear +"-"+ (endMonth+1) +"-"+endDay+"%20"+ endHour +":"+ endMin +":00";
        category = ((Spinner)findViewById(R.id.spCategory)).getSelectedItem().toString();

        String response = "";
        int failed = 0;
        if(name.equals("")){
            etName.setBackgroundColor(Color.RED);
            failed++;
        }
        else{
            etName.setBackgroundColor(Color.WHITE);
        }
        if(description.equals("")){
            etDescription.setBackgroundColor(Color.RED);
            failed++;
        }
        else{
            etDescription.setBackgroundColor(Color.WHITE);
        }
        if(location.equals("Choose a Location")){
            tvAddress.setTextColor(Color.RED);
            failed++;
        }
        else{
            tvAddress.setTextColor(Color.BLACK);
        }
        if(failed>0){
            return;
        }
        new API.hitPage().execute("http://ec2-54-226-112-134.compute-1.amazonaws.com/" +
                "add.php?name=%22" + name + "%22&lat=" + lat + "&long=" + lng + "&description=%22" + description +
                "%22&" + "start=%22" +startTime+ "%22&end=%22" + endTime+ "%22&category=%22"+category+"%22&" +
                "image=%22%22&address=%22" + location + "%22");

        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.putExtra("Toast","Your event has been added!");
        startActivity(intent);
    }
    private void setUpCalendarVars(){
        mCal = Calendar.getInstance();
        startDay = mCal.get(Calendar.DAY_OF_MONTH);
        startMonth = mCal.get(Calendar.MONTH);
        startYear = mCal.get(Calendar.YEAR);
        startHour = mCal.get(Calendar.HOUR_OF_DAY);
        startMin = mCal.get(Calendar.MINUTE);

        endDay = mCal.get(Calendar.DAY_OF_MONTH);
        endMonth = mCal.get(Calendar.MONTH);
        endYear = mCal.get(Calendar.YEAR);
        endHour = mCal.get(Calendar.HOUR_OF_DAY);
        endMin = mCal.get(Calendar.MINUTE);

        tvStartDate = (TextView) findViewById(R.id.buttonDate1);
        tvStartTime = (TextView) findViewById(R.id.buttonTime1);

        tvEndDate = (TextView) findViewById(R.id.buttonDate2);
        tvEndTime = (TextView) findViewById(R.id.buttonTime2);

        tvAddress = (TextView) findViewById(R.id.etLocation);
    }

    private void setUpTheListeners() {
        //set the text that's displayed initially
        System.out.println((startMonth+1)+"/"+startDay +"/"+startYear);
        tvStartDate.setText((startMonth+1)+"/"+startDay +"/"+startYear);
        tvStartTime.setText(startHour +":"+ startMin);
        System.out.println((endMonth+1) +"/" + endDay+"/"+ endYear);
        tvEndDate.setText((endMonth+1) +"/" + endDay+"/"+ endYear);
        tvEndTime.setText(endHour +":"+ endMin);

        //do this when the start date is clicked
        tvStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v1) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddEvent.this,new DatePickerDialog.OnDateSetListener(){
                    @Override
                    public void onDateSet(DatePicker view1, int year1, int monthOfYear1, int dayOfMonth1) {
                        //Change the text shown
                        tvStartDate.setText((monthOfYear1+1)+"/"+dayOfMonth1+"/"+year1);
                        //change the variables
                        startDay = dayOfMonth1;
                        startMonth = monthOfYear1;
                        startYear = year1;
                    }
                }, startYear, startMonth, startDay);
                datePickerDialog.show();
            }
        });

        //do this when the end date is clicked
        tvEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v2) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddEvent.this,new DatePickerDialog.OnDateSetListener(){
                    @Override
                    public void onDateSet(DatePicker view2, int year2, int monthOfYear2, int dayOfMonth2) {
                        //Change the text shown
                        tvEndDate.setText((monthOfYear2+1)+"/"+dayOfMonth2+"/"+year2);
                        //change the variables
                        endDay = dayOfMonth2;
                        endMonth = monthOfYear2;
                        endYear = year2;
                    }
                }, endYear, endMonth, endDay);
                datePickerDialog.show();
            }
        });

        //do this when the start time is clicked
        tvStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v3) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddEvent.this,new TimePickerDialog.OnTimeSetListener(){
                    @Override
                    public void onTimeSet(TimePicker view1, int hour1, int min1) {
                        //change the text shown
                        String showMin;
                        if(min1 < 10) {
                            showMin = "0" + Integer.toString(min1);
                        }
                        else{
                            showMin = Integer.toString(min1);
                        }
                        tvStartTime.setText(hour1+":"+showMin);
                        //change the variables
                        startHour = hour1;
                        startMin = min1;
                    }
                }, startHour, startMin, true);
                timePickerDialog.show();
            }
        });

        //do this when the end time is clicked
        tvEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v4) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddEvent.this,new TimePickerDialog.OnTimeSetListener(){
                    @Override
                    public void onTimeSet(TimePicker view2, int hour2, int min2) {
                        //change the text shown
                        String showMin;
                        if(min2 < 10) {
                            showMin = "0" + Integer.toString(min2);
                        }
                        else{
                            showMin = Integer.toString(min2);
                        }
                        tvEndTime.setText(hour2+":"+showMin);
                        //change the variables
                        endHour = hour2;
                        endMin = min2;
                    }
                }, endHour, endMin, true);
                timePickerDialog.show();
            }
        });

        tvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v4) {
                LoadPlacePicker(REQUEST_PLACE_PICKER);
            }
        });
    }

    //method to create place picker
    private void LoadPlacePicker(int PlacePickerRequest)
    {
        try {

            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(AddEvent.this);
            // Start the Intent by requesting a result, identified by a request code.
            startActivityForResult(intent, PlacePickerRequest);


        } catch (GooglePlayServicesRepairableException e) {
            GooglePlayServicesUtil
                    .getErrorDialog(e.getConnectionStatusCode(), AddEvent.this, 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(AddEvent.this, "Google Play Services is not available.",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    //get the results of an intent. If it's placepicker, we'll handle it,
    //otherwise we have the original activity happen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PLACE_PICKER) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                String toastMsg = String.format("Sender Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                lat = place.getLatLng().latitude;
                lng = place.getLatLng().longitude;
                tvAddress.setText(place.getAddress());
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data);
        }


    }
}
