package com.example.finalapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

import java.io.FileNotFoundException;

public class Bill extends AppCompatActivity {
    TextView tv_meter_no, tv_ca_no, tv_name;
    String meter, ca, name, previous;
    EditText meter_reading;
    Button btn_generate;
    ImageView back_icon;
    SQLiteDatabase db;
    String c_name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);
        tv_meter_no=findViewById(R.id.tv_meter_no);
        tv_ca_no=findViewById(R.id.tv_cano_no);
        tv_name=findViewById(R.id.tv_name);
        meter_reading=findViewById(R.id.curr_reading);
        btn_generate=findViewById(R.id.btn_generate);
        back_icon=findViewById(R.id.back_icon);

        //Back icon
        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Bill.this, Details.class);
                startActivity(i);
            }
        });

        // Creating database and table
        db = openOrCreateDatabase("BillDB", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS customers(meter_no VARCHAR, name VARCHAR, ca_no VARCHAR, curr_reading INTEGER);");

        //Displaying Details (meter no., cano., name)
        Intent i=getIntent();
        meter=i.getStringExtra("meter_key");
        ca=i.getStringExtra("ca_key");
        name=i.getStringExtra("name_key");

        tv_meter_no.setText(meter);
        tv_ca_no.setText(ca);
        tv_name.setText(name);

        //prev reading gets updated
        Cursor c = db.rawQuery("SELECT * FROM bill_details WHERE meter_no='" + meter + "'", null);
        if (c.moveToFirst()) {
            previous=c.getString(2);
            db.execSQL("UPDATE bill_details SET prev_reading='" + previous + "' WHERE meter_no='" + meter +"' ");
            Toast.makeText(getApplicationContext(),"Prev Reading Modified",Toast.LENGTH_SHORT).show();
        }

        btn_generate.setOnClickListener(this::onClick);
    }
    public void onClick(View view){
        if (meter.length() == 0) {
            showMessage("Error", "Meter number not valid");
            return;
        }
        Cursor c = db.rawQuery("SELECT * FROM bill_details WHERE meter_no='" + meter + "'", null);
        if (c.moveToFirst()) {
            db.execSQL("UPDATE bill_details SET curr_reading='" + meter_reading.getText() + "'WHERE meter_no='" + meter +"' ");
            showMessage("Success", "Record Modified");
            Intent intent=new Intent(getApplicationContext(),Bill_Preview.class);
            startActivity(intent);

            //Calculation----> Kaushal

            int n1 = Integer.parseInt(c.getString(1));//previous reading
            int n2 = Integer.parseInt(c.getString(2));//current reading
            int unit = n2 - n1;
            double billAmount = calculateElectricityBill(unit);

            String name = c.getString(1);
            String meterNo = c.getString(0);
            String caNo = c.getString(2);
            String address = "Ponda";
            String currentReading = c.getString(4);//"2345";
            String total = String.valueOf(billAmount);

            clearText();

        } else {
            showMessage("Error", "Invalid Value");
        }
        clearText();

        // Generate PDF
        generatePDF();
    }
    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    public void clearText() {
        meter_reading.setText("");
    }

    private void generatePDF() {

        if (meter.toString().trim().length() == 0) {
            showMessage("Error", "Please enter Rollno");
            return;
        }
        Cursor c = db.rawQuery("SELECT * FROM customers WHERE meter_no='" + meter + "'", null);
        if (c.moveToFirst()) {
            c_name=c.getString(2);
        } else {
            showMessage("Error", "Invalid Rollno");
            clearText();
        }

        String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/example.pdf";
        try {
            PdfWriter writer = new PdfWriter(pdfPath);
            PdfDocument pdfDocument = new PdfDocument(writer);
            PageSize pageSize = new PageSize(300,600);
            Document document = new Document(pdfDocument,pageSize);

            //Row1

            // Create a Paragraph with the desired text
            Paragraph Title = new Paragraph();

            // Set the text alignment to center
            Title.setTextAlignment(TextAlignment.CENTER);
            // Set the font size for the paragraph
            Title.setFontSize(14);
            //Adding Content to the PDF File
            Title.add("ELECTRICITY DEPARTMENT\nGOVERNMENT OF GOA\n\n").setBold();

            // Add the paragraph to the document
            document.add(Title);

            /*------------------------------------------------------------------------------------*/

            //Row2

            Table table1 =new Table(4);

            // Set the text alignment for the table
            table1.setTextAlignment(TextAlignment.CENTER);
            table1.setFontSize(10);
            // Set the width of the table
            table1.setWidth(UnitValue.createPercentValue(100));
            // Set border properties for the table
            table1.setBorder(Border.NO_BORDER);

            // Add headers to the table
            Paragraph Meter_No = new Paragraph();
            Paragraph Unit = new Paragraph();
            Paragraph San_Load = new Paragraph();
            Paragraph Tariff_Category = new Paragraph();
            Meter_No.add("Meter No");
            Unit.add("Unit");
            San_Load.add("San Load");
            Tariff_Category.add("Tariff Category");

            // Add data rows to the table
            Paragraph MValue = new Paragraph();
            Paragraph UValue = new Paragraph();
            Paragraph SValue = new Paragraph();
            Paragraph TValue = new Paragraph();
            MValue.add("1234");
            UValue.add("KWH");
            SValue.add("2.62kw");
            TValue.add("LTD");

            // Add headers to the table
            table1.addCell(new Cell(1,1).add(Meter_No).setBold().setBorder(Border.NO_BORDER));
            table1.addCell(new Cell(1,1).add(Unit).setBold().setBorder(Border.NO_BORDER));
            table1.addCell(new Cell(1,1).add(San_Load).setBold().setBorder(Border.NO_BORDER));
            table1.addCell(new Cell(1,1).add(Tariff_Category).setBold().setBorder(Border.NO_BORDER));

            //Add data rows to the table
            table1.addCell(new Cell(2,1).add(MValue).setBorder(Border.NO_BORDER));
            table1.addCell(new Cell(2,1).add(UValue).setBorder(Border.NO_BORDER));
            table1.addCell(new Cell(2,1).add(SValue).setBorder(Border.NO_BORDER));
            table1.addCell(new Cell(2,1).add(TValue).setBorder(Border.NO_BORDER));

            // Add the table to the document
            document.add(table1.setHorizontalAlignment(HorizontalAlignment.CENTER));

            /*------------------------------------------------------------------------------------*/

            // Add a line break
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));

            /*------------------------------------------------------------------------------------*/

            //Row3

            Table table2 =new Table(4);

            // Set the text alignment for the table
            table2.setTextAlignment(TextAlignment.CENTER);
            table2.setFontSize(10);
            // Set the width of the table
            table2.setWidth(UnitValue.createPercentValue(100));
            // Set border properties for the table
            table2.setBorder(Border.NO_BORDER);

            // Add headers to the table
            Paragraph Bill_NO = new Paragraph();
            Paragraph Bill_Date = new Paragraph();
            Paragraph Bill_Time = new Paragraph();
            Paragraph Meter_Reader = new Paragraph();
            Bill_NO.add("Bill no");
            Bill_Date.add("Bill Date");
            Bill_Time.add("Bill Time");
            Meter_Reader.add("Meter Reader");

            // Add data rows to the table
            Paragraph BNValue = new Paragraph();
            Paragraph BDValue = new Paragraph();
            Paragraph BTValue = new Paragraph();
            Paragraph MRValue = new Paragraph();
            BNValue.add("1234");
            BDValue.add("23/01/2023");
            BTValue.add("12:45 PM");
            MRValue.add("xyz");

            // Add headers to the table
            table2.addCell(new Cell(1,1).add(Bill_NO).setBold().setBorder(Border.NO_BORDER));
            table2.addCell(new Cell(1,1).add(Bill_Date).setBold().setBorder(Border.NO_BORDER));
            table2.addCell(new Cell(1,1).add(Bill_Time).setBold().setBorder(Border.NO_BORDER));
            table2.addCell(new Cell(1,1).add(Meter_Reader).setBold().setBorder(Border.NO_BORDER));

            //Add data rows to the table
            table2.addCell(new Cell(2,1).add(BNValue).setBorder(Border.NO_BORDER));
            table2.addCell(new Cell(2,1).add(BDValue).setBorder(Border.NO_BORDER));
            table2.addCell(new Cell(2,1).add(BTValue).setBorder(Border.NO_BORDER));
            table2.addCell(new Cell(2,1).add(MRValue).setBorder(Border.NO_BORDER));

            // Add the table to the document
            document.add(table2.setHorizontalAlignment(HorizontalAlignment.CENTER));

            /*------------------------------------------------------------------------------------*/

            // Add a line break
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));

            /*------------------------------------------------------------------------------------*/

            //Row4

            Paragraph paragraph1 = new Paragraph();

            // Set the text alignment to center
            paragraph1.setTextAlignment(TextAlignment.LEFT);
            // Set the font size for the paragraph
            paragraph1.setFontSize(10);
            //Adding Content to the PDF File

            //Add CA NO
            paragraph1.add(new Text("CA No ").setBold());
            paragraph1.add("         ");
            paragraph1.add("1234\n");

            //Add Name
            paragraph1.add(new Text("Name ").setBold());
            paragraph1.add("          ");
            paragraph1.add(c_name);
            paragraph1.add("\n");

            //Add Address
            paragraph1.add(new Text("Address ").setBold());
            paragraph1.add("      ");
            paragraph1.add("Goa\n");

            //Add Tel
            paragraph1.add(new Text("Tel ").setBold());
            paragraph1.add("              ");
            paragraph1.add("1234567890\n");

            //Add Email
            paragraph1.add(new Text("Email Id ").setBold());
            paragraph1.add("      ");
            paragraph1.add("xyz@gmail.com\n\n");

            // Add the paragraph to the document
            document.add(paragraph1);

            /*------------------------------------------------------------------------------------*/

            //Row5

            Table table3 =new Table(3);

            table3.setFontSize(10);
            // Set the width of the table
            table3.setWidth(UnitValue.createPercentValue(100));
            // Set border properties for the table
            table3.setBorder(Border.NO_BORDER);

            // Add headers to the table
            Paragraph From = new Paragraph();
            Paragraph To = new Paragraph();
            Paragraph Days = new Paragraph();
            From.add("From");
            To.add("To");
            Days.add("Days");

            // Add data rows to the table
            Paragraph FromValue = new Paragraph();
            Paragraph T0Value = new Paragraph();
            Paragraph DaysValue = new Paragraph();
            FromValue.add("01/01/2023");
            T0Value.add("31/01/2023");
            DaysValue.add("31");

            // Add headers to the table
            table3.addCell(new Cell(1,1).add(From).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT));
            table3.addCell(new Cell(1,1).add(To).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
            table3.addCell(new Cell(1,1).add(Days).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

            //Add data rows to the table
            table3.addCell(new Cell(2,1).add(FromValue).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT));
            table3.addCell(new Cell(2,1).add(T0Value).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
            table3.addCell(new Cell(2,1).add(DaysValue).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

            document.add(table3);

            /*------------------------------------------------------------------------------------*/

            // Add a line break
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));

            /*------------------------------------------------------------------------------------*/

            //Row 6

            Table table4 = new Table(3);

            table4.setFontSize(10);
            // Set the width of the table
            table4.setWidth(UnitValue.createPercentValue(100));
            // Set border properties for the table
            table4.setBorder(Border.NO_BORDER);

            // Add headers to the table
            Paragraph Current_Reading = new Paragraph();
            Paragraph Prev_Reading = new Paragraph();
            Paragraph Unit_Diff = new Paragraph();
            Paragraph Fixed_Charge = new Paragraph();
            Paragraph Meter_Rent = new Paragraph();
            Paragraph Prev_Pending_Amount = new Paragraph();

            Current_Reading.add("Current Reading");
            Prev_Reading.add("Prev Reading");
            Unit_Diff.add("Unit Diff");
            Fixed_Charge.add("Fixed Charge");
            Meter_Rent.add("Meter Rent");
            Prev_Pending_Amount.add("Prev Pending Amount");

            // Add data rows to the table
            Paragraph CurrentValue = new Paragraph();
            Paragraph PrevValue = new Paragraph();
            Paragraph UnitDiffValue = new Paragraph();
            Paragraph FixedChargeValue = new Paragraph();
            Paragraph MeterRentValue = new Paragraph();
            Paragraph PrevPendingAmountValue = new Paragraph();

            CurrentValue.add("2080");
            PrevValue.add("2050");
            UnitDiffValue.add("30");
            FixedChargeValue.add("10");
            MeterRentValue.add("10");
            PrevPendingAmountValue.add("300");

            // Add headers to the table
            table4.addCell(new Cell(1,1).add(Current_Reading).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT));
            //Add data rows to the table
            table4.addCell(new Cell(1,3).add(CurrentValue).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

            // Add headers to the table
            table4.addCell(new Cell(1,1).add(Prev_Reading).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT));
            //Add data rows to the table
            table4.addCell(new Cell(1,3).add(PrevValue).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

            // Add headers to the table
            table4.addCell(new Cell(1,1).add(Unit_Diff).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT));
            //Add data rows to the table
            table4.addCell(new Cell(1,3).add(UnitDiffValue).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

            // Add headers to the table
            table4.addCell(new Cell(1,1).add(Fixed_Charge).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT));
            //Add data rows to the table
            table4.addCell(new Cell(1,3).add(FixedChargeValue).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

            // Add headers to the table
            table4.addCell(new Cell(1,1).add(Meter_Rent).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT));
            //Add data rows to the table
            table4.addCell(new Cell(1,3).add(MeterRentValue).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

            // Add headers to the table
            table4.addCell(new Cell(1,1).add(Prev_Pending_Amount).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT));
            //Add data rows to the table
            table4.addCell(new Cell(1,3).add(PrevPendingAmountValue).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

            // Add the paragraph to the document
            document.add(table4);

            /*------------------------------------------------------------------------------------*/

            //Row7

            //Add line
            document.add(new Paragraph("---------------------------------------------------------"));

            Table table5 =new Table(2);

            table5.setFontSize(10);
            // Set the width of the table
            table5.setWidth(UnitValue.createPercentValue(100));
            // Set border properties for the table
            table5.setBorder(Border.NO_BORDER);

            // Add headers to the table
            Paragraph Due_Date = new Paragraph();
            Paragraph Total = new Paragraph();
            Due_Date.add("Due Date");
            Total.add("Total");

            // Add data rows to the table
            Paragraph Due_DateValue = new Paragraph();
            Paragraph TotalValue = new Paragraph();
            Due_DateValue.add("20/02/2023");
            TotalValue.add("1500");

            // Add headers to the table
            table5.addCell(new Cell(1,1).add(Due_Date).setBold().setBorder(Border.NO_BORDER)/*.setTextAlignment(TextAlignment.LEFT)*/);
            table5.addCell(new Cell(1,1).add(Total).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

            //Add data rows to the table
            table5.addCell(new Cell(2,1).add(Due_DateValue).setBorder(Border.NO_BORDER)/*.setTextAlignment(TextAlignment.LEFT)8*/.setBold());
            table5.addCell(new Cell(2,1).add(TotalValue).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT).setBold());

            document.add(table5);

            /*------------------------------------------------------------------------------------*/

            document.close();
            Toast.makeText(this, "PDF generated successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(Bill.this,Bill_Preview.class);
            intent.putExtra("PDF_PATH",pdfPath);
            startActivity(intent);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //Calculation
    private double calculateElectricityBill(int unit) {
        double billAmount = 0;
        int slab;
        switch (unit / 100) {
            case 0:
                slab = 0;
                break;
            case 1:
                slab = 1;
                break;
            case 2:
                slab = 2;
                break;
            case 3:
                slab = 3;
                break;
            case 4:
                slab = 4;
                break;
            default:
                slab = 5;
                break;
        }
        switch (slab) {
            case 0:
                billAmount = (unit * 1.75);
                break;
            case 1:
                billAmount = 100 * 1.75 + (unit-100) * 2.60;
                break;
            case 2:
                billAmount = 100 * 1.75 + 100 * 2.60 + (unit-200) * 3.30;
                break;
            case 3:
                billAmount = 100 * 1.75 + 100 * 2.60 + 100 * 3.30 + (unit-300) * 4.40;
                break;
            default:
                billAmount = 100 * 1.75 + 100 * 2.60 + 100 * 3.30 + 100 * 4.40 + (unit-400) * 5.10;
                break;
        }
        return billAmount;
    }
}