package com.example.eascanfinal;

public class Post {
    private String title;
    private String body;
    private String imageBase64;

    public Post(String title, String body, String imageBase64) {
        this.title = title;
        this.body = body;
        this.imageBase64 = imageBase64;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getImageBase64() {
        return imageBase64;
    }
}
