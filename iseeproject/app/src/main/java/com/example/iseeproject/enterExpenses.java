package com.example.iseeproject;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class enterExpenses extends AppCompatActivity  implements AdapterView.OnItemSelectedListener
{
    dbHandler peopleDB;
    EditText datepick, amount;
    Calendar myCalendar;
    static String USERPREF = "USER"; // or other values
    private ImageButton menuBtn;
    Spinner spinner,spinner1;
    DatePickerDialog.OnDateSetListener date;
    String expenseTime,username,category,payment_method;
    double expAmount;
    menuHandler MenuHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_expenses);

        Bundle b = getIntent().getExtras();
        if (b != null)
            username = b.getString("username");

        MenuHandler = new menuHandler(enterExpenses.this, username);
        peopleDB = new dbHandler(this);
        User usr = peopleDB.getUser(username);
        //getting expenses categories names from database and avoiding hardcoded values

        Set<String> cats = peopleDB.getThresholds(username).keySet();
        List<String> categories1= Arrays.asList(cats.toArray(new String[cats.size()]));

        List<String> categories = new ArrayList<String>();
        categories.add("Cash");
        categories.add("Card  ");
        categories.add("Online");


        spinner= (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> datAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, categories);
        datAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(datAdapter);
        spinner.setOnItemSelectedListener(this);

        spinner1 =(Spinner)findViewById(R.id.spinnerCategory);

        ArrayAdapter<String> datAdapter1 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, categories1);
        datAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(datAdapter1);
        spinner1.setSelected(false);
//        spinner1.setSelection(0,true);
//        spinner1.setOnItemSelectedListener(this);

        peopleDB = new dbHandler(this);

        menuBtn  = (ImageButton) findViewById(R.id.menuLines);
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                PopupMenu popup = new PopupMenu(enterExpenses.this, v);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return MenuHandler.onMenuItemClick(item);
                    }
                });
                popup.inflate(R.menu.drawermenu);
                popup.show();
            }
        });


        //we set the calendar view in the ui
        myCalendar = Calendar.getInstance();

        datepick = (EditText) findViewById(R.id.selectDate);
        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub

                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        datepick.setOnClickListener(new View.OnClickListener() {

              @Override
               public void onClick(View v) {
                   // TODO Auto-generated method stub
                  DatePickerDialog mDatePicker = new DatePickerDialog(enterExpenses.this, date, myCalendar
                          .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
                  mDatePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
                  mDatePicker.show();
              }

          }

        );

        //we set the button behaviour
        Button confirmbtn = (Button) findViewById(R.id.confirmBtn);
        Button backbtn = (Button) findViewById(R.id.backBtn);

        confirmbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                //getting values of the parameters of the new expense

                String category = "";
                //TODO Ensure that only one button is checked at a time

                amount = (EditText) findViewById(R.id.amountText);
                //TODO check if payment method is also selected
                if (TextUtils.isEmpty(datepick.getText())  || TextUtils.isEmpty(amount.getText())  ){
                    Toast t = Toast.makeText(enterExpenses.this,
                            "All fields must be given", Toast.LENGTH_LONG);
                    t.show();
                }
                else {
                    checkInput();
                }
            }
        });

        backbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                MenuHandler.goToHomePage();
            }
        });

    }

    public void checkInput() {
        User user = peopleDB.getUser(username);
        //get sum of money spent in expenses
        expAmount = Double.parseDouble(amount.getText().toString());
        expenseTime = datepick.getText().toString();

        //get values of spinners
        payment_method = spinner.getSelectedItem().toString();
        category = spinner1.getSelectedItem().toString();
        if(expAmount <= 0.0) {
            Toast t = Toast.makeText(enterExpenses.this,
                    "Expense price should be more than 0", Toast.LENGTH_LONG);
            t.show();
        }
        else {
            double sum = 0;
            Map<String, Double> cat = peopleDB.getThresholds(username);
            if (peopleDB.expensesExist(user)) {
                List<Expenses> exp = peopleDB.getAllExpenses(user);
                for (Expenses expense : exp) {
                    if (expense.getCategory().equals(category)){
                        sum += expense.getPrice();
                    }
                }}
            if ((sum + expAmount) > cat.get(category)) {
                AlertDialog.Builder bx1 = new AlertDialog.Builder(enterExpenses.this);
                bx1.setCancelable(true);
                bx1.setTitle("Attention!You are about to overcome "+
                        category +" budget");

                bx1.setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                        //creating the expense instance and adding it to the database
                        addNewExp();

                    }
                });
                bx1.setPositiveButton("Update Category Threshold", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                        MenuHandler.goToDetails();

                    }
                });

                AlertDialog alertDialog = bx1.create();
                alertDialog.show();
            }
            else {
                //if expenses don't overcome threshold
                addNewExp();
            }
        }
    }

    public void addNewExp() {
        //Expenses newExpense = new Expenses(expenseTime,username,expAmount,category,payment_method);
        //the id is auto incremented from the database, therefore we don't care about the value we insert here
        Expenses newExpense = new Expenses(expenseTime,0,username,expAmount,category,payment_method);

        peopleDB.addExpenses(newExpense);
        Toast t = Toast.makeText(enterExpenses.this,
                "Successful addition of new expense", Toast.LENGTH_LONG);
        t.show();
        //and then refreshing
        Intent myIntent = new Intent(enterExpenses.this, enterExpenses.class);
        Bundle b = new Bundle();
        b.putString("username",username);

        myIntent.putExtras(b); //Put your id to your next Intent
        startActivity(myIntent);
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.welcomeUseractivity) {
            return true;
        }

        return enterExpenses.super.onOptionsItemSelected(item);
    }


    private void updateLabel() {
        String myFormat = "MM-dd-yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.GERMANY);

        datepick.setText(sdf.format(myCalendar.getTime()));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        /*
        if (parent.getId() == R.id.spinner) {
            // On selecting a spinner item
            String item = parent.getItemAtPosition(position).toString();
            // Showing selected spinner item
            Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
        }
        else if(parent.getId() == R.id.spinnerCategory){
            // On selecting a spinner item
            String item1 = parent.getItemAtPosition(position).toString();
            // Showing selected spinner item
            Toast.makeText(parent.getContext(), "Selected: " + item1, Toast.LENGTH_LONG).show();
        }*/

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

        Toast t = Toast.makeText(enterExpenses.this,
                "Select one Category", Toast.LENGTH_LONG);
        t.show();
    }
}