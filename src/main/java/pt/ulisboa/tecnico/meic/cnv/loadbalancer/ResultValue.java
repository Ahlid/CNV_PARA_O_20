package pt.ulisboa.tecnico.meic.cnv.loadbalancer;

public class ResultValue {
    private int x0;
    private int x1;
    private int y0;
    private int y1;
    private int basicBlocks;
    private String maze;

    public ResultValue(int x0, int x1, int y0, int y1, int basicBlocks, String maze) {
        this.x0 = x0;
        this.x1 = x1;
        this.y0 = y0;
        this.y1 = y1;
        this.basicBlocks = basicBlocks;
        this.maze = maze;
    }

    public int getX0() {
        return x0;
    }

    public void setX0(int x0) {
        this.x0 = x0;
    }

    public int getX1() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public int getY0() {
        return y0;
    }

    public void setY0(int y0) {
        this.y0 = y0;
    }

    public int getY1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public int getBasicBlocks() {
        return basicBlocks;
    }

    public void setBasicBlocks(int basicBlocks) {
        this.basicBlocks = basicBlocks;
    }

    public String getMaze() {
        return maze;
    }

    public void setMaze(String maze) {
        this.maze = maze;
    }


    @Override
    public String toString() {
        return "ResultValue{" +
                "x0=" + x0 +
                ", x1=" + x1 +
                ", y0=" + y0 +
                ", y1=" + y1 +
                ", basicBlocks=" + basicBlocks +
                ", maze='" + maze + '\'' +
                '}';
    }
}
