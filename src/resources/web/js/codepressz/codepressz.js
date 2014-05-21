/** 
 * ZK port of CodePress - Real Time Syntax Highlighting Editor written in JavaScript - http://codepress.org/
 * 
 * Copyright (C) 2008 Thomas Mueller <thomas.mueller@empego.net>
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the 
 * GNU Lesser General Public License as published by the Free Software Foundation.
 * 
 * Read the full licence: http://www.opensource.org/licenses/lgpl-license.php
 */

zkCodepress = {};

zkCodepress.init = function (ed) {

	s = document.getElementsByTagName('script');
	for(var i=0,n=s.length;i<n;i++) {
		if(s[i].src.match('codepress.js')) {
			CodePress.path = s[i].src.replace('codepress.js','');
		}
	}

	id = ed.id;
	ed.id += '_cp';

	eval(id +'= new CodePress(ed)');
	zkCodepress.ifrm = eval(id);
	zkCodepress.ifrm.id = id;

	ed.parentNode.insertBefore(zkCodepress.ifrm, ed);


	var meta = zkau.getMeta(ed);
	if (meta) {
		zkau.removeOnSend(meta.onsend); //just in case
	} else {
		meta = {
			onsend: function (implicit) {
				//don't send back if implicit (such as onTimer)
				if (!implicit) {
					var cped = zkCodepress.ifrm;
					if (cped) zkCodepress.onblur(cped, true);
				}
			}
		};		
		zkau.setMeta(ed, meta);
	}
	zkau.addOnSend(meta.onsend);

};

zkCodepress.onblur = function (cped, ahead) {

	if(cped.textarea.value == cped.getCode()){
		return;
	}

	var uuid = $uuid(cped.id);
	var comp = $e(uuid);
	var val = cped.getCode();
	var evt = {uuid: uuid, cmd: "onChange", data: [val]};

	if (ahead) zkau.sendAhead(evt);
	else if (!zkCodepress._ahead(evt))
		zkau.send(evt, zkau.asapTimeout(comp, "onChange"));

	cped.textarea.value = cped.getCode()
};

zkCodepress._ahead = function (evt) {
	if (zkau.events) { //3.0 or later
		var es = zkau.events(evt.uuid);
		for (var j = es.length; --j >= 0;)
			if (es[j].ctl) {
				es.splice(j, 0, evt);
				return true;
			}
	}
	return false
};

zkCodepress.cleanup = function (ed) {
	var meta = zkau.getMeta(ed);
	if (meta) zkau.removeOnSend(meta.onsend);
};

/** Called by the server to set the attribute. */
zkCodepress.setAttr = function (ed, name, value) {
	if (zkCodepress.ifrm) {
		switch (name) {
		case "value":
			zkCodepress.ifrm.setCode(value);
			zkCodepress.ifrm.textarea.value = value;
			return true;
		}
	}
	return false;
};