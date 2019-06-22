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
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.util.leap.Iterator;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Subasta extends Behaviour {

    private static final int TIEMPO_SUBASTA = 6;
    private static final int TIEMPO_RONDA = 2;
    private final int numBuyers;
    private FishMarketAgent agente;
    private ACLMessage message;
    private int rechazosPuja = 0;
    private int rondas = 0;
    private Long periodo;
    private boolean enEjecucion;
    private int step;
    private boolean done;
    private Fish fish;
    private SellerBuyerDB database = SellerBuyerDB.getInstance();
    private HashMap<Integer, ACLMessage> candidatos;
    private MessageTemplate mt;
    private int tiempoInicial;
    private int tiempoInicialRonda;
    private int tiempoFinal;

    public Subasta(Agent a, ACLMessage message, Fish fish, int numBuyers) {
        this.agente = (FishMarketAgent) a;
        this.enEjecucion = false;
        this.step = 0;
        this.candidatos = new HashMap<>();
        this.message = message;
        this.mt = null;
        this.fish = fish;
        this.tiempoFinal = 0;
        this.tiempoInicial = 0;
        this.tiempoInicialRonda = 0;
        this.numBuyers = numBuyers;
        this.done = false;
    }

    @Override
    public void action() {
        if (((FishMarketAgent) agente).getFaseActual() == TimePOAAgent.FASE_SUBASTA) {
            switch (step) {
                case 0:
                    agente.send(message);
                    mt = MessageTemplate.MatchConversationId("subasta");
                    tiempoInicial = ((FishMarketAgent) agente).getSimTime().getTime();
                    tiempoInicialRonda = ((FishMarketAgent) agente).getSimTime().getTime();
                    step++;
                    break;
                case 1:
                    if (tiempoFinal - tiempoInicial <= TIEMPO_SUBASTA) {
                        if (tiempoFinal - tiempoInicialRonda <= TIEMPO_RONDA) {
                            double cantidadPujada = 0;
                            AID mejorCandidato = null;


                            ACLMessage propose = agente.receive(mt);
                            if (propose != null) {                                                 // TODO: Es NULO
                                if (propose.getPerformative() == ACLMessage.PROPOSE) {                      // TODO: Hay un NULLPOINTEREXCEPTION
                                    ACLMessage mensajeAdjudicacion = propose.createReply();
                                    mensajeAdjudicacion.setConversationId("subasta");
                                    mensajeAdjudicacion.setReplyByDate(new Date(System.currentTimeMillis() + 1000));

                                    Buyer buyer = database.getBuyer(propose.getSender().getLocalName());       // TODO: Es LONJA ???
                                    System.out.println(buyer.getCif() + " ha pujado");

                                    candidatos.put(((FishMarketAgent) agente).getSimTime().getTime(), propose);
                                    if (candidatos.size() > 0) {
                                        int primero = candidatos.keySet().stream().sorted(Integer::compareTo).collect(Collectors.toList()).get(0);
                                        ACLMessage response = candidatos.get(primero);
                                        mejorCandidato = response.getSender();

                                        try {
                                            cantidadPujada = Double.parseDouble((String) propose.getContentObject());
                                        } catch (UnreadableException e1) {
                                            e1.printStackTrace();
                                        }

                                        mensajeAdjudicacion.setPerformative(ACLMessage.ACCEPT_PROPOSAL);

                                       // ((FishMarketAgent) agente).setSubastando(false);
                                        System.out.println("[VENDIDO]: Se acepta la puja por " + cantidadPujada + " del comprador " + mejorCandidato.getLocalName());
                                        System.out.println("Lote: " + fish.getNombre().toUpperCase());
                                        fish.setPrecioFinal(cantidadPujada);
                                        database.registrarVenta(mejorCandidato.getLocalName(), fish, cantidadPujada);         //  Se registra la venta del lote

                                        try {
                                            mensajeAdjudicacion.setContentObject((Fish) fish);
                                            mensajeAdjudicacion.addReceiver(mejorCandidato);
                                        } catch (IOException e1) {
                                            e1.printStackTrace();
                                        }

                                        System.out.println("SO FAR, SO GOOD");
                                        agente.send(mensajeAdjudicacion);

                                        ACLMessage mensajeDenegacion = new ACLMessage();
                                        mensajeDenegacion.setPerformative(ACLMessage.REJECT_PROPOSAL);
                                        mensajeDenegacion.setConversationId("subasta");
                                        mensajeAdjudicacion.setReplyByDate(new Date(System.currentTimeMillis() + 1000));
                                        java.util.Iterator<ACLMessage> it = candidatos.values().iterator();
                                        while (it.hasNext()) {
                                            ACLMessage msg = it.next();
                                            if (!msg.equals(response)) {
                                                mensajeDenegacion.addReceiver(msg.getSender());
                                            }
                                        }
                                        agente.send(mensajeDenegacion);

                                        candidatos.clear();
                                        step = 3;
                                        done = true;
                                    }
                                } else {
                                    rechazosPuja++;
                                    if (rechazosPuja == numBuyers) {                                                    // Si todos los compradores rechazan la puja
                                        System.out.println("[NADIE QUIERE " + fish.getNombre().toUpperCase() + " A PRECIO DE " + fish.getPrecioFinal() +" ]");
                                        rechazosPuja = 0;
                                        double precio = fish.getPrecioFinal() - fish.getPrecioFinal() * 0.2;            // Se reduce el precio
                                        if (precio <= fish.getPrecioMinimo()) {                                         // Si su precio actual es menor que el 15% del precio inicial
                                            step = 3;
                                            done = true;
                                            System.out.println("[[EL LOTE " + ((FishMarketAgent) agente).getLotesASubastar().size() + " SE CIERRA POR BAJO PRECIO]]");
                                            System.out.println("EL LOTE " + fish.getNombre() + " ha sido eliminado de la subasta");
                                        } else {
                                            // Se actualiza la ronda
                                            rondas++;
                                            System.out.println("[EXPIRA EL TIEMPO DE RONDA]");
                                            System.out.println("[" + fish.getNombre().toUpperCase() + "]" + "CAMBIO DE RONDA -> " + rondas);
                                            System.out.print("HORA:");
                                            System.out.println("    " + ((FishMarketAgent) agente).getSimTime().getTime());
                                            System.out.println("PRECIO ACTUAL DE " + fish.getNombre().toUpperCase() + ": " + precio);
                                            fish.setPrecioFinal(precio);
                                            step = 2;
                                        }
                                    }
                                }
                            }

                        } else {
                            double precio = fish.getPrecioFinal() - fish.getPrecioFinal() * 0.2;            // Se reduce el precio
                            if (precio <= fish.getPrecioMinimo()) {                                         // Si su precio actual es menor que el 15% del precio inicial
                                step = 3;
                                done = true;
                                System.out.println("[[EL LOTE " + ((FishMarketAgent) agente).getLotesASubastar().size() + " SE CIERRA POR BAJO PRECIO]]");
                                System.out.println("EL LOTE " + fish.getNombre() + " ha sido eliminado de la subasta");

                            } else {

                                rondas++;        // Se actualiza la ronda
                                System.out.println("[" + fish.getNombre().toUpperCase() + "]" + "CAMBIO DE RONDA -> " + rondas);
                                System.out.print("HORA:");
                                System.out.println("    " + ((FishMarketAgent) agente).getSimTime().getTime());
                                System.out.println("PRECIO ACTUAL DE " + fish.getNombre().toUpperCase() + ": " + precio);
                                fish.setPrecioFinal(precio);
                                step = 2;
                            }
                        }
                        tiempoFinal = ((FishMarketAgent) agente).getSimTime().getTime();
                    } else {

                        System.out.println("[SE CIERRA LA SUBASTA ACTUAL POR TIEMPO]");
                        done = true;
                        step = 3;
                    }

                    break;
                case 2:
                    if (tiempoFinal - tiempoInicial <= TIEMPO_SUBASTA) {
                        ACLMessage mensaje = prepareRequest(fish);
                        agente.send(mensaje);
                        tiempoInicialRonda = ((FishMarketAgent) agente).getSimTime().getTime();
                        step = 1;
                    } else {
                        System.out.println("[SE CIERRA LA SUBASTA ACTUAL POR TIEMPO]");
                        done = true;
                        step = 3;
                    }
                    break;
            }
        }

    }

    @Override
    public boolean done() {
        if (done) {
            ((FishMarketAgent) agente).setSubastando(false);
        }
        return done;
    }


    public ACLMessage prepareRequest(Fish fish) {

        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        msg.setConversationId("subasta");
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));
        Iterator it = message.getAllReceiver();
        while (it.hasNext()) {
            AID aidReceiver = (AID) it.next();
            msg.addReceiver(aidReceiver);
        }
        msg.setConversationId("subasta");
        try {
            msg.setContentObject((Serializable) fish);
            ((FishMarketAgent) agente).setSubastando(true);
        } catch (IOException e) {
            System.err.println(" ## FALLO ## ");
            ((FishMarketAgent) agente).setSubastando(false);
        }

        return msg;
    }

}
