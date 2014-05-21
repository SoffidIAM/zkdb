package es.caib.zkib.component;

import java.io.IOException;
import java.io.InputStream;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.zkoss.zk.ui.UiException;

import es.caib.zkib.datasource.AbstractDataSource;
import es.caib.zkib.datasource.CommitException;
import es.caib.zkib.jxpath.xml.DOMParser;

public class XMLDataSource extends AbstractDataSource {

	/**
	 * Versió 1.1:  Alejandro Usero Ruiz - 13/07/2011
	 *   - Si el primer node del fitxer XML és un comentari, l'ignorem
	 *     fins que trobem un fill que no siga comentari.
	 *     
	 * Versió 1.2: 20-08-2012
	 * 	 - Fem que si no troba el resource (xml) emprant zk
	 *     (getDesktop().getWeapp()) empre el classloader de la classe 
	 * 	
	 */
	private static final long serialVersionUID = 6138399623413047624L;
	private String src;
	private transient Object datanode = null;
	
	public XMLDataSource() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Object getData() {
		if (datanode == null)
		{
			try {
				InputStream input = getDesktop().getWebApp().getResourceAsStream(src);
				if ( input == null) {
					// Intentem fer-ho emprant el classloader
					input = getClass().getResourceAsStream(src);
				} 
				if (input==null) { 
					throw new UiException ("XML file not found "+src);
				}
					
				DOMParser p = new DOMParser ();
				Document doc = (Document) p.parseXML(input);
				org.w3c.dom.Node first = doc.getFirstChild();
				// Si el primer node es un comentari, agafem següent node
				if (first==null) datanode = first; 
				else { //Mirem si el primer node és un comentari, i l'ignorem
					if (first.getNodeType()!= org.w3c.dom.Node.COMMENT_NODE)
						datanode = first;
					else { //cerquem un fill no comentari:
						NodeList fills = doc.getChildNodes();
						boolean trobat = false;
						for (int i=0; !trobat &&  i < fills.getLength(); i++) {
							if (fills.item(i)!=null && fills.item(i).getNodeType() != org.w3c.dom.Node.COMMENT_NODE) {
								trobat = true;
								datanode = fills.item(i);
							}
						}

						if (!trobat) datanode = null;
					}
				}
				
				input.close();
			} catch (IOException e) {
				throw new UiException (e);
			}
		}
		return datanode;
	}

	/**
	 * @return Returns the src.
	 */
	public String getSrc() {
		return src;
	}

	/**
	 * @param src The src to set.
	 */
	public void setSrc(String src) {
		this.src = src;
	}

	public void commit() throws CommitException {
		throw new UnsupportedOperationException ("commit");
		
	}

	public boolean isCommitPending() {
		return false;
	}

	public String getRootPath() {
		return "/";
	}

}
