package pt.ulisboa.tecnico.meic.cnv.instrumentation;
//
// InstrumentationTool.java
//
// This program measures and instruments to obtain different statistics
// about Java programs.
//
// Copyright (c) 1998 by Han B. Lee (hanlee@cs.colorado.edu).
// ALL RIGHTS RESERVED.
//
// Permission to use, copy, modify, and distribute this software and its
// documentation for non-commercial purposes is hereby granted provided 
// that this copyright notice appears in all copies.
// 
// This software is provided "as is".  The licensor makes no warrenties, either
// expressed or implied, about its correctness or performance.  The licensor
// shall not be liable for any damages suffered as a result of using
// and modifying this software.

import BIT.highBIT.*;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;
import java.io.PrintWriter;


public class InstrumentationTool {

    public static void printUsage() {
        System.out.println("Syntax: java InstrumentationTool -stat_type in_path [out_path]");
        System.out.println("        where stat_type can be:");
        System.out.println("        static:     static properties");
        System.out.println("        dynamic:    dynamic properties");
        System.out.println("        alloc:      memory allocation instructions");
        System.out.println("        load_store: loads and stores (both field and regular)");
        System.out.println("        branch:     gathers branch outcome statistics");
        System.out.println();
        System.out.println("        in_path:  directory from which the class files are read");
        System.out.println("        out_path: directory to which the class files are written");
        System.out.println("        Both in_path and out_path are required unless stat_type is static");
        System.out.println("        in which case only in_path is required");
        System.exit(-1);
    }


    public static void doDynamic(File in_dir, File out_dir) {
        String filelist[] = in_dir.list();

        for (int i = 0; i < filelist.length; i++) {
            String filename = filelist[i];
            if (filename.endsWith(".class")) {
                String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
                String out_filename = out_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
                ClassInfo ci = new ClassInfo(in_filename);

                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {

                    Routine routine = (Routine) e.nextElement();
                    routine.addBefore("pt/ulisboa/tecnico/meic/cnv/instrumentation/MetricsInstrumentation", "dynMethodCount", new String(routine.getMethodName()));

                    for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                        BasicBlock bb = (BasicBlock) b.nextElement();
                        bb.addBefore("pt/ulisboa/tecnico/meic/cnv/instrumentation/MetricsInstrumentation", "dynInstrCount", new Integer(bb.size()));
                    }

                }

                System.out.println(ci.getClassName());
                if (ci.getClassName().equals("pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/Main")) {
                    ci.addBefore("pt/ulisboa/tecnico/meic/cnv/instrumentation/MetricsInstrumentation", "setRequest", "null");
                    ci.addAfter("pt/ulisboa/tecnico/meic/cnv/instrumentation/MetricsInstrumentation", "printDynamic", ci.getClassName());
                    ci.addAfter("pt/ulisboa/tecnico/meic/cnv/instrumentation/MetricsInstrumentation", "endOfThreadExecution", "null");
                }
                ci.write(out_filename);
            }
        }
    }


    public static void main(String argv[]) {
        if (argv.length < 2 || !argv[0].startsWith("-")) {
            printUsage();
        }

        if (argv[0].equals("-dynamic")) {
            if (argv.length != 3) {
                printUsage();
            }

            try {
                File in_dir = new File(argv[1]);
                File out_dir = new File(argv[2]);

                if (in_dir.isDirectory() && out_dir.isDirectory()) {
                    doDynamic(in_dir, out_dir);
                } else {
                    printUsage();
                }
            } catch (NullPointerException e) {
                printUsage();
            }
        }
    }

}
