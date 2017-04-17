package com.pokemonshowdown.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.pokemonshowdown.R;
import com.pokemonshowdown.application.MyApplication;


public class WelcomeActivity extends BaseActivity {

    // Need to update server array in strings.xml as well
    public static final String[] SERVER_ADDRESSES = {
            "ws://sim.smogon.com:8000/showdown/websocket",
            "ws://sim.smogon.com:8001/showdown/websocket"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Spinner spinner = (Spinner) findViewById(R.id.server_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.server, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > SERVER_ADDRESSES.length - 1) {
                    position = 0;
                }
                MyApplication.getMyApplication().setServerAddress(SERVER_ADDRESSES[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                MyApplication.getMyApplication().setServerAddress(SERVER_ADDRESSES[0]);
            }
        });

        findViewById(R.id.launch_application).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, ContainerActivity.class));
            }
        });
    }

}
