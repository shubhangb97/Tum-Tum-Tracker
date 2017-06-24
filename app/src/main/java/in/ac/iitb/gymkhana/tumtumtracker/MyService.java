package in.ac.iitb.gymkhana.tumtumtracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MyService extends Service {
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    public MyService() {
    }
    private class locationListener implements LocationListener
    {
        Location mLastLocation;

        public locationListener(String provider)
        {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            mLastLocation.set(location);
            double lat,lon;
            lat=mLastLocation.getLatitude();
            lon=mLastLocation.getLongitude();
            URL u= null;
            try {
                u = new URL("nmm");//put URL here
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                HttpURLConnection h = (HttpURLConnection) u.openConnection();
                h.setRequestMethod("POST");
                OutputStream os=h.getOutputStream();
                BufferedWriter d= new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
                d.write(lat+"");
                d.write(lon+"");
                d.flush();
                d.close();
                os.close();

            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onProviderDisabled(String provider)
        {
        }

        @Override
        public void onProviderEnabled(String provider)
        {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
        }
    }
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }
    locationListener mLocationListener = new locationListener(LocationManager.GPS_PROVIDER);

    @Override
    public void onCreate()
    {
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListener);
        } catch (java.lang.SecurityException ex) {
        } catch (IllegalArgumentException ex) {
        }
        double lat,lon;
        lat=mLocationListener.mLastLocation.getLatitude();
        lon=mLocationListener.mLastLocation.getLongitude();
        URL u= null;
        try {
            u = new URL("");//put URL here
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpURLConnection h = (HttpURLConnection) u.openConnection();
            h.setRequestMethod("POST");
            OutputStream os=h.getOutputStream();
            BufferedWriter d= new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
            d.write(lat+"");
            d.write(lon+"");
            d.flush();
            d.close();
            os.close();

        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (mLocationManager != null) {

                try {
                    mLocationManager.removeUpdates(mLocationListener);
                } catch (Exception ex) {
                }

        }
    }
}
