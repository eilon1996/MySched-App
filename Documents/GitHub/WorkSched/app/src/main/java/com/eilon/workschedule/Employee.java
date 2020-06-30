package com.eilon.workschedule;

import com.eilon.App;

public class Employee {
    private String name;
    private String fName;
    private String email;
    int indexInBranch;
    private String bYear;
    private String password;
    private String branch;
    private String company;
    private String phone;
    private String address;
    private boolean gander; // true male false female
    private  boolean [] professions; // acording to the company array professions

    private double hourlyPay;
    // 0 - regular worker, 1 - branch manager, 2 - region manager
    private int manager;
    private boolean authorized;

    private String nextWeekManagerComments;
    private String thisWeekManagerComments;
    private String lastWeekManagerComments;

    private boolean [][] possibleShifts;

    //show if the user working in other branches
    //shift,day,branch index
    private String nextWeekHelp;
    private String thisWeekHelp;
    private String lastWeekHelp;

    private Employee next;


    public Employee(String name,String fName, String email, String bYear, String company, String branch, String phone, String address,boolean gander, boolean [] professions){
        this.name = name.toLowerCase();
        this.fName = fName.toLowerCase();
        this.email = email.toLowerCase();
        this.bYear = bYear.toLowerCase();
        this.password = this.bYear;
        this.branch = branch.toLowerCase();
        this.company = company.toLowerCase();
        this.phone = phone.toLowerCase();
        this.address = address.toLowerCase();
        this.gander = gander;
        this.professions = professions;
        Branch b = getBranch();
        if(b.getNumOfEmployees() == 0){
            if(getCompany().getNumOfBranches() == 0)
                this.manager = 2;
            else this.manager = 1;
        } else this.manager = 0;
        indexInBranch =b.getNumOfEmployees();
        b.addEmployee(name+"\n"+fName);
        possibleShifts = new boolean[getCompany().getNumOfShifts()][7];
        this.nextWeekManagerComments = "";
        this.nextWeekHelp = "";

        DatabaseHelper db = new DatabaseHelper(App.getContext());
        db.addEmplolyee(this);
    }

    public void setPossibleShifts(boolean [][] possibleShifts){
        this.possibleShifts = possibleShifts;
        Update update = new Update();
        update.run();
    }

    public void setNext(Employee next) {
        this.next = next;
    }

    public Employee getNext() {
        return next;
    }

    public void setName(String name) {
        this.name = name;
        Update update = new Update();
        update.run();
    }
    public String getFullNameTwoLines() {
        return name+"\n"+fName;
    }
    public String getFullNameOneLine() {
        return name+" "+fName;
    }

    public void setEmail(String email) {
        String oldEmail = this.email;
        this.email = email;
        DatabaseHelper databaseHelper = new DatabaseHelper(App.getContext());
        databaseHelper.updateEmployee(oldEmail,this);
    }
    public String getEmail() {
        return email;
    }

    public int getIndexInBranch() {
        return indexInBranch;
    }

    public void setIndexInBranch(int indexInBranch) {
        this.indexInBranch = indexInBranch;
    }

    public void setbYear(String bYear) {
        this.bYear = bYear;
        Update update = new Update();
        update.run();
    }
    public String getbYear() {
        return bYear;
    }

    public void setPassword(String password) {
        this.password = password;
        Update update = new Update();
        update.run();
    }
    public String getPassword() { return password; }

    public void setBranch(String branch) {
        this.branch = branch;
        Update update = new Update();
        update.run();
    }
    public String getBranchName() {
        return branch;
    }
    public Branch getBranch(){
        DatabaseHelper db = new DatabaseHelper(App.getContext());
        Branch b = db.getBranch(company, branch);
        db.close();
        return b;
    }

    // you cant setCompany(), you will have to create a new employee in that company
    public String getCompanyName() {return company;}
    public Company getCompany(){
        DatabaseHelper db = new DatabaseHelper(App.getContext());
        Company c = db.getCompany(company);
        db.close();
        return c;
    }

    public void setPhone(String phone) {
        this.phone = phone;
        Update update = new Update();
        update.run();
    }
    public String getPhone() {  return phone; }

    public void setAddress(String address) {
        this.address = address;
        Update update = new Update();
        update.run();
    }
    public String getAddress() { return address; }

    public void setProfessions(boolean [] professions){
        this.professions = professions;
        Update update = new Update();
        update.run();
    }
    public boolean [] getProfessions() {
        return professions;
    }

    public void setHourlyPay(double hp) {
        hourlyPay = hp;
        Update update = new Update();
        update.run();
    }
    public double getHourlyPay() {
        return hourlyPay;
    }

    public void setManger(int m) {
        manager = m;
        Update update = new Update();
        update.run();
    }
    public int getManger() {
        return manager;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
        Update update = new Update();
        update.run();
    }
    public boolean getAuthorized(){
        return authorized;
    }

