package com.xahid.Motivational_Quotes.app;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        FirebaseMessaging.getInstance().subscribeToTopic("firewallappnotification");
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

    }
}
