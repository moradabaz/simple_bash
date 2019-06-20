package es.um.poa.tests;


import es.um.poa.Objetos.Buyer;
import es.um.poa.agents.buyer.BuyerAgentConfig;
import es.um.poa.productos.FishDeseoConfig;
import org.junit.Before;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BuyerAgentConfigTest {

    BuyerAgentConfig config = null;
    Buyer buyer = null;
    List<FishDeseoConfig> fishDeseoConfigs;

    @Before
    public void inicializar() {
        try {
            Yaml yaml = new Yaml();
            InputStream inputStream;
            inputStream = new FileInputStream("/Users/morad/Downloads/poa-lonja/configs/buyerType1.yaml");
            config = yaml.load(inputStream);
           // fishDeseoConfigs = new LinkedList<>(config.getlistaDeseos());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assertNotNull(config);
    }

    @Test
    public void crearComprador() {
        buyer = new Buyer(config.getCif(), config.getNombre());
        assertNotNull(buyer);
    }

    @Test
    public void hayDeseo(){
        assertNotNull(config.getlistaDeseos());
    }

    @Test
    public void parsearListPeces() {
        buyer = new Buyer(config.getCif(), config.getNombre());
        LinkedList<String> lista = new LinkedList<>();
        for (FishDeseoConfig fishConfig : fishDeseoConfigs) {
            lista.add(fishConfig.getNombre());
        }
        buyer.setListaDeseos(lista);
        assertEquals(lista.size(), 3);
        for (String pez: buyer.getListaDeseos()) {
            System.out.println(pez);
        }
    }






}
