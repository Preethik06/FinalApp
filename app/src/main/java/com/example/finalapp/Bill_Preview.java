package com.example.finalapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

public class Bill_Preview extends AppCompatActivity {

    private PDFView pdfView;
    ImageView back_icon;
    Button btnPrint, btnDetails;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_preview);
        back_icon= findViewById(R.id.back_icon);
        pdfView = findViewById(R.id.pdfView);
        btnPrint = findViewById(R.id.btnPrint);
        btnDetails = findViewById(R.id.btnBackDetails);

        //Back icon
        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Bill_Preview.this, Bill.class);
                startActivity(i);
            }
        });

        // Get the PDF file path from the intent
        String filePath = getIntent().getStringExtra("PDF_PATH");

        // Load and display the PDF file
        pdfView.fromFile(new File(filePath))
                .enableSwipe(true) // Allows to swipe pages horizontally
                .load();

        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.dynamixsoftware.printershare");

                if (intent != null) {
                    // The app exists, so launch it
                    startActivity(intent);
                } else {
                    // The app doesn't exist on the device
                    Toast.makeText(Bill_Preview.this, "App not installed", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2=new Intent(Bill_Preview.this, Details.class);
                startActivity(intent2);
            }
        });
    }
}