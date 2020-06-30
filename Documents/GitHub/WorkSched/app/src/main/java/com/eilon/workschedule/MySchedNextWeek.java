package com.eilon.workschedule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.eilon.App;
import com.google.gson.Gson;

public class MySchedNextWeek extends AppCompatActivity {


    SharedPreferences sp;
    SharedPreferences.Editor edit;
    DatabaseHelper databaseHelper;
    Gson gson;
    Employee user;
    Branch branch;
    Company company;
    int manager;

    Employee [] allEmployees;
    Branch [] allBranches;

    String[][] sched;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_sched);

        sp = getSharedPreferences("key", 0);
        edit = sp.edit();
        gson = new Gson();
        user = gson.fromJson(sp.getString("user", null), Employee.class);
        if (user == null) {
            Intent intent = new Intent(this, Login.class);
            Toast.makeText(this, "לא נמצא משתמש הכנס מחדש", Toast.LENGTH_LONG);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            startActivity(intent);
        }

        databaseHelper = new DatabaseHelper(this);
        company = gson.fromJson(sp.getString("company", null), Company.class);
        branch = gson.fromJson(sp.getString("branch", null), Branch.class);
        //  branch.setOrders(null);
        databaseHelper = new DatabaseHelper(this);

        manager = user.getManger();
        if(manager>0){
            allEmployees = branch.getAllEmployees();
            if(manager>1){
                allBranches = company.getAllBranches();
            }
        }

        createNavigationBar();

        FillSched fillSched = new FillSched();
        fillSched.run();

        Simplify simplify = new Simplify();
        TextView time = findViewById(R.id.date);
        time.setText(simplify.getNextWeekDate());

        setShiftsName();

        String comments = "";
        String temp = user.getNextWeekManagerComments();
        if(temp != null) {
            String[] managerComments = temp.split("\n");
            if (managerComments != null) {
                for (int i = 0; i < managerComments.length; i++) {
                    comments += simplify.getShiftName(managerComments[i].substring(0, 2) + managerComments[i].substring(2));
                }
            }
        }
        //צריך לשנות לשבוע הנוכחי
        comments += branch.getManagerCommentsNextWeek();

        if(comments.length() >0) {
            TextView managerCommentsTV = findViewById(R.id.managerComments);
            managerCommentsTV.setText("הערות"+"\n"+comments);
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

    class FillSched implements Runnable{
        public FillSched(){}

        @Override
        public void run() {
            sched = branch.getNextWeek();///next/this/last
            if(sched == null){
                findViewById(R.id.noSched).setVisibility(View.VISIBLE);
                return;
            }
            findViewById(R.id.noSched).setVisibility(View.GONE);
            int numOfShifts = company.getNumOfShifts();
            databaseHelper.close();


            for (int shift = 0; shift < numOfShifts; shift++) {
                for (int day = 0; day < 7; day++) {
                    String shiftName = "shift" + (shift * 7 + day);
                    int shiftId = getResources().getIdentifier(shiftName, "id", getPackageName());
                    TextView textView = findViewById(shiftId);
                    textView.setText(sched[shift][day]);
                }
            }
        }
    }

    public void createNavigationBar(){
        TextView setSched = findViewById(R.id.setSched);
        if(manager>0) {
            setSched.setVisibility(View.VISIBLE);
            setSched.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MySchedNextWeek.this, ManagerSetSched.class);
                    startActivity(intent);
                }
            });
        }
        TextView mySched = findViewById(R.id.mySched);
        mySched.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MySchedNextWeek.this, MySchedLastWeek.class);
                startActivity(intent);
            }
        });

        TextView nextWeek = findViewById(R.id.nextWeek);
        nextWeek.setBackground(ContextCompat.getDrawable(App.getContext(), R.drawable.shape_round_dark));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        Menu subm = menu.getItem(4).getSubMenu(); // get my MenuItem with placeholder submenu
        subm.clear(); // delete place holder

        for(int i = 0;i < allEmployees.length; i++)
        {
            subm.add(0, i, i,allEmployees[i].getFullNameOneLine()); // משתנה שני הוא ה ID
        }


        if(manager == 0){
            // לא עובד
            menu.getItem(1).setVisible(false);
            menu.getItem(2).setVisible(false);
        }
        if(manager == 1){
            menu.getItem(2).setVisible(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            /*
            case R.id.editUser:
                edit.putString("editedEmployee", user.getEmail()).apply();
                intent = new Intent(MySchedLastWeek.this, EditEmployee.class);
                Toast.makeText(this, "edit user", Toast.LENGTH_SHORT).show();
                 startActivity(intent);
                break;
            case R.id.lastWeek:
                intent = new Intent(MySchedLastWeek.this, LastWeekSched.class);
                Toast.makeText(this, "last week", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            case R.id.salary:
                intent = new Intent(MySchedLastWeek.this, ExpectedSalary.class);
                Toast.makeText(this, "salary", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            case  R.id.editEmployees:
                edit.putString("editedEmployee", user.getEmail()).apply();
                intent = new Intent(MySchedLastWeek.this, ExpectedSalary.class);
                Toast.makeText(this, "edit employee", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            case R.id.editBranch:
                intent = new Intent(MySchedLastWeek.this, RegistrateBranch.class);
                Toast.makeText(this, "edit branch", Toast.LENGTH_SHORT).show();
                startActivity(intent);
                */
            case R.id.logout:
                edit.putString("user",null).apply();
                intent = new Intent(MySchedNextWeek.this, Login.class);
                startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

}