package es.um.poa.agents.seller;

import es.um.poa.Objetos.Seller;
import es.um.poa.agents.TimePOAAgent;
import es.um.poa.agents.seller.protocolos.DepositoPescado;
import es.um.poa.agents.seller.protocolos.RegistroVendedor;
import es.um.poa.productos.Fish;
import es.um.poa.productos.FishConfig;
import jade.core.AID;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class SellerAgent extends TimePOAAgent {


	private String cif;
	private List<FishConfig> pescados;
		
	public void setup() {
		super.setup();
		
		Object[] args = getArguments();
		if (args != null && args.length == 1) {
			String configFile = (String) args[0];
			SellerAgentConfig config = initAgentFromConfigFile(configFile);
			
			if(config != null) {

				// REGISTRAMOS EL VENDEDOR XD
				ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
				request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
				request.addReceiver(new AID("Lonja", AID.ISLOCALNAME));
				request.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
				request.setConversationId("seller-register");

				try {
					this.cif = config.getCif();
					this.pescados = config.getListaPescado();
					Seller seller = new Seller(config.getCif(), config.getNombre());
					request.setContentObject((Serializable) seller);
				} catch (IOException e) {

				}

				SequentialBehaviour sequentialBehaviour = new SequentialBehaviour();
				sequentialBehaviour.addSubBehaviour(new RegistroVendedor(this, request));


				// AQUI PRESENTO MIS PECES XD

				ACLMessage requestFish = new ACLMessage(ACLMessage.REQUEST);
				requestFish.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
				requestFish.addReceiver(new AID("Lonja", AID.ISLOCALNAME));
				requestFish.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
				requestFish.setConversationId("seller-fish");

				try {
					LinkedList<Fish> listaFish = parseFish(pescados);
					requestFish.setContentObject((Serializable) listaFish);
				} catch (IOException e ) {

				}

				sequentialBehaviour.addSubBehaviour(new DepositoPescado(this, requestFish));

				addBehaviour(sequentialBehaviour);

			} else {
				doDelete();
			}
		} else {
			getLogger().info("ERROR", "Requiere fichero de cofiguración.");
			doDelete();
		}
	}

	public String getCif() {
		return cif;
	}

	public LinkedList<Fish> parseFish(List<FishConfig> fishConfig) {
		LinkedList<Fish> lista = new LinkedList<>();
		for (FishConfig fConfig : fishConfig) {
			Fish fish = new Fish(fConfig.getNombre(), fConfig.getTipoProducto(), fConfig.getPeso(), fConfig.getPrecioReserva());
			lista.add(fish);
		}
		return lista;
	}

	
	private SellerAgentConfig initAgentFromConfigFile(String fileName) {
		SellerAgentConfig config = null;
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
