package com.example.cmc7.hktoilet;

import android.app.AlertDialog;
import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener {
    private static final String DEBUG_TAG = "HttpExample";
    private String urlText;
    private TextView textView;
    private ListView listView;
    private JSONObject jsonRootObject;
    private ArrayList<Toilet> toilets;
    private ToiletArrayAdapter toiletArrayAdapter;

    //Location Services Google API:
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    protected static final String TAG = "HKToilet.App";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Our code starts from here:
        textView = (TextView)findViewById(R.id.textView);
        //urlText = getResources().getString(R.string.json_url);

        //ListView Code
        toilets = new ArrayList<>();
        toilets.add(new Toilet(getString(R.string.please_refresh), "------", "0"));
        listView = (ListView)findViewById(R.id.listView);
        toiletArrayAdapter = new ToiletArrayAdapter(this, toilets);
        listView.setAdapter(toiletArrayAdapter);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        getMenuInflater().inflate(R.menu.action_refresh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_refresh){
            getLocation();
            refreshHandler_download(null);
        }else if(id == R.id.action_about){
            aboutDialog();
        }

        //noinspection SimplifiableIfStatement
        /*
        if (id == R.id.action_settings) {
            return true;
        }
        */

        return super.onOptionsItemSelected(item);
    }


    // When user clicks button, calls AsyncTask.
    // Before attempting to fetch the URL, makes sure that there is a network connection.
    public void refreshHandler_download(View view) {
        // Gets the URL from the UI's text field.
        String stringUrl = urlText;
        // Gets the connectivity and network info objects.
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        // Download URL only if connectivity is okay.
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask().execute(stringUrl);
        } else {
            textView.setText("No network connection available.");
        }

    }


    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //textView.setText(result);

            try{
                jsonRootObject = new JSONObject(result);
                JSONArray jsonArray = jsonRootObject.optJSONArray("results");
                toilets.clear();

                //Parse JSON and pack them as ArrayList of Toilet
                for(int i=0; i<jsonArray.length(); i++){
                    //Add one toilet into the ArrayList
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Toilet toilet = new Toilet();
                    toilet.name = jsonObject.optString("name");
                    toilet.address = jsonObject.optString("address");
                    toilet.distance = jsonObject.optString("distance");
                    toilets.add(toilet);

                }

                toiletArrayAdapter.notifyDataSetChanged();

            }catch(JSONException e){
                e.printStackTrace();
            }

        }
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a StringBuffer
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder builder = new StringBuilder();
            String line;
            while((line = reader.readLine())!=null){
                builder.append(line);
            }

            //Convert and return the StringBuffer to String
            return builder.toString();

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    protected void onStart() {
        if(mGoogleApiClient!=null) {
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    protected void onStop() {
        if(mGoogleApiClient!=null){
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        getLocation();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }


    private void getLocation(){
        try{
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }catch(SecurityException e){
            e.printStackTrace();
        }

        if (mLastLocation != null) {
            // fetch and convert the last lat and long
            String latitude = String.valueOf(mLastLocation.getLatitude());
            String longitude = String.valueOf(mLastLocation.getLongitude());
            // show to user the current lat and long
            textView.setText(getText(R.string.my_location));
            textView.append(latitude);
            textView.append(", "+longitude);
            // change the URL with correct lat and long
            urlText = getResources().getString(R.string.json_url);
            urlText = urlText.concat("lat="+latitude);
            urlText = urlText.concat("&lng="+longitude);

        } else {
            Toast.makeText(this, R.string.no_location_detected, Toast.LENGTH_LONG).show();
        }

    }

    private void aboutDialog(){
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.about_message)
                .setTitle(R.string.about_title);
        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();

    }
}
