package es.um.poa.agents.seller;

public class Lot {
	private int time;
    private float kg;
    private String type;
        
	@Override
	public String toString() {
		return "Lot [time=" + time + ", kg=" + kg + ", type=" + type + "]";
	}
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	public float getKg() {
		return kg;
	}
	public void setKg(float kg) {
		this.kg = kg;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
