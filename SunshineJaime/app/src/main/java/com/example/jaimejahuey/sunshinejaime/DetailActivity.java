package com.example.jaimejahuey.sunshinejaime;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.jaimejahuey.sunshinejaime.data.Utility;
import com.example.jaimejahuey.sunshinejaime.data.WeatherContract;

import org.w3c.dom.Text;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

//        Intent intent = getIntent();
//        String info = intent.getStringExtra("WEATHERINFO");

//        Log.v("Extra" , " " + info);
//        if (intent != null) {
//            mForecastStr = intent.getDataString();
//        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);

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

        return super.onOptionsItemSelected(item);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

        ShareActionProvider mShareActionProvider;
        private static final String SHARE_HASHTAG = "#SunshineApp";
        private String forecastInfo;
        TextView forecastInfoTextView;
        private static final int MY_LOADER_ID = 1;
        String intentURI;

        //Columng for grabbing the info in loadfinished.
        //Copied from forecasfragment
        static final int COL_WEATHER_ID = 0;
        static final int COL_WEATHER_DATE = 1;
        static final int COL_WEATHER_DESC = 2;
        static final int COL_WEATHER_MAX_TEMP = 3;
        static final int COL_WEATHER_MIN_TEMP = 4;
        static final int COL_LOCATION_SETTING = 5;
        static final int COL_WEATHER_CONDITION_ID = 6;
        static final int COL_COORD_LAT = 7;
        static final int COL_COORD_LONG = 8;



        public PlaceholderFragment() {
            //Will call the onCreateOptionsMenu
            setHasOptionsMenu(true);
        }
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            getActivity().getSupportLoaderManager().initLoader(MY_LOADER_ID, null, this);
            Log.v("onActivityCreated  " , " after loader init");

            super.onActivityCreated(savedInstanceState);

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            Intent intent = getActivity().getIntent();
            if (intent != null) {
                intentURI = intent.getDataString();
                Log.v("URI ", intentURI);
            }
//            forecastInfo = intent.getStringExtra("WEATHERINFO");

//            ((TextView) rootView.findViewById(R.id.forecastInfo)).setText(forecastInfo);
            forecastInfoTextView = (TextView) rootView.findViewById(R.id.forecastInfo);

            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
            // Inflate the menu; this adds items to the action bar if it is present.
            menuInflater.inflate(R.menu.detailfragment, menu);

            //Setting up the sharing provider

            // Locate MenuItem with ShareActionProvider
            MenuItem item = menu.findItem(R.id.menu_item_share);

            // Get the provider and hold onto it to set/change the share intent.
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

            // Attach an intent to this ShareActionProvider.  You can update this at any time,
            // like when the user selects a new piece of com.example.jaimejahuey.sunshinejaime.data they might like to share.
            if (mShareActionProvider != null ) {
                mShareActionProvider.setShareIntent(createShareIntent());
            } else {
                Log.d("Error: ", "Share Action Provider is null?");
            }
        }

        private Intent createShareIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, forecastInfo + SHARE_HASHTAG);
            return shareIntent;
        }

        public Loader onCreateLoader(int id, Bundle args) {

            //Buidling the Uri, or could have done intent.getdata()
            Uri builtUri = Uri.parse(intentURI).buildUpon().build();

            Log.v("Loader called " , " 2");

            CursorLoader cursorLoader = new CursorLoader(getContext(),
                    builtUri,
                    ForecastFragment.FORECAST_COLUMNS,
                    null,
                    null,
                    null);

            return cursorLoader;
        }

        //Perform any UI updates here. Since the data is done loading here
        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

            if (!data.moveToFirst()) { return; }

            String dateString = Utility.formatDate(
                    data.getLong(COL_WEATHER_DATE));

            String weatherDescription =
                    data.getString(COL_WEATHER_DESC);

            boolean isMetric = Utility.isMetric(getActivity());

            String high = Utility.formatTemperature(
                    data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);

            String low = Utility.formatTemperature(
                    data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);

            forecastInfo = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

            forecastInfoTextView.setText(forecastInfo);

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareIntent());
            }
        }


        //Remove all references to cursoer data
        @Override
        public void onLoaderReset(Loader<Cursor> loader) {


        }

    }
}
