package es.caib.zkib.datamodel.xml.definition;

import java.io.Serializable;

public class CatchDefinition implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String value;
    public String exception;
    
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public String getException() {
        return exception;
    }
    public void setException(String exception) {
        this.exception = exception;
    }

}
