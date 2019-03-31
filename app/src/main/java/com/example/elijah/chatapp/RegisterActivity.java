package com.example.elijah.chatapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.HashMap;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout DisplayName, Email, Password;
    private Button createB;
    private FirebaseAuth mAuth;

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


        mAuth = FirebaseAuth.getInstance();

        DisplayName = (TextInputLayout) findViewById(R.id.reg_displayName);
        Email = (TextInputLayout) findViewById(R.id.reg_email);
        Password = (TextInputLayout) findViewById(R.id.reg_password);
        createB = (Button) findViewById(R.id.reg_create);



        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance("EC","BC");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        ECGenParameterSpec ecsp;

        ecsp = new ECGenParameterSpec("secp192k1");
        try {
            kpg.initialize(ecsp);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        final KeyPair kpU = kpg.genKeyPair();
        PrivateKey privKeyU = kpU.getPrivate();
        PublicKey pubKeyU = kpU.getPublic();
        Log.e("User U: " , privKeyU.toString());
        Log.e("User U: " , pubKeyU.toString());
        System.out.println("User U: " + privKeyU.toString());
        System.out.println("User U: " + pubKeyU.toString());


        KeyPair kpV = kpg.genKeyPair();
        PrivateKey privKeyV = kpV.getPrivate();
        PublicKey pubKeyV = kpV.getPublic();
        Log.e("User v: " , privKeyV.toString());
        Log.e("User v: " , pubKeyV.toString());
        System.out.println("User V: " + privKeyV.toString());
        System.out.println("User V: " + pubKeyV.toString());

        KeyAgreement ecdhU = null;
        try {
            ecdhU = KeyAgreement.getInstance("ECDH");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            ecdhU.init(privKeyU);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        try {
            ecdhU.doPhase(pubKeyV,true);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        KeyAgreement ecdhV = null;
        try {
            ecdhV = KeyAgreement.getInstance("ECDH");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            ecdhV.init(privKeyV);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        try {
            ecdhV.doPhase(pubKeyU,true);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        final SecretKey uAesKey = new SecretKeySpec(ecdhU.generateSecret(),0,16,"AES");
        SecretKey vAesKey = new SecretKeySpec(ecdhV.generateSecret(),0,16,"AES");



        Log.e("AES U", uAesKey.getEncoded().toString());
        Log.e("AES V", vAesKey.getEncoded().toString());

        String plaintext = "Hello, its eli. will you please fucking encrypt already.";


        String encryptedText= DHcryptography.encrypt(plaintext,uAesKey);

        Log.e("Encrypted Text", encryptedText);

        String decryptedText = DHcryptography.decrypt(encryptedText,uAesKey);
        Log.e("Decrypted Text", decryptedText);

        String idk = "";



        createB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String display_name = DisplayName.getEditText().getText().toString();
                String email = Email.getEditText().getText().toString();
                String password = Password.getEditText().getText().toString();

                if(!TextUtils.isEmpty(display_name) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {

                    KeyPair keyPair = DHcryptography.generateKeypair();
                    PrivateKey privKey = keyPair.getPrivate();
                    PublicKey pubKey = keyPair.getPublic();
                    Log.e("User U: " , privKey.toString());
                    Log.e("User U: " , pubKey.toString());
                    System.out.println("User U: " + privKey.toString());
                    System.out.println("User U: " + pubKey.toString());

                    String filename = "PrivateKeyFile";
                    String fileContents = null;
                    try {
                        fileContents = Cryptography.savePrivateKey(kpU.getPrivate());
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    }
                    FileOutputStream outputStream;

                    try {
                        outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                        outputStream.write(fileContents.getBytes());
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    progressDialog.setTitle("Registering User");
                    progressDialog.setMessage("Please wait while we create your account");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    registerUser(display_name, email, password,kpU.getPublic());
                }
            }
        });
    }

    private void registerUser(final String display_name, String email, String password, final PublicKey publickey) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.e("Success?", String.valueOf(task.isSuccessful()));
                if(task.isSuccessful()){

                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = firebaseUser.getUid();
                    String publicKey = "";

                    try {
                        publicKey = Cryptography.savePublicKey(publickey);
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    }


                    database = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                    String device_token = FirebaseInstanceId.getInstance().getToken();



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
