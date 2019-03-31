package com.example.elijah.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class Login_Activity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputLayout email, password;
    private Button signinbtn;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_);
        toolbar = (Toolbar)findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        email = (TextInputLayout)findViewById(R.id.sign_email);
        password = (TextInputLayout)findViewById(R.id.sign_password);
        signinbtn = (Button)findViewById(R.id.sign_btn);

        signinbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Email = email.getEditText().getText().toString();
                String Password = password.getEditText().getText().toString();

                if(!TextUtils.isEmpty(Email) || !TextUtils.isEmpty(Password)){
                    progressDialog.setTitle("Logging you in");
                    progressDialog.setMessage("Please wait while we sign you in");
                    progressDialog.dismiss();
                    loginUser(Email, Password);
                }
            }
        });

    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();

                    String current_uid = mAuth.getCurrentUser().getUid();



                    String deviceToken = FirebaseInstanceId.getInstance().getToken();


                    databaseReference.child(current_uid).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent mainIntent = new Intent(Login_Activity.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();
                        }
                    });

                }else{
                    progressDialog.hide();
                    Toast.makeText(Login_Activity.this,"Cannot sign in", Toast.LENGTH_LONG).show();
                }

            }
        });

    }
}
