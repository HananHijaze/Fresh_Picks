package com.example.fresh_picks;

import com.example.fresh_picks.APIS.MongoDBHelper;
import android.app.Application;

public class FreshPicksApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize MongoDB when the app starts
        MongoDBHelper.init();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // Close MongoDB when the app is terminated
        MongoDBHelper.close();
    }
}
