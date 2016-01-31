package com.hackbridge.viral;

import java.io.IOException;
import java.net.Socket;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
    private LocationManager        locationManager;
    private String                 provider;
    private MyLocationListener     mylistener;
    private Criteria               criteria;

    private boolean                round_on;
    private AwarenessState         awareness;
    private PhysicalState          physical;
    private long                   identity;
    private String                 server = "188.166.154.60";
    private int                    port   = 25000;
    private static MessageSender   ms;
    private static MessageReceiver mr;
    private Socket                 sock;

    public static Handler          handle;

    public void setRoundOn(boolean isOn)
    {
        round_on = isOn;
    }

    public void writeNotification(String title, String body)
    {
        Notification.Builder mBuilder =
            new Notification.Builder(this)
                    .setSmallIcon(R.drawable.icon_syringe_left)
                    .setContentTitle(title)
                    .setContentText(body);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);
        //
        // // The stack builder object will contain an artificial back stack for the
        // // started Activity.
        // // This ensures that navigating backward from the Activity leads out of
        // // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
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
        PhysicalState oldState = loadPhysicalState();
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        switch (physicalState)
        {
            case SUSCEPTIBLE:
                editor.putInt("physical", 0);
                break;
            case VACCINATED:
                if (oldState != PhysicalState.VACCINATED) writeNotification("Success!",
                        "Your vaccination has been successful!");
                editor.putInt("physical", 1);
                break;
            case INFECTED:
                /*
                 * if (oldState != PhysicalState.INFECTED)
                 * Toast.makeText(MainActivity.this, "You have been infected",
                 * Toast.LENGTH_SHORT).show();
                 */
                editor.putInt("physical", 2);
                break;
        }
        editor.commit();
        Log.d("LAG-LOGIC", "Physiscal State is: " + physical);
        physical = physicalState;
    }

    private String loadCode()
    {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        // identity reading and init switch
        String code = sharedPref.getString("code", "");
        Log.d("LAG-LOGIC", "Loaded code is: " + code);
        return code;
    }

    public void setCode(String code)
    {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("code", code);
        editor.commit();
        Log.d("LAG-LOGIC", "Code is: " + code);
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

        final ImageView orb = (ImageView) findViewById(R.id.orb);
        final ImageView leftSyringe = (ImageView) findViewById(R.id.leftSyringe);
        final ImageView rightSyringe =
            (ImageView) findViewById(R.id.rightSyringe);
        final EditText codeInputTextBox =
            (EditText) findViewById(R.id.codeInputTextBox);
        final TextView vaccCodeLabel =
            (TextView) findViewById(R.id.vaccCodeLabel);
        final Button submitButton = (Button) findViewById(R.id.submitButton);
        final TextView stateLabel = (TextView) findViewById(R.id.stateLabel);
        final EditText codeGiver = (EditText) findViewById(R.id.codeGiver);

        // bear with this for now
        submitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (ms != null)
                ms.sendMessage(new CodeMessage(identity, codeInputTextBox
                        .getText()
                        .toString()));
            }
        });
        
        handle = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(android.os.Message inputMessage)
            {
                ChangeMessage front = (ChangeMessage) inputMessage.obj;
                PhysicalState physical = front.getInfected();
                String code = front.getCode();

                PhysicalState oldPhysical = loadPhysicalState();

                if (oldPhysical != physical)
                {
                    switch (physical)
                    {
                        case SUSCEPTIBLE:
                            orb.setImageResource(R.drawable.circle_blue);
                            stateLabel.setText("SUSCEPTIBLE");
                            break;
                        case VACCINATED:
                            orb.setImageResource(R.drawable.circle_green);
                            stateLabel.setText("VACCINATED");
                            break;
                        case INFECTED:
                            orb.setImageResource(R.drawable.circle_red);
                            stateLabel.setText("INFECTED");
                            writeNotification("You are INFECTED!",
                                    "Visit Viral, and don't lose hope!");
                            break;
                    }
                    setPhysicalState(physical);
                }

                String oldCode = loadCode();
                if (!code.equals(oldCode))
                {
                    // azurirati lable
                    setCode(code);
                    codeGiver.setText("Your Viral code is:\n\n" + code + "\n\nShare with care!");
                    if (!code.equals(""))
                    {
                        writeNotification("You are AWARE!",
                                "Visit Viral for your vaccination code!");
                    }
                }
            }
        };

        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the location provider
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_LOW); // default

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
        // location updates: at least 1 meter and 100millsecs change
        locationManager.requestLocationUpdates(provider, 100, 0.5f, mylistener);

        round_on = false;
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
