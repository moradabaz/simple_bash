package es.um.poa.agents.objetos;

import java.io.Serializable;

public class Fish implements Serializable {

    private String propietario;
    private String tipoPescado;
    private float kilos;
    private int precioInicial;
    private int precioActual;
    private int precioMinimo;
    private Tiempo horaDeposito;

    public Fish(String propietario, int precionIncial, int precioActual, int precioMinimo, float kilos, String tipoPescado, Tiempo tiempo) {
        this.propietario = propietario;
        this.kilos = kilos;
        this.tipoPescado = tipoPescado;
        this.precioInicial = precionIncial;
        this.precioActual = precioActual;
        this.precioMinimo = precioMinimo;
        this.horaDeposito = tiempo;
    }

    public Tiempo getHoraDeposito() {
        return horaDeposito;
    }

    public String getPropietario() {
        return propietario;
    }

    public void setPropietario(String propietario) {
        this.propietario = propietario;
    }

    public String getTipoPescado() {
        return tipoPescado;
    }

    public void setTipoPescado(String tipoPescado) {
        this.tipoPescado = tipoPescado;
    }

    public float getKilos() {
        return kilos;
    }

    public void setKilos(float kilos) {
        this.kilos = kilos;
    }

    public int getPrecioInicial() {
        return precioInicial;
    }

    public void setPrecioInicial(int precioInicial) {
        this.precioInicial = precioInicial;
    }

    public int getPrecioActual() {
        return precioActual;
    }

    public void setPrecioActual(int precioActual) {
        this.precioActual = precioActual;
    }

    public int getPrecioMinimo() {
        return precioMinimo;
    }

    public void setPrecioMinimo(int precioMinimo) {
        this.precioMinimo = precioMinimo;
    }

    public void setHoraDeposito(Tiempo horaDeposito) {
        this.horaDeposito = horaDeposito;
    }
}
