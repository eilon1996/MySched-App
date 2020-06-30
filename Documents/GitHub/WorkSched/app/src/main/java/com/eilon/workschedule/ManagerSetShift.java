package com.eilon.workschedule;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.eilon.App;
import com.google.gson.Gson;

public class ManagerSetShift extends AppCompatActivity {


    LinearLayout working;
    LinearLayout workingP;
    LinearLayout canWork;
    LinearLayout cannotWork;
    LinearLayout canWorkP;

    TextView title;
    String shiftName;
    int employeesNeeded;
    Button newEmployee;

    Employee[] allEmployees;

    int shift;
    int day;
    Branch branch;
    String []order;
    String [][][] orders;

    SharedPreferences sp;
    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manager_set_shift);
        // TODO לבדוק שוב את ההוספת עובד גם באופן ידני וגם כעובד שלם

        working = findViewById(R.id.working);
        workingP = findViewById(R.id.workingP);
        canWork = findViewById(R.id.canWork);
        cannotWork = findViewById(R.id.cannotWork);
        canWorkP = findViewById(R.id.canWorkP);

        newEmployee = findViewById(R.id.newEmployee);

        //     working.setOnDragListener(dragListener);
        workingP.setOnDragListener(dragListener);
        //    canWork.setOnDragListener(dragListener);
        //    cannotWork.setOnDragListener(dragListener);
        canWorkP.setOnDragListener(dragListener);

        //restore all employees

        sp = getSharedPreferences("key", 0);
        gson = new Gson();
        branch = gson.fromJson(sp.getString("branch", null), Branch.class);

        allEmployees = branch.getAllEmployees();
        String shiftInNumbers = sp.getString("shift", null);
        shift = Integer.valueOf(String.valueOf(shiftInNumbers.charAt(0)));
        day = Integer.valueOf(String.valueOf(shiftInNumbers.charAt(1)));
        orders = branch.getOrders();
        order = orders[shift][day];
        Simplify simplify = new Simplify();
        shiftName = simplify.getShiftName(shiftInNumbers);

        //create a title for the activity according to the day, shift, and employees needed
        title = findViewById(R.id.title);

        FillLayout fillLayout = new FillLayout();
        fillLayout.run();

        employeesNeeded = branch.getSchedRequired()[shift][day];

        if (employeesNeeded == 1) title.setText(shiftName + " צריך עובד 1");
        else title.setText(shiftName + "צריכים " + employeesNeeded + " עובדים");

        final int difference = working.getChildCount() - employeesNeeded;
        if (difference > 0)
            title.setText(shiftName + "- יש " + difference + " יותר מדי ");
        else if (difference < 0)
            title.setText(shiftName + "- חסר " + (difference * -1));
        else title.setText(shiftName);

        //updating the shift
        Button update = findViewById(R.id.update);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Update update = new Update();
                update.run();
            }
        });

        newEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LayoutInflater inflater = getLayoutInflater();
                final View layout = inflater.inflate(R.layout.dialog_textview, null);
                final AlertDialog.Builder alert = new AlertDialog.Builder(ManagerSetShift.this);
                alert.setMessage("כתוב את שם העובד");
                alert.setTitle("הוסף עובד");
                alert.setView(layout)
                        .setPositiveButton("הוסף", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                EditText editText = layout.findViewById(R.id.comment);
                                String name = editText.getText().toString();
                                if(name.contains(",")){
                                    Toast.makeText(ManagerSetShift.this, "השם לא יכול להכיל , ", Toast.LENGTH_SHORT).show();
                                    alert.create();
                                    alert.show();
                                }
                                Button button = createButton(-1);
                                button.setBackgroundColor(Color.LTGRAY);
                                button.setText(name);
                                button.setContentDescription("*");// describe the user place in the data base
                                button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(final View view) {
                                        Toast.makeText(ManagerSetShift.this, "אי אפשר לכתוב הערה לעובד שהוספת", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                working.addView(button);
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
    }

        class Update implements Runnable{
            @Override
            public void run() {

                LinearLayout workingPlace;
                Button button;
                String nextWeek = "";
                for (int j = 0; j < order.length; j++) {
                    order[j] = "";
                    switch (j) {
                        case 0:
                            workingPlace = findViewById(R.id.working);
                            break;
                        case 1:
                            workingPlace = findViewById(R.id.canWork);
                            break;
                        default:
                            workingPlace = findViewById(R.id.cannotWork);
                    }
                    for (int i = 0; i < workingPlace.getChildCount(); i++) {
                        button = (Button) workingPlace.getChildAt(i);
                        String content = button.getContentDescription().toString();
                        if (content != null) {
                            if (content.equals("*")) {
                                String [] name = button.getText().toString().split(" ");
                                if(name.length == 1) {
                                    order[j] += "," + name[0];
                                    if (j == 0) nextWeek += "," + name[0];
                                } else {
                                    order[j] += "," + name[0]+"\n" +name[1];
                                    if (j == 0) nextWeek += "," + name[0]+"\n"+name[1];
                                }
                            }
                            else{
                                order[j] += "," +content;
                                if(j == 0) nextWeek += ","+allEmployees[Integer.valueOf(content)].getFullNameTwoLines();
                            }
                        }
                    }
                    if (order[j].length() >0) order[j] = order[j].substring(1);
                    if (j == 0 && nextWeek.length() >0) nextWeek = nextWeek.substring(1);
                }
                int difference = working.getChildCount() - employeesNeeded;
                if (difference < 0){
                    branch.setShortEmployees(shift,day,difference*-1);
                } else branch.setShortEmployees(shift,day,0);
                branch.updateOrders(shift,day,order);
                branch.setNextWeek(nextWeek,shift,day);
                SharedPreferences.Editor edit = sp.edit();
                edit.putString("branch", gson.toJson(branch)).apply();
                Intent intent = new Intent(ManagerSetShift.this, ManagerSetSched.class);
                startActivity(intent);

            }
        }

        class FillLayout implements Runnable{
            @Override
            public void run() {
                //organise workers by their status
                LinearLayout workingPlace;
                Drawable color;
                //int color;
                for (int j = 0; j < order.length; j++) {
                    switch (String.valueOf(j)) {
                        case "0":
                            workingPlace = findViewById(R.id.working);
                            color =  ContextCompat.getDrawable(App.getContext(), R.drawable.light_grey);
                            break;
                        case "1":
                            workingPlace = findViewById(R.id.canWork);
                            color = ContextCompat.getDrawable(App.getContext(), R.drawable.light_grey);
                            break;
                        default:
                            workingPlace = findViewById(R.id.cannotWork);
                            color = ContextCompat.getDrawable(App.getContext(), R.drawable.red);
                    }

                    String[] scan = order[j].split(",");
                    if (scan[0].length() > 0) {
                        for (int k = 0; scan != null && k < scan.length; k++) {
                            Button button;
                            try {
                                int i = Integer.valueOf(scan[k]);
                                button = createButton(i);
                                button.setText(allEmployees[i].getFullNameOneLine());
                            } catch (NumberFormatException e) {
                                button = createButton(-1);
                                button.setText(scan[k]);
                            }
                            button.setBackground(color);
                            workingPlace.addView(button);
                        }
                    }
                }


            }
        }

    private Button createButton(int index) {

        Button b = new Button(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(5, 15, 5, 15);
        b.setLayoutParams(lp);
        b.setGravity(Gravity.CENTER);
        b.setOnLongClickListener(longclickListener);
        b.setOnDragListener(dragListener);


        if (index != -1) {
            final Employee employee = allEmployees[index];
            //on short click make a dialog to add a comment for each user // and watch his comment
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    LayoutInflater inflater = getLayoutInflater();
                    final View layout = inflater.inflate(R.layout.dialog_textview, null);
                    AlertDialog.Builder alert = new AlertDialog.Builder(ManagerSetShift.this);
                    alert.setMessage("הוסף הערה אישית");
                    alert.setTitle("הערה");
                    alert.setView(layout)
                            .setPositiveButton("שלח", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    EditText editText = layout.findViewById(R.id.comment);
                                    String comment = editText.getText().toString();
                                    employee.setNextWeekManagerComments(shiftName + " -f " + comment);
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
            b.setContentDescription(String.valueOf(index));
        } else {

            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    Toast.makeText(ManagerSetShift.this, "אי אפשר לכתוב הערה לעובד שהוספת", Toast.LENGTH_SHORT).show();
                }
            });

            b.setContentDescription("*");
        }

        return b;
    }


    View.OnLongClickListener longclickListener = new View.OnLongClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        @SuppressWarnings( "deprecation" )
        public boolean onLongClick(View v) {
            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder dragShadowBuilder = new View.DragShadowBuilder(v);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                v.startDragAndDrop(data, dragShadowBuilder, v, 0);
            } else {
                v.startDrag(data, dragShadowBuilder, v, 0);
            }
            //    v.startDrag(data, dragShadowBuilder, v, 0);

            // v.startDragAndDrop(data, dragShadowBuilder, v, 0);

            return true;
        }
    };


    View.OnDragListener dragListener = new View.OnDragListener() {
        LinearLayout parentV;

        @Override
        public boolean onDrag(View erea, DragEvent event) {
            int dragEvent = event.getAction();
            final View view = (View) event.getLocalState();//התצוגה שנגררת
            if (view.getParent() != null) {
                parentV = (LinearLayout) view.getParent();
            }
            LinearLayout parentE = null;
            if (erea.getClass() == Button.class) parentE = (LinearLayout) erea.getParent();
            else if (erea == workingP) parentE = working;
            else if (((ColorDrawable) view.getBackground()).getColor() == Color.RED)
                parentE = cannotWork;
            else parentE = canWork;

            switch (dragEvent) {
                case DragEvent.ACTION_DRAG_ENTERED:
                    if (view != erea) {
                        if(parentE == working){
                            if (view.getParent() != null)
                                parentV.removeView(view);
                            parentE.addView(view,parentE.indexOfChild(erea));
                        }
                        else if(erea == working){
                            if (view.getParent() != null)
                                parentV.removeView(view);
                            LinearLayout l = (LinearLayout)erea;
                            l.addView(view);
                        }
                    }
                    break;

                case DragEvent.ACTION_DRAG_EXITED:
                    if (parentE != working && view.getParent() != null)
                        parentV.removeView(view);

                    break;

                case DragEvent.ACTION_DROP:
                    if (parentE != working && erea != working) {
                        if(view.getBackground() == ContextCompat.getDrawable(App.getContext(), R.drawable.red)){
                            if (view.getParent() != null)
                                parentV.removeView(view);
                            cannotWork.addView(view);
                        } else {
                            if (view.getParent() != null)
                                parentV.removeView(view);
                            canWork.addView(view);
                        }
                    }
                    break;
            }
            //update title
            int difference = working.getChildCount() - employeesNeeded;
            if (difference > 0)
                title.setText(shiftName + "- יש " + difference + " יותר מדי ");
            else if (difference < 0)
                title.setText(shiftName + "- חסר " + difference);
            else title.setText(shiftName);

            return true;
        }
    };
}
