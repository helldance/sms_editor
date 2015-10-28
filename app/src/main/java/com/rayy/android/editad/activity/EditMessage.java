/**
 * Copyright @2010 Rayy.
 * EditMessage.java
 */
package com.rayy.android.editad.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.rayy.android.editad.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author rayy
 * @date Jan 20, 2011
 */
public class EditMessage extends Activity {

    private EditText sender, body, date;
    private Button save;
    private String _id;
    private int key;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit);

        Bundle bundle = getIntent().getExtras();

        sender = (EditText) findViewById(R.id.sender);
        date = (EditText) findViewById(R.id.send_date);
        body = (EditText) findViewById(R.id.body);

        sender.setText(bundle.getString("sender"));
        date.setText(bundle.getString("date"));
        body.setText(bundle.getString("body"));
        _id = bundle.getString("_id");
        key = bundle.getInt("key");

        TextView type = (TextView) findViewById(R.id.type);
        String s_type = bundle.getString("type");
        if (!s_type.equalsIgnoreCase("1"))
            type.setText(getResources().getString(R.string.to));

        save = (Button) findViewById(R.id.save);
        save.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                saveMessage();
            }

        });
    }

    protected void saveMessage() {
        // TODO Auto-generated method stub
        Uri uri = Uri.parse("content://sms");

        ContentValues cv = new ContentValues();

        cv.put("body", body.getText().toString());
        cv.put("address", sender.getText().toString());

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy");
        try {
            Date calldate = sdf.parse(date.getText().toString());
            Log.v("TAG", calldate.getTime() + "");
            cv.put("date", calldate.getTime());
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //Log.v("TAG", "WOCAONIMA"+ read.getCheckedRadioButtonId() + " " + type.getCheckedRadioButtonId());

        getContentResolver().update(uri, cv, "_id = ?", new String[]{_id});

        Toast.makeText(this, "Message is updated", Toast.LENGTH_SHORT).show();

        //setResult(RESULT_OK);
        setResult(key);

        this.finish();

        //startActivity(new Intent(this, LoadMessage.class));
    }
}
