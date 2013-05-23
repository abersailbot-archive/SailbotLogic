package main;
import java.io.IOException;

import behavior.BoatBehavior;
import behavior.PIDBehavior;

/**
 * @author thip
 * @author Kamil Mrowiec <kam20@aber.ac.uk>
 * @version 1.0 (4 May 2013)
 */
public class Boat{
	
	private BoatBehavior behavior;
	public Waypoints waypoints;
	public Communication com;

	private int heading;
	private int windDirection;
	private int waypointHeading;

	private int sailTension;
	private int rudderPosition;

    private Position position;
    private Position nextWayPoint;

    /**
     * Creates boat with given waypoints.
     */
	public Boat(Waypoints wps){
		waypoints = wps;
		behavior = new PIDBehavior(this);
		com = new Communication();

        position = new Position ();
	}
	
	/**
	 * Creates boat with no waypoints.
	 */
	public Boat(){
		waypoints = new Waypoints();
		behavior = new PIDBehavior(this);
		com = new Communication();

        position = new Position ();
	}

	public void update(){
		if(waypoints.isEmpty()){
			System.out.println("No waypoints to go to.");
			return;
		}
		try{
			//Get sensors reading from Python controller
			readSensors();
			
			//Check if waypoint is reached, if so, go to next one.
			if(waypoints.waypointReached(this.position)) waypoints.moveToNext();
			
		}catch(IOException e){
			e.printStackTrace();
		}

		behavior.applyBehavior();
		behavior = behavior.nextBehavior();
	
		this.updateRudder(rudderPosition);
		this.updateSail(sailTension);

		try{
			Thread.sleep(1000);
		}catch(InterruptedException e){
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		}
	}

	public void readSensors() throws IOException{
		
		// Getting GPS location
		com.sendMessage("get easting");
		double easting = Double.parseDouble(com.readMessage());

		com.sendMessage("get northing");
		double northing = Math.abs(Double.parseDouble(com.readMessage()));

		position.set(easting, northing);
		
		//Getting compass and wind sensors readings
		com.sendRequest("get compass");
		heading = Integer.parseInt(com.readMessage());

		com.sendRequest("get wind_dir");
		windDirection = Integer.parseInt(com.readMessage());

        /////////////////////////
		//On real boat those will be calculated here, not received from the Python code.		
		
//		com.sendRequest("get waypointdir");
//		waypointHeading = Integer.parseInt(com.readMessage());
//
//		com.sendMessage("get waypointnum");
//		int wayPointNumber = Integer.parseInt(com.readMessage());
//
//		com.sendMessage("get waypointnorthing " + wayPointNumber);
//		nextWayPoint
//				.setLat(Math.abs(Double.parseDouble(com.readMessage())));
//
//		com.sendMessage("get waypointeasting " + wayPointNumber);
//		nextWayPoint.setLon(Double.parseDouble(com.readMessage()));

        ///////////////////

		
	}

	public void updateRudder(int position){
		this.rudderPosition = position;
		try{
			com.sendMessage("set rudder " + rudderPosition);
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public void updateSail(int tension){
		this.sailTension = tension;
		try{
			com.sendMessage("set sail " + sailTension);
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public int getHeading(){
		return heading;
	}

	public int getWindDirection(){
		return windDirection;
	}

	public int getWaypointHeading(){
		return (int) Position.getHeading(position, nextWayPoint);
	}

    public Position getPosition ()
    {
        return position;
    }

    public Position getNextWayPoint ()
    {
        return waypoints.getNextWaypoint();
    }
}