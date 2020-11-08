package providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import models.Couriers;

public class CuriProvider {

    DatabaseReference mDatabase;

    public CuriProvider(){

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Couriers");

    }

    public Task<Void> create(Couriers couriers) {

        return mDatabase.child(couriers.getId()).setValue(couriers);

    }

}
