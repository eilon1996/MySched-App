package com.eilon.workschedule;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

public class EditCompany extends AppCompatActivity {

    LinearLayout myProfessions;
    LinearLayout userProfessions;
    boolean[][] basicWeekShifts;

    RadioGroup numOfShifts;
    Spinner allCompanies;
    Spinner type;
    EditText professionText;
    RadioGroup net;
    EditText companyNameET;
    EditText otherType;

    String typeSelected;
    String companyName;
    boolean isNet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registrate_company);


        basicWeekShifts = new boolean[3][7];
        myProfessions = findViewById(R.id.myProfessions);
        userProfessions = findViewById(R.id.userProfessions);
        companyNameET = findViewById(R.id.company);
        net = findViewById(R.id.net);
        allCompanies = findViewById(R.id.allCompanies);
        type = findViewById(R.id.type);
        professionText = findViewById(R.id.professionText);
        otherType = findViewById(R.id.otherType);


        //set company adapter and when item been chosen set branch adapter
        final DatabaseHelper databaseHelper = new DatabaseHelper(EditCompany.this);
        final Gson gson = new Gson();
        String[] temp = databaseHelper.getAllCompaniesNames();
        databaseHelper.close();
        final String[] companies;
        if(temp != null) {
            companies = new String[temp.length + 1];
            companies[0] = "מצא את העסק שלך";
            for (int i = 1; i < companies.length; i++)
                companies[i] = temp[i - 1];
            final Button branch = findViewById(R.id.branch);
            final ArrayAdapter<String> companyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, companies);
            allCompanies.setAdapter(companyAdapter);
            allCompanies.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }

            });


            branch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (allCompanies.getSelectedItemPosition() == 0)
                        Toast.makeText(EditCompany.this, "בחר עסק", Toast.LENGTH_SHORT).show();
                    else {
                        companyName = allCompanies.getSelectedItem().toString();
                        final SharedPreferences sp = getSharedPreferences("key", 0);
                        final SharedPreferences.Editor editor = sp.edit();
                        editor.putString("companyName", companyName);
                        editor.apply();
                        if(databaseHelper.getNet(companyName)) {
                            Intent intent = new Intent(EditCompany.this, RegistrateBranch.class);
                            startActivity(intent);
                        }
                        else {
                            final AlertDialog.Builder alert = new AlertDialog.Builder(EditCompany.this);
                            alert.setMessage("העסק שבחרת אינו סניף" + "\n" + "יש לך אפשרות ליצור משתמש בתוך העסק או לפתוח עסק חדש בקלות");
                            alert.setPositiveButton("עסק חדש", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                                    .setNegativeButton("עובד חדש", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            String [] branchName = databaseHelper.getAllBranchesNameForCompany(companyName);
                                            databaseHelper.close();
                                            editor.putString("companyName", companyName).apply();
                                            editor.putString("branchName", branchName[0]).apply();
                                            Intent intent = new Intent(EditCompany.this, EditEmployee.class);
                                            startActivity(intent);
                                        }
                                    });

                            alert.create();
                            alert.show();

                        }
                    }
                }
            });
        }
        //setting the diffrent type of professions that are in the company
        String[] types = {"מסעדה", "בר", "חנות", "תחנת דלק", "אבטחה", "אחר"};
        final ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, types);
        type.setAdapter(typeAdapter);

        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                myProfessions.removeAllViewsInLayout();

                String text1 = "", text2 = "", text3 = "";
                if (position == 0) {
                    text1 = "מלצר/ית";
                    text2 = "טבח/ית";
                    text3 = "אחמ'ש";
                } else if (position == 1) {
                    text1 = "מלצר/ית";
                    text2 = "ברמן/ית";
                    text3 = "אחמ'ש";
                } else if (position == 2) {
                    text1 = "סדרן/ית";
                    text2 = "קופאי/ת";
                    text3 = "מנהל סניף";
                } else if (position == 3) {
                    text1 = "קופאי/ת";
                    text2 = "מתדלק/ית";
                    text3 = "מנהל סניף";
                } else if (position == 4) {
                    text1 = "סייר/ת";
                    text2 = "מאבטח/ת";
                    text3 = "מנהל";
                } else if (position == 5){
                    otherType.setVisibility(View.VISIBLE);
                }
                if (position != 5) {
                    otherType.setVisibility(View.GONE);
                    Button button = new Button(EditCompany.this);
                    addButton(button, text1, false);
                    button = new Button(EditCompany.this);
                    addButton(button, text2, false);
                    button = new Button(EditCompany.this);
                    addButton(button, text3, false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Button addProfession = findViewById(R.id.enterProfession);
        addProfession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (professionText.getText().toString().length() > 0) {
                    Button button = new Button(EditCompany.this);
                    addButton(button, professionText.getText().toString(), true);
                }
            }
        });

        //adjust the basic week's shifts by the num of shifts a day
        numOfShifts = findViewById(R.id.numOfShifts);
        numOfShifts.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                TableRow row2 = findViewById(R.id.row2);
                TableRow row3 = findViewById(R.id.row3);
                RadioButton rb = findViewById(checkedId);
                String s = rb.getContentDescription().toString();
                if (s.equals("1")) {
                    row2.setVisibility(View.INVISIBLE);
                    row3.setVisibility(View.INVISIBLE);
                } else if (s.equals("2")) {
                    row2.setVisibility(View.VISIBLE);
                    row3.setVisibility(View.INVISIBLE);
                } else {
                    row2.setVisibility(View.VISIBLE);
                    row3.setVisibility(View.VISIBLE);
                }

            }
        });

        //enter form data if correct
        Button submit = findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if the form is valid
                boolean allGood = true;
                TextView missing = findViewById(R.id.wrongMissingPart);
                companyName = "";
                typeSelected = "";
                isNet = false;
                if (companyNameET.getText() != null) {
                    if (companyNameET.getText().toString().length() == 0 || net.getCheckedRadioButtonId() == -1 || numOfShifts.getCheckedRadioButtonId() == -1) {
                        allGood = false;
                        missing.setVisibility(View.VISIBLE);
                    } else{
                        companyName = companyNameET.getText().toString();
                        missing.setVisibility(View.GONE);
                    }

                    TextView wrongCompany = findViewById(R.id.wrongCompany);
                    if (databaseHelper.findCompany(companyName) != null) {
                        allGood = false;
                        wrongCompany.setText("החברה כבר קיימת");
                    }else if (companyNameET.getText().toString().length() == 0){
                        wrongCompany.setText("מלא את שם החברה");
                    } else wrongCompany.setText("");

                    TextView wrongType = findViewById(R.id.wrongType);
                    if(type.getSelectedItem().toString().equals("אחר")) {
                        typeSelected = otherType.getText().toString();
                        if(typeSelected.length() == 0){
                            allGood = false;
                            wrongType.setText("מלא את סוג העסק");
                        }
                    }
                    else{
                        typeSelected = type.getSelectedItem().toString();
                        wrongType.setText("");
                    }
                    TextView wrongNumOfShifts = findViewById(R.id.wrongNumOfShifts);
                    if(numOfShifts.getCheckedRadioButtonId() == -1){
                        wrongNumOfShifts.setText("בחר מספר משמרות ביום");
                    } else
                        wrongNumOfShifts.setText("");
                } else {
                    allGood = false;
                    missing.setVisibility(View.VISIBLE);
                }
                TextView wrongNet = findViewById(R.id.wrongNet);
                if(findViewById(net.getCheckedRadioButtonId()) == null){
                    wrongNet.setText("בחר האם העסק הוא חברה או לא");
                }else {
                    wrongNet.setText("");
                    isNet = findViewById(net.getCheckedRadioButtonId()).getContentDescription().equals("yes");
                }
                if (allGood) {
                    CreateCompany createCompany = new CreateCompany();
                    createCompany.run();
                }
                else {
                    findViewById(R.id.wrongMissingPart).setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void addButton(Button button, String text, boolean belongToUser) {
        for (int i = 0; i < myProfessions.getChildCount(); i++) {
            Button check = (Button) myProfessions.getChildAt(i);
            if (check.getText().toString().equals(text)){
                Toast.makeText(EditCompany.this, "כבר קיים תפקיד כזה", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        for (int i = 0; i < userProfessions.getChildCount(); i++) {
            Button check = (Button) userProfessions.getChildAt(i);
            if (check.getText().toString().equals(text)){
                Toast.makeText(EditCompany.this, "כבר קיים תפקיד כזה", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        button.setText(text);
        button.setTextSize(8);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout parent = (LinearLayout) view.getParent();
                parent.removeView(view);
            }
        });
        if (belongToUser) userProfessions.addView(button);
        else myProfessions.addView(button);
    }

    class CreateCompany implements Runnable{
        public CreateCompany(){}
        @Override
        public void run() {

            int myProfessionsCount = myProfessions.getChildCount();
            int userProfessionsCount = userProfessions.getChildCount();
            String[] professions = new String[myProfessionsCount+ userProfessionsCount];

            for (int i = 0; i < myProfessionsCount; i++) {
                Button b = (Button) myProfessions.getChildAt(i);
                professions[i] = b.getText().toString();
            }
            for (int i = 0; i < userProfessionsCount; i++) {
                Button b = (Button) userProfessions.getChildAt(i);
                professions[i + myProfessionsCount] = b.getText().toString();
            }
            int id = numOfShifts.getCheckedRadioButtonId();
            int n = Integer.valueOf(findViewById(id).getContentDescription().toString());
            if(type.getSelectedItem().toString().equals("אחר"))
                typeSelected = otherType.getText().toString();
            else  typeSelected = type.getSelectedItem().toString();
            new Company(companyName,n,professions,typeSelected, isNet);
            SharedPreferences sp = getSharedPreferences("key", 0);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("companyName", companyName);
            editor.apply();
            Toast.makeText(EditCompany.this, "צעד ראשון בוצע בהצלחה", Toast.LENGTH_SHORT).show();
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(EditCompany.this, RegistrateBranch.class);
            startActivity(intent);
        }
    }
}




