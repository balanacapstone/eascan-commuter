package com.example.eascanfinal;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class QRPage extends Fragment {

    private static final String ARG_USERNAME = "username";
    private String username;
    private ImageView qrCodeImageView;
    private Button generateQrButton;

    public QRPage() {
        // Required empty public constructor
    }

    public static QRPage newInstance(String username) {
        QRPage fragment = new QRPage();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_q_r_page, container, false);

        qrCodeImageView = view.findViewById(R.id.qr_code_image);
        generateQrButton = view.findViewById(R.id.generate_qr_button);

        // Fetch existing QR code from server
        fetchQRCodeFromServer();

        generateQrButton.setOnClickListener(v -> {
            // Only allow generating a new QR code if none exists
            if (qrCodeImageView.getDrawable() == null) {
                generateQRCode(username, qrCodeImageView);
            } else {
                Toast.makeText(getContext(), "QR Code already exists.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void fetchQRCodeFromServer() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://192.168.254.102:8080/eascan/get_qr_code.php",
                response -> {
                    try {
                        Log.d("QRPage", "Response: " + response); // Log the response
                        JSONObject jsonResponse = new JSONObject(response);
                        String qrCodeData = jsonResponse.getString("qrCodeData");
                        if (!qrCodeData.isEmpty()) {
                            // Load the QR code into the ImageView
                            byte[] decodedString = Base64.decode(qrCodeData.replace("data:image/png;base64,", ""), Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            qrCodeImageView.setImageBitmap(decodedByte);
                            generateQrButton.setVisibility(View.GONE); // Hide the button if QR code exists
                        } else {
                            Toast.makeText(getContext(), "No QR Code found.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error fetching QR Code", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error fetching QR Code: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                return params;
            }
        };

        // Add the request to the RequestQueue
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(stringRequest);
    }


    private void generateQRCode(String content, ImageView imageView) {
        try {
            BitMatrix bitMatrix = new com.google.zxing.qrcode.QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            imageView.setImageBitmap(bitmap);

            // Convert bitmap to base64
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String qrCodeBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);

            // Save to server
            saveQRCodeToServer(username, qrCodeBase64);
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error generating QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveQRCodeToServer(String username, String qrCodeBase64) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://192.168.254.102:8080/eascan/save_qr_code.php",
                response -> Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(getContext(), "Error saving QR Code: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("qrCodeData", "data:image/png;base64," + qrCodeBase64); // Prefixing with base64 data type
                return params;
            }
        };

        // Add the request to the RequestQueue
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(stringRequest);
    }
}
