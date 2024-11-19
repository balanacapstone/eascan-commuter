package com.example.eascanfinal;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SecondRegister extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Button register, uploadButton;
    private ImageView imageID;
    private Uri imageUri;
    private String imagePath; // Variable to hold the image path

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second_register);

        // Initialize views
        register = findViewById(R.id.btnRegister);
        uploadButton = findViewById(R.id.btnupload);
        imageID = findViewById(R.id.imageID);

        // Retrieve the data from Intent
        Intent intent = getIntent();
        String fullName = intent.getStringExtra("fullname");
        String username = intent.getStringExtra("username");
        String location = intent.getStringExtra("location");
        String password = intent.getStringExtra("password");
        String date = intent.getStringExtra("date");

        // Check if data is null
        if (fullName == null || username == null || location == null || password == null || date == null) {
            Toast.makeText(this, "Failed to retrieve user data.", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if data is not available
            return;
        }

        // Handle the registration click
        register.setOnClickListener(v -> {
            if (imagePath != null) {
                new RegisterTask().execute(fullName, username, location, password, date, imagePath);
            } else {
                Toast.makeText(SecondRegister.this, "Please upload an ID image.", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle the upload button click
        uploadButton.setOnClickListener(v -> openImageChooser());

        // Apply window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Method to open image chooser
    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select ID Image"), PICK_IMAGE_REQUEST);
    }

    // Handle the result of the image chooser
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                // Display the selected image in the ImageView
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageID.setImageBitmap(bitmap);

                // Save the image path
                imagePath = saveImageToPath(imageUri, "users/" + "username" + "/image/picture.jpg");
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to save image to a specific path
    private String saveImageToPath(Uri imageUri, String relativePath) {
        // Create a file in the app's external files directory
        File directory = new File(getExternalFilesDir(null), relativePath);
        if (!directory.getParentFile().exists()) {
            directory.getParentFile().mkdirs(); // Create parent directories if they don't exist
        }

        File imageFile = new File(directory.getAbsolutePath());
        try (InputStream inputStream = getContentResolver().openInputStream(imageUri);
             FileOutputStream outputStream = new FileOutputStream(imageFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null; // Handle error if needed
        }

        return imageFile.getAbsolutePath(); // Return the path where the image is saved
    }

    // AsyncTask for registration
    private class RegisterTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String fullName = params[0];
            String username = params[1];
            String location = params[2];
            String password = params[3];
            String date = params[4];
            String imagePath = params[5];
            String serverUrl = "http://192.168.254.108/eascan/register.php"; // Replace with your server URL

            try {
                // Set up the connection
                URL url = new URL(serverUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // Prepare the parameters
                StringBuilder postData = new StringBuilder();
                postData.append("fullname=").append(fullName)
                        .append("&username=").append(username)
                        .append("&location=").append(location)
                        .append("&password=").append(password)
                        .append("&date=").append(date)
                        .append("&imagePath=").append(imagePath);

                // Send the request
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(postData.toString());
                dos.flush();
                dos.close();

                // Get the response
                InputStream inputStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString(); // Return server response
            } catch (Exception e) {
                e.printStackTrace();
                return null; // Return null in case of error
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Toast.makeText(SecondRegister.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                // Navigate to the main screen
                Intent intent = new Intent(SecondRegister.this, MainScreen.class);
                startActivity(intent);
                finish(); // Close this activity
            } else {
                Toast.makeText(SecondRegister.this, "Registration Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}