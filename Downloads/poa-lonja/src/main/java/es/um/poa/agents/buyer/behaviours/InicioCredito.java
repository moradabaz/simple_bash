package es.um.poa.agents.buyer.behaviours;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

/**
 * Comportamiento para el inicio del credito, es decir, la declaración del credito por
 * parte del comprador.
 */
public class InicioCredito extends Behaviour {

    private Agent agente;
    private ACLMessage mensaje;
    private int step;
    private boolean done;

    public InicioCredito(Agent a, ACLMessage msg) {
        this.agente = a;
        this.mensaje = msg;
        this.step = 0;
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
            System.out.println("No se ha podido declarar el credito");
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

    @Override
    public void action() {
        switch (step) {
            case 0:
                agente.send(mensaje);
                step++;
                break;
            case 1:
                ACLMessage reply = agente.receive();
                if (reply != null) {
                    switch (reply.getPerformative()) {
                        case ACLMessage.AGREE:
                            handleInform(reply);
                            break;
                        case ACLMessage.FAILURE:
                            handleFailure(reply);
                        case ACLMessage.REFUSE:
                            handleRefuse(reply);
                        default:
                            System.err.println("NO SE HA ENTENDIDO EL MENSAJE");
                    }
                    done = true;
                }
        }
    }

    @Override
    public boolean done() {
        return done;
    }
}
