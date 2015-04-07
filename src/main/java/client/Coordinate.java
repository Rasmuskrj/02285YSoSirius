package client;

public class Coordinate {
    private int row;
    private int column;
    public Coordinate(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }
    
    public String toString() {
    	return this.row + ", " + this.column;
    }
    
    @Override
    public int hashCode() {
    	return 3571 + this.row + this.column;
    }
    
    @Override
    public boolean equals(Object obj) {
    	if (this == obj)
    		return true;
    	if (obj == null)
    		return false;
    	if (this.getClass() != obj.getClass())
    		return false;
    	Coordinate other = (Coordinate) obj;
    	if (this.row != other.row || this.column != other.column)
    		return false;
    	return true;
    }


}
