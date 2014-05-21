<%@ taglib uri="/WEB-INF/tld/web/core.dsp.tld" prefix="c" %>
<c:set var="self" value="${requestScope.arg.self}"/>
<c:set var="edid" value="${self.uuid}!ed"/>
<textarea id="${self.uuid}"${self.outerAttrs} z.type="codepressz.codepressz.Codepress"
${c:attr('class',self.options)}${c:attr('style',self.style)}>
${c:escapeXML(self.value)}
</textarea>