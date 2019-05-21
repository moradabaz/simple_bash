package es.um.poa.agents.buyer.behaviours;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

public class InicioCredito extends AchieveREInitiator {

    private Agent agente;
    private ACLMessage mensaje;

    public InicioCredito(Agent a, ACLMessage msg) {
        super(a, msg);
        this.agente = a;
        this.mensaje = msg;
    }

    public void handleInform(ACLMessage inform) {
        System.out.println("El agente comprador" + agente.getLocalName() + " ha depositado su credito");
    }

    public void handleRefuse(ACLMessage refuse) {
        System.out.println("El agente " + refuse.getSender().getLocalName() + " ha rechazado el deposito de credito agente "
                + agente.getLocalName());
    }

    public void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            System.out.println("No hay ningun agente de respuesta");
        } else {
            System.out.println("Agent " + failure.getSender().getLocalName() + " failed to perform the requested action: " + agente.getLocalName());
        }
    }

    public Agent getAgente() {
        return agente;
    }

    public void setAgente(Agent agente) {
        this.agente = agente;
    }

    public ACLMessage getMsg() {
        return mensaje;
    }

    public void setMsg(ACLMessage msg) {
        this.mensaje = msg;
    }

}
