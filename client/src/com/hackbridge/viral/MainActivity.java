package com.hackbridge.viral;

import java.io.IOException;
import java.net.Socket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity
{
    private ImageView          orb;
    private ImageView          leftSyringe;
    private ImageView          rightSyringe;
    private EditText           codeInputTextBox;
    private TextView           vaccCodeLabel;
    private Button             submitButton;
    private TextView           stateLabel;
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
        Drawable drawable;
        switch (phys)
        {
            case -1:
                // writes start state and initialises physical state
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("physical", 0);
                editor.commit();
                physical = PhysicalState.SUSCEPTIBLE;
                //orb.setImageResource(R.drawable.circle_blue);
                //stateLabel.setText("SUSCEPTIBLE");
                break;
            case 0:
                physical = PhysicalState.SUSCEPTIBLE;
                //orb.setImageResource(R.drawable.circle_blue);
                //stateLabel.setText("SUSCEPTIBLE");
                break;
            case 1:
                physical = PhysicalState.VACCINATED;
                //orb.setImageResource(R.drawable.circle_green);
                //stateLabel.setText("VACCINATED");
                break;
            case 2:
                physical = PhysicalState.INFECTED;
                //orb.setImageResource(R.drawable.circle_red);
                //stateLabel.setText("INFECTED");
                break;
        }
        Log.d("LAG-LOGIC", "Loaded Physiscal State is: " + physical);
        return physical;
    }

    public void setPhysicalState(PhysicalState physicalState)
    {
        PhysicalState oldState = loadPhysicalState();
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        switch (physicalState)
        {
            case SUSCEPTIBLE:
                editor.putInt("physical", 0);
                break;
            case VACCINATED:
               /* if (oldState != PhysicalState.VACCINATED) Toast.makeText(
                        MainActivity.this, "Successful vaccination!",
                        Toast.LENGTH_SHORT).show();*/
                editor.putInt("physical", 1);
                break;
            case INFECTED:
                /*if (oldState != PhysicalState.INFECTED) Toast.makeText(MainActivity.this,
                        "You have been infected",
                        Toast.LENGTH_SHORT).show();*/
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

    public void setAwareness(AwarenessState aware, String code)
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

        orb = (ImageView) findViewById(R.id.orb);
        leftSyringe = (ImageView) findViewById(R.id.leftSyringe);
        rightSyringe = (ImageView) findViewById(R.id.rightSyringe);
        codeInputTextBox = (EditText) findViewById(R.id.codeInputTextBox);
        vaccCodeLabel = (TextView) findViewById(R.id.vaccCodeLabel);
        submitButton = (Button) findViewById(R.id.submitButton);
        stateLabel = (TextView) findViewById(R.id.stateLabel);
        
        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the location provider
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_LOW); // default

        // bear with this for now
        submitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (ms != null)
                ms.sendMessage(new CodeMessage(identity, codeInputTextBox.getText()
                        .toString()));
                codeInputTextBox.setText("");
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
