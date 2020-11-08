package activities.Resi;

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
import models.Residentes;
import providers.AuthProvider;
import providers.ResiProvider;

public class RegisterActivity extends AppCompatActivity {

    //VISTAS
AuthProvider mAuthProvider;
ResiProvider mResiProvider;

    Button mButtonRegister;
    TextInputEditText mTextInputName;
    TextInputEditText mTextInputEmail;
    TextInputEditText mTextInputPassword;


    AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        MyToolbar.show(this,"Registro de Residente", true);

        mAuthProvider = new AuthProvider();
        mResiProvider = new ResiProvider();


        mDialog = new SpotsDialog.Builder().setContext(RegisterActivity.this).setMessage("Espere un momento.").build();


        mButtonRegister = findViewById(R.id.btnRegister);
        mTextInputName = findViewById(R.id.textInputName);
        mTextInputEmail = findViewById(R.id.textInputEmail);
        mTextInputPassword = findViewById(R.id.textInputPassword);





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


        if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
            if (password.length() >= 6) {
                mDialog.show();
               register(name, email, password);
            }
            else {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show();
            }
        }
        else {


            Toast.makeText(this, "Ingrese todos los campos.", Toast.LENGTH_SHORT).show();
        }
    }

    void register(final String name, final String email, String password)
    {
        mAuthProvider.register(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mDialog.hide();
                if (task.isSuccessful()) {
                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Residentes residentes = new Residentes(id, name, email);
                     create(residentes);
                }
                else {
                    Toast.makeText(RegisterActivity.this, "No se pudo registrar el usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    void create(Residentes residentes){
        mResiProvider.create(residentes).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                   // Toast.makeText(RegisterActivity.this, "El registro se realizó con exito.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, MapResiActivity.class);
                    // Con esto el usuario ya no podrá regresar hacia atrás
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                }else
                {
                    Toast.makeText(RegisterActivity.this, "No se pudo registrar el usuario.", Toast.LENGTH_SHORT).show();
                    
                }
            }
        });
    }
    /*
    void saveUser(String id, String name, String email) {
        String selectedUser = mPref.getString("user", "");
        User user = new User();
        user.setEmail(email);
        user.setName(name);

        if (selectedUser.equals("Curi")) {
            mDatabase.child("Users").child("Couriers").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registro exitoso.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(RegisterActivity.this, "Fallo el registro.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else if (selectedUser.equals("Resi")){
            mDatabase.child("Users").child("Residentes").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(RegisterActivity.this, "Fallo el registro", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
*/

}
