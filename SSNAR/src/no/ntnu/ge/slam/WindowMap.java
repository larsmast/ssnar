/**
 * This code is written as part of a Master Thesis
 * the spring of 2017.
 *
 * Geir Eikeland (Master 2017 @ NTNU)
 */
package no.ntnu.ge.slam;

import no.ntnu.et.map.MapLocation;

/**
 *
 * @author geirhei
 */
public class WindowMap {
    private int[][] map;
    private final int height;
    private final int width;
    private final Object mapLock = new Object();
    
    public WindowMap(int height, int width) {
        this.height = height;
        this.width = width;
        map = new int[this.height][this.width];
        initWindowMap();
    }
    
    private void initWindowMap() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                map[i][j] = 2; // unexplored
            }
        }
    }
    
    public boolean shift(MapLocation currentLoc, MapLocation newLoc) {
        int dx = newLoc.getRow() - currentLoc.getRow();
        int dy = newLoc.getColumn() - currentLoc.getColumn();
        if (dx == 0 && dy == 0) {
            return false;
        } else {
            if (dx > 0) {
                // shift right
            } else if (dx < 0) {
                // shift left
            }
            if (dy > 0) {
                // shift down
            } else if (dy < 0) {
                // shift up
            }
            return true;
        }
    }
    
    /**
     * Updates the map.
     * (This method also updates the restricted and weakly
     * restricted area of the map if the occupied status of a cell changes.)
     * 
     * @param location
     * @param measurement 
     */
    public void addMeasurement(MapLocation location, boolean measurement) {
        int row = location.getRow();
        int col = location.getColumn();
        if (measurement) {
            map[row][col] = 1; //occupied
        } else {
            map[row][col] = 0; // free
        } // (unexplored = 2)

        // If the cell changes from occupied to free or vice versa, the restricted
        // status of nearby cells are updated here:
        /*
        if(measuredCell.stateChanged()){
            ArrayList<MapLocation> restricted = createCircle(location, 15);
            ArrayList<MapLocation> weaklyRestricted = createCircle(location, 25);
            for(MapLocation location2: restricted){
                if(measuredCell.isOccupied()){
                    map.get(location2).addRestrictingCell(measuredCell);
                }
                else {
                    map.get(location2).removeRestrictingCell(measuredCell);
                }
            }
            for(MapLocation location2: weaklyRestricted){
                if(measuredCell.isOccupied()){
                    map.get(location2).addWeaklyRestrictingCell(measuredCell);
                }
                else {
                    map.get(location2).removeWeaklyRestrictingCell(measuredCell);
                }
            }
        }
        */
    }
}
