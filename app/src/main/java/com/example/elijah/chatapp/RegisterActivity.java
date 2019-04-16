package com.example.elijah.chatapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout DisplayName, Email, Password;
    private Button createB;
    private FirebaseAuth Auth;

    private Toolbar toolbar;
    private ProgressDialog progressDialog;
    private DatabaseReference database;
    private KeyPair keyPair;


    @SuppressLint({"WrongViewCast", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        toolbar = (Toolbar)findViewById(R.id.register_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);


        Auth = FirebaseAuth.getInstance();

        DisplayName = (TextInputLayout) findViewById(R.id.reg_displayName);
        Email = (TextInputLayout) findViewById(R.id.reg_email);
        Password = (TextInputLayout) findViewById(R.id.reg_password);
        createB = (Button) findViewById(R.id.reg_create);



        createB.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                String display_name = DisplayName.getEditText().getText().toString();
                String email = Email.getEditText().getText().toString();
                String password = Password.getEditText().getText().toString();

                if(!TextUtils.isEmpty(display_name) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {


                    //When the user signs in we generate the key pair using methods from the DHcryptography.java class.
                    KeyPair keyPair = DHcryptography.generateKeypair();
                    PrivateKey privKey = keyPair.getPrivate();
                    PublicKey pubKey = keyPair.getPublic();
                    //Print the private key and public key to error logs.
                    Log.e("Your private Key: " , privKey.toString());
                    Log.e("Your Public Key: " , pubKey.toString());


                    //Saving the private key.
                    String filename = "PrivateKeyFile";
                    String fileContents = null;
                    try {
                        fileContents = Cryptography.savePrivateKey(privKey);
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    }
                    FileOutputStream outputStream;

                    try {
                        //Saving private key to private file.
                        //https://stackoverflow.com/questions/41301511/how-to-create-hidden-directory-in-android
                        //Quote from stack overflow
                        // "You can save files directly on the device's internal storage. By default, files saved to the
                        // internal storage are private to your application and other applications cannot access them (nor can the user).
                        // When the user uninstalls your application, these files are removed."
                        outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                        outputStream.write(fileContents.getBytes());
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.e("Saving Private Key", "Private Key saved");


                    //Progress dialog.
                    progressDialog.setTitle("Registering User");
                    progressDialog.setMessage("Please wait while we create your account");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    //Pass in information including publickey to register.
                    registerUser(display_name, email, password,pubKey);
                }
            }
        });
    }

    //Method to register the user.
    private void registerUser(final String display_name, String email, String password, final PublicKey publickey) {
        //After passing in the information we connect to firebase and use firebase methods to authenticate our new user.
        Auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.e("Success?", String.valueOf(task.isSuccessful()));

                //If it is successful we get their user ID.
                if(task.isSuccessful()){

                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = firebaseUser.getUid();
                    String publicKey = "";

                    //Turn public key into a string of bytes using the DHcryptography.java class
                    try {
                        publicKey = DHcryptography.savePublicKey(publickey);
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    }


                    //Get the users device token.
                    database = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                    String device_token = FirebaseInstanceId.getInstance().getToken();



                    //figured out how to do this after watching this youtube video https://www.youtube.com/watch?v=iA-w8yFx0qw
                    HashMap<String, String> usermap = new HashMap<String, String>();
                    usermap.put("name",display_name);
                    usermap.put("status", "Hi there. this is a default status. Change it");
                    usermap.put("image", "default");
                    usermap.put("thumb_image", "default");
                    usermap.put("device_token", device_token);
                    usermap.put("PublicKey", publicKey);


                    database.setValue(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                progressDialog.dismiss();

                                Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                startActivity(mainIntent);
                                finish();

                            }else{
                                progressDialog.hide();
                                Toast.makeText(RegisterActivity.this,"Cannot sign in. Check the form and try again", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }else {
                    progressDialog.hide();
                    Toast.makeText(RegisterActivity.this,"Cannot sign in. Check the form and try again", Toast.LENGTH_LONG).show();
                }
            }

        });
    }

    private static void saveToFile(String fileName,
                                   BigInteger mod, BigInteger exp)
            throws Exception{
        ObjectOutputStream oout = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(fileName)));
        try {
            oout.writeObject(mod);
            oout.writeObject(exp);
        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            oout.close();
        }
    }
}
