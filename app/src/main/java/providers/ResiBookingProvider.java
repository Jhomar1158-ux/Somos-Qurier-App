package providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import models.ResiBooking;

public class ResiBookingProvider {

    private DatabaseReference mDatabase;

    public ResiBookingProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("ResiBooking");
    }

    public Task<Void> create(ResiBooking resiBooking){
        return  mDatabase.child(resiBooking.getIdResi()).setValue(resiBooking);
    }

    public Task<Void> updateStatus(String idResiBooking, String status){
        Map<String, Object> map = new HashMap<>();
        map.put("status",status);
        return mDatabase.child(idResiBooking).updateChildren(map);
    }
}
