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
	window.addEventListener("resize", function() {
		zkCodemirror.refresh(ed);
	});
	
	// Detect visibilty
	ed.__hidden = true;
	
	if (true)
		new ResizeSensor(ed, function ()  { zkCodemirror.refresh(ed); });
	else {
	try {
		var observer = new IntersectionObserver((entries, observer) => {
			entries.forEach(entry => {
				if (entry.intersectionRatio > 0) {
					if (ed.__hidden) {
						window.setTimeout (() => { zkCodemirror.refresh(ed); }, 100);
//						window.setTimeout (() => { zkCodemirror.refresh(ed); }, 1000);
						ed.__hidden = false;
					}
				} else if (!ed.__hidden) {
					ed.__hidden = true;
				}
			});
		}, {root: document.documentElement});
		
		zkCodemirror.refresh(ed);

		observer.observe(ed);
	} catch (e) {
		// Not available in IE
	}
	}
};


zkCodemirror.refresh=function(ed) {
	ed.codemirror = null;
	var v = ed.firstElementChild
	while (v != null) {
		var v2 = v.nextElementSibling;
		v.remove();
		v = v2;
	}
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
			var req = {uuid: ed.id, cmd: "onChange", data : [ed.codemirror.getDoc().getValue()], ignorable: true};
			zkau.send (req, 5);
		});
	ed.codemirror.on ("focus", function() {
		ed.codemirror.refresh();
	});

}

zkCodemirror.onSize=function(ed) {
	if (ed.codemirror)
		ed.codemirror.refresh();
}

zkCodemirror.cleanup = function (ed) {
};

/** Called by the server to set the attribute. */
zkCodemirror.setAttr = function (ed, name, value) {
	switch (name) {
	case "value":
		ed.value=value;
		ed.codemirror.doc.setValue(value);
		setTimeout( function() {zkCodemirror.refresh(ed)},100);
		return true;;
	}
	return false;
};

