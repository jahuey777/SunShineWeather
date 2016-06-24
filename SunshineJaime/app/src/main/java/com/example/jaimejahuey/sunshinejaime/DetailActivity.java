package com.example.jaimejahuey.sunshinejaime;

import android.content.Intent;
import android.support.v4.app.Fragment;
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
    public static class PlaceholderFragment extends Fragment {

        private static final String SHARE_HASHTAG = "#SunshineApp";
        private String forecastInfo;


        public PlaceholderFragment() {
            //Will call the onCreateOptionsMenu
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            Intent intent = getActivity().getIntent();
            if (intent != null) {
                forecastInfo = intent.getDataString();
            }
//            forecastInfo = intent.getStringExtra("WEATHERINFO");

            ((TextView) rootView.findViewById(R.id.forecastInfo)).setText(forecastInfo);

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
            ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

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
    }
}
