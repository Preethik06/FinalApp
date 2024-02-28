package com.example.finalapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    EditText et_email, et_password;
    Button btn_login;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et_email=(EditText) findViewById(R.id.email);
        et_password=(EditText) findViewById(R.id.password);
        btn_login=findViewById(R.id.btn_login);

        // Creating database and table
        db = openOrCreateDatabase("BillDB", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS users(email VARCHAR,pass VARCHAR);");

        btn_login.setOnClickListener(this::onClick);
    }
    public void onClick(View view){
        // Display a record from the Student table
        if (view == btn_login) {
            // Checking for empty email and pass
            if (et_email.getText().toString().trim().length() == 0) {
                if (et_password.getText().toString().trim().length() == 0){
                    showMessage("Error", "Please enter Email and Password");
                    return;
                }
            }
            Cursor c = db.rawQuery("SELECT * FROM users WHERE email='" + et_email.getText() + "'", null);
            Cursor c2 = db.rawQuery("SELECT * FROM users WHERE pass='" + et_password.getText() + "'", null);
            if (c.moveToFirst() && c2.moveToFirst()) {
                Toast.makeText(getApplicationContext(),"Login Successfull", Toast.LENGTH_SHORT).show();
                Intent i=new Intent(MainActivity.this,Home.class);
                finish();
                startActivity(i);
            } else {
                showMessage("Error", "Invalid Email or Password");
                clearText();
            }
        }
    }
    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    public void clearText() {
        et_email.setText("");
        et_password.setText("");
        et_email.requestFocus();
    }
}