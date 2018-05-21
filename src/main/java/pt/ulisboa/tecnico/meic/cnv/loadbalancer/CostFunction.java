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


    private static ArrayList<ResultValue> previousResultsA = new ArrayList<>();

    public static void main(String[] args) {


     /*   previousResultsA.add(new ResultValue(0, 1, 1, 1, 83107, "astar"));
        previousResultsA.add(new ResultValue(0, 10, 1, 10, 9508827, "astar"));
        previousResultsA.add(new ResultValue(0, 20, 1, 21, 18944537, "astar"));
        previousResultsA.add(new ResultValue(25, 0, 25, 49, 16042320, "astar"));
        previousResultsA.add(new ResultValue(25, 48, 25, 49, 49444678, "astar"));
        previousResultsA.add(new ResultValue(25, 48, 25, 49, 49444678, "astar"));
        previousResultsA.add(new ResultValue(25, 48, 25, 1, 51991158, "astar"));
        previousResultsA.add(new ResultValue(1, 10, 1, 21, 3253813, "astar"));
        previousResultsA.add(new ResultValue(1, 10, 1, 15, 6337247, "astar"));
        previousResultsA.add(new ResultValue(1, 8, 1, 8, 5103073, "astar"));*/

        System.out.printf("%d\n", calculateAstarCost(previousResultsA, 1, 8, 1, 8));
        System.out.printf("%d\n", calculateAstarCost(previousResultsA, 1, 6, 1, 6));
        System.out.printf("%d\n", calculateAstarCost(previousResultsA, 1, 25, 1, 15));
        System.out.printf("%d\n", calculateAstarCost(previousResultsA, 1, 20, 1, 15));
    }

    public static long calculateAstarCost(ArrayList<ResultValue> previousResultsA, int x0, int x1, int y0, int y1) {

        long worstCase = (((Math.abs(x1 - x0) * Math.abs(y1 - y0) * ASTAR_DISTANCE_COST + ASTAR_INITIAL_COST) +
                ((Math.abs(x1 - x0) + 1) * (Math.abs(y1 - y0) + 1) * ASTAR_DISTANCE_COST + ASTAR_INITIAL_COST)) / 2);

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

    public static long calculateBfsCost(ArrayList<ResultValue> previousResultsA, int x0, int x1, int y0, int y1) {

        long worstCase = (((Math.abs(x1 - x0) * Math.abs(y1 - y0) * BFS_DISTANCE_COST + BFS_INITIAL_COST) +
                ((Math.abs(x1 - x0) + 1) * (Math.abs(y1 - y0) + 1) * BFS_DISTANCE_COST + BFS_INITIAL_COST)) / 2);

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

    public static long calculateDfsCost(ArrayList<ResultValue> previousResultsA, int x0, int x1, int y0, int y1) {

        System.out.println(previousResultsA);

        long worstCase = (((Math.abs(x1 - x0) * Math.abs(y1 - y0) * DFS_DISTANCE_COST + DFS_INITIAL_COST) +
                ((Math.abs(x1 - x0) + 1) * (Math.abs(y1 - y0) + 1) * DFS_DISTANCE_COST + DFS_INITIAL_COST)) / 2);

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
                return calculateAstarCost(resultValues, x0, x1, y0, y1);
            } else if (strategy.equals("bfs")) {
                return calculateBfsCost(resultValues, x0, x1, y0, y1);
            } else {
                return calculateDfsCost(resultValues, x0, x1, y0, y1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //todo: read from dynamo
        //todo: transform into resultvalue
        //todo: calculate expected cost

        return 0;
    }
}
