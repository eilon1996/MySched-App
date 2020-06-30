package com.eilon.workschedule;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

public class RequiredSched extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_edit_text);

        //restore the connected user
        SharedPreferences sp = getSharedPreferences("key", 0);
        Gson gson = new Gson();
        String json = sp.getString("employee0", "");
        Employee user = gson.fromJson(json, Employee.class);
        final Branch branch = user.getBranch();
        final int numOfShifts = 0;
        switch (numOfShifts) {
            case 1:
                findViewById(R.id.n2).setVisibility(View.INVISIBLE);
                findViewById(R.id.n3).setVisibility(View.INVISIBLE);
                break;
            case 2:
                findViewById(R.id.n3).setVisibility(View.INVISIBLE);
                break;
        }
        // initialized acording to the required schedual
        int[][] layout = branch.getSchedRequired();
        for (int i = 0; i < 7 * numOfShifts; i++) {
            int id = getResources().getIdentifier("shift" + i, "drawable", RequiredSched.this.getPackageName());
            EditText editText = findViewById(id);
            editText.setText(Integer.toString(layout[1 / 7][i % 7]));
        }
            findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int[][] sched = new int[numOfShifts][7];
                    for (int i = 0; i < 7 * numOfShifts; i++) {
                        int id = getResources().getIdentifier("shift" + i, "drawable", RequiredSched.this.getPackageName());
                        EditText editText = findViewById(id);
                        int n;
                        boolean success = true;
                        try {
                            Integer.valueOf(editText.getText().toString());
                        } catch (Exception e) {
                            Toast.makeText(RequiredSched.this, "ניתן להכניס רק מספרים", Toast.LENGTH_SHORT).show();
                            success = false;
                        }
                        if (success)
                            sched[1 / 7][i % 7] = Integer.valueOf(editText.getText().toString());
                        else return;
                    }
                    branch.setSchedRequired(sched);
                    Toast.makeText(RequiredSched.this, "הסידור נשמר בהצלחה", Toast.LENGTH_SHORT).show();
                }
            });
        findViewById(R.id.comment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RequiredSched.this, RequiredSched.class);
                startActivity(intent);
            }
        });
    }
}
