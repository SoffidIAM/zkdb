<%@ taglib uri="/WEB-INF/tld/web/core.dsp.tld" prefix="c" %>
<c:set var="self" value="${requestScope.arg.self}"/>
<c:set var="edid" value="${self.uuid}!ed"/>
<div id="${self.uuid}"${self.outerAttrs} z.type="ckeditor.ckeditor.Ckeditor"
${c:attr('class',self.sclass)}${c:attr('style',self.style)} ${self.innerAttrs}>
</div>
