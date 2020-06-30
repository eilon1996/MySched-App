package com.eilon.workschedule;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.eilon.App;
import com.google.gson.Gson;

public class MySched extends AppCompatActivity {


    SharedPreferences sp;
    SharedPreferences.Editor edit;
    DatabaseHelper databaseHelper;
    Gson gson;
    Employee user;
    Branch branch;
    Company company;
    int manager;

    Employee[] allEmployees;
    Branch[] allBranches;
    int numOfShifts;

    String[][] sched;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_sched);

        sp = getSharedPreferences("key", 0);
        edit = sp.edit();
        gson = new Gson();
        user = gson.fromJson(sp.getString("user", null), Employee.class);
        databaseHelper = new DatabaseHelper(this);
        databaseHelper.close();
        company = gson.fromJson(sp.getString("company", null), Company.class);
        branch = gson.fromJson(sp.getString("branch", null), Branch.class);
        //  branch.setOrders(null);

        manager = user.getManger();
        if (manager > 0) {
            allEmployees = branch.getAllEmployees();
            if (manager > 1) {
                allBranches = company.getAllBranches();
            }
        }

        createNavigationBar();

        setShiftsName();
        FillSched fillSched = new FillSched();
        fillSched.run();

        Simplify simplify = new Simplify();
        TextView time = findViewById(R.id.date);
        time.setText(simplify.getThisWeekDate());


        String comments = "";
        String temp = user.getThisWeekManagerComments();
        if (temp != null) {
            String[] managerComments = temp.split("\n");
            for (int i = 0; i < managerComments.length; i++) {
                comments += simplify.getShiftName(managerComments[i].substring(0, 2) + managerComments[i].substring(2));
            }
        }

        comments += branch.getManagerCommentsThisWeek();

        if (comments.length() > 0) {
            TextView managerCommentsTV = findViewById(R.id.managerComments);
            managerCommentsTV.setText("הערות" + "\n" + comments);
        }
    }


    private void setShiftsName() {
        String[] daysName = branch.getDaysName();
        if (daysName != null) {
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
        if (shiftsName.length > 1) {
            shiftNameTV.setText(shiftsName[1]);
            shiftNameTV = findViewById(R.id.shiftName3);
            if (shiftsName.length > 2) {
                shiftNameTV.setText(shiftsName[2]);
            } else
                findViewById(R.id.row3).setVisibility(View.GONE);

        } else {
            findViewById(R.id.row2).setVisibility(View.GONE);
            findViewById(R.id.row3).setVisibility(View.GONE);
        }
    }

    class FillSched implements Runnable {
        @Override
        public void run() {
            for (int shift = 0; shift < numOfShifts; shift++) {
                int biggestLayoutPlace = 0;
                int biggestLayoutSize = 0;
                for (int day = 0; day < 7; day++) {
                    String shiftName = "shift" + (shift * 7 + day);
                    int shiftId = getResources().getIdentifier(shiftName, "id", getPackageName());
                    LinearLayout layout = findViewById(shiftId);
                    String[] names = sched[shift][day].split(",");
                    for (int i = 0; i < names.length; i++) {
                        TextView textView = new TextView(MySched.this);
                        textView.setText(names[i]);
                        layout.addView(textView);
                    }
                    if (sched[shift][day] != null && sched[shift][day].split(",").length > biggestLayoutSize) {
                        biggestLayoutSize = sched[shift][day].split(",").length;
                        biggestLayoutPlace = day;
                    }
                }

                if(biggestLayoutSize > 0) {
                    String shiftName = "shift" + (shift * 7 + biggestLayoutPlace);
                    int shiftId = getResources().getIdentifier(shiftName, "id", getPackageName());
                    LinearLayout layout = findViewById(shiftId);
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
                    params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    layout.setLayoutParams(params);
                }
            }
        }
    }
/*
    class FillSched implements Runnable{
        public FillSched(){}

        @Override
        public void run() {
            sched = gson.fromJson(sp.getString("sched", null),String[][].class);
            if(sched == null){
                sched = branch.getThisWeek();
                if(sched == null){
                    findViewById(R.id.noSched).setVisibility(View.VISIBLE);
                    return;
                }else {
                    edit.putString("sched",gson.toJson(sched)).apply();
                }
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
                    textView.setTextSize(12);
                }
            }
        }
    }

 */

    public void createNavigationBar(){
        TextView setSched = findViewById(R.id.setSched);
        if(manager>0) {
            setSched.setVisibility(View.VISIBLE);
            setSched.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MySched.this, ManagerSetSched.class);
                    startActivity(intent);
                }
            });
        }
        TextView mySched = findViewById(R.id.mySched);
        mySched.setBackground(ContextCompat.getDrawable(App.getContext(), R.drawable.shape_round_dark));

        TextView fillMySched = findViewById(R.id.fillMySched);
        TextView nextWeek = findViewById(R.id.nextWeek);
        if (branch.getSchedReady()) {
            fillMySched.setVisibility(View.GONE);
            nextWeek.setVisibility(View.VISIBLE);
            nextWeek.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MySched.this, MySchedNextWeek.class);
                    startActivity(intent);
                }
            });
        } else {
            fillMySched.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MySched.this, FillPossibleShifts.class);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        if(manager == 0){
            menu.getItem(2).setVisible(false);
            menu.getItem(3).setVisible(false);
            menu.getItem(4).setVisible(false);
            menu.getItem(5).setVisible(false);
        }
         if(manager == 1){
            menu.getItem(5).setVisible(true);
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

                לפתוח דיאלוג בשביל לבחור סניף או עובד
*/
            case  R.id.editEmployees:
                if(manager >0){
                    final AlertDialog.Builder alert = new AlertDialog.Builder(this);
                    LayoutInflater inflater = getLayoutInflater();
                    final View layout = inflater.inflate(R.layout.dialog_radio_group, null);
                    alert.setView(layout);
                    final RadioGroup radioGroup = (RadioGroup)((ScrollView)((LinearLayout)layout).getChildAt(0)).getChildAt(0);
                    for(int i = 0; i < allEmployees.length;i++){
                        RadioButton radioButton = new RadioButton(App.getContext());
                        radioButton.setText(allEmployees[i].getFullNameOneLine());
                        radioGroup.addView(radioButton);
                    }
                    alert.setTitle("ערוך משתמש");
                    alert.setPositiveButton("בחר", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int checkedId = radioGroup.getCheckedRadioButtonId();
                            int index = radioGroup.indexOfChild(findViewById(checkedId));
                            if(index>=0) {
                                edit.putString("editEmployee", gson.toJson(allEmployees[index])).apply();
                                Intent intent = new Intent(MySched.this, EditEmployee.class);
                                startActivity(intent);
                            } Toast.makeText(MySched.this,"בחר עובד", Toast.LENGTH_SHORT);
                        }
                    });
                    alert.setNegativeButton("בטל", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    alert.create();
                    alert.show();
                }

            case R.id.editBranch:
                intent = new Intent(MySched.this, EditBranch.class);
                startActivity(intent);

            case R.id.changeBranch:
                if(manager >1){
                final AlertDialog.Builder alert = new AlertDialog.Builder(this);
                LayoutInflater inflater = getLayoutInflater();
                final View layout = inflater.inflate(R.layout.dialog_radio_group, null);
                alert.setView(layout);
                final RadioGroup radioGroup = (RadioGroup)((ScrollView)((LinearLayout)layout).getChildAt(0)).getChildAt(0);
                final String [] branchesName = company.getAllBranchesAddresses();
                for(int i = 0; i < branchesName.length;i++){
                    RadioButton radioButton = new RadioButton(App.getContext());
                    radioButton.setText(branchesName[i]);
                    radioGroup.addView(radioButton);
                }
                alert.setTitle("החלף סניף");
                alert.setPositiveButton("בחר", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int checkedId = radioGroup.getCheckedRadioButtonId();
                        int index = radioGroup.indexOfChild(findViewById(checkedId));
                        if(index>=0) {
                            edit.putString("branch", gson.toJson(databaseHelper.getBranch(company.getComapnyName(),branchesName[index]))).apply();
                            databaseHelper.close();
                            Intent intent = new Intent(MySched.this, MySched.class);
                            startActivity(intent);
                        } Toast.makeText(MySched.this,"בחר סניף", Toast.LENGTH_SHORT);
                    }
                });
                alert.setNegativeButton("בטל", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                alert.create();
                alert.show();
            }
            case R.id.logout:
                edit.putString("user",null).apply();
                edit.putString("branch",null).apply();
                edit.putString("company",null).apply();
                intent = new Intent(MySched.this, Login.class);
                startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

}