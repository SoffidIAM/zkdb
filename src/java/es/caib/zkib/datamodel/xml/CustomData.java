package es.caib.zkib.datamodel.xml;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;

import es.caib.zkib.datamodel.xml.definition.ModelDefinition;

public class CustomData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ModelDefinition model;
	private String type;
	/**
	 * @return Returns the model.
	 */
	public ModelDefinition getModel() {
		return model;
	}
	/**
	 * @param model The model to set.
	 */
	public void setModel(ModelDefinition model) {
		this.model = model;
	}
	/**
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type The type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}
}
