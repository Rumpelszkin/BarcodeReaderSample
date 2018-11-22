package com.varvet.barcodereadersample;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import static com.varvet.barcodereadersample.MainActivity.QR_CODE_KEY;

public class AddScanedContact extends AppCompatActivity {

    Button button;
    EditText editText;
    String foo;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_scaned_contact);

        button = (Button) findViewById(R.id.button);
        editText = (EditText) findViewById(R.id.editText);

        foo = getIntent().getStringExtra("newName");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String temp1 = getIntent().getStringExtra("newName");

                Intent intent = new Intent();


              //  intent.putExtra(QR_CODE_KEY,foo);

                intent.putExtra("messenger",editText.getText().toString()+"<...>"+temp1);
                setResult(Activity.RESULT_OK,intent);
                finish();




            }
        });





    }





}
