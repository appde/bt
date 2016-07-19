package com.example.test.bt;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.example.test.bt.model.DataManager;
import com.example.test.bt.model.interchange.GetPropertiesCommand;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button getPropButton = (Button) findViewById(R.id.getProperties);
        getPropButton.setOnClickListener(view -> DataManager.getInstance()
                .send(new GetPropertiesCommand())
                .subscribe());

        Button getPropButton2 = (Button) findViewById(R.id.getProperties2);
        getPropButton.setOnClickListener(view -> DataManager.getInstance().send(new GetPropertiesCommand()).subscribe());

        DataManager.getInstance();
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

}
