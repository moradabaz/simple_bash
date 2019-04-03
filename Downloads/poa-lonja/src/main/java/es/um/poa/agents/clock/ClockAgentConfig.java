package es.um.poa.agents.clock;

public class ClockAgentConfig {
	private int unitTimeMillis;
	private int numUnitDay;
	private int numSimDays;
	
	@Override
	public String toString() {
		return "ClockAgentConfig [unitTimeMillis=" + unitTimeMillis + ", numUnitDay=" + numUnitDay + ", numSimDays="
				+ numSimDays + "]";
	}
	
	public int getUnitTimeMillis() {
		return unitTimeMillis;
	}
	public void setUnitTimeMillis(int unitTimeMillis) {
		this.unitTimeMillis = unitTimeMillis;
	}
	public int getNumUnitDay() {
		return numUnitDay;
	}
	public void setNumUnitDay(int numUnitDay) {
		this.numUnitDay = numUnitDay;
	}
	public int getNumSimDays() {
		return numSimDays;
	}
	public void setNumSimDays(int numSimDays) {
		this.numSimDays = numSimDays;
	}
}
