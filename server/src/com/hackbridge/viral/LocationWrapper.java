package com.hackbridge.viral;

import java.io.Serializable;
import android.location.Location;

public class LocationWrapper  implements Serializable
{
    private double longitude;
    private double latitude;
    private double altitude; // 0.0 if not available
    
    public double getLongitude()
    {
        return longitude;
    }
    
    public double getLatitude()
    {
        return latitude;
    }
    
    public double getAltitude()
    {
        return altitude;
    }
    
    public LocationWrapper(Location loc)
    {
        longitude = loc.getLongitude();
        latitude = loc.getLatitude();
        altitude = loc.getAltitude(); // 0.0 if not available
    }
    
}
