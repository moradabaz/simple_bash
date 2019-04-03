package es.um.poa.agents.clock;

public class SimTimeOntology {
	
	public static String RUNNING = "RUNNING";
	public static String END = "END";
	
	private String simState;
	private int day;
	private int time;

	public SimTimeOntology() {
		this.simState = RUNNING;
	}
	
	public SimTimeOntology(int day, int time) {
		this();
		this.day = day;
		this.time = time;
	}
	
	public String getSimState() {
		return simState;
	}
	public void setSimState(String sim_state) {
		this.simState = sim_state;
	}
	public int getDay() {
		return day;
	}
	public void setDay(int day) {
		this.day = day;
	}
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	
	public String toString() {
		return "SimTimeOntology(day="+day+",time="+time+",simState="+simState+")";
	}
}
