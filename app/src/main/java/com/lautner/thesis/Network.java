package com.lautner.thesis;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

public class Network {

    private Context context;
    private static final Strategy STRATEGY = Strategy.P2P_CLUSTER;
    private String SERVICE_ID;
    private User user;
    private String leftEndpoint;
    private String rightEndpoint;
    private TextView newsFeed;
    private Button getRunner;
    private Button getService;
    private Button getSecurity;
    private Activity activity;

    Network(Context context, Activity activity, User user){
        this.context = context;
        this.user = user;
        this.SERVICE_ID = activity.getPackageName();
        this.leftEndpoint = "";
        this.rightEndpoint = "";
        this.activity = activity;
        this.newsFeed = this.activity.findViewById(R.id.newsFeed);
        this.getRunner = this.activity.findViewById(R.id.getRunner);
        this.getService = this.activity.findViewById(R.id.getService);
        this.getSecurity = this.activity.findViewById(R.id.getSecurity);
    }

    private void activateControls(){
        switch (user.getRole())
        {
            case BARTENDER:
                getRunner.setEnabled(true);
                getSecurity.setEnabled(true);
                break;
            case DJ_BOOTH:
                getSecurity.setEnabled(true);
                break;
            case RUNNER:
                break;
            case SECURITY:
                getService.setEnabled(true);
                break;
        }
    }

    void create(){
        Log.d("CREATE", "create network");
        startAdvertising();
    }

    void join(){
        Log.d("JOIN", "join network");
        startDiscovery();
    }

    void log(String message){
        this.newsFeed.append(message + "\n");
    }

    void sendMessage(Message message) {
        if(!leftEndpoint.isEmpty()) {
            Log.d("SendToLeftEndpoint", leftEndpoint);
            sendPayload(leftEndpoint, message);
        }

        if(!rightEndpoint.isEmpty()){
            Log.d("SendToRightEndpointt", rightEndpoint);
            sendPayload(rightEndpoint, message);
        }
    }

    private void sendPayload(String endpointId, Message message){

        byte[] messageAsBytes = ObjectConverter.serialize(message);

        Nearby.getConnectionsClient(context).sendPayload(
                endpointId, Payload.fromBytes(messageAsBytes));
    }

    private void forwardPayload(String endpointId, Payload payload){

        if(endpointId.equals(leftEndpoint) && !rightEndpoint.isEmpty()){
            Nearby.getConnectionsClient(context).sendPayload(rightEndpoint, payload);
        }

        if(endpointId.equals(rightEndpoint) && !leftEndpoint.isEmpty()){
            Nearby.getConnectionsClient(context).sendPayload(leftEndpoint, payload);
        }
    }

    private void receive(byte[] payloadAsBytes){
        //String message = new String(payload, UTF_8);
        Message message = (Message)ObjectConverter.deserialize(payloadAsBytes);

        if (message == null || !message.getToRole().equals(user.getRole())){
            return;
        }

        Log.d("payload received", message.toString());
        newsFeed.append(DateFormat.format("hh:mm", System.currentTimeMillis()) + ": ");
        newsFeed.append(message.getFromName() + ": " + message.getMessage());
        newsFeed.append("\n");
    }

    void disconnect(){
        Nearby.getConnectionsClient(context).stopAllEndpoints();
    }

    private void startAdvertising() {
        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();
        log("start advertising");
        Nearby.getConnectionsClient(context)
                .startAdvertising(
                        user.getName(), SERVICE_ID, advertiseCallback, advertisingOptions)
                .addOnSuccessListener(
                        (Void unused) -> {
                            // We're advertising!
                            Log.d("Advertising : ", user.getName());
                        })
                .addOnFailureListener(
                        (Exception e) -> {
                            Log.d("Advertising Exception", e.toString());
                            // We were unable to start advertising.
                        });
    }

    private void startDiscovery() {
        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();
        log("start discover");
        Nearby.getConnectionsClient(context)
                .startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener(
                        (Void unused) -> {
                            // We're discovering!
                            Log.d("DISCOVERING", user.getName());
                        })
                .addOnFailureListener(
                        (Exception e) -> {
                            // We're unable to start discovering.
                        });
    }

