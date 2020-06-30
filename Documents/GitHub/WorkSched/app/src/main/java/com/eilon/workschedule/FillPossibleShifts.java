package com.eilon.workschedule;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.eilon.App;
import com.google.gson.Gson;

public class FillPossibleShifts extends AppCompatActivity {

    Employee user;
    Branch branch;
    Company company;
    int numOfShifts;
    boolean [][] possibleShifts;
    Gson gson;
    LinearLayout tableButton;

    SharedPreferences sp;
    SharedPreferences.Editor edit;
    DatabaseHelper databaseHelper;
    int manager;
    String[] allEmployeesName;
    int numOfOriginalEmployees;
    int numOfAllEmployees;
    int [][] scheduleRequired;

    TextView fillMySched;
    TextView nextWeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fill_my_sched);
        databaseHelper = new DatabaseHelper(this);
        sp = getSharedPreferences("key", 0);
        edit = sp.edit();
        gson = new Gson();
        user = gson.fromJson(sp.getString("user", null), Employee.class);
        company = gson.fromJson(sp.getString("company", null), Company.class);
        branch = gson.fromJson(sp.getString("branch", null), Branch.class);

        scheduleRequired = branch.getSchedRequired();
        numOfShifts = company.getNumOfShifts();
        manager = user.getManger();
        allEmployeesName = branch.getAllEmployeesName();
        numOfOriginalEmployees = allEmployeesName.length;
        numOfAllEmployees = numOfOriginalEmployees;
        fillMySched = findViewById(R.id.fillMySched);
        nextWeek = findViewById(R.id.nextWeek);
        createNavigationBar();

        SharedPreferences sp = getSharedPreferences("key", 0);
        gson = new Gson();
        user = gson.fromJson(sp.getString("user",null),Employee.class);
        branch = gson.fromJson(sp.getString("branch",null),Branch.class);
        company = gson.fromJson(sp.getString("company",null),Company.class);

        numOfShifts = company.getNumOfShifts();
        possibleShifts = user.getPossibleShifts();
        if(possibleShifts == null) {
            possibleShifts = new boolean[numOfShifts][7];
        }

        SetTable setTable = new SetTable();
        setTable.run();

        setShiftsName();

        Simplify simplify = new Simplify();
        String date = simplify.getNextWeekDate();
        TextView dateTV = findViewById(R.id.date);
        dateTV.setText(date);

        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.setPossibleShifts(possibleShifts);
                EditText comment = findViewById(R.id.comment);
                if(comment.getText().length() != 0)
                    branch.addEmployeesComments(user.getFullNameOneLine(), comment.getText().toString());
                branch.setAssignedShifts(user.getIndexInBranch());
                Toast.makeText(FillPossibleShifts.this, "משמרות הוגשו בהצלחה", Toast.LENGTH_SHORT).show();
                edit.putString("user",gson.toJson(databaseHelper.getEmployee(user.getEmail()))).apply();
                edit.putString("branch",gson.toJson(databaseHelper.getBranch(company.getComapnyName(),branch.getBranchName()))).apply();
            }
        });

    }

    private void setShiftsName(){
        String [] daysName = branch.getDaysName();
        if(daysName != null) {
            for (int day = 0; day < 7; day++) {
                String shiftName = "day" + day;
                int shiftId = getResources().getIdentifier(shiftName, "id", getPackageName());
                TextView textView = findViewById(shiftId);
                textView.setText(daysName[day]);
            }
        }
        String[] shiftsName = branch.getShiftsName();
        TextView shiftNameTV = findViewById(R.id.shiftName1);
        shiftNameTV.setText(shiftsName[0]);
        shiftNameTV = findViewById(R.id.shiftName2);
        if(shiftsName.length > 1){
            shiftNameTV.setText(shiftsName[1]);
            shiftNameTV = findViewById(R.id.shiftName3);
            if(shiftsName.length > 2) {
                shiftNameTV.setText(shiftsName[2]);
            }
            else
                findViewById(R.id.row3).setVisibility(View.GONE);

        } else {
            findViewById(R.id.row2).setVisibility(View.GONE);
            findViewById(R.id.row3).setVisibility(View.GONE);
        }


        /*
        String[] shiftsName = branch.getShiftsName();
        TextView shiftNameTV = findViewById(R.id.shiftName1);
        shiftNameTV.setText(shiftsName[0]);
        shiftNameTV = findViewById(R.id.shiftName2);
        if(shiftsName.length > 1){
            shiftNameTV.setText(shiftsName[1]);
            shiftNameTV = findViewById(R.id.shiftName3);
            if(shiftsName.length > 2)
                shiftNameTV.setText(shiftsName[2]);
            else
                shiftNameTV.setVisibility(View.GONE);

        } else {
            shiftNameTV.setVisibility(View.GONE);
        }

         */
    }

    class SetTable implements Runnable{

        int darkGrey;
        int lightGrey;
        public SetTable(){
            this.darkGrey = R.color.PrimaryDarkGrey;
            this.lightGrey = R.color.PrimaryLightGrey;
        }
        @Override
        public void run() {
            setShiftsName();

            for (int shift = 0; shift < numOfShifts; shift++) {
                for (int day = 0; day < 7; day++) {
                    String shiftName = "shift" + (shift * 7 + day);
                    int shiftId = getResources().getIdentifier(shiftName, "id", getPackageName());
                    Button button = findViewById(shiftId);
                    if(possibleShifts[shift][day]){
                        button.setText("√");
                        button.setBackgroundColor(ContextCompat.getColor(FillPossibleShifts.this, R.color.PrimaryDarkGrey));
                        possibleShifts[shift][day] = true;
                    }

                    button.setOnClickListener(new View.OnClickListener() {
                        @SuppressLint("ResourceAsColor")
                        @Override
                        public void onClick(View view) {
                            Button button = (Button) view;
                            int shift = Integer.valueOf(String.valueOf(button.getContentDescription().charAt(0)));
                            int day = Integer.valueOf(String.valueOf(button.getContentDescription().charAt(1)));
                            if(! possibleShifts[shift][day]){
                                button.setText("√");
                                button.setBackgroundColor(ContextCompat.getColor(FillPossibleShifts.this, R.color.PrimaryDarkGrey));
                                possibleShifts[shift][day] = true;
                            } else{
                                button.setText("");
                                button.setBackgroundColor(ContextCompat.getColor(FillPossibleShifts.this, R.color.PrimaryLightGrey));
                                possibleShifts[shift][day] = false;
                            }
                        }
                    });
                }
            }
        }

        private void setShiftsName(){
            String[] shiftsName = branch.getShiftsName();
            TextView shiftNameTV = findViewById(R.id.shiftName1);
            shiftNameTV.setText(shiftsName[0]);
            shiftNameTV = findViewById(R.id.shiftName2);
            if(shiftsName.length > 1){
                shiftNameTV.setText(shiftsName[1]);
                shiftNameTV = findViewById(R.id.shiftName3);
                if(shiftsName.length > 2)
                    shiftNameTV.setText(shiftsName[2]);
                else
                    shiftNameTV.setVisibility(View.GONE);

            } else {
                shiftNameTV.setVisibility(View.GONE);
            }
        }
    }


    public void createNavigationBar() {

        fillMySched.setVisibility(View.VISIBLE);
        fillMySched.setBackground(ContextCompat.getDrawable(App.getContext(), R.drawable.shape_round_dark));

        TextView mySched = findViewById(R.id.mySched);
        mySched.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FillPossibleShifts.this, MySched.class);
                startActivity(intent);
            }
        });

        TextView setSched = findViewById(R.id.setSched);
        if(manager>0) {
            setSched.setVisibility(View.VISIBLE);
            setSched.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(FillPossibleShifts.this, ManagerSetSched.class);
                    startActivity(intent);
                }
            });
        }
    }

}