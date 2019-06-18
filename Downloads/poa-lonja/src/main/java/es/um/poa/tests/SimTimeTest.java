package es.um.poa.tests;

import es.um.poa.agents.clock.SimTimeOntology;
import org.junit.Before;
import org.junit.Test;

public class SimTimeTest {

    public SimTimeOntology simTime;

    @Before
    public void inicializar() {
        simTime = new SimTimeOntology(1,2);
    }

    @Test
    public void test() {
        System.out.println(simTime.getTime());
    }
}
