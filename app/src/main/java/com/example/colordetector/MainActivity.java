package com.example.colordetector;

import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.text.TextUtils;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static RequestQueue requestQueue;
    public static final int GET_FROM_GALLERY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new NukeSSLCerts().nuke();
        super.onCreate(savedInstanceState);
        //Sets a queue for requests
        requestQueue = Volley.newRequestQueue(this);
        setContentView(R.layout.activity_main);

        Button uploadButton = findViewById(R.id.upload);
        //Sets a listener to open the gallery upon clicking the upload button.
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Checks for requests
        if(requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                //Selects the image and saves it to the bitmap variable
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                //Scaling bitmap
                float aspectRatio = bitmap.getWidth() / (float) bitmap.getHeight();
                int width = 512;
                int height = Math.round(width / aspectRatio);
                Bitmap scaledBitmap = bitmap.createScaledBitmap(bitmap, width, height, true);
                //Array of the image's pixels that is currently empty
                int[] pixelArray = new int[scaledBitmap.getHeight() * scaledBitmap.getWidth()];
                //Populates the array with the image's pixels
                scaledBitmap.getPixels(pixelArray, 0, scaledBitmap.getWidth(), 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());
                process(pixelArray);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        openWebPage("https://www.thecolorapi.com/");
    }
    //Function to find the colors of an image
    protected Map findColors(int[] pixelArray) {
        //Hash map with counters for each color
        Map<List<String>, Integer> colorCount = new HashMap<>(256);
        //Find count of each pixel
        for (int i = 0; i < pixelArray.length; i++) {
            //Get R, G and B values of each pixel
            String R = Integer.toString(Color.red(pixelArray[i]));
            String G = Integer.toString(Color.green(pixelArray[i]));
            String B = Integer.toString(Color.blue(pixelArray[i]));
            //Connecting these values together for each color by grouping in a list
            List<String> currentColor = new ArrayList<>(3);
            currentColor.add(R);
            currentColor.add(G);
            currentColor.add(B);
            //Getting the current counter value for the color
            Integer value = colorCount.get(currentColor);
            //If the color is not in the map, add it with a count of 1
            if (value == null) {
                //Adds color
                colorCount.put(currentColor, 1);
            } else {
                //Or else increment the counter of the existing color by 1
                colorCount.put(currentColor, value + 1);
            }
        }
        //System.out.println(colorCount);
        return colorCount;
    }
    //Function to send a GET request to get verbal information about a color
    protected void process(int[] pixelArray) throws IOException {
        String url = "https://thecolorapi.com/id";
        Map<List<String>, Integer> colorCount = findColors(pixelArray);
        int numberOfColors = 0;
        //Returns all keys that have this maximum value to find colors that occur this exact number of times
        while (numberOfColors != 3) {
            //Finds the maximum value count in the map
            Integer maximum = Collections.max(colorCount.values());
            for (Map.Entry<List<String>, Integer> entry : colorCount.entrySet()) {
                if (entry.getValue() == maximum) {
                    numberOfColors++;
                    String rgb = TextUtils.join(",", entry.getKey());
                    String query = url + "?" + "rgb=" + rgb;
                    System.out.println(query);
                    sendRequest(query);
                    colorCount.put(entry.getKey(), 0);
                    break;
                }
            }
        }
    }

    public void sendRequest(final String query) {

        JsonObjectRequest data = new JsonObjectRequest
                (Request.Method.GET, query, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response: ", response.toString());
                        System.out.println(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }

                });

        data.setRetryPolicy(new DefaultRetryPolicy(5000, 1, 1.0f));
        requestQueue.add(data);

    protected void display(JSONObject input) {
        
    }
    /**
     * Open a web page of a specified URL
     *
     * @param url URL to open
     */
    protected void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
