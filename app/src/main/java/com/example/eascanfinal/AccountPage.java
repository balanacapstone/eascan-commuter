package com.example.eascanfinal;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class AccountPage extends Fragment {

    private static final String ARG_USERNAME = "username";
    private String username;
    Button logoutButton, historyButton;
    private TextView fullNameTextView, usernameTextView;
    private ImageView profileImageView;

    public static AccountPage newInstance(String username) {
        AccountPage fragment = new AccountPage();
        Bundle args = new Bundle();
        args.putString(ARG_USERNAME, username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            username = getArguments().getString(ARG_USERNAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account_page, container, false);

        // Initialize UI elements
        fullNameTextView = view.findViewById(R.id.fullNameAcc);
        usernameTextView = view.findViewById(R.id.usernameAcc);
        profileImageView = view.findViewById(R.id.imageView);
        logoutButton = view.findViewById(R.id.logoutButton);
        historyButton = view.findViewById(R.id.historyButton);
        historyButton.setOnClickListener(v -> {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, TransactionHistory.newInstance(username));
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        logoutButton.setOnClickListener(v -> {
            // Perform logout or navigate to another activity
            Intent intent = new Intent(getActivity(), Login.class);
            startActivity(intent);
            getActivity().finish(); // Close current activity
        });
        // Fetch user details
        fetchUserDetails(username);

        return view;
    }

    private void fetchUserDetails(String username) {
        new Thread(() -> {
            try {
                // Create URL
                URL url = new URL("http://192.168.254.102:8080/eascan/get_user_details.php");

                // Create HttpURLConnection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Send POST data
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write("username=" + username);
                writer.flush();
                writer.close();

                // Get response
                InputStream inputStream = connection.getInputStream();
                Scanner scanner = new Scanner(inputStream);
                StringBuilder response = new StringBuilder();
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
                scanner.close();

                // Parse JSON and update UI
                getActivity().runOnUiThread(() -> parseUserDetails(response.toString()));

            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Failed to fetch user details", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void parseUserDetails(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            String fullName = jsonObject.getString("fullname");
            String username = jsonObject.getString("username");
            String profilePicBase64 = jsonObject.getString("profilepic");

            // Update UI
            fullNameTextView.setText(fullName);
            usernameTextView.setText("@" + username);

            // Decode the Base64 profile picture and display it
            byte[] decodedString = Base64.decode(profilePicBase64.split(",")[1], Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            profileImageView.setImageBitmap(decodedByte); // Set the profile picture in ImageView

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Failed to parse user details", Toast.LENGTH_SHORT).show();
        }
    }

}

