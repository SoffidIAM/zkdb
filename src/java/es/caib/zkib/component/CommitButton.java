package es.caib.zkib.component;

import java.io.IOException;
import java.io.Writer;

import org.zkoss.xml.HTMLs;
import org.zkoss.xml.XMLs;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.render.SmartWriter;
import org.zkoss.zul.Button;
import org.zkoss.zul.Toolbarbutton;

import es.caib.zkib.component.DataModel;
import es.caib.zkib.datasource.CommitException;
import es.caib.zkib.datasource.DataSource;

public class CommitButton extends DatasourceButton  {
	/**
	 * 
	 */
	public CommitButton ()
	{
		super();
		setImage ("~./img/document-save.gif"); //$NON-NLS-1$
		
		addEventListener(Events.ON_CLICK, new EventListener() {
			public boolean isAsap() {return true;}
			public void onEvent(org.zkoss.zk.ui.event.Event event) {
				if (getDatamodel() != null)
				{
					Component c = Path.getComponent(getSpaceOwner(), getDatamodel());
					if (c != null && c instanceof DataModel)
					{
						DataModel dm = (DataModel) c;
						try {
							dm.commit() ;
						} catch (CommitException e) {
							throw new UiException (e);
						}
					}
				}
			}
		});
	}
}
