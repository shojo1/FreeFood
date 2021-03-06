package students.college.freefood;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener,
//        GoogleMap.OnInfoWindowCloseListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener { //implements OnMapReadyCallback {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Marker mCurrLocationMarker;
    private LocationRequest mLocationRequest;
    private ArrayList<FreeFoodEvent> ffeArray = new ArrayList<FreeFoodEvent>();
    private int mdistance = 20;
    int cbNone = 0;
    int cbRR = 0;
    int cbCE = 0;
    int cbHH = 0;
    int cbGL = 0;
    int cbP = 0;
    private LatLng mlatLng = new LatLng(35.265956, -36.862455);
    private int newtworkIssues = 0; //0 if no network issues, 1 if network issues
    private User m_user;
    private LinearLayout m_preLayoutSpace;
    private AdView mAdView;


    /**
     * When this view is loaded (when a user clicks on our app for the first time, and it wasnt already open)
     * @param savedInstanceState - if there is some cache for the app this is it.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        readUser();

        Intent intent = getIntent();
        //try to get a toast message from whatever sent you here
        String message = intent.getStringExtra("Toast");
        //if there was a message, show it to the user
        if(message != null && !message.equals(""))
            Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
        mdistance = m_user.getRadius();
        cbNone = m_user.getFilter(0);
        cbRR = m_user.getFilter(1);
        cbCE = m_user.getFilter(2);
        cbHH = m_user.getFilter(3);
        cbGL = m_user.getFilter(4);
        cbP = m_user.getFilter(5);

        System.out.println("THE RADIUS IS: "+mdistance);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();
        }

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        keyPrivateVariablesToLayout();
        MobileAds.initialize(this, "ca-app-pub-6153564065949295~3246609206");
        mAdView = (AdView) findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder()
               // .addTestDevice("57BAA04D839C980E2F9720252A22323C")
                .build();

        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);

    }

    /**
     * When the app is ready to display the map (immediately after onCreate)
     * @param googleMap some variable for the map.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mlatLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
        mMap.setOnInfoWindowClickListener(this);
//        mMap.setOnInfoWindowCloseListener(this);
        mMap.setOnMarkerClickListener(this);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng latLng)
            {
                //Code for marker deselect goes here
                if(m_preLayoutSpace!=null) {
                    m_preLayoutSpace.setBackgroundColor(Color.WHITE);
                }
            }
        });


        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        final LocationManager manager = (LocationManager) getSystemService( getApplicationContext().LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }

    }
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * This is part of letting google know we are allowed to use their map
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    /**
     * What to do when the app is connected to the internet. (connects them to gooogle maps)
     * @param bundle - Im not sure what this is.
     */
    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(20000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    /**
     * What to do if the user suspends the app (if we want to do anything?)
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * What to do when the user's location is updated
     * @param location - the user's current location
     */
    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        mlatLng = new LatLng(location.getLatitude(), location.getLongitude());

        String filter = "";
        if (cbNone + cbRR + cbCE +cbHH + cbGL  + cbP > 0) {
            if (cbNone == 1){
                filter = filter + "&cbNone=1";
            }
            if (cbRR == 1){
                filter = filter + "&cbRR=1";
            }
            if (cbCE == 1){
                filter = filter + "&cbCE=1";
            }
            if (cbHH == 1){
                filter = filter + "&cbHH=1";
            }
            if (cbGL == 1){
                filter = filter + "&cbGL=1";
            }
            if (cbP == 1){
                filter = filter + "&cbP=1";
            }

        }
        new getEvents().execute(getApplicationContext().getString(R.string.ip) + "get.php?lat=" +
                mlatLng.latitude + "&long=" + mlatLng.longitude + "&distance=" + mdistance + filter);
        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mlatLng));

        if(mdistance > 15)
                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        else if(mdistance > 10)
                mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
        else if(mdistance > 5)
                mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
        else
                mMap.animateCamera(CameraUpdateFactory.zoomTo(14));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }

    /**
     * What to do if the user has no internect connection?
     * @param connectionResult - the result of checking their connection status
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
        Asks the user if we can use their current location.
     */
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 123;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Gets the results from asking the user for permission to use their current location
     * @param requestCode - what type of request was asked (in this case it will be a request for location permission)
     * @param permissions - what permissions were given
     * @param grantResults - the results from the user. 1 is allow, -1 is don't allow.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    /**
     * handle marker click event
     * This function should show the user some information about the event going down at
     * the clicked location.
     * @param marker - a marker icon on the google map.
     * @return - just returns false at the end.
     */
    @Override
    public boolean onMarkerClick(Marker marker)
    {
        System.out.println("Looking for: "+marker.toString());
        //Code for on marker click goes here
        // Highlight event in details view
        if(m_preLayoutSpace!=null) {
            m_preLayoutSpace.setBackgroundColor(Color.WHITE);
        }

        LinearLayout layoutSpace = (LinearLayout)findViewById(R.id.mainEventListLayout);
        LinearLayout a = layoutSpace.findViewWithTag(marker);

        m_preLayoutSpace = layoutSpace.findViewWithTag(marker);
        layoutSpace.removeView(a);
        a.setBackgroundColor(Color.LTGRAY);
        layoutSpace.addView(a, 0);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        LinearLayout layoutSpace = (LinearLayout)findViewById(R.id.mainEventListLayout);
        LinearLayout a = layoutSpace.findViewWithTag(marker);
//        layoutSpace.removeView(a);
//        a.setBackgroundColor(Color.LTGRAY);
//        layoutSpace.addView(a, 0);
//        a.findViewsWithText("Details")[0].click();
        Button b = a.findViewWithTag("Details");
        b.performClick();
    }



    /**
     * This is a quick function for initializing the variables of this class to the
     * IDs of different views on the layout.
     */
    public void keyPrivateVariablesToLayout()
    {
    }

    /**
     * when the up arrow is clicked, show the event list.
     * @param view - up arrow
     */
    public void onEventsClick(View view)
    {
        Intent i = new Intent(getApplicationContext(),EventList.class);
        i.putExtra("event",ffeArray);
        startActivity(i);
    }

    /**
     * when the hamburger is clicked, show some user options
     * @param view - hamburger button
     */
    public void goToOptions(View view)
    {
        Intent i = new Intent(getApplicationContext(),NavigationMenu.class);
        startActivity(i);
    }

    /**
     * pulls the events from the database, fills teh ffeArray, and creates markers for each event.
     */
    public void generateEventList()
    {
        //this is the encompassing layout for all of the events to go in
        LinearLayout layoutSpace = (LinearLayout)findViewById(R.id.mainEventListLayout);
        layoutSpace.setBackgroundColor(Color.WHITE);
        mMap.clear();
        layoutSpace.removeAllViewsInLayout();

        for(int i = 0; i < ffeArray.size(); i++)
        {
            //create a dummy layout to hold event info
            LinearLayout a = new LinearLayout(this);
            a.setOrientation(LinearLayout.HORIZONTAL);
            a.setBackgroundColor(Color.WHITE);
            a.setMinimumHeight(200);
            a.setGravity(Gravity.CENTER);
            mlatLng = new LatLng(Double.parseDouble(ffeArray.get(i).getLat()), Double.parseDouble(ffeArray.get(i).getLon()));

            //create a marker to go with this event
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(mlatLng);
            markerOptions.title(ffeArray.get(i).getName());
            if(m_user.getLikedEvent(ffeArray.get(i).getHash()))
            {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            }
            mCurrLocationMarker = mMap.addMarker(markerOptions);

            //create an image for each event
            ImageView imv = new ImageView(this);
            imv.setMinimumWidth(50);
            imv.setMinimumHeight(75);
            imv.setMaxHeight(75);
            imv.setImageResource(ffeArray.get(i).getCategoryInt());
            imv.setId(i);
            imv.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
//                    mCurrLocationMarker.showInfoWindow();
                    LatLng latLng = new LatLng(Double.parseDouble(ffeArray.get(v.getId()).getLat()), Double.parseDouble(ffeArray.get(v.getId()).getLon()));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng), 250, null);
                }
            });
            a.addView(imv);

            // the name of the event (as a text view)
            TextView tv = new TextView(this);
            tv.setText(ffeArray.get(i).getName());
            //if there are likes, there is less room for the name
            if(ffeArray.get(i).getLikes() > 0)
            {
                tv.setWidth(300);
                //shorten the string if it doesnt fit
                if(tv.getText().length() >= 15)
                {
                    tv.setText(tv.getText().toString().substring(0,12)+"...");
                }
            }
            else
            {
                tv.setWidth(600);
                if(tv.getText().length() >= 30)
                {
                    tv.setText(tv.getText().toString().substring(0,25)+"...");
                }
            }
            tv.setHeight(75);
            tv.setId(i);
            tv.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
