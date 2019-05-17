package es.um.poa.agents.buyer;

public class BuyerAgentConfig {
	private String nombre;
	private String cif;
	private double budget;
	
	@Override
	public String toString() {
		return "BuyerAgentConfig [budget=" + budget + "]";
	}

	public double getBudget() {
		return budget;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getCif() {
		return cif;
	}

	public void setCif(String cif) {
		this.cif = cif;
	}

	public void setBudget(double budget) {
		this.budget = budget;
	}
}
