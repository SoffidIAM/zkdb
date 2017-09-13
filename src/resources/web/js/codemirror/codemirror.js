/** 
 * ZK port of Codemirror - Real Time Syntax Highlighting Editor written in JavaScript - http://codemirror.net/
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the 
 * GNU Lesser General Public License as published by the Free Software Foundation.
 * 
 * Read the full licence: http://www.opensource.org/licenses/lgpl-license.php
 */

zkCodemirror = {};

zkCodemirror.init = function (ed) {
	var v = ed.value;
	if (v == undefined)
		v = ed.getAttribute("value");
	if (v == undefined)
		v = "";
	var lang = ed.getAttribute("z.lang");
	if (lang == undefined)
		lang = "xml";
	var ln = ed.getAttribute("z.linenumbers");
	var ro = ed.getAttribute("z.readonly");
	ed.codemirror = new CodeMirror(ed, {
		value: v,
		hintOptions:   {
			globalVars: JSON.parse( ed.getAttribute("z.globalvars")),
		    hint: CodeMirror.hint.auto,
		    completeSingle: true,
		    alignWithWord: true,
		    closeCharacters: /[\s()\[\]{};:>,]/,
		    closeOnUnfocus: true,
		    completeOnSingleClick: false,
		    container: null,
		    customKeys: null,
		    extraKeys: null
		},
		smartIndent: true,
		mode: lang,
		extraKeys: {"Ctrl-Space": "autocomplete"},
		lineNumbers: ("true" == ln),
		readOnly: ("true" == ro)
	});
	ed.codemirror.setSize (ed.getAttribute("width"), ed.getAttribute("height"));
	ed.codemirror.on ("change", function() {
			var req = {uuid: ed.id, cmd: "onChange", data : [ed.codemirror.getDoc().getValue()]};
			zkau.send (req, 5);
		});
	ed.codemirror.on ("focus", function() {
		ed.codemirror.refresh();
	});

	window.addEventListener("resize", function() {
		ed.codemirror.setSize(ed.offsetWidth, ed.offsetHeight);
		ed.codemirror.refresh();
	});
	
};

zkCodemirror.refresh = function (ed) {
	ed.codemirror.refresh();
};


zkCodemirror.cleanup = function (ed) {
};

/** Called by the server to set the attribute. */
zkCodemirror.setAttr = function (ed, name, value) {
	switch (name) {
	case "value":
		ed.codemirror.doc.setValue(value);
		return true;;
	}
	return false;
};

