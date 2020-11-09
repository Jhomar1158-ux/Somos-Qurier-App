package activities.Resi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.optic.curri.R;
import com.optic.curri.utils.DecodePoints;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import models.FCMBody;
import models.FCMResponse;
import models.ResiBooking;
import providers.AuthProvider;
import providers.GeofireProvider;
import providers.GoogleApiProvider;
import providers.NotificationProvider;
import providers.ResiBookingProvider;
import providers.TokenProvider;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestCourierActivity extends AppCompatActivity {

    private LottieAnimationView mAnimation1;
    private TextView mTextViewLookingFor;
    private Button mButtonCancelRequest;
    private GeofireProvider mGoefireProvider;
    private double mExtraOriginLat;
    private double mExtraOriginLng;

    private LatLng mOriginLatLng;

    private double mRadius = 0.1;

    private boolean mCourierFound = false;
    private String mIdCourierFound = "";
    private LatLng mCourierFoundLatLng;
    private NotificationProvider mNotificationProvider;

    private TokenProvider mTokenProvider;


    private ResiBookingProvider mResiBookingProvider;
    private AuthProvider mAuthProvider;

    private String mExtraOrigin;
    private String mExtraDestination;
    private double mExtraDestinationLat;
    private double mExtraDestinationLng;

    private LatLng mDestinationLatLng;
    private GoogleApiProvider mGoogleApiProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_courier);

        mAnimation1 = findViewById(R.id.animation_1);
        mTextViewLookingFor = findViewById(R.id.textViewLookingFor);
        mButtonCancelRequest = findViewById(R.id.btnCancelRequest);

        mAnimation1.playAnimation();

        mExtraOrigin = getIntent().getStringExtra("origin");
        mExtraDestination = getIntent().getStringExtra("destination");
        mExtraDestinationLat = getIntent().getDoubleExtra("destination_lat",0);
        mExtraDestinationLng = getIntent().getDoubleExtra("destination_lng",0);
        mExtraOriginLat = getIntent().getDoubleExtra("origin_lat",0);
        mExtraOriginLng = getIntent().getDoubleExtra("origin_lng",0);

        mOriginLatLng = new LatLng(mExtraOriginLat,mExtraOriginLng);
        mDestinationLatLng = new LatLng(mExtraDestinationLat,mExtraDestinationLat);


        mGoefireProvider = new GeofireProvider();

        mNotificationProvider = new NotificationProvider();

        mTokenProvider = new TokenProvider();

        mResiBookingProvider = new ResiBookingProvider();
        mGoogleApiProvider = new GoogleApiProvider(RequestCourierActivity.this);

        mAuthProvider = new AuthProvider();
        getClosestCourier();
    }

    private void getClosestCourier() {
        mGoefireProvider.getActiveCouriers(mOriginLatLng, mRadius).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //Se ejecuta cuando encuentra a un conducto nos devuelve la llave del usuario o ID
                if(!mCourierFound){
                    mCourierFound = true;
                    mIdCourierFound = key;
                    mCourierFoundLatLng = new LatLng(location.latitude,location.longitude);
                    mTextViewLookingFor.setText("QURIER ENCONTRADO\nESPERANDO RESPUESTA");
                    createResiBooking();
                    Log.d("COURIER" , "ID: " +mIdCourierFound);
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                //Ingresa cuando termina la búsqueda del conducto en un radio de 0.1km
                if (!mCourierFound){
                    mRadius = mRadius + 0.1f; //Lo incrementamos en caso no haya encontrado a uno

                    // No encontró un conductor
                    if (mRadius > 5) {
                        mTextViewLookingFor.setText("NO SE ENCONTRÓ UN QURIER");
                        Toast.makeText(RequestCourierActivity.this, "NO SE ENCONTRÓ UN QURIER", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else {
                        getClosestCourier();
                    }

                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
    private void createResiBooking(){

        mGoogleApiProvider.getDirections(mOriginLatLng, mCourierFoundLatLng).enqueue(new Callback<String>() {
            @Override
            //Aquí estaremos viendo la respuesta del servidor
            public void onResponse(Call<String> call, Response<String> response) {
                try {

                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");
                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanceText = distance.getString("text");
                    String durationText = duration.getString("text");

                    sendNotification(durationText, distanceText);

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

    private void sendNotification(final String time, final String km){
        mTokenProvider.getToken(mIdCourierFound).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String token = dataSnapshot.child("token").getValue().toString();
                    Map<String, String> map = new HashMap<>();
                    map.put("title", "Solicitud de servicio a "+ time + " de tu posición");
                    map.put("body",
                                    "Un residente está solicitando un servicio a una distancia de "+ km + " \n" +
                                    "Origen: " +mExtraOrigin + "\n" +
                                    "Destino: " +mExtraDestination
                    );
                    map.put("idResi",mAuthProvider.getId());
                    FCMBody fcmBody = new FCMBody(token, "high", map);
                    mNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body() != null){
                                if (response.body().getSuccess() == 1){
                                    ResiBooking resiBooking = new ResiBooking(
                                            mAuthProvider.getId(),
                                            mIdCourierFound,
                                            mExtraDestination,
                                            mExtraOrigin,
                                            time,
                                            km,
                                            "create",
                                            mExtraOriginLat,
                                            mExtraOriginLng,
                                            mExtraDestinationLat,
                                            mExtraDestinationLng
                                    );
                                    mResiBookingProvider.create(resiBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(RequestCourierActivity.this, "La petición se ejecutó correctamente.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                   // Toast.makeText(RequestCourierActivity.this, "La notiicación se ha enviado correctamente.", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(RequestCourierActivity.this, "No se pudo enviar la notificación.", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                Toast.makeText(RequestCourierActivity.this, "No se pudo enviar la notificación.", Toast.LENGTH_SHORT).show();

                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("Error", "Error" +t.getMessage());
                        }
                    });
                }
                else {
                    Toast.makeText(RequestCourierActivity.this, "No se pudo enviar la notificación porque el conductor no tiene un token de sesión.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}