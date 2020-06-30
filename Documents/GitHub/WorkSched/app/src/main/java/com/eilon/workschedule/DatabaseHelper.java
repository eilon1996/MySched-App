package com.eilon.workschedule;


import android.database.SQLException;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "contactsManager";

    // Table Names
    private static final String TABLE_EMPLOYEES = "employees";
    private static final String TABLE_BRANCHES = "branches";
    private static final String TABLE_COMPANIES = "companies";

    // column names for TABLE_EMPLOYEES
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_EMPLOYEE_JSON = "employee";
    private static final String KEY_COMPANY_BRANCH = "companyBranch";

    // column names for TABLE_BRANCHES
    private static final String KEY_BRANCH_NAME = "branchName";
    private static final String KEY_BRANCH_BELONG_TO = "companyName";
    private static final String KEY_BRANCH_JSON = "branch";

    // column names for TABLE_COMPANIES
    private static final String KEY_COMPANY_NAME = "name";
    private static final String KEY_COMPANY_JSON = "company";
    private static final String KEY_COMPANY_NET = "net";


    private static final String CREATE_TABLE_EMPLOYEES = "CREATE TABLE " + TABLE_EMPLOYEES + "(" + KEY_EMAIL + " TEXT," + KEY_PASSWORD + " TEXT," + KEY_EMPLOYEE_JSON + " TEXT," + KEY_COMPANY_BRANCH + " TEXT" + ")";
    private static final String CREATE_TABLE_BRANCHES = "CREATE TABLE " + TABLE_BRANCHES + "(" + KEY_BRANCH_NAME + " TEXT," + KEY_BRANCH_BELONG_TO + " TEXT," + KEY_BRANCH_JSON + " TEXT" + ")";
    private static final String CREATE_TABLE_COMPANIES = "CREATE TABLE " + TABLE_COMPANIES + "(" + KEY_COMPANY_NAME + " TEXT," + KEY_COMPANY_JSON + " TEXT,"+ KEY_COMPANY_NET + " NUMERIC" + ")";


    public void onCreate(SQLiteDatabase db) {
        // creating tables
        db.execSQL(CREATE_TABLE_EMPLOYEES);
        db.execSQL(CREATE_TABLE_BRANCHES);
        db.execSQL(CREATE_TABLE_COMPANIES);
        //do not put db.close() here !!!
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public void onUpgrade(SQLiteDatabase db, int i, int j) {

    }

    public void deleteData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_EMPLOYEES);
        db.execSQL("DELETE FROM " + TABLE_BRANCHES);
        db.execSQL("DELETE FROM " + TABLE_COMPANIES);
        db.close();

    }


    public boolean addEmplolyee(Employee employee) {

        Gson gson = new Gson();
        return addEmplolyee(employee.getEmail(), employee.getbYear(), gson.toJson(employee),
                employee.getCompanyName() +""+ employee.getBranchName());
    }


    public boolean addEmplolyee(String email, String bYear, String jsonEmployee, String companyBranch) {
        Cursor cursor = findEmployeeByEmail(email);
        if (cursor != null) return false;

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_EMAIL, email);
        contentValues.put(KEY_PASSWORD, bYear);
        contentValues.put(KEY_EMPLOYEE_JSON, jsonEmployee);
        contentValues.put(KEY_COMPANY_BRANCH, companyBranch);

        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.insert(TABLE_EMPLOYEES, null, contentValues);
        if (result == -1) return false;
        return true;
    }

    public boolean updateEmployee(String email, Employee newEmployee) {
        Gson gson = new Gson();
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("UPDATE " + TABLE_EMPLOYEES + " SET " + KEY_EMAIL + " = '" + newEmployee.getEmail() + "', " + KEY_PASSWORD + " = '" + newEmployee.getbYear() + "', "
                    + KEY_EMPLOYEE_JSON + " = '" + gson.toJson(newEmployee) + "', "
                    + KEY_COMPANY_BRANCH + " = '" + newEmployee.getCompanyName() + newEmployee.getBranchName() + "' "
                    + "WHERE " + KEY_EMAIL + " = '" + email + "'");

            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public Cursor findEmployeeByEmail(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_EMPLOYEES, null);
        if (data.moveToFirst()) {
            data = db.rawQuery("SELECT * FROM " + TABLE_EMPLOYEES + "  WHERE " + KEY_EMAIL + " = '" + email + "'", null);
            if (data.moveToFirst()) return data;
        }
        db.close();
        return null;
    }

    public Employee getEmployee(String email) {
        Cursor cursor = findEmployeeByEmail(email);
        if (cursor == null || !cursor.moveToFirst()) return null;
        Gson gson = new Gson();
        return gson.fromJson(cursor.getString(2), Employee.class);
    }

    public String getEmployeePassword(String email) {
        Cursor cursor = findEmployeeByEmail(email);
        if (cursor == null) return null;
        return cursor.getString(1);
    }

    public Employee[] getAllEmployeesForBranch(String companyBranch) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_EMPLOYEES, null);
        if (!data.moveToFirst()) return null;
        data = db.rawQuery("SELECT * FROM " + TABLE_EMPLOYEES + " WHERE " + KEY_COMPANY_BRANCH + " = '" + companyBranch + "'", null);
        if (!data.moveToFirst()) return null;
        int count = data.getCount();
        Employee[] result = new Employee[count];
        Gson gson = new Gson();
        for (int i = 0; i < count; i++) {
            result[i] = gson.fromJson(data.getString(2), Employee.class);
            data.moveToNext();
        }
        db.close();
        return result;
    }

    public String getAllEmployeesDetails() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_EMPLOYEES, null);
        if (!data.moveToFirst()) return null;
        int count = data.getCount();
        String result = "";
        for (int i = 0; i < count; i++) {
            result += data.getString(0)+","  +data.getString(1)+"," +data.getString(3)+"\n";
            data.moveToNext();
        }
        db.close();
        return result;
    }

    public Employee [] getAllEmployees(){

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_EMPLOYEES, null);
        if (data == null || !data.moveToFirst()) return null;
        int count = data.getCount();
        Employee[] result = new Employee[count];
        Gson gson = new Gson();
        for (int i = 0; i < count; i++) {
            result[i] = gson.fromJson(data.getString(2), Employee.class);
            data.moveToNext();
        }
        db.close();
        return result;
    }


    public String[] getAllEmployeesEmailForBranch(String companyBranch) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_EMPLOYEES, null);
        if (!data.moveToFirst()) return null;
        data = db.rawQuery("SELECT * FROM " + TABLE_EMPLOYEES + "  WHERE " + KEY_COMPANY_BRANCH + " = '" + companyBranch + "'", null);
        if (!data.moveToFirst()) return null;
        int count = data.getCount();
        String[] result = new String[count];
        Gson gson = new Gson();
        for (int i = 0; i < count; i++) {
            result[i] = data.getString(0);
            data.moveToNext();
        }
        db.close();
        return result;
    }

    public String getEmployeeInBranchAtIndexJson(String companyBranch, int index) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_EMPLOYEES, null);
        if (!data.moveToFirst()) return null;
        data = db.rawQuery("SELECT * FROM " + TABLE_EMPLOYEES + "  WHERE " + KEY_COMPANY_BRANCH + " = '" + companyBranch + "'", null);
        if (!data.moveToFirst()) return null;
        int count = data.getCount();
        if(count <=index) return null;

        for (int i = 0; i <= index; i++) {
            data.moveToNext();
        }
        db.close();
        return data.getString(2);
    }

    public void deleteEmployee(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_EMPLOYEES + " WHERE " + KEY_EMAIL + " = '" + email + "'");
        close();
    }

    public boolean addBranch(Branch branch) {
        Gson gson = new Gson();
        return addBranch(branch.getBranchName(), branch.getCompanyName(), gson.toJson(branch));
    }


    public boolean addBranch(String branchName, String companyName, String jsonBranch) {
        Cursor cursor = findBranch(companyName, branchName);
        if (cursor != null) return false;

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_BRANCH_NAME, branchName);
        contentValues.put(KEY_BRANCH_BELONG_TO, companyName);
        contentValues.put(KEY_BRANCH_JSON, jsonBranch);

        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.insert(TABLE_BRANCHES, null, contentValues);
        if (result == -1) return false;
        return true;
    }

    public boolean updateBranch(Branch branch) {
        Gson gson = new Gson();
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("UPDATE " + TABLE_BRANCHES + " SET " + KEY_BRANCH_JSON + " = '" + gson.toJson(branch) +"'"
                    + " WHERE " + KEY_BRANCH_NAME + " = '" + branch.getBranchName() + "'"
                    + " AND " + KEY_BRANCH_BELONG_TO + " = '" + branch.getCompanyName() + "'");
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public Cursor findBranch(String company, String branch) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_BRANCHES, null);
        if (data.moveToFirst()) {
            data = db.rawQuery("SELECT * FROM " + TABLE_BRANCHES + "  WHERE " + KEY_BRANCH_BELONG_TO + " = '" + company + "'"
                    + " AND " + KEY_BRANCH_NAME + " = '" + branch + "'", null);
            if (data.moveToFirst()) return data;
        }
        db.close();
        return null;
    }

    public Branch getBranch(String company, String branch) {
        Cursor cursor = findBranch(company, branch);
        if (cursor == null) return null;
        Gson gson = new Gson();
        return gson.fromJson(cursor.getString(2), Branch.class);
    }

    public Branch[] getAllBranchesForCompany(String company) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_BRANCHES, null);
        if (!data.moveToFirst()) return null;
        data = db.rawQuery("SELECT * FROM " + TABLE_BRANCHES + " WHERE " + KEY_BRANCH_BELONG_TO + " = '" + company + "'", null);
        if (!data.moveToFirst()) return null;
        int count = data.getCount();
        Branch[] result = new Branch[count];
        Gson gson = new Gson();
        for (int i = 0; i < count; i++) {
            result[i] = gson.fromJson(data.getString(2), Branch.class);
            data.moveToNext();
        }
        db.close();
        return result;
    }

    public String[] getAllBranchesNameForCompany(String company) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_BRANCHES, null);
        if (!data.moveToFirst()) return null;
        data = db.rawQuery("SELECT * FROM " + TABLE_BRANCHES + "  WHERE " + KEY_BRANCH_BELONG_TO + " = '" + company + "'", null);
        if (!data.moveToFirst()) return null;
        int count = data.getCount();
        String[] result = new String[count];
        Gson gson = new Gson();
        for (int i = 0; i < count; i++) {
            result[i] = data.getString(0);
            data.moveToNext();
        }
        db.close();
        return result;
    }

    public Branch[] getAllBranches() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_BRANCHES, null);
        if (!data.moveToFirst()) return null;
        int count = data.getCount();
        Branch[] result = new Branch[count];
        Gson gson = new Gson();
        for (int i = 0; i < count; i++) {
            result[i] = gson.fromJson(data.getString(2), Branch.class);
            data.moveToNext();
        }
        db.close();
        return result;
    }


    public void deleteBranch(String company, String branch) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_BRANCHES + " WHERE " + KEY_BRANCH_BELONG_TO + " = '" + company + "' AND " + KEY_BRANCH_NAME + " = '" + branch + "'");
        close();
    }

    public boolean addCompany(Company company) {
        Gson gson = new Gson();
        return addCompany(company.getComapnyName(), gson.toJson(company), company.getNet());
    }


    public boolean addCompany(String companyName, String jsonCompany, boolean net) {
        Cursor cursor = findCompany(companyName);
        if (cursor != null) return false;
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_COMPANY_NAME, companyName);
        contentValues.put(KEY_COMPANY_JSON, jsonCompany);
        int netInt = (net)? 1:0;
        contentValues.put(KEY_COMPANY_NET, netInt);

        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.insert(TABLE_COMPANIES, null, contentValues);
        if (result == -1) return false;
        return true;
    }

    public boolean updateCompany(Company company) {
        Gson gson = new Gson();
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("UPDATE " + TABLE_COMPANIES + " SET " + KEY_COMPANY_JSON + " = '" + gson.toJson(company)+"'"
                    + " WHERE " + KEY_COMPANY_NAME + " = '" + company.getComapnyName() + "'");
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public Cursor findCompany(String companyName) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_COMPANIES, null);
        if (data.moveToFirst()) {
            data = db.rawQuery("SELECT * FROM " + TABLE_COMPANIES + "  WHERE " + KEY_COMPANY_NAME + " = '" + companyName + "'", null);
            if (data.moveToFirst()) return data;
        }
        db.close();
        return null;
    }

    public Company getCompany(String companyName) {
        Cursor cursor = findCompany(companyName);
        if (cursor == null) return null;
        Gson gson = new Gson();
        return gson.fromJson(cursor.getString(1), Company.class);
    }

    public Company [] getAllCompanies(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_COMPANIES, null);
        if (!data.moveToFirst()) return null;
        int count = data.getCount();
        Company[] result = new Company[count];
        Gson gson = new Gson();
        for (int i = 0; i < count; i++) {
            result[i] = gson.fromJson(data.getString(1), Company.class);
            data.moveToNext();
        }
        db.close();
        return result;
    }


    public String [] getAllCompaniesNames(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_COMPANIES, null);
        if (!data.moveToFirst()) return null;
        int count = data.getCount();
        String[] result = new String[count];
        Gson gson = new Gson();
        for (int i = 0; i < count; i++) {
            result[i] = data.getString(0);
            data.moveToNext();
        }
        db.close();
        return result;
    }


    public String [] getAllNetCompaniesNames(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_COMPANIES + " WHERE "+ KEY_COMPANY_NET+ " = '1'", null);
        if (!data.moveToFirst()) return null;
        int count = data.getCount();
        String[] result = new String[count];
        Gson gson = new Gson();
        for (int i = 0; i < count; i++) {
            result[i] = data.getString(0);
            data.moveToNext();
        }
        db.close();
        return result;
    }

    public boolean getNet(String companyName){
        Cursor company = findCompany(companyName);
        if(company == null) return false;
        int net =company.getInt(2);
        return (net==1);
    }
}