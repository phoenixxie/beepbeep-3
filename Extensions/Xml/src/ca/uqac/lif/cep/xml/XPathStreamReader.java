package ca.uqac.lif.cep.xml;

import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.SingleProcessor;

import javax.xml.stream.*;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.Queue;

public class XPathStreamReader extends SingleProcessor {

    private static XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    private static XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

    String filename;
    String xpath;
    XMLEventReader reader = null;
    String curElement = "";
    StringWriter writer = null;

    public XPathStreamReader(String filename, String xpath) {
        super(0, 1);

        this.filename = filename;
        this.xpath = xpath;
    }

    @Override
    protected Queue<Object[]> compute(Object[] objects) {
        if (reader == null) {
            InputStream in = null;
            try {
                in = new FileInputStream(filename);
                reader = inputFactory.createXMLEventReader(in);
            } catch (FileNotFoundException | XMLStreamException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        try {
            XMLEventWriter xmlEventWriter = null;
            boolean got = false;

            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();

                if (event.isStartElement()) {
                    curElement = curElement + "/" + ((StartElement)event).getName().toString();
                    if (this.xpath.equals(curElement)) {
                        writer = new StringWriter();
                        xmlEventWriter = outputFactory.createXMLEventWriter(writer);
                    }
                } else if (event.isEndElement()) {
                    curElement = curElement.substring(0, curElement.lastIndexOf("/" + ((EndElement) event).getName().toString()));
                    if (!curElement.startsWith(this.xpath) && writer != null) {
                        got = true;
                    }
                }

                if (writer != null) {
                    xmlEventWriter.add(event);
                }
                if (got) {
                    xmlEventWriter.close();
                    xmlEventWriter = null;
                    String xml = writer.toString();
                    writer = null;
                    got = false;
                    return wrapObject(xml);
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
            try {
                reader.close();
            } catch (XMLStreamException ee) {
                ee.printStackTrace();
            }

            return null;
        }

        try {
            reader.close();
        } catch (XMLStreamException e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }

    @Override
    public Processor clone() {
        System.err.println("Cannot be cloned!");
        System.exit(1);

        return null;
    }
}
