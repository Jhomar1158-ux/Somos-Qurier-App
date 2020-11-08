package providers;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.optic.curri.R;

import activities.Retrofit.IGoogleApi;
import activities.Retrofit.RetrofitResi;
import retrofit2.Call;

public class GoogleApiProvider {

    private Context context;

    public GoogleApiProvider(Context context){
            this.context = context;
    }

    public Call<String> getDirections(LatLng originLatLng, LatLng destinationLatLng){
        String baseurl = "https://maps.googleapis.com";
        String query = "/maps/api/directions/json?mode=driving&transit_routing_preferences=less_driving&"
                + "origin=" + originLatLng.latitude + "," + originLatLng.longitude + "&"
                + "destination=" + destinationLatLng.latitude + "," + destinationLatLng.longitude + "&"
                + "key=" + context.getResources().getString(R.string.google_maps_key);
        return RetrofitResi.getResi(baseurl).create(IGoogleApi.class).getDirections(baseurl+query);
    }
}
