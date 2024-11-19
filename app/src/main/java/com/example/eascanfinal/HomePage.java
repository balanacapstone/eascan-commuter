package com.example.eascanfinal;

import android.os.Bundle;
import android.util.Log;
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

public class HomePage extends Fragment {

    private static final String ARG_USERNAME = "username";
    private String username;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private ArrayList<Post> postList;

    public static HomePage newInstance(String username) {
        HomePage fragment = new HomePage();
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
        View view = inflater.inflate(R.layout.fragment_home_page, container, false);

        recyclerView = view.findViewById(R.id.postRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);  // Pass context correctly
        recyclerView.setAdapter(postAdapter);

        // Fetch posts
        fetchPosts();

        return view;
    }

    private void fetchPosts() {
        new Thread(() -> {
            try {
                URL url = new URL("http://192.168.254.102:8080/eascan/get_posts.php");

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Send POST data
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write("");
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
                getActivity().runOnUiThread(() -> parsePosts(response.toString()));

            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Failed to fetch posts", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void parsePosts(String jsonResponse) {
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject postObject = jsonArray.getJSONObject(i);
                String title = postObject.getString("title");
                String body = postObject.getString("body");
                String imageUrl = postObject.getString("image");

                // Log the image URL
                Log.d("HomePage", "Image URL: " + imageUrl);

                postList.add(new Post(title, body, imageUrl));
            }

            // Notify adapter of data change
            postAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Failed to parse posts", Toast.LENGTH_SHORT).show();
        }
    }
}