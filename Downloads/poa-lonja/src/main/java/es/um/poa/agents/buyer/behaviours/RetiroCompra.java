package es.um.poa.agents.buyer.behaviours;

import es.um.poa.Objetos.ListaMovimientos;
import es.um.poa.Objetos.Movimiento;
import es.um.poa.Objetos.SellerBuyerDB;
import es.um.poa.agents.TimePOAAgent;
import es.um.poa.agents.buyer.BuyerAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.Date;


/**
 *  La clase retiro compra representa el comportamiento que ejecuta un comprador cuando quiere retirar un producto
 *  adquirdo
 */
public class RetiroCompra extends Behaviour {

    private boolean done;
    private Agent agente;
    private int step;
    private SellerBuyerDB database = SellerBuyerDB.getInstance();

    public RetiroCompra(Agent a) {
        this.agente = a;
        this.step = 0;
        this.done = false;

    }

    public void handleInform(ACLMessage inform) {
        ListaMovimientos lista = database.getListaMovimientosBuyer(((BuyerAgent) agente).getLocalName());
        if (lista != null) {
            for (Movimiento m : lista.getMovimientos()) {
                System.out.println(m.toString());
            }
         }
    }

    public void handleRefuse(ACLMessage refuse) {
        System.out.println("  (Retirada Compra) El mensaje enviado ha sido por rechazado por el agente " + refuse.getSender().getLocalName());
    }

    public void handleFailure(ACLMessage failure) {
        if (agente.getAMS().equals(failure.getSender())) {
            System.out.println(" (Retirada Compra): Ha habido un fallo en el envío");
            System.out.println(agente.getAMS());
            System.out.println(failure.getSender());
        } else {
            System.out.println("(Retirada Compra): El agente receptor NO existe");
        }

    }

    public Agent getAgente() {
        return agente;
    }

    public void setAgente(Agent agente) {
        this.agente = agente;
    }

    @Override
    public void action() {
        if (((BuyerAgent) agente).getSimTime() != null) {
            if (((BuyerAgent) agente).getFaseActual() == TimePOAAgent.FASE_RETIRADA_COMPRADOR) {
                switch (step) {
                    case 0:
                        ACLMessage request = null;
                        try {
                            request = createRequest();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (request != null) {
                            agente.send(request);
                            step++;
                        }
                        break;
                    case 1:
                        ACLMessage response = ((BuyerAgent) agente).receive();
                        if (response != null) {
                            switch (response.getPerformative()) {
                                case ACLMessage.ACCEPT_PROPOSAL:
                                    handleInform(response);
                                    done = true;
                                    break;
                                case ACLMessage.FAILURE:
                                    handleFailure(response);
                                    break;
                                case ACLMessage.REJECT_PROPOSAL:
                                    handleRefuse(response);
                                    break;
                                default:
                                    break;
                            }
                        }
                        break;
                }
            }
        }
    }

    /**
     * Crea una solicitud de mensaje para proponer una retirada de lotes.
     * @return
     * @throws IOException
     */
    public ACLMessage createRequest() throws IOException {
        ACLMessage request = new ACLMessage(ACLMessage.PROPOSE);
        request.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
        request.addReceiver(new AID("Lonja", AID.ISLOCALNAME));
        request.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
        request.setConversationId("retiro-compra");
        request.setContentObject(((BuyerAgent) agente).getAdjudicaciones());
        return request;
    }

    @Override
    public boolean done() {
        return done;
    }
}








