package es.um.poa.agents.fishmarket;

import es.um.poa.agents.TimePOAAgent;
import es.um.poa.productos.Fish;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedList;

public class FishMarketAgent extends TimePOAAgent {

	private LinkedList<Fish> pesacadoEnSubasta;
	private LinkedList<Fish> pescadoVendido;
	private double ingresos;
	private double comisionPorLote;

	public void setup() {
		super.setup();
		Object[] args = getArguments();
		if (args != null && args.length == 1) {
			String configFile = (String) args[0];
			FishMarketAgentConfig config = initAgentFromConfigFile(configFile);
			
			if(config != null) {
				
			}
		} else {
			this.getLogger().info("ERROR", "Requiere fichero de cofiguración.");
			doDelete();
		}
	}
	
	private FishMarketAgentConfig initAgentFromConfigFile(String fileName) {
		FishMarketAgentConfig config = null;
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