    private final ConnectionLifecycleCallback advertiseCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    //logD("Connection request from " + connectionInfo.getEndpointName());
                    // Automatically accept the connection on both sides.
                    Nearby.getConnectionsClient(context)
                            .acceptConnection(endpointId, payloadCallback)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("ADVERTISE CNC CALLBACK", endpointId);
                                stopAdvertising();
                            })
                            .addOnFailureListener(e -> {
                            });
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            // We're connected! Can now start sending and receiving data.
                            Log.d("SAFE LEFT ENDPOINT", endpointId);
                            log("connected, safe left");
                            leftEndpoint = endpointId;
                            activateControls();
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            // The connection was rejected by one or both sides.
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            // The connection broke before it was able to be accepted.
                            break;
                        default:
                            // Unknown status code
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    // We've been disconnected from this endpoint. No more data can be
                    // sent or received.
                    Log.d("onDisconnectCalled", endpointId);
                    log("advertise DISCONNECT");
                    if(endpointId.equals(leftEndpoint)){
                        leftEndpoint = "";
                        startAdvertising();
                    }

                    if(endpointId.equals(rightEndpoint)){
                        rightEndpoint = "";
                        startDiscovery();
                    }
                }
            };

    private final ConnectionLifecycleCallback discoverCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
                    // Automatically accept the connection on both sides.
                    Nearby.getConnectionsClient(context).acceptConnection(endpointId, payloadCallback);
                    Log.d("DISCOVER callback", endpointId);
                }

                @Override
                public void onConnectionResult(@NonNull String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            Nearby.getConnectionsClient(context).stopDiscovery();
                            // We're connected! Can now start sending and receiving data.
                            Log.d("SAFE RIGHT ENDPOINT", endpointId);
                            log("connected, safe right");
                            rightEndpoint = endpointId;
                            activateControls();
                            stopDiscovering();
                            startAdvertising();
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            // The connection was rejected by one or both sides.
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            // The connection broke before it was able to be accepted.
                            break;
                        default:
                            // Unknown status code
                    }
                }

                @Override
                public void onDisconnected(@NonNull String endpointId) {
                    // We've been disconnected from this endpoint. No more data can be
                    // sent or received.
                    Log.d("onDisconnectCalled", endpointId);
                    log("discover DISCONNECTED");
                    if(endpointId.equals(leftEndpoint)){
                        leftEndpoint = "";
                        startAdvertising();
                    }

                    if(endpointId.equals(rightEndpoint)){
                        rightEndpoint = "";
                        startDiscovery();
                    }
                }
            };

    private EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo info) {
                    // An endpoint was found. We request a connection to it.
                    Nearby.getConnectionsClient(context)
                            .requestConnection(user.getName(), endpointId, discoverCallback)
                            .addOnSuccessListener(
                                    (Void unused) -> {
                                        Log.d("ENDPOINT FOUND", "Request connection");
                                        // We successfully requested a connection. Now both sides
                                        // must accept before the connection is established.
                                    })
                            .addOnFailureListener(
                                    (Exception e) -> {
                                        Log.e("DiscoveryCallbackFailed", e.toString());
                                        // Nearby Connections failed to request the connection.
                                    });
                }

                @Override
                public void onEndpointLost(@NonNull String endpointId) {
                    // A previously discovered endpoint has gone away.
                    Log.d("ENDPOINT LOST", endpointId);
                    log("discovered endpoint lost");
                    //
                }
            };

    private void stopAdvertising() { Nearby.getConnectionsClient(context).stopAdvertising();
    }

    private void stopDiscovering() {
        Nearby.getConnectionsClient(context).stopDiscovery();
    }

    private PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    // This always gets the full data of the payload. Will be null if it's not a BYTES
                    // payload. You can check the payload type with payload.getType().
                    byte[] receivedBytes = payload.asBytes();
                    Log.d("payloadAsBytes", receivedBytes.toString());
                    receive(receivedBytes);
                    forwardPayload(endpointId, payload);
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    // Bytes payloads are sent as a single chunk, so you'll receive a SUCCESS update immediately
                    // after the call to onPayloadReceived().
                }
            };
}
