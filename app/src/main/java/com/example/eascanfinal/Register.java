package com.example.eascanfinal;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;

public class Register extends AppCompatActivity {

    private TextInputEditText fullnameEditText;
    private TextInputEditText usernameEditText;
    private TextInputEditText locationEditText;
    private TextInputEditText passwordEditText;
    private TextView dateTextView;
    private ImageView profileImageView;
    private ImageView idImageView;
    private String selectedDate;
    private Uri profileImageUri;
    private Uri idImageUri;

    private static final int PICK_IMAGE_PROFILE = 1;
    private static final int PICK_IMAGE_ID = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fullnameEditText = findViewById(R.id.fullname);
        usernameEditText = findViewById(R.id.username);
        locationEditText = findViewById(R.id.location);
        passwordEditText = findViewById(R.id.password);
        dateTextView = findViewById(R.id.btnDate);
        profileImageView = findViewById(R.id.imageView);
        idImageView = findViewById(R.id.imageID);

        dateTextView.setOnClickListener(v -> showDatePicker());
        findViewById(R.id.btnRegister).setOnClickListener(v -> registerUser());

        // Set click listener for profile image selection
        findViewById(R.id.addprofile).setOnClickListener(v -> selectImage(PICK_IMAGE_PROFILE));

        // Set click listener for ID image selection
        findViewById(R.id.addID).setOnClickListener(v -> selectImage(PICK_IMAGE_ID));
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                    dateTextView.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void registerUser() {
        String fullname = fullnameEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(fullname) || TextUtils.isEmpty(username) ||
                TextUtils.isEmpty(location) || TextUtils.isEmpty(password) ||
                selectedDate == null || profileImageUri == null || idImageUri == null) {
            Toast.makeText(this, "Please fill all fields, select a date, and upload images", Toast.LENGTH_SHORT).show();
            return;
        }

        // Execute async task to upload the data
        new UploadDataTask().execute(fullname, username, location, password, selectedDate, profileImageUri, idImageUri);
    }

    private void selectImage(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (requestCode == PICK_IMAGE_PROFILE) {
                profileImageView.setImageURI(selectedImageUri);
                profileImageUri = selectedImageUri; // Store the URI directly
            } else if (requestCode == PICK_IMAGE_ID) {
                idImageView.setImageURI(selectedImageUri);
                idImageUri = selectedImageUri; // Store the URI directly
            }
        }
    }

    private class UploadDataTask extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... params) {
            String fullname = (String) params[0];
            String username = (String) params[1];
            String location = (String) params[2];
            String password = (String) params[3];
            String date = (String) params[4];
            Uri profileImageUri = (Uri) params[5];
            Uri idImageUri = (Uri) params[6];

            String profileImageData = encodeImageToBase64(profileImageUri);
            String idImageData = encodeImageToBase64(idImageUri);
            String response = "";

            try {
                // Inside UploadDataTask in the Register.java file
                String postData = "fullname=" + URLEncoder.encode(fullname, "UTF-8") +
                        "&username=" + URLEncoder.encode(username, "UTF-8") +
                        "&location=" + URLEncoder.encode(location, "UTF-8") +
                        "&password=" + URLEncoder.encode(password, "UTF-8") +
                        "&date=" + URLEncoder.encode(date, "UTF-8") +
                        "&type=" + URLEncoder.encode("customer", "UTF-8") +  // Pass the type dynamically
                        "&profileImageData=" + URLEncoder.encode(profileImageData, "UTF-8") +
                        "&idImageData=" + URLEncoder.encode(idImageData, "UTF-8");


                // Create URL object
                URL url = new URL("http://192.168.254.102:8080/eascan/register.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // Send data
                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                // Get response
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    response = "Registration successful!";
                } else {
                    response = "Registration failed: " + conn.getResponseMessage();
                }

                conn.disconnect();
            } catch (Exception e) {
                response = "Registration failed: " + e.getMessage();
            }

            return response;
        }

        private String encodeImageToBase64(Uri imageUri) {
            String base64Image = "";
            try {
                // Convert URI to Bitmap with reduced dimensions
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 500, 500, true); // Resize to 500x500 pixels

                // Compress Bitmap to byte array with quality set to 50
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream); // Compress to 50% quality
                byte[] byteArray = stream.toByteArray();

                // Convert byte array to Base64 string
                base64Image = "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return base64Image;
        }


        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(Register.this, result, Toast.LENGTH_SHORT).show();

            // If registration is successful, open MainScreen
            if (result.equals("Registration successful!")) {
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
                finish(); // Close the registration screen
            }
        }
    }
}
