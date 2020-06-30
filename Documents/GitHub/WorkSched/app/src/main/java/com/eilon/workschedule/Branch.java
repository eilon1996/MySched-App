package com.eilon.workschedule;

import com.eilon.App;

public class Branch {
    // the address and name of the branch
    private String address;
    private String companyName;
    private int numOfEmployees;

    // the number of employees needed in every shift
    private int[][] schedRequired;

    //length = num of shifts contains the name of the shifts as the manager want the employee to see
    private String[] shiftsName;
    //length = 7 contains the name of the days as the manager want the employee to see
    private String[] daysName;

    // ...Week will contain the String of every shift that will be represent to the user
    private String[][] nextWeek;
    private String[][] thisWeek;
    private String[][] lastWeek;

    // [shift][day][working/can work/can't work] = indexes of the employees for temp employees the index will be negative (i.e -3) will be saperate by ","
    private String [][][] orders;
    private String[] employeesName;
    // length = 10 (at the beginning will rise if the user will need more). hold the names of temp employees
    private String[] tempNames;
    private boolean [][][] tempPossibleShifts;

    // [shift][day][numOfEmployees] separate by "*" and start with the place in the shift that the comment will belong
    private String [][][] constantComments;

    // length = num of employees. is the employee assigned shifts or not
    private boolean[] assignedShifts;
    private int[][] shortInEmployees;

    //will the employee will be able to see the sched or not
    private boolean schedReady;


    // the comments that the employee wants the manager to see
    private String employeesComments;

    // will contain the comment that the maneger want every employee to se
    private String managerCommentsNextWeek;
    private String managerCommentsThisWeek;
    private String managerCommentsLastWeek;

    // length num of branch in the companyName
    private boolean [] coOperateBranches;

    // [employees][shift][day] will hold the trio shift+day+num of branch in coOperates, separate by ","
   //private String[][][] helpOtherBranches;

    public Branch(String address, String company, int [][] schedRequired, String [] shiftsName, String [] daysName, String [][][] constantComments){
        this.address = address.toLowerCase();
        this.companyName = company.toLowerCase();
        this.schedRequired = schedRequired;
        this.shiftsName = shiftsName;
        this.daysName = daysName;
        this.constantComments = constantComments;
        this.employeesComments = "";
        this.managerCommentsNextWeek = "";
        this.shortInEmployees = new int[schedRequired.length][7];

        DatabaseHelper databaseHelper = new DatabaseHelper(App.getContext());
        databaseHelper.addBranch(this);

        Company c = getComapny();
        c.addBranch(address);
    }

    public void setAddress(String address) {
        getComapny().updateBranch(this.address,address);
        this.address = address;
        Employee [] employees = getAllEmployees();
        for(int i = 0; i < numOfEmployees; i++)
            employees[i].setBranch(address);
        Update update = new Update();
        update.run();
    }
    public String getBranchName(){return address;}

    public void setCompanyName(String companyName){
        this.companyName = companyName;
    }
    public String getCompanyName(){return companyName;}
    public Company getComapny(){
        DatabaseHelper databaseHelper = new DatabaseHelper(App.getContext());
        Company result = databaseHelper.getCompany(companyName);
        databaseHelper.close();
        return result;
    }

    public void addEmployee(String name){
        String [] tempName = new String[numOfEmployees+1];
        boolean [] tempAssign = new boolean[numOfEmployees+1];
        for(int i = 0; i < numOfEmployees; i++) {
            tempName[i] = employeesName[i];
            tempAssign[i] = assignedShifts[i];
        }
        tempName[numOfEmployees] = name;
        employeesName = tempName;
        numOfEmployees++;
        Update update = new Update();
        update.run();
    }
    public void updateEmployeeName(String oldName, String newName) {
        for (int i = 0; i < numOfEmployees; i++)
            if (employeesName[i].equals(oldName))
                employeesName[i] = newName;
        Update update = new Update();
        update.run();
    }


    // deleting employee from the database
    // the action wont affect this and last week schedule
    public void deleteEmployee(Employee employee){
        DeleteEmployee deleteEmployee = new DeleteEmployee(employee);
    }

