package es.um.poa.tests;


import es.um.poa.Objetos.Buyer;
import es.um.poa.Objetos.Seller;
import es.um.poa.Objetos.SellerBuyerDB;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SellerBuyerDBTest {


    Seller seller1;
    Seller seller2;
    Seller seller3;
    Buyer buyer1;

    SellerBuyerDB database;

    @Before
    public void inicializarComponentes() {
        System.out.println("##COMENCEMOS##");
        database = SellerBuyerDB.getInstance();
        seller1 = new Seller(randomAlphaNumeric(12), "Pepe Andres");
        seller2 = new Seller(randomAlphaNumeric(12), "Carlos Tomas");
        seller3 = new Seller(randomAlphaNumeric(12), "Pedro Angel");
        buyer1 = new Buyer(randomAlphaNumeric(12), "Jose miguel", 1300);
        database.registrarSeller(seller1);
        database.registrarSeller(seller2);
        database.registrarSeller(seller3);
        database.registrarBuyer(buyer1);
    }


    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    @Test
    public void checkSeller1() {
        System.out.println(database.checkBuyerByID(seller1.getIdentificador()));
    }


    @Test
    public void modificarSeller1() {

    }


    @Test
    public void eliminarSeller1() {
        database.removeSellerByID(seller1.getIdentificador());
        assertNull(database.getSeller(seller1.getIdentificador()));
    }


    @Test
    public void eliminarSeller1alt() {
        database.removeSeller(seller1);
        assertNull(database.getSeller(seller1.getIdentificador()));
    }


    @Test
    public void eliminarBuyer1() {
        database.removerBuyerByID(buyer1.getCif());
        assertNull(database.getBuyer(buyer1.getCif()));
    }


    @Test
    public void mostrarSellers() {
        for (String id : database.getSellerIDs()) {
            System.out.println(id);
        }
    }


    @Test
    public void getSeller1() {
        Seller seller = database.getSeller(seller1.getIdentificador());
        assertEquals(seller.getIdentificador(), seller1.getIdentificador());
        System.out.println("OK");
    }



    @Test
    public void probar_generador() {
        String cadena1 = randomAlphaNumeric(12);
        System.out.println(cadena1);
        String cadena2 = randomAlphaNumeric(12);
        System.out.println(cadena2);
        assertNotEquals(cadena1, cadena2);
    }
}