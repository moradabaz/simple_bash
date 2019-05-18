package es.um.poa.tests;

import es.um.poa.productos.Fish;
import es.um.poa.productos.FishConfig;
import es.um.poa.productos.TipoProducto;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class FishTest {

    Fish fish;
    List<FishConfig> listaPeces;
    @Before
    public void inicializar() {
        fish = new Fish("Sardina", TipoProducto.PESCADO_AZUL);
    }

    @Test
    public void crearFish1() {
        String tipoNombre = "pescado azul";
        TipoProducto tipoProducto = TipoProducto.getType(tipoNombre);
        assertEquals(TipoProducto.PESCADO_AZUL, tipoProducto);
    }

    @Test
    public void crearFish2() {
        String tipoNombre = "pescado azul";
        TipoProducto tipoProducto = TipoProducto.getType(tipoNombre);
        System.out.println(TipoProducto.PESCADO_AZUL.getName() + " " + tipoProducto.getName());
        assertEquals(TipoProducto.PESCADO_AZUL.getName(), tipoProducto.getName());
    }




}