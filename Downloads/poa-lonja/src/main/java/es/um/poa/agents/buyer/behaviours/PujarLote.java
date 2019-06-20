package es.um.poa.agents.buyer.behaviours;

import es.um.poa.agents.TimePOAAgent;
import es.um.poa.agents.buyer.BuyerAgent;
import es.um.poa.productos.Fish;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.io.Serializable;

public class PujarLote extends Behaviour {
    private Agent agent;
    private MessageTemplate cfp;
    private boolean done;

    public PujarLote(Agent a, MessageTemplate cfp) {
        this.agent = a;
        this.done = false;
        this.cfp = cfp;
    }
    public ACLMessage prepareResponse(ACLMessage cfp) throws NotUnderstoodException, RefuseException {

        Fish objetoSubastado = null;
        try {
            objetoSubastado = (Fish) cfp.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }

        if (objetoSubastado != null) {
            if (((BuyerAgent) agent).estaInterasadoEn(objetoSubastado.getNombre())) {
                double saldo = ((BuyerAgent) agent).getSaldo();
                double precioPropuesta = calcularPrecioPuja(objetoSubastado);
                if (esFavorable(objetoSubastado, precioPropuesta, saldo)) {
                    System.out.println("[NOTIFY_BUYER] Lo aceptamos");
                    ACLMessage propose = cfp.createReply();
                    propose.setPerformative(ACLMessage.PROPOSE);
                    double precio = ((BuyerAgent) agent).getPrecioPropuesta();
                    try {
                        propose.setContentObject((Serializable) String.valueOf(precio));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return propose;
                } else {
                    System.out.println("[NOTIFY_BUYER] Lo rechazamos porque no es favorable");
                    ACLMessage refuse = cfp.createReply();
                    refuse.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    return refuse;
                }
            } else {
                System.out.println("[NOTIFY_BUYER] Lo rechazamos porque no estamos interesados");
                ACLMessage refuse = cfp.createReply();
                refuse.setPerformative(ACLMessage.REJECT_PROPOSAL);
                return refuse;
            }

        } else {
            ACLMessage failure = cfp.createReply();
            failure.setPerformative(ACLMessage.FAILURE);
            return failure;
        }
    }


    /**
     * Esta funcion calcula si una subasta es favorable para pujar o no.
     * Se compara el saldo actual con el precio que esta dispuesto a pagar, así como el
     * precio actual que tiene el lote
     * @param fishEnSubasta
     * @param propuesta
     * @param saldo
     * @return
     */
    public boolean esFavorable(Fish fishEnSubasta, double propuesta, double saldo) {
        return (fishEnSubasta.getPrecioFinal() <= propuesta && saldo > propuesta);

    }

    /**
     * Calcula el precio que esta dispuesto el comprador a pagar por un determinado lote.
     * El calculo se hace mediante una variable aleatoria.
     * @param fish  El lote que se esta subastando
     * @return  Devuelve el precio que esta dispuesto a pagar.
     */
    public double calcularPrecioPuja(Fish fish) {
        double precio = Math.floor(Math.random() * (fish.getPrecioMinimo() - fish.getPrecioFinal() + 1) + fish.getPrecioFinal());
        return precio;
    }

    @Override
    public void action() {
        ACLMessage subastaRequest = agent.receive(cfp);
        if (subastaRequest != null) {
            System.out.println("[NOTIFY_BUYER] Me ha llegado un mensaje");
            if (subastaRequest.getPerformative() == ACLMessage.CFP) {
                System.out.println("[NOTIFY_BUYER] Es CFP");
                if (((BuyerAgent) agent).getFaseActual() == TimePOAAgent.FASE_SUBASTA) {
                    System.out.println("[NOTIFY_BUYER] La fase actual es " + ((BuyerAgent) agent).getFaseActual());
                    ACLMessage propuesta = null;
                    try {
                        propuesta = prepareResponse(subastaRequest);
                        agent.send(propuesta);
                        done = false;
                    } catch (NotUnderstoodException e) {
                        e.printStackTrace();
                    } catch (RefuseException e) {
                        e.printStackTrace();
                    }

                } else {
                }
            }
        }
    }

    @Override
    public boolean done() {
        return done;
    }
}
