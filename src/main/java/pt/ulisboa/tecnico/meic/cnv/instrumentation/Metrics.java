package pt.ulisboa.tecnico.meic.cnv.instrumentation;

import java.io.PrintWriter;
import java.io.File;
import java.io.FileOutputStream;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class Metrics {

    private int threadID;
    private HashMap<String, Integer> methodCounts = new HashMap<>();
    private static final String CSV_SEPARATOR = ",";
    private static final int NUM_METHODS = 5;

    private long dyn_method_count = 0;
    private long dyn_bb_count = 0;
    private long dyn_instr_count = 0;


    private LinkedHashMap<String, String> requestParams;

    public LinkedHashMap<String, String> getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(LinkedHashMap<String, String> requestParams) {
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

    public long getDyn_method_count() {
        return dyn_method_count;
    }

    public void setDyn_method_count(long dyn_method_count) {
        this.dyn_method_count = dyn_method_count;
    }

    public long getDyn_bb_count() {
        return dyn_bb_count;
    }

    public void setDyn_bb_count(long dyn_bb_count) {
        this.dyn_bb_count = dyn_bb_count;
    }

    public long getDyn_instr_count() {
        return dyn_instr_count;
    }

    public void setDyn_instr_count(long dyn_instr_count) {
        this.dyn_instr_count = dyn_instr_count;
    }


    public void print() {


        System.out.println("Thread ID:" + threadID);
        System.out.println("Request:" + getRequestParams());
        System.out.println();
        System.out.println("Dynamic information summary:");
        System.out.println("Number of methods:      " + dyn_method_count);
        System.out.println("Number of basic blocks: " + dyn_bb_count);
        System.out.println("Number of instructions: " + dyn_instr_count);

        if (dyn_method_count == 0) {
            return;
        }

    }

    // Comparator that compares String keys
    private class ValueComparator implements Comparator<String> {

        HashMap<String, Integer> map = new HashMap<String, Integer>();

        public ValueComparator(HashMap<String, Integer> map) {
            this.map.putAll(map);
        }

        @Override
        public int compare(String s1, String s2) {
            return map.get(s2).compareTo(map.get(s1));  // Compare in descending order
        }
    }

    private TreeMap<String, Integer> getHighestMethodCounts(int numMethods) {

        // Sort TreeMap based on method counts
        Comparator<String> comparator = new ValueComparator(methodCounts);
        TreeMap<String, Integer> sortedMethodCounts = new TreeMap<String, Integer>(comparator);
        sortedMethodCounts.putAll(methodCounts);

        // Iterate sorted map and keep only numMethods entries
        TreeMap<String, Integer> highestMethodCounts = new TreeMap<String, Integer>(comparator);
        Iterator<Entry<String, Integer>> it = sortedMethodCounts.entrySet().iterator();

        for (int i = 0; i < numMethods; i++) {
            Entry<String, Integer> entry = it.next();
            highestMethodCounts.put(entry.getKey(), entry.getValue());
        }

        return highestMethodCounts;
    }

    private String getCSVLine(List<String> values) {

        StringBuilder csvBuilder = new StringBuilder();
        Iterator<String> iter = values.iterator();

        csvBuilder.append(iter.next());
        while (iter.hasNext()) {
            csvBuilder.append(CSV_SEPARATOR);
            csvBuilder.append(iter.next());
        }

        return csvBuilder.toString();
    }

    public void addMethodCount(String name) {
        if (methodCounts.containsKey(name)) {
            methodCounts.put(name, methodCounts.get(name) + 1);
        } else {
            methodCounts.put(name, 1);
        }
    }
}
