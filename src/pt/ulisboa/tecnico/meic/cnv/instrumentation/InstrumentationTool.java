package pt.ulisboa.tecnico.meic.cnv.instrumentation;

import BIT.highBIT.*;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;
import java.util.List;
import java.util.Arrays;
import java.io.PrintWriter;

public class InstrumentationTool {

    private static final String metricsInstrClass = "pt/ulisboa/tecnico/meic/cnv/instrumentation/MetricsInstrumentation";
    private static List<String> methodWhitelist = Arrays.asList(new String[]{"getContent", "readObject", "isUnvisitedPassage", "isVisitedPassage", "isWall", "getX", "getY", "getHeight", "getPos", "solveAux", "compare"});

    public static void runInstrumentation(File in_dir, File out_dir) {
        String filelist[] = in_dir.list();

        for (int i = 0; i < filelist.length; i++) {
            String filename = filelist[i];
            if (filename.endsWith(".class")) {
                String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
                String out_filename = out_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
                ClassInfo ci = new ClassInfo(in_filename);

                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {

                    Routine routine = (Routine) e.nextElement();
                    if (methodWhitelist.contains(routine.getMethodName())) {
                        routine.addBefore(metricsInstrClass, "dynMethodCount", new String(routine.getMethodName()));

                        for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                            BasicBlock bb = (BasicBlock) b.nextElement();
                            bb.addBefore(metricsInstrClass, "dynBasicBlockCount", new Integer(bb.size()));
                        }
                    }

                }

                if (ci.getClassName().equals("pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/Main")) {
                    ci.addBefore(metricsInstrClass, "setRequest", "null");
                    ci.addAfter(metricsInstrClass, "printDynamic", ci.getClassName());
                    ci.addAfter(metricsInstrClass, "endOfThreadExecution", "null");
                }
                ci.write(out_filename);
            }
        }
    }

    public static void main(String argv[]) {

        if (argv.length < 2) {
            System.out.println("args are mandatory: [USAGE] java InstrumentationTool IN_DIR OUT_DIR");
            return;
        }

        try {
            File in_dir = new File(argv[0]);
            File out_dir = new File(argv[1]);

            if (in_dir.isDirectory() && out_dir.isDirectory()) {
                runInstrumentation(in_dir, out_dir);
            } else {
                System.out.println("IN_DIR and OUT_DIR must be valid directories");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
