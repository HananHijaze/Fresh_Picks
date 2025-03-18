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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;

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

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        // ✅ Initialize UI elements
        loadingIndicator = findViewById(R.id.loading_indicator);
        appLogo = findViewById(R.id.imageView); // ✅ Match the correct ImageView ID

        // ✅ Hide ProgressBar unless language is being changed
        if (!isLanguageChange) {
            loadingIndicator.setVisibility(View.GONE);
        }

        // ✅ Initialize database manager and start listening for super sales
        databaseManager = new DatabaseManager();
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
            databaseManager.stopListeners(); // ✅ Stop Firestore listener when activity is destroyed
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


}
