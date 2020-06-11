package es.caib.zkib.component;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;

public class ReorderEvent extends Event {
	public ReorderEvent(String name, Component target) {
		super(name, target);
	}
	
	int srcPosition;
	Integer insertBeforePosition;
	Object srcObject;
	Object insertBeforeObject;
	public int getSrcPosition() {
		return srcPosition;
	}
	public void setSrcPosition(int srcPosition) {
		this.srcPosition = srcPosition;
	}
	public Integer getInsertBeforePosition() {
		return insertBeforePosition;
	}
	public void setInsertBeforePosition(Integer insertBeforePosition) {
		this.insertBeforePosition = insertBeforePosition;
	}
	public Object getSrcObject() {
		return srcObject;
	}
	public void setSrcObject(Object srcObject) {
		this.srcObject = srcObject;
	}
	public Object getInsertBeforeObject() {
		return insertBeforeObject;
	}
	public void setInsertBeforeObject(Object insertBeforeObject) {
		this.insertBeforeObject = insertBeforeObject;
	}
}