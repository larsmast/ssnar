/**
 * This code is written as part of a Master Thesis
 * the spring of 2017.
 *
 * Geir Eikeland (Master 2017 @ NTNU)
 */
package no.ntnu.ge.slam;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import no.ntnu.et.general.Angle;
import no.ntnu.et.general.Pose;
import no.ntnu.et.general.Position;
import no.ntnu.et.map.MapLocation;
import static no.ntnu.et.map.MapLocation.getOctant;
import static no.ntnu.et.map.MapLocation.sum;
import static no.ntnu.et.mapping.MappingController.getLineBetweenPoints;
import no.ntnu.et.mapping.Sensor;
import no.ntnu.et.simulator.SlamRobot;
import no.ntnu.tem.communication.Inbox;

/**
 *
 * @author geirhei
 */
public class SlamMappingController extends Thread {
    private final int cellSize = 2;
    private SlamRobot robot;
    private Inbox inbox;
    private WindowMap map;
    private WindowMap localWindow;
    private WindowMap remoteWindow;
    private boolean paused;
    private LinkedBlockingQueue<int[]> updateQueue;
    private SlamMeasurementHandler measurementHandler;
    private MapLocation origoLocation;
    //private boolean robotBusy;
    
    public SlamMappingController(SlamRobot robot, Inbox inbox) {
        this.robot = robot;
        this.inbox = inbox;
        map = robot.getWindowMap();
        localWindow = robot.getLocalWindow();
        remoteWindow = robot.getRemoteWindow();
        updateQueue = robot.getUpdateQueue();
        measurementHandler = new SlamMeasurementHandler(robot);
        origoLocation = null;
        //robotBusy = robot.isBusy();
        
    }
    
    @Override
    public void start(){
        if(!isAlive()){
            super.start();
        }
        paused = false;
    }

    /**
     * Pauses the mapping
     */
    public void pause(){
        paused = true;
    }
    
    /**
     * Returns if the mapping is running or paused
     * @return 
     */
    public boolean isRunning(){
        return !paused;
    }
    
