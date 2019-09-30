package com.lautner.thesis;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class NetworkActivity extends AppCompatActivity {

    User user;
    String userName;
    Role role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);
        this.user = (User)getIntent().getSerializableExtra("User");
    }

    public void createNetwork(View view){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("User", this.user);
        intent.putExtra("NETWORK_CREATION_TYPE", "create");
        startActivity(intent);
    }

    public void joinNetwork(View view){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("User", this.user);
        intent.putExtra("NETWORK_CREATION_TYPE", "join");
        startActivity(intent);
    }
}
