package com.eilon.workschedule;

import com.eilon.App;

public class Company {
    private String name;
    private String [] branchesName;
    private int numOfBranches;
    private int numOfShifts;
    private String [] professions;
    private String type; // חנות/מסעדה/חברת שמירות
    private boolean net;

    public Company(String name,  int numOfShifts,String [] professions, String type, boolean net){
        this.name = name.toLowerCase();
        this.numOfShifts = numOfShifts;
        this.professions = professions;
        this.type = type.toLowerCase();
        this.net = net;

        DatabaseHelper databaseHelper = new DatabaseHelper(App.getContext());
        databaseHelper.addCompany(this);
    }

    public String getComapnyName() {
        return name;
    }


    public void setNumOfShifts(int numOfShifts) {
        this.numOfShifts = numOfShifts;
        Update update = new Update();
        update.run();
    }

    public int getNumOfShifts(){return numOfShifts;}

    public void setProfessions(String [] professions) {
        this.professions = professions;
        Update update = new Update();
        update.run();
    }
    public String[] getProfessions() {
        return professions;
    }
    public int getNumOfProfessions() {
        return professions.length;
    }


    public void setType(String type) {
        this.type = type;
        Update update = new Update();
        update.run();
    }
    public String getType() {return type;}

    public void setNet(boolean net) {
        this.net = net;
        Update update = new Update();
        update.run();
    }
    public boolean getNet(){return net;}

    public int getNumOfBranches() {
        return numOfBranches;
    }
    public void setNumOfBranches(int i) {
        numOfBranches = i;
        Update update = new Update();
        update.run();
    }
    public Branch [] getAllBranches(){
        DatabaseHelper databaseHelper = new DatabaseHelper(App.getContext());
        Branch [] result = databaseHelper.getAllBranchesForCompany(name);
        databaseHelper.close();
        return result;
    }

    public String[] getAllBranchesAddresses() {
        return branchesName;
    }

    public int getBranchIndex(String address) {
        if(branchesName == null) return -1;
        for (int i = 0; i < numOfBranches; i++) {
            if (branchesName[i].equals(address)) return i;
        }
        return -1;
    }

    public String getBranchAddress(int index){
            return branchesName[index];
    }

    public Branch getBranch(String address){
        DatabaseHelper databaseHelper = new DatabaseHelper(App.getContext());
        return databaseHelper.getBranch(name, address);
    }

    public Branch getBranch(int index){
        return getBranch(getBranchAddress(index));
    }

    public void setBranchesName(String[] branchesName) {
        this.branchesName = branchesName;
        Update update = new Update();
        update.run();
    }

    public void addBranch(String address){
        String [] temp = new String[numOfBranches+1];
        for(int i = 0; i < numOfBranches; i ++)
            temp[i] = branchesName[i];
        temp[numOfBranches] = address;
        numOfBranches++;
        Update update = new Update();
        update.run();
    }
    public void deleteBranch(String address) {
        numOfBranches--;
        String[] temp = new String[numOfBranches];
        int i;
        for (i = 0; i < numOfBranches && !branchesName[i].equals(address); i++)
            temp[i] = branchesName[i];
        while (i < numOfBranches) {
            temp[i] = branchesName[i + 1];
            i++;
        }
        temp[numOfBranches-1] = address;
        DatabaseHelper databaseHelper = new DatabaseHelper(App.getContext());
        databaseHelper.deleteBranch(name, address);
        Update update = new Update();
        update.run();
    }
    public void updateBranch(String oldAddress, String newAddress){
        for(int i = 0; i < numOfBranches; i ++)
            if(branchesName[i].equals(oldAddress)) branchesName[i] = newAddress;
        Update update = new Update();
        update.run();
    }
    public void switchWeek(){
        Branch [] branches = getAllBranches();
        Employee [] employees;
        for(int i = 0; i < branches.length; i++){
            branches[i].switchWeek();
            employees = branches[i].getAllEmployees();
            for(int j = 0; j < employees.length; j++)
                employees[i].switchWeek();
        }
    }


    class Update implements Runnable{
        public Update(){
        }
        @Override
        public void run() {
            DatabaseHelper databaseHelper = new DatabaseHelper(App.getContext());
            boolean result = databaseHelper.updateCompany(Company.this);
            databaseHelper.close();
        }
    }

}
