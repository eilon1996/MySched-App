package com.eilon.workschedule;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.eilon.App;
import com.google.gson.Gson;

public class RegistrateBranch extends AppCompatActivity {


    TableRow row1;
    TableRow row2;
    TableRow row3;

    TableRow lrow1;
    TableRow lrow2;
    TableRow lrow3;

    Spinner allBranches;

    TextView yourCompany;

    LinearLayout branchExist;
    LinearLayout table_linear_layout;
    LinearLayout table_edit_text;

    Company company;
    int[][] schedRequired;
    EditText addressET;
    String companyName;
    int numOfShifts;
    int biggestNumOfEmployees;

    DatabaseHelper databaseHelper;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    Gson gson;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registrate_branch);

        branchExist = findViewById(R.id.branchExist);

        table_linear_layout = findViewById(R.id.table_linear_layout);
        table_edit_text = findViewById(R.id.table_edit_text);

        row1 = table_edit_text.findViewById(R.id.row1);
        row2 = table_edit_text.findViewById(R.id.row2);
        row3 = table_edit_text.findViewById(R.id.row3);
        lrow1 = table_linear_layout.findViewById(R.id.row1);
        lrow2 = table_linear_layout.findViewById(R.id.row2);
        lrow3 = table_linear_layout.findViewById(R.id.row3);
        addressET = findViewById(R.id.address);

        allBranches = findViewById(R.id.allBranches);

        yourCompany = findViewById(R.id.yourCompany);

        sp = getSharedPreferences("key", 0);
        editor = sp.edit();
        gson = new Gson();

        companyName = sp.getString("companyName", null);
        databaseHelper = new DatabaseHelper(App.getContext());
        if(companyName != null) company = databaseHelper.getCompany(companyName);
        if (company == null) {
            findViewById(R.id.companyWasChosen).setVisibility(View.GONE);
            findViewById(R.id.chooseCompany).setVisibility(View.VISIBLE);
            Spinner allCompanies = findViewById(R.id.allCompanies);
            String[] temp = databaseHelper.getAllNetCompaniesNames();
            databaseHelper.close();
            if (temp != null) {
                final String[] companies = new String[temp.length + 1];
                companies[0] = "מצא את העסק שלך";

                for (int i = 1; i < companies.length; i++)
                    companies[i] = temp[i - 1];

                final ArrayAdapter<String> companyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, companies);
                allCompanies.setAdapter(companyAdapter);
                allCompanies.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, final int position, long l) {
                        if (position != 0) {
                            findViewById(R.id.companyWasChosen).setVisibility(View.VISIBLE);
                            updateCompany(companies[position]);
                        } else
                            findViewById(R.id.companyWasChosen).setVisibility(View.GONE);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }

                });
            } else {
                Toast.makeText(RegistrateBranch.this, "צור קודם כל את העסק", Toast.LENGTH_SHORT).show();
                if (databaseHelper.getNet(companyName)) {
                    databaseHelper.close();
                    Intent intent = new Intent(RegistrateBranch.this, RegistrateCompany.class);
                    startActivity(intent);
                } else {
                    final AlertDialog.Builder alert = new AlertDialog.Builder(RegistrateBranch.this);
                    alert.setMessage("העסק שבחרת אינו רשת ולכן לא ניתן להוסיף לו סניפים");
                    alert.setPositiveButton(" עובד חדש", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("companyName", companyName);
                            Intent intent = new Intent(RegistrateBranch.this, RegistrateEmployee.class);
                            startActivity(intent);
                        }
                    }).setNegativeButton("עסק חדש", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("companyName", companyName);
                            Intent intent = new Intent(RegistrateBranch.this, RegistrateCompany.class);
                            startActivity(intent);
                        }
                    }).setNeutralButton("בטל", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    alert.create();
                    alert.show();
                }
            }
        } else {
            findViewById(R.id.companyWasChosen).setVisibility(View.VISIBLE);
            findViewById(R.id.chooseCompany).setVisibility(View.GONE);
            updateCompany(companyName);
            schedRequired = new int[numOfShifts][7];
        }

        findViewById(R.id.createNewEmployee).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (allBranches.getSelectedItemPosition() != 0) {
                    SharedPreferences sp = getSharedPreferences("key", 0);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("companyName",companyName).apply();
                    Branch branch = databaseHelper.getBranch(companyName,allBranches.getSelectedItem().toString() );
                    editor.putString("branch", gson.toJson(branch)).apply();
                    Intent intent = new Intent(RegistrateBranch.this, RegistrateEmployee.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(RegistrateBranch.this, "בחר סניף", Toast.LENGTH_SHORT).show();
                }

            }
        });


        findViewById(R.id.createSched).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateSched createSched = new CreateSched();
                createSched.run();
            }
        });

        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateBranch createBranch = new CreateBranch();
                createBranch.run();
            }
        });

    }

    private void updateCompany(String companyName) {
        if (companyName == null) {
            branchExist.setVisibility(View.GONE);
            return;
        }

        yourCompany.setText(companyName);
        company = databaseHelper.getCompany(companyName);
        databaseHelper.close();
        numOfShifts = company.getNumOfShifts();

        schedRequired = new int[numOfShifts][7];
        String[] temp = company.getAllBranchesAddresses();
        if (temp != null && temp.length>0) {
            branchExist.setVisibility(View.VISIBLE);
            final String[] branches = new String[temp.length + 1];
            branches[0] = "מצא את הסניף שלך";
            for (int i = 1; i < branches.length; i++)
                branches[i] = temp[i - 1];

            ArrayAdapter<String> branchAdapter = new ArrayAdapter<>(RegistrateBranch.this, android.R.layout.simple_list_item_1, branches);
            allBranches.setAdapter(branchAdapter);
        } else
            branchExist.setVisibility(View.GONE);

        switch (numOfShifts) {
            case 1:
                row1.setVisibility(View.VISIBLE);
                row2.setVisibility(View.INVISIBLE);
                row3.setVisibility(View.INVISIBLE);
                lrow1.setVisibility(View.VISIBLE);
                lrow2.setVisibility(View.INVISIBLE);
                lrow3.setVisibility(View.INVISIBLE);
                break;
            case 2:
                row1.setVisibility(View.VISIBLE);
                row2.setVisibility(View.VISIBLE);
                row3.setVisibility(View.INVISIBLE);
                lrow1.setVisibility(View.VISIBLE);
                lrow2.setVisibility(View.VISIBLE);
                lrow3.setVisibility(View.INVISIBLE);
                break;
            case 3:
                row1.setVisibility(View.VISIBLE);
                row2.setVisibility(View.VISIBLE);
                row3.setVisibility(View.VISIBLE);
                lrow1.setVisibility(View.VISIBLE);
                lrow2.setVisibility(View.VISIBLE);
                lrow3.setVisibility(View.VISIBLE);
                break;
        }
        if (company.getNet() && temp != null)
            branchExist.setVisibility(View.VISIBLE);
        else branchExist.setVisibility(View.VISIBLE);
    }

    class CreateSched implements Runnable {

        public CreateSched() {

        }

        @Override
        public void run() {
            boolean allGood = true;
            boolean isEmptyTable = true;
            findViewById(R.id.wrongSchedRequired).setVisibility(View.INVISIBLE);

            biggestNumOfEmployees = 0;
            for (int shift = 0; shift < numOfShifts && allGood; shift++) {
                for (int day = 0; day < 7 && allGood; day++) {
                    String shiftName = "shift" + (shift * 7 + day);
                    int shiftId = table_edit_text.getResources().getIdentifier(shiftName, "id", getPackageName());
                    EditText editText = findViewById(shiftId);
                    int numOfEmployees = 0;
                    if (editText.getText().length() != 0) {
                        try {
                            numOfEmployees = Integer.valueOf(editText.getText().toString());
                            if (numOfEmployees < 0) {
                                Toast.makeText(RegistrateBranch.this, "אי אפשר להכניס מספר שלילי", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (numOfEmployees > 100) {
                                Toast.makeText(RegistrateBranch.this, "אי אפשר לרשום יותר מ 100 עובדים ", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (biggestNumOfEmployees < numOfEmployees)
                                biggestNumOfEmployees = numOfEmployees;
                            if (numOfEmployees > 0) {
                                schedRequired[shift][day] = numOfEmployees;
                                isEmptyTable = false;
                            }
                        } catch (NumberFormatException e) {
                            findViewById(R.id.wrongSchedRequired).setVisibility(View.VISIBLE);
                            return;
                        }
                    }
                }
            }

                    if(isEmptyTable){
                        Toast.makeText(RegistrateBranch.this,"צריך למלא לפחות משמרת אחת ", Toast.LENGTH_SHORT).show();
                        return;
                    }

            LayoutInflater layoutInflater = getLayoutInflater();
            for (int shift = 0; shift < numOfShifts && allGood; shift++) {
                int biggestLayoutPlace = 0;
                int biggestLayoutSize = 0;
                for (int day = 0; day < 7 && allGood; day++) {
                    String shiftName = "shift" + (shift * 7 + day);
                    int shiftId = table_linear_layout.getResources().getIdentifier(shiftName, "id", getPackageName());
                    LinearLayout layout = table_linear_layout.findViewById(shiftId);
                    layout.removeAllViewsInLayout();
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
                    params.height = LinearLayout.LayoutParams.MATCH_PARENT;
                    layout.setLayoutParams(params);
                    for (int i = 0; i < schedRequired[shift][day]; i++) {
                        LinearLayout parent = (LinearLayout)layoutInflater.inflate(R.layout.text_view_names, null);
                        TextView textView = parent.findViewById(R.id.textView);
                        parent.removeView(textView);
                        textView.setText("עובד " + (i + 1));
                        layout.addView(textView);
                    }
                    if (schedRequired[shift][day] > biggestLayoutSize) {
                        biggestLayoutSize = schedRequired[shift][day];
                        biggestLayoutPlace = day;
                    }
                }

                if (biggestLayoutSize > 0) {
                    String shiftName = "shift" + (shift * 7 + biggestLayoutPlace);
                    int shiftId = getResources().getIdentifier(shiftName, "id", getPackageName());
                    LinearLayout layout = table_linear_layout.findViewById(shiftId);
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
                    params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    layout.setLayoutParams(params);
                }
            }
                        /*
                        TextView button = new TextView(RegistrateBranch.this);
                        button.setText("עובד " + (i + 1));
                        button.setTextSize(7);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                TextView button = (TextView)view;
                                LayoutInflater inflater = getLayoutInflater();
                                final View layout = inflater.inflate(R.layout.dialog_textview, null);
                                final AlertDialog.Builder alert = new AlertDialog.Builder(RegistrateBranch.this);
                                alert.setMessage("ההודעה קצרה תהיה קבועה למקום העובד בסידור");
                                alert.setTitle("הוסף הודעה");
                                layout.findViewById(R.id.textView).setContentDescription();
                                alert.setView(layout)
                                        .setPositiveButton("הוסף", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                EditText editText = layout.findViewById(R.id.comment);
                                                String comment = editText.getText().toString();
                                                String buttonText = button.getText().toString();
                                                int index = buttonText.indexOf("\n");
                                                if (index == -1)
                                                    button.setText(button.getText() + "\n" + comment);
                                                else
                                                    button.setText(button.getText().toString().substring(0, index) + "\n" + comment);

                                                button.setContentDescription(comment);
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
                        layout.addView(button);

                    }

                         */

            if (isEmptyTable) {
                findViewById(R.id.wrongEmptyTable).setVisibility(View.VISIBLE);
                findViewById(R.id.wrongSchedRequired).setVisibility(View.GONE);
                findViewById(R.id.afterCreateSched).setVisibility(View.GONE);
                allGood = false;
            }
            if (allGood) {
                findViewById(R.id.wrongSchedRequired).setVisibility(View.GONE);
                findViewById(R.id.wrongEmptyTable).setVisibility(View.GONE);
                findViewById(R.id.afterCreateSched).setVisibility(View.VISIBLE);
            }
        }
    }

    class CreateBranch implements Runnable {
        @Override
        public void run() {

            findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(addressET.getText() == null || addressET.getText().length()==0){
                        Toast.makeText(RegistrateBranch.this, "הכתובת לא יכולה להיות ריקה", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String address = addressET.getText().toString();
                    String [] branchesName = company.getAllBranchesAddresses();
                    if(branchesName != null) {
                        for (int i = 0; i < branchesName.length; i++) {
                            if (address.equals(branchesName[i])) {
                                Toast.makeText(RegistrateBranch.this, "סניף אחר כבר משתמש בכתובת הזו", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    }


                    biggestNumOfEmployees = ((biggestNumOfEmployees/10)+1)*10;
                    String [][][] constantComments = new String[numOfShifts][7][biggestNumOfEmployees];
                   /* for (int shift = 0; shift < numOfShifts; shift++) {
                        for (int day = 0; day < 7; day++) {
                            String shiftName = "shift" + (shift * 7 + day);
                            int shiftId = table_linear_layout_edittext_edge.getResources().getIdentifier(shiftName, "id", getPackageName());
                            LinearLayout linearLayout = findViewById(shiftId);
                            schedRequired[shift][day] = linearLayout.getChildCount();
                            for (int i = 0; i < linearLayout.getChildCount(); i++) {
                                CharSequence comment = linearLayout.getChildAt(i).getContentDescription();
                                if (comment != null && comment.length() != 0)
                                    constantComments[shift][day][i] = comment.toString();
                            }
                        }
                    }
                     */

                    String[] shiftsName = new String[numOfShifts];
                    boolean allGood = true;

                    EditText editText = table_linear_layout.findViewById(R.id.shiftName1);
                    if (editText.getText() != null && editText.getText().length() > 0)
                        shiftsName[0] = editText.getText().toString();
                    else allGood = false;
                    if (numOfShifts > 1) {
                        editText = table_linear_layout.findViewById(R.id.shiftName2);
                        if (editText.getText() != null && editText.getText().length() > 0)
                            shiftsName[1] = editText.getText().toString();
                        else allGood = false;
                        if (numOfShifts == 3) {
                            editText = table_linear_layout.findViewById(R.id.shiftName3);
                            if (editText.getText() != null && editText.getText().length() > 0)
                                shiftsName[2] = editText.getText().toString();
                            else allGood = false;
                        }
                    }

                    String[] daysName = new String[7];
                    editText = table_linear_layout.findViewById(R.id.day0);
                    if (editText.getText() != null && editText.getText().length() > 0)
                        daysName[0] = editText.getText().toString();
                    editText = table_linear_layout.findViewById(R.id.day1);
                    if (editText.getText() != null && editText.getText().length() > 0)
                        daysName[1] = editText.getText().toString();
                    editText = table_linear_layout.findViewById(R.id.day2);
                    if (editText.getText() != null && editText.getText().length() > 0)
                        daysName[2] = editText.getText().toString();
                    editText = table_linear_layout.findViewById(R.id.day3);
                    if (editText.getText() != null && editText.getText().length() > 0)
                        daysName[3] = editText.getText().toString();
                    editText = table_linear_layout.findViewById(R.id.day4);
                    if (editText.getText() != null && editText.getText().length() > 0)
                        daysName[4] = editText.getText().toString();
                    editText = table_linear_layout.findViewById(R.id.day5);
                    if (editText.getText() != null && editText.getText().length() > 0)
                        daysName[5] = editText.getText().toString();
                    editText = table_linear_layout.findViewById(R.id.day6);
                    if (editText.getText() != null && editText.getText().length() > 0)
                        daysName[6] = editText.getText().toString();



                    if (allGood) {
                        Branch branch = new Branch(address, companyName, schedRequired, shiftsName,daysName, constantComments);
                        Gson gson = new Gson();
                        editor.putString("branch", gson.toJson(branch)).apply();
                        editor.putString("companyName", companyName).apply();

                        Toast.makeText(RegistrateBranch.this, "הסניף נוצר בהצלחה", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(RegistrateBranch.this, RegistrateEmployee.class);
                        startActivity(intent);
                    } else
                        findViewById(R.id.wrongShiftName).setVisibility(View.VISIBLE);


                }
            });
        }
    }
}
