package com.example.finalapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

public class Bill_Preview extends AppCompatActivity {
    private PDFView pdfView;
    Button btnPrint;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_preview);


        /*btnPrint.setOnClickListener(new View.OnClickListener() {
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
        });*/
    }
}