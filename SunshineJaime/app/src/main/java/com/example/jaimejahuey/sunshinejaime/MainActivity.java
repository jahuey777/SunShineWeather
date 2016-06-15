package com.example.jaimejahuey.sunshinejaime;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

////http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&units=metric&cnt=7
//weather api key fe21a4b27caaecf3baa6d3b396b1a02f
//http://api.openweathermap.org/data/2.5/forecast/daily?q=BoilingSprings,US&units=metric&cnt=7&APPID=fe21a4b27caaecf3baa6d3b396b1a02f

/**
 * Created by jaimejahuey on 6/2/16.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.v("LifeCycle: " , "onCreate");
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_map){
            //Grabbing the zip code location
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String zip = sharedPreferences.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));

            Uri geoLocation = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q", zip).build();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(geoLocation);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
            else{
                Log.v("Couldn't launch map."," No map App installed");
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v("LifeCycle: " , "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("LifeCycle: " , "onResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v("LifeCycle: " , "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v("LifeCycle: " , "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("LifeCycle: " , "onDestroy");
    }
}