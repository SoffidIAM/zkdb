<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>
<%@ taglib uri="http://www.zkoss.org/dsp/zk/core" prefix="z" %>
<c:set var="self" value="${requestScope.arg.self}"/>
<div z.type="zul.switch.Switch" unselectable="on" id="${self.uuid}"${self.outerAttrs}${self.innerAttrs}>
	<span class="on">${c:l('mesg:org.zkoss.zul.mesg.MZul:YES')}</span>
	<span class="off">${c:l('mesg:org.zkoss.zul.mesg.MZul:NO')}</span>
	<span id="${self.uuid}!slider" class="slider not-dragging"><span class="handle"></span></span>
</div>