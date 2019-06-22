package es.um.poa.agents.fishmarket.behaviours;

import es.um.poa.Objetos.Buyer;
import es.um.poa.Objetos.SellerBuyerDB;
import es.um.poa.agents.TimePOAAgent;
import es.um.poa.agents.fishmarket.FishMarketAgent;
import es.um.poa.productos.Fish;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;


/**
 * La clase SubastaLote representa el comportamiento que ejecuta la lonja cuando va a
 * subastar un lote.
 */
public class SubastaLote extends Behaviour {

    private Agent agente;
    private int rechazosPuja = 0;
    private int rondas = 0;
    private Long periodo;
    private SellerBuyerDB database = SellerBuyerDB.getInstance();
    private LinkedList<Fish> lotesASubastar;
    private boolean done;

    public SubastaLote(Agent a, long period) {
        this.agente = a;
        this.periodo = period;
        this.lotesASubastar = database.getLotes();
        this.done = false;
    }


    public ACLMessage prepareRequest(LinkedList<Buyer> buyers, Fish fish) {


        double precioPescado = (fish.getPrecioPorKilo() * fish.getPeso()) * 1.12;
        fish.setPrecioSalida(precioPescado);
        fish.setPrecioFinal(precioPescado);
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        for (Buyer buyer : buyers) {
            msg.addReceiver(new AID(buyer.getCif(), AID.ISLOCALNAME));
        }
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));
        msg.setConversationId("subasta");

        try {
            msg.setContentObject((Serializable) fish);
        } catch (IOException e) {
            System.err.println(" ## FALLO ## ");

        }

        return msg;
    }

    /**
     * El proceso que ejecuta la lonja es sencillo:
     * - Recoge todos los lotes que tiene para subastar y los mete en una lista
     * Recoge el primero lote y establece un precio de salida dado su precio de reserva y peso mas una
     * comision.
     * Luego envia un mensaje a todos los compradores notificandoles la subasta del lote.
     */

    @Override
    public void action() {
        if (((FishMarketAgent) agente).getSimTime() != null) {
            if (((FishMarketAgent) agente).getFaseActual() == TimePOAAgent.FASE_SUBASTA) {
                Random random = new Random();
                /**
                 * Este comportamiento ejecuta la gestion de las posibles respuestas a la
                 * subasta.
                 * Si durante la subasta la lonja acepta recube una propuesta de puja por parte de varios compradores,
                 * eligirá el que haya pujado antes.
                 * El a ese comprador se le manda un mensaje de aceptacion y un mensaje de denegacion al resto.
                 *
                 * Si todos los compradores rechazan el lote, se baja el precio de este y se sigue una nueva ronda
                 * Si el precio rebajado es menor que el precio minimo fijado para venderse, se descarta el lote
                 */
                agente.addBehaviour(new TickerBehaviour(agente, periodo) {
                    @Override
                    protected void onTick() {

                        if (!lotesASubastar.isEmpty()) {

                            if (!((FishMarketAgent) agente).isSubastando()) {
                                ((FishMarketAgent) agente).setSubastando(true);
                                rondas = 1;
                                Fish fish = lotesASubastar.getFirst();
                                lotesASubastar.removeFirst();
                                LinkedList<Buyer> buyers = database.getAllBuyers();
                                ACLMessage mensajeSubasta = prepareRequest(buyers, fish);

                                System.out.println("[ LOTE " + fish.toString() + " ]");
                                System.out.println("[ PRECIO DE SALIDA: " + fish.getPrecioSalida() + " ]");

                                System.out.print("[ DIA DE SUBASTA:");
                                System.out.println("  " + ((FishMarketAgent) agente).getSimTime().getDay() + " ]");

                                System.out.print("[ HORA DE SUBASTA:");
                                System.out.println("  " + ((FishMarketAgent) agente).getSimTime().getTime() + " ]");
                                agente.addBehaviour(new Subasta(agente, mensajeSubasta, fish, buyers.size()));

                            } else {
                                //done = true;
                            }

                        } else {
                            done = true;
                        }
                    }
                });
            }
        }
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    @Override
    public boolean done() {
        if (done)
            System.out.println("[FINALIZACION][CIERRE DE LA SUBASTA]");
        return done;
    }
}