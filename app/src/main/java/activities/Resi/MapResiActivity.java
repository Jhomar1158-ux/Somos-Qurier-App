package activities.Resi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DatabaseError;
import com.google.maps.android.SphericalUtil;
import com.optic.curri.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import activities.MainActivity;
import includes.MyToolbar;
import providers.AuthProvider;
import providers.GeofireProvider;
import providers.TokenProvider;

public class MapResiActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private AuthProvider mAuthProvider;
    private GeofireProvider mGeoFireProvider;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;

    private Marker mMarker;
    private LatLng mCurrentLatlng;

    private List<Marker> mCouriersMarkers = new ArrayList<>();
    private boolean mIsFirstTime = true;

    private AutocompleteSupportFragment mAutocomplete;
    private AutocompleteSupportFragment mAutocompleteDestination;
    private PlacesClient mPlaces;

    //Primer buscador
    private String mOrigin;
    private LatLng mOriginLatLng;

    //Segundo buscador
    private String mDestination;
    private LatLng mDestinationLatLng;

    private GoogleMap.OnCameraIdleListener mCameraIdListener;
    private Button mButtonRequestCourier;

    private TokenProvider mTokenProvider;

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {

                    mCurrentLatlng = new LatLng(location.getLatitude(), location.getLongitude());
                    // SINCRONIZAMOS EL ICONO DEL COURIER
                    /*
                    if (mMarker != null) {
                        mMarker.remove();
                    }
                    mMarker = mMap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(), location.getLongitude())

                            )
                                    .title("Tu posición")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_resi))
                    );*/
                    // OBTENER LA LOCALIZACION DEL USUARIO EN TIEMPO REAL
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(17f)
                                    .build()
                    ));

                    if (mIsFirstTime) {
                        //Ponemos en false para que se ejecute solo una vez.
                        mIsFirstTime = false;
                        getActiveCouriers();
                        limitSearch();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_resi);
        MyToolbar.show(this, "Residente", false);

        mAuthProvider = new AuthProvider();
        mGeoFireProvider = new GeofireProvider();
        mTokenProvider = new TokenProvider();
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);
        mButtonRequestCourier = findViewById(R.id.btnRequestCourier);


        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }

        mPlaces = Places.createClient(this);
        instanceAutocompleteOrigin();
        instanceAutocompleteDestination();
        onCameraMove();

        mButtonRequestCourier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestCourier();
            }
        });
        // CASTEO

        generateToken();
    }

    private void requestCourier(){
        if (mOriginLatLng != null && mDestinationLatLng != null)
        {
            Intent intent = new Intent(MapResiActivity.this,DetailRequestActivity.class);
            intent.putExtra("origin_lat",mOriginLatLng.latitude);
            intent.putExtra("origin_lng",mOriginLatLng.longitude);
            intent.putExtra("destination_lat",mDestinationLatLng.latitude);
            intent.putExtra("destination_lng",mDestinationLatLng.longitude);
            intent.putExtra("origin", mOrigin);
            intent.putExtra("destination", mDestination);
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, "Debe seleccionar el lugar de recogida y el destino.", Toast.LENGTH_SHORT).show();
        }
    }

    private  void limitSearch(){
        LatLng northSide = SphericalUtil.computeOffset(mCurrentLatlng,5000, 0);
        LatLng southSide = SphericalUtil.computeOffset(mCurrentLatlng,5000, 180);
        mAutocomplete.setCountry("PE");
        mAutocomplete.setLocationBias(RectangularBounds.newInstance(southSide,northSide));
        mAutocompleteDestination.setCountry("PE");
        mAutocompleteDestination.setLocationBias(RectangularBounds.newInstance(southSide,northSide));
    }

    private void onCameraMove(){
        mCameraIdListener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                try {
                    Geocoder geocoder = new Geocoder(MapResiActivity.this);
                    mOriginLatLng = mMap.getCameraPosition().target;
                    List<Address> addressList = geocoder.getFromLocation(mOriginLatLng.latitude,mOriginLatLng.longitude, 1);
                    String city = addressList.get(0).getLocality();
                    String country = addressList.get(0).getCountryName();
                    String address = addressList.get(0).getAddressLine(0);
                    mOrigin = address + " " + city;
                    mAutocomplete.setText(address + " " + city);
                }
                catch (Exception e){
                    Log.d("Error: ","Mensaje de error: "+ e.getMessage());
                }
            }
        };

    }

    private void instanceAutocompleteOrigin(){
        mAutocompleteDestination = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placeAutocompleteDestination);
        mAutocompleteDestination.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        mAutocompleteDestination.setHint("Destino");
        mAutocompleteDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // Recogemos el nombre de la posición de salida

                mDestination = place.getName();
                mDestinationLatLng = place.getLatLng();

                Log.d("PLACE", "Name: " + mDestination);
                Log.d("PLACE", "Lat: " + mDestinationLatLng.latitude);
                Log.d("PLACE", "Long: " + mDestinationLatLng.longitude);

            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });
    }
    private void instanceAutocompleteDestination(){
        mAutocomplete = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placeAutocompleteOrigin);
        mAutocomplete.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        mAutocomplete.setHint("Origen");
        mAutocomplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // Recogemos el nombre de la posición de salida

                mOrigin = place.getName();
                mOriginLatLng = place.getLatLng();

                Log.d("PLACE", "Name: " + mOrigin);
                Log.d("PLACE", "Lat: " + mOriginLatLng.latitude);
                Log.d("PLACE", "Long: " + mOriginLatLng.longitude);


            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });

    }

    private void getActiveCouriers() {
        mGeoFireProvider.getActiveCouriers(mCurrentLatlng, 10).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            // Donde añadiremos los marcadores de los conductores que se unan
            public void onKeyEntered(String key, GeoLocation location) {

                for (Marker marker : mCouriersMarkers) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            return;
                        }

                    }

                }
                LatLng courierLatLng = new LatLng(location.latitude, location.longitude);
                Marker marker = mMap.addMarker(new MarkerOptions().position(courierLatLng).title("Courier disponible.").icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_curi_1)));
                marker.setTag(key);
                mCouriersMarkers.add(marker);
            }

            @Override
            // ELiminimos los marcadores de los conductores que se desconectan
            public void onKeyExited(String key) {
                for (Marker marker : mCouriersMarkers) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            marker.remove();
                            mCouriersMarkers.remove(marker);
                            return;
                        }

                    }

                }

            }

            @Override
            //Actualizaremos en tiempo real la posición del courier mientras se mueve
            public void onKeyMoved(String key, GeoLocation location) {
                for (Marker marker : mCouriersMarkers) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            marker.setPosition(new LatLng(location.latitude, location.longitude));
                            return;
                        }

                    }

                }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnCameraIdleListener(mCameraIdListener);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);

        startLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (gpsActived()) {
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    } else {
                        // Para mostrarle al usuario que tiene el GPS apagado.
                        showAlertDialogNOGPS();
                    }
                } else {
                    //Para que le pida sí o sí al usuario que active su GPS
                    checkLocationPermissions();
                }
            } else {
                checkLocationPermissions();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Si el usuario activa el GPS recién podrá entrar !!!
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived()) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mMap.setMyLocationEnabled(true);
        }
        else if (requestCode == SETTINGS_REQUEST_CODE && !gpsActived()){
            showAlertDialogNOGPS();
        }
    }

    private void showAlertDialogNOGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Por favor activa tu ubicación para continuar.")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE);
                    }
                }).create().show();
    }


    //Nos dirá si tiene activo el GPS
    private boolean gpsActived() {
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true;
        }
        return isActive;
    }

    private void startLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                }
                else {
                    showAlertDialogNOGPS();
                }
            }
            else {
                checkLocationPermissions();
            }
        } else {
            if (gpsActived()) {
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            }
            else {
                showAlertDialogNOGPS();
            }
        }
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar.")
                        .setMessage("Esta aplicacion requiere de los permisos de ubicación para poder utilizarse.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapResiActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            }
            else {
                ActivityCompat.requestPermissions(MapResiActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.resimenu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
        }
        return super.onOptionsItemSelected(item);
    }

    void logout() {
        mAuthProvider.logout();
        Intent intent = new Intent(MapResiActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    void generateToken(){
        mTokenProvider.create(mAuthProvider.getId());

    }
}