package pt.ulisboa.tecnico.meic.cnv.loadbalancer;

import java.util.ArrayList;

public class CostFunction {


    private final static int ASTAR_INITIAL_COST = 83241;
    private final static int DFS_INITIAL_COST = 90128;
    private final static int BFS_INITIAL_COST = 90139;


    private final static int ASTAR_DISTANCE_COST = 88028;
    private final static int DFS_DISTANCE_COST = 35929;
    private final static int BFS_DISTANCE_COST = 6898;


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

    public static int calculateAstarCost(ArrayList<ResultValue> previousResultsA, int x0, int x1, int y0, int y1) {

        int worstCase = (((Math.abs(x1 - x0) * Math.abs(y1 - y0) * ASTAR_DISTANCE_COST + ASTAR_INITIAL_COST) +
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

        int expectedCase = (int) (selectedSample.getBasicBlocks() + distanceExpected * distanceExpected * multiplier *
                ASTAR_DISTANCE_COST);

        if (distanceExpected == 0)
            return expectedCase;

        //  System.out.println(expectedCase);
        // System.out.println(worstCase);
        //  System.out.println(worstCase * 1.0 / expectedCase);


        return (expectedCase + worstCase) / 2;


    }

    public static int calculateBfsCost(ArrayList<ResultValue> previousResultsA, int x0, int x1, int y0, int y1) {

        int worstCase = (((Math.abs(x1 - x0) * Math.abs(y1 - y0) * BFS_DISTANCE_COST + BFS_INITIAL_COST) +
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

        int distanceExpected = selectedSample.getX0() - x0 + x1 - selectedSample.getX1() + selectedSample.getY0() - y0 + y1 - selectedSample.getY1();


        int multiplier = 1;
        if (distanceExpected < 0) {
            multiplier = -1;
        }

        //  System.out.println(currentCombinedDistance);
        // System.out.println(distanceExpected);

        int expectedCase = (int) (selectedSample.getBasicBlocks() + distanceExpected * distanceExpected * multiplier *
                BFS_DISTANCE_COST);

        if (distanceExpected == 0)
            return expectedCase;

        //  System.out.println(expectedCase);
        // System.out.println(worstCase);
        //  System.out.println(worstCase * 1.0 / expectedCase);


        return (expectedCase + worstCase) / 2;


    }

    public static int calculateDfsCost(ArrayList<ResultValue> previousResultsA, int x0, int x1, int y0, int y1) {

        int worstCase = (((Math.abs(x1 - x0) * Math.abs(y1 - y0) * DFS_DISTANCE_COST + DFS_INITIAL_COST) +
                ((Math.abs(x1 - x0) + 1) * (Math.abs(y1 - y0) + 1) * DFS_DISTANCE_COST + DFS_INITIAL_COST)) / 2);

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

        int expectedCase = (int) (selectedSample.getBasicBlocks() + distanceExpected * distanceExpected * multiplier *
                DFS_DISTANCE_COST);

        if (distanceExpected == 0)
            return expectedCase;

        //  System.out.println(expectedCase);
        // System.out.println(worstCase);
        //  System.out.println(worstCase * 1.0 / expectedCase);


        return (expectedCase + worstCase) / 2;


    }
}