    class DeleteEmployee implements Runnable{

        private Employee employee;
        public DeleteEmployee (Employee employee){
            this.employee = employee;
        }
        @Override
        public void run() {

            String name = employee.getFullNameOneLine();
            String [] temp = new String[numOfEmployees-1];
            int employeeIndex = getEmployeeIndex(name);
            for(int i = 0; i < employeeIndex; i++)
                temp[i] = employeesName[i];
            for(int i = employeeIndex+1; i < numOfEmployees; i++)
                temp[i] = employeesName[i];
            employeesName = temp;
            numOfEmployees--;
            DatabaseHelper  databaseHelper = new DatabaseHelper(App.getContext());
            databaseHelper.deleteEmployee(employee.getEmail());
            orders = null;

            //deleting the employee from the orders of all other branches
            String [][] help = employee.getNextWeekHelp();
            Company company = getComapny();
            for (int shift = 0; shift < orders.length; shift++) {
                for (int day = 0; day < 7; day++) {
                    if(help[shift][day] != null) {
                        int branchIndex = Integer.valueOf(help[shift][day]);
                        if(!deleteFromOrders(shift,day,0,employeeIndex,branchIndex))
                            deleteFromOrders(shift,day,1,employeeIndex,branchIndex);
                    }
                }
            }
            Update update = new Update();
            update.run();
        /*
        int employeeIndex = getEmployeeIndex(employee.getFullNameOneLine());
        int lastEmployeeIndex = numOfEmployees-1;
        DatabaseHelper databaseHelper = new DatabaseHelper(App.getContext());
        Company company = getComapny();
        if(orders!= null) {
            if(employeeIndex==lastEmployeeIndex) {
                for (int shift = 0; shift < orders.length; shift++) {
                    for (int day = 0; day < 7; day++) {
                        for (int i = 0; i < 3; i++) {
                            deleteFromOrders(shift,day,i,employeeIndex,this);
                        }
                    }
                }
                String [] help = employee.getNextWeekHelp().split(",");
                for(int i = 0; help!= null && i<help.length;i++){
                    int shift = Integer.valueOf(help[i].substring(0,1));
                    int day = Integer.valueOf(help[i].substring(1,2));
                    int branchIndex = Integer.valueOf(help[i].substring(2));
                    if(!deleteFromOrders(shift,day,0,employeeIndex,company.getBranch(branchIndex)))
                        deleteFromOrders(shift,day,1,employeeIndex,company.getBranch(branchIndex));
                }
                databaseHelper.deleteEmployee(employee.getEmail());
            } else {
                for (int shift = 0; shift < orders.length; shift++) {
                    for (int day = 0; day < 7; day++) {
                        for (int i = 0; i < 3; i++) {
                            deleteNSwitchFromOrders(shift,day,1,employeeIndex,lastEmployeeIndex,this);
                        }
                    }
                }
                databaseHelper.updateEmployee(employee.getEmail(),getEmployee(lastEmployeeIndex));
                databaseHelper.deleteEmployee(employee.getEmail());
            }
            updateNextWeek();
        }
        */
        }

        private boolean deleteFromOrders(int shift, int day, int orderIndex, int employeeIndex, int branchIndex){
            Branch branch = getComapny().getBranch(branchIndex);
            String employeeName = employeesName[employeeIndex];
            String[][][]orders = branch.getOrders();
            boolean delete = false;
            String[] tempOrder = orders[shift][day][orderIndex].split(",");
            orders[shift][day][orderIndex] = "";
            for (int j = 0; j < tempOrder.length; j++) {
                if (!tempOrder[j].equals(branchIndex + "*"+employeeName))
                    orders[shift][day][orderIndex] += "," + tempOrder[j];
                else delete = true;
            }
            if (orders[shift][day][orderIndex].length() > 0)
                orders[shift][day][orderIndex] = orders[shift][day][orderIndex].substring(1);
            if(delete && branch != Branch.this) branch.setOrders(orders);
            return delete;
        }

    }

