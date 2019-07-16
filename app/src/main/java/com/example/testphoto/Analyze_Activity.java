package com.example.testphoto;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

public class Analyze_Activity extends AppCompatActivity {

    private ImageView processed_img;
    private ImageButton back_btn;
    private ImageView confirm_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze_);

        processed_img = findViewById(R.id.processed_img);
        back_btn = findViewById(R.id.back_btn);
        confirm_btn = findViewById(R.id.confirm_btn);





    }
}
