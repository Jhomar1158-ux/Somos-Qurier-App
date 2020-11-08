package activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.optic.curri.R;

import activities.Curi.MapCuriActivity;
import activities.Resi.MapResiActivity;

public class MainActivity extends AppCompatActivity {

    //Referenciamos los botones

    Button mButtonIamCuri;
    Button mButtonIamResi;


    SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPref = getApplicationContext().getSharedPreferences("typeUser",MODE_PRIVATE);
        final SharedPreferences.Editor editor = mPref.edit();


        mButtonIamCuri =findViewById(R.id.btnIamCuri);
        mButtonIamResi =findViewById(R.id.btnIamResi);

        //Añadiremos el botón del clic
//Para el botón Curi
        mButtonIamCuri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Le decimos que hará cuando presione el botón
                editor.putString("user","Curi");
                editor.apply();
                goToSelectAuth();
            }
        });
        mButtonIamResi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Le decimos que hará cuando presione el botón
                editor.putString("user","Resi");
                editor.apply();
                goToSelectAuth();
            }
        });
        }

    @Override
    protected void onStart() {
        super.onStart();

    if (FirebaseAuth.getInstance().getCurrentUser() != null)
    {
        String user = mPref.getString("user","");
        if(user.equals("Resi"))
        {
            Intent intent = new Intent(MainActivity.this, MapResiActivity.class);
            // Con esto el usuario ya no podrá regresar hacia atrás
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        else
        {
            Intent intent = new Intent(MainActivity.this, MapCuriActivity.class);
            // Con esto el usuario ya no podrá regresar hacia atrás
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

}

        private void goToSelectAuth() {
            Intent intent = new Intent(MainActivity.this, SelectOptionAuthActivity.class);
            startActivity(intent);
        }

}
