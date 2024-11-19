package com.example.eascanfinal;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class WalletPage extends Fragment {

    private static final String ARG_USERNAME = "username";
    private String username;

    private TextView balanceTextView;
    private Button depositButton;
    private float currentBalance;

    public static WalletPage newInstance(String username) {
        WalletPage fragment = new WalletPage();
        Bundle args = new Bundle();
        args.putString(ARG_USERNAME, username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            username = getArguments().getString(ARG_USERNAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet_page, container, false);

        balanceTextView = view.findViewById(R.id.balanceTextView);
        depositButton = view.findViewById(R.id.depositButton);

        // Load the initial balance from the database
        loadBalanceFromServer();

        // Handle deposit button click
        depositButton.setOnClickListener(v -> showDepositDialog());

        return view;
    }

    // Show a dialog to enter deposit amount
    private void showDepositDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Enter Deposit Amount");

        // Set up input
        final EditText input = new EditText(getContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        // Set up buttons
        builder.setPositiveButton("Deposit", (dialog, which) -> {
            String amountStr = input.getText().toString();
            if (!amountStr.isEmpty()) {
                float depositAmount = Float.parseFloat(amountStr);
                updateBalance(depositAmount);
            } else {
                Toast.makeText(getContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // Update the balance both locally and on the server
    private void updateBalance(float depositAmount) {
        currentBalance += depositAmount; // Update local balance
        balanceTextView.setText("Balance: ₱" + currentBalance); // Update the TextView

        // Update the server-side database
        new Thread(() -> {
            try {
                URL url = new URL("http://192.168.254.102:8080/eascan/update_balance.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                // Send data to server
                String data = "username=" + username + "&balance=" + currentBalance;
                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes());
                os.flush();
                os.close();

                // Handle response from the server
                Scanner inStream = new Scanner(conn.getInputStream());
                while (inStream.hasNextLine()) {
                    Log.d("Response", inStream.nextLine());
                }
                conn.disconnect();

                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Balance updated!", Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to update balance", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // Load the balance from the server
    private void loadBalanceFromServer() {
        new Thread(() -> {
            try {
                URL url = new URL("http://192.168.254.102:8080/eascan/get_balance.php?username=" + username);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                // Handle response from the server
                Scanner inStream = new Scanner(conn.getInputStream());
                StringBuilder response = new StringBuilder();
                while (inStream.hasNextLine()) {
                    response.append(inStream.nextLine());
                }

                // Parse the JSON response
                JSONObject jsonResponse = new JSONObject(response.toString());
                currentBalance = (float) jsonResponse.getDouble("balance");

                // Update the UI on the main thread
                getActivity().runOnUiThread(() -> balanceTextView.setText("Balance: ₱" + currentBalance));

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to load balance", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
