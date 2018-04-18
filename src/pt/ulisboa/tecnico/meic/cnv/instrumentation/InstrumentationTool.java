package pt.ulisboa.tecnico.meic.cnv.instrumentation;

import java.io.*;
import java.util.Enumeration;
import java.util.Vector;

import pt.ulisboa.tecnico.meic.cnv.storage.Messenger;
import org.apache.log4j.*;

import BIT.highBIT.BasicBlock;
import BIT.highBIT.ClassInfo;
import BIT.highBIT.Routine;
import pt.ulisboa.tecnico.meic.cnv.httpserver.WebServer;

public class InstrumentationTool {
    final static Logger logger = Logger.getLogger(InstrumentationTool.class);
    private static final int METRIC = 20000;

    private static final String itPackage = "pt/ulisboa/tecnico/meic/cnv/instrumentation/InstrumentationTool";

    private static PrintStream out = null;
    private static int i_count = 0, b_count = 0, m_count = 1;
    private static Messenger messenger = null;


    public static final String usage = "Usage: java pt.ulisboa.tecnico.meic.cnv.instrumentation.InstrumentationTool inputClass.class"
            + "\nLogging is written to log4j-metrics.log";

    public static void main(String args[]) {
        //logger.addMessage("starting");
        logger.info("init");
        try {
            if (args.length < 1) {
                System.err.println(usage);
                System.exit(-1);
            }
            messenger = new Messenger();
            File file_in = new File(args[0]);
            String path = new String(file_in.getAbsolutePath());
            assert path.endsWith(".class");
            logger.info("starting to instrument class");
            instrument(path);
        } catch (Exception e) {
            System.err.println("Exception ocurred, check log for details.");
            e.printStackTrace();
            //logger.fatal("Exception in main:");
            //logger.fatal(e.getMessage());
            System.exit(-1);
        }
    }

    /**
     *  Instruments the given class file.
     */
    @SuppressWarnings("unchecked")
    public static void instrument(String classFile) {
        try {
            ClassInfo ci = new ClassInfo(classFile); /* read & process the class */

            Vector<Routine> routines = ci.getRoutines();

            
            // loop through all the routines
            // see java.util.Enumeration for more information on Enumeration class
            for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                Routine routine = (Routine) e.nextElement();
                if (routine.getMethodName().equals("generateMaze")) {
                    logger.info("found method");
                    routine.addBefore(itPackage, "mcount", new Integer(1));
                    for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                        BasicBlock bb = (BasicBlock) b.nextElement();
                        bb.addBefore(itPackage, "count", new Integer(bb.size()));;
                    }
                }
            }
            ci.addAfter(itPackage, "printICount", ci.getClassName());
            ci.write(classFile);

        } catch (Exception e) {
            System.err.println("Exception ocurred, check log for details.");
            e.printStackTrace();
            logger.fatal("Exception in instrumentThreadCount:");
            //logger.fatal(e.getMessage());
        }
    }

    public static synchronized void printICount(String foo) {
        System.out.println(i_count + " instructions in " + b_count + " basic blocks were executed in " + m_count + " methods.");
        logger.info(m_count);
        m_count=0;
    }


    public static synchronized void count(int incr) {
        logger.info("count bb: " + b_count);
        i_count += incr;
        b_count++;
    }

    public static synchronized void mcount(int incr) {
        logger.info("m++ count: " + m_count);
        if (m_count-- == 0){
            m_count = METRIC;
//            WebServer.workerUpdateMetrics(m_count);
        }
    }

}
