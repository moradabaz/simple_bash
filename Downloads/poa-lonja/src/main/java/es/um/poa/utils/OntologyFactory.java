package es.um.poa.utils;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.um.poa.agents.clock.SimTimeOntology;

public class OntologyFactory {

	public static SimTimeOntology getSimTimeOntologyObject(String json) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(json, SimTimeOntology.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getSimTimeOntologyJSON(SimTimeOntology obj) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}
}
