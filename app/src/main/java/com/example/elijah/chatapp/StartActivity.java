package com.example.elijah.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    private Button regBtn, signbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        regBtn = (Button)findViewById(R.id.reg_button);
        signbtn = (Button)findViewById(R.id.HaveAccountBtn);

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent reg_intent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(reg_intent);
                finish();
            }
        });
        signbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signIntent = new Intent(StartActivity.this, Login_Activity.class);
                startActivity(signIntent);
                finish();

            }
        });




    }
}
