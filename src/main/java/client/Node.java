package main.java.client;

import java.awt.*;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;

/**
 * Created by simon on 24/03/15.
 */
public class Node {
    public static HashMap<Coordinate, Boolean> walls = new HashMap<Coordinate, Boolean>();
    private HashMap<Box, Coordinate> boxes = new HashMap<Box, Coordinate>();
    public static HashMap<Goal, Coordinate> goals = new HashMap<Goal, Coordinate>();

    public HashMap<Box, Coordinate> getBoxes() {
        return boxes;
    }

    public void setBoxes(HashMap<Box, Coordinate> boxes) {
        this.boxes = boxes;
    }
}