    public void deleteEmployee(String email){
        DatabaseHelper  databaseHelper = new DatabaseHelper(App.getContext());
        deleteEmployee(databaseHelper.getEmployee(email));
    }

    public void setNumOfEmployees(int numOfEmployees){
        this.numOfEmployees = numOfEmployees;
        Update update = new Update();
        update.run();
    }

    public void setNumOfEmployees(int addOrDelete, String name){
        this.numOfEmployees += numOfEmployees;
        if(addOrDelete == -1) deleteEmployee(name);
        Update update = new Update();
        update.run();
    }
    public int getNumOfEmployees(){return numOfEmployees;}

    // return the index of the branches that get help in from[employee] at [shift][day]
    public String [][][] getHelpForOtherBranch() {

        String [][][] result = new String[numOfEmployees][schedRequired.length][7];
        Employee [] allEmployees = getAllEmployees();
        for(int i = 0; i<numOfEmployees; i++)
            result[i] = allEmployees[i].getNextWeekHelp();
        return result;
    }

    public void setSchedRequired(int[][] schedrequired) {
        this.schedRequired = schedrequired;
        Update update = new Update();
        update.run();
    }
    public int[][] getSchedRequired() {
        return schedRequired;
    }

    public void setShiftsName(String[] shiftsName) {
        this.shiftsName = shiftsName;
        Update update = new Update();
        update.run();
    }
    public String[] getShiftsName() {return shiftsName;}

    public void setDaysName(String[] daysName) {
        this.daysName = daysName;
        Update update = new Update();
        update.run();
    }

    public String[] getDaysName() {
        return daysName;
    }

    public void setTempEmployeesName(String[] names){
        tempNames = names;
        Update update = new Update();
        update.run();
    }
    public void setTempEmployeesName(String name, int index){
        tempNames[index] = name;
        Update update = new Update();
        update.run();
    }

    public void setTempEmployeesPossibleShifts(boolean[][][] possibleShifts){
        tempPossibleShifts = possibleShifts;
        Update update = new Update();
        update.run();
    }
    public void setTempEmployeesPossibleShifts(boolean[][]possibleShifts, int index){
        tempPossibleShifts[index] = possibleShifts;
        Update update = new Update();
        update.run();
    }
    public void deleteTempEmployee(int index){
        // למחוק משמרות ושם
    }

    public boolean[][][] getTempPossibleShifts() {
        return tempPossibleShifts;
    }

    public String[] getTempNames() {
        return tempNames;
    }
    public boolean[][] getTempPossibleShifts(int index) {
        return tempPossibleShifts[index];
    }

    public String getTempNames(int index) {
        return tempNames[index];
    }


    public void setAssignedShifts(int indexOfEmployee) {
        this.assignedShifts[indexOfEmployee] = true;
        Update update = new Update();
        update.run();
    }

    public void setAssignedShiftsNumbers(int num){
        assignedShifts = new boolean[num];
        Update update = new Update();
        update.run();
    }
    public boolean[] getAssignedShifts() {
        if(assignedShifts == null) {
            assignedShifts = new boolean[numOfEmployees];
            Update update = new Update();
            update.run();
        }
        return assignedShifts;
    }

    public void setConstantComments(String[][][] constantComments) {
        this.constantComments = constantComments;
        Update update = new Update();
        update.run();
    }
    public String[][][] getConstantComments() {
        return constantComments;
    }

    public void setNextWeek(String[][] nextWeek){
        this.nextWeek = nextWeek;
        Update update= new Update();
        update.run();
    }
    public void setNextWeek(String nextWeek, int shift, int day){
        this.nextWeek[shift][day] = nextWeek;
        Update update= new Update();
        update.run();
    }

    public void setNextWeek() {
        for(int shift = 0; shift<orders.length; shift++){
            for(int day = 0; day<7; day++){
                String [] employees = orders[shift][day][0].split(",");
                String names = "";
                if(employees != null) {
                    for (int i = 0; i < employees.length; i++) {
                        try{
                            int index = Integer.valueOf(employees[i]);
                            names+= employeesName[index];
                        } catch (Exception e){
                            names += employees[i];
                        }
                    }
                    nextWeek[shift][day] = names;
                }
            }
        }
        Update update = new Update();
        update.run();
    }
    public String[][] getNextWeek() {
        return nextWeek;
    }
    public String[][] getThisWeek() {return thisWeek;}
    public String[][] getLastWeek() {return lastWeek;}

