package es.um.poa.agents.buyer;

import es.um.poa.Objetos.Buyer;
import es.um.poa.agents.TimePOAAgent;
import es.um.poa.agents.buyer.behaviours.InicioCredito;
import es.um.poa.agents.buyer.behaviours.RegistroComprador;
import es.um.poa.agents.buyer.behaviours.RetiroCompra;
import es.um.poa.productos.Fish;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Date;
import java.util.LinkedList;

public class BuyerAgent extends TimePOAAgent {

	private LinkedList<Fish> listaFavoritos = new LinkedList<Fish>();
	private LinkedList<Fish> productosAdquiridos = new LinkedList<>();
	private boolean productoAdquirido = true;
	private boolean peticionRegistroEnviada=false;
	private boolean peticionInicioCreditoEnviada=false;



	public void setup() {

		super.setup();


		Object[] args = getArguments();
		if (args != null && args.length == 1) {
			String configFile = (String) args[0];
			BuyerAgentConfig config = initAgentFromConfigFile(configFile);

			if(config != null) {
				// Aqui tiene que registrarse
				ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
				request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
				request.addReceiver(new AID( "Lonja", AID.ISLOCALNAME));
				request.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
				request.setConversationId("buyer-register");

				try {
					 Buyer buyer = new Buyer(config.getCif(), config.getNombre(), config.getBudget());
					 request.setContentObject((Serializable) buyer);
				} catch (IOException e) {
					e.printStackTrace();
				}

				//this.send(request);
				addBehaviour(new RegistroComprador(this, request));

			} else {
				doDelete();
			}


			/*
			 A trav�s del protocolo Fipa-Request vamos a manejar la situaci�n en la cual el
			  comprador va a a�adir cr�dito a su cuenta. Dicho comprador manda un mensaje con
			  su cif,nombre(los dos?) y el cr�dito a a�adir al agente Lonja, que a�adira el
			  cr�dito a dicho comprador y le mandar� un mensaje Agree.

			 * */

			if(config != null & !peticionInicioCreditoEnviada) {

				peticionInicioCreditoEnviada = true;
				//******************* A�ADIR CREDITO A SU CUENTA*******************************//
				ACLMessage requestAddCredit = new ACLMessage(ACLMessage.REQUEST);
				requestAddCredit.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
				requestAddCredit.addReceiver(new AID( "Lonja", AID.ISLOCALNAME));
				requestAddCredit.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
				requestAddCredit.setConversationId("buyer-addCredit");


				try {
					Buyer buyer = new Buyer(config.getCif(), config.getNombre(), config.getBudget());
					requestAddCredit.setContentObject(buyer);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				addBehaviour(new InicioCredito(this, requestAddCredit));

			}else if (config==null){
				doDelete();
			}


			///*******************************************************
			///PROTOCOLO RETIRADA DE LO COMPRADO
			//**********************************************************


			//		PONERLE INICIO DE LA PETICION

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
			config = yaml.load(inputStream);
			getLogger().info("initAgentFromConfigFile", config.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return config;
	}



}
