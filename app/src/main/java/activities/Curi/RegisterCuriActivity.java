package activities.Curi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.optic.curri.R;

import dmax.dialog.SpotsDialog;
import includes.MyToolbar;
import models.Couriers;
import providers.AuthProvider;
import providers.CuriProvider;

public class RegisterCuriActivity extends AppCompatActivity {

    //VISTAS
    AuthProvider mAuthProvider;
    CuriProvider mCuriProvider;

    Button mButtonRegister;
    TextInputEditText mTextInputName;
    TextInputEditText mTextInputEmail;
    TextInputEditText mTextInputPassword;
    TextInputEditText mTextInputEdad;
    TextInputEditText mTextInputGenero;
    TextInputEditText mTextInputNumero;


    AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_curi);

        MyToolbar.show(this,"Registro del courier", true);

        mAuthProvider = new AuthProvider();
        mCuriProvider = new CuriProvider();


        mDialog = new SpotsDialog.Builder().setContext(RegisterCuriActivity.this).setMessage("Espere un momento.").build();


        mButtonRegister = findViewById(R.id.btnRegister);
        mTextInputName = findViewById(R.id.textInputName);
        mTextInputEmail = findViewById(R.id.textInputEmail);
        mTextInputPassword = findViewById(R.id.textInputPassword);
        mTextInputEdad = findViewById(R.id.textInputEdad);
        mTextInputGenero = findViewById(R.id.textInputGenero);
        mTextInputNumero = findViewById(R.id.textInputNumero);





        mButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickRegister();
            }
        });
    }
    void clickRegister()
    {
        final String name = mTextInputName.getText().toString();
        final String email = mTextInputEmail.getText().toString();
        final String password = mTextInputPassword.getText().toString();
        final String edad = mTextInputEdad.getText().toString();
        final String genero = mTextInputGenero.getText().toString();
        final String numero = mTextInputNumero.getText().toString();


        if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty() && !edad.isEmpty() && !genero.isEmpty() && !numero.isEmpty()) {
            if (password.length() >= 6) {
                mDialog.show();
                register(name, email, password, edad, genero, numero);
            }
            else {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show();
            }
        }
        else {


            Toast.makeText(this, "Ingrese todos los campos.", Toast.LENGTH_SHORT).show();
        }
    }

    void register(final String name, final String email, String password, final String edad, final String genero, final String numero)
    {
        mAuthProvider.register(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mDialog.hide();
                if (task.isSuccessful()) {
                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Couriers couriers = new Couriers(id, name, email, edad, numero, genero );
                    create(couriers);
                }
                else {
                    Toast.makeText(RegisterCuriActivity.this, "No se pudo registrar el usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    void create(Couriers couriers){
        mCuriProvider.create(couriers).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    //Toast.makeText(RegisterCuriActivity.this, "El registro se realizó con exito.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterCuriActivity.this,MapCuriActivity.class);
                    // Con esto el usuario ya no podrá regresar hacia atrás
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }else
                {
                    Toast.makeText(RegisterCuriActivity.this, "No se pudo registrar el usuario.", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
}