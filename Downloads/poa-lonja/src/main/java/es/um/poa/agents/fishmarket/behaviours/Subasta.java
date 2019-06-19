package es.um.poa.agents.fishmarket.behaviours;

import es.um.poa.Objetos.Buyer;
import es.um.poa.Objetos.SellerBuyerDB;
import es.um.poa.agents.TimePOAAgent;
import es.um.poa.agents.fishmarket.FishMarketAgent;
import es.um.poa.productos.Fish;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

public class Subasta extends Behaviour {

    private Agent agente;
    private int rechazosPuja = 0;
    private int rondas = 0;
    private Long periodo;
    private boolean enEjecucion;
    private int step;
    private SellerBuyerDB database = SellerBuyerDB.getInstance();

    public Subasta(Agent a) {
        this.agente = a;
        this.enEjecucion = false;
        this.step = 0;
    }

    @Override
    public void action() {
        if (((FishMarketAgent) agente).getSimTime() != null) {
            if (((FishMarketAgent) agente).getFaseActual() == TimePOAAgent.FASE_SUBASTA) {
                switch (step) {
                    case 0:
                        Random random = new Random();
                        LinkedList<Fish> lotesASubastar = ((FishMarketAgent) agente).getLotesASubastar();

                        if (!lotesASubastar.isEmpty()) {
                            System.out.println("### COMIENZA LA SUBASTA SURMANOS ######");
                            rondas = 1;
                            Fish fish = lotesASubastar.getFirst();
                            double precioPescado = (fish.getPrecioReserva() * fish.getPeso()) * 1.12;
                            fish.setPrecioSalida(precioPescado);

                            ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                            LinkedList<Buyer> buyers = database.getAllBuyers();
                            for (Buyer buyer : buyers) {
                                msg.addReceiver(new AID(buyer.getCif(), AID.ISLOCALNAME));
                            }
                            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                            msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));
                            msg.setConversationId("subasta");
                            try {
                                msg.setContentObject((Serializable) fish);
                                ((FishMarketAgent) agente).setSubastando(true);
                            } catch (IOException e) {
                                System.err.println(" ## FALLO ## ");
                                ((FishMarketAgent) agente).setSubastando(false);

                            }
                        }
                }
            }
        }

    }

    @Override
    public boolean done() {
        return false;
    }
}
