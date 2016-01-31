package com.hackbridge.viral;

import java.io.IOException;
import java.net.Socket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.appnavigation.R;

public class MainActivity extends Activity
{
    private TextView           latitude;
    private TextView           longitude;
    private TextView           choice;
    private CheckBox           fineAcc;
    private Button             choose;
    private TextView           provText;
    private LocationManager    locationManager;
    private String             provider;
    private MyLocationListener mylistener;
    private Criteria           criteria;

    private boolean            round_on;
    private AwarenessState     awareness;
    private PhysicalState      physical;
    private long               identity;
    private String             server = "188.166.154.60";
    private int                port   = 25000;
    private MessageSender      ms;
    private MessageReceiver    mr;
    private Socket             sock;

    public void setRoundOn(boolean isOn)
    {
        round_on = isOn;
    }

    public PhysicalState loadPhysicalState()
    {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        // identity reading and init switch
        int phys = sharedPref.getInt("physical", -1);
        switch (phys)
        {
            case -1:
                // writes start state and initialises physical state
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("physical", 0);
                editor.commit();
                physical = PhysicalState.SUSCEPTIBLE;
                break;
            case 0:
                physical = PhysicalState.SUSCEPTIBLE;
                break;
            case 1:
                physical = PhysicalState.VACCINATED;
                break;
            case 2:
                physical = PhysicalState.INFECTED;
                break;
        }
        Log.d("LAG-LOGIC", "Loaded Physiscal State is: " + physical);
        return physical;
    }

    public void setPhysicalState(PhysicalState physicalState)
    {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        switch (physicalState)
        {
            case SUSCEPTIBLE:
                editor.putInt("physical", 0);
                break;
            case VACCINATED:
                editor.putInt("physical", 1);
                break;
            case INFECTED:
                editor.putInt("physical", 2);
                break;
        }
        editor.commit();
        Log.d("LAG-LOGIC", "Physiscal State is: " + physical);
        physical = physicalState;
    }

    private AwarenessState loadAwareness()
    {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        // identity reading and init switch
        int avr = sharedPref.getInt("aware", -1);
        switch (avr)
        {
            case -1:
                // writes start state and initialises awareness
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("aware", 0);
                editor.commit();
                awareness = AwarenessState.UNAWARE;
                break;
            case 0:
                awareness = AwarenessState.UNAWARE;
                break;
            case 1:
                awareness = AwarenessState.AWARE;
                break;
        }
        Log.d("LAG-LOGIC", "Loaded awareness is: " + awareness);
        return awareness;
    }

    public void setAwareness(AwarenessState aware)
    {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        switch (aware)
        {
            case UNAWARE:
                editor.putInt("aware", 0);
                break;
            case AWARE:
                editor.putInt("aware", 1);
                break;
        }
        editor.commit();
        awareness = aware;
        Log.d("LAG-LOGIC", "Awareness is: " + awareness);
        // TODO
    }

    private long loadIdentity()
    {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        identity = sharedPref.getLong("identity", -1);
        Log.d("LAG-LOGIC", "Loaded identity is: " + identity);
        return identity;
    }

    public void setIdentity(long ident)
    {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("identity", ident);
        editor.commit();
        identity = ident;
        Log.d("LAG-LOGIC", "Identity is: " + identity);
        // TODO
        //
    }

