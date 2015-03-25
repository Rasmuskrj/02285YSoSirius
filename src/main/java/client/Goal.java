package client;

public class Goal {
    private char letter;
    private Coordinate coordinate;

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
}
