package com.example.eascanfinal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainScreen extends AppCompatActivity {

    private TextView usernameTV;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        // Initialize UI components
        usernameTV = findViewById(R.id.usernameTV);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Get the username passed from the login activity
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        usernameTV.setText("Hi! @" + username); // Set the username in the TextView

        // Load the default fragment with username
        loadFragment(HomePage.newInstance(username));

        // Set listener for bottom navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = HomePage.newInstance(username); // Home Fragment with username
            } else if (item.getItemId() == R.id.nav_qr) {
                selectedFragment = QRPage.newInstance(username); // QR Fragment with username
            } else if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = AccountPage.newInstance(username); // Profile Fragment with username
            } else if (item.getItemId() == R.id.nav_wallet) {
                selectedFragment = WalletPage.newInstance(username); // Wallet Fragment with username
            }
            return loadFragment(selectedFragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            return true;
        }
        return false;
    }
}
