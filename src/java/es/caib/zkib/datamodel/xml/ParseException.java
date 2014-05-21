package es.caib.zkib.datamodel.xml;

import java.io.File;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ParseException extends Exception {

	public ParseException() {
		super();
	}
	
	private static String toText (Node node)
	{
		String result = "<"+node.getNodeName()+" ";
		for (int i = 0; i < node.getAttributes().getLength(); i++)
		{
			Node attr = node.getAttributes().item(i);
			result = result +  attr.getNodeName();
			result = result + "=\""+attr.getNodeValue()+"\" ";
		}
		result = result + ">";
		return result;
	}
	public ParseException(String message, Node node) {
		super(message+"\nParsing: "+toText(node));
	}

	public ParseException(Throwable cause) {
		super(cause);
	}

	public ParseException(String message, Node node, Throwable cause) {
		super(message+"\nParsing:"+toText(node), cause);
	}

}
