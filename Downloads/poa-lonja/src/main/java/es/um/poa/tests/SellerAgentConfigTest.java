package es.um.poa.tests;

import es.um.poa.Objetos.Seller;
import es.um.poa.agents.seller.SellerAgentConfig;
import es.um.poa.productos.Fish;
import es.um.poa.productos.FishConfig;
import org.junit.Before;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class SellerAgentConfigTest {


    SellerAgentConfig config = null;
    Seller seller = null;
    List<FishConfig> listaPeces;

    @Before
    public void funcion() {
        try {
            Yaml yaml = new Yaml();
            InputStream inputStream;
            inputStream = new FileInputStream("/Users/morad/Downloads/poa-lonja/configs/sellerType1.yaml");
            config = yaml.load(inputStream);
            listaPeces = new LinkedList<>(config.getListaPescado());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assertNotNull(config);
    }

    @Test
    public void hayLotes() {
        assertNotNull(config.getListaPescado());
    }

    @Test
    public void crearObjeto() {
        seller = new Seller(config.getCif(), config.getNombre());
        assertNotNull(seller);
    }


    @Test
    public void parsearListPeces() {
        LinkedList<Fish> lista = new LinkedList<>();
        for (FishConfig fishConfig : listaPeces) {
            Fish fish = new Fish(fishConfig.getNombre(), fishConfig.getTipoProducto(), fishConfig.getPeso(), fishConfig.getPrecioReserva());
            System.out.println(fish.getNombre());
            System.out.println(fish.getTipoProducto());
            lista.add(fish);
        }
        assertEquals(lista.size(), 2);
    }

    @Test
    public void mostrarLotes() {
        crearObjeto();
        for (Fish f : seller.getListaPescado()) {
            System.out.println("PESCADO");
            System.out.println(f.getNombre());
            System.out.println(f.getTipoProducto().getName());
        }
    }


}