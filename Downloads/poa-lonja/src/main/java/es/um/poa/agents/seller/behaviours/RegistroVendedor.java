package es.um.poa.agents.seller.behaviours;

import es.um.poa.Objetos.Concepto;
import es.um.poa.Objetos.Movimiento;
import es.um.poa.Objetos.SellerBuyerDB;
import es.um.poa.agents.TimePOAAgent;
import es.um.poa.agents.seller.SellerAgent;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;


/**
 * La clase RegistroVendedor representa el comportamiento ejecutado por el vendedor para enviar
 * una solicitud de registro.
 */
public class RegistroVendedor extends Behaviour {

    private Agent agente;
    private ACLMessage mensaje;
    private int step;
    private boolean done;

    public RegistroVendedor(Agent a, ACLMessage msg) {
        this.agente = a;
        this.mensaje = msg;
        this.step = 0;
        this.done = false;
    }


    public void handleAgree(ACLMessage agree) {
        System.out.println("++ El agente " + ((SellerAgent)agente).getLocalName() + " HA SIDO REGISTRADO CORRECTAMENTE");
        String descripcion = "Se registra el vendedor " + ((SellerAgent) agente).getLocalName();
        Movimiento movimiento = new Movimiento(agente.getLocalName(), ((SellerAgent) agente).getSimTime().getTime(), Concepto.REGISTRO, descripcion);
        SellerBuyerDB.getInstance().registarMovimientoSeller(agente.getLocalName(), movimiento);
    }

    public void handleRefuse(ACLMessage refuse) {
        System.out.println("-- El agente " + refuse.getSender().getLocalName() + " ha rechazado la solicitud del agente vendedor"
                + agente.getLocalName());
    }

    public void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            System.out.println("-- Responder does not exist");
        } else {
            System.out.println("-- Agent " + failure.getSender().getLocalName()
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

    @Override
    public void action() {
        if (((SellerAgent) agente).getSimTime() != null) {
            if (((SellerAgent) agente).getFaseActual() == TimePOAAgent.FASE_REGISTRO) {
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
                                    done = true;
                                    break;
                                case ACLMessage.REFUSE:
                                    handleRefuse(reply);
                                    break;
                                case ACLMessage.FAILURE:
                                    handleFailure(reply);
                                    break;
                            }
                            done = true;
                        }
                        break;
                    default:
                        System.out.println("NO SE HA ENTENDIDO EL MENSAJE DE REGISTRO VENDEDOR ");
                        break;
                }
            }
        }

    }

    @Override
    public boolean done() {
        return done;
    }
}