//                    mCurrLocationMarker.showInfoWindow();
                    LatLng latLng = new LatLng(Double.parseDouble(ffeArray.get(v.getId()).getLat()), Double.parseDouble(ffeArray.get(v.getId()).getLon()));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng), 250, null);
                }
            });
            a.addView(tv);


            //if there are likes, show them
            if(ffeArray.get(i).getLikes() > 0)
            {
                TextView t = new TextView(this);
                t.setText("Likes: "+ffeArray.get(i).getLikes());
                t.setHeight(75);
                t.setWidth(300);
                a.addView(t);
            }

            //add a button here to get more details
            final Button eventButton = new Button(this);
            eventButton.setId(i);
            eventButton.setTag("Details");
            eventButton.setText("Details");

            //when someone clicks this details button, they go to EventDetails
            eventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(getApplicationContext(), EventDetails.class);
                    //System.out.println(ffeArray.get(eventButton.getId()).getName());
                    FreeFoodEvent ffe = ffeArray.get(eventButton.getId());
                    i.putExtra("event", ffe);
                    startActivityForResult(i,0);
                }
            });
         a.addView(eventButton);

            //add this layout to the full layout
            a.setTag(mCurrLocationMarker);
            layoutSpace.addView(a);
        }
    }


    /**
     * user clicks the add button, goes to addEvent
     * @param view - the plus button
     */
    public void AddClick(View view)
    {
        //System.out.println("I totally made a new event!");
        Intent i = new Intent(getApplicationContext(),AddEvent.class);
        startActivity(i);
    }

    /**
     * when the user comes back here, usually from the NavigationMenu
     * @param requestCode - the code used to send the user out
     * @param resultCode - the code we recieve when the user comes back
     * @param data - data from the screen they were sent to
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //regenerate the list.
        readUser();
        onLocationChanged(mLastLocation);
        //generateEventList();
    }


    /**
     * Reads infromation about the User object from a .data file.
     */
    public void readUser()
    {
        try
        {
            FileInputStream fis = new FileInputStream (new File(getFilesDir()+"userData.data"));
            ObjectInputStream ois = new ObjectInputStream(fis);
            m_user = (User) ois.readObject();
            fis.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            m_user = new User();
            writeUser();
        }
    }

    /**
     * Saves information about the User object to a .data file
     */
    public void writeUser()
    {
        // Write to disk with FileOutputStream
        try
        {
            FileOutputStream fout = new FileOutputStream(getFilesDir()+"userData.data");
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(m_user);
            fout.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


/**
     * a private class to get the events around you. OnPostExecute generated the ffe array and calls
     * the generateEventList method
     */
    private class getEvents extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... params) {

        HttpURLConnection connection = null;
        HttpURLConnection connection2 = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000); //set timeout to 3 seconds
            connection.connect();


            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
                //Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

            }

            return buffer.toString();


        } catch (java.net.SocketTimeoutException | UnknownHostException e) {
            connection.disconnect();
            try {
                URL url = new URL(params[0]);
                connection2 = (HttpURLConnection) url.openConnection();
                connection2.setConnectTimeout(3000); //set timeout to 3 seconds
                connection2.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                    //Log.d("Response2: ", "> " + line);   //here youll get whole response
                }
            } catch (java.net.SocketTimeoutException | UnknownHostException e2) {
                e2.printStackTrace();
                newtworkIssues = 1;
            } catch (MalformedURLException e2) {
                e2.printStackTrace();
            } catch (IOException e2) {
                e.printStackTrace();
            } finally {
                if (connection2 != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e2) {
                    e.printStackTrace();
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    protected void onPostExecute(String jsonStr) {
        if (newtworkIssues == 1) {
            //System.out.println("The Internet Failed!");
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setMessage(R.string.failed)
                    .setPositiveButton(R.string.tryAgain, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // FIRE ZE MISSILES!
                        }
                    });
            // Create the AlertDialog object and return it
            builder.create();
            builder.show();

        } else {
            //System.out.println("NETWORK SUCCESS");
            try {
                JSONArray jsonarray = new JSONArray(jsonStr);
                ffeArray.clear();
                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject jsonobject = jsonarray.getJSONObject(i);
                    ffeArray.add(new FreeFoodEvent(jsonobject.getString("EventName"), jsonobject.getString("Description"), jsonobject.getString("Lat"), jsonobject.getString("Lon"), jsonobject.getString("StartTime"), jsonobject.getString("EndTime"), jsonobject.getString("Address"), jsonobject.getString("Category"), jsonobject.getInt("Likes"), jsonobject.getString("Hash")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //Log.d("freeFoodEvents: ", "> " + ffeArray.toString());
            generateEventList();
        }
    }
    }
}
