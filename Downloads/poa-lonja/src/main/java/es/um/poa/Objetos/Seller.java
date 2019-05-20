package es.um.poa.Objetos;

import es.um.poa.productos.EstadoVenta;
import es.um.poa.productos.Fish;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;


/**
 * La clase Seller representa un vendedor
 * Sus atributos son:
 * - cif: Numero de identificacion del vendedor
 * - nombre: Nombre del vendedor
 * - numeroVentas: Numero de ventas realizadas
 */
public class Seller implements Serializable {

    private final String cif;
    private String nombre;
    private LinkedList<Fish> listaPescado;
    private LinkedList<Fish> pescadoVendido;


    public Seller(String cif, String nombre) {
        this.cif = cif;
        this.nombre = nombre;
        this.listaPescado = new LinkedList<>();
        this.pescadoVendido = new LinkedList<>();
    }

    public Seller(String cif, String nombre, List<Fish> listaPescado) {
        this.cif = cif;
        this.nombre = nombre;
        this.listaPescado = new LinkedList<Fish>(listaPescado);
        this.pescadoVendido = new LinkedList<>();
    }


    /**
     *
     * @return Retorna el numero de identificacion del vendedor
     */
    public String getCif() {
        return cif;
    }


    /*
     * @return Retorna el nombre del vendedor
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @return  Retorna el numero de ventas realizadas por el vendedor
     */
    public int getNumeroVentas() {
        return pescadoVendido.size();
    }

    /**
     * @return Retorna la lista de articulos (pescado) que oferta
     */
    public LinkedList<Fish> getListaPescado() {
        return listaPescado;
    }

    public void setListaPescado(List<Fish> listaPescado) {
        this.listaPescado = new LinkedList<>(listaPescado);
        for (Fish fish : this.listaPescado) {
            fish.setIdComprador(cif);
        }
    }

    public void registrarLotes() {
        for (Fish fish : this.listaPescado) {
            fish.setEstadoVenta(EstadoVenta.REGISTRADO);
        }
    }

    /**
     * @return  Retorna la lista de articulos vendidos
     */
    public LinkedList<Fish> getPescadoVendido() {
        return pescadoVendido;
    }


    /**
     *
     * @param nombre
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }


    /**
     *
     * @param fish
     */
    public void anadirPescadoALista(Fish fish) {
        listaPescado.add(fish);
    }


    /**
     *
     * @param fish
     */
    public void eliminarPescadoDeLista(Fish fish) {
        if (listaPescado.contains(fish))
            listaPescado.remove(fish);
    }


    /**
     *
     * @param fish
     * @return
     */
    public boolean estaEnOferta(Fish fish) {
        return listaPescado.contains(fish);
    }


    /**
     *
     * @param fish
     */
    public void anadirPescadoAVendido(Fish fish) {
        pescadoVendido.add(fish);
    }


    /**
     *
     * @param fish
     */
    public void eliminarPescadoVendido(Fish fish) {
        if (pescadoVendido.contains(fish))
            pescadoVendido.remove(fish);
    }


    /**
     *
     * @param fish
     * @return
     */
    public boolean estaVendido(Fish fish) {
        return pescadoVendido.contains(fish);
    }

}
