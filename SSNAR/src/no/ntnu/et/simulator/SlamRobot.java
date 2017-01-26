/**
 * This code is written as part of a Master Thesis
 * the spring of 2017.
 *
 * Geir Eikeland (Master 2017 @ NTNU)
 * 
 * Based on BasicRobot.java
 */
package no.ntnu.et.simulator;

import java.util.concurrent.LinkedBlockingQueue;
import no.ntnu.et.general.Pose;
import no.ntnu.et.general.Position;
import no.ntnu.et.map.MapLocation;

/**
 *
 * @author geirhei
 */
public class SlamRobot extends SimRobot {
    private final int windowHeight = 100;
    private final int windowWidth = 100;
    private int[][] mapWindow;
    private LinkedBlockingQueue<int[]> updateQueue;
    private final Object mapLock = new Object();
    
    SlamRobot(SimWorld world, Pose initialPose, String name, int id) {
        super(world, initialPose, name, id);
        mapWindow = new int[windowHeight][windowWidth];
        initMapWindow(mapWindow);
        updateQueue = new LinkedBlockingQueue<>(5);
    }
    
    private void initMapWindow(int[][] mapWindow) {
        for (int i = 0; i < windowHeight; i++) {
            for (int j = 0; j < windowWidth; j++) {
                mapWindow[i][j] = 2; // unexplored
            }
        }
    }
    
    public int[][] getMapWindow() {
        synchronized (mapLock) {
            return mapWindow;
        }
    }
    
    public LinkedBlockingQueue<int[]> getUpdateQueue() {
        return updateQueue;
    }
    
    /**
     * Adds a measurement to the internal updateQueue.
     * 
     * @param meas
     * @return True if successful. Throws IllegalStateException is queue is full.
     */
    boolean addUpdate(int[] meas) {
        try {
            return updateQueue.add(meas);
        } catch (Exception e) {
            System.err.println("Exception in addUpdate: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Returns and removes an update from the internal updateQueue.
     * 
     * @return int[] if successful, null if no elements in queue
     */
    public int[] getUpdate() {
        return updateQueue.poll();
    }
    
    public MapLocation getInitialLocation() {
        Pose initialPose = getInitialPose();
        Position initialPosition = initialPose.getPosition();
        return new MapLocation((int)initialPosition.getXValue(), (int)initialPosition.getYValue());
    }
}
