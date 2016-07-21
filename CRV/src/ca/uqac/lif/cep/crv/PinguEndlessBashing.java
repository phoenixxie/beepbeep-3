package ca.uqac.lif.cep.crv;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Fork;
import ca.uqac.lif.cep.Pullable;
import ca.uqac.lif.cep.epl.WindowFunction;
import ca.uqac.lif.cep.sets.IsSupersetOrEqual;
import ca.uqac.lif.cep.sets.ToStringHashSet;
import ca.uqac.lif.cep.sets.YMinusX;
import ca.uqac.lif.cep.tuples.*;
import ca.uqac.lif.cep.xml.XPathEvaluator;
import ca.uqac.lif.cep.xml.XPathStreamReader;
import ca.uqac.lif.cep.xml.XmlFeeder;
import ca.uqac.lif.cep.xml.XmlTupleGenerator;
import ca.uqac.lif.xml.XPathExpression;
import ca.uqac.lif.xml.XPathExpression.XPathParseException;

import javax.xml.xpath.XPathExpressionException;
import java.io.FileNotFoundException;

public class PinguEndlessBashing {

    public static void main(String[] args) throws FileNotFoundException, Connector.ConnectorException, XPathExpressionException {

        if (args.length != 1) {
            return;
        }

        String filepath = args[0];

        String labelId = "/characters/character/id";
        String labelStatus = "/characters/character/status";


        XPathStreamReader reader = new XPathStreamReader(filepath, "/trace/message/characters");
        XmlFeeder feeder = new XmlFeeder();
        XmlTupleGenerator xtg = new XmlTupleGenerator(labelId, labelStatus);
//        XPathEvaluator xpath = new XPathEvaluator(XPathExpression.parse("characters/character/id"));
//        WindowFunction wf = new WindowFunction(IsSupersetOrEqual.instance);
        Where basher = new Where(new EqualsExpression(new AttributeNamePlain(labelStatus), new StringExpression("BASHER")));
        Where walker = new Where(new EqualsExpression(new AttributeNamePlain(labelStatus), new StringExpression("WALKER")));
        Fork fork = new Fork(2);

        Connector.connect(reader, feeder, xtg, fork);
        Connector.connect(fork, basher, 0, 0);
        Connector.connect(fork, walker, 1, 0);

        Select selectBasher = new Select(1, labelId);
        Select selectWalker = new Select(1, labelId);
        ToStringHashSet tshsBasher = new ToStringHashSet();
        ToStringHashSet tshsWalker = new ToStringHashSet();
        WindowFunction wfBasher = new WindowFunction(YMinusX.instance);

        Connector.connect(basher, selectBasher, tshsBasher, wfBasher);
        Connector.connect(walker, selectWalker, tshsWalker);

        Pullable p = tshsBasher.getPullableOutput(0);

        while (p.hasNext() != Pullable.NextStatus.NO) {

            Object result = p.pull();
            if (result == null) {
                continue;
            }

            System.out.println(result);

        }

        System.out.println("STATUS: Satisfied");

    }

}
