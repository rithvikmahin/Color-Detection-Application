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
                //Array of the image's pixels that is currently empty
                int[] pixelArray = new int[bitmap.getHeight() * bitmap.getWidth()];
                //Populates the array with the image's pixels
                bitmap.getPixels(pixelArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
                //Getting the R, G and B values of the first pixels
                String R = Integer.toString(Color.red(pixelArray[0]));
                String G = Integer.toString(Color.green(pixelArray[0]));
                String B = Integer.toString(Color.blue(pixelArray[0]));
                //Logging the values to test if they are valid values
                Log.d("Red", R);
                Log.d("Green", G);
                Log.d("Blue", B);


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
