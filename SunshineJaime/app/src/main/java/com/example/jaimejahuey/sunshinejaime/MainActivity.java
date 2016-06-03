package com.example.jaimejahuey.sunshinejaime;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

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
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            List<String> weatherList = new ArrayList<>();
            weatherList.add("Today - Sunny - 88/63");
            weatherList.add("Tomorrow - Sunny - 90/69");
            weatherList.add("Sunday - Sunny - 93/59");
            weatherList.add("Monday - Sunny - 82/58");
            weatherList.add("Tuesday - Sunny - 84/63");

            //Context, id of listitem layoug, textivw, and data
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, weatherList);
            ListView listView = (ListView) rootView.findViewById(R.id.list_view_forecast);

            listView.setAdapter(adapter);

            return rootView;
        }
    }
}