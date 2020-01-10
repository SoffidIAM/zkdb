package es.caib.zkib.component;

import java.util.Iterator;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;

import es.caib.zkib.binder.SingletonBinder;
import es.caib.zkib.binder.SmartEvents;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathSubscriber;

public class DataRadiogroup extends Radiogroup implements XPathSubscriber {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1805040798731878821L;
	SingletonBinder binder = new SingletonBinder (this);
	private boolean duringOnUpdate;

	public DataRadiogroup() {
		super();
	}

	public void setBind (String bind)
	{
		binder.setDataPath(bind);
		SmartEvents.postEvent("onInitRender", this, null);
	}
	
	public String getBind ()
	{
		return binder.getDataPath();
	}
	
	public void onUpdate (XPathEvent event) {
		updateRadios();
	}

	/**
	 * 
	 */
	private void updateRadios() {
		Object newVal = binder.getValue();
		int selected = -1;
		Iterator it = getChildren().iterator();
		int i = 0;
		while (it.hasNext())
		{
			Radio r = (Radio) it.next ();
			r.setDisabled(!binder.isVoid () && ! binder.isValid());
			if (r.getValue() != null && r.getValue().equals(newVal))
			{
				selected = i;
			}
			else
				i++;
		}
		try {
			duringOnUpdate = true;
			setSelectedIndex ( selected );
		} finally {
			duringOnUpdate = false;
		}
	}

	
	/* (non-Javadoc)
	 * @see org.zkoss.zul.Radiogroup#setSelectedIndex(int)
	 */
	public void setSelectedIndex(int jsel) {
		super.setSelectedIndex(jsel);
		updateBinder(); 
	}
	
	protected void updateBinder ()
	{
		if ( ! duringOnUpdate) 
		{
			if (getSelectedItem() == null)
				binder.setValue( null );
			else
				binder.setValue(getSelectedItem().getValue());
		}
	}

	public void onInitRender() {
		updateRadios();
	}

	public void setPage(Page page) {
		super.setPage(page);
		binder.setPage(page);
	}

	public void setParent(Component parent) {
		super.setParent(parent);
		binder.setParent(parent);
	}

	public Object clone() {
		DataRadiogroup clone = (DataRadiogroup) super.clone();
		clone.binder = new SingletonBinder (clone);
		clone.binder.setDataPath(binder.getDataPath());
		return clone;
	}
}
