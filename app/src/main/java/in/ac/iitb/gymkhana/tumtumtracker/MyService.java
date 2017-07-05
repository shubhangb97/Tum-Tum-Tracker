package in.ac.iitb.gymkhana.tumtumtracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

public class MyService extends Service {
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    Ndef nfctag;
        private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }
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
            if(nfctag.isConnected())
            {
            mLastLocation.set(location);
            double lat,lon;
            lat=mLastLocation.getLatitude();
            lon=mLastLocation.getLongitude();
            sendData(lat,lon);
            }
            else
            {
                stopSelf();
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
        Tag tag=intent.getParcelableExtra("ndeftag");
        nfctag=Ndef.get(tag);

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
        sendData(lat,lon);

    }
    public void sendData(double lat,double lon)
    {
        URL u;
        URL uget;
        try {
            u = new URL("http://139.59.36.85/");
            uget=new URL("http://139.59.36.85/?format=json");
            String nfctext="";
            NdefMessage nfcid=nfctag.getCachedNdefMessage();
            NdefRecord[] records = nfcid.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    nfctext = readText(ndefRecord);
                }
            }
            HttpURLConnection hget = (HttpURLConnection) uget.openConnection();
            hget.setRequestMethod("GET");
            InputStream iget=hget.getInputStream();
            BufferedReader br=new BufferedReader(new InputStreamReader(iget));
            JSONArray busdata=new JSONArray(br.readLine());
            JSONObject jobj=null;
            for(int i=0;i<busdata.length();i++)
            {
                jobj=busdata.getJSONObject(i);
                if(jobj.getInt("nfc_id")==Integer.parseInt(nfctext))
                    break;
            }
            JSONObject j=new JSONObject();
            j.put("bus_id",jobj.getInt("bus_id"));
            j.put("nfc_id",Integer.parseInt(nfctext));
            j.put("latitude",lat);
            j.put("longitude",lon);

            HttpURLConnection h = (HttpURLConnection) u.openConnection();
            h.setRequestMethod("POST");
            OutputStream os=h.getOutputStream();
            BufferedWriter d= new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
            d.write(j.toString());
            d.flush();
            d.close();
            os.close();

        }
        catch(JSONException e){
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch(IOException e)
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
