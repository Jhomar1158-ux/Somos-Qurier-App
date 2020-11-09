package activities.Resi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.optic.curri.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import includes.MyToolbar;
import providers.GoogleApiProvider;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.optic.curri.utils.DecodePoints;

public class DetailRequestActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private double mExtraOriginLat;
    private double mExtraOriginLng;
    private double mExtraDestinationLat;
    private double mExtraDestinationLng;
    private String mExtraOrigin;
    private String mExtraDestination;

    private LatLng mOriginLatlng;
    private LatLng mDestinationLatlng;

    private GoogleApiProvider mGoogleApiProvider;

    private List<LatLng> mmPolylineList;
    private PolylineOptions mPolylineOptions;

    private TextView mTextViewOrigin;
    private TextView mTextViewDestination;
    private TextView mTextViewTime;
    private TextView mTextViewDistance;
    private Button mButttonRequest;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_request);
        MyToolbar.show(this,"Información", true);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mExtraOriginLat = getIntent().getDoubleExtra("origin_lat",0);
        mExtraOriginLng = getIntent().getDoubleExtra("origin_lng",0);
        mExtraDestinationLat = getIntent().getDoubleExtra("destination_lat",0);
        mExtraDestinationLng= getIntent().getDoubleExtra("destination_lng",0);
         mExtraOrigin = getIntent().getStringExtra("origin");
         mExtraDestination = getIntent().getStringExtra("destination");

        mOriginLatlng = new LatLng(mExtraOriginLat,mExtraOriginLng);
        mDestinationLatlng = new LatLng(mExtraDestinationLat,mExtraDestinationLng);

        mGoogleApiProvider = new GoogleApiProvider(DetailRequestActivity.this);

        mTextViewOrigin = findViewById(R.id.textViewOrigin);
        mTextViewDestination = findViewById(R.id.textViewDestination);
        mTextViewTime = findViewById(R.id.textViewTime);
        mTextViewDistance = findViewById(R.id.textViewDistance);
        mButttonRequest = findViewById(R.id.btnRequestNow);

        mTextViewOrigin.setText(mExtraOrigin);
        mTextViewDestination.setText(mExtraDestination);
        mButttonRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToRequestCourier();
            }
        });

    }
    private void goToRequestCourier() {
        Intent intent = new Intent(DetailRequestActivity.this, RequestCourierActivity.class);
        intent.putExtra("origin_lat",mOriginLatlng.latitude);
        intent.putExtra("origin_lng",mOriginLatlng.longitude);
        intent.putExtra("origin",mExtraOrigin);
        intent.putExtra("destination",mExtraDestination);
        intent.putExtra("destination_lat",mDestinationLatlng.latitude);
        intent.putExtra("destination_lng",mDestinationLatlng.longitude);
        startActivity(intent);
        finish();
    }

    private void drawRoute(){
        mGoogleApiProvider.getDirections(mOriginLatlng, mDestinationLatlng).enqueue(new Callback<String>() {
            @Override
            //Aquí estaremos viendo la respuesta del servidor
            public void onResponse(Call<String> call, Response<String> response) {
                try {

                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");
                    mmPolylineList = DecodePoints.decodePoly(points);
                    mPolylineOptions = new PolylineOptions();
                    mPolylineOptions.color(Color.DKGRAY);
                    mPolylineOptions.width(13f);
                    mPolylineOptions.startCap(new SquareCap());
                    mPolylineOptions.jointType(JointType.ROUND);
                    mPolylineOptions.addAll(mmPolylineList);
                    mMap.addPolyline(mPolylineOptions);

                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");

                    String distanceText = distance.getString("text");
                    String durationText = duration.getString("text");
                    mTextViewTime.setText(durationText);
                    mTextViewDistance.setText(distanceText);


                }catch (Exception e){
                    Log.d("Error", "Error encontrado" + e.getMessage());
                }

            }

            @Override
            //En caso de que falle la respuesta al servidor
            public void onFailure(Call<String> call, Throwable t) {

            }
        });


    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.addMarker(new MarkerOptions().position(mOriginLatlng).title("Origen").icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_pin_map_rojo_1)));
        mMap.addMarker(new MarkerOptions().position(mDestinationLatlng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_pin_map_verde_1)));

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                .target(mOriginLatlng)
                .zoom(14f)
                .build()
        ));

        drawRoute();
    }
}