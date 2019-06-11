package es.um.poa.agents.fishmarket.behaviours;

import es.um.poa.Objetos.Buyer;
import es.um.poa.Objetos.SellerBuyerDB;
import es.um.poa.agents.fishmarket.FishMarketAgent;
import es.um.poa.productos.Fish;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;


/**
 * La clase SubastaLote representa el comportamiento que ejecuta la lonja cuando va a
 * subastar un lote.
 */
public class SubastaLote extends TickerBehaviour {

    private Agent agente;
    private int rechazosPuja = 0;
    private int rondas = 0;
    private Long periodo;
    private boolean enEjecucion;
    private SellerBuyerDB database = SellerBuyerDB.getInstance();

    public SubastaLote(Agent a, long period) {
        super(a, period);
        this.agente = a;
        this.periodo = period;
        this.enEjecucion = false;
    }

    /**
     * El proceso que ejecuta la lonja es sencillo:
     * - Recoge todos los lotes que tiene para subastar y los mete en una lista
     * Recoge el primero lote y establece un precio de salida dado su precio de reserva y peso mas una
     * comision.
     * Luego envia un mensaje a todos los compradores notificandoles la subasta del lote.
     */
    @Override
    protected void onTick() {

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

            // Aqui mando un mensaje a todos los agentes de que voy a subastar un producto
            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
            msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));
            try {
                msg.setContentObject((Serializable) fish);
                ((FishMarketAgent) agente).setSubastando(true);
            } catch (IOException e) {
                System.err.println(" ## FALLO ## ");
                ((FishMarketAgent) agente).setSubastando(false);

            }


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
            agente.addBehaviour(new TickerBehaviour(agente, 80) {
                @Override
                protected void onTick() {

                    System.out.println("LOTE " + fish.toString());
                    System.out.println("PRECIO DE SALIDA: " + fish.getPrecioSalida());

                    System.out.println("DIA DE SUBASTA:");
                    System.out.println("    " + ((FishMarketAgent) agente).getSimTime().getDay());

                    System.out.println("HORA DE SUBASTA:");
                    System.out.println("    " + ((FishMarketAgent) agente).getSimTime().getTime());
                    agente.addBehaviour(new ContractNetInitiator(agente, msg) {

                        protected void handleAllResponses(Vector responses, Vector acceptances) {
                            if (((FishMarketAgent) agente).isSubastando()) {
                                ////    ATENCION -> NOS FALTA PONER LA MARCA DE TIEMPO

                                LinkedList<AID> candidados = new LinkedList<>();
                                double cantidadPujada = 0;
                                AID pujadorCandidato = null;
                                ACLMessage respuesta = new ACLMessage();
                                Enumeration e = responses.elements();               // Lista de respuestas
                                long ultimoTiempo = 0;
                                while(e.hasMoreElements()) {                        // Mientras hayan mas elementos
                                    ACLMessage msg = (ACLMessage) e.nextElement();
                                    long marcaTiempo = ((FishMarketAgent) agente).getSimTime().getTime();   //

                                    if (msg.getPerformative() == ACLMessage.PROPOSE) {                      // Si un elemente puja
                                        //reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                                        //
                                        //  FALTA PONER EL CRITERIO DE ELECCION
                                        //
                                        respuesta.setPerformative(ACLMessage.ACCEPT_PROPOSAL);              // Se acepta la puja
                                        acceptances.addElement(respuesta);                                  // Se anyade ese elemento a las respuestas
                                        double dineroPujado = Double.parseDouble(msg.getContent());         // Se extrae el dinero pujado
                                        cantidadPujada = dineroPujado;
                                        pujadorCandidato = msg.getSender();                                 // Se toma el candidato pujador
                                        if (((FishMarketAgent) agente).isSubastando()) {                    // Si se esta subastando
                                            ((FishMarketAgent) agente).setSubastando(false);                //      Se cierra la puja
                                            System.out.println("Se acepta la puja por " + cantidadPujada
                                                    + " del comprador " + pujadorCandidato.getLocalName());

                                            database.registrarVenta(pujadorCandidato.getLocalName(), fish, cantidadPujada);         //  Se registra la venta del lote

                                            try {
                                                respuesta.setContentObject((double) cantidadPujada);
                                            } catch (IOException e1) {
                                                e1.printStackTrace();
                                            }
                                        } else {
                                            respuesta.setPerformative(ACLMessage.REJECT_PROPOSAL);
                                        }

                                    } else if (msg.getPerformative() == ACLMessage.REFUSE) {
                                        rechazosPuja++;
                                        if (rechazosPuja == responses.size()) {                             // Si todos los compradores rechazan la puja
                                            rechazosPuja = 0;
                                            double precio = precioPescado - precioPescado * 0.2;            // Se reduce el precio
                                            if (precio <= precioPescado * 0.15) {                           // Si su precio actual es menor que el 15% del precio inicial
                                                ((FishMarketAgent) agente).setSubastando(false);            // Se descarta
                                                if (((FishMarketAgent)agente).getLotesASubastar().size() > 0) {     //  Se descarta el lote
                                                    ((FishMarketAgent)agente).removeFirstLote();
                                                    System.out.println("Numero de lotes en espera: " + ((FishMarketAgent)agente).getLotesASubastar().size());
                                                    System.out.println("El lote " + fish.getNombre() + " ha sido eliminado de la subasta");
                                                }
                                            } else {                                                        // Se actualiza la ronda
                                                rondas++;
                                                System.out.println("RONDA -> " + rondas);
                                                System.out.println("HORA DE SUBASTA:");
                                                System.out.println("    " + ((FishMarketAgent) agente).getSimTime().getTime());
                                                System.out.println("PRECIO ACTUAL: " + precio);
                                            }
                                        }
                                    }
                                }

                            }
                        }

                    });
                }
            });
        }
    }


}