    public void setNextWeekManagerComments(String nextWeekManagerComments) {
        this.nextWeekManagerComments += nextWeekManagerComments + "\n";
        Update update = new Update();
        update.run();
    }
    public String getNextWeekManagerComments(){
        if(nextWeekManagerComments.length()>0)
            return nextWeekManagerComments.substring(0,nextWeekManagerComments.length()-2);
        else return null;
    }
    public String getThisWeekManagerComments(){
        if(thisWeekManagerComments == null) return null;
        if(thisWeekManagerComments.length()>0)
            return thisWeekManagerComments.substring(0,thisWeekManagerComments.length()-2);
        else return null;
    }
    public String getLastWeekManagerComments(){
        if(lastWeekManagerComments.length()>0)
                return lastWeekManagerComments.substring(0,lastWeekManagerComments.length()-2);
        else return null;
    }

    public void addNextWeekHelp(int shift,int day, int branchIndex) {
        if(nextWeekHelp.length()>0)
            this.nextWeekHelp += ","+shift+day+branchIndex;
        else
            this.nextWeekHelp += shift+day+branchIndex;
        Update update = new Update();
        update.run();
    }

    public void deleteNextWeekHelp(String shiftDay) {
        int index = this.nextWeekHelp.indexOf(shiftDay);
        if(index == -1) return;
        this.nextWeekHelp = nextWeekHelp.substring(0,index)+nextWeekHelp.substring(index+2);
        Update update = new Update();
        update.run();
    }

    public String [][] getNextWeek(){
        Branch branch = getBranch();
        String [][] resulte = branch.getNextWeek();
        if(!branch.getSchedReady() || resulte==null) return null;
        if(nextWeekHelp.length()>0){
            String [] help = nextWeekHelp.split(",");
            for(int i = 0; i < help.length; i++)
                resulte[Integer.valueOf(help[i].substring(0,1))][Integer.valueOf(help[i].substring(1,2))] =
                        getCompany().getBranchAddress(Integer.valueOf(help[i].substring(2)));
        }
        return resulte;
    }
    public String [][] getThisWeek(){
        Branch branch = getBranch();
        String [][] resulte = branch.getThisWeek();
        if(resulte==null) return null;
        if(thisWeekHelp.length()>0){
            String [] help = thisWeekHelp.split(",");
            for(int i = 0; i < help.length; i++)
                resulte[Integer.valueOf(help[i].substring(0,1))][Integer.valueOf(help[i].substring(1,2))] =
                        getCompany().getBranchAddress(Integer.valueOf(help[i].substring(2)));
        }
        return resulte;
    }
    public String [][] getLastWeek(){
        Branch branch = getBranch();
        String [][] resulte = branch.getLastWeek();
        if(resulte==null) return null;
        if(lastWeekHelp.length()>0){
            String [] help = lastWeekHelp.split(",");
            for(int i = 0; i < help.length; i++)
                resulte[Integer.valueOf(help[i].substring(0,1))][Integer.valueOf(help[i].substring(1,2))] =
                        getCompany().getBranchAddress(Integer.valueOf(help[i].substring(2)));
        }
        return resulte;
    }

    public String [][] getNextWeekHelp() {
        String [][] result = new String[possibleShifts.length][7];
        String [] tempArry = nextWeekHelp.split(",");
        if(tempArry != null) {
            for (int i = 0; i < tempArry.length; i++) {
                if(tempArry[i] != null && tempArry[i].length()>0)
                    result[Integer.valueOf(tempArry[i].substring(0, 1))][Integer.valueOf(tempArry[i].substring(1, 2))] = tempArry[i].substring(2);
            }
        }
        return result;
    }
    public String getThisWeekHelp() {
        return thisWeekHelp;
    }
    public String getLastWeekHelp() {
        return lastWeekHelp;
    }
/*
    public void setPossibleShifts(boolean[][] possibleShifts){
        this.possibleShifts = possibleShifts;

        String extraEmployees = branch.getExtraEmployees();
        if(index.equals("-1")) return;
        if(index.length() <2) index = "0"+index;
        if(extraEmployees == null) extraEmployees = "";
        for (int shift = 0; shift < possibleShifts.length; shift++) {
            for (int day = 0; day < 7; day++) {
                if(possibleShifts[shift][day])
                    extraEmployees += shift+day+index;
            }
        }
        //update ExtraEmployee for the help of other branches
        branch.setExtraEmployees(extraEmployees);
        Update update = new Update();
        update.run();
    }

 */
    public boolean[][] getPossibleShifts(){
        return possibleShifts;
    }

    public void switchWeek(){
        lastWeekManagerComments = thisWeekManagerComments;
        thisWeekManagerComments = nextWeekManagerComments;
        nextWeekManagerComments = "";

        lastWeekHelp = thisWeekHelp;
        thisWeekHelp = nextWeekHelp;
        nextWeekHelp = "";

        possibleShifts =  new boolean[getCompany().getNumOfShifts()][7];

        Update update = new Update();
        update.run();
    }

    public void deleteEmployee(){
        DatabaseHelper databaseHelper = new DatabaseHelper(App.getContext());
        databaseHelper.deleteEmployee(email);
        getBranch().deleteEmployee(this);
    }

    class Update implements Runnable{
        public Update(){
        }
        @Override
        public void run() {
            DatabaseHelper databaseHelper = new DatabaseHelper(App.getContext());
            boolean result = databaseHelper.updateEmployee(email, Employee.this);
            databaseHelper.close();
        }
    }
}