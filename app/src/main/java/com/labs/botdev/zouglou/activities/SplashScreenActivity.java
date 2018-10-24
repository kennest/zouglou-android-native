package com.labs.botdev.zouglou.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import com.labs.botdev.zouglou.R;
import com.mikhaellopez.circularfillableloaders.CircularFillableLoaders;

import java.util.Locale;

public class SplashScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        CircularFillableLoaders circularFillableLoaders = (CircularFillableLoaders) findViewById(R.id.circularFillableLoaders);
        TextView remain = findViewById(R.id.remain_time);
        new CountDownTimer(10000, 150) {
            public void onTick(long millisUntilFinished) {
                Log.e("millisUntilFinished",""+millisUntilFinished);
                Log.e("millisUntilFinished/100",""+(millisUntilFinished/150));
                remain.setText(String.format(Locale.FRANCE, "%d %s", millisUntilFinished / 150,"%"));
// Set Progress
                circularFillableLoaders.setProgress((int) (millisUntilFinished / 150));
// Set Wave and Border Color
                //circularFillableLoaders.setColor(Color.RED);
// Set Border Width
// Set Wave Amplitude (between 0.00f and 0.10f)
                circularFillableLoaders.setAmplitudeRatio(0.08f);
            }

            public void onFinish() {
                remain.setText("OK!");
                Intent map = new Intent(SplashScreenActivity.this, MapActivity.class);
                startActivity(map);
                finish();
            }
        }.start();

    }
}
