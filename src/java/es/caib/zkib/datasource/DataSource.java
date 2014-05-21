package es.caib.zkib.datasource;


import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathSubscriber;
import es.caib.zkib.jxpath.JXPathContext;
import es.caib.zkib.jxpath.Variables;

public interface DataSource {

	public abstract JXPathContext getJXPathContext();

	public abstract void subscribeToExpression(String xpath, XPathSubscriber subscriber);

	public abstract void unsubscribeToExpression(String xpath, XPathSubscriber subscriber);

	public abstract void sendEvent(XPathEvent event);
	
	public abstract void commit () throws CommitException;
	
	public abstract Variables getVariables ();
	
	public abstract boolean isCommitPending();
	
	public abstract String getRootPath();

}