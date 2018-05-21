package pt.ulisboa.tecnico.meic.cnv.loadbalancer;

import pt.ulisboa.tecnico.meic.cnv.storage.Messenger;

import java.util.*;

public class CostFunction {


    private final static long ASTAR_INITIAL_COST = 83241L;
    private final static long DFS_INITIAL_COST = 90128L;
    private final static long BFS_INITIAL_COST = 90139L;


    private final static long ASTAR_DISTANCE_COST = 88028L;
    private final static long DFS_DISTANCE_COST = 35929L;
    private final static long BFS_DISTANCE_COST = 6898L;


    public static HashMap<Integer, Double> astarV;
    public static HashMap<Integer, Double> bfsV;
    public static HashMap<Integer, Double> dfsV;

    static {
        astarV = new HashMap();
        astarV.put(10, 1.0);
        astarV.put(20, 1.0);
        astarV.put(30, 1.0);
        astarV.put(40, 1.0);
        astarV.put(50, 1.0);
        astarV.put(60, 1.0);
        astarV.put(70, 1.0);
        astarV.put(80, 1.0);
        astarV.put(90, 1.0);

        bfsV = new HashMap();
        bfsV.put(10, 1.745786269);
        bfsV.put(20, 1.331722142);
        bfsV.put(30, 1.197415342);
        bfsV.put(40, 1.134977036);
        bfsV.put(50, 1.101060126);
        bfsV.put(60, 1.076816376);
        bfsV.put(70, 1.057542583);
        bfsV.put(80, 1.049745343);
        bfsV.put(90, 1.040675452);

        dfsV = new HashMap();
        dfsV.put(10, 1.94598237);
        dfsV.put(20, 1.461898777);
        dfsV.put(30, 1.297970488);
        dfsV.put(40, 1.218769479);
        dfsV.put(50, 1.174761305);
        dfsV.put(60, 1.140720308);
        dfsV.put(70, 1.110712347);
        dfsV.put(80, 1.100321691);
        dfsV.put(90, 1.085569101);

    }


    private static ArrayList<ResultValue> previousResultsA = new ArrayList<>();

    public static long calculateAstarCost(ArrayList<ResultValue> previousResultsA, int x0, int x1, int y0, int y1, int velocity) {

        long worstCase = (((Math.abs(x1 - x0) * Math.abs(y1 - y0) * ASTAR_DISTANCE_COST + ASTAR_INITIAL_COST) +
                ((Math.abs(x1 - x0) + 1) * (Math.abs(y1 - y0) + 1) * ASTAR_DISTANCE_COST + ASTAR_INITIAL_COST)) / 2);

        worstCase = calculateVCost(velocity, worstCase, astarV);

        if (previousResultsA == null || previousResultsA.size() == 0) {
            return worstCase;
        }

        int currentCombinedDistance = 1000 * 1000;
        ResultValue selectedSample = null;

        for (ResultValue resultValue : previousResultsA) {

            int distance = Math.abs(x0 - resultValue.getX0()) + Math.abs(x1 - resultValue.getX1()) + Math.abs(y0 - resultValue.getY0()) + Math.abs(y1 - resultValue.getY1());

            if (distance < currentCombinedDistance) {
                currentCombinedDistance = distance;
                selectedSample = resultValue;
            }
        }


        System.out.println(selectedSample);

        int distanceExpected = selectedSample.getX0() - x0 + x1 - selectedSample.getX1() + selectedSample.getY0() - y0 + y1 - selectedSample.getY1();


        int multiplier = 1;
        if (distanceExpected < 0) {
            multiplier = -1;
        }

        //  System.out.println(currentCombinedDistance);
        // System.out.println(distanceExpected);

        long expectedCase = (selectedSample.getBasicBlocks() + distanceExpected * distanceExpected * multiplier *
                ASTAR_DISTANCE_COST);

        if (expectedCase < 0)
            return worstCase;

        if (distanceExpected == 0)
            return expectedCase;

        if (Math.abs(distanceExpected) > 10) {
            return worstCase;
        }

        //  System.out.println(expectedCase);
        // System.out.println(worstCase);
        //  System.out.println(worstCase * 1.0 / expectedCase);


        return (expectedCase + worstCase) / 2;


    }

    public static long calculateBfsCost(ArrayList<ResultValue> previousResultsA, int x0, int x1, int y0, int y1, int velocity) {

        long worstCase = (((Math.abs(x1 - x0) * Math.abs(y1 - y0) * BFS_DISTANCE_COST + BFS_INITIAL_COST) +
                ((Math.abs(x1 - x0) + 1) * (Math.abs(y1 - y0) + 1) * BFS_DISTANCE_COST + BFS_INITIAL_COST)) / 2);

        worstCase = calculateVCost(velocity, worstCase, bfsV);

        if (previousResultsA == null || previousResultsA.size() == 0) {
            return worstCase;
        }

        int currentCombinedDistance = 1000 * 1000;
        ResultValue selectedSample = null;

        for (ResultValue resultValue : previousResultsA) {

            int distance = Math.abs(x0 - resultValue.getX0()) + Math.abs(x1 - resultValue.getX1()) + Math.abs(y0 - resultValue.getY0()) + Math.abs(y1 - resultValue.getY1());

            if (distance < currentCombinedDistance) {
                currentCombinedDistance = distance;
                selectedSample = resultValue;
            }
        }


        System.out.println(selectedSample);

        long distanceExpected = selectedSample.getX0() - x0 + x1 - selectedSample.getX1() + selectedSample.getY0() - y0 + y1 - selectedSample.getY1();


        int multiplier = 1;
        if (distanceExpected < 0) {
            multiplier = -1;
        }

        //  System.out.println(currentCombinedDistance);
        // System.out.println(distanceExpected);

        long expectedCase = (selectedSample.getBasicBlocks() + distanceExpected * distanceExpected * multiplier *
                BFS_DISTANCE_COST);


        if (expectedCase < 0)
            return worstCase;

        if (distanceExpected == 0)
            return expectedCase;

        if (Math.abs(distanceExpected) > 10) {
            return worstCase;
        }

        //  System.out.println(expectedCase);
        // System.out.println(worstCase);
        //  System.out.println(worstCase * 1.0 / expectedCase);


        return (expectedCase + worstCase) / 2;


    }

    public static long calculateDfsCost(ArrayList<ResultValue> previousResultsA, int x0, int x1, int y0, int y1, int velocity) {

        System.out.println(previousResultsA);

        long worstCase = (((Math.abs(x1 - x0) * Math.abs(y1 - y0) * DFS_DISTANCE_COST + DFS_INITIAL_COST) +
                ((Math.abs(x1 - x0) + 1) * (Math.abs(y1 - y0) + 1) * DFS_DISTANCE_COST + DFS_INITIAL_COST)) / 2);

        worstCase = calculateVCost(velocity, worstCase, dfsV);

        System.out.println("WORST CASE");
        System.out.println(worstCase);
        System.out.println((Math.abs(x1 - x0) * Math.abs(y1 - y0) * DFS_DISTANCE_COST + DFS_INITIAL_COST));
        System.out.println(((Math.abs(x1 - x0) + 1) * (Math.abs(y1 - y0) + 1) * DFS_DISTANCE_COST + DFS_INITIAL_COST));

        if (previousResultsA == null || previousResultsA.size() == 0) {
            return worstCase;
        }

        int currentCombinedDistance = 1000 * 1000;
        ResultValue selectedSample = null;

        for (ResultValue resultValue : previousResultsA) {

            int distance = Math.abs(x0 - resultValue.getX0()) + Math.abs(x1 - resultValue.getX1()) + Math.abs(y0 - resultValue.getY0()) + Math.abs(y1 - resultValue.getY1());

            if (distance < currentCombinedDistance) {
                currentCombinedDistance = distance;
                selectedSample = resultValue;
            }
        }


        System.out.println(selectedSample);

        int distanceExpected = selectedSample.getX0() - x0 + x1 - selectedSample.getX1() + selectedSample.getY0() - y0 + y1 - selectedSample.getY1();


        int multiplier = 1;
        if (distanceExpected < 0) {
            multiplier = -1;
        }

        //  System.out.println(currentCombinedDistance);
        // System.out.println(distanceExpected);

        long expectedCase = (selectedSample.getBasicBlocks() + distanceExpected * distanceExpected * multiplier *
                DFS_DISTANCE_COST);

        if (expectedCase < 0)
            return worstCase;


        if (distanceExpected == 0)
            return expectedCase;

        if (Math.abs(distanceExpected) > 10) {
            return worstCase;
        }


        //  System.out.println(expectedCase);
        // System.out.println(worstCase);
        //  System.out.println(worstCase * 1.0 / expectedCase);


        return (expectedCase + worstCase) / 2;

    }

