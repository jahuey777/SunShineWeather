package com.example.jaimejahuey.sunshinejaime;

/**
 * Created by jaimejahuey on 6/3/16.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.jaimejahuey.sunshinejaime.data.Utility;
import com.example.jaimejahuey.sunshinejaime.data.WeatherContract;
import com.example.jaimejahuey.sunshinejaime.service.SunshineService;


/**
 * A placeholder fragment containing a simple view.
 */

////http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&units=metric&cnt=7
//weather api key fe21a4b27caaecf3baa6d3b396b1a02f
//http://api.openweathermap.org/data/2.5/forecast/daily?q=BoilingSprings,US&units=metric&cnt=7&APPID=fe21a4b27caaecf3baa6d3b396b1a02f

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private ForecastAdapter forecastAdapter;
    private static final int MY_LOADER_ID = 0;

    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;

    private static final String SELECTED_KEY = "selected_position";

    private boolean mUseTodayLayout;

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    public static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }


    public ForecastFragment() {
    }

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getActivity().getSupportLoaderManager().initLoader(MY_LOADER_ID, null, this);

        super.onActivityCreated(savedInstanceState);

    }
    // since we read the location when we create the loader, all we need to do is restart things
    void onLocationChanged( ) {
        updateWeather();
        getLoaderManager().restartLoader(MY_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        forecastAdapter = new ForecastAdapter(getContext(),null, 0);

//        ListView listView = (ListView) rootView.findViewById(R.id.list_view_forecast);
//        listView.setAdapter(forecastAdapter);

        mListView= (ListView) rootView.findViewById(R.id.list_view_forecast);
        mListView.setAdapter(forecastAdapter);


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
//                    Intent intent = new Intent(getActivity(), DetailActivity.class)
//                            .setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
//                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
//                            ));
//                    startActivity(intent);
                    ((Callback) getActivity())
                            .onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
                            ));
                }
                mPosition = position;
            }
        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        forecastAdapter.setmUseTodayLayout(mUseTodayLayout);
        /*Code with the regular cursor, this is before the cursorloader*/
//        String locationSetting = Utility.getPreferredLocation(getContext());
//        //Sort order by ascending date
//        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
//        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting,System.currentTimeMillis());
//
//        Cursor cursor = getActivity().getContentResolver().query(weatherForLocationUri, null,null,null,sortOrder);
//
//
//            // The CursorAdapter will take data from our cursor and populate the ListView
//            // However, we cannot use FLAG_AUTO_REQUERY since it is deprecated, so we will end
//            // up with an empty list the first time we run.
//        forecastAdapter = new ForecastAdapter(getContext(),cursor,0);
//
//        //Context, id of listitem layoug, textivw, and com.example.jaimejahuey.sunshinejaime.data
//        forecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, weatherList);
//        ListView listView = (ListView) rootView.findViewById(R.id.list_view_forecast);
//        listView.setAdapter(forecastAdapter);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//                String weatherInfo = forecastAdapter.getItem(position);
//                Toast.makeText(getActivity(),"item " + position + "clicked " + weatherInfo, Toast.LENGTH_LONG ).show();
//
//                Intent intent = new Intent(getActivity(), DetailActivity.class);
//                intent.putExtra("WEATHERINFO", weatherInfo);
//
//                startActivity(intent);
//            }
//        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        super.onCreateOptionsMenu(menu, menuInflater);

        menuInflater.inflate(R.menu.forceastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateWeather() {

        //Using service now
//        FetchWeatherTask weatherTask = new FetchWeatherTask(getContext());
//
////        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
////        String zip = preferences.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
//
//        String zip = Utility.getPreferredLocation(getActivity());
//        Log.v("Zip code " , zip);
//        weatherTask.execute(zip);

        Intent intent = new Intent(getActivity(), SunshineService.class);

        intent.putExtra(SunshineService.LOCATION_QUERY_EXTRA, Utility.getPreferredLocation(getActivity()));
        getActivity().startService(intent);

    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public void onResume() {
        super.onResume();
//        Log.v("Called on " , "resume");
//        getActivity().getSupportLoaderManager().initLoader(MY_LOADER_ID, null, this);
//        forecastAdapter.notifyDataSetChanged();
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, filter the query to return weather only for
        // dates after or including today.

        String locationSetting = Utility.getPreferredLocation(getContext());
        //Sort order by ascending date
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting,System.currentTimeMillis());

        CursorLoader cursorLoader = new CursorLoader(getContext(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);

        return cursorLoader;
    }

    //Perform any UI updates here. Since the data is done loading here
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        forecastAdapter.swapCursor(data);

        if(mPosition != ListView.INVALID_POSITION){
            //If we dont need to restart the loader, and there's a desired position to restore to, do so now
            mListView.smoothScrollToPosition(mPosition);
        }

    }


    //Remove all references to cursor data
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        forecastAdapter.swapCursor(null);
    }

    public void setmUseTodayLayout(boolean useTodayLayout){
        mUseTodayLayout = useTodayLayout;
        if(forecastAdapter!= null){
            forecastAdapter.setmUseTodayLayout(mUseTodayLayout);
        }
    }


