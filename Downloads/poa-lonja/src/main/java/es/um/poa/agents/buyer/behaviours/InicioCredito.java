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

    /**
     * Manejador de un mensaje con una perfomativa REFUSE
     * @param refuse
     */
    public void handleRefuse(ACLMessage refuse) {
        System.out.println("El agente " + refuse.getSender().getLocalName() + " ha rechazado el deposito de credito agente "
                + agente.getLocalName());
    }

    /**
     * Manejador de un mensaje con una perfomativa FAILURE
     * @param failure
     */
    public void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            System.out.println("No se ha podido declarar el credito");
        } else {
            System.out.println("Agent " + failure.getSender().getLocalName() + " failed to perform the requested action: " + agente.getLocalName());
        }
    }

    /**
     *
     * @return Devuelve el agente del comportamiento
     */
    public Agent getAgente() {
        return agente;
    }

    /**
     * Estable el agente del comportamiento
     * @param agente
     */
    public void setAgente(Agent agente) {
        this.agente = agente;
    }

    /**
     *
     * @return Devuelve el mensaje del comportamiento
     */
    public ACLMessage getMsg() {
        return mensaje;
    }

    /**
     * Establece el mensaje del comportamiento
     * @param msg
     */
    public void setMsg(ACLMessage msg) {
        this.mensaje = msg;
    }

    /**
     * Acción del comportamiento.
     * Envia el mensaje de solicitud. Se espera el mensaje de respuesta y luego se trata.
     */
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
                            handleAgree(reply);
                            break;
                        case ACLMessage.FAILURE:
                            handleFailure(reply);
                        case ACLMessage.REFUSE:
                            handleRefuse(reply);
                            break;
                    }
                    done = true;
                }
        }
    }

    /**
     * Manejador de un mensaje con una perfomativa AGREE
     * @param reply
     */
    private void handleAgree(ACLMessage reply) {
        System.out.println("El agente " + agente.getLocalName() + " ha depositado su credito con exito");
    }

    /**
     *
     * @return
     */
    @Override
    public boolean done() {
        return done;
    }
}
