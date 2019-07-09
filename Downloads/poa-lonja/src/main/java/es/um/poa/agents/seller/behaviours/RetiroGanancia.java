package es.um.poa.agents.seller.behaviours;

import es.um.poa.Objetos.ListaMovimientos;
import es.um.poa.Objetos.Movimiento;
import es.um.poa.Objetos.SellerBuyerDB;
import es.um.poa.agents.TimePOAAgent;
import es.um.poa.agents.seller.SellerAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.Date;

public class RetiroGanancia extends Behaviour {

    private boolean done;
    private Agent agente;
    private int step;
    private SellerBuyerDB database = SellerBuyerDB.getInstance();

    public RetiroGanancia(Agent a) {
        this.agente = a;
        this.step = 0;
        this.done = false;

    }

    public void handleInform(ACLMessage inform) {
        System.out.println(" El vendedor recoje sus ganacias");
        ListaMovimientos lista = database.getListaMovimientoSeller(((SellerAgent) agente).getLocalName());
        if (lista != null) {
            for (Movimiento m : lista.getMovimientos()) {
                System.out.println(m.toString());
            }
        }
    }

    public void handleRefuse(ACLMessage refuse) {
        System.out.println(" El vendedor " + refuse.getSender().getLocalName() + " No ha vendido NADA");
    }

    public void handleFailure(ACLMessage failure) {
        if (agente.getAMS().equals(failure.getSender())) {
            System.out.println(" X: Ha habido un fallo en el envío");
            System.out.println(agente.getAMS());
            System.out.println(failure.getSender());
        } else {
            System.out.println(" X: El agente receptor NO existe");
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
        if (((SellerAgent) agente).getSimTime() != null) {
            if (((SellerAgent) agente).getFaseActual() == TimePOAAgent.FASE_RETIRADA_VENDEDOR) {
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
                        ACLMessage response = ((SellerAgent) agente).receive();
                        if (response != null) {
                            switch (response.getPerformative()) {
                                case ACLMessage.AGREE:
                                    handleInform(response);
                                   // done = true;
                                    break;
                                case ACLMessage.FAILURE:
                                    handleFailure(response);
                                    break;
                                case ACLMessage.REFUSE:
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

    public ACLMessage createRequest() throws IOException {
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        request.addReceiver(new AID("Lonja", AID.ISLOCALNAME));
        request.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
        request.setConversationId("retiro-ganancia");
        return request;
    }

    @Override
    public boolean done() {
        return done;
    }
}
