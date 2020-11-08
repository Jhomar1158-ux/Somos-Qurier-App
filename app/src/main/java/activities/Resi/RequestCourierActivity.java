package activities.Resi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.optic.curri.R;

import java.util.HashMap;
import java.util.Map;

import models.FCMBody;
import models.FCMResponse;
import providers.GeofireProvider;
import providers.NotificationProvider;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_courier);

        mAnimation1 = findViewById(R.id.animation_1);
        mTextViewLookingFor = findViewById(R.id.textViewLookingFor);
        mButtonCancelRequest = findViewById(R.id.btnCancelRequest);

        mAnimation1.playAnimation();

        mExtraOriginLat = getIntent().getDoubleExtra("origin_lat",0);
        mExtraOriginLng = getIntent().getDoubleExtra("origin_lng",0);

        mOriginLatLng = new LatLng(mExtraOriginLat,mExtraOriginLng);

        mGoefireProvider = new GeofireProvider();

        mNotificationProvider = new NotificationProvider();

        mTokenProvider = new TokenProvider();
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
                    sendNotification();
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

    private void sendNotification(){
        mTokenProvider.getToken(mIdCourierFound).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String token = dataSnapshot.child("token").getValue().toString();
                Map<String, String> map = new HashMap<>();
                map.put("title", "SOLICITUD DE SERVICIO");
                map.put("body", "Un residente está solicitando un servicio");
                FCMBody fcmBody = new FCMBody(token, "high", map);
                mNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if (response.body() != null){
                            if (response.body().getSuccess() == 1){
                                Toast.makeText(RequestCourierActivity.this, "La notiicación se ha enviado correctamente.", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(RequestCourierActivity.this, "No se pudo enviar la notificación.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {
                        Log.d("Error", "Error" +t.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}