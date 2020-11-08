package providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import models.Residentes;

public class ResiProvider {

    DatabaseReference mDatabase;

    public ResiProvider(){

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Residentes");

    }

    public Task<Void> create(Residentes residentes) {

        Map<String, Object> map = new HashMap<>();
        map.put("name",residentes.getName());
        map.put("email",residentes.getEmail());
        return mDatabase.child(residentes.getId()).setValue(map);

    }

}
