package com.example.maplocation;

import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;
 

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
 
public class MainActivity extends FragmentActivity implements LoaderCallbacks<Cursor> {
 
	private static final LatLng LOCATION_SATELLITE = null;
	private final LatLng LOCATION_UNIV = new LatLng(33.783768, -118.114336);
	private final LatLng LOCATION_ECS = new LatLng(33.782777, -118.111868);
	private GoogleMap gMap;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        gMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        gMap.addMarker(new MarkerOptions().position(LOCATION_ECS).title("Find me here!"));
 
        
        gMap.setOnMapClickListener(new OnMapClickListener() {
 
            @Override
            public void onMapClick(LatLng point) {
 
                // Drawing marker on the map
                drawMarker(point);
 
                // Creating an instance of ContentValues
                ContentValues contentValues = new ContentValues();
 
                // Setting latitude in ContentValues
                contentValues.put(LocationsDB.FIELD_LAT, point.latitude );
 
                // Setting longitude in ContentValues
                contentValues.put(LocationsDB.FIELD_LNG, point.longitude);
 
                // Setting zoom in ContentValues
                contentValues.put(LocationsDB.FIELD_ZOOM, gMap.getCameraPosition().zoom);
 
                // Creating an instance of LocationInsertTask
                LocationInsertTask insertTask = new LocationInsertTask();
 
                // Storing the latitude, longitude and zoom level to SQLite database
                insertTask.execute(contentValues);
 
                Toast.makeText(getBaseContext(), "A marker added.", Toast.LENGTH_SHORT).show();
            }
        });
 
        gMap.setOnMapLongClickListener(new OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
 
                // Removing all markers from the Google Map
            	gMap.clear();
 
                // Creating an instance of LocationDeleteTask
                LocationDeleteTask deleteTask = new LocationDeleteTask();
 
                // Deleting all the rows from SQLite database table
                deleteTask.execute();
 
                Toast.makeText(getBaseContext(), "All markers removed.", Toast.LENGTH_LONG).show();
            }
        });
    }
 
    private void drawMarker(LatLng point){
        // Creating an instance of MarkerOptions
        MarkerOptions markerOptions = new MarkerOptions();
 
        // Setting latitude and longitude for the marker
        markerOptions.position(point);
 
        // Adding marker on the Google Map
        gMap.addMarker(markerOptions);
    }
 
    private class LocationInsertTask extends AsyncTask<ContentValues, Void, Void>{
        @Override
        protected Void doInBackground(ContentValues... contentValues) {
 
            // Setting up values to insert the clicked location into SQLite database 
            getContentResolver().insert(LocationsContentProvider.CONTENT_URI, contentValues[0]);
            return null;
        }
    }
 
    private class LocationDeleteTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
 
            // Deleting all the locations stored in SQLite database 
            getContentResolver().delete(LocationsContentProvider.CONTENT_URI, null, null);
            return null;
        }
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
 
    @Override
    public Loader<Cursor> onCreateLoader(int arg0,
        Bundle arg1) {
 
        // Uri to the content provider LocationsContentProvider
        Uri uri = LocationsContentProvider.CONTENT_URI;
 
        // Fetches all the rows from locations table
        return new CursorLoader(this, uri, null, null, null, null);
    }
 
    @Override
    public void onLoadFinished(Loader<Cursor> arg0,
        Cursor arg1) {
        int locationCount = 0;
        double lat=0;
        double lng=0;
        float zoom=0;
 
        // Number of locations available in the SQLite database table
        locationCount = arg1.getCount();
 
        // Move the current record pointer to the first row of the table
        arg1.moveToFirst();
 
        for(int i=0;i<locationCount;i++){
 
            // Get the latitude
            lat = arg1.getDouble(arg1.getColumnIndex(LocationsDB.FIELD_LAT));
 
            // Get the longitude
            lng = arg1.getDouble(arg1.getColumnIndex(LocationsDB.FIELD_LNG));
 
            // Get the zoom level
            zoom = arg1.getFloat(arg1.getColumnIndex(LocationsDB.FIELD_ZOOM));
 
            // Creating an instance of LatLng to plot the location in Google Maps
            LatLng location = new LatLng(lat, lng);
 
            // Drawing the marker in the Google Maps
            drawMarker(location);
 
            // Traverse the pointer to the next row
            arg1.moveToNext();
        }
 
        if(locationCount>0){
            // Moving CameraPosition to last clicked position
        	gMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat,lng)));
 
            // Setting the zoom level in the map on last position  is clicked
        	gMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
        }
    }
 
    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        // TODO Auto-generated method stub
    }
    
    public void onClick_CSULB(View v)
    {
    	gMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
    	CameraUpdate update = CameraUpdateFactory.newLatLngZoom(LOCATION_UNIV, 16);
    	gMap.animateCamera(update);
    }
    
    public void onClick_ECS(View v)
    {
    	gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    	CameraUpdate update = CameraUpdateFactory.newLatLngZoom(LOCATION_ECS, 18);
    	gMap.animateCamera(update);
    }

    public void onClick_City(View v)
    {
    	gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
    	CameraUpdate update = CameraUpdateFactory.newLatLngZoom(LOCATION_UNIV, 13);
    	gMap.animateCamera(update);
    }
}




/*
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends Activity {

	private static final LatLng LOCATION_SATELLITE = null;
	private final LatLng LOCATION_UNIV = new LatLng(33.783768, -118.114336);
	private final LatLng LOCATION_ECS = new LatLng(33.782777, -118.111868);
	private GoogleMap map;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.addMarker(new MarkerOptions().position(LOCATION_ECS).title("Find me here!"));
    }

    public void onClick_CSULB(View v)
    {
    	map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
    	CameraUpdate update = CameraUpdateFactory.newLatLngZoom(LOCATION_UNIV, 14);
    	map.animateCamera(update);
    }
    
    public void onClick_ECS(View v)
    {
    	map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    	CameraUpdate update = CameraUpdateFactory.newLatLngZoom(LOCATION_ECS, 16);
    	map.animateCamera(update);
    }

    public void onClick_City(View v)
    {
    	map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
    	CameraUpdate update = CameraUpdateFactory.newLatLngZoom(LOCATION_UNIV, 9);
    	map.animateCamera(update);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
*/