    /** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        latitude = (TextView) findViewById(R.id.lat);
        longitude = (TextView) findViewById(R.id.lon);
        provText = (TextView) findViewById(R.id.prov);
        choice = (TextView) findViewById(R.id.choice);
        fineAcc = (CheckBox) findViewById(R.id.fineAccuracy);
        choose = (Button) findViewById(R.id.chooseRadio);
        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the location provider
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_LOW); // default

        // user defines the criteria
        choose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                if (fineAcc.isChecked())
                {
                    criteria.setAccuracy(Criteria.ACCURACY_FINE);
                    choice.setText("fine accuracy selected");
                }
                else
                {
                    criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                    choice.setText("coarse accuracy selected");
                }
            }
        });

        criteria.setCostAllowed(false);
        // get the best provider depending on the criteria
        provider = locationManager.getBestProvider(criteria, false);

        // temporary solution!
        provider = LocationManager.NETWORK_PROVIDER;

        // the last known location of this provider
        Location location = locationManager.getLastKnownLocation(provider);

        mylistener = new MyLocationListener();

        if (location != null)
        {
            mylistener.onLocationChanged(location);
        }
        else
        {
            // leads to the settings because there is no last known location
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
        // location updates: at least 1 meter and 200millsecs change
        locationManager.requestLocationUpdates(provider, 100, 0.5f, mylistener);

        round_on = false;
        awareness = loadAwareness();
        physical = loadPhysicalState();
        identity = loadIdentity();

        final MainActivity ma = this;
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    Log.d("LAG", "Precket created");
                    sock = new Socket(server, port);
                    Log.d("LAG", "Socket created");
                    mr = new MessageReceiver(ma, sock);
                    mr.setDaemon(true);
                    mr.start();
                    ms = new MessageSender(sock);

                    if (identity == -1) ms.sendMessage(new HelloNewMessage());
                    else ms.sendMessage(new HelloMessage(identity));
                }
                catch (IOException e)
                {
                    Log.d("LAG", "Cannot connect to " + server + ", port " + port);
                    // TODO
                    // handle this better!
                    return;
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    private class MyLocationListener implements LocationListener
    {

        @Override
        public void onLocationChanged(Location location)
        {
            // Initialize the location fields
            latitude.setText("Latitude: " + String.valueOf(location.getLatitude()));
            longitude.setText("Longitude: " + String.valueOf(location.getLongitude()));
            provText.setText(provider + " provider has been selected.");

            Toast.makeText(MainActivity.this, "Location changed!",
                    Toast.LENGTH_SHORT).show();

            if (round_on && ms != null) ms.sendMessage(new PositionMessage(identity,
                    new LocationWrapper(location.getLongitude(),
                            location.getLatitude(),
                            location.getAltitude())));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Toast.makeText(MainActivity.this,
                    provider + "'s status changed to " + status + "!",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Toast.makeText(MainActivity.this, "Provider " + provider + " enabled!",
                    Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Toast.makeText(MainActivity.this, "Provider " + provider + " disabled!",
                    Toast.LENGTH_SHORT).show();
        }
    }
}

/*
 * Copyright (C) 2012 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
/*
 * package com.hackbridge.viral;
 * 
 * import java.util.ArrayList; import java.util.List;
 * 
 * import com.javacodegeeks.android.locationservicetest.MainActivity;
 * 
 * import android.app.ListActivity; import android.content.Context; import
 * android.content.Intent; import android.content.SharedPreferences; import
 * android.content.pm.PackageManager; import android.content.pm.ResolveInfo; import
 * android.location.Location; import android.location.LocationListener; import
 * android.os.Bundle; import android.provider.Settings; import android.util.Log; import
 * android.view.View; import android.view.ViewGroup; import android.widget.BaseAdapter;
 * import android.widget.ListView; import android.widget.TextView; import
 * android.widget.Toast;
 * 
 * /** Home activity for app navigation code samples.
 */
/*
 * public class AppNavHomeActivity extends ListActivity { private AwarenessState
 * getAware() { SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
 * // identity reading and init switch int avr = sharedPref.getInt("aware", -1);
 * 
 * switch (avr) { case -1: // writes start state and initialises awareness
 * SharedPreferences.Editor editor = sharedPref.edit(); editor.putInt("aware", 0);
 * editor.commit(); return AwarenessState.UNAWARE; case 0: return AwarenessState.UNAWARE;
 * case 1: return AwarenessState.AWARE; } return AwarenessState.UNAWARE; }
 * 
 * private void setAware(AwarenessState aware) { SharedPreferences sharedPref =
 * this.getPreferences(Context.MODE_PRIVATE); SharedPreferences.Editor editor =
 * sharedPref.edit(); switch (aware) { case UNAWARE: editor.putInt("aware", 0); break;
 * case AWARE: editor.putInt("aware", 1); break; } editor.commit(); }
 * 
 * private long getIdentity() { SharedPreferences sharedPref =
 * this.getPreferences(Context.MODE_PRIVATE); return sharedPref.getLong("identity", -1); }
 * 
 * private void setIdentity(long ident) { SharedPreferences sharedPref =
 * this.getPreferences(Context.MODE_PRIVATE); SharedPreferences.Editor editor =
 * sharedPref.edit(); editor.putLong("identity", ident); editor.commit(); }
 * 
 * @Override protected void onCreate(Bundle savedInstanceState) {
 * super.onCreate(savedInstanceState);
 * 
 * Log.d("LAG-GEO", "Before geo, onCreate in App.."); Intent intent = new
 * Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS); startActivity(intent);
 * 
 * //locationManager.requestLocationUpdates(provider, 200, 1, mylistener);
 * 
 * Log.d("LAG-GEO", "After creation geo"); setListAdapter(new
 * SampleAdapter(querySampleActivities())); }
 * 
 * @Override protected void onListItemClick(ListView lv, View v, int pos, long id) {
 * SampleInfo info = (SampleInfo) getListAdapter().getItem(pos);
 * 
 * Log.d("LAG", "GAZ"); while (true) { Log.d("LAG-GEO", "looping"); try {
 * Thread.sleep(2000); } catch (InterruptedException e) { Log.d("LAG-GEO",
 * "InterruptedException? What should I do?"); } if (location.getLocation() != null) {
 * Log.d("LAG-GEO", "Latitude: " + location.getLocation().getLatitude() + ", Longitude: "
 * + location.getLocation().getLongitude() + ", Altitude: " +
 * location.getLocation().getAltitude()); break; } } // NetworkTest.runTest();
 * Log.d("LAG", "GAX");
 * 
 * startActivity(info.intent); }
 * 
 * protected List<SampleInfo> querySampleActivities() { Intent intent = new
 * Intent(Intent.ACTION_MAIN, null); intent.setPackage(getPackageName());
 * intent.addCategory(Intent.CATEGORY_SAMPLE_CODE);
 * 
 * PackageManager pm = getPackageManager(); List<ResolveInfo> infos =
 * pm.queryIntentActivities(intent, 0);
 * 
 * ArrayList<SampleInfo> samples = new ArrayList<SampleInfo>();
 * 
 * final int count = infos.size(); for (int i = 0; i < count; i++) { final ResolveInfo
 * info = infos.get(i); final CharSequence labelSeq = info.loadLabel(pm); String label =
 * labelSeq != null ? labelSeq.toString() : info.activityInfo.name;
 * 
 * Intent target = new Intent();
 * target.setClassName(info.activityInfo.applicationInfo.packageName,
 * info.activityInfo.name); SampleInfo sample = new SampleInfo(label, target);
 * samples.add(sample); }
 * 
 * return samples; }
 * 
 * static class SampleInfo { String name; Intent intent;
 * 
 * SampleInfo(String name, Intent intent) { this.name = name; this.intent = intent; } }
 * 
 * class SampleAdapter extends BaseAdapter { private List<SampleInfo> mItems;
 * 
 * public SampleAdapter(List<SampleInfo> items) { mItems = items; }
 * 
 * @Override public int getCount() { return mItems.size(); }
 * 
 * @Override public Object getItem(int position) { return mItems.get(position); }
 * 
 * @Override public long getItemId(int position) { return position; }
 * 
 * @Override public View getView(int position, View convertView, ViewGroup parent) { if
 * (convertView == null) { convertView =
 * getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
 * convertView.setTag(convertView.findViewById(android.R.id.text1)); } TextView tv =
 * (TextView) convertView.getTag(); tv.setText(mItems.get(position).name); return
 * convertView; }
 * 
 * }
 * 
 * private class MyLocationListener implements LocationListener {
 * 
 * @Override public void onLocationChanged(Location location) { // Initialize the location
 * fields latitude.setText("Latitude: " + String.valueOf(location.getLatitude()));
 * longitude.setText("Longitude: " + String.valueOf(location.getLongitude()));
 * provText.setText(provider + " provider has been selected.");
 * 
 * Toast.makeText(MainActivity.this, "Location changed!", Toast.LENGTH_SHORT).show(); }
 * 
 * @Override public void onStatusChanged(String provider, int status, Bundle extras) {
 * Toast.makeText(MainActivity.this, provider + "'s status changed to " + status + "!",
 * Toast.LENGTH_SHORT).show(); }
 * 
 * @Override public void onProviderEnabled(String provider) {
 * Toast.makeText(MainActivity.this, "Provider " + provider + " enabled!",
 * Toast.LENGTH_SHORT).show();
 * 
 * }
 * 
 * @Override public void onProviderDisabled(String provider) {
 * Toast.makeText(MainActivity.this, "Provider " + provider + " disabled!",
 * Toast.LENGTH_SHORT).show(); } } }
 */