package es.um.poa.agents.buyer;

import es.um.poa.productos.FishDeseoConfig;

import java.util.List;

public class BuyerAgentConfig {


	private String nombre;
	private String cif;
	private double budget;
	public List<FishDeseoConfig> listaDeseos;


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

	public List<FishDeseoConfig> getlistaDeseos() {
		return listaDeseos;
	}
}