    public static long calculateCost(String request) {

        try {
            //  System.out.println("COST");
            // System.out.println(request);
            String maze = request.split("m=")[1].split("&")[0].substring(0, request.split("m=")[1].split("&")[0].length() - 5);
            String strategy = request.split("s=")[1];
            String velocity = request.split("v=")[1].split("&")[0];
            int x0 = Integer.parseInt(request.split("x0=")[1].split("&")[0]);
            int x1 = Integer.parseInt(request.split("x1=")[1].split("&")[0]);
            int y0 = Integer.parseInt(request.split("y0=")[1].split("&")[0]);
            int y1 = Integer.parseInt(request.split("y1=")[1].split("&")[0]);

           /* System.out.println(maze);
            System.out.println(strategy);
            System.out.println(velocity);
            System.out.println(x0);
            System.out.println(x1);
            System.out.println(y0);
            System.out.println(y1);
*/
            System.out.println("Getting cache");
            LinkedHashMap<String, Map<String, String>> cache = Messenger.getCacheTable();
            Set<String> keys = cache.keySet();
            Iterator it = keys.iterator();

            ArrayList<ResultValue> resultValues = new ArrayList<>();

            while (it.hasNext()) {

                Map<String, String> entry = cache.get(it.next());
                System.out.println("entry");
                System.out.println(entry);
                if (entry.get("maze").equals(maze) && entry.get("strategy").equals(strategy)) {

                    String entryParams = entry.get("request");
                    int entryBB = Integer.parseInt(entry.get("bb"));
                    int x00 = Integer.parseInt(entryParams.split("x0=")[1].split(",")[0]);
                    int x11 = Integer.parseInt(entryParams.split("x1=")[1].split(",")[0]);
                    int y00 = Integer.parseInt(entryParams.split("y0=")[1].split(",")[0]);
                    int y11 = Integer.parseInt(entryParams.split("y1=")[1].split(",")[0]);
                    int v = Integer.parseInt(entryParams.split("v=")[1].split(",")[0]);


                    if (Integer.parseInt(velocity) == v) {
                        resultValues.add(new ResultValue(x00, x11, y00, y11, entryBB, maze));

                    }


                }


            }

            if (strategy.equals("astar")) {
                return calculateAstarCost(resultValues, x0, x1, y0, y1, Integer.parseInt(velocity));
            } else if (strategy.equals("bfs")) {
                return calculateBfsCost(resultValues, x0, x1, y0, y1, Integer.parseInt(velocity));
            } else {
                return calculateDfsCost(resultValues, x0, x1, y0, y1, Integer.parseInt(velocity));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //todo: read from dynamo
        //todo: transform into resultvalue
        //todo: calculate expected cost

        return 0;
    }

    public static long calculateVCost(int v, long currentCost, HashMap<Integer, Double> statistics) {
        if (v >= 95) {
            return currentCost;
        }

        long result = currentCost;

        for (int x = 90; x > v - 5; x -= 10) {
            result = (long) (result * statistics.get(x));
        }


        return result;
    }

}
