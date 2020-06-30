package com.eilon.workschedule;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.eilon.App;
import com.google.gson.Gson;

import java.util.Calendar;

public class RegistrateEmployee extends AppCompatActivity {

    Company company;
    String branchName;
    LinearLayout chooseCompany;
    EditText name;
    EditText fName; // family name
    EditText phone;
    EditText email;
    EditText bYear;
    EditText address;
    RadioButton male;
    LinearLayout professions;
    Spinner allCompanies;
    Spinner allBranches;
    SharedPreferences sp;
    SharedPreferences.Editor edit;
    DatabaseHelper databaseHelper;
    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registrate_employee);

        chooseCompany = findViewById(R.id.chooseCompany);
        name = findViewById(R.id.name);
        fName = findViewById(R.id.fName);
        phone = findViewById(R.id.phone);
        email = findViewById(R.id.email);
        bYear = findViewById(R.id.bYear);
        address = findViewById(R.id.address);
        male = findViewById(R.id.male);
        professions = findViewById(R.id.professions); // צריך להוסיף צ'ק בוקס של מקצועות
        allCompanies = findViewById(R.id.companies);
        allBranches = findViewById(R.id.branches);

        sp = getSharedPreferences("key", 0);
        gson = new Gson();
        edit = sp.edit();
        databaseHelper = new DatabaseHelper(App.getContext());
        {
            String[] temp = databaseHelper.getAllCompaniesNames();
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
                        UpdateCompany updateCompany;
                        if (position != 0) {
                            updateCompany = new UpdateCompany(companies[position]);
                            updateCompany.run();
                        } else company = null;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });

                String companyName = sp.getString("companyName", null);
                if (companyName != null) {
                    for (int i = 1; i < companies.length; i++) {
                        if (companies[i].equals(companyName)) {
                            allCompanies.setSelection(i);
                            break;
                        }
                    }
                    branchName = sp.getString("branchName", null);
                    UpdateCompany updateCompany = new UpdateCompany(companyName);
                    updateCompany.run();
                }
            }
        }
        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Submit submit = new Submit();
                submit.run();
            }

        });
        findViewById(R.id.newCompany).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegistrateEmployee.this, RegistrateCompany.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.newBranch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegistrateEmployee.this, RegistrateBranch.class);
                startActivity(intent);
            }
        });
    }

    class Submit implements Runnable{
        @Override
        public void run() {
            boolean allGood = true;
            TextView wrongName = findViewById(R.id.wrongName);
            //check all fields are filled
            if (name.getText().length() == 0 || fName.getText().length() == 0 || phone.getText().length() == 0 || email.getText().length() == 0 || company == null || branchName == null
                    || bYear.getText().length() == 0 || address.getText().length() == 0) {
                findViewById(R.id.wrongMissingPart).setVisibility(View.VISIBLE);
                allGood = false;
            } else {
                findViewById(R.id.wrongMissingPart).setVisibility(View.GONE);
            }
            String nameString = name.getText().toString();
            String fNameString = fName.getText().toString();
            boolean correctName = true;

            {//check name
                if (nameString.contains(",") || fNameString.contains(",")) {
                    allGood = false;
                    wrongName.setText("השם יכול להכיל רק אותיות");
                    wrongName.setVisibility(View.VISIBLE);
                } else if (correctName) {
                    wrongName.setVisibility(View.GONE);
                }

                String[] employeesName = company.getBranch(branchName).getAllEmployeesName();
                if (employeesName != null) {
                    for (int i = 0; i < employeesName.length; i++) {
                        if ((nameString + "\n" + fNameString).equals(employeesName[i])) {
                            allGood = false;
                            wrongName.setText("כבר קיים עובד עם שם כמו שלך \n אתה יכול להוסיף כינוי או לשנות מעט את השם");
                            wrongName.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            if (email.getText() != null && email.getText().length()>1) {
                final String emailS = email.getText().toString();
                if (emailS.indexOf("@") < 1 && (emailS.indexOf(".com") != emailS.length() - 5 || emailS.indexOf(".co.il") != emailS.length() - 7)) {
                    findViewById(R.id.wrongEmailUsed).setVisibility(View.GONE);
                    findViewById(R.id.wrongEmailForm).setVisibility(View.VISIBLE);
                    allGood = false;
                } else if (databaseHelper.findEmployeeByEmail(emailS) != null) {
                    final AlertDialog.Builder alert = new AlertDialog.Builder(RegistrateEmployee.this);
                    alert.setMessage("כבר קיים משתמש לאימייל זה, הזן סיסמה וכנס אליו" + "\n" + "נזכיר שהסיסמה שלך היא שנת הלידה שלך");
                    alert.setTitle("משתמש תפוס");
                    AlertDialog dialog = alert.show();
                    TextView messageView = dialog.findViewById(android.R.id.message);
                    messageView.setGravity(Gravity.RIGHT);

                    TextView titleView = dialog.findViewById(RegistrateEmployee.this.getResources().getIdentifier("alertTitle", "id", "android"));
                    if (titleView != null) {
                        titleView.setGravity(Gravity.RIGHT);
                    }
                    LayoutInflater inflater = getLayoutInflater();
                    final View layout = inflater.inflate(R.layout.dialog_textview, null);
                    alert.setView(layout);
                    alert.setPositiveButton("כניסה", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            EditText editText = layout.findViewById(R.id.comment);
                            Employee user = databaseHelper.getEmployee(emailS);
                            if (editText.getText().toString().equals(user.getPassword())) {
                                edit.putString("user", gson.toJson(user)).apply();
                                edit.putString("branch", gson.toJson(user.getBranch())).apply();
                                edit.putString("company", gson.toJson(user.getCompany())).apply();

                                edit.putString("branchName",null).apply();
                                edit.putString("companyName",null).apply();
                                Intent intent = new Intent(RegistrateEmployee.this, MySched.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(RegistrateEmployee.this, "סיסמה שגויה", Toast.LENGTH_LONG);
                            }
                        }
                    })
                            .setNegativeButton("ביטול", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });

                    alert.create();
                    alert.show();
                    findViewById(R.id.wrongEmailUsed).setVisibility(View.VISIBLE);
                    findViewById(R.id.wrongEmailForm).setVisibility(View.GONE);
                    allGood = false;
                }
            }
            if (bYear.getText() != null) {
                try {
                    int bYearI = Integer.valueOf(bYear.getText().toString());
                    if (bYearI < 1900 || bYearI > Calendar.getInstance().get(Calendar.YEAR)) {
                        findViewById(R.id.wrongBYear).setVisibility(View.VISIBLE);
                        allGood = false;
                    } else
                        findViewById(R.id.wrongBYear).setVisibility(View.GONE);
                } catch (NumberFormatException e) {
                    findViewById(R.id.wrongBYear).setVisibility(View.VISIBLE);
                    allGood = false;
                }
            }
            if (phone.getText() != null) {
                try {
                    Integer.valueOf(phone.getText().toString());
                    if (phone.getText().toString().length() != 10) {
                        findViewById(R.id.wrongPhone).setVisibility(View.VISIBLE);
                        allGood = false;
                    } else
                        findViewById(R.id.wrongPhone).setVisibility(View.GONE);
                } catch (NumberFormatException e) {
                    findViewById(R.id.wrongPhone).setVisibility(View.VISIBLE);
                    allGood = false;
                }
            }
            if (allGood) {
                AddEmployee addEmployee = new AddEmployee();
                addEmployee.run();
            }

        }
    }

    class UpdateCompany implements Runnable {

        String companyName;

        public UpdateCompany(String companyName) {
            this.companyName = companyName;
        }

        public void run() {
            DatabaseHelper databaseHelper = new DatabaseHelper(App.getContext());
            company = databaseHelper.getCompany(companyName);
            String[] temp = company.getAllBranchesAddresses();
            if(temp == null) {
                temp = databaseHelper.getAllBranchesNameForCompany(companyName);
                if(temp != null) company.setBranchesName(temp);
            }
            if (chooseCompany.getVisibility() == View.VISIBLE && temp != null) {
                final String[] branches = new String[temp.length + 1];
                branches[0] = "מצא את הסניף שלך";
                for (int i = 1; i < branches.length; i++)
                    branches[i] = temp[i - 1];

                ArrayAdapter<String> branchAdapter = new ArrayAdapter<>(RegistrateEmployee.this, android.R.layout.simple_list_item_1, branches);
                allBranches.setAdapter(branchAdapter);
                allBranches.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, final int position, long l) {
                        if (position != 0)
                            branchName = branches[position];
                        else branchName = null;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
                if (branchName != null) {
                    for (int i = 1; i < branches.length; i++) {
                        if (branches[i].equals(branchName)) {
                            allBranches.setSelection(i);
                            break;
                        }
                    }
                }
                if(!company.getNet()) allBranches.setSelection(0);
            }
            LinearLayout professions = findViewById(R.id.professions);
            professions.removeAllViews();
            String[] pros = company.getProfessions();
            LinearLayout line = null;
            for (int i = 0; i < pros.length; i++) {
                if (i % 3 == 0) {
                    line = new LinearLayout(RegistrateEmployee.this);
                    line.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                    professions.addView(line);
                }
                line.setOrientation(LinearLayout.HORIZONTAL);
                CheckBox checkBox = new CheckBox(RegistrateEmployee.this);
                checkBox.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                checkBox.setText(pros[i]);
                line.addView(checkBox);
            }
        }
    }

    class AddEmployee implements Runnable {
        public AddEmployee() {
        }

        @Override
        public void run() {

            boolean[] pros = new boolean[company.getNumOfProfessions()];
            LinearLayout line = null;
            for (int i = 0; i < pros.length; i++) {
                if (i % 3 == 0) line = (LinearLayout) professions.getChildAt(i / 3);
                CheckBox p = (CheckBox) line.getChildAt(i % 3);
                pros[i] = p.isChecked();
            }
            new Employee(name.getText().toString(),fName.getText().toString(), email.getText().toString(), bYear.getText().toString(), company.getComapnyName(), branchName,
                    phone.getText().toString(), address.getText().toString(), male.isSelected(), pros);

            final AlertDialog.Builder alert = new AlertDialog.Builder(RegistrateEmployee.this);
            alert.setMessage("משתמש נוצר בהצלחה" + "\n" + "הסיסמה שלך היא שנת הלידה שלך");
            alert.setPositiveButton("אש", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Employee user = databaseHelper.getEmployee(email.getText().toString());
                    edit.putString("user", gson.toJson(user)).apply();
                    edit.putString("branch", gson.toJson(user.getBranch())).apply();
                    edit.putString("company", gson.toJson(user.getCompany())).apply();
                    Intent intent = new Intent(RegistrateEmployee.this, MySched.class);
                    startActivity(intent);
                }
            });
            AlertDialog dialog = alert.show();
            TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
            messageView.setGravity(Gravity.RIGHT);

            TextView titleView = (TextView) dialog.findViewById(RegistrateEmployee.this.getResources().getIdentifier("alertTitle", "id", "android"));
            if (titleView != null) {
                titleView.setGravity(Gravity.RIGHT);
            }
            alert.create();
            alert.show();
        }
    }
}