package client;

public class Box {
    private char letter;
    private String color;
    private Coordinate coordinate;

    public Box(char letter, String color, Coordinate coordinate) {
        this.letter = letter;
        this.color = color;
        this.coordinate = coordinate;
    }

    public char getLetter() {
        return letter;
    }

    public void setLetter(char letter) {
        this.letter = letter;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
    
    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }
}