    public void setOrders(String [][][] orders){
        this.orders = orders;
        Update update = new Update();
        update.run();
    }

    public void updateOrders(int shift, int day, String [] order){
        orders[shift][day] = order;
        Update update = new Update();
        update.run();
    }
    public String[][][] getOrders() {  return orders;  }

    //update the shifts that the employee assigned
    /*
    public void updatePossibleShifts(boolean [][] possibleShifts, int index){
        if (assignedShifts[index]) {
            for (int shift = 0; shift < orders.length; shift++) {
                for (int day = 0; day < 7; day++) {
                    boolean found = false;
                    int j = 2;
                    String[] search = orders[shift][day][j].split(",");
                    if (search != null) {
                        for (int k = 0; k < search.length && !found; k++) {
                            if (search[k].equals(index)) {
                                remove(shift, day, j, k, search);
                                if (orders[shift][day][1].length() == 0)
                                    orders[shift][day][1] += index;
                                else orders[shift][day][1] += "," + index;
                                found = true;
                            }
                        }
                    }
                    for (j = 0; j < 2 && !found; j++) {
                        search = orders[shift][day][j].split(",");
                        if (search != null) {
                            for (int k = 0; k < search.length && !found; k++) {
                                if (!search[k].equals(index)) {
                                    remove(shift, day, j, k, search);
                                    if (orders[shift][day][2].length() == 0)
                                        orders[shift][day][2] += index;
                                    else orders[shift][day][2] += "," + index;
                                    found = true;
                                }
                            }
                        }
                    }
                }
            }
        }

    }
    private void remove(int shift, int day, int j, int index, String [] employees){
        if(employees.length == 1){
            orders[shift][day][j]= "";
        }
        else {
            for (int i = 0; i < index; i++)
                orders[shift][day][j] = ","+employees[i];
            for (int i = index + 1; i < employees.length; i++)
                orders[shift][day][j] = ","+employees[i];
            orders[shift][day][j] = orders[shift][day][j].substring(1);
        }
    }
*/
    public void setShortInEmployees(int [][] shortInEmployees) {
        this.shortInEmployees = shortInEmployees;
        Update update = new Update();
        update.run();
    }
    public void setShortEmployees (int shift, int day, int shorts) {
        this.shortInEmployees[shift][day] = shorts;
        Update update = new Update();
        update.run();
    }
    public void ShortEmployeesPlusOne (int shift, int day) {
        this.shortInEmployees[shift][day]++;
        Update update = new Update();
        update.run();
    }
    public void ShortEmployeesMinusOne (int shift, int day) {
        this.shortInEmployees[shift][day]--;
        Update update = new Update();
        update.run();
    }

    public int [][] getShortInEmployees() {
        return shortInEmployees;
    }
    public int getShortEmployees(int shift, int day) {
        return shortInEmployees[shift][day];
    }

    public void setSchedReady(boolean schedReady) {
        this.schedReady = schedReady;
        Update update = new Update();
        update.run();
    }
    public boolean getSchedReady() { return schedReady;  }


    public void addCoOperateBranches(int index) {
        coOperateBranches[index] = true;
        Update update = new Update();
        update.run();
    }
    public void addCoOperateBranches(String coOperateBranch) {
        addCoOperateBranches(getComapny().getBranchIndex(coOperateBranch));
    }
    public void deleteCoOperateBranches(int index) {
        coOperateBranches[index] = false;
        Update update = new Update();
        update.run();
    }
    public void deleteCoOperateBranches(String coOperateBranch) {
        deleteCoOperateBranches(getComapny().getBranchIndex(coOperateBranch));
    }

