package es.caib.zkib.datamodel;


import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Events;

import es.caib.zkib.binder.SmartEvents;
import es.caib.zkib.datasource.AbstractDataSource;
import es.caib.zkib.datasource.CommitException;
import es.caib.zkib.events.XPathRerunEvent;
import es.caib.zkib.exceptions.ValidationException;




public abstract class AbstractDataModel extends AbstractDataSource  {
	public AbstractDataModel() {
		super();
	}

	public abstract DataModelNode getDataNode();

	public Object getData() {
		return getDataNode ();
	}

	
	public void refresh ()
	{
		getDataNode().refresh();
		SmartEvents.postEvent("onChange", this, null);
        sendEvent(new XPathRerunEvent (this, "/"));
	}
	
	
	public boolean isCommitPending ()
	{
		return getDataNode().isCommitPending();
	}
	
	public synchronized void commit() throws CommitException {
		try {
			UserTransaction tx;
			try {
				tx = (UserTransaction) new InitialContext()
						.lookup("/UserTransaction");
			} catch (NamingException e1) {
				
				try {
					tx = (UserTransaction) new InitialContext()
							.lookup("java:/comp/UserTransaction");
				} catch (NamingException e2) {
					
					throw new RuntimeException(e2);
				}
			}
			try {
				tx.begin();
			} catch (NotSupportedException e1) {
				throw new RuntimeException(e1);
			}
			try {
				getDataNode().prepareCommit();
				getDataNode().commit();
				SmartEvents.postEvent("onCommit", this, null);
			} catch (CommitException e) {
				if (tx != null)
					tx.setRollbackOnly();
				e.activateDataModel();
				throw e;
			} finally {
				if (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK)
					tx.rollback();
				else
					tx.commit();
			}
		} catch (SystemException e) {
			throw new RuntimeException (e);
		} catch (HeuristicMixedException e) {
			throw new RuntimeException (e);
		} catch (HeuristicRollbackException e) {
			throw new RuntimeException (e);
		} catch (RollbackException e) {
			throw new RuntimeException (e);
		}
	}

}
