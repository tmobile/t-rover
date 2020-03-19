package com.twofuse.trover;


import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;

/**
 * Created by David B. Moffett on 5/29/13.
 */

public class SplashPageActivity extends Activity implements View.OnClickListener {

    private CountDownTimer countDownTimer;

    private void setupTimer() {

        countDownTimer = new CountDownTimer(5000,1000) {

            public void onTick (long millisUntilFinished){
                if(millisUntilFinished < 4000){
                    this.cancel();
                    Intent actIntent = new Intent(SplashPageActivity.this, MainActivity.class);
                    startActivity(actIntent);
                    SplashPageActivity.this.finish();
                    return;
                }
            }

            public void onFinish() {
                Intent actIntent = new Intent(SplashPageActivity.this, MainActivity.class);
                startActivity(actIntent);
                SplashPageActivity.this.finish();
                return;
            }

        }.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_page);
        View view = findViewById(R.id.splash_page);
        view.setOnClickListener(this);
        getActionBar().hide();
        IntentFilter filter = new IntentFilter();
    }


    @Override
    public void onResume() {
        super.onResume();
        setupTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        countDownTimer = null;
    }


    @Override
    public void onClick(View view) {
        if(!SplashPageActivity.this.isFinishing()){
            Intent intent = new Intent(SplashPageActivity.this, MainActivity.class);
            SplashPageActivity.this.startActivity(intent);
            // finish this splash screen
            SplashPageActivity.this.finish();
        }
    }

}
