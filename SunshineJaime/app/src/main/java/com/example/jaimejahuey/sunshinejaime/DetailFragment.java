package com.example.jaimejahuey.sunshinejaime;

/**
 * Created by jaimejahuey on 6/29/16.
 */

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jaimejahuey.sunshinejaime.data.Utility;
import com.example.jaimejahuey.sunshinejaime.data.WeatherContract.WeatherEntry;
import com.example.jaimejahuey.sunshinejaime.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    ShareActionProvider mShareActionProvider;
    private static final String SHARE_HASHTAG = "#SunshineApp";
    private String sharingDetail;
    TextView forecastInfoTextView;
    private static final int MY_LOADER_ID = 1;
    String intentURI;

    private static final String[] DETAIL_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_HUMIDITY,
            WeatherEntry.COLUMN_PRESSURE,
            WeatherEntry.COLUMN_WIND_SPEED,
            WeatherEntry.COLUMN_DEGREES,
            WeatherEntry.COLUMN_WEATHER_ID,
            // This works because the WeatherProvider returns location data joined with
            // weather data, even though they're stored in two different tables.
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };
    //Tied with the details_columns. If details_columns changes then these mucst change as weell
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_HUMIDITY = 5;
    public static final int COL_WEATHER_PRESSURE = 6;
    public static final int COL_WEATHER_WIND_SPEED = 7;
    public static final int COL_WEATHER_DEGREES = 8;
    public static final int COL_WEATHER_CONDITION_ID =9;

    private ImageView mIconView;
    private TextView mFriendlyDateView;
    private TextView mDateView;
    private TextView mDescriptionView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;


    public DetailFragment() {
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
//            sharingDetail = intent.getStringExtra("WEATHERINFO");

//            ((TextView) rootView.findViewById(R.id.sharingDetail)).setText(sharingDetail);
        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        mFriendlyDateView = (TextView) rootView.findViewById(R.id.detail_day_textview);
        mDescriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        mHighTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        mLowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        mHumidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        mWindView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        mPressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);

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
        shareIntent.putExtra(Intent.EXTRA_TEXT, sharingDetail + SHARE_HASHTAG);
        return shareIntent;
    }

    public Loader onCreateLoader(int id, Bundle args) {

        //Buidling the Uri, or could have done intent.getdata()
        Uri builtUri = Uri.parse(intentURI).buildUpon().build();

        Log.v("Loader called " , " 2");

        CursorLoader cursorLoader = new CursorLoader(getContext(),
                builtUri,
                DETAIL_COLUMNS,
                null,
                null,
                null);

        return cursorLoader;
    }

    //Perform any UI updates here. Since the data is done loading here
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (!data.moveToFirst()) { return; }

        long date = data.getLong(COL_WEATHER_DATE);

        String weatherDescription =
                data.getString(COL_WEATHER_DESC);

        boolean isMetric = Utility.isMetric(getActivity());

        String high = Utility.formatTemperature(getContext(),
                data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);

        String low = Utility.formatTemperature(getContext(),
                data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);

        float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
        float windSpeed = data.getFloat(COL_WEATHER_WIND_SPEED);
        float windDir = data.getFloat(COL_WEATHER_DEGREES);
        float pressure = data.getFloat(COL_WEATHER_PRESSURE);

        int conditionId = data.getInt(COL_WEATHER_CONDITION_ID);

        mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(conditionId ));
        mDateView.setText(Utility.getFormattedMonthDay(getActivity(),date));
        mFriendlyDateView.setText(Utility.getFriendlyDayString(getActivity(),date));
        mDescriptionView.setText(weatherDescription);
        mHighTempView.setText(high);
        mLowTempView.setText(low);
        mHumidityView.setText(getActivity().getString(R.string.format_humidity,humidity));
        mWindView.setText(Utility.getFormattedWind(getActivity(), windSpeed, windDir));
        mPressureView.setText(getActivity().getString(R.string.format_pressure, pressure));

        sharingDetail = String.format("%s - %s - %s/%s", Utility.getFormattedMonthDay(getActivity(),date), weatherDescription, high, low);


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
