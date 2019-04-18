package es.um.poa.productos;

import java.io.Serializable;

public class Fish implements Serializable {

    private static final int NO_ASIGNADO = -100;

    private EstadoVenta estadoVenta;
    private final TipoProducto tipoProducto;
    private final String nombre;
    private int calidad;
    private double peso;
    private double precioReserva;
    private double precioSalida;
    private double precioFinal;
    private Tiempo horaRegistro;
    private Tiempo horaVenta;
    private int idComprador;
    private int idVendedor;

    public Fish() {
        nombre = null;
        tipoProducto = TipoProducto.DESCONOCIDO;
    }
}
