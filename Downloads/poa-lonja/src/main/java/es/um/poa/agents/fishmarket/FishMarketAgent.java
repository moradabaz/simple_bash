package es.um.poa.agents.fishmarket;

import es.um.poa.agents.TimePOAAgent;
import es.um.poa.agents.fishmarket.behaviours.*;
import es.um.poa.productos.Fish;
import jade.lang.acl.MessageTemplate;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;

import static jade.lang.acl.MessageTemplate.MatchConversationId;

/**
 * El Agente FishMarket representa la lonja de pescado. Este agente va a tener:
 * - Un contador de ingresos
 * - Un porcentaje de comision
 * Tambien tendrá una base de datos (@SellerBuyerDB) para almecenar los vendedores y compradores
 *
 */
public class FishMarketAgent extends TimePOAAgent {

	public static final double COMISION_LOTE = 0.12;

	private double ingresos = 0;
	private boolean subastando = false;
	private LinkedList<Fish> lotesASubastar = new LinkedList<Fish>();
	private HashMap<String, Double> gananciasVendedore = new HashMap<String, Double>();
	private boolean isSubastaON = false;
	private boolean isRetiradaCompraON = false;
	private boolean isRetiradaGananciaON = false;

	public void setup() {
		super.setup();
		Object[] args = getArguments();
		if (args != null && args.length == 1) {

			String configFile = (String) args[0];
			FishMarketAgentConfig config = initAgentFromConfigFile(configFile);

			if(config != null) {
				/**
				 * Mensaje para crear una respuesta la solicitud de registro de un agente comprador
				 */
				MessageTemplate messageTemplate = MatchConversationId("buyer-register");
				// aniadimos el comportamiento del registro de comprrador
				addBehaviour(new RegistroCompradorResp(this, messageTemplate));

				MessageTemplate messageTemplateAddCredit = MessageTemplate.MatchConversationId("buyer-addCredit");
				addBehaviour(new InicioCreditoResp(this, messageTemplateAddCredit));

				/**
				 * Mensaje para crear una respuesta de solicitud de registro de un comprador
				 */
				MessageTemplate messageTemplateRV = MatchConversationId("seller-register");
				// Aniadimos un comportamiento de registro de vendedor
				addBehaviour(new RegistroVendedorResp(this, messageTemplateRV));

				// Mensaje para crear una respuesta para el deposito de la lonja por parte del vendedor
				MessageTemplate messageTemplateDP = MessageTemplate.MatchConversationId("deposito-fish");
				// Aniadimos un comportamiento de respuesta a la solicitud de pescado
				addBehaviour(new DepositoPescadoResp(this, messageTemplateDP));






			}

		} else {
			this.getLogger().info("ERROR", "Requiere fichero de cofiguración.");
			doDelete();
		}
	}

	@Override
	public void checkAgentBehaviours() {
		int faseActual = getFaseActual();
		switch (faseActual) {
			case FASE_SUBASTA:
				if (!isSubastaON) {
					addBehaviour(new SubastaLote(this, 1000));
					isSubastaON = true;
				}
				break;
			case FASE_RETIRADA_COMPRADOR:
				if (!isRetiradaCompraON) {
					MessageTemplate retiraCompramsg = MessageTemplate.MatchConversationId("retiro-compra");
					addBehaviour(new RetiroCompraResp(this, retiraCompramsg));
					isRetiradaCompraON = true;
				}
				break;
			case FASE_RETIRADA_VENDEDOR:
				if (!isRetiradaGananciaON) {
					MessageTemplate retiroGanancia = MessageTemplate.MatchConversationId("retiro-ganancia");
					addBehaviour(new RetiroGananciaResp(this, retiroGanancia));
					isRetiradaGananciaON = true;
				}
		}
	}

	private FishMarketAgentConfig initAgentFromConfigFile(String fileName) {
		FishMarketAgentConfig config = null;
		try {
			Yaml yaml = new Yaml();
			InputStream inputStream;
			inputStream = new FileInputStream(fileName);
			config = (FishMarketAgentConfig) yaml.load(inputStream);
			getLogger().info("initAgentFromConfigFile", config.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return config;
	}


	public void anadirLoteASubasta(Fish fish) {
		lotesASubastar.add(fish);
	}

	public void anadirTodosLotes(LinkedList<Fish> lista) {
		lotesASubastar.addAll(lista);
	}

	public void removeLoteFromSubasta(Fish fish) {
		if (lotesASubastar.contains(fish))
			lotesASubastar.add(fish);
	}

	public void removeFirstLote() {
		if (!lotesASubastar.isEmpty())
			lotesASubastar.removeFirst();
	}

	public LinkedList<Fish> getLotesASubastar() {
		return lotesASubastar;
	}

	private void parseBuyerConfig(String buyerFile) {}

	public void setSubastando(boolean subastando) {
		this.subastando = subastando;
	}

	public boolean isSubastando() {
		return subastando;
	}

	public void incrementarIngreso(double ingreso) {
		this.ingresos += ingreso;
	}

	public void incrementarGanancia(String cif, double ingreso) {
		double ganancia = gananciasVendedore.get(cif);
		ganancia += ingreso;
		gananciasVendedore.put(cif, ganancia);
	}
	public void anadirVendedorGanancia(String cif, double ingreso) {
		if (gananciasVendedore.containsKey(cif)) {
			incrementarGanancia(cif, ingreso);
		} else {
			this.gananciasVendedore.put(cif, ingreso);
		}
	}

	public HashMap<String, Double> getGananciasVendedore() {
		return gananciasVendedore;
	}


	public double getIngresos() {
		return ingresos;
	}
}
