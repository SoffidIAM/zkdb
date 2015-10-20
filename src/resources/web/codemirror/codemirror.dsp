<%@ taglib uri="/WEB-INF/tld/web/core.dsp.tld" prefix="c" %>
<c:set var="self" value="${requestScope.arg.self}"/>
<c:set var="edid" value="${self.uuid}!ed"/>
<link rel="stylesheet" href="${pageContext.request.contextPath}/zkau/web/js/ext/codemirror/lib/codemirror.css">
<script href="${pageContext.request.contextPath}/zkau/web/js/ext/codemirror/addon/runmode/colorize.js"> </script>
<script href="${pageContext.request.contextPath}/zkau/web/js/ext/codemirror/addon/hint/xml-hints.js"> </script>
<script href="${pageContext.request.contextPath}/zkau/web/js/ext/codemirror/addon/hint/javascript-hints.js"> </script>
<div id="${self.uuid}"${self.outerAttrs} z.type="codemirror.codemirror.Codemirror"
${c:attr('class',self.sclass)}${c:attr('style',self.style)} ${self.innerAttrs}>
</textarea>