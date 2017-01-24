package com.vedant.hereami;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class MainActivity extends FragmentActivity implements GeoQueryEventListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnCameraChangeListener, OnMapReadyCallback {
    private static final GeoLocation INITIAL_CENTER = new GeoLocation(23.0977, 72.5491);
    private static final int INITIAL_ZOOM_LEVEL = 14;
    private static final String GEO_FIRE_DB = "https://geofire-17aa9.firebaseio.com";
    private static final String GEO_FIRE_REF = GEO_FIRE_DB + "/_data";

    public GoogleMap map;
    public Circle searchCircle;
    public GeoFire geoFire;
    public GeoQuery geoQuery;
    private Map<String, Marker> markers;
    public Marker marker;

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LatLng latLngCenter = new LatLng(INITIAL_CENTER.latitude, INITIAL_CENTER.longitude);
        Log.d("LatlngCenter",latLngCenter.toString());



        searchCircle = map.addCircle(new CircleOptions().center(new LatLng(23.0977, 72.5491)).radius(100000));
        searchCircle.setFillColor(Color.argb(66, 255, 0, 255));
        searchCircle.setStrokeColor(Color.argb(66, 0, 0, 0));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngCenter, INITIAL_ZOOM_LEVEL));
        map.setOnCameraChangeListener(this);

        map.setMyLocationEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MultiDex.install(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // FirebaseOptions options = new FirebaseOptions.Builder().setApplicationId("geofire").setDatabaseUrl(GEO_FIRE_DB).build();
        //FirebaseApp app = FirebaseApp.initializeApp(this, options);

        // setup GeoFire
        this.geoFire = new GeoFire(FirebaseDatabase.getInstance().getReferenceFromUrl(GEO_FIRE_REF));
        // radius in km
        this.geoQuery = this.geoFire.queryAtLocation(INITIAL_CENTER, 1);



        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        LocationListener mlocListener = new MyLocationListener();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /* Class My Location Listener */
    public class MyLocationListener implements LocationListener {


        @Override
        public void onLocationChanged(Location loc) {

            //loc.getLatitude();
            //loc.getLongitude();
            geoFire.setLocation("a", new GeoLocation(loc.getLatitude(), loc.getLongitude()), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    if (error != null) {
                        System.err.println("There was an error saving the location to GeoFire: " + error);
                    } else {
                        System.out.println("Location saved on server successfully!");
                    }
                }

            });
    }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        // Add a new marker to the map
        System.out.println(location.latitude);
        System.out.println(location.longitude);
        geoFire.getLocation("a", new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null) {


                    System.out.println(String.format("The location for key %s is [%f,%f]", key, location.latitude, location.longitude));
                    if(marker!=null)
                    {

                        marker.remove();
                    }
                    marker = map.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)));
                    marker.setTag(0);
                    System.out.println(marker.getId());
                    //markers.put(key, marker);
                } else {
                    System.out.println(String.format("There is no location for key %s in GeoFire", key));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("There was an error getting the GeoFire location: " + databaseError);
            }
        });
        //Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)));
        //markers.put(key, marker);
    }


    @Override
    public void onKeyExited(String key) {
        // Remove any old marker
        System.out.println("Key is " + key);

        marker.getTag();
        this.marker.remove();
        //if (marker != null) {
         //   marker.remove();
           // this.markers.remove(key);
    //markers.clear();
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        // Move the marker
        //Marker marker = this.markers.get(key);
        //if (marker != null) {
         //   this.animateMarkerTo(marker, location.latitude, location.longitude);
        //}


        geoFire.getLocation("a", new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null) {
                    System.out.println(String.format("The location for key %s is [%f,%f]", key, location.latitude, location.longitude));
                    if(marker!=null)
                    {

                        marker.remove();
                    }
                    marker = map.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)));
                   marker.setTag(0);

                    // markers.put(key, marker);
                } else {
                    System.out.println(String.format("There is no location for key %s in GeoFire", key));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("There was an error getting the GeoFire location: " + databaseError);
            }
        });
    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("There was an unexpected error querying GeoFire: " + error.getMessage())
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    private double zoomLevelToRadius(double zoomLevel) {
        // Approximation to fit circle into view
        return 16384000/Math.pow(2, zoomLevel);
    }
    @Override
    protected void onStop() {
        super.onStop();
        // remove all event listeners to stop updating in the background
        this.geoQuery.removeAllListeners();
        //for (Marker marker: this.markers.values()) {
          //  marker.remove();
        //}
        //this.markers.clear();
    }
    @Override
    protected void onStart() {
        super.onStart();
        // add an event listener to start updating locations again
        this.geoQuery.addGeoQueryEventListener(this);
    }
    private void animateMarkerTo(final Marker marker, final double lat, final double lng) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long DURATION_MS = 3000;
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final LatLng startPosition = marker.getPosition();
        handler.post(new Runnable() {
            @Override
            public void run() {
                float elapsed = SystemClock.uptimeMillis() - start;
                float t = elapsed/DURATION_MS;
                float v = interpolator.getInterpolation(t);

                double currentLat = (lat - startPosition.latitude) * v + startPosition.latitude;
                double currentLng = (lng - startPosition.longitude) * v + startPosition.longitude;
                marker.setPosition(new LatLng(currentLat, currentLng));

                // if animation is not finished yet, repeat
                if (t < 1) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }


    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        // Update the search criteria for this geoQuery and the circle on the map
        LatLng center = cameraPosition.target;
        double radius = zoomLevelToRadius(cameraPosition.zoom);
        this.searchCircle.setCenter(center);
        this.searchCircle.setRadius(radius);
        this.geoQuery.setCenter(new GeoLocation(center.latitude, center.longitude));
        // radius in km
        this.geoQuery.setRadius(radius/1000);
    }
}
