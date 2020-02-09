package es.caib.zkib.binder.list;

import org.zkoss.zul.ListModel;
import org.zkoss.zul.event.ListDataEvent;

public class SoffidListDataEvent extends ListDataEvent {

	public static final int INTERVAL_UPDATED = 5;

	public SoffidListDataEvent(ListModel model, int type, int index0, int index1) {
		super(model, type, index0, index1);
	}

}
