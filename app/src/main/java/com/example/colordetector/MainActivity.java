package com.example.colordetector;

import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static RequestQueue requestQueue;
    public static final int GET_FROM_GALLERY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Sets a queue for requests
        requestQueue = Volley.newRequestQueue(this);
        setContentView(R.layout.activity_main);

        Button uploadButton = findViewById(R.id.upload);
        //Sets a listener to open the gallery upon clicking the upload button
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
                //Scaling bitmap to 256 x 256
                Bitmap scaledBitmap = bitmap.createScaledBitmap(bitmap, 256, 256, true);
                //Array of the image's pixels that is currently empty
                int[] pixelArray = new int[scaledBitmap.getHeight() * scaledBitmap.getWidth()];
                //Populates the array with the image's pixels
                scaledBitmap.getPixels(pixelArray, 0, scaledBitmap.getWidth(), 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());
                findColors(pixelArray);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                colorCount.put(currentColor, 1);
            } else {
                //Or else increment the counter of the existing color by 1
                colorCount.put(currentColor, value + 1);
            }
        }
        System.out.println(colorCount);
        return colorCount;
    }
}
