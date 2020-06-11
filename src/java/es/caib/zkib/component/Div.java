package es.caib.zkib.component;

import org.zkoss.xml.HTMLs;

public class Div extends org.zkoss.zul.Div {
	String title;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getInnerAttrs() {
		StringBuffer sb = new StringBuffer();
		HTMLs.appendAttribute(sb, "title", title);
		sb.append(super.getInnerAttrs());
		return sb.toString();
	}
}