//    public class FetchWeatherTask extends AsyncTask<String,Void,String[]>{
//
//        @Override
//        protected String[] doInBackground(String... params) {
//            HttpURLConnection urlConnection = null;
//
//            // Will contain the raw JSON response as a string.
//            String forecastJsonStr = null;
//
//            int numDays = 7;
//            final String DAYS_PARAM = "cnt";
//
//            try {
//                final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
//                //URI Builder instead, makes it easier to change the parameters and such for the url.
//                //params[0] is the string we are passing in, or the zip code
//
//                //Fixing the zip code
//                String zipCode = params[0] + ",us";
//
//                Uri build = Uri.parse(BASE_URL).buildUpon().appendQueryParameter("q", zipCode)
//                        .appendQueryParameter("mode", "json").appendQueryParameter("units", "metric")
//                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays)).appendQueryParameter("APPID","fe21a4b27caaecf3baa6d3b396b1a02f").build();
//
//                URL url = new URL(build.toString());
//
//                            Log.v("URL " , " " + build.toString());
//
//
//                urlConnection = (HttpURLConnection) url.openConnection();
//
//                urlConnection.setDoInput(true);
//                urlConnection.setRequestMethod("GET");
//                urlConnection.setRequestProperty("Content-Type", "application/json");
//
//                // handle issues
//                int statusCode = urlConnection.getResponseCode();
//                Log.v("Response Code " , " " + urlConnection.getResponseCode());
//
//                if (statusCode == HttpURLConnection.HTTP_OK) {
//                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
//
//                    String line;
//
//                    StringBuffer response = new StringBuffer();
//                    while((line=in.readLine())!= null){
//                        response.append(line);
//                    }
//                    in.close();
//
//                    return getWeatherDataFromJson(response.toString(),numDays);
//                }
//                else{
//                    Log.v("Post", " request not worked");
//                }
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        //Can use as well, but above is shorter and cleaner.
//        //@Override
//        //protected String[] doInBackground(String... params) {
//        /**    // These two need to be declared outside the try/catch
//            // so that they can be closed in the finally block.
//            HttpURLConnection urlConnection = null;
//            BufferedReader reader = null;
//
//            // Will contain the raw JSON response as a string.
//            String forecastJsonStr = null;
//
//            int numDays = 7;
//            final String DAYS_PARAM = "cnt";
//
//            try {
//                // Construct the URL for the OpenWeatherMap query
//                // Possible parameters are available at OWM's forecast API page, at
//                // http://openweathermap.org/API#forecast
//                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7&APPID=fe21a4b27caaecf3baa6d3b396b1a02f");
//
//
//                final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
//                //URI Builder instead, makes it easier to change the parameters and such for the url.
//                //params[0] is the string we are passing in, or the zip code
//
//                Uri build = Uri.parse(BASE_URL).buildUpon().appendQueryParameter("q", params[0])
//                        .appendQueryParameter("mode", "json").appendQueryParameter("units", "metric")
//                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays)).appendQueryParameter("APPID","fe21a4b27caaecf3baa6d3b396b1a02f").build();
//
//                //Testing to see if the Uri is build right
//                Log.v("URI build", build.toString());
//
//                //The commmented line above was when we hard coded the whole url. Now we can
//                //add our own values since we are appending the url together.
//                URL url = new URL(build.toString());
//
//                // Create the request to OpenWeatherMap, and open the connection
//                urlConnection = (HttpURLConnection) url.openConnection();
//                urlConnection.setRequestMethod("GET");
//                urlConnection.connect();
//
//                // Read the input stream into a String
//                InputStream inputStream = urlConnection.getInputStream();
//                StringBuffer buffer = new StringBuffer();
//                if (inputStream == null) {
//                    // Nothing to do.
//                    return null;
//                }
//                reader = new BufferedReader(new InputStreamReader(inputStream));
//
//                String line;
//
//                while ((line = reader.readLine()) != null) {
//                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
//                    // But it does make debugging a *lot* easier if you print out the completed
//                    // buffer for debugging.
//                    buffer.append(line + "\n");
//                }
//
//                if (buffer.length() == 0) {
//                    // Stream was empty.  No point in parsing.
//                    return null;
//                }
//                forecastJsonStr = buffer.toString();
//
//                Log.v("forecastSTring ", forecastJsonStr );
//
//            } catch (IOException e) {
//                Log.e("PlaceholderFragment", "Error ", e);
//                // If the code didn't successfully get the weather com.example.jaimejahuey.sunshinejaime.data, there's no point in attemping
//                // to parse it.
//                return null;
//            } finally{
//                if (urlConnection != null) {
//                    urlConnection.disconnect();
//                }
//                if (reader != null) {
//                    try {
//                        reader.close();
//                    } catch (final IOException e) {
//                        Log.e("PlaceholderFragment", "Error closing stream", e);
//                    }
//                }
//            }
//            try {
//                return getWeatherDataFromJson(forecastJsonStr, numDays);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            return null;
//        } */
//
//        /* The date/time conversion code is going to be moved outside the asynctask later,
// * so for convenience we're breaking it out into its own method now.
// */
//        private String getReadableDateString(long time){
//            // Because the API returns a unix timestamp (measured in seconds),
//            // it must be converted to milliseconds in order to be converted to valid date.
//            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
//            return shortenedDateFormat.format(time);
//        }
//
//        /**
//         * Prepare the weather high/lows for presentation.
//         */
//        private String formatHighLows(double high, double low) {
//            //Check the preferences to see which we need to do
//            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
//            String uniType = sharedPreferences.getString(getString(R.string.pref_unit_key),getString(R.string.pref_units_metric));
//
//            Log.d("Unit type:", uniType);
//
//
//            if(uniType.equals(getString(R.string.pref_units_imperial))){
//                high = (high*1.8) + 32;
//                low = (low*1.8) + 32;
//            }
//            else if(!uniType.equals(getString(R.string.pref_units_metric))){
//                Log.d("Unite type not found :", uniType);
//            }
//
//            // For presentation, assume the user doesn't care about tenths of a degree.
//            long roundedHigh = Math.round(high);
//            long roundedLow = Math.round(low);
//
//            String highLowStr = roundedHigh + "/" + roundedLow;
//            return highLowStr;
//        }
//
//        /**
//         * Take the String representing the complete forecast in JSON Format and
//         * pull out the com.example.jaimejahuey.sunshinejaime.data we need to construct the Strings needed for the wireframes.
//         *
//         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
//         * into an Object hierarchy for us.
//         */
//        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
//                throws JSONException {
//
//            // These are the names of the JSON objects that need to be extracted.
//            final String OWM_LIST = "list";
//            final String OWM_WEATHER = "weather";
//            final String OWM_TEMPERATURE = "temp";
//            final String OWM_MAX = "max";
//            final String OWM_MIN = "min";
//            final String OWM_DESCRIPTION = "main";
//
//            JSONObject forecastJson = new JSONObject(forecastJsonStr);
//            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
//
//            //THIS IS DEPRECATED, USING GREGORIAN INSTEAD
//            /* OWM returns daily forecasts based upon the local time of the city that is being
//            // asked for, which means that we need to know the GMT offset to translate this com.example.jaimejahuey.sunshinejaime.data
//            // properly.
//
//            // Since this com.example.jaimejahuey.sunshinejaime.data is also sent in-order and the first day is always the
//            // current day, we're going to take advantage of that to get a nice
//            // normalized UTC date for all of our weather.
//
////            android.text.format.Time dayTime = new Time();
////            dayTime.setToNow();
////
////            // we start at the day returned by local time. Otherwise this is a mess.
////            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
////
////            // now we work exclusively in UTC
////            dayTime = new Time();*/
//
//            String[] resultStrs = new String[numDays];
//            for(int i = 0; i < weatherArray.length(); i++) {
//                // For now, using the format "Day, description, hi/low"
//                String day;
//                String description;
//                String highAndLow;
//
//                // Get the JSON object representing the day
//                JSONObject dayForecast = weatherArray.getJSONObject(i);
//
//                //Deprecated
//                /* The date/time is returned as a long.  We need to convert that
//                // into something human-readable, since most people won't read "1400356800" as
//                // "this saturday".
////                long dateTime;
////                // Cheating to convert this to UTC time, which is what we want anyhow
////                dateTime = dayTime.setJulianDay(julianStartDay+i);
////                day = getReadableDateString(dateTime);*/
//
//                //create a Gregorian Calendar, which is in current date
//                GregorianCalendar gc = new GregorianCalendar();
//                //add i dates to current date of calendar
//                gc.add(GregorianCalendar.DATE, i);
//                //get that date, format it, and "save" it on variable day
//                Date time = gc.getTime();
//                SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
//                day = shortenedDateFormat.format(time);
//
//
//                // description is in a child array called "weather", which is 1 element long.
//                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
//                description = weatherObject.getString(OWM_DESCRIPTION);
//
//                // Temperatures are in a child object called "temp".  Try not to name variables
//                // "temp" when working with temperature.  It confuses everybody.
//                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
//                double high = temperatureObject.getDouble(OWM_MAX);
//                double low = temperatureObject.getDouble(OWM_MIN);
//
//                highAndLow = formatHighLows(high, low);
//                resultStrs[i] = day + " - " + description + " - " + highAndLow;
//            }
//
//            for (String s : resultStrs) {
//                Log.v("ForecastFrag", "Forecast entry: " + s);
//            }
//            return resultStrs;
//
//        }
//
//        @Override
//        protected void onPostExecute(String[] strings) {
//            if(strings!=null)
//            {
//                forecastAdapter.clear();
//                //Log.v("Strings", "are null");
//
//                forecastAdapter.addAll(strings);
//            }
//        }
//    }
}