    public Branch[] getCoOperateBranches() {
        if (coOperateBranches == null || coOperateBranches.equals("")) return null;
        int count = 0;
        for(int i = 0; i<coOperateBranches.length; i++)
            if(coOperateBranches[i]) count++;

        Branch [] result = new Branch[count];
        Branch [] branches = getComapny().getAllBranches();
        for(int i = 0; i <coOperateBranches.length; i++){
            count--;
            result[count] = branches[i];
        }
        return result;
    }

    public Employee [] getAllEmployees(){
        DatabaseHelper db = new DatabaseHelper(App.getContext());
        return db.getAllEmployeesForBranch(companyName +address);
    }

    public String [] getAllEmployeesName(){
        return employeesName;
        /* // useing the database
        DatabaseHelper db = new DatabaseHelper(App.getContext());
        Employee [] allEmployees = db.getAllEmployeesForBranch(companyName +address);
        String [] resulte = new String[allEmployees.length];
        for(int i = 0; i < resulte.length; i++)
            resulte[i] = allEmployees[i].getFullNameTwoLines();
        return resulte;

         */
    }
    public String getEmployeeName(int index){
        try{
            String name = employeesName[index];
            return name;
        } catch (Exception e){
            return null;
        }
    }

    public int getEmployeeIndex(String name){
        String [] names = name.split("\n");
        for(int i = 0; i < numOfEmployees; i++){
            if(employeesName[i].split("\n")[0].equals(names[0])
                    && employeesName[i].split("\n")[1].equals(names[1])) return i;
        }
        return -1;
    }


    public Employee getEmployee(int index){
        return getAllEmployees()[index];
    }
    public String [] getAllEmployeesNameEmails(){
        DatabaseHelper db = new DatabaseHelper(App.getContext());
        return db.getAllEmployeesEmailForBranch(companyName +address);
    }

    public boolean[][][] getAllPossibleShifts(){
        Employee [] allEmployees = getAllEmployees();
        boolean [][][] allPossibleShifts = new boolean[numOfEmployees][3][7];
        for(int i = 0; i< numOfEmployees; i++){
            allPossibleShifts[i] = allEmployees[i].getPossibleShifts();
        }
        return allPossibleShifts;
    }

    public void addEmployeesComments(String employeeName, String comment) {
        this.employeesComments += "\n"+employeeName+": " +comment;
        Update update = new Update();
        update.run();
    }
    public String getEmployeesComments() {
        if(employeesComments.length() == 0) return "אין הערות של העובדים";
        return "הערות עובדים:" + employeesComments;
    }

    public void setManagerComments(String comments) {
        this.managerCommentsNextWeek = comments;
        Update update = new Update();
        update.run();
    }
    public String getManagerCommentsNextWeek() {
        if(managerCommentsNextWeek == null)managerCommentsNextWeek = "";
        return managerCommentsNextWeek;
    }
    public String getManagerCommentsThisWeek() {
        if(managerCommentsThisWeek == null) managerCommentsThisWeek = "";
        return managerCommentsThisWeek;
    }
    public String getManagerCommentsLastWeek() {
        if(managerCommentsLastWeek == null) managerCommentsLastWeek = "";
        return managerCommentsLastWeek;
    }

    public void switchWeek(){
        employeesComments = "";
        lastWeek = thisWeek;
        thisWeek = nextWeek;
        nextWeek = null;

        managerCommentsLastWeek = managerCommentsThisWeek;
        managerCommentsThisWeek = managerCommentsNextWeek;
        managerCommentsNextWeek = "";

        schedReady = false;
        orders = null;
        Update update = new Update();
        update.run();
    }




    class Update implements Runnable{
        public Update(){
        }
        @Override
        public void run() {
/*
            SharedPreferences sp =  PreferenceManager.getDefaultSharedPreferences(App.getContext());
            SharedPreferences.Editor editor = sp.edit();
            Gson gson = new Gson();
            editor.putString("branch",gson.toJson(this));
*/
            DatabaseHelper databaseHelper = new DatabaseHelper(App.getContext());
            boolean result = databaseHelper.updateBranch(Branch.this);
        }
    }
}
