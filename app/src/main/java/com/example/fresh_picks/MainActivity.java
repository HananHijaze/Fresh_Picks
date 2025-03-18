package com.example.fresh_picks;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.content.Context;

import com.example.fresh_picks.DAO.AppDatabase;
import com.example.fresh_picks.DAO.UserDao;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private UserDao userDao;
    private DatabaseManager databaseManager;
    private ProgressBar loadingIndicator; // ✅ ProgressBar for language change
    private ImageView appLogo; // ✅ ImageView to be hidden when changing language
    private boolean isLanguageChange = false; // ✅ Track if language is being changed

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // ✅ Load language settings before setting content view
        LanguageManager languageManager = new LanguageManager(this);
        setAppLocale(languageManager.getLanguage());
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        // ✅ Initialize UI elements
        loadingIndicator = findViewById(R.id.loading_indicator);
        appLogo = findViewById(R.id.imageView); // ✅ Match the correct ImageView ID

        // ✅ Hide ProgressBar unless language is being changed
        if (!isLanguageChange) {
            loadingIndicator.setVisibility(View.GONE);
        }
        createNotificationChannel();

        // ✅ Initialize database manager and start listening for super sales
        databaseManager = new DatabaseManager(this);
        databaseManager.startSuperSaleListener();

        // ✅ Adjust padding for system bars dynamically
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ✅ Initialize Room database
        AppDatabase db = AppDatabase.getInstance(this);
        userDao = db.userDao();

        // ✅ Run database check asynchronously
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            boolean hasUsers = !userDao.getAllUsers().isEmpty(); // Fetch users from Room
            Log.d(TAG, "Has users in DB: " + hasUsers);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                loadingIndicator.setVisibility(View.GONE); // ✅ Hide loading indicator

                Intent intent = new Intent(MainActivity.this, hasUsers ? MainActivity2.class : LogIn.class);
                startActivity(intent);
                finish();
            }, 1000); // ✅ Reduced delay to 1 second for faster transition
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseManager != null) {
            databaseManager.stopListeners();
        }
    }


    private void setAppLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Configuration config = getBaseContext().getResources().getConfiguration();
        config.setLocale(locale);
        config.setLayoutDirection(locale);

        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        // ✅ Save the language setting
        LanguageManager languageManager = new LanguageManager(this);
        languageManager.setLanguage(langCode);
    }


    public void changeLanguage(String langCode) {
        isLanguageChange = true;
        loadingIndicator.setVisibility(View.VISIBLE);
        appLogo.setVisibility(View.GONE);

        setAppLocale(langCode);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            finish();
            startActivity(intent);
        }, 500); // Smooth transition
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Fresh Picks Notifications";
            String description = "Alerts for new sales and order updates";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("fresh_picks_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    public void showNotification(String title, String message) {
        // Check if notification permission is granted before proceeding
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "❌ Notification permission not granted. Cannot show notification.");
                return; // Stop execution if permission is not granted
            }
        }

        Intent intent = new Intent(this, MainActivity2.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "fresh_picks_channel")
                .setSmallIcon(R.drawable.logo) // Change to your actual notification icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        try {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "❌ SecurityException: Missing POST_NOTIFICATIONS permission.", e);
        }
    }

}
