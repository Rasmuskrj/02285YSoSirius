package client;

public class Box {
    private char letter;
    private String color;
    private Coordinate coordinate;
    private boolean inFinalPosition = false;

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

    public boolean isInFinalPosition() {
        return inFinalPosition;
    }

    public void setInFinalPosition(boolean inFinalPosition) {
        this.inFinalPosition = inFinalPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Box box = (Box) o;

        if (letter != box.letter) return false;
        if (inFinalPosition != box.inFinalPosition) return false;
        if (color != null ? !color.equals(box.color) : box.color != null) return false;
        return coordinate.equals(box.coordinate);

    }

    @Override
    public int hashCode() {
        int result = (int) letter;
        result = 31 * result + (color != null ? color.hashCode() : 0);
        result = 31 * result + coordinate.hashCode();
        result = 31 * result + (inFinalPosition ? 1 : 0);
        return result;
    }
}
