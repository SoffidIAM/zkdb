package es.caib.zkib.datasource;

import java.util.Vector;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;

public class DOMNode implements Node {

	protected short type;
	protected Vector children = new Vector ();
	protected Vector attributes = new Vector();
	protected String name;
	protected String value;
	protected String prefix;
	
	public DOMNode() {
		super();
	}

	public short getNodeType() {
		return type;
	}

	public void normalize() {

	}

	public boolean hasAttributes() {
		return ! attributes.isEmpty();
	}

	public boolean hasChildNodes() {
		return ! children.isEmpty();
	}

	public String getLocalName() {
		return name;
	}

	public String getNamespaceURI() {
		return null;
	}

	public String getNodeName() {
		return name;
	}

	public String getNodeValue() throws DOMException {
		return value;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setNodeValue(String nodeValue) throws DOMException {
		value = nodeValue;
	}

	public void setPrefix(String prefix) throws DOMException {
		this.prefix = prefix;

	}

	public Document getOwnerDocument() {
		return null;
	}

	public NamedNodeMap getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	public Node getFirstChild() {
		// TODO Auto-generated method stub
		return null;
	}

	public Node getLastChild() {
		// TODO Auto-generated method stub
		return null;
	}

	public Node getNextSibling() {
		// TODO Auto-generated method stub
		return null;
	}

	public Node getParentNode() {
		// TODO Auto-generated method stub
		return null;
	}

	public Node getPreviousSibling() {
		// TODO Auto-generated method stub
		return null;
	}

	public Node cloneNode(boolean deep) {
		// TODO Auto-generated method stub
		return null;
	}

	public NodeList getChildNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSupported(String feature, String version) {
		// TODO Auto-generated method stub
		return false;
	}

	public Node appendChild(Node newChild) throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	public Node removeChild(Node oldChild) throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	public Node insertBefore(Node newChild, Node refChild) throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getBaseURI() {
		return null;
	}

	public short compareDocumentPosition(Node other) throws DOMException {
		return 0;
	}

	public String getTextContent() throws DOMException {
		return null;
	}

	public void setTextContent(String textContent) throws DOMException {
		
	}

	public boolean isSameNode(Node other) {
		return false;
	}

	public String lookupPrefix(String namespaceURI) {
		return null;
	}

	public boolean isDefaultNamespace(String namespaceURI) {
		return false;
	}

	public String lookupNamespaceURI(String prefix) {
		return null;
	}

	public boolean isEqualNode(Node arg) {
		return false;
	}

	public Object getFeature(String feature, String version) {
		return null;
	}

	public Object setUserData(String key, Object data, UserDataHandler handler) {
		return null;
	}

	public Object getUserData(String key) {
		return null;
	}

}
