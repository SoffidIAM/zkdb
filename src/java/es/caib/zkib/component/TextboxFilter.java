package es.caib.zkib.component;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Textbox;

import es.caib.zkib.events.SerializableEventListener;

public class TextboxFilter extends Textbox implements HeaderFilter {
	String bind;
	public String getBind() {
		return bind;
	}
	public void setBind(String bind) {
		this.bind = bind;
	}

	public TextboxFilter() {
		super();
		final TextboxFilter filter = this;
        EventListener onSelectListener = new SerializableEventListener() 
        {
            public void onEvent(org.zkoss.zk.ui.event.Event arg0) 
            {
            	Component c = filter;
            	do
            	{
            		c = c.getParent();
            	} while (c != null && ! (c instanceof DataListbox));
            	if (c != null)
            		((DataListbox) c).onUpdateFilterSelection();
            };
        };
        this.addEventListener("onOK", onSelectListener);
        this.addEventListener("onChange", onSelectListener);
        setSclass("listfilter");
	}
	
	public void appendFilter(StringBuffer filter) {
		if (getValue() != null && getValue().trim().length() > 0)
			filter.append ("[contains(soffid:toLowerCase(")
			.append(getBind())
			.append("),\"")
			.append(getValue().replaceAll("\"", "\\\"").toLowerCase())
			.append("\")]");
	}
}
