/** 
 * ZK port of Codemirror - Real Time Syntax Highlighting Editor written in JavaScript - http://codemirror.net/
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the 
 * GNU Lesser General Public License as published by the Free Software Foundation.
 * 
 * Read the full licence: http://www.opensource.org/licenses/lgpl-license.php
 */

zkCkeditor = {};

zkCkeditor.init = function (ed) {
  ed.value = ed.getAttribute("value");
  if (ed.value == undefined) ed.value = "";
  zkCkeditor.refresh(ed);
};


zkCkeditor.refresh=function(ed) {
	ed.editor = null;
	ClassicEditor
        .create( ed )
        .then ( newEditor => { 
        	ed.editor = newEditor;
        	ed.editor.setData (ed.value);
        	ed.editor.model.document.on( 'change:data', () => {
				var req = {uuid: ed.id, cmd: "onChange", data : [ed.editor.getData()], ignorable: true};
				zkau.send (req, 5);
			});
        } )
        .catch( error => {
            console.error( error );
        } );
}

zkCkeditor.onSize=function(ed) {
}

zkCkeditor.cleanup = function (ed) {
};

/** Called by the server to set the attribute. */
zkCkeditor.setAttr = function (ed, name, value) {
	switch (name) {
	case "value":
		ed.value=value;
		ed.editor.setData (value);
		return true;;
	}
	return false;
};

