package com.hackbridge.viral;

import android.app.*;
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
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import java.io.IOException;
import java.net.Socket;

public class MainActivity extends Activity
{
    public static Handler handle;
    private static MessageSender ms;
    private static MessageReceiver mr;
    private LocationManager locationManager;
    private String provider;
    private MyLocationListener mylistener;
    private Criteria criteria;
    private boolean round_on;
    private AwarenessState awareness;
    private PhysicalState physical;
    private long identity;
    private String server = "192.168.1.12";//"192.168.0.30";//"77.46.191.59";//"188.166.154.60";
    private int port = 25000;
    private Socket sock;
    final MainActivity ma = this;

    public void setRoundOn(boolean isOn)
    {
        round_on = isOn;
    }

    /**
     * creates a new instance of the sender and receiver threads
     *
     * @return true on success
     */
    public synchronized void restartEverything()
    {
            Log.d("LAG", "attempting reconnection");
            Thread messageThread = new Thread()
            {
                @Override
                public void run()
                {
                    ms = null;
                    mr = null;
                    try
                    {
                        Log.d("LAG", "Pre socket creation " + server + ", port " + port);
                        sock = new Socket(server, port);
                        Log.d("LAG", "Socket created");

                    } catch (IOException e)
                    {
                        // we reattempt connection
                        if (!reatemptConnection())
                        {
                            // we failed to reconnect
                            fail();
                        }
                    }

                    boolean success;
                    do
                    {
                        // attempt to make the sender and receiver and restart on failure
                        success = setupSenderAndREceiver();
                    } while (!success && reatemptConnection());

                    if (!success)
                    {
                        fail();
                    }
                    else
                    {
                        mr.setDaemon(true);
                        mr.start();
                    }
                }
            };
            messageThread.setDaemon(true);
            messageThread.start();
    }

    /**
     * Notifies user about failure and kills the app
     */
    private void fail()
    {
        // FAILURE, so we notify the user and exit
        Toast.makeText(MainActivity.this, "Failed to connect to server, please try again later!", Toast.LENGTH_LONG).show();
        System.exit(1);
    }

    /**
     * Attempts to reconnect to the defined server and port
     *
     * @return the socket if successful and null if not
     */
    private Socket reconnect()
    {
        try
        {
            sock = new Socket(server, port);
            return sock;
        } catch (IOException e)
        {
            Log.d("LAG", "Cannot connect to " + server + ", port " + port);
            return null;
        }
    }

    /**
     * Repeatedly Aattempts to reconnect to the defined server and port with exponential backoff
     */
    private boolean reatemptConnection()
    {
        int backoff = 1;
        boolean socket_is_alive = false;
        for (int i = 0; i < 7 && !socket_is_alive; i++)
        {
            Log.d("LAG", "Retrying connection: " + i);
            try
            {
                Thread.sleep(backoff * 1000); // sleep backoff seconds
            } catch (InterruptedException e)
            {
                // silently ignore this
            }
            // attempt reconnection
            if (reconnect() != null)
            {
                // success! we continue
                socket_is_alive = true;
            }
            backoff *= 2;
        }
        return socket_is_alive;
    }

    /**
     * Instantiates the sender and receiver threads
     *
     * @return true on success
     */
    private boolean setupSenderAndREceiver()
    {
        mr = new MessageReceiver(ma, sock);
        try
        {
            ms = new MessageSender(ma, sock);
        } catch (IOException e)
        {
            Log.d("LAG", "Could not initialise sender thread, retrying!");
            return false;
        }

        if (identity == -1) ms.sendMessage(new HelloNewMessage());
        else ms.sendMessage(new HelloMessage(identity));
        Log.d("LAG", "Sending hello message with id " + identity);
        return true;
    }

