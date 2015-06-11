package es.caib.zkib.component;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Listbox;

public class ListboxFilter extends Listbox implements HeaderFilter
{
	String bind;
	public String getBind() {
		return bind;
	}
	public void setBind(String bind) {
		this.bind = bind;
	}

	   /*
     * (non-Javadoc)
     * 
     * @see org.zkoss.zul.Listbox#smartUpdate(java.lang.String,
     *      java.lang.String)
     */
    public void smartUpdate(String attr, String value) {
        super.smartUpdate(attr, value);
        if ("selectedIndex".equals(attr) || "select".equals(attr)) {
        	Component c = this;
        	do
        	{
        		c = c.getParent();
        	} while (c != null && ! (c instanceof DataListbox));
        	if (c != null)
        		((DataListbox) c).onUpdateFilterSelection();
        }
    }
    
	public ListboxFilter() {
		super();
		setMold("select");
        EventListener onSelectListener = new EventListener() {
            public void onEvent(org.zkoss.zk.ui.event.Event arg0) {// NOTHING
                                                                    // TO DO
            };
        };
        this.addEventListener("onSelect", onSelectListener);
        setSclass("listfilter");
	}
	
	public void appendFilter(StringBuffer filter) {
		if (getSelectedItem() != null && getSelectedItem().getValue() != null)
			filter.append ('[')
			.append(getBind())
			.append("='")
			.append(getSelectedItem().getValue().toString().replaceAll("'", "\\'"))
			.append("']");
	}
}
