package com.hackbridge.viral;

import java.io.Serializable;
import android.location.Location;

public class LocationWrapper  implements Serializable
{
    private Location l;
    
    public Location getLocation()
    {
        return l;
    }
    
    public LocationWrapper(Location loc)
    {
        l = loc;
    }
    
}
