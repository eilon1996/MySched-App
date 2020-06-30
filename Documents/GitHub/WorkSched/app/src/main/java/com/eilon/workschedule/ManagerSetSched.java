package com.eilon.workschedule;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.eilon.App;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ManagerSetSched extends AppCompatActivity {

    SharedPreferences sp;
    SharedPreferences.Editor edit;
    DatabaseHelper databaseHelper;
    Gson gson;
    Employee user;
    Branch branch;
    Company company;
    int manager;
    int numOfShifts;
    String[] allEmployeesName;
    int numOfOriginalEmployees;
    int numOfAllEmployees;
    boolean[][][] allPossibleShifts;
    Branch[] coOperateBranches;

    int[][] scheduleRequired;
    String[][][] orders;
    int[][] shorts;
    LinearLayout newEmployeesLL;
    Button createAllEmployeesOptions;
    LinearLayout allEmployeesOptions;

    TextView fillMySched;
    String[][] nextWeek;
    TextView nextWeekTextView;
    LinearLayout coOperateLL;
    LinearLayout coOperateButtons;
    String[][][][] coOperateOrders;
    boolean[][][][] coOperateAllPossibleShift;
    String[][] coOperateEmployeesName;
    Simplify simplify;

    // [branches length][employees length]
    String[][] employeesHelp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manager_set_sched);

        databaseHelper = new DatabaseHelper(this);
        sp = getSharedPreferences("key", 0);
        edit = sp.edit();
        gson = new Gson();
        user = gson.fromJson(sp.getString("user", null), Employee.class);
        company = gson.fromJson(sp.getString("company", null), Company.class);
        branch = gson.fromJson(sp.getString("branch", null), Branch.class);


        simplify = new Simplify();
        scheduleRequired = branch.getSchedRequired();
        numOfShifts = company.getNumOfShifts();
        manager = user.getManger();
        allEmployeesName = branch.getAllEmployeesName();
        numOfOriginalEmployees = allEmployeesName.length;
        numOfAllEmployees = numOfOriginalEmployees;
        fillMySched = findViewById(R.id.fillMySched);
        nextWeekTextView = findViewById(R.id.nextWeek);
        nextWeek = branch.getNextWeek();

        coOperateLL = findViewById(R.id.coOperateLL);
        coOperateButtons = findViewById(R.id.coOperateButtons);

        { /// fill employees name that did not assigned shifts
            boolean[] assignedShifts = branch.getAssignedShifts();
            String notAssignedShiftsName = "";
            for (int i = 0; i < numOfOriginalEmployees; i++) {
                if (!assignedShifts[i]) {
                    int index = allEmployeesName[i].indexOf("\n");
                    String name = allEmployeesName[i].substring(0, index) + " " + allEmployeesName[i].substring(index + 1);
                    notAssignedShiftsName += name + ", ";
                }
            }
            TextView notAssigned = findViewById(R.id.notAssign);
            if (notAssignedShiftsName.length() == 0) notAssigned.setText("כולם הגישו משמרות");
            else
                notAssigned.setText("לא הגישו משמרות: " + notAssignedShiftsName.substring(0, notAssignedShiftsName.length() - 2));
        }

        {
            TextView comments = findViewById(R.id.comments);
            comments.setText(branch.getEmployeesComments());
        }
        allPossibleShifts = branch.getAllPossibleShifts();
        String[][][] helpOther = branch.getHelpForOtherBranch();
        if (helpOther != null)
            for (int i = 0; i < numOfOriginalEmployees; i++) {
                if (helpOther[i] != null)
                    for (int shift = 0; shift < numOfShifts; shift++) {
                        for (int day = 0; day < 7; day++) {
                            if (helpOther[i][shift][day] != null) {
                                allPossibleShifts[i][shift][day] = false;
                            }
                        }
                    }
            }
        orders = branch.getOrders();
        if (orders == null) {
            orders = new String[numOfShifts][7][3];
            nextWeek = new String[numOfShifts][7];
            createNFillSched();
        } else {
            shorts = branch.getShortInEmployees();
            orders = branch.getOrders();
            FillAllShifts2 fillAllShifts = new FillAllShifts2();
            fillAllShifts.run();
            EditText comments = findViewById(R.id.managerComments);
            comments.setText(branch.getManagerCommentsNextWeek(), TextView.BufferType.EDITABLE);
        }

        setShiftsName();

        coOperateBranches = branch.getCoOperateBranches();
        if (coOperateBranches == null) employeesHelp = null;
        else employeesHelp = new String[coOperateBranches.length][];

        createNavigationBar();
        final Button submit = findViewById(R.id.submit);
        final Button unSubmit = findViewById(R.id.unSubmit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                branch.setSchedReady(true);
                submit.setVisibility(View.GONE);
                unSubmit.setVisibility(View.VISIBLE);
                Toast.makeText(ManagerSetSched.this, "הסידור עודכן בהצלחה", Toast.LENGTH_SHORT).show();

                fillMySched.setVisibility(View.GONE);
                nextWeekTextView.setVisibility(View.VISIBLE);
            }
        });
        findViewById(R.id.unSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit.setVisibility(View.VISIBLE);
                unSubmit.setVisibility(View.GONE);
                branch.setSchedReady(false);
                Toast.makeText(ManagerSetSched.this, "הסידור מוסתר כעט", Toast.LENGTH_SHORT).show();
                fillMySched.setVisibility(View.VISIBLE);
                nextWeekTextView.setVisibility(View.GONE);
            }
        });

        if (branch.getSchedReady()) {
            submit.setVisibility(View.GONE);
            unSubmit.setVisibility(View.VISIBLE);
            fillMySched.setVisibility(View.GONE);
            nextWeekTextView.setVisibility(View.VISIBLE);
        }

        createAllEmployeesOptions = findViewById(R.id.createAllEmployeesOptions);
        createAllEmployeesOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAllEmployeesOptions.setVisibility(View.GONE);
                FillAllOptions fillAllOptions = new FillAllOptions();
                fillAllOptions.run();
            }
        });

        findViewById(R.id.refreshSched).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(ManagerSetSched.this);
                alert.setMessage("ברגע שנוצר סידור חדש, הסידור הנוכחי ימחק");
                alert.setTitle("סידור חדש");
                alert.setPositiveButton("אישור", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        createNFillSched();
                    }
                })
                        .setNegativeButton("בטל", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                alert.create();
                alert.show();

            }
        });

        newEmployeesLL = findViewById(R.id.newEmployeeLL);
        findViewById(R.id.createNewEmployee).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] tempNames = branch.getTempNames();
                if (newEmployeesLL.getChildCount() == 0 && (tempNames != null && tempNames.length != 0)) {
                    ExistTempEmployee existTempEmployee = new ExistTempEmployee();
                    existTempEmployee.run();
                } else {
                    NewTempEmployee newEmployee = new NewTempEmployee();
                    newEmployee.run();
                }
            }
        });

        findViewById(R.id.createCoOperate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (coOperateBranches == null) {
                    LayoutInflater layoutInflater = getLayoutInflater();
                    final LinearLayout layout = (LinearLayout) layoutInflater.inflate(R.layout.dialog_empty, null);
                    CheckBox checkBox;
                    final Branch[] branches = company.getAllBranches();
                    for (int i = 0; i < branches.length; i++) {
                        if (!branches[i].equals(branch.getBranchName())) {
                            checkBox = new CheckBox(ManagerSetSched.this);
                            checkBox.setText(branches[i].getBranchName());
                            checkBox.setContentDescription(String.valueOf(i));
                            layout.addView(checkBox);
                        }
                    }
                    final AlertDialog.Builder alert = new AlertDialog.Builder(ManagerSetSched.this);
                    alert.setView(layout);
                    alert.setMessage("אין לך כרגע אף שיתוף פעולה \n בחר סניפים קרובים אליך שתוכלו לשתף עובדים ביחד");
                    alert.setTitle("צור שיתוף פעולה");
                    alert.setPositiveButton("אישור", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            CheckBox checkBox;
                            for (int j = 0; j < layout.getChildCount(); j++) {
                                checkBox = (CheckBox) layout.getChildAt(j);
                                if (checkBox.isChecked()) {
                                    branch.addCoOperateBranches(checkBox.getText().toString());
                                    branches[Integer.valueOf(String.valueOf(checkBox.getContentDescription()))]
                                            .addCoOperateBranches(branch.getBranchName());
                                }
                            }
                            edit.putString("branch", gson.toJson(branch));
                        }
                    })
                            .setNegativeButton("בטל", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                    alert.create();
                    alert.show();
                } else {
                    CoOperate coOperate = new CoOperate();
                    coOperate.run();
                }
            }
        });
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

    private void createNFillSched() {
        shorts = new int[numOfShifts][7];
        int employeesLoop = 0; // track the next user that need to be assigned for the shift to better distribute the shifts between the employees
        for (int shift = 0; shift < numOfShifts; shift++) {
            int biggestLayoutPlace = 0;
            int biggestLayoutSize = 0;
            for (int day = 0; day < 7; day++) {
                int found = 0; //count the number of employees that have been assigned to this shift
                orders[shift][day][0] = "";
                orders[shift][day][1] = "";
                orders[shift][day][2] = "";
                nextWeek[shift][day] = "";
                // to solve edge problem of 1 employee
                if (numOfAllEmployees == 1) {
                    if (allPossibleShifts[0][shift][day]) {
                        if (scheduleRequired[shift][day] > 0) {
                            orders[shift][day][0] = "0";
                            nextWeek[shift][day] = allEmployeesName[0];
                            found = 1;
                        } else orders[shift][day][1] = "0";
                    } else {
                        orders[shift][day][2] = "0";
                    }
                } else {
                    for (int i = 0; i < numOfAllEmployees && found < scheduleRequired[shift][day]; i++) {
                        if (allPossibleShifts[employeesLoop][shift][day]) {
                            if (employeesLoop >= numOfOriginalEmployees) {
                                orders[shift][day][0] += "," + allEmployeesName[employeesLoop];
                            } else {
                                orders[shift][day][0] += "," + employeesLoop;
                            }
                            nextWeek[shift][day] += "," + allEmployeesName[employeesLoop];
                            found++;
                        } else {
                            if (employeesLoop >= numOfOriginalEmployees) {
                                orders[shift][day][2] += "," + allEmployeesName[employeesLoop];
                            } else {
                                orders[shift][day][2] += "," + employeesLoop;
                            }
                        }
                        employeesLoop = (employeesLoop + 1) % numOfAllEmployees;
                    }
                    {
                        //to keep track who was the last to be assign we will use a temporary variable to fill the extra employees
                        int temp = employeesLoop;
                        while (found >= scheduleRequired[shift][day] && temp < numOfAllEmployees) {
                            int index; // 1 - can work 2 - cant work
                            if (allPossibleShifts[employeesLoop][shift][day])
                                index = 1;
                            else index = 2;
                            if (temp >= numOfOriginalEmployees)
                                orders[shift][day][index] += "," + allEmployeesName[temp];
                            else
                                orders[shift][day][index] += "," + temp;
                            temp++;
                        }
                        // delete the , at the beginning;
                        if (orders[shift][day][0] != null && orders[shift][day][0].length() > 0)
                            orders[shift][day][0] = orders[shift][day][0].substring(1);
                        if (orders[shift][day][1] != null && orders[shift][day][1].length() > 0)
                            orders[shift][day][1] = orders[shift][day][1].substring(1);
                        if (orders[shift][day][2] != null && orders[shift][day][2].length() > 0)
                            orders[shift][day][2] = orders[shift][day][2].substring(1);
                        if (nextWeek[shift][day] != null && nextWeek[shift][day].length() > 0)
                            nextWeek[shift][day] = nextWeek[shift][day].substring(1);
                    }

                }
              //  FillShift fillShift = new FillShift(shift, day);
                //fillShift.run();
                shorts[shift][day] = scheduleRequired[shift][day] - found;
                if (nextWeek[shift][day] != null && nextWeek[shift][day].split(",").length > biggestLayoutSize) {
                    biggestLayoutSize = nextWeek[shift][day].split(",").length;
                    biggestLayoutPlace = day;
                } //TODO
            }
            if (biggestLayoutSize > 0) {
                String shiftName = "shift" + (shift * 7 + biggestLayoutPlace);
                int shiftId = getResources().getIdentifier(shiftName, "id", getPackageName());
                LinearLayout layout = findViewById(shiftId);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
                params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                layout.setLayoutParams(params);
            }
        }
        branch.setOrders(orders);
        branch.setNextWeek(nextWeek);
        branch.setShortInEmployees(shorts);
        EditText comments = findViewById(R.id.managerComments);
        if (comments.getText().length() > 0)
            branch.setManagerComments(comments.getText().toString());
        edit.putString("branch", gson.toJson(branch)).apply();


        FillAllShifts2 fillAllShifts2 = new FillAllShifts2();
        fillAllShifts2.run();
    }

    class SetSchedWithOrders implements Runnable {
        @Override
        public void run() {
            //option in the future

        }
    }

    class ExistTempEmployee implements Runnable {
        @Override
        public void run() {
            boolean[][][] tempPossibleShifts = branch.getTempPossibleShifts();
            String[] tempNames = branch.getTempNames();

            NewTempEmployee newTempEmployee = new NewTempEmployee();
            LinearLayout indexChild;
            for (int i = 0; i < tempNames.length; i++) {
                newTempEmployee.run();
                indexChild = (LinearLayout) newEmployeesLL.getChildAt(i);
                indexChild.findViewById(R.id.addEmployee).setVisibility(View.GONE);
                indexChild.findViewById(R.id.updateEmployee).setVisibility(View.VISIBLE);
                indexChild.findViewById(R.id.deleteEmployee).setVisibility(View.VISIBLE);

                EditText name = indexChild.findViewById(R.id.name);
                name.setText(tempNames[i]);

                for(int j = numOfShifts+1; j<=3; j++){
                    String shiftName = "row" + j;
                    int shiftId = getResources().getIdentifier(shiftName, "id", getPackageName());
                    indexChild.findViewById(shiftId).setVisibility(View.GONE);
                }
                for (int shift = 0; shift < numOfShifts; shift++) {
                    for (int day = 0; day < 7; day++) {

                        String shiftName = "shift" + (shift * 7 + day);
                        int shiftId = getResources().getIdentifier(shiftName, "id", getPackageName());
                        Button button = indexChild.findViewById(shiftId);
                        if (tempPossibleShifts[i][shift][day]) {
                            button.setText("√");
                            button.setBackgroundColor(ContextCompat.getColor(ManagerSetSched.this, R.color.PrimaryDarkGrey));
                        } else {
                            button.setText("");
                            button.setBackgroundColor(ContextCompat.getColor(ManagerSetSched.this, R.color.PrimaryLightGrey));
                        }
                    }
                }
            }

            if (allEmployeesName == null) {
                allEmployeesName = tempNames;
                allPossibleShifts = tempPossibleShifts;
                numOfAllEmployees = tempNames.length;
            } else {
                numOfAllEmployees = tempNames.length + numOfOriginalEmployees;
                String[] newNames = new String[numOfAllEmployees];
                boolean[][][] newTempPossibleShift = new boolean[numOfAllEmployees][][];
                for (int i = 0; i < numOfOriginalEmployees; i++) {
                    newNames[i] = allEmployeesName[i];
                    newTempPossibleShift[i] = allPossibleShifts[i];
                }
                for (int i = 0; i < numOfAllEmployees - numOfOriginalEmployees; i++) {
                    newNames[i + numOfOriginalEmployees] = tempNames[i];
                    newTempPossibleShift[i + numOfOriginalEmployees] = tempPossibleShifts[i];
                }
                allPossibleShifts = newTempPossibleShift;
                allEmployeesName = newNames;
            }

        }
    }

    class NewTempEmployee implements Runnable {
        @Override
        public void run() {
            LayoutInflater layoutInflater = getLayoutInflater();
            LinearLayout layout1 = (LinearLayout) layoutInflater.inflate(R.layout.manager_add_employee, null);
            LinearLayout layout = layout1.findViewById(R.id.layout);
            for(int j = numOfShifts+1; j<=3; j++){
                String shiftName = "row" + j;
                int shiftId = getResources().getIdentifier(shiftName, "id", getPackageName());
                layout.findViewById(shiftId).setVisibility(View.GONE);
            }
            for (int shift = 0; shift < numOfShifts; shift++) {
                for (int day = 0; day < 7; day++) {
                    String shiftName = "shift" + (shift * 7 + day);
                    int shiftId = getResources().getIdentifier(shiftName, "id", getPackageName());
                    Button button = layout.findViewById(shiftId);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Button button = (Button) view;
                            if (button.getText() == null || button.getText().length() == 0) {
                                button.setText("√");
                                button.setBackgroundColor(ContextCompat.getColor(ManagerSetSched.this, R.color.PrimaryDarkGrey));
                            } else {
                                button.setText("");
                                button.setBackgroundColor(ContextCompat.getColor(ManagerSetSched.this, R.color.PrimaryLightGrey));
                            }
                        }
                    });
                }
            }
            layout1.removeView(layout);
            newEmployeesLL.addView(layout);

            layout.findViewById(R.id.addEmployee).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LinearLayout temp = (LinearLayout) view.getParent();
                    LinearLayout layout = (LinearLayout) temp.getParent();
                    EditText name = layout.findViewById(R.id.name);
                    if (name == null || name.getText().length() == 0) {
                        Toast.makeText(ManagerSetSched.this, "חייב לתת שם לכל עובד", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    view.setVisibility(View.GONE);
                    layout.findViewById(R.id.updateEmployee).setVisibility(View.VISIBLE);
                    layout.findViewById(R.id.deleteEmployee).setVisibility(View.VISIBLE);
                    if (allPossibleShifts == null) {
                        allPossibleShifts = new boolean[1][numOfShifts][7];
                        allEmployeesName = new String[1];
                        allEmployeesName[0] = name.getText().toString();
                    } else {
                        boolean[][][] tempPossibleShifts = new boolean[numOfAllEmployees + 1][numOfShifts][7];
                        String[] tempAllNames = new String[numOfAllEmployees + 1];
                        for (int i = 0; i < numOfAllEmployees; i++) {
                            tempPossibleShifts[i] = allPossibleShifts[i];
                            tempAllNames[i] = allEmployeesName[i];
                        }
                        allPossibleShifts = tempPossibleShifts;
                        allEmployeesName = tempAllNames;
                    }
                    numOfAllEmployees++;
                    edit.putString("branch", gson.toJson(branch)).apply();
                    Toast.makeText(ManagerSetSched.this, "עובד נוסף בהצלחה", Toast.LENGTH_SHORT).show();
                    ;
                    updateEmploee(layout);
                }
            });


            layout.findViewById(R.id.updateEmployee).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LinearLayout temp = (LinearLayout) view.getParent();
                    LinearLayout layout = (LinearLayout) temp.getParent();
                    EditText name = layout.findViewById(R.id.name);
                    if (name == null || name.getText().length() == 0) {
                        Toast.makeText(ManagerSetSched.this, "חייב לתת שם לכל עובד", Toast.LENGTH_SHORT).show();
                        ;
                        return;
                    }
                    Toast.makeText(ManagerSetSched.this, "עובד עודכן בהבצלחה", Toast.LENGTH_SHORT).show();

                    updateEmploee(layout);
                    //         branch.setTempEmployee(newEmployeesLL);
                    edit.putString("branch", gson.toJson(branch)).apply();
                }
            });


            layout.findViewById(R.id.deleteEmployee).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LinearLayout temp = (LinearLayout) view.getParent();
                    LinearLayout layout = (LinearLayout) temp.getParent();
                    int index = newEmployeesLL.indexOfChild(layout);
                    boolean[][][] tempPossibleShifts = new boolean[numOfAllEmployees - 1][numOfShifts][7];
                    String[] tempAllNames = new String[numOfAllEmployees - 1];
                    for (int i = 0; i < index; i++) {
                        tempPossibleShifts[i] = allPossibleShifts[i];
                        tempAllNames[i] = allEmployeesName[i];
                    }
                    for (int i = index; i < numOfAllEmployees - 1; i++) {
                        tempPossibleShifts[i] = allPossibleShifts[i + 1];
                        tempAllNames[i] = allEmployeesName[i + 1];
                    }
                    allPossibleShifts = tempPossibleShifts;
                    allEmployeesName = tempAllNames;
                    numOfAllEmployees--;
                    newEmployeesLL.removeView(layout);
                    branch.deleteTempEmployee(index);
                    edit.putString("branch", gson.toJson(branch)).apply();
                }
            });

        }

        private boolean updateEmploee(LinearLayout layout) {
            EditText name = layout.findViewById(R.id.name);
            int index = newEmployeesLL.indexOfChild(layout) + numOfOriginalEmployees;
            allEmployeesName[index] = name.getText().toString();
            for (int shift = 0; shift < numOfShifts; shift++) {
                for (int day = 0; day < 7; day++) {
                    String shiftName = "shift" + (shift * 7 + day);
                    int shiftId = getResources().getIdentifier(shiftName, "id", getPackageName());
                    Button button = layout.findViewById(shiftId);
                    if (button.getText() != null && button.getText().length() != 0) {
                        allPossibleShifts[index][shift][day] = true;
                    } else allPossibleShifts[index][shift][day] = false;
                }
            }
            branch.setTempEmployeesPossibleShifts(allPossibleShifts[index], index);
            branch.setTempEmployeesName(name.getText().toString(), index);
            edit.putString("branch", gson.toJson(branch)).apply();
            return true;
        }
    }

    class FillAllOptions implements Runnable {
        @Override
        public void run() {
            LayoutInflater layoutInflater = getLayoutInflater();
            allEmployeesOptions = findViewById(R.id.allEmployeesOptions);
            allEmployeesOptions.setVisibility(View.VISIBLE);
            if (allEmployeesOptions.getChildCount() > 0) return;

            for (int i = 0; i < numOfOriginalEmployees; i++) {
                TextView name = new TextView(ManagerSetSched.this);
                name.setText("\n" + allEmployeesName[i]);
                allEmployeesOptions.addView(name);
                LinearLayout tableButton = (LinearLayout) layoutInflater.inflate(R.layout.table_button, null);
                LinearLayout layout = tableButton.findViewById(R.id.layout);
                TableLayout tableLayout = (TableLayout) layout.getChildAt(0);

                for (int shift = 0; shift < numOfShifts; shift++) {
                    for (int day = 0; day < 7; day++) {
                        TableRow row = (TableRow) tableLayout.getChildAt(shift + 1);
                        TextView column = (TextView) row.getChildAt(6 - day);
                        if (allPossibleShifts[i][shift][day]) {
                            column.setText("√");
                            column.setBackgroundColor(ContextCompat.getColor(ManagerSetSched.this, R.color.PrimaryDarkGrey));

/*
                            String shiftName = "shift" + (shift * 7 + day);
                        int shiftId = tableButton.getResources().getIdentifier(shiftName, "id", getPackageName());
                        Button textView = findViewById(shiftId);
                            textView.setText("√");
                            textView.setBackgroundColor(ContextCompat.getColor(ManagerSetSched.this, R.color.PrimaryDarkGrey));
                       */
                        } else {
                            column.setBackgroundColor(ContextCompat.getColor(ManagerSetSched.this, R.color.PrimaryLightGrey));
                        }
                    }
                }

                tableButton.removeView(layout);
                allEmployeesOptions.addView(layout);

            }
            Button close = new Button(ManagerSetSched.this);
            close.setText("כווץ חזרה");
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    allEmployeesOptions.setVisibility(View.GONE);
                    createAllEmployeesOptions.setVisibility(View.VISIBLE);
                }
            });
            allEmployeesOptions.addView(close);
        }
    }

    class FillAllShifts implements Runnable {

        @Override
        public void run() {
            FillShift fillShift;
            for (int shift = 0; shift < numOfShifts; shift++) {
                int biggestLayoutPlace = 0;
                int biggestLayoutSize = 0;
                for (int day = 0; day < 7; day++) {
                    if (nextWeek[shift][day] != null && nextWeek[shift][day].split(",").length > biggestLayoutSize) {
                        biggestLayoutSize = nextWeek[shift][day].split(",").length;
                        biggestLayoutPlace = day;
                    }
                    fillShift = new FillShift(shift, day);
                    fillShift.run();
                }
                if (biggestLayoutSize > 0) {
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

    class FillAllShifts2 implements Runnable {

        @Override
        public void run() {
            LayoutInflater layoutInflater = getLayoutInflater();
            for (int shift = 0; shift < numOfShifts; shift++) {
                int biggestLayoutPlace = 0;
                int biggestLayoutSize = 0;
                for (int day = 0; day < 7; day++) {
                    int difference = shorts[shift][day];
                    String shiftName = "shift" + (shift * 7 + day);
                    int shiftId = getResources().getIdentifier(shiftName, "id", getPackageName());
                    LinearLayout layout = findViewById(shiftId);
                    layout.removeAllViews();
                    layout.getLayoutParams().height = LinearLayout.LayoutParams.MATCH_PARENT;
                    if (nextWeek[shift][day] != null) {
                        String[] names = nextWeek[shift][day].split(",");
                        if (names != null && !names[0].equals("")) {
                            for (int i = 0; i < names.length; i++) {
                                LinearLayout parent = (LinearLayout) layoutInflater.inflate(R.layout.text_view_names, null);
                                TextView textView = parent.findViewById(R.id.textView);
                                parent.removeView(textView);
                                textView.setText(names[i]);
                                layout.addView(textView);
                            }
                        }
                    }
                    if (difference == 1) {
                        LinearLayout parent = (LinearLayout) layoutInflater.inflate(R.layout.text_view_names, null);
                        TextView textView = parent.findViewById(R.id.textView);
                        parent.removeView(textView);
                        textView.setText("חסר עובד אחד");
                        layout.addView(textView);
                    } else if (difference > 1) {
                        LinearLayout parent = (LinearLayout) layoutInflater.inflate(R.layout.text_view_names, null);
                        TextView textView = parent.findViewById(R.id.textView);
                        parent.removeView(textView);
                        textView.setText("חסרים " + difference + " עובדים");
                        layout.addView(textView);
                    }
                    layout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            LinearLayout row = (LinearLayout) view.getParent();
                            int day = 6 - row.indexOfChild(view);
                            LinearLayout col = (LinearLayout) row.getParent();
                            int shift = col.indexOfChild(row) - 1;
                            edit.putString("shift", shift + "" + day).apply();
                            Set<String> set = new HashSet<>(Arrays.asList(orders[shift][day]));
                            edit.putStringSet("orders", set).apply();
                            edit.putString("user", gson.toJson(databaseHelper.getEmployee(user.getEmail()))).apply();
                            edit.putString("branch", gson.toJson(databaseHelper.getBranch(user.getCompanyName(), user.getBranchName()))).apply();
                            edit.putString("company", gson.toJson(databaseHelper.getCompany(user.getCompanyName()))).apply();

                            Intent intent = new Intent(ManagerSetSched.this, ManagerSetShift.class);
                            startActivity(intent);
                        }
                    });
                    if (biggestLayoutSize < layout.getChildCount()) {
                        biggestLayoutSize = layout.getChildCount();
                        biggestLayoutPlace = day;
                    }
                }
                if (biggestLayoutSize >0) {
                    String shiftName = "shift" + (shift * 7 + biggestLayoutPlace);
                    int shiftId = getResources().getIdentifier(shiftName, "id", getPackageName());
                    LinearLayout layout = findViewById(shiftId);
                    layout.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                }
            }
        }
    }

    private void shorts() {
        int difference;
        String missing = "";

        for (int shift = 0; shift < numOfShifts; shift++) {
            for (int day = 0; day < 7; day++) {
                if (shorts[shift][day] > 0) {
                    if (shorts[shift][day] == 1)
                        missing = "חסר עובד אחד";
                    else if (shorts[shift][day] > 1)
                        missing = "חסרים " + shorts[shift][day] + " עובדים";

                    String shiftName = "shift" + (shift * 7 + day);
                    int shiftId = getResources().getIdentifier(shiftName, "id", getPackageName());
                    TextView textView = findViewById(shiftId);
                    textView.setText(textView.getText() + missing);
                }
            }
        }
    }

    class FillShift implements Runnable {
        private int shift;
        private int day;
        private int differnce;

        public FillShift(int shift, int day) {
            this.shift = shift;
            this.day = day;
            this.differnce = shorts[shift][day];
        }

        @Override
        public void run() {
            if (orders[shift][day][0] == null) {
                orders[shift][day][0] = "";
                return;
            }

            LayoutInflater layoutInflater = getLayoutInflater();
            String shiftName = "shift" + (shift * 7 + day);
            int shiftId = getResources().getIdentifier(shiftName, "id", getPackageName());
            LinearLayout layout = findViewById(shiftId);
            layout.removeAllViews();
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
            params.height = LinearLayout.LayoutParams.MATCH_PARENT;
            layout.setLayoutParams(params);
            if (nextWeek[shift][day] != null) {
                String[] names = nextWeek[shift][day].split(",");
                if (names != null) {
                    //TODO לבדוק למה הסידור של מזכרת בתיה לא ממלא חוסרים ולמגדיל שיכבה
                    for (int i = 0; i < names.length; i++) {
                        LinearLayout parent = (LinearLayout) layoutInflater.inflate(R.layout.text_view_names, null);
                        TextView textView = parent.findViewById(R.id.textView);
                        parent.removeView(textView);
                        textView.setText(names[i]);
                        layout.addView(textView);
                    }
                }
            }
            if (differnce == 1) {
                LinearLayout parent = (LinearLayout) layoutInflater.inflate(R.layout.text_view_names, null);
                TextView textView = parent.findViewById(R.id.textView);
                parent.removeView(textView);
                textView.setText("חסר עובד אחד");
                layout.addView(textView);
            } else if (differnce > 1) {
                LinearLayout parent = (LinearLayout) layoutInflater.inflate(R.layout.text_view_names, null);
                TextView textView = parent.findViewById(R.id.textView);
                parent.removeView(textView);
                textView.setText("חסרים " + differnce + " עובדים");
                layout.addView(textView);
            }
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LinearLayout row = (LinearLayout) view.getParent();
                    int day = 6 - row.indexOfChild(view);
                    LinearLayout col = (LinearLayout) row.getParent();
                    int shift = col.indexOfChild(row) - 1;
                    edit.putString("shift", shift + "" + day).apply();
                    Set<String> set = new HashSet<>(Arrays.asList(orders[shift][day]));
                    edit.putStringSet("orders", set).apply();
                    edit.putString("user", gson.toJson(databaseHelper.getEmployee(user.getEmail()))).apply();
                    edit.putString("branch", gson.toJson(databaseHelper.getBranch(user.getCompanyName(), user.getBranchName()))).apply();
                    edit.putString("company", gson.toJson(databaseHelper.getCompany(user.getCompanyName()))).apply();

                    Intent intent = new Intent(ManagerSetSched.this, ManagerSetShift.class);
                    startActivity(intent);
                }
            });
        }
    }

    //let the manager the option to use other branches employees
    class CoOperate implements Runnable {
        @Override
        public void run() {
            findViewById(R.id.createCoOperate).setVisibility(View.GONE);
            coOperateLL.setVisibility(View.VISIBLE);
            coOperateButtons.setVisibility(View.VISIBLE);
            if (coOperateLL.getChildCount() > 0) {
                return;
            }
            coOperateOrders = new String[coOperateBranches.length][][][];
            coOperateAllPossibleShift = new boolean[coOperateBranches.length][][][];
            coOperateEmployeesName = new String[coOperateBranches.length][];
            for (int i = 0; i < coOperateBranches.length; i++) {
                coOperateOrders[i] = coOperateBranches[i].getOrders();
                if (coOperateOrders[i] == null)
                    coOperateAllPossibleShift[i] = coOperateBranches[i].getAllPossibleShifts();
            }
            for (int i = 0; i < coOperateBranches.length; i++) {
                coOperateEmployeesName[i] = coOperateBranches[i].getAllEmployeesName();
            }

            //run on all the shifts with missing employees at every branch to check for relevent extra employees
            for (int shift = 0; shift < numOfShifts; shift++) {
                for (int day = 0; day < 7; day++) {
                    if (shorts[shift][day] > 0) {
                        for (int i = 0; i < coOperateOrders.length; i++) {
                            if (coOperateOrders[i] != null)
                                if (coOperateOrders[i][shift][day][1] != null && coOperateOrders[i][shift][day][1].length() > 0)
                                    createOption(shift, day, i, coOperateOrders[i][shift][day][1]);
                                else {
                                    String options = "";
                                    for (int j = 0; j < coOperateAllPossibleShift[i].length; j++) {
                                        if (coOperateAllPossibleShift[i][j][shift][day])
                                            options += "," + coOperateEmployeesName[i][j];
                                    }
                                    if (options.length() > 0)
                                        createOption(shift, day, i, options.substring(1));
                                }
                        }
                    }
                }
            }

            if (coOperateLL.getChildCount() == 0) {
                findViewById(R.id.emptyCoOperate).setVisibility(View.VISIBLE);
            } else {
                coOperateButtons.setVisibility(View.VISIBLE);
            }
        }

        private void createOption(int shift, int day, int branchIndex, String options) {

            int countForNewLine = 0;
            //set weight
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1.0f
            );
            LinearLayout newLine = null;
            String[] arryOptions = options.split(",");
            for (int i = 0; i < arryOptions.length; i++) {
                //creating a new line of options
                if (countForNewLine % 4 == 0) {
                    newLine = new LinearLayout(ManagerSetSched.this);
                    newLine.setOrientation(LinearLayout.HORIZONTAL);
                    newLine.setWeightSum(4);
                    newLine.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

                    TextView textView = new TextView(ManagerSetSched.this);
                    textView.setLayoutParams(param);
                    //for the first line put the shift name for the rest only empty space
                    if (countForNewLine == 0)
                        textView.setText(simplify.getShiftName(shift, day) + "\n" + coOperateBranches[branchIndex].getBranchName());
                    newLine.addView(textView);
                    coOperateLL.addView(newLine);
                    countForNewLine++;
                }
                countForNewLine++;
                String name = coOperateEmployeesName[branchIndex][Integer.valueOf(arryOptions[i])];
                Button b = new Button(ManagerSetSched.this);
                b.setBackground(ContextCompat.getDrawable(App.getContext(), R.drawable.shape_round_light));
                b.setText(name);
                b.setLayoutParams(param);
                b.setContentDescription(shift + "," + day + "," + branchIndex + "," + arryOptions[i]);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //add or remove the employee from the "order" and update the "employeeHelp" and the layout
                        String[] content = view.getContentDescription().toString().split(",");
                        int shift = Integer.valueOf(content[0]);
                        int day = Integer.valueOf(content[1]);
                        int branchIndex = Integer.valueOf(content[2]);
                        int employeeIndex = Integer.valueOf(content[3]);
                        String name = coOperateBranches[branchIndex].getEmployeeName(employeeIndex);
                        if (view.getBackground() == ContextCompat.getDrawable(App.getContext(), R.drawable.shape_round_light)) {
                            view.setBackground(ContextCompat.getDrawable(App.getContext(), R.drawable.shape_round_dark));
                            Employee employee = coOperateBranches[branchIndex].getEmployee(employeeIndex);
                            employee.addNextWeekHelp(shift, day, branchIndex);
                            if (employeesHelp[branchIndex][employeeIndex] == null || employeesHelp[branchIndex][employeeIndex].length() == 0)
                                employeesHelp[branchIndex][employeeIndex] += shift + day;
                            else
                                employeesHelp[branchIndex][employeeIndex] += "," + shift + day;
                            orders[shift][Integer.valueOf(day)][0] += "," + branchIndex + "*" + name;

                        } else {
                            String[] search = employeesHelp[branchIndex][employeeIndex].split(",");
                            if (search != null) {
                                employeesHelp[branchIndex][employeeIndex] = "";
                                for (int i = 0; i < search.length; i++) {
                                    if (!search[i].equals(shift + day))
                                        employeesHelp[branchIndex][employeeIndex] += "," + search[i];
                                }
                                if (employeesHelp[branchIndex][employeeIndex].length() > 0)
                                    employeesHelp[branchIndex][employeeIndex] = employeesHelp[branchIndex][employeeIndex].substring(1);
                            }
                            view.setBackground(ContextCompat.getDrawable(App.getContext(), R.drawable.shape_round_light));
                            String[] tempOrder = orders[shift][day][0].split(",");
                            orders[shift][day][0] = "";
                            for (int i = 0; tempOrder != null && i < tempOrder.length; i++)
                                if (!tempOrder[i].equals(branchIndex + "*" + employeeIndex))
                                    orders[shift][day][0] += "," + tempOrder[i];
                            if (orders[shift][day][0].length() > 0)
                                orders[shift][day][0] = orders[shift][day][0].substring(1);
                        }
                        FillShift fillShift = new FillShift(shift, day);
                        fillShift.run();
                    }
                });
                newLine.addView(b);
            }
        }
    }

    public void createNavigationBar() {
        TextView setSched = findViewById(R.id.setSched);
        setSched.setVisibility(View.VISIBLE);
        setSched.setBackground(ContextCompat.getDrawable(App.getContext(), R.drawable.shape_round_dark));

        TextView mySched = findViewById(R.id.mySched);
        mySched.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit.putString("user", gson.toJson(databaseHelper.getEmployee(user.getEmail()))).apply();
                edit.putString("branch", gson.toJson(databaseHelper.getBranch(user.getCompanyName(), user.getBranchName()))).apply();
                edit.putString("company", gson.toJson(databaseHelper.getCompany(user.getCompanyName()))).apply();

                Intent intent = new Intent(ManagerSetSched.this, MySched.class);
                startActivity(intent);
            }
        });
        if (branch.getSchedReady()) {
            fillMySched.setVisibility(View.GONE);
            nextWeekTextView.setVisibility(View.VISIBLE);
        }
        nextWeekTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit.putString("user", gson.toJson(databaseHelper.getEmployee(user.getEmail()))).apply();
                edit.putString("branch", gson.toJson(databaseHelper.getBranch(user.getCompanyName(), user.getBranchName()))).apply();
                edit.putString("company", gson.toJson(databaseHelper.getCompany(user.getCompanyName()))).apply();

                Intent intent = new Intent(ManagerSetSched.this, MySchedNextWeek.class);
                startActivity(intent);
            }
        });
        fillMySched.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit.putString("user", gson.toJson(databaseHelper.getEmployee(user.getEmail()))).apply();
                edit.putString("branch", gson.toJson(databaseHelper.getBranch(user.getCompanyName(), user.getBranchName()))).apply();
                edit.putString("company", gson.toJson(databaseHelper.getCompany(user.getCompanyName()))).apply();

                Intent intent = new Intent(ManagerSetSched.this, FillPossibleShifts.class);
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        Menu subm = menu.getItem(4).getSubMenu(); // get my MenuItem with placeholder submenu
        subm.clear(); // delete place holder

        for (int i = 0; i < allEmployeesName.length; i++) {
            subm.add(0, i, i, allEmployeesName[i]); // משתנה שני הוא ה ID
        }


        if (manager == 0) {
            // לא עובד
            menu.getItem(1).setVisible(false);
            menu.getItem(2).setVisible(false);
        }
        if (manager == 1) {
            menu.getItem(2).setVisible(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;
        switch (item.getItemId()) {

            case R.id.editUser:
                edit.putString("user", gson.toJson(databaseHelper.getEmployee(user.getEmail()))).apply();
                edit.putString("branch", gson.toJson(databaseHelper.getBranch(user.getCompanyName(), user.getBranchName()))).apply();
                edit.putString("company", gson.toJson(databaseHelper.getCompany(user.getCompanyName()))).apply();

                edit.putString("editedEmployee", user.getEmail()).apply();
                intent = new Intent(this, EditEmployee.class);
                Toast.makeText(this, "edit user", Toast.LENGTH_SHORT).show();
                startActivity(intent);
                break;
         /*    case R.id.lastWeek:
                    edit.putString("user",gson.toJson(databaseHelper.getEmployee(user.getEmail()))).apply();
                    edit.putString("branch",gson.toJson(databaseHelper.getBranch(user.getCompanyName(),user.getBranchName()))).apply();
                    edit.putString("company",gson.toJson(databaseHelper.getCompany(user.getCompanyName()))).apply();

                intent = new Intent(MySchedLastWeek.this, LastWeekSched.class);
                Toast.makeText(this, "last week", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            case R.id.salary:
                    edit.putString("user",gson.toJson(databaseHelper.getEmployee(user.getEmail()))).apply();
                    edit.putString("branch",gson.toJson(databaseHelper.getBranch(user.getCompanyName(),user.getBranchName()))).apply();
                    edit.putString("company",gson.toJson(databaseHelper.getCompany(user.getCompanyName()))).apply();

                intent = new Intent(MySchedLastWeek.this, ExpectedSalary.class);
                Toast.makeText(this, "salary", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            case  R.id.editEmployees:
                    edit.putString("user",gson.toJson(databaseHelper.getEmployee(user.getEmail()))).apply();
                    edit.putString("branch",gson.toJson(databaseHelper.getBranch(user.getCompanyName(),user.getBranchName()))).apply();
                    edit.putString("company",gson.toJson(databaseHelper.getCompany(user.getCompanyName()))).apply();

                edit.putString("editedEmployee", user.getEmail()).apply();
                intent = new Intent(MySchedLastWeek.this, ExpectedSalary.class);
                Toast.makeText(this, "edit employee", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            case R.id.editBranch:
                    edit.putString("user",gson.toJson(databaseHelper.getEmployee(user.getEmail()))).apply();
                    edit.putString("branch",gson.toJson(databaseHelper.getBranch(user.getCompanyName(),user.getBranchName()))).apply();
                    edit.putString("company",gson.toJson(databaseHelper.getCompany(user.getCompanyName()))).apply();

                intent = new Intent(MySchedLastWeek.this, RegistrateBranch.class);
                Toast.makeText(this, "edit branch", Toast.LENGTH_SHORT).show();
                startActivity(intent);
         */

            case R.id.logout:
                edit.putString("user", gson.toJson(databaseHelper.getEmployee(user.getEmail()))).apply();
                edit.putString("branch", gson.toJson(databaseHelper.getBranch(user.getCompanyName(), user.getBranchName()))).apply();
                edit.putString("company", gson.toJson(databaseHelper.getCompany(user.getCompanyName()))).apply();

                edit.putString("user", null).apply();
                intent = new Intent(ManagerSetSched.this, Login.class);
                startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}