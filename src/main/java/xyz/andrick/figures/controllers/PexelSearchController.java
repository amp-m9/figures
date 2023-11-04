package xyz.andrick.figures.controllers;

import com.google.gson.Gson;
import xyz.andrick.figures.records.PexelResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class PexelSearchController {
    public static PexelResponse search(String query) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.pexels.com/v1/search?query=" + query + "&per_page=20"))
                .header("authorization", "QGiZyqSr4zsKiqrAWwjToKWgc6jVs7M5fB7Lmk11cW3SeZnY4yWQz1vB")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (response.statusCode() != 200)
            throw new Exception("failed");
        Gson gson = new Gson();
        return gson.fromJson(response.body(), PexelResponse.class);
    }
}
