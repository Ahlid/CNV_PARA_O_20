package pt.ulisboa.tecnico.meic.cnv.instrumentation;

import java.io.PrintWriter;
import java.util.HashMap;

public class Metrics {

    private int threadID;
    private HashMap<String, Integer> methodCounts = new HashMap<>();

    private int dyn_method_count = 0;
    private int dyn_bb_count = 0;
    private long dyn_instr_count = 0;

    private int newcount = 0;
    private int newarraycount = 0;
    private int anewarraycount = 0;
    private int multianewarraycount = 0;

    private int loadcount = 0;
    private int storecount = 0;
    private int fieldloadcount = 0;
    private int fieldstorecount = 0;

    private StatisticsBranch[] branch_info;
    private int branch_number;
    private int branch_pc;
    private String branch_class_name;
    private String branch_method_name;

    private String[] requestParams;

    public String[] getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String[] requestParams) {
        this.requestParams = requestParams;
    }

    public int getThreadID() {
        return threadID;
    }

    public void setThreadID(int threadID) {
        this.threadID = threadID;
    }

    public HashMap<String, Integer> getMethodCounts() {
        return methodCounts;
    }

    public void setMethodCounts(HashMap<String, Integer> methodCounts) {
        this.methodCounts = methodCounts;
    }

    public int getDyn_method_count() {
        return dyn_method_count;
    }

    public void setDyn_method_count(int dyn_method_count) {
        this.dyn_method_count = dyn_method_count;
    }

    public int getDyn_bb_count() {
        return dyn_bb_count;
    }

    public void setDyn_bb_count(int dyn_bb_count) {
        this.dyn_bb_count = dyn_bb_count;
    }

    public long getDyn_instr_count() {
        return dyn_instr_count;
    }

    public void setDyn_instr_count(long dyn_instr_count) {
        this.dyn_instr_count = dyn_instr_count;
    }

    public int getNewcount() {
        return newcount;
    }

    public void setNewcount(int newcount) {
        this.newcount = newcount;
    }

    public int getNewarraycount() {
        return newarraycount;
    }

    public void setNewarraycount(int newarraycount) {
        this.newarraycount = newarraycount;
    }

    public int getAnewarraycount() {
        return anewarraycount;
    }

    public void setAnewarraycount(int anewarraycount) {
        this.anewarraycount = anewarraycount;
    }

    public int getMultianewarraycount() {
        return multianewarraycount;
    }

    public void setMultianewarraycount(int multianewarraycount) {
        this.multianewarraycount = multianewarraycount;
    }

    public int getLoadcount() {
        return loadcount;
    }

    public void setLoadcount(int loadcount) {
        this.loadcount = loadcount;
    }

    public int getStorecount() {
        return storecount;
    }

    public void setStorecount(int storecount) {
        this.storecount = storecount;
    }

    public int getFieldloadcount() {
        return fieldloadcount;
    }

    public void setFieldloadcount(int fieldloadcount) {
        this.fieldloadcount = fieldloadcount;
    }

    public int getFieldstorecount() {
        return fieldstorecount;
    }

    public void setFieldstorecount(int fieldstorecount) {
        this.fieldstorecount = fieldstorecount;
    }

    public StatisticsBranch[] getBranch_info() {
        return branch_info;
    }

    public void setBranch_info(StatisticsBranch[] branch_info) {
        this.branch_info = branch_info;
    }

    public int getBranch_number() {
        return branch_number;
    }

    public void setBranch_number(int branch_number) {
        this.branch_number = branch_number;
    }

    public int getBranch_pc() {
        return branch_pc;
    }

    public void setBranch_pc(int branch_pc) {
        this.branch_pc = branch_pc;
    }

    public String getBranch_class_name() {
        return branch_class_name;
    }

    public void setBranch_class_name(String branch_class_name) {
        this.branch_class_name = branch_class_name;
    }

    public String getBranch_method_name() {
        return branch_method_name;
    }

    public void setBranch_method_name(String branch_method_name) {
        this.branch_method_name = branch_method_name;
    }

    public void print() {

        System.out.println("Results");

        System.out.println();
        System.out.println("Request params:");
        //get the thread id
        String filename = Thread.currentThread().getId() + "-";
        for (String s : this.requestParams) {
            filename = (filename + s);
            System.out.println(s);
        }
        filename = (filename + ".txt");
        System.out.println();
        System.out.println("Methods:");

        for (String entry : methodCounts.keySet()) {
            System.out.println(entry + ": " + methodCounts.get(entry));
        }


        System.out.println();

        System.out.println("Dynamic information summary:");
        System.out.println("Number of methods:      " + dyn_method_count);
        System.out.println("Number of basic blocks: " + dyn_bb_count);
        System.out.println("Number of instructions: " + dyn_instr_count);

        if (dyn_method_count == 0) {
            return;
        }

        float instr_per_bb = (float) dyn_instr_count / (float) dyn_bb_count;
        float instr_per_method = (float) dyn_instr_count / (float) dyn_method_count;
        float bb_per_method = (float) dyn_bb_count / (float) dyn_method_count;

        System.out.println("Average number of instructions per basic block: " + instr_per_bb);
        System.out.println("Average number of instructions per method:      " + instr_per_method);
        System.out.println("Average number of basic blocks per method:      " + bb_per_method);


        try (PrintWriter out = new PrintWriter(filename)) {

            for (String entry : methodCounts.keySet()) {
                out.println(entry + ": " + methodCounts.get(entry));
            }


            out.println("Dynamic information summary:");
            out.println("Number of methods:      " + dyn_method_count);
            out.println("Number of basic blocks: " + dyn_bb_count);
            out.println("Number of instructions: " + dyn_instr_count);

            if (dyn_method_count == 0) {
                return;
            }

            instr_per_bb = (float) dyn_instr_count / (float) dyn_bb_count;
            instr_per_method = (float) dyn_instr_count / (float) dyn_method_count;
            bb_per_method = (float) dyn_bb_count / (float) dyn_method_count;

            out.println("Average number of instructions per basic block: " + instr_per_bb);
            out.println("Average number of instructions per method:      " + instr_per_method);
            out.println("Average number of basic blocks per method:      " + bb_per_method);

        } catch (Exception e) {
            System.out.println("oops");
        }


    }

    public void addMethodCount(String name) {
        if (methodCounts.containsKey(name)) {
            methodCounts.put(name, methodCounts.get(name) + 1);
        } else {
            methodCounts.put(name, 1);
        }
    }
}