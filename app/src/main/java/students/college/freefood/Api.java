package students.college.freefood;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guita on 10/7/2017.
 */

public class Api {

    private ArrayList<FreeFoodEvent> m_freeFoodEvents = new ArrayList<FreeFoodEvent>();

    Api(ArrayList<FreeFoodEvent> freeFoodEvents)
    {
        m_freeFoodEvents = freeFoodEvents;
    }
    //}



    private class addEvent extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


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

        protected void onPostExecute(String ab) {

            //return (ab);
        }
    }
    //39.2546371&long=-76.7131987&distance=500
//    public void getEvents(LatLng myLatLng, int distance)
//    {
//        new getEvents().execute("http://ec2-54-226-112-134.compute-1.amazonaws.com/get.php?lat=" +
//                myLatLng.latitude + "&long=" + myLatLng.longitude + "&distance=" + distance);
//        //Log.d("freeFoodEvents: ", "> " + freeFoodEvents.toString());
//    }

    //http://ec2-54-226-112-134.compute-1.amazonaws.com/add.php?name=%22food%22&lat=39.3&long=-76.72&description=%22Get%20Hungry%22&start=%222017-10-07%2011:00:00%22&end=%222017-10-07%2022:00:00%22&category=%22%22&image=%22%22&address=%221000%20Hilltop%20Circle,%20Baltimore,%20MD%22
    public void addEvent(String name, LatLng latLng, String description, String start, String end, String category, String image, String address)
    {

    }
}