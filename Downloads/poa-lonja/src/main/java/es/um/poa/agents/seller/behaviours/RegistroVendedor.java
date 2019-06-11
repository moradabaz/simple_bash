package es.um.poa.agents.seller.behaviours;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;


/**
 * La clase RegistroVendedor representa el comportamiento ejecutado por el vendedor para enviar
 * una solicitud de registro.
 */
public class RegistroVendedor extends AchieveREInitiator {

    private Agent agente;
    private ACLMessage mensaje;

    public RegistroVendedor(Agent a, ACLMessage msg) {
        super(a, msg);
        this.agente = a;
        this.mensaje = msg;
    }

    public void handleInform(ACLMessage inform) {
        System.out.println("El agente vendedor" + agente.getLocalName() + " se ha registrado correctamente");
    }

    public void handleRefuse(ACLMessage refuse) {
        System.out.println("El agente " + refuse.getSender().getLocalName() + " ha rechazado la solicitud del agente vendedor"
                + agente.getLocalName());
    }

    public void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            System.out.println("Responder does not exist");
        } else {
            System.out.println("Agent " + failure.getSender().getLocalName()
                    + " failed to perform the requested action: " + agente.getLocalName());
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
