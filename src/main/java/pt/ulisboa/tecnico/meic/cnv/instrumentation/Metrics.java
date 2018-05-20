package pt.ulisboa.tecnico.meic.cnv.instrumentation;

import java.io.PrintWriter;
import java.io.File;
import java.io.FileOutputStream;

import java.util.LinkedHashMap;

public class Metrics {

    private int threadID;

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
        System.out.println("Number of basic blocks: " + dyn_bb_count);
        System.out.println("Number of instructions: " + dyn_instr_count);

    }

}
