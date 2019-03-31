package com.example.elijah.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;

    private CircleImageView mimage;
    private TextView mname, mstatus;
    private Button changeStatus, ChangePicture;

    private static  final int GALLERY_PICK = 1;

    private StorageReference storageReference;

    private ProgressDialog progressDialog;

    private Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        changeStatus = (Button)findViewById(R.id.changeprofile);
        ChangePicture =(Button)findViewById(R.id.changeimg);



        mimage = (CircleImageView)findViewById(R.id.profile_image);
        mname = (TextView)findViewById(R.id.Name);
        mstatus = (TextView)findViewById(R.id.profile);
        storageReference = FirebaseStorage.getInstance().getReference();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentID = firebaseUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentID);
        databaseReference.keepSynced(true);


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").toString();

                mname.setText(name);
                mstatus.setText(status);

                if(!image.equals("default")) {

                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.ic_launcher_background).into(mimage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.ic_launcher_background).into(mimage);

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        changeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent statusIntent = new Intent(SettingsActivity.this,StatusActivity.class);
                startActivity(statusIntent);

            }
        });

        ChangePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Toast.makeText(SettingsActivity.this, "you got it", Toast.LENGTH_SHORT).show();
            if (resultCode == RESULT_OK) {
                progressDialog = new ProgressDialog(SettingsActivity.this);
                progressDialog.setTitle("Uploading Image");
                progressDialog.setMessage("Please wait while image is being uploaded");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                Uri resultUri = result.getUri();

                final File thumb_filePath = new File(resultUri.getPath());




                String currentUserId = firebaseUser.getUid();

                try {
                     bitmap = new Compressor(this).setMaxWidth(200).setMaxHeight(200).setQuality(75).compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();


                StorageReference filepath = storageReference.child("profileImages").child(currentUserId+".jpg");
                final StorageReference thumb_filepath = storageReference.child("profileImages").child("thumbs").child(currentUserId+"jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            final String downloadURL = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    String  thumb_downloadurl = thumb_task.getResult().getDownloadUrl().toString();
                                    if(thumb_task.isSuccessful()){

                                        Map update_hashMap = new HashMap<>();
                                        update_hashMap.put("image", downloadURL);
                                        update_hashMap.put("thumb_image", thumb_downloadurl);
                                        databaseReference.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful()){
                                                    progressDialog.dismiss();
                                                    Toast.makeText(SettingsActivity.this,"Its uploaded", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });

                                    }else{
                                        Toast.makeText(SettingsActivity.this, "Thumb nail error", Toast.LENGTH_LONG).show();
                                        progressDialog.dismiss();
                                    }
                                }
                            });



                        }else{
                            Toast.makeText(SettingsActivity.this,"You aint got shit", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
