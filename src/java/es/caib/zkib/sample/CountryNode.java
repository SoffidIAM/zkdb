package es.caib.zkib.sample;

import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import es.caib.zkib.datamodel.DataContext;
import es.caib.zkib.datamodel.DataNode;
import es.caib.zkib.datamodel.Finder;
import es.caib.zkib.datamodel.SimpleDataNode;

public class CountryNode extends DataNode {

	public CountryNode(DataContext ctx) {
		super(ctx);
		addFinder("city",
				new Finder () {
					public java.util.Collection find() throws Exception {
						Country c = (Country) getInstance();
						Vector v = new Vector();
						if (c.abbreviation.equals("de"))
						{
							City c1 = new City ();
							c1.name = "Berlin";
							City c2 = new City ();
							c2.name = "Bonn";
							City c3 = new City ();
							c3.name = "Hamburg";
							v.add (c1);
							v.add (c2);
							v.add (c3);
						}
						if (c.abbreviation.equals("es"))
						{
							City c1 = new City ();
							c1.name = "Palma de Mallorca";
							City c2 = new City ();
							c2.name = "Madrid";
							v.add (c1);
							v.add (c2);
						}
						if (c.abbreviation.equals("us"))
						{
							City c1 = new City ();
							c1.name = "Washington";
							v.add (c1);
						}
						return v;
					};
					public Object newInstance() throws Exception {
						return new City ();
					}
					public boolean refreshAfterCommit() {
						return false;
					}
				}, 
				SimpleDataNode.class);
	}

	protected void doInsert() throws Exception {
		// TODO Auto-generated method stub

	}

	protected void doUpdate() throws Exception {
		// TODO Auto-generated method stub

	}

	protected void doDelete() throws Exception {
		// TODO Auto-generated method stub

	}

	public Object getParentId() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getCurrentId() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getChildProperty() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object loadParentObject() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
