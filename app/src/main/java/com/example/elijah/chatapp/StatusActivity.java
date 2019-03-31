package com.example.elijah.chatapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputLayout textInputLayout;
    private Button button;

    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentID = currentUser.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentID);




        toolbar = (Toolbar)findViewById(R.id.StatusBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);

        textInputLayout = (TextInputLayout)findViewById(R.id.StatusText);
        button = (Button)findViewById(R.id.StatusChangeBtn);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                progressDialog.setTitle("Saving Changes");
                progressDialog.setMessage("Please wait while we save changed");
                progressDialog.show();
                String status = textInputLayout.getEditText().getText().toString();

                databaseReference.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            progressDialog.dismiss();
                            Toast.makeText(StatusActivity.this, "Status update complete", Toast.LENGTH_LONG).show();
                        }else{
                            progressDialog.hide();
                            Toast.makeText(StatusActivity.this, "Status update failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });




            }
        });

    }
}
