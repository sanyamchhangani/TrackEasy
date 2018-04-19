package com.example.akanshugupta.trackeasy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Spinner spinner;
    private Spinner floor;
    private int floor_no;
    private Button track;
    String option;
    View view;
    ArrayAdapter<CharSequence> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinner = (Spinner) findViewById(R.id.spinner);
        floor = (Spinner) findViewById(R.id.floor);
        track = (Button) findViewById(R.id.track);
        option = "select_building";

        adapter = ArrayAdapter.createFromResource(this,R.array.buildings_list,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //String item = parent.getItemAtPosition(position);
                if(position == 0){
                    option = "select_building";
                    floor.setVisibility(floor.INVISIBLE);
                }
                else if(position == 1){
                    option = "SIT";
                    floor.setVisibility(floor.VISIBLE);
                    ArrayAdapter<CharSequence> floor_adapter = ArrayAdapter.createFromResource(MainActivity.this,R.array.sit_floors,android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    floor.setAdapter(floor_adapter);
                    floor_no=0;
                    floor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if(position == 1){
                                floor_no = 1;
                            }
                            else if(position == 0){
                                floor_no = 0;
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        track.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(option == "select_building"){
                    Toast.makeText(MainActivity.this,"select building",Toast.LENGTH_SHORT).show();
                }
                else if(option.equals("SIT")) {
                    if(floor_no == 1){
                        Intent intent = new Intent(MainActivity.this,MapActivity.class);
                        intent.putExtra("map_name","sit_ground");
                        startActivity(intent);
                    }
                    else if(floor_no == 0){
                        Toast.makeText(MainActivity.this,"select floor",Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

    }
}