    @Override
    public void run() {
        fillTestRemoteWindow(); // For testing with simple occupied row in window
        remoteWindow.setGlobalStartRow(remoteWindow.getHeight());
        remoteWindow.setGlobalStartColumn(0); // already set
        
        while (true) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
            if (paused){
                continue;
            }
            
            if (measurementHandler.updateMeasurement() == false) {
                continue;
            }
            
            Position robotPosition = measurementHandler.getRobotPosition();
            Angle robotAngle = measurementHandler.getRobotHeading();
            Pose robotPose = new Pose(robotPosition, robotAngle);

            // Find the location of the robot in the world map
            MapLocation robotLocation = findLocationInMap(robotPosition);
            if (origoLocation == null) {
                origoLocation = robotLocation;
            }
            
            // Check if robot is busy, if not set origoLocation to current location
            if (!robot.isBusy()) {
                origoLocation = robotLocation;
            }
            
            //TEST
            /*
            MapLocation measLoc1 = new MapLocation(49,0);
            MapLocation measLoc2 = new MapLocation(49,99);
            MapLocation measLoc3 = new MapLocation(0,49);
            MapLocation measLoc4 = new MapLocation(99,49);
            map.addMeasurement(measLoc1, true);
            map.addMeasurement(measLoc2, true);
            map.addMeasurement(measLoc3, true);
            map.addMeasurement(measLoc4, true);
            //ArrayList<MapLocation> lineOfSight = getLineBetweenPoints(new MapLocation(40,0), measLoc);
            //for (MapLocation location : lineOfSight) {
            //    map.addMeasurement(location, false);
            //}
            map.shift(new MapLocation(49,49), new MapLocation(50,49));
            map.print();
            //TEST
            */
            
            Sensor[] sensors = measurementHandler.getIRSensorData();
            for (Sensor sensor : sensors) {
                //boolean tooClose = false; - does not care about position of other robots
                
                MapLocation windowLocation = findLocationInWindow(robotPose);
                MapLocation measurementLocation = findLocationInWindowMap(sensor.getOffsetPosition());
                if (sensor.isMeasurement()) {
                    map.addMeasurement(measurementLocation, true);
                }
                
                // Create a measurements indicating no obstacle in the sensors line of sight
                //ArrayList<MapLocation> lineOfSight = getLineBetweenPoints(robotLocation, measurementLocation);
                ArrayList<MapLocation> lineOfSight = getLineBetweenPoints(new MapLocation(49,49), measurementLocation);
                for (MapLocation location : lineOfSight) {
                    map.addMeasurement(location, false);
                }
            //map.print();
            }
        //map.print();   

        }
    }
    
    /**
     * Returns the MapLocation that corresponds to the specified position.
     * From GridMap.java by Eirik Thon.
     * 
     * @param position
     * @return MapLocation
     */
    private MapLocation findLocationInMap(Position position) {
        int row = 0;
        if(position.getYValue() >= 0){
            row = (int)(position.getYValue()/cellSize);
        }else{
            if(position.getYValue()%cellSize == 0){
                row = (int)(position.getYValue()/cellSize);
            }else{
                row = (int)(position.getYValue()/cellSize)-1;
            }
        }
        int column = 0;
        if(position.getXValue() >= 0){
            column = (int)(position.getXValue()/cellSize);
        }else{
            if(position.getXValue()%cellSize == 0){
                column = (int)(position.getXValue()/cellSize);
            }else{
                column = (int)(position.getXValue()/cellSize)-1;
            }
        }
        return new MapLocation(row, column);
    }
    
    
    private MapLocation findLocationInWindowMap(Position position) {
        int row = 0;
        if(position.getYValue() >= 0){
            row = (int)(position.getYValue()/cellSize);
        }else{
            if(position.getYValue()%cellSize == 0){
                row = (int)(position.getYValue()/cellSize);
            }else{
                row = (int)(position.getYValue()/cellSize)-1;
            }
        }
        int column = 0;
        if(position.getXValue() >= 0){
            column = (int)(position.getXValue()/cellSize);
        }else{
            if(position.getXValue()%cellSize == 0){
                column = (int)(position.getXValue()/cellSize);
            }else{
                column = (int)(position.getXValue()/cellSize)-1;
            }
        }
        // compensate for negative indices
        row += map.getHeight()/2 - 1;
        column += map.getWidth()/2 - 1;
        return new MapLocation(row, column);
    }
    
    
    private MapLocation findLocationInWindow(Pose pose) {
        Position globalPosition = pose.getPosition();
        MapLocation globalMapLocation = findLocationInMap(globalPosition);
        int dx = globalMapLocation.getColumn() - origoLocation.getColumn();
        int dy = globalMapLocation.getRow() - origoLocation.getRow();
        if (dx == 0 && dy == 0) {
            return new MapLocation(0, 0);
        }
        
        MapLocation startLocation = new MapLocation(49, 49);
        MapLocation offset;
        MapLocation windowLocation;
        
        double heading = pose.getHeading().getValue(); // print value of heading?
        int octant = getOctant(heading);
        switch (octant) {
            case 0:
            case 7:
                offset = new MapLocation(dx, dy);
                break;
            
            case 1:
            case 2:
                offset = new MapLocation(dy, dx);
                break;
                
            case 3:
            case 4:
                offset = new MapLocation(-dy, dx);
                break;
                
            case 5:
            case 6:
                offset = new MapLocation(-dx, -dy);
                break;
                
            default:
                offset = new MapLocation(0, 0);
                break;
        }
        
        windowLocation = sum(startLocation, offset);
        System.out.println("Row: " + windowLocation.getRow() + ", Col: " + windowLocation.getColumn());
        return windowLocation;
    }
    
    // fix: negavtive locations
    private boolean robotHasMoved(MapLocation currentLoc, MapLocation newLoc) {
        int dx = newLoc.getRow() - currentLoc.getRow();
        int dy = newLoc.getColumn() - currentLoc.getColumn();
        return !(dx == 0 && dy == 0);
    }
    
    private void fillTestRemoteWindow() {
        int[][] window = remoteWindow.getWindow();
        for (int i = 0; i < remoteWindow.getHeight(); i++) {
            for (int j = 0; j < remoteWindow.getWidth(); j++) {
                window[i][j] = 2;
            }
        }
        for (int k = 0; k < remoteWindow.getWidth(); k++) {
            window[75][k] = 1;
        }
    }
    
}
