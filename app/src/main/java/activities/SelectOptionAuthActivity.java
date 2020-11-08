package activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.optic.curri.R;

import activities.Curi.RegisterCuriActivity;
import activities.Resi.RegisterActivity;

public class SelectOptionAuthActivity extends AppCompatActivity {

    // definimos

    Toolbar mToolbar;
    Button mButtonGoToLogin;
    Button mButtonGoToRegister;

    SharedPreferences mPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_option_auth);
        //Referencia
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Seleccionar opción");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mButtonGoToLogin = findViewById(R.id.btnGoToLogin);
        mButtonGoToRegister = findViewById(R.id.btnGotoRegister);
        mButtonGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToLogin();
            }
        });
        mButtonGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToRegister();
            }
        });

        mPref = getApplicationContext().getSharedPreferences("typeUser",MODE_PRIVATE);

    }
    public void goToLogin()
    {
        Intent intent = new Intent(SelectOptionAuthActivity.this,LoginActivity.class);
        startActivity(intent);
    }
    public void goToRegister()
    {
        String typeUser = mPref.getString("user","");
        if (typeUser.equals("Resi"))
        {
            Intent intent = new Intent(SelectOptionAuthActivity.this, RegisterActivity.class);
            startActivity(intent);
        }
        else
        {
            Intent intent = new Intent(SelectOptionAuthActivity.this, RegisterCuriActivity.class);
            startActivity(intent);

        }

    }
}