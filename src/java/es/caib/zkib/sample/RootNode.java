package es.caib.zkib.sample;

import java.util.Vector;

import es.caib.zkib.datamodel.DataContext;
import es.caib.zkib.datamodel.Finder;
import es.caib.zkib.datamodel.SimpleDataNode;

public class RootNode extends SimpleDataNode {

	public RootNode(DataContext ctx) {
		super(ctx);
		// Title
		addFinder("title",
				new Finder () {
					public java.util.Collection find() throws Exception {
						Title t = new Title ();
						t.name = "Paises del mundo";
		
						Vector v = new Vector();
						v.add (t);
						return v;
					};
					public Object newInstance() throws Exception {
						throw new UnsupportedOperationException();
					}
					public boolean refreshAfterCommit() {
						return false;
					}
				}, 
				SimpleDataNode.class);
		// Countries
		addFinder("country",
			new Finder () {
				public java.util.Collection find() throws Exception {
					Country c1 = new Country ();
					c1.name="USA";
					c1.abbreviation="us";
					Country c2 = new Country ();
					c2.name="Deutschland";
					c2.abbreviation="de";
					Country c3 = new Country ();
					c3.name="España";
					c3.abbreviation="es";
	
					Vector v = new Vector();
					v.add (c1);
					v.add (c2);
					v.add (c3);
					return v;
				};
				public Object newInstance() throws Exception {
					Country c = new Country ();
					c.name = "??";
					c.abbreviation = "?";
					return c;
				}
				public boolean refreshAfterCommit() {
					return false;
				}
			}, 
			CountryNode.class);
	}

}
