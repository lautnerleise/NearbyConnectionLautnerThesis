package com.lautner.thesis;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private TextView newsFeed;
    private User user;
    private String networkCreationType;
    private Button getRunner;


    private static final String TAG = "MainActivity";
    private Context context;
    private static final String[] REQUIRED_PERMISSIONS =
            new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            };
    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 5432;

    Network network;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        newsFeed = findViewById(R.id.newsFeed);
        newsFeed.setMovementMethod(new ScrollingMovementMethod());
        getRunner = findViewById(R.id.getRunner);

        this.user = (User)getIntent().getSerializableExtra("User");
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            networkCreationType = extras.getString("NETWORK_CREATION_TYPE");
            Log.d("userName", user.getName());
            Log.d("role", user.getRole().toString());
            Log.d("networkCreationType", networkCreationType);
        }

        this.network = new Network(context, this, this.user);

        if(networkCreationType.equals("create"))
            network.create();
        else if (networkCreationType.equals("join"))
            network.join();
    }

    public void getRunner(View view){
        Message message = new Message("Runner benötigt", user.getName(), "", Role.RUNNER);
        network.sendMessage(message);
        Log.d("sendText()", "text wird gesendet");
    }

    public void getSecurity(View view){
        Message message = new Message("Security benötigt", user.getName(), "", Role.SECURITY);
        network.sendMessage(message);
        Log.d("sendText()", "text wird gesendet");
    }

    public void getService(View view){
        Message message = new Message("Service benötigt", user.getName(), "", Role.BARTENDER);
        network.sendMessage(message);
        Log.d("sendText()", "text wird gesendet");
    }

    public void disconnect(View view){
        network.disconnect();
    }

    /**
     * Called when the user has accepted (or denied) our permission request.
     */
    @CallSuper
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_REQUIRED_PERMISSIONS) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, R.string.error_missing_permissions, Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }
            recreate();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
            }
        }
    }
    @Override
    protected void onStop() {
        //Nearby.getConnectionsClient(context).stopAdvertising();
        //Nearby.getConnectionsClient(context).stopAllEndpoints();

        super.onStop();
    }

    protected void logV(String msg) {
        Log.v(TAG,msg);
        appendToLogs(toColor(msg, getResources().getColor(R.color.log_verbose)));
    }

    protected void logD(String msg) {
        Log.d(TAG,msg);
        appendToLogs(toColor(msg, getResources().getColor(R.color.log_debug)));
    }

    protected void logW(String msg) {
        Log.w(TAG,msg);
        appendToLogs(toColor(msg, getResources().getColor(R.color.log_warning)));
    }

    protected void logW(String msg, Throwable e) {
        Log.w(TAG,msg, e);
        appendToLogs(toColor(msg, getResources().getColor(R.color.log_warning)));
    }

    protected void logE(String msg, Throwable e) {
        Log.e(TAG,msg, e);
        appendToLogs(toColor(msg, getResources().getColor(R.color.log_error)));
    }

    private void appendToLogs(CharSequence msg) {

    }

    private static CharSequence toColor(String msg, int color) {
        SpannableString spannable = new SpannableString(msg);
        spannable.setSpan(new ForegroundColorSpan(color), 0, msg.length(), 0);
        return spannable;
    }
}
