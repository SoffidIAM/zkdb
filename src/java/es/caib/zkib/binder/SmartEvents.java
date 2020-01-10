package es.caib.zkib.binder;

import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;

public class SmartEvents {

	public static void postEvent(String eventName, Component _component, Object data) {
		if (_component.getListenerIterator(eventName).hasNext() ||
				(_component instanceof AbstractComponent && 
						((AbstractComponent)_component).getEventHandler(eventName) != null))
			Events.postEvent(eventName, _component, data);
	}

}
