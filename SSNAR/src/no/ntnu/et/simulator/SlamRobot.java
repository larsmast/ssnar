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

/**
 *
 * @author geirhei
 */
public class SlamRobot extends SimRobot {
    private final int windowHeight = 100;
    private final int windowWidth = 100;
    private int[][] mapWindow;
    private LinkedBlockingQueue<int[]> measurementQueue;
    
    SlamRobot(SimWorld world, Pose initialPose, String name, int id) {
        super(world, initialPose, name, id);
        mapWindow = new int[windowHeight][windowWidth];
        measurementQueue = new LinkedBlockingQueue(5);
    }
    
    public int[][] getMapWindow() {
        return mapWindow;
    }
    
    public LinkedBlockingQueue<int[]> getMeasurementQueue() {
        return measurementQueue;
    }
    
    /**
     * Adds a measurement to the internal measurementQueue.
     * 
     * @param meas
     * @return True if successful. Throws IllegalStateException is queue is full.
     */
    boolean addMeasurement(int[] meas) {
        try {
            return measurementQueue.add(meas);
        } catch (Exception e) {
            System.err.println("Exception in addMeasurement: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Returns and removes a measurement from the internal measurementQueue.
     * 
     * @return int[] if successful, null if no elements in queue
     */
    public int[] getMeasurement() {
        return measurementQueue.poll();
    }
}
