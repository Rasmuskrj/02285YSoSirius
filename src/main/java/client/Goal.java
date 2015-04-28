package client;

public class Goal {
    private char letter;
    private Coordinate coordinate;
    private int priority = 0;
    private boolean currentMainGoal = false;

    public Goal(char letter, Coordinate coordinate) {
        this.letter = letter;
        this.coordinate = coordinate;
    }

    public char getLetter() {
        return letter;
    }

    public void setLetter(char letter) {
        this.letter = letter;
    }
    
    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isCurrentMainGoal() {
        return currentMainGoal;
    }

    public void setCurrentMainGoal(boolean currentMainGoal) {
        this.currentMainGoal = currentMainGoal;
    }
}
