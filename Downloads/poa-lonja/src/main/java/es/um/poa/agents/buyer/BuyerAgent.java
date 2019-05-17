package es.um.poa.agents.buyer;

import es.um.poa.Objetos.Buyer;
import es.um.poa.agents.TimePOAAgent;
import es.um.poa.agents.buyer.behaviours.RegistroComprador;
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
