package es.um.poa.Objetos;

import es.um.poa.productos.Fish;

import java.io.Serializable;
import java.util.LinkedList;


/**
 *  La clase buyer representa un comprador.
 *  Contiene:
 *  - Un identificador que será el CIF
 *  - El nombre del comprador
 *  - El saldo
 *  - El gasto que tiene
 *  - Una lista con los articulos comprados
 */
public class Buyer implements Serializable {

    private final String identificador;
    private final String nombre;
    private double saldo;
    private double gastos;
    private LinkedList<Fish> articulosComprados;
    private LinkedList<String> listaDeseos;


    /**
     *  @param identificador
     * @param nombre
     * @param saldo
     */
    public Buyer(String identificador, String nombre, double saldo) {
        this.identificador = identificador;
        this.nombre = nombre;
        this.saldo = saldo;
        this.gastos = 0;
        this.articulosComprados = new LinkedList<>();
    }

    public Buyer(String cif, String nombre) {
        this.identificador = cif;
        this.nombre = nombre;
        this.saldo = 0;
        this.gastos = 0;
        this.articulosComprados = new LinkedList<>();
    }


    /*
     * Retorna el identificador del comprador
     */
    public String getCif() {
        return identificador;
    }

    /**
     * @return Retorna su saldo actual
     */
    public double getSaldo() {
        return saldo;
    }

    /**
     * Se establece el saldo del comprador
     * @param saldo
     */
    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    /**
     * @return
     */
    public double getGastos() {
        return gastos;
    }

    public void setGastos(double gastos) {
        this.gastos = gastos;
    }

    public LinkedList<Fish> getArticulosComprados() {
        return articulosComprados;
    }


    public void setArticulosComprados(LinkedList<Fish> articulosComprados) {
        this.articulosComprados = articulosComprados;
    }

    public String getNombre() {
        return nombre;
    }

    public LinkedList<String> getListaDeseos() {
        return listaDeseos;
    }

    public void setListaDeseos(LinkedList<String> listaDeseos) {
        this.listaDeseos = listaDeseos;
    }

    public void comprarLote(Fish fish) {
        if (!articulosComprados.contains(fish))
            this.articulosComprados.add(fish);
    }
}
