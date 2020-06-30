package com.eilon.workschedule;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.eilon.App;
import com.google.gson.Gson;

public class EditBranch extends AppCompatActivity {

    EditText addressET;
    TextView wrongAddress;
    int numOfShifts;

    String [][][] constantComments;
    int [][] schedRequired;
    Company company;
    Branch branch;

    DatabaseHelper databaseHelper;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    Gson gson;

    LinearLayout table_edit_text;
    LinearLayout table_linear_layout;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_branch);

        sp = getSharedPreferences("key", 0);
        editor = sp.edit();
        databaseHelper = new DatabaseHelper(App.getContext());
        gson = new Gson();

        company = gson.fromJson(sp.getString("company", null), Company.class);
        branch = gson.fromJson(sp.getString("branch", null), Branch.class);
        constantComments = branch.getConstantComments();
        if(constantComments == null) constantComments = new String[numOfShifts][7][branch.getNumOfEmployees()];
        schedRequired = branch.getSchedRequired();
        if(schedRequired == null) schedRequired = new int[numOfShifts][7];

        table_edit_text = findViewById(R.id.table_edit_text);
        setShiftsName(table_edit_text);
        table_linear_layout = findViewById(R.id.table_linear_layout);
        setShiftsName(table_linear_layout);

        final String [] branchesName = company.getAllBranchesAddresses();
        wrongAddress = findViewById(R.id.wrongAddressEmpty);
        findViewById(R.id.setName).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean allGood = true;
                if(addressET.getText().length()>0) {
                    String name = addressET.getText().toString();
                    if(name.contains(",")|| name.contains("*")) {
                        wrongAddress.setText("מותר להשתמש רק באותיות ומספרים");
                        allGood = false;
                    }
                    else{
                        int index = company.getBranchIndex(branch.getBranchName());
                        for(int i = 0; i<index; i++){
                            if(name.equals(branchesName[i])){
                                wrongAddress.setText("סניף אחר כבר משתמש בכתובת הזו");
                                allGood = false;
                            }
                        }
                        for(int i = index+1; i<branchesName.length; i++){
                            if(name.equals(branchesName[i])){
                                wrongAddress.setText("סניף אחר כבר משתמש בכתובת הזו");
                                allGood = false;
                            }
                        }
                    }
                    if(allGood) {
                        SetName setName = new SetName(name);
                        setName.run();
                        wrongAddress.setText("");
                        Toast.makeText(EditBranch.this, "הכתובת שונתה בהצלחה", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    wrongAddress.setText("נא מלא את הכתובת");
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

        findViewById(R.id.setSched).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for (int shift = 0; shift < numOfShifts; shift++) {
                    for (int day = 0; day < 7; day++) {
                        String shiftName = "shift" + (shift * 7 + day);
                        int shiftId = table_linear_layout.getResources().getIdentifier(shiftName, "id", getPackageName());
                        LinearLayout linearLayout = findViewById(shiftId);
                        schedRequired[shift][day] = linearLayout.getChildCount();
                        for (int i = 0; i < linearLayout.getChildCount(); i++) {
                            CharSequence comment = linearLayout.getChildAt(i).getContentDescription();
                            if (comment != null && comment.length() != 0)
                                constantComments[shift][day][i] = comment.toString();
                        }
                    }
                }
                branch.setSchedRequired(schedRequired);
                branch.setConstantComments(constantComments);

                String [] shiftsName = new String[numOfShifts];
                boolean allGood = true;

                EditText editText = table_linear_layout.findViewById(R.id.shiftName1);
                if(editText.getText() != null && editText.getText().length()>0)
                    shiftsName[1] = editText.getText().toString();
                else allGood = false;
                if(numOfShifts>1) {
                    editText = table_linear_layout.findViewById(R.id.shiftName2);
                    if (editText.getText() != null && editText.getText().length() > 0)
                        shiftsName[1] = editText.getText().toString();
                    else allGood = false;
                    if(numOfShifts == 3) {
                        editText = table_linear_layout.findViewById(R.id.shiftName3);
                        if (editText.getText() != null && editText.getText().length() > 0)
                            shiftsName[1] = editText.getText().toString();
                        else allGood = false;
                    }
                }
                if(allGood) {
                    findViewById(R.id.wrongShiftName).setVisibility(View.GONE);
                    Toast.makeText(EditBranch.this, "הסידור עודכן בהצלחה", Toast.LENGTH_SHORT).show();
                }
                else
                    findViewById(R.id.wrongShiftName).setVisibility(View.VISIBLE);

            }});
    }

    private void setShiftsName(LinearLayout layout){
        float size = 12;
        for (int day = 0; day < 7; day++) {
            String shiftName = "day" + day;
            int shiftId = getResources().getIdentifier(shiftName, "id", getPackageName());
            TextView textView = findViewById(shiftId);
            textView.setTextSize(size);
        }
        String[] shiftsName = branch.getShiftsName();
        TextView shiftNameTV = layout.findViewById(R.id.shiftName1);
        shiftNameTV.setText(shiftsName[0]);
        shiftNameTV.setTextSize(size);
        shiftNameTV = layout.findViewById(R.id.shiftName2);
        if(shiftsName.length > 1){
            shiftNameTV.setText(shiftsName[1]);
            shiftNameTV.setTextSize(size);
            shiftNameTV = layout.findViewById(R.id.shiftName3);
            if(shiftsName.length > 2) {
                shiftNameTV.setText(shiftsName[2]);
                shiftNameTV.setTextSize(size);
            }
            else
                layout.findViewById(R.id.row3).setVisibility(View.GONE);

        } else {
            layout.findViewById(R.id.row2).setVisibility(View.GONE);
            layout.findViewById(R.id.row3).setVisibility(View.GONE);
        }
    }

    class SetName implements Runnable{
        private String name;
        private SetName(String name){
            this.name = name;
        }
        @Override
        public void run() {
            branch.setAddress(name);
        }
    }

    class CreateSched implements Runnable {
        @Override
        public void run() {

            boolean allGood = true;
            boolean isEmptyTable = true;

            findViewById(R.id.wrongSchedRequired).setVisibility(View.INVISIBLE);


            for (int shift = 0; shift < numOfShifts && allGood; shift++) {
                for (int day = 0; day < 7 && allGood; day++) {
                    String shiftName = "shift" + (shift * 7 + day);
                    int shiftId = table_edit_text.getResources().getIdentifier(shiftName, "id", getPackageName());
                    EditText editText = findViewById(shiftId);
                    int numOfEmployees = 0;
                    if (editText.getText().length() != 0) {
                        try {
                            numOfEmployees = Integer.valueOf(editText.getText().toString());
                            isEmptyTable = false;
                        } catch (NumberFormatException e) {
                            findViewById(R.id.wrongSchedRequired).setVisibility(View.VISIBLE);
                            allGood = false;
                        }
                    }
                    shiftId = table_linear_layout.getResources().getIdentifier("l" + shiftName, "id", getPackageName());
                    LinearLayout layout = findViewById(shiftId);
                    layout.removeAllViewsInLayout();

                    for (int i = 0; i < numOfEmployees; i++) {
                        final Button button = new Button(EditBranch.this);

                        if(constantComments[shift][day][i] != null) {
                            button.setContentDescription(constantComments[shift][day][i]);
                            button.setText("עובד " + (i + 1)+"\n" +constantComments[shift][day][i]);
                        }else
                            button.setText("עובד " + (i + 1));
                        button.setTextSize(8);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                LayoutInflater inflater = getLayoutInflater();
                                final View layout = inflater.inflate(R.layout.dialog_textview, null);
                                final AlertDialog.Builder alert = new AlertDialog.Builder(EditBranch.this);
                                alert.setMessage("ההודעה קצרה תהיה קבועה למקום העובד בסידור");
                                alert.setTitle("הוסף הודעה");
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
                }
            }
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
}