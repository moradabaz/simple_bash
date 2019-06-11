package es.um.poa.agents.buyer;

import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * La clase BuyerRegister representa el comportamiento que ejecuta un comprador cuando se quiere
 * registrar en la lonja enviandole una peticion de registro.
 *
 */
public class BuyerRegister extends AchieveREInitiator {

    private Agent buyerAgent;
    private ACLMessage msg;

    public BuyerRegister(Agent a, ACLMessage msg) {
        super(a, msg);
        this.buyerAgent = a;
        this.msg = msg;
    }

    public BuyerRegister(Agent a, ACLMessage msg, DataStore store) {
        super(a, msg, store);
    }

    /**
     * Handler de Informacion
     * @param information
     */
    public void handdleInformation(ACLMessage information) {
        System.out.println();
    }

    /**
     * Manejador de mensaje de rechazo o denegacion
     * @param refuse
     */
    public void handleRefuse(ACLMessage refuse) {
        System.out.println("Agent " + refuse.getSender().getLocalName() + " refused to perform the requested action: "
                + buyerAgent.getLocalName());
    }

    /**
     * Manejador de mensaje de fallo
     * @param failure
     */
    public void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            System.out.println("Responder does not exist");
        } else {
            System.out.println("Agent " + failure.getSender().getLocalName()
                    + " failed to perform the requested action: " + buyerAgent.getLocalName());
        }
    }

    public Agent getBuyerAgent() {
        return buyerAgent;
    }

    public void setBuyerAgent(Agent buyerAgent) {
        this.buyerAgent = buyerAgent;
    }

    public ACLMessage getMsg() {
        return msg;
    }

    public void setMsg(ACLMessage msg) {
        this.msg = msg;
    }
}
