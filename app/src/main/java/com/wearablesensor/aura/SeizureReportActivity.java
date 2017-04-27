package com.wearablesensor.aura;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by alyson on 25/04/17.
 */

public class SeizureReportActivity extends AppCompatActivity {
    private final static String TAG = SeizureReportActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_seizure_report);
    }
}
