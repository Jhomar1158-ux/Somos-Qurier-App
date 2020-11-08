package providers;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GeofireProvider {

    private DatabaseReference mDatabase;
    private GeoFire mGeofire;


    public GeofireProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("active_couriers");
        mGeofire = new GeoFire(mDatabase);
    }

    public void saveLocation(String idCourier, LatLng latLng){

        mGeofire.setLocation(idCourier,new GeoLocation(latLng.latitude,latLng.longitude));
    }

    public void removeLocation(String idCourier){
        mGeofire.removeLocation(idCourier);
    }
    public GeoQuery getActiveCouriers (LatLng latLng, double radius){

        GeoQuery geoQuery = mGeofire.queryAtLocation(new GeoLocation(latLng.latitude,latLng.longitude), radius);
        geoQuery.removeAllListeners();
        return geoQuery;
    }
}
