package com.example.eascanfinal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private ArrayList<Transaction> transactions;

    public TransactionAdapter(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.conductorUsernameTextView.setText("Conductor: " + transaction.getConductorUsername());
        holder.fareTypeTextView.setText("Amount: " + transaction.getTotal());
        holder.dateTextView.setText("Date: " + transaction.getDate());
        holder.locationTextView.setText("Location: " + transaction.getLocation());
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView conductorUsernameTextView, fareTypeTextView, dateTextView, locationTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            conductorUsernameTextView = itemView.findViewById(R.id.conductorUsername);
            fareTypeTextView = itemView.findViewById(R.id.fareType);
            dateTextView = itemView.findViewById(R.id.date);
            locationTextView = itemView.findViewById(R.id.location);
        }
    }
}
