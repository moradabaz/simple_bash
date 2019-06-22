package es.um.poa.agents.buyer;

import es.um.poa.Objetos.Buyer;
import es.um.poa.agents.TimePOAAgent;
import es.um.poa.agents.buyer.behaviours.InicioCredito;
import es.um.poa.agents.buyer.behaviours.PujarLote;
import es.um.poa.agents.buyer.behaviours.RegistroComprador;
import es.um.poa.agents.buyer.behaviours.RetiroCompra;
import es.um.poa.productos.Fish;
import jade.core.AID;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;


/**
 * El Agente BuyerAgente representa al agente comprador y sus comportamientos
 * Tiene un atributo booleano ppara saber si ha declarado su saldo en la lonja
 */
public class BuyerAgent extends TimePOAAgent {

	private boolean peticionInicioCreditoEnviada=false;
	private LinkedList<String> listaDeseos = new LinkedList<String>();
	private HashMap<Integer, Fish> articulosAdjudicados;
	private double saldo = 0;
	private double precioPropuesta = 0;

	SequentialBehaviour seq;

	public BuyerAgent() {
		articulosAdjudicados = new HashMap<>();
	}

	/**
	 * Dentro de la funcion, se incluyen los comportamientos que el agente
	 * comprador va a realizar
	 *
	 * Primeramente extrae los datos a partir del fichero de configuracion.
	 * Los comportamientos que implementa son:
	 *	- Registro del comprador (FIPA REQUEST)
	 *	- Declaracion o inicio de credito si ya se ha registrado (FIPA REQUEST)
	 *  - Puja por lote
	 *  -
	 */
	public void setup() {

		super.setup();

		Object[] args = getArguments();
		if (args != null && args.length == 1) {
			String configFile = (String) args[0];
			BuyerAgentConfig config = initAgentFromConfigFile(configFile);


			if(config != null) {
				// Aqui tiene que registrarse

				//if (getSimTime() != null) {
				seq = new SequentialBehaviour();
				ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
				request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
				request.addReceiver(new AID("Lonja", AID.ISLOCALNAME));
				request.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
				request.setConversationId("buyer-register");
				try {
					Buyer buyer = new Buyer(config.getCif(), config.getNombre(), config.getBudget(), config.getlistaDeseos());
					listaDeseos = new LinkedList<String>(buyer.getListaDeseos());                            /// NULLPOINTEREXCEPTCION
					setSaldo(buyer.getSaldo());
					request.setContentObject((Serializable) buyer);
				} catch (IOException e) {
					e.printStackTrace();
				}

				seq.addSubBehaviour(new RegistroComprador(this, request));

				ACLMessage requestAddCredit = new ACLMessage(ACLMessage.REQUEST);
				requestAddCredit.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
				requestAddCredit.addReceiver(new AID("Lonja", AID.ISLOCALNAME));
				requestAddCredit.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
				requestAddCredit.setConversationId("buyer-addCredit");

				try {
					Buyer buyer = new Buyer(config.getCif(), config.getNombre(), config.getBudget());
					requestAddCredit.setContentObject(buyer);
					this.setSaldo(config.getBudget());
				} catch (IOException e) {
					e.printStackTrace();
				}


				seq.addSubBehaviour(new InicioCredito(this, requestAddCredit));

				addBehaviour(seq);

				//addBehaviour(new Comportamiento(config));
				//this.send(request);

					/*
					 A trav�s del protocolo Fipa-Request vamos a manejar la situaci�n en la cual el
					  comprador va a a�adir cr�dito a su cuenta. Dicho comprador manda un mensaje con
					  su cif,nombre(los dos?) y el cr�dito a a�adir al agente Lonja, que a�adira el
					  cr�dito a dicho comprador y le mandar� un mensaje Agree.

					 * */


					MessageTemplate template = MessageTemplate.MatchConversationId("subasta");
					addBehaviour(new PujarLote(this, template));

					addBehaviour(new RetiroCompra(this));

				//}
			}else if (config==null){
				doDelete();
			}



			///*******************************************************
			///PROTOCOLO RETIRADA DE LO COMPRADO
			//**********************************************************
/**

 ACLMessage requestRetire = new ACLMessage(ACLMessage.PROPOSE);
 requestRetire.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
 requestRetire.addReceiver(new AID( "Lonja", AID.ISLOCALNAME));
 requestRetire.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
 requestRetire.setConversationId("buyer-Retire");


 try {
 Buyer buyer = new Buyer(config.getCif(), config.getNombre(), config.getBudget());
 requestRetire.setContentObject((Serializable) buyer);
 } catch (IOException e) {
 // TODO Auto-generated catch block
 e.printStackTrace();
 }

 addBehaviour(new RetiroCompra(this, requestRetire));
 **/


		} else {
			getLogger().info("ERROR", "Requiere fichero de cofiguración.");
			doDelete();
		}
	}

	private BuyerAgentConfig initAgentFromConfigFile(String fileName) {
		BuyerAgentConfig config = null;
		try {
			Yaml yaml = new Yaml();
			InputStream inputStream;
			inputStream = new FileInputStream(fileName);
			config = (BuyerAgentConfig) yaml.load(inputStream);
			getLogger().info("initAgentFromConfigFile", config.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return config;
	}


	public LinkedList<String> getListaDeseos() {
		return 	listaDeseos;
	}

	public boolean estaInterasadoEn(String nombre) {
		if (listaDeseos.isEmpty())	return false;
		return listaDeseos.contains(nombre);
	}

	public void setSaldo(double saldo) {
		this.saldo = saldo;
	}

	public void decremetnarSaldo(double saldo) {
		this.saldo -= saldo;
	}

	public void incrementarSaldo(double saldo) {
		this.saldo += saldo;
	}

	public double getSaldo() {
		return saldo;
	}

	public void setPrecioPropuesta(double precioPropuesta) {
		this.precioPropuesta = precioPropuesta;
	}

	public double getPrecioPropuesta() {
		return precioPropuesta;
	}

	public HashMap<Integer, Fish> getAdjudicaciones() {
		return articulosAdjudicados;
	}

	public void addArticuloAdjudicado(int tiempo, Fish fish) {
		this.articulosAdjudicados.put(tiempo, fish);
	}
	public void eliminarDeListaDeseos(String nombre) {
		listaDeseos.remove(nombre);
	}
}
