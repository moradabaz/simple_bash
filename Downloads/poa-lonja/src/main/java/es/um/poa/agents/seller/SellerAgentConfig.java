package es.um.poa.agents.seller;

import es.um.poa.productos.Fish;

import java.util.List;

public class SellerAgentConfig {

	private String cif;
	private String nombre;
	List<Fish> lots;


	@Override
	public String toString() {
		return "SellerAgentConfig [lots=" + lots + "]";
	}

	public List<Fish> getLots() {
		return lots;
	}

	public void setLots(List<Fish> lots) {
		this.lots = lots;
	}

	public String getCif() {
		return cif;
	}

	public void setCif(String cif) {
		this.cif = cif;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
}
