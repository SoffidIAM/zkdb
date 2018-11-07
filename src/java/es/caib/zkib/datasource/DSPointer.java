package es.caib.zkib.datasource;

import es.caib.zkib.jxpath.Pointer;
import es.caib.zkib.jxpath.ri.model.beans.BeanPropertyPointer;
import es.caib.zkib.jxpath.util.ValueUtils;

public class DSPointer implements es.caib.zkib.jxpath.Pointer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -391737605707515282L;
	Pointer _impl;
	JXPContext _ctx;
	public DSPointer(JXPContext ctx, Pointer impl) {
		_impl = impl;
		_ctx = ctx;
	}
	/* (non-Javadoc)
	 * @see es.caib.zkib.jxpath.Pointer#asPath()
	 */
	public String asPath() {
		String s = _impl.asPath();
		if ( s.length() == 0)
			return "/";
		else
			return s;
	}
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		return _impl.compareTo(o);
	}
	/* (non-Javadoc)
	 * @see es.caib.zkib.jxpath.Pointer#getNode()
	 */
	public Object getNode() {
		return _impl.getNode();
	}
	/* (non-Javadoc)
	 * @see es.caib.zkib.jxpath.Pointer#getRootNode()
	 */
	public Object getRootNode() {
		return _impl.getRootNode();
	}
	/* (non-Javadoc)
	 * @see es.caib.zkib.jxpath.Pointer#getValue()
	 */
	public Object getValue() {
		if (_impl instanceof BeanPropertyPointer )
		{
			try {
				_impl = _ctx.getNativePointer(_impl.asPath());
			} catch (Exception e)
			{
				return null;
			}
		}
		return _impl.getValue();
	}
	/* (non-Javadoc)
	 * @see es.caib.zkib.jxpath.Pes.caib.seycon.net.web.zul.eventsointer#setValue(java.lang.Object)
	 */
	public void setValue(Object value) {
		_impl.setValue(value);
		_ctx.onUpdate( asPath());
	}
	/* (non-Javadoc)
	 * @see es.caib.zkib.jxpath.Pointer#clone()
	 */
	public Object clone() {
		return new DSPointer ( _ctx, (Pointer) _impl.clone());
	}

	public void invalidate ()
	{
		_ctx.onReRunXPath(asPath());
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return _impl.asPath();
	}

}
