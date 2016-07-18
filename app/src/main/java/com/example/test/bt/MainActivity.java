package com.example.test.bt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.test.bt.model.DataManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DataManager.getInstance();
    }
}
