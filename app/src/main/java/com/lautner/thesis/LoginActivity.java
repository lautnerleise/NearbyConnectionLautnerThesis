package com.lautner.thesis;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class LoginActivity extends AppCompatActivity {

    EditText name;
    Spinner role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        name = findViewById(R.id.userName);
        role = findViewById(R.id.role);
        role.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Role.values()));
    }

    public void next(View view){
        User user = new User(name.getText().toString(), (Role)role.getSelectedItem());

        Intent intent = new Intent(this, NetworkActivity.class);
        intent.putExtra("User", user);
        //intent.putExtra("ROLE", role.getSelectedItem().toString());
        startActivity(intent);
    }
}
