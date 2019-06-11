package es.um.poa.agents.fishmarket.behaviours;

import es.um.poa.Objetos.Buyer;
import es.um.poa.Objetos.SellerBuyerDB;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREResponder;


/**
 * La clase RegistroCompradorResp corresponde al comportamiento que ejecuta la lonja cuando
 * recibe una peticion de regitro por parte de un comprador
 *
 */
public class RegistroCompradorResp extends AchieveREResponder {


    private Agent agente;
    private MessageTemplate mensaje;
    private SellerBuyerDB dataBase = SellerBuyerDB.getInstance();

    public RegistroCompradorResp(Agent a, MessageTemplate mt) {
        super(a, mt);
        this.agente = a;
        this.mensaje = mt;
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
    @Override
    public ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {

        try {

            System.out.println(">>>>>>> Estamos preparando la respuesta " + request.getContentObject());
            Buyer buyer = ((Buyer) request.getContentObject());
            String cif = buyer.getCif();
            String nombre = buyer.getNombre();
            dataBase.registrarBuyer(buyer);

            System.out.println("Mensaje recibido √");
            System.out.println("Agente tipo " + buyer.getClass().getName() + " { " + "cif: " + cif + " | " + " nombre: " + nombre + " }");

            // RESPUESTA
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.AGREE);
            return reply;

        } catch (UnreadableException e) {
            e.printStackTrace();
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
            return reply;
        }

    }

    /**
     *
     * @param request
     * @param response
     * @return
     * @throws FailureException
     */
    @Override
    public ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {

        System.out.println(
                "Agente " + agente.getLocalName() + ": completado con exito: " + request.getSender().getLocalName());

        ACLMessage inform = request.createReply();
        inform.setPerformative(ACLMessage.INFORM);
        return inform;
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
}
