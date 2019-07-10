package es.um.poa.agents.fishmarket.behaviours;

import es.um.poa.Objetos.Buyer;
import es.um.poa.Objetos.SellerBuyerDB;
import es.um.poa.agents.TimePOAAgent;
import es.um.poa.agents.fishmarket.FishMarketAgent;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


/**
 * La clase RegistroCompradorResp corresponde al comportamiento que ejecuta la lonja cuando
 * recibe una peticion de regitro por parte de un comprador
 *
 */
public class RegistroCompradorResp extends Behaviour {


    private Agent agente;
    private MessageTemplate mensaje;
    private SellerBuyerDB dataBase = SellerBuyerDB.getInstance();
    private int step;
    private boolean done;

    public RegistroCompradorResp(Agent a, MessageTemplate mt) {
        this.agente = a;
        this.mensaje = mt;
        this.step = 0;
        this.done = false;
    }

    /**
     * Este metodo redefinido de la clase AchieveREResponder reponde al mensaje de peticion.
     * El mensaje contiene el objeto Buyer que se quiere registrar.
     * Se recoge su CIF y su nombre, y se registra en la base de datos.
     * Crea una respuesta de aceptacion.
     *
     * @param request
     * @return
     * @throws NotUnderstoodException
     * @throws RefuseException
     */
    public ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {

        try {

            Buyer buyer = ((Buyer) request.getContentObject());
            String cif = buyer.getCif();
            String nombre = buyer.getNombre();
            if (!dataBase.checkBuyerByID(buyer.getCif())) {
                dataBase.registrarBuyer(buyer);

                System.out.println("Agente tipo " + buyer.getClass().getName() + " { " + "cif: " + cif + " | " + " nombre: " + nombre + " } ACEPTADO PARA REGISTRO");
                ACLMessage reply = request.createReply();
                reply.setPerformative(ACLMessage.AGREE);
                return reply;
            } else {
                ACLMessage refuseReply = request.createReply();
                refuseReply.setPerformative(ACLMessage.REFUSE);
                return refuseReply;
            }

        } catch (UnreadableException e) {
            e.printStackTrace();
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.FAILURE);
            return reply;
        }

    }

    public Agent getAgente() {
        return agente;
    }

    public void setAgente(Agent agente) {
        this.agente = agente;
    }

    public MessageTemplate getMsg() {
        return mensaje;
    }

    public void setMsg(MessageTemplate msg) {
        this.mensaje = msg;
    }

    @Override
    public void action() {
        if (((FishMarketAgent)agente).getSimTime() != null) {
            if (((FishMarketAgent) agente).getFaseActual() == TimePOAAgent.FASE_REGISTRO) {
                ACLMessage request = agente.receive(mensaje);
                if (request != null) {
                    if (request.getPerformative() == ACLMessage.REQUEST) {
                        ACLMessage response = null;
                        try {
                            response = prepareResponse(request);
                        } catch (NotUnderstoodException e) {
                            e.printStackTrace();
                        } catch (RefuseException e) {
                            e.printStackTrace();
                        }
                        if (response != null)
                            agente.send(response);
                        step++;
                    }
                }
            } else {
                done = false;
            }
        }
    }

    @Override
    public boolean done() {
        return done;
    }
}
