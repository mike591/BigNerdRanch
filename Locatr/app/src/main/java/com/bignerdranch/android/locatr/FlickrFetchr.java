package com.bignerdranch.android.locatr;

import android.location.Location;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mike on 2/19/2016.
 */
public class FlickrFetchr {
    private static final String TAG = "FlickrFetchr";

    private static final String API_Key = "19f474c86a8d628fbf32e0a2ef002e7e";
    private static final String FETCH_RECENTS_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    private static final Uri ENDPOINT = Uri.parse("https://api.flickr.com/services/rest/")
            //The Uri.Builder is useful for generating a proper URL with all its parameters
            .buildUpon()
            .appendQueryParameter("api_key", API_Key)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s, geo")
            .build();


    public byte[] getUrlBytes(String urlSpec) throws IOException {
        //First create a URL from the string urlSpec
        URL url = new URL(urlSpec);
        //Then we CREATE A CONNECTION (not connected yet) to the URL by using the openConnection() method
        //We cast it to http so that we can use more methods on it.
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try {
            //The out is where we store the data that is returned from our connection.
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            //This lets you finally connect to our endpoint. We need to call getInputStream().
            //This is our query?
            InputStream in = connection.getInputStream();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) >0 ){
                //We read() repeatedly until our connection runs out of data
                out.write(buffer, 0, bytesRead);
            }
            //When no more data is returned for packaging, we spit out the data and close the output stream.
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        //This method calls the getUrlBytes method and converts the output into a String.
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchRecentPhotos(){
        String url = buildUrl(FETCH_RECENTS_METHOD, null);
        return downloadGalleryItems(url);
    }

    public List<GalleryItem> searchPhotos(String query) {
        String url = buildUrl(SEARCH_METHOD, query);
        return downloadGalleryItems(url);
    }

    public List<GalleryItem> searchPhotos(Location location) {
        String url = buildUrl(location);
        return downloadGalleryItems(url);
    }

    private List<GalleryItem> downloadGalleryItems(String url) {
        List<GalleryItem> items = new ArrayList<>();

        //This builds the url and grabs the string generated in the getUrlBytes/getUrlString.
        try {
            //Once you have build the URL, you pass it through the getUrlString method
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            //The JSONObject parses our data for us and presents it in a nice format for easy reading
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        }

        return items;
    }

    //helper method to build URL for search or grabbing recent uploads
    private String buildUrl(String method, String query) {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", method);
        if (method.equals(SEARCH_METHOD)) {
            uriBuilder.appendQueryParameter("text", query);
        }

        return uriBuilder.build().toString();
    }

    private String buildUrl(Location location) {
        return ENDPOINT.buildUpon()
                .appendQueryParameter("method", SEARCH_METHOD)
                .appendQueryParameter("lat", "" + location.getLatitude())
                .appendQueryParameter("lon", "" + location.getLongitude())
                .build().toString();
    }

    //The parseItems method goes through the jsonBody created from the fetchItems method and generates the list of gallery items.
    private void parseItems(List<GalleryItem> items, JSONObject jsonBody) throws IOException, JSONException {
        //First we get the outermost xml category
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        //Then we can use it to get inside to the individual items in the array
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for (int i = 0; i < photoJsonArray.length(); i++) {
            //Each item in the array is a single picture and we want to create an item and store it into our gallery list
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();
            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));

            if (!photoJsonObject.has("url_s")) {
                continue;
            }

            item.setUrl(photoJsonObject.getString("url_s"));
            item.setOwner(photoJsonObject.getString("owner"));
            item.setLat(photoJsonObject.getDouble("latitude"));
            item.setLon(photoJsonObject.getDouble("longitude"));
            //items is the list that contains each GalleryItem and we are adding into it each item that we've created/parsed
            items.add(item);
        }

    }

}
