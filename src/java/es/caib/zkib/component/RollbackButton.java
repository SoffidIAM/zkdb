package es.caib.zkib.component;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import es.caib.zkib.datasource.CommitException;
import es.caib.zkib.datasource.XPathUtils;

public class RollbackButton extends DatasourceButton  {
	/**
	 * 
	 */
	public RollbackButton ()
	{
		super();
		setImage ("~./img/document-save.gif"); //$NON-NLS-1$
		
		addEventListener(Events.ON_CLICK, new EventListener() {
			public boolean isAsap() {return true;}
			public void onEvent(org.zkoss.zk.ui.event.Event event) {
				if (getDatamodel() != null)
				{
					Component c = XPathUtils.getPath(RollbackButton.this, getDatamodel());
					if (c != null && c instanceof DataModel)
					{
						DataModel dm = (DataModel) c;
						dm.refresh() ;
					}
				}
			}
		});
	}
}
