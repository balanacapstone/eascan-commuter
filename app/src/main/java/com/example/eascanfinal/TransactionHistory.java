package com.example.eascanfinal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class TransactionHistory extends Fragment {

    private static final String ARG_USERNAME = "username";
    private String username;
    private RecyclerView recyclerView;
    private TransactionAdapter transactionAdapter;
    private ArrayList<Transaction> transactionList;

    public static TransactionHistory newInstance(String username) {
        TransactionHistory fragment = new TransactionHistory();
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
        View view = inflater.inflate(R.layout.fragment_transaction_history, container, false);

        recyclerView = view.findViewById(R.id.transactionRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(transactionList);
        recyclerView.setAdapter(transactionAdapter);

        // Fetch transactions for this user
        fetchTransactionHistory(username);

        return view;
    }

    private void fetchTransactionHistory(String username) {
        new Thread(() -> {
            try {
                // Create URL
                URL url = new URL("http://192.168.254.102:8080/eascan/get_user_transactions.php");

                // Create HttpURLConnection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Send POST data
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write("customer_username=" + username);
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
                getActivity().runOnUiThread(() -> parseTransactionHistory(response.toString()));

            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Failed to fetch transactions", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void parseTransactionHistory(String jsonResponse) {
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject transactionObject = jsonArray.getJSONObject(i);
                String conductorUsername = transactionObject.getString("conductor_username");
                double total = transactionObject.getDouble("total");
                String date = transactionObject.getString("date");
                String location = transactionObject.getString("location");

                // Add transaction to the list
                Transaction transaction = new Transaction(conductorUsername, total, date, location);
                transactionList.add(transaction);
            }

            // Notify the adapter about data change
            transactionAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Failed to parse transactions", Toast.LENGTH_SHORT).show();
        }
    }
}
