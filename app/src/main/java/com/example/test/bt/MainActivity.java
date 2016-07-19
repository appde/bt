package com.example.test.bt;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.test.bt.model.DataManager;
import com.example.test.bt.presenter.BTPresenter;
import com.example.test.bt.presenter.MainBTPresenter;

public class MainActivity extends AppCompatActivity implements BTView {

    private BTPresenter btPresenter;
    private Button propButton;
    TextView propTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btPresenter = new MainBTPresenter();
        btPresenter.attachView(this);

        propTextView = (TextView) findViewById(R.id.properties_text_view);


        propButton = (Button) findViewById(R.id.properties_button);
        propButton.setOnClickListener(view -> btPresenter.getProperties());

        Button getPropButton2 = (Button) findViewById(R.id.getProperties2);

        DataManager.getInstance();
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    public void setProperties(String properties) {
        propTextView.setText(properties);
    }
}
