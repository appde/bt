package com.example.test.bt;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.test.bt.model.DataManager;
import com.example.test.bt.presenter.BTPresenter;
import com.example.test.bt.presenter.MainBTPresenter;

public class MainActivity extends AppCompatActivity implements BTView {

    private BTPresenter btPresenter;
    private Button propButton;
    TextView propTextView;
    TextView dbTextView;
    ImageView propImageView;
    ImageView dbImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btPresenter = new MainBTPresenter();
        btPresenter.attachView(this);

        propTextView = (TextView) findViewById(R.id.properties_text_view);

        propButton = (Button) findViewById(R.id.properties_button);
        propButton.setOnClickListener(view -> btPresenter.getProperties());

        dbTextView = (TextView) findViewById(R.id.db_record_text_view);

        Button dbButton = (Button) findViewById(R.id.db_record_button);
        dbButton.setOnClickListener(view -> btPresenter.writeDBRecord(1, 2, "db write"));


        propImageView = (ImageView) findViewById(R.id.properties_imageView);
        dbImageView = (ImageView) findViewById(R.id.db_record_imageView);

        DataManager.getInstance();
    }

    @Override
    public synchronized void updateProperties(String properties) {
        propTextView.setText(properties);
    }

    @Override
    public synchronized void indicateProperties(boolean isQueueOk) {
        if (isQueueOk) {
            propImageView.setBackgroundResource(android.R.color.holo_green_light);
        } else {
            propImageView.setBackgroundResource(android.R.color.holo_orange_light);
        }
    }

    @Override
    public synchronized void updateWriteDBAnswer(String properties) {
        dbTextView.setText(properties);
    }

    @Override
    public synchronized void indicateWriteDBAnswer(boolean isQueueOk) {
        if (isQueueOk) {
            dbImageView.setBackgroundResource(android.R.color.holo_green_light);
        } else {
            dbImageView.setBackgroundResource(android.R.color.holo_orange_light);
        }
    }


}
