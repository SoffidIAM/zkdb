package es.caib.zkib.component;

import java.io.IOException;
import java.io.Writer;

import org.zkoss.xml.HTMLs;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.render.SmartWriter;
import org.zkoss.zul.impl.XulElement;

public class Pager extends XulElement  {
	String datatable;
	String datatree2; 
	
	public Pager() {
		setSclass("pager");
	}
	
    @Override
	public void redraw(Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final Pager self = this;
		final String uuid = self.getUuid();		

		wh.write("<span id=\"").write(uuid).write("\" z.type=\"zul.pager.Pager\"")
		.write(self.getOuterAttrs()).write(self.getInnerAttrs())
		.write(">")
		.write("<span id=\"").write(uuid).write("!prev\" style='cursor: pointer'>&#x25c4;</span>")
		.write("<span id=\"").write(uuid).write("!text\"></span>")
		.write("<span id=\"").write(uuid).write("!next\" style='cursor: pointer'>&#x25ba;</span>")
		.write("</span>");
	}

	@Override
	public String getOuterAttrs() {
		StringBuffer sb = new StringBuffer ( super.getOuterAttrs() );
		
		HTMLs.appendAttribute(sb, "datatable", getTableReference());
		HTMLs.appendAttribute(sb, "datatree2", getTreeReference());

		return sb.toString();
	}

	private String getTableReference() {
		if ( datatable == null)
			return null;
		else
		{
			Component c = Path.getComponent(getSpaceOwner(), datatable);
			if (c == null)
				return null;
			else if (c instanceof DataTable)
				return c.getUuid();
			else
				throw new UiException("Error rendering pager. Object "+datatable+" is not a datatable");
		}
	}

	private String getTreeReference() {
		if ( datatree2 == null)
			return null;
		else
		{
			Component c = Path.getComponent(getSpaceOwner(), datatree2);
			if (c == null)
				return null;
			else if (c instanceof DataTree2)
				return c.getUuid();
			else
				throw new UiException("Error rendering pager. Object "+datatable+" is not a datatree2");
		}
	}

	public String getDatatable() {
		return datatable;
	}

	public void setDatatable(String datatable) {
		this.datatable = datatable;
		smartUpdate("datatable", getTableReference());
	}

	public String getDatatree2() {
		return datatree2;
	}

	public void setDatatree2(String datatree2) {
		this.datatree2 = datatree2;
	}



}
