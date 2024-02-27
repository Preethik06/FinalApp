package com.example.finalapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;

public class Details extends AppCompatActivity {
    EditText et_meter_no, et_name;
    Button btn_submit;
    ImageView back_icon;
    String meter, ca, name;
    SQLiteDatabase db;
    SearchView searchView;
    ListView listView;
    ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        //btn_submit=findViewById(R.id.btn_submit);
        back_icon=findViewById(R.id.back_icon);

        searchView = findViewById(R.id.searchView);
        listView = findViewById(R.id.listView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);
        listView.setVisibility(View.GONE);
        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(), Home.class);
                startActivity(intent);
            }
        });

        // Creating database and table
        db = openOrCreateDatabase("BillDB", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS customers(meter_no VARCHAR, ca_no VARCHAR, name VARCHAR, address VARCHAR, email VARCHAR, phone_no int, charges_id VARCHAR);");

        //Search
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                listView.setVisibility(View.VISIBLE);
                adapter.getFilter().filter(newText);
                return true;
            }
        });

        // Assuming you have a ListView named listView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item from the adapter
                String selectedItem = (String) parent.getItemAtPosition(position);

                // You can then perform actions based on the selected item, or extract data from it
                // For example, you can split the string to get meterNo and name
                String[] parts = selectedItem.split(", ");
                String selectedMeterNo = parts[0];
                String selectedName = parts[1];

                Cursor c = db.rawQuery("SELECT * FROM customers WHERE meter_no='" + selectedMeterNo.trim() + "'", null);
                if (c.moveToFirst()) {
                    Toast.makeText(getApplicationContext(), "Authentication Successfull", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Details.this, Bill.class);
                    meter = c.getString(0);
                    name = c.getString(2);
                    ca = c.getString(1);
                    intent.putExtra("meter_key", meter);
                    intent.putExtra("ca_key", ca);
                    intent.putExtra("name_key", name);
                    startActivity(intent);

                } else {
                    showMessage("Error", "Invalid Meter Number or Name");
                }
                //ends fetching code
            }
        });
        listView.setTextFilterEnabled(true);
        //search end
        //btn_submit.setOnClickListener(this);
    }
    private void search(String keyword) {
        adapter.clear();
        Cursor cursor = db.rawQuery("SELECT * FROM customers WHERE meter_no LIKE '%" + keyword + "%' OR name LIKE '%" + keyword + "%'", null);
        if (cursor.getCount() == 0) {
            Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                String meterNo = cursor.getString(0);
                String name = cursor.getString(2);
                adapter.add("" + meterNo + ",  " + name);
            }
        }
        cursor.close();
    }
    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }
}