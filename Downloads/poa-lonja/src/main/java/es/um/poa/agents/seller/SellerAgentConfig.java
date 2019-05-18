package es.um.poa.agents.seller;

import es.um.poa.productos.FishConfig;

import java.util.List;

public class SellerAgentConfig {

	public String cif;
	public String nombre;
	public List<FishConfig> listaPescado;

	@Override
	public String toString() {
		return "SellerAgentConfig [lots=" + listaPescado + "]";
	}

	public List<FishConfig> getListaPescado() {
		return listaPescado;
	}

	public void setListaPescado(List<FishConfig> catchs) {
		this.listaPescado = catchs;
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