    /**
     * Sends a pop-up notification
     */
    public void writeNotification(String title, String body)
    {
        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.icon_syringe_left)
                        .setContentTitle(title)
                        .setContentText(body);
        // Creates an explicit intent for an Activity in the app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // the application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Notification mNotification = mBuilder.build();
        // We want to hide the notification after it was selected
        mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(1, mNotification);
    }

    /**
     * Loads the physical state from the saved state
     */
    public PhysicalState loadPhysicalState()
    {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        // identity reading and init switch
        int phys = sharedPref.getInt("physical", -1);
        switch (phys)
        {
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

    /**
     * Saves the given physical state in memory
     */
    public void setPhysicalState(PhysicalState physicalState)
    {
        PhysicalState oldState = loadPhysicalState();
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        switch (physicalState)
        {
            case SUSCEPTIBLE:
            case CARRIER:
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

    /**
     * Loads the special code from the saved state if one exists
     */
    private String loadCode()
    {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        // identity reading and init switch
        String code = sharedPref.getString("code", "");
        Log.d("LAG-LOGIC", "Loaded code is: " + code);
        return code;
    }

    /**
     * Saves the given code in memory
     */
    public void setCode(String code)
    {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("code", code);
        editor.commit();
        Log.d("LAG-LOGIC", "Code is: " + code);
    }

    /**
     * Loads the unique identity number
     */
    private long loadIdentity()
    {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        identity = sharedPref.getLong("identity", -1);
        Log.d("LAG-LOGIC", "Loaded identity is: " + identity);
        return identity;
    }

    /**
     * Stores the unique identity number
     */
    public void setIdentity(long ident)
    {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("identity", ident);
        editor.commit();
        identity = ident;
        Log.d("LAG-LOGIC", "Identity is: " + identity);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent mainIntent = getIntent();

        String ipText =  mainIntent.getStringExtra("ip");
        String portText = mainIntent.getStringExtra("port");
        Log.d("LAG", "ip "+ ipText + "\nport " + portText);
        try
        {
            server = ipText;
            port = Integer.parseInt(portText);
        } catch (NumberFormatException n)
        {
            // fails because the port was malformed
            fail();
        }

        final ImageView orb = (ImageView) findViewById(R.id.orb);
        final ImageView leftSyringe = (ImageView) findViewById(R.id.leftSyringe);
        final ImageView rightSyringe = (ImageView) findViewById(R.id.rightSyringe);
        final EditText codeInputTextBox = (EditText) findViewById(R.id.codeInputTextBox);
        final TextView vaccCodeLabel = (TextView) findViewById(R.id.vaccCodeLabel);
        final Button submitButton = (Button) findViewById(R.id.submitButton);
        final TextView stateLabel = (TextView) findViewById(R.id.stateLabel);
        final EditText codeGiver = (EditText) findViewById(R.id.codeGiver);
        final TextView instructionsLabel = (TextView) findViewById(R.id.instructionsLabel);

        // add a listener for the button
        submitButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (ms != null)
                    ms.sendMessage(new CodeMessage(identity, codeInputTextBox.getText().toString()));
            }
        });

        /*
         * Main UI thread
         */
        handle = new Handler(Looper.getMainLooper())
        {
            @Override
            public void handleMessage(android.os.Message inputMessage)
            {
                ChangeMessage front = (ChangeMessage) inputMessage.obj;
                PhysicalState physical = front.getInfected();
                String code = front.getCode();

                if (code.equals("~"))
                {
                    orb.setImageResource(R.drawable.circle_gray);
                    stateLabel.setText("ROUND NOT STARTED");
                    instructionsLabel.setText("");
                    codeGiver.setText("");
                    setCode("");
                }
                else if (code.equals("-"))
                {
                    orb.setImageResource(R.drawable.circle_gray);
                    stateLabel.setText("ROUND FINISHED");
                    instructionsLabel.setText("");
                    codeGiver.setText("");
                    writeNotification("You have lost!", "You were unsuccessful in accomplishing your objective. Better luck next time!");
                    setCode("");
                }
                else if (code.equals("+"))
                {
                    orb.setImageResource(R.drawable.circle_gray);
                    stateLabel.setText("ROUND FINISHED");
                    instructionsLabel.setText("");
                    codeGiver.setText("");
                    writeNotification("You have won!", "You have successfully accomplished your objective! Good work!");
                    setCode("");
                }
                else
                {
                    if (inputMessage.what == 1)
                    {
                        // human
                        orb.setImageResource(R.drawable.circle_blue);
                        stateLabel.setText("SUSCEPTIBLE");
                        setPhysicalState(PhysicalState.SUSCEPTIBLE);
                        String s = "You are a <b>HUMAN</b>!<br>Your objective is to finish the round without getting infected.";
                        instructionsLabel.setText(Html.fromHtml(s));
                    }
                    else if (inputMessage.what == 2)
                    {
                        // infector
                        orb.setImageResource(R.drawable.circle_blue);
                        stateLabel.setText("SUSCEPTIBLE");
                        setPhysicalState(PhysicalState.SUSCEPTIBLE);
                        String s = "You are an <b>INFECTOR</b>!<br>Your objective is to help infect at least half of the population by the end of the round.";
                        instructionsLabel.setText(Html.fromHtml(s));
                    }

                    PhysicalState oldPhysical = loadPhysicalState();

                    if (oldPhysical != physical)
                    {
                        switch (physical)
                        {
                            case SUSCEPTIBLE:
                            case CARRIER:
                                orb.setImageResource(R.drawable.circle_blue);
                                stateLabel.setText("SUSCEPTIBLE");
                                break;
                            case VACCINATED:
                                orb.setImageResource(R.drawable.circle_green);
                                stateLabel.setText("VACCINATED");
                                writeNotification("You are VACCINATED!", "Your vaccination has been successful!");
                                break;
                            case INFECTED:
                                orb.setImageResource(R.drawable.circle_red);
                                stateLabel.setText("INFECTED");
                                writeNotification("You are INFECTED!", "Visit Viral, and don't lose hope!");
                                break;
                        }
                        setPhysicalState(physical);
                    }

                    String oldCode = loadCode();
                    if (!code.equals(oldCode))
                    {
                        setCode(code);
                        codeGiver.setText("Your Viral code is:\n\n" + code + "\n\nShare with care!");
                        if (!code.equals(""))
                        {
                            writeNotification("You are AWARE!", "Visit Viral for your vaccination code!");
                        }
                    }
                }
            }
        };


        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // provider = LocationManager.NETWORK_PROVIDER;
        if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER) &&
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            provider = LocationManager.GPS_PROVIDER;
            Log.d("LAG-GPS", "GPS location provider selected");
        }
        else if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER) &&
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
            provider = LocationManager.NETWORK_PROVIDER;
            Log.d("LAG-GPS", "NETWORK location provider selected");
        }
        else
        {
            // TODO ADD ERROR HANDLING FOR NO NETWORK OR GPS PROVIDER AVAILABLE
            return;
        }
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

        final MainActivity m = this;
        restartEverything();
    }

    /**
     * Listener designated to fire off a wrapped location to the server, once position changes
     */
    private class MyLocationListener implements LocationListener
    {
        @Override
        public void onLocationChanged(Location location)
        {
            if (round_on && ms != null)
                ms.sendMessage(new PositionMessage(identity, new LocationWrapper(location.getLongitude(), location.getLatitude(), location.getAltitude())));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Toast.makeText(MainActivity.this, provider + "'s status changed to " + status + "!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Toast.makeText(MainActivity.this, "Provider " + provider + " enabled!", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Toast.makeText(MainActivity.this, "Provider " + provider + " disabled!", Toast.LENGTH_SHORT).show();
        }
    }
}
