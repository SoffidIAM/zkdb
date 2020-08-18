package es.caib.zkib.component;

import java.io.IOException;
import java.io.Writer;

import org.zkoss.xml.HTMLs;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zk.ui.metainfo.EventHandler;
import org.zkoss.zk.ui.metainfo.ZScript;
import org.zkoss.zk.ui.render.SmartWriter;
import org.zkoss.zul.Button;

import es.caib.zkdb.yaml.Yaml2Json;

public class Wizard extends Div implements AfterCompose {
	String steps;
	int selected = 0;
	public String getSteps() {
		return steps;
	}
	
	public void setSteps(String steps) throws IOException {
		this.steps = new Yaml2Json().transform(steps);
		smartUpdate("steps", steps);
	}
	
	public int getSelected() {
		return selected;
	}
	
	public void setSelected(int selected) {
		this.selected = selected;
		smartUpdate("selected", selected);
	}
	
    @Override
	public void redraw(Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final String uuid = getUuid();		

		wh.write("<div id=\"").write(uuid).write("\" z.type=\"zul.wizard.Wizard\"")
		.write(getOuterAttrs()).write(getInnerAttrs())
		.write(">")
		.writeChildren(this)
		.write("</div>");
	}

	@Override
	public String getInnerAttrs() {
		StringBuffer sb = new StringBuffer();
		HTMLs.appendAttribute(sb, "steps", steps);
		HTMLs.appendAttribute(sb, "selected", selected);
		sb.append(super.getInnerAttrs());
		return sb.toString();
	}

	public void start() {
		setSelected(0);
	}
	
    public void next() {
    	setSelected(selected + 1);
    }

    public void previous() {
    	if (selected > 0)
    		setSelected(selected - 1);
    }

	public void afterCompose() {
		addEventHandler("onOK",  new EventHandler(ZScript.parseContent("ref:"+getId()+".onNext"), null));
	}
	
	public void onNext(Event ev) {
		Button b = findLastButton ((Component) getChildren().get(getSelected()));
		if ( b != null )
			Events.sendEvent(new Event("onClick", b));
	}

	private Button findLastButton(Component object) {
		if (object instanceof Button)
			return (Button) object;
		for (Component child = object.getLastChild(); child != null; child = child.getPreviousSibling())
		{
			Button b = findLastButton(child);
			if (b != null) return b;
		}
		return null;
	}
}
