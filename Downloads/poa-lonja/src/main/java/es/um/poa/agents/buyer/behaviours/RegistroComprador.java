package es.um.poa.agents.buyer.behaviours;

import es.um.poa.agents.TimePOAAgent;
import es.um.poa.agents.buyer.BuyerAgent;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

/**
 * La clase BuyerRegister representa el comportamiento que ejecuta un comprador cuando se quiere
 * registrar en la lonja enviandole una peticion de registro.
 *
 */
public class RegistroComprador extends Behaviour {

    private Agent agente;
    private ACLMessage mensaje;
    private boolean done;
    private int step;

    public RegistroComprador(Agent a, ACLMessage msg) {
       // super(a,msg);
        this.agente = a;
        this.mensaje = msg;
        this.done = false;
        this.step = 0;
    }


    /**
     * Manejador de mensaje de informacion
     * @param inform
     */
    public void handleInform(ACLMessage inform) {
        System.out.println(" El mensaje " + inform.getConversationId() + " ha sido notificado correctamente");
        System.out.println("[TIEMPO_REGISTRO] -> " + ((BuyerAgent) agente).getSimTime().toString());
    }

    /**
     * Manejador de mensaje de denegacion o rechazo
     * @param refuse
     */
    public void handleRefuse(ACLMessage refuse) {
        System.out.println(" ! >> (Registro Comprador)El mensaje enviado ha sido por rechazado por el agente " + refuse.getSender().getLocalName());
    }

    /**
     * Manejador de mensaje de fallo
     * @param failure
     */
    public void handleFailure(ACLMessage failure) {
        if (agente.getAMS().equals(failure.getSender())) {
            System.out.println(" X: Ha habido un fallo en el envío");
            System.out.println(agente.getAMS());
            System.out.println(failure.getSender());
        } else {
            System.out.println(" X: El agente receptor NO existe");
        }

    }

    public void handleAgree(ACLMessage agree) {
        System.out.println("++ El agente " + ((BuyerAgent) agente).getLocalName() + " HA SIDO REGISTRADO CORRECTAMENTE");
    }

    public Agent getAgente() {
        return agente;
    }

    public void setAgente(Agent agente) {
        this.agente = agente;
    }



    public ACLMessage getMensaje() {
        return mensaje;
    }

    public void setMensaje(ACLMessage mensaje) {
        this.mensaje = mensaje;
    }

    /**
     * El comprador envia un mensaje de solicitud a la lonja y espera la respuesta
     */
    @Override
    public void action() {
        if (((BuyerAgent) agente).getSimTime() != null) {
            if (((BuyerAgent) agente).getFaseActual() == TimePOAAgent.FASE_REGISTRO) {
                switch (step) {
                    case 0:
                        agente.send(mensaje);
                        step = 1;
                        break;
                    case 1:
                        ACLMessage reply = agente.receive();
                        if (reply != null) {
                            switch (reply.getPerformative()) {
                                case ACLMessage.AGREE:
                                    handleAgree(reply);
                                    done = true;
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
        }
    }

    @Override
    public boolean done() {
        return done;
    }
}
