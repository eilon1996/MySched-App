package com.eilon.workschedule;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.eilon.App;
import com.google.gson.Gson;

//Login activity
public class Login extends AppCompatActivity {

    private SharedPreferences sp;
    private Gson gson;
    private DatabaseHelper db;
    private SharedPreferences.Editor editor;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);
        setContentView(R.layout.login);

        db = new DatabaseHelper(this);
        sp = getSharedPreferences("key", 0);
        gson = new Gson();
        editor = sp.edit();
        if(sp.getString("user",null) != null){
            AutoConnect autoConnect = new AutoConnect();
            autoConnect.run();
        }

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Connect connect = new Connect();
                connect.run();
            }
        });

        findViewById(R.id.newEmployee).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.remove("branchName").apply();
                editor.remove("companyName").apply();
                Intent intent = new Intent(Login.this, RegistrateEmployee.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.newBranch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.remove("branchName").apply();
                editor.remove("companyName").apply();
                Intent intent = new Intent(Login.this, RegistrateBranch.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.newCompany).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.remove("branchName").apply();
                editor.remove("companyName").apply();
                Intent intent = new Intent(Login.this, RegistrateCompany.class);
                startActivity(intent);
            }
        });

        DatabaseHelper databaseHelper = new DatabaseHelper(App.getContext());
        TextView allEmployees = findViewById(R.id.allEmployees);
        allEmployees.setText(databaseHelper.getAllEmployeesDetails());
    }

    class AutoConnect implements Runnable{
        @Override
        public void run() {
            Employee user = gson.fromJson(sp.getString("user",null),Employee.class);
            editor.putString("user",gson.toJson(db.getEmployee(user.getEmail()))).apply();
            editor.putString("branch",gson.toJson(db.getBranch(user.getCompanyName(),user.getBranchName()))).apply();
            editor.putString("company",gson.toJson(db.getCompany(user.getCompanyName()))).apply();

            Intent intent = new Intent(Login.this, MySched.class);
            startActivity(intent);
        }
    }

    class Connect implements Runnable{
        @Override
        public void run() {
            EditText emailET = findViewById(R.id.email);
            String email = emailET.getText().toString();
            EditText password = findViewById(R.id.password);
            if (email.length() != 0) {
                email = email.toLowerCase();
                Employee user = db.getEmployee(email);
                db.close();
                if (user == null) {
                    findViewById(R.id.wrongEmail).setVisibility(View.VISIBLE);
                    findViewById(R.id.wrongPassword).setVisibility(View.INVISIBLE);
                } else if (user.getPassword().equals(password.getText().toString())) {

                    editor.putString("user", gson.toJson(user)).apply();
                    editor.putString("branch", gson.toJson(user.getBranch())).apply();
                    editor.putString("company", gson.toJson(user.getCompany())).apply();

                    Intent intent = new Intent(Login.this, MySchedLastWeek.class);
                    startActivity(intent);
                } else {
                    findViewById(R.id.wrongEmail).setVisibility(View.INVISIBLE);
                    findViewById(R.id.wrongPassword).setVisibility(View.VISIBLE);

                }
            }
        }
    }
}