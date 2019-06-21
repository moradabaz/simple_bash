package es.um.poa.agents.seller;


import es.um.poa.Objetos.Seller;
import es.um.poa.Objetos.SellerBuyerDB;
import es.um.poa.agents.TimePOAAgent;
import es.um.poa.agents.seller.behaviours.DepositoPescado;
import es.um.poa.agents.seller.behaviours.RegistroVendedor;
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

/**
 * El Agente SellerAgent representa el Agente comprador que se encarga de ejecutar los comportamientos de :
 * - Registro en la Lonja
 * - Deposito de articulos
 * - Retirada del dinero ganado
 * Contiene una lista de FishConfig para extraer
 * los atributos de cada lote
 */
public class SellerAgent extends TimePOAAgent {


	private String cif;
	private List<FishConfig> pescados;
	private SellerBuyerDB database = SellerBuyerDB.getInstance();
	/**
	 * La funcion setup es la que abarca los comportamientos.
	 */
	public void setup() {
		super.setup();
		
		Object[] args = getArguments();
		if (args != null && args.length == 1) {
			String configFile = (String) args[0];
			SellerAgentConfig config = initAgentFromConfigFile(configFile);
			
			if(config != null) {


				SequentialBehaviour seq = new SequentialBehaviour();
				ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
				request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
				request.addReceiver(new AID("Lonja", AID.ISLOCALNAME));
				request.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
				request.setConversationId("seller-register");

				Seller seller = null;

				try {
					this.cif = config.getCif();
					this.pescados = config.getListaPescado();
					seller = new Seller(config.getCif(), config.getNombre());
					request.setContentObject((Serializable) seller);
				} catch (IOException e) {

				}

				seq.addSubBehaviour(new RegistroVendedor(this, request));

				ACLMessage requestFish = new ACLMessage(ACLMessage.REQUEST);                    // Crea una solicitud para el deposito de lotes
				requestFish.setProtocol(jade.domain.FIPANames.InteractionProtocol.FIPA_REQUEST);            //
				requestFish.setConversationId("deposito-fish");                                    //
				requestFish.addReceiver(new AID("Lonja", AID.ISLOCALNAME));                                                //
				requestFish.setReplyByDate(new Date(System.currentTimeMillis() + 10000));        //


				try {
					LinkedList<Fish> listaFish = parseFish(pescados);                            // Sacamos la lista de lotes de las configuraciones
					seller.setListaPescado(listaFish);                                            // insertamos los lotes en la lista de lotes del vendedr
					database.anadirLotes(listaFish);
					requestFish.setContentObject((Serializable) seller);                        //
				} catch (IOException e) {

				}

				seq.addSubBehaviour(new DepositoPescado(this, requestFish));                        // Añadimos el comporamiento de deposito de pescado

				addBehaviour(seq);

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
			config = (SellerAgentConfig) yaml.load(inputStream);
			getLogger().info("initAgentFromConfigFile", config.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return config;
	}


}
