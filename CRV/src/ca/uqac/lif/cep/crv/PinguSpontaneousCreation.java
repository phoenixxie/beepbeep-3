package ca.uqac.lif.cep.crv;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Pullable;
import ca.uqac.lif.cep.epl.WindowFunction;
import ca.uqac.lif.cep.sets.IsSupersetOrEqual;
import ca.uqac.lif.cep.sets.ToStringHashSet;
import ca.uqac.lif.cep.xml.XmlFeeder;
import ca.uqac.lif.cep.xml.XPathEvaluator;
import ca.uqac.lif.cep.xml.XPathStreamReader;
import ca.uqac.lif.xml.XPathExpression;
import ca.uqac.lif.xml.XPathExpression.XPathParseException;

import java.io.FileNotFoundException;

public class PinguSpontaneousCreation {

    public static void main(String[] args) throws FileNotFoundException, XPathParseException, Connector.ConnectorException {

        if (args.length != 1) {
            return;
        }

        String filepath = args[0];


        XPathStreamReader reader = new XPathStreamReader(filepath, "/trace/message/characters");
        XmlFeeder feeder = new XmlFeeder();
        XPathEvaluator xpath = new XPathEvaluator(XPathExpression.parse("characters/character/id"));
        WindowFunction wf = new WindowFunction(IsSupersetOrEqual.instance);
        ToStringHashSet tshs = new ToStringHashSet();
        Connector.connect(reader, feeder, xpath, tshs, wf);

        Pullable p = wf.getPullableOutput(0);

        while (p.hasNext() != Pullable.NextStatus.NO) {

            Object result = p.pull();
            if (result == null) {
                continue;
            }
            if (result instanceof Boolean && (Boolean)result == false) {
                System.out.println("STATUS: Violated");
                System.exit(0);
            }
        }

        System.out.println("STATUS: Satisfied");

    }

}
