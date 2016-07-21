package ca.uqac.lif.cep.xml;

import ca.uqac.lif.azrael.ObjectHandler;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.SingleProcessor;
import ca.uqac.lif.cep.tuples.FixedTupleBuilder;
import ca.uqac.lif.cep.tuples.NamedTupleMap;
import ca.uqac.lif.xml.XmlElement;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

public class XmlTupleGenerator extends SingleProcessor {

    List<String> xpaths;
    List<XPathExpression> expressions;
    static XPathFactory xPathfactory = XPathFactory.newInstance();
    static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    static DocumentBuilder builder;

    static {
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }


    public XmlTupleGenerator(String...paths) throws XPathExpressionException {
        super(1, 1);

        xpaths = Arrays.asList(paths);
        expressions =  new ArrayList<>(paths.length);
        XPath xpath = xPathfactory.newXPath();

        for (String path : paths) {
            expressions.add(xpath.compile(path));
        }
    }

    @Override
    protected Queue<Object[]> compute(Object[] inputs) {
        XmlElement element = (XmlElement)inputs[0];
        Document doc = null;

        try {
            doc = builder.parse(new ByteArrayInputStream(element.toString().getBytes()));
        } catch (SAXException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        String xml = element.toString();
        List<List<String>> values = new ArrayList<>(xpaths.size());

        int size = 0;
        try {

            for (XPathExpression expression : expressions) {
                NodeList nl = (NodeList)expression.evaluate(doc, XPathConstants.NODESET);
                List<String> strings = new ArrayList<>(nl.getLength());

                for (int i = 0; i < nl.getLength(); ++i) {
                    strings.add(nl.item(i).getTextContent());
                }
                size = strings.size();
                values.add(strings);
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        if (size == 0) {
            return null;
        }

        List<NamedTupleMap> result = new ArrayList<>(size);

        for (int i = 0; i < size; ++i) {
            NamedTupleMap tuple = new NamedTupleMap();
            for (int j = 0; j < xpaths.size(); ++j) {
                tuple.put(xpaths.get(j), values.get(j).get(i));
            }
            result.add(tuple);
        }

        return wrapObject(result);
    }

    @Override
    public Processor clone() {
        return null;
    }
}
