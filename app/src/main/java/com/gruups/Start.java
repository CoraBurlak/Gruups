package com.gruups;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class Start extends Activity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_menu);
    }

    public void presenterButton(View view) {
         Intent send = new Intent(Start.this, Receive.class);
         startActivity(send);
    }

    public void audienceButton(View view) {
        Intent send = new Intent(Start.this, IPEnter.class);
        startActivity(send);
    }
}
