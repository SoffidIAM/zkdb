<%@ taglib uri="/WEB-INF/tld/web/core.dsp.tld" prefix="c" %>
<c:set var="self" value="${requestScope.arg.self}"/>
<c:set var="edid" value="${self.uuid}!ed"/>
<link rel="stylesheet" type="text/css" href="/zkau/web/js/ext/codemirror/lib/codemirror.css"/>
<script src="${pageContext.request.contextPath}/zkau/web/js/ext/codemirror/mode/xml/xml.js"> </script>
<script src="${pageContext.request.contextPath}/zkau/web/js/ext/codemirror/mode/javascript/javascript.js"> </script>
<div id="${self.uuid}"${self.outerAttrs} z.type="codemirror.codemirror.Codemirror"
${c:attr('class',self.sclass)}${c:attr('style',self.style)} ${self.innerAttrs}>
</textarea>