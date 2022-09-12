zk.load("zul.datasource"); 
zk.load("zul.switch"); 
zk.load("zul.db"); 

zkDataText={};
zkDataPassword={};
zkDataSeparator={};
zkDataList={};
zkDataCommon={};
zkDataDate={};
zkDataNameDescription={};
zkDataDescription={};
zkDataSwitch={};
zkDataImage={};
zkDataHtml={};
zkDataBinary={};
/********************* TEXT ELEMENT ********************/
zkDataSeparator.addElement = function(e, parent, pos) {
	parent.classList.add("separator")
}

zkDataSeparator.init = function (e) {
	e.addInputElement = zkDataSeparator.addElement;
	zkDataCommon.init(e);
};

zkDataSeparator.cleanup = zkDataSeparator.onHide = function(B) {
    var A = $real(B);
    if (A) {
        zkVld.closeErrbox(A.id, true)
    }
	zkDataCommon.cleanup(B);
}

zkDataSeparator.setAttr = function (ed, name, value) {
	return zkDataCommon.setAttr(ed,name,value);
}

/********************* TEXT ELEMENT ********************/
zkDataText.addElement = function(e, parent, pos) {
	var i = document.createElement(e.disabled && e.readOnly ? "div" : e.multiline ? "textarea": "input");
	if (e.getAttribute("number") == 'true')
		i.setAttribute("type", "number");
	else
		i.setAttribute("type", "text");
		
	if (e.multiline && e.getAttribute("rows") != null)
		i.setAttribute("rows", e.getAttribute("rows"));
		
	if (e.required && !e.readOnly && !e.disabled)
		i.setAttribute("class", "text required");
	else
		i.setAttribute("class", "text");
	
	if (e.getAttribute("maxlength"))
		i.setAttribute("maxlength", e.getAttribute("maxlength"));
	if (e.getAttribute("readonly") != null) {
		i.setAttribute("readonly", e.getAttribute("readonly"));
        zk.addClass(i, "readonly");
	} else if (e.disabled) {
		i.setAttribute("disabled", e.disabled);
        zk.addClass(i, "text-disd");
        zk.addClass(i, "disabled");
	}
	else if (e.getAttribute("placeholder") != null)
		i.setAttribute("placeholder", e.getAttribute("placeholder"));
	else if (e.getAttribute("label") != null)
		i.setAttribute("placeholder", e.getAttribute("label"));

	i.setAttribute("id", parent.id+"!input");
	if (e.getAttribute("maxlength"))
		i.maxlen=e.getAttribute("maxlength");
	if (e.getAttribute("maxlength")) {
		i.style.maxWidth = (Number(e.getAttribute("maxlength"))+2)+"ch";
	}
	i.position = pos;
	i.databox = e;
	if (e.multivalue) {
		if (pos < e.value.length) i.defaultValue = i.value = e.value[pos];
	} else {
		i.defaultValue = i.value = e.value;
	}
	if (e.disabled && e.readOnly) {
		if (i.value==null || i.value=="")
			i.innerHTML="&nbsp;";
		else
			i.innerText = i.value;
	}
	parent.appendChild(i);
	
	if (e.getAttribute("selecticon") && (e.getAttribute("forceSelectIcon") != null || !e.readOnly && !e.disabled)) {
		var b = document.createElement("button");
		b.setAttribute("type", "button");
		b.setAttribute("class", "icon-button");
		b.position = pos;
		b.databoxid = e.getAttribute("id");
		parent.appendChild(b);
		zk.listen(b, "click", zkDataCommon.onOpenSelect);
		var s = document.createElement("img");
		s.setAttribute("class","selecticon")
		s.setAttribute("src", e.getAttribute("selecticon"))
		b.appendChild(s);
		if (e.getAttribute("selecticon2") != null) {
			s.classList.add("menu2std");
			s = document.createElement("img");
			s.setAttribute("class","selecticon menu2rev")
			s.setAttribute("src", e.getAttribute("selecticon2"))
			b.appendChild(s);
		}
	}

	zkDataCommon.createRemoveIcon(e, parent, pos);
	zkDataCommon.registerInput (e, i);
	/* add event listeners */
    zk.listen(i, "focus", zkDataCommon.onfocus);
    zk.listen(i, "blur", zkDataCommon.onblur);
    zk.listen(i, "select", zkDataCommon.onselect);
    zk.listen(i, "keydown", zkDataCommon.onkeydown);
    zk.listen(i, "input", zkDataCommon.oninput);
    
    /* set css class */
    if (e.readOnly && e.disabled) { // DIV must not be styled
    } else {
    	if (e.readOnly || e.disabled) {
	        zk.addClass(i, "readonly")
	        i.setAttribute("readonly","readonly")
	    }
	    if (e.disabled) {
	        zk.addClass(i, "text-disd")
	        i.setAttribute("disabled","disabled")
	    }
    }
}

zkDataText.init = function (e) {
	e.addInputElement = zkDataText.addElement;
	zkDataCommon.init(e);
};

zkDataText.cleanup = zkDataText.onHide = function(B) {
    var A = $real(B);
    if (A) {
        zkVld.closeErrbox(A.id, true)
    }
	zkDataCommon.cleanup(e);
}

zkDataText.setAttr = function (ed, name, value) {
	return zkDataCommon.setAttr(ed,name,value);
}

/********************* PASSWORD ELEMENT ********************/
zkDataPassword.addElement = function(e, parent, pos) {
	var f = document.createElement("form");
	parent.appendChild(f);
	f.setAttribute("onsubmit", "return false;");
	
	var i = document.createElement("input");
	if (e.required  && !e.readOnly && !e.disabled)
		i.setAttribute("class", "text required");
	else
		i.setAttribute("class", "text");
	i.setAttribute("type", "password");
	
	if (e.getAttribute("readonly") != null) {
		i.setAttribute("readonly", e.getAttribute("readonly"));
        zk.addClass(i, "readonly");
	} else if (e.disabled) {
		i.setAttribute("disabled", e.disabled);
        zk.addClass(i, "text-disd");
        zk.addClass(i, "disabled");
	}
	else if (e.getAttribute("placeholder") != null)
		i.setAttribute("placeholder", e.getAttribute("placeholder"));
	else if (e.getAttribute("label") != null)
		i.setAttribute("placeholder", e.getAttribute("label"));

	i.setAttribute("autocomplete", "new-password");
	i.setAttribute("id", parent.id+"!input");
	if (e.getAttribute("maxlength"))
		i.maxlen=e.getAttribute("maxlength");
	if (e.getAttribute("maxlength")) {
		i.style.maxWidth = (Number(e.getAttribute("maxlength"))+2)+"ch";
	}
	i.position = pos;
	i.databox = e;
	if (e.multivalue) {
		if (pos < e.value.length) i.defaultValue = i.value = e.value[pos];
	} else {
		i.defaultValue = i.value = e.value;
	}
	f.appendChild(i);
	
	zkDataCommon.createRemoveIcon(e, parent, pos);
	zkDataCommon.registerInput (e, i);
	/* add event listeners */
    zk.listen(i, "focus", zkDataCommon.onfocus);
    zk.listen(i, "blur", zkDataCommon.onblur);
    zk.listen(i, "select", zkDataCommon.onselect);
    zk.listen(i, "keydown", zkDataCommon.onkeydown);
    zk.listen(i, "input", zkDataCommon.oninput);
    
    /* set css class */
    if (e.readOnly) {
        zk.addClass(i, "readonly")
        i.setAttribute("readonly","readonly")
    }
    if (e.disabled) {
        zk.addClass(i, "text-disd")
        i.setAttribute("disabled","disabled")
    }
}

zkDataPassword.init = function (e) {
	e.addInputElement = zkDataPassword.addElement;
	zkDataCommon.init(e);
};

zkDataPassword.cleanup = zkDataPassword.onHide = zkDataText.cleanup;

zkDataPassword.setAttr = zkDataText.setAttr;
/********************* LISTBOX ELEMENT ********************/
zkDataList.addElement = function(e, parent, pos) {
	var sel = document.createElement("select");
	if (e.required && !e.readOnly && !e.disabled)
		sel.setAttribute("class", "select required");
	else
		sel.setAttribute("class", "select");
	sel.setAttribute("id", parent.id+"!input");
	sel.position = pos;
	sel.databox = e;
	var value;
	if (e.multivalue) {
		if (pos < e.value.length) value = e.value[pos];
	} else {
		value = e.value;
	}
	
	parent.appendChild(sel);

	var options = JSON.parse(e.getAttribute("values"));
	if (e.getAttribute("required") == null || e.getAttribute("required") == "false") {
		var op = document.createElement("option");
		if ( sel.value == null || sel.value == "")
			op.setAttribute("selected", "selected");
		sel.appendChild(op);
	}
	for (var i = 0; i < options.length; i++) {
		var option = options[i];
		var label, key;
		var split = option.indexOf(":");
		if (split >= 0) {
			key = option.substring(0,split).trim();
			key = decodeURIComponent(key.replace(/\+/g, " "));
			label = option.substring(split+1).trim();
		} else {
			key = label = option;
		}
		var op = document.createElement("option");
		op.setAttribute("value", key);
		op.innerText = label;
		if ( key == value)
			op.setAttribute("selected", "selected");
		sel.appendChild(op);
	}

	zkDataCommon.createRemoveIcon(e, parent, pos);
	zkDataCommon.registerInput (e, sel);
	/* add event listeners */
    zk.listen(sel, "input", zkDataList.onselect);
    
    /* set css class */
    if (e.readOnly || e.disabled) {
        zk.addClass(sel, "readonly")
        sel.setAttribute("disabled","disabled")
    }
	else if (e.getAttribute("placeholder") != null)
		sel.setAttribute("placeholder", e.getAttribute("placeholder"));
}

zkDataList.init = function (e) {
	e.addInputElement = zkDataList.addElement;
	zkDataCommon.init(e);
};

zkDataList.cleanup = zkDataList.onHide = function(B) {
    var A = $real(B);
    if (A) {
        zkVld.closeErrbox(A.id, true)
    }
	zkDataCommon.cleanup(e);
}

zkDataList.setAttr = function (ed, name, value) {
	return zkDataCommon.setAttr(ed,name,value);
}

zkDataList.onselect = function(ev) {
	var el = ev.currentTarget;
	zkDataCommon.onupdate(el, el.databox, false);
	zkDataCommon.oninput (ev);
}

/********************* SWITCH ********************/
zkDataSwitch.addElement = function(e, parent, pos) {
	parent.position = pos;
	parent.classList.add("switch");
	parent.databoxid = e.getAttribute("id");
	
	var on = document.createElement("span")
	on.innerText = e.getAttribute("onLabel");
	on.setAttribute("class", "on");
	parent.appendChild(on);
	var off = document.createElement("span");
	off.innerText = e.getAttribute("offLabel");
	off.setAttribute("class", "off");
	parent.appendChild(off);
	var slider = document.createElement("span");
	slider.zkSwitch = parent; 
	slider.setAttribute("id",parent.getAttribute("id")+"!slider");
	slider.setAttribute("class", "slider not-dragging");
	parent.appendChild(slider);
	zk.listen(slider, "mousedown", zkSwitch.onSliderMouseDown);

	var handle = document.createElement("span");
	handle.setAttribute("class", "handle");
	slider.appendChild(handle);

	parent.disabled = e.readOnly;
	if (parent.disabled)
		parent.classList.add("disabled");
	else
		parent.classList.remove("disabled");

	/* add event listeners */
    zk.listen(parent, "click", zkDataSwitch.onClick);
    
	if (e.multivalue) {
		if (pos < e.value.length) parent.checked = e.value[pos] && e.value[pos] != "false";
	} else {
		parent.checked = e.value  && e.value != "false";
	}
	zkDataSwitch.syncSlider(parent);
	zkDataCommon.registerInput (e, parent);
}

zkDataSwitch.syncSlider = function (s) {
	var slider = document.getElementById(s.id+"!slider");
	slider.style.position = "absolute";
	slider.style.left = "";
	slider.classList.remove(s.checked?"slider-off":"slider-on");
	slider.classList.add(s.checked?"slider-on":"slider-off");
}

zkDataSwitch.onClick = function (ev) {
	var s = ev.currentTarget;
	if (! s.disabled)
	{
		var slider = document.getElementById(s.id+"!slider");
		zkDataSwitch.slider = slider;
		document.removeEventListener("mousemove", zkDataSwitch.onSliderMousemove);
		document.addEventListener("mouseup", zkDataSwitch.onSliderMouseup);
		if (slider.classList.contains("dragging"))
		{
			slider.classList.add("not-dragging");
			slider.classList.remove("dragging");
		} else {
			s.checked = ! s.checked;
		}
		var dbi = s.databoxid;
		var databox=$e(dbi);
		if (databox.multivalue) {
			databox.value[s.position] = s.checked;
		} else {
			databox.value = s.checked;
		}
	    zkau.sendasap({
	        uuid: $uuid(databox),
	        cmd: "onChange",
	        data: [s.checked, false, s.checked, s.position]
	    }, zk.delayTime_onChange ? zk.delayTime_onChange : 150)
		zkDataSwitch.syncSlider(s);
		zkDatasource.updatedElement(s);
	}
}

zkDataSwitch.onSliderMouseDown=function (ev) {
	var slider = ev.currentTarget;
	var s = slider.zkSwitch;
	if ( !s.disabled)
	{
		if (slider.classList.contains("not-dragging"))
		{
			zkDataSwitch.slider = slider;
			zkDataSwitch.initialX = ev.clientX;
			if ( s.checked ) zkDataSwitch.initialX -= slider.clientWidth;
			document.addEventListener("mousemove", zkDataSwitch.onSliderMousemove);
			document.addEventListener("mouseup", zkDataSwitch.onSliderMouseup);
		}
	}
}

zkDataSwitch.onSliderMousemove=function (ev) {
	var slider = zkDataSwitch.slider;
	slider.classList.add("dragging");
	slider.classList.remove("not-dragging");
	var s = slider.zkSwitch;
	var x = ev.clientX - zkDataSwitch.initialX;
	x = x - slider.clientLeft;
	if (x < 0) x = 0;
	var rect = s.getBoundingClientRect();
	if (x > slider.clientWidth) x = slider.clientWidth;
	slider.style.left = new String(x) + "px";
	s.checked = x > slider.clientWidth / 2;
	zkDatasource.updatedElement(s);
}

zkDataSwitch.onSliderMouseup=function (ev) {
	var s = zkDataSwitch.slider.zkSwitch;
	document.removeEventListener("mousemove", zkDataSwitch.onSliderMousemove);
	document.removeEventListener("mouseup", zkDataSwitch.onSliderMouseup);
}

zkDataSwitch.init = function (e) {
	e.addInputElement = zkDataSwitch.addElement;
	zkDataCommon.init(e);
};

zkDataSwitch.cleanup = zkDataSwitch.onHide = function(B) {
    var A = $real(B);
    if (A) {
        zkVld.closeErrbox(A.id, true)
    }
	zkDataCommon.cleanup(e);
}

zkDataSwitch.setAttr = function (ed, name, value) {
	return zkDataCommon.setAttr(ed,name,value);
}



/********************* NAME_DESCRIPTION ****************/
zkDataNameDescription.addElement = function(e, parent, pos) {
	var rowValue = e.multivalue ? (e.value != null && pos < e.value.length? e.value[pos]: null): e.value;
	if (rowValue == null) rowValue = ["",""];
	
	var i = document.createElement("input");
	if (e.required && !e.readOnly && !e.disabled)
		i.setAttribute("class", "name required");
	else
		i.setAttribute("class", "name");


	i.setAttribute("id", parent.id+"!input");
	if (e.getAttribute("maxlength"))
		i.maxlen=e.getAttribute("maxlength");
	if (e.getAttribute("maxlength")) {
		i.style.maxWidth = (Number(e.getAttribute("maxlength"))+2)+"ch";
	}
	i.position = pos;
	i.databox = e;
	i.defaultValue = i.value = rowValue[0];
	
	parent.appendChild(i);
	
	if (e.getAttribute("selecticon") && !e.readOnly && !e.disabled) {
		var b = document.createElement("button");
		b.setAttribute("type", "button");
		b.setAttribute("class", "icon-button");
		b.position = pos;
		b.databoxid = e.getAttribute("id");
		parent.appendChild(b);
		zk.listen(b, "click", zkDataCommon.onOpenSelect);
		var s = document.createElement("img");
		s.setAttribute("class","selecticon")
		s.setAttribute("src", e.getAttribute("selecticon"))
		b.appendChild(s);
		if (e.getAttribute("selecticon2") != null) {
			s.classList.add("menu2std");
			s = document.createElement("img");
			s.setAttribute("class","selecticon menu2rev")
			s.setAttribute("src", e.getAttribute("selecticon2"))
			b.appendChild(s);
		}
	}
	
	var l = document.createElement("span");
	if (e.hyperlink)
		l.innerHTML = rowValue[1];
	else
		l.innerText = rowValue[1];
	l.setAttribute("id", parent.id+"!Label");
	parent.appendChild(l);
	
	
	zkDataCommon.createRemoveIcon(e, parent, pos);
	zkDataCommon.registerInput (e, i);
	/* add event listeners */
    zk.listen(i, "focus", zkDataCommon.onfocus);
    zk.listen(i, "blur", zkDataCommon.onblur);
    zk.listen(i, "select", zkDataCommon.onselect);
    zk.listen(i, "keydown", zkDataCommon.onkeydown);
    zk.listen(i, "input", zkDataCommon.oninput);
    zk.listen(i, "input", zkDataNameDescription.oninput);
    
    /* set css class */
    if (e.readOnly) {
        zk.addClass(i, "readonly")
        i.setAttribute("readonly","readonly")
    }
    if (e.disabled) {
        zk.addClass(i, "text-disd")
        i.setAttribute("disabled","disabled")
    }
	if (!e.readOnly && !e.disabled) {
		if (e.getAttribute("placeholder") != null)
			i.setAttribute("placeholder", e.getAttribute("placeholder"));
		else if (e.getAttribute("label") != null)
			i.setAttribute("placeholder", e.getAttribute("label"));		
	}
    
    if (rowValue[2] && rowValue[2].length > 0) {
    	var w = document.getElementById($uuid(parent)+"!Warning");
    	if (w)
    		w.style.display = "";
    	var wl = document.getElementById($uuid(parent)+"!WarningLabel");
    	if (wl)
    		wl.innerText = rowValue[2];
	} 
}

zkDataNameDescription.setDescription = function(e, pos, description) {
	var id;
	if (e.multivalue) {
		if (e.value[pos] != null)
			e.value[pos][1] = description;
		id = e.getAttribute("id")+"_"+pos; 
	} else if (e.value != null) {
		e.value[1] = description;
		id = e.getAttribute("id")+"_container"; 
	}
	var label = document.getElementById(id+"!Label");
	if (label) {
		if (e.hyperlink)
			label.innerHTML = description;
		else
			label.innerText = description;
	}
}

zkDataNameDescription.init = function (e) {
	e.addInputElement = zkDataNameDescription.addElement;
	e.useDescription = false;
	e.useNameDescription = true;
	e.hyperlink = e.getAttribute("hyperlink") != null;
	zkDataCommon.init(e);
};

zkDataNameDescription.cleanup = zkDataNameDescription.onHide = function(B) {
    var A = $real(B);
    if (A) {
        zkVld.closeErrbox(A.id, true)
    }
	zkDataCommon.cleanup(e);
}

zkDataNameDescription.setAttr = function (ed, name, value) {
	return zkDataCommon.setAttr(ed,name,value);
}

zkDataNameDescription.oninput = function (evt) {
	var i = Event.element(evt);
	var databox = i.databox;
	zkDataCommon.openSearchPopup(i, databox);
}

zkDataNameDescription.onContinueSearchResponse = function(ed, id, data) {
	var div = document.getElementById(id);
	if (div) {
		var data = JSON.parse(data);
		zkDataCommon.addSearchResult(div, data, false);
	}
}

zkDataNameDescription.onStartSearchResponse = function(ed, id, data) {
	var div = document.getElementById(id);
	if (div) {
		var data = JSON.parse(data);
		zkDataCommon.addSearchResult(div, data, true);
	}
}

zkDataNameDescription.onEndSearchResponse = function(ed, id, msg) {
	var div = document.getElementById(id);
	if (div) {
		if (div.childElementCount == 2) {
 			div.firstElementChild.style.display = "block";
 			div.firstElementChild.innerText = msg;
 		}

		div.lastElementChild.style.display="none";
	}
}

/********************* DESCRIPTION ****************/
zkDataDescription.addElement = function(e, parent, pos) {
	var rowValue = e.multivalue ? (pos < e.value.length? e.value[pos]: ""): e.value;
	var i = document.createElement("input");
	if (e.required && !e.readOnly && !e.disabled)
		i.setAttribute("class", "description required");
	else
		i.setAttribute("class", "description");
	
	if (e.getAttribute("readonly") != null) {
		i.setAttribute("readonly", e.getAttribute("readonly"));
        zk.addClass(i, "readonly");
	} else if (e.disabled) {
		i.setAttribute("disabled", e.disabled);
        zk.addClass(i, "text-disd");
        zk.addClass(i, "disabled");
	}
	else if (e.getAttribute("placeholder") != null)
		i.setAttribute("placeholder", e.getAttribute("placeholder"));
	else if (e.getAttribute("label") != null)
		i.setAttribute("placeholder", e.getAttribute("label"));

	i.setAttribute("id", parent.id+"!input");
	if (e.getAttribute("maxlength"))
		i.maxlen=e.getAttribute("maxlength");
	if (e.getAttribute("maxlength")) {
		i.style.maxWidth = (Number(e.getAttribute("maxlength"))+2)+"ch";
	}
	i.position = pos;
	i.databox = e;
	var rowValue = e.multivalue ? (pos < e.value.length? e.value[pos]: ""): e.value;
	i.defaultValue = i.actualValue = rowValue == null? null: rowValue[0];
	i.value = rowValue == null ? null: rowValue[1];
	
	parent.appendChild(i);
	
	if (e.getAttribute("selecticon") &&  !e.readOnly && !e.disabled) {
		var b = document.createElement("button");
		b.setAttribute("type", "button");
		b.setAttribute("class", "icon-button");
		b.position = pos;
		b.databoxid = e.getAttribute("id");
		parent.appendChild(b);
		zk.listen(b, "click", zkDataCommon.onOpenSelect);
		var s = document.createElement("img");
		s.setAttribute("class","selecticon")
		s.setAttribute("src", e.getAttribute("selecticon"))
		b.appendChild(s);
		if (e.getAttribute("selecticon2") != null) {
			s.classList.add("menu2std");
			s = document.createElement("img");
			s.setAttribute("class","selecticon menu2rev")
			s.setAttribute("src", e.getAttribute("selecticon2"))
			b.appendChild(s);
		}
	}
	
	zkDataCommon.createRemoveIcon(e, parent, pos);
	zkDataCommon.registerInput (e, i);
	/* add event listeners */
    zk.listen(i, "focus", zkDataCommon.onfocus);
    zk.listen(i, "blur", zkDataCommon.onblur);
    zk.listen(i, "select", zkDataCommon.onselect);
    zk.listen(i, "keydown", zkDataCommon.onkeydown);
    zk.listen(i, "input", zkDataDescription.oninput);
    
    /* set css class */
    if (e.readOnly) {
        zk.addClass(i, "readonly")
        i.setAttribute("readonly","readonly")
    }
    if (e.disabled) {
        zk.addClass(i, "text-disd")
        i.setAttribute("disabled","disabled")
    }
	if (!e.disabeld && !e.readOnly) {
		if (e.getAttribute("placeholder") != null)
			i.setAttribute("placeholder", e.getAttribute("placeholder"));
		else if (e.getAttribute("label") != null)
			i.setAttribute("placeholder", e.getAttribute("label"));
		
	}
    if (rowValue != null && rowValue[2] != null && rowValue[2].length > 0) {
    	var w = document.getElementById($uuid(parent)+"!Warning");
    	if (w)
    		w.style.display = "";
    	var wl = document.getElementById($uuid(parent)+"!WarningLabel");
    	if (wl)
    		wl.innerText = rowValue[2];
	} 
}

zkDataDescription.setDescription = function(e, pos, description) {
}

zkDataDescription.init = function (e) {
	e.addInputElement = zkDataDescription.addElement;
	e.useDescription = true;
	e.useNameDescription = false;
	zkDataCommon.init(e);
};

zkDataDescription.cleanup = zkDataDescription.onHide = function(B) {
    var A = $real(B);
    if (A) {
        zkVld.closeErrbox(A.id, true)
    }
	zkDataCommon.cleanup(e);
}

zkDataDescription.setAttr = function (ed, name, value) {
	return zkDataCommon.setAttr(ed,name,value);
}

zkDataDescription.oninput = function (evt) {
	var i = Event.element(evt);
	var databox = i.databox;
	i.actualValue = "";
	zkDataCommon.openSearchPopup(i, databox);
}

zkDataDescription.onStartSearchResponse = function(ed, id, data) {
	var div = document.getElementById(id);
	if (div) {
		var data = JSON.parse(data);
		zkDataCommon.addSearchResult(div, data, true);
	}
}

zkDataDescription.onContinueSearchResponse = function(ed, id, data) {
	var div = document.getElementById(id);
	if (div) {
		var data = JSON.parse(data);
		zkDataCommon.addSearchResult(div, data, false);
	}
}

zkDataDescription.onEndSearchResponse = function(ed, id, msg) {
	var div = document.getElementById(id);
	if (div) {
		if (div.childElementCount == 2) {
 			div.firstElementChild.style.display = "block";
 			div.firstElementChild.innerText = msg;
 		}
		div.lastElementChild.style.display="none";
	}
}
/********************* DATE ELEMENT ********************/
zkDataDate.addElement = function(e, parent, pos) {
	parent.classList.add("datebox");
	// input
	var i = document.createElement("input");
	i.setAttribute("class", "dateboxinp");
	i.position = pos;
	i.databox = e;

	if (e.getAttribute("readonly") != null) {
		i.setAttribute("readonly", e.getAttribute("readonly"));
        zk.addClass(i, "readonly");
	} else if (e.disabled) {
		i.setAttribute("disabled", e.disabled);
        zk.addClass(i, "text-disd");
        zk.addClass(i, "disabled");
	}
	else if (e.getAttribute("placeholder") != null)
		i.setAttribute("placeholder", e.getAttribute("placeholder"));
	else if (e.getAttribute("label") != null)
		i.setAttribute("placeholder", e.getAttribute("label"));

	i.setAttribute("id", parent.id+"!real");
	if (e.multivalue) {
		if (pos < e.value.length) i.defaultValue = i.value = e.value[pos];
	} else {
		i.defaultValue = i.value = e.value;
	}
	parent.setAttribute("z.fmt", e.getAttribute("z.fmt"));
	parent.appendChild(i);
	
	// button
	if (! e.readOnly && !e.disabled) {
		var span = document.createElement("span")
		span.setAttribute("class", "zbtnbk");
		parent.appendChild(span);
		var b = document.createElement("img");
		b.setAttribute("id", parent.id+"!btn");
		b.setAttribute("src", e.getAttribute("calendaricon"));
		span.appendChild(b);
		zk.listen(b, "click", function (evt) {if (!i.disabled && !zk.dragging) zkDataDate.onbutton(parent, evt);});
		
		var dd = document.createElement("div");
		dd.setAttribute("class", "dateboxpp");
		dd.setAttribute("id", parent.id+"!pp");
		dd.style.display = "none";
		dd.setAttribute("tabindex", "-1");
		parent.appendChild(dd);
		zk.listen(dd, "click", zkDataDate.closepp);
	}
	zkDataCommon.createRemoveIcon(e, parent, pos);
	zkDataCommon.registerInput (e, i);
	
	/* add event listeners */
//	zk.listen(i, zk.ie ? "keydown": "keypress", zkDataDate.onkey);
		//IE: use keydown. otherwise, it causes the window to scroll
	zk.listen(i, "focus", zkDataCommon.onfocus);
    zk.listen(i, "blur", zkDataCommon.onblur);
    zk.listen(i, "select", zkDataCommon.onselect);
    zk.listen(i, "keydown", zkDataCommon.onkeydown);
    zk.listen(i, "input", zkDataCommon.oninput);
    
}

zkDataDate.cleanup = zkDataDate.onHide = function(B) {
    var A = $real(B);
    if (A) {
        zkVld.closeErrbox(A.id, true)
    }
	zkDataCommon.cleanup(e);
}


zkDataDate.init = function (cmp) {
	cmp.addInputElement = zkDataDate.addElement;
	zkDataCommon.init(cmp);
};

zkDataDate.validate = function (cmp) {
	var inp = $e(cmp.id+"!real");
	if (inp.value) {
		var fmt = getZKAttr(cmp, "fmt");
		var d = zk.parseDate(inp.value, fmt, getZKAttr(cmp, "lenient") == "false");
		if (!d) return msgzul.DATE_REQUIRED+fmt;

		inp.value = zk.formatDate(d, fmt); //meta might not be ready
	}
	return null;
};

/** Handles setAttr. */
zkDataDate.setAttr = function (cmp, nm, val) {
	if ("z.fmt" == nm) {
		zkau.setAttr(cmp, nm, val);

		var inp = $real(cmp);
		if (inp) {
			var d = zk.parseDate(inp.value, val);
			if (d) inp.value = zk.formatDate(d, val);
		}
		return true;
	} else if ("style" == nm) {
		var inp = $real(cmp);
		if (inp) zkau.setAttr(inp, nm, zk.getTextStyle(val, true, true));
	} else if ("style.width" == nm) {
		var inp = $real(cmp);
		if (inp) {
			inp.style.width = val;
			return true;
		}
	} else if ("style.height" == nm) {
		var inp = $real(cmp);
		if (inp) {
			inp.style.height = val;
			return true;
		}
	} else if ("z.sel" == nm ) {
		return zkTxbox.setAttr(cmp, nm, val);
	} else if ("z.btnVisi" == nm) {
		var btn = $e(cmp.id + "!btn");
		if (btn) btn.style.display = val == "true" ? "": "none";
		return true;
	}
	return zkDataCommon.setAttr(cmp,nm,val);
};
zkDataDate.rmAttr = function (cmp, nm) {
	if ("style" == nm) {
		var inp = $real(cmp);
		if (inp) zkau.rmAttr(inp, nm);
	} else if ("style.width" == nm) {
		var inp = $real(cmp);
		if (inp) inp.style.width = "";
	} else if ("style.height" == nm) {
		var inp = $real(cmp);
		if (inp) inp.style.height = "";
	}
	zkau.rmAttr(cmp, nm);
	return true;
};

zkDataDate.onkey = function (evt) {
	var inp = Event.element(evt);
	if (!inp) return true;

	var uuid = $uuid(inp.id);
	var pp = $e(uuid + "!pp");
	if (!pp) return true;

	var opened = $visible(pp);
	if (Event.keyCode(evt) == 9) { //TAB; IE: close now to show covered SELECT
		if (opened) zkDataDate.close(pp);
		return true; //don't eat
	}

	if (Event.keyCode(evt) == 38 || Event.keyCode(evt) == 40) {//UP/DN
		if (evt.altKey) {
			if (Event.keyCode(evt) == 38) { //UP
				if (opened) zkDataDate.close(pp);
			} else {
				if (!opened) zkDataDate.open(pp);
			}
			//FF: if we eat UP/DN, Alt+UP degenerate to Alt (select menubar)
			if (zk.ie) {
				Event.stop(evt);
				return false;
			}
			return true;
		}
		if (!opened) {
			zkDataDate.open(pp);
			Event.stop(evt);
			return false;
		}
	}

	if (opened) {
		var meta = zkau.getMeta(uuid);
		if (meta) {
			//Request 1551019: better responsive
			if (Event.keyCode(evt) == 13) { //ENTER
				meta.onchange();
				return true;
			}

			var ofs = Event.keyCode(evt) == 37 ? -1: Event.keyCode(evt) == 39 ? 1:
				Event.keyCode(evt) == 38 ? -7: Event.keyCode(evt) == 40 ? 7: 0;
			if (ofs) {
				meta.shift(ofs);
				inp.value = meta.getDateString();
				zk.asyncSelect(inp.id);
				Event.stop(evt);
				return false;
			}
		}
	}
	return true;
};

/* Whn the button is clicked on button. */
zkDataDate.onbutton = function (cmp, evt) {
	var pp = $e(cmp.id + "!pp");
	if (pp) {
		if (!$visible(pp)) zkDataDate.open(pp);
		else zkDataDate.close(pp, true);

		if (!evt) evt = window.event; //Bug 1911864
		Event.stop(evt);
	}
};
zkDataDate.dropdn = function (cmp, dropdown) {
	var pp = $e(cmp.id + "!pp");
	if (pp) {
		if ("true" == dropdown) zkDataDate.open(pp);
		else zkDataDate.close(pp, true);
	}
};

zkDataDate.open = function (pp) {
	pp = $e(pp);
	zkau.closeFloats(pp); //including popups
	zkau._dtbox.setFloatId(pp.id);

	var uuid = $uuid(pp.id);
	var cb = $e(uuid);
	if (!cb) return;

	var meta = zkau.getMeta(cb);
	if (meta) meta.init();
	else zkau.setMeta(cb, new zk.Cal(cb, pp));

	pp.style.width = pp.style.height = "auto";
	pp.style.position = "absolute"; //just in case
	pp.style.overflow = "auto"; //just in case
	pp.style.display = "block";
	pp.style.zIndex = "88000";
	//No special child, so no need to: zk.onVisiAt(pp);

	//FF: Bug 1486840
	//IE: Bug 1766244 (after specifying position:relative to grid/tree/listbox)
	zk.setVParent(pp);	

	//fix size
	if (pp.offsetHeight > 200) {
		pp.style.height = "200px";
		pp.style.width = "auto"; //recalc
	} else if (pp.offsetHeight < 10) {
		pp.style.height = "10px"; //minimal
	}
	if (pp.offsetWidth < cb.offsetWidth) {
		pp.style.width = cb.offsetWidth + "px";
	} else {
		var wd = zk.innerWidth() - 20;
		if (wd < cb.offsetWidth) wd = cb.offsetWidth;
		if (pp.offsetWidth > wd) pp.style.width = wd;
	}

	zk.position(pp, cb, "after-start");

	setTimeout(()=>{zkDataDate._repos(uuid)}, 3);
		//IE issue: we have to re-position again because some dimensions
		//might not be correct here
};
/** Re-position the popup. */
zkDataDate._repos = function (uuid) {
	var cb = $e(uuid);
	if (!cb) return;

	var pp = $e(uuid + "!pp");
	var inpId = cb.id + "!real";
	var inp = $e(inpId);

	zk.position(pp, cb, "after-start");
	zkau.hideCovered();
	zk.asyncFocus(inpId);
};

zkDataDate.close = function (pp, focus) {
	var uuid = $uuid(pp.id);

	pp.style.display = "none";
	zk.unsetVParent(pp);

	pp = $e(pp);
	zkau._dtbox.setFloatId(null);
	//No special child, so no need to: zk.onHideAt(pp);
	zkau.hideCovered();

	if (focus)
		zk.asyncFocus(uuid + "!real");
	
	var el = $e(uuid);
	var databox = $e(el.databoxid);
	var input = $e(uuid+'!real');
	zkDatasource.updatedElement(input);
	zkDataCommon.updatedElement(input);
	zkDataCommon.onupdate(input, databox, true);
	
};
zkDataDate.closepp = function (evt) {
	if (!evt) evt = window.event;
	var pp = Event.element(evt);
	if (pp.onclick) return;
	for (; pp; pp = pp.parentNode) {
		if (pp.id) {
			if (pp.id.endsWith("!pp")) {
				zkDataDate.close(pp, true);
				return; //done
			}
		}
	}
};

zk.FloatDatebox = Class.create();
Object.extend(Object.extend(zk.FloatDatebox.prototype, zk.Float.prototype), {
	_close: function (el) {
		zkDataDate.close(el);
	}
});
if (!zkau._dtbox)
	zkau.floats.push(zkau._dtbox = new zk.FloatDatebox()); //hook to zkau.js

/********************* IMAGE ELEMENT ********************/
zkDataImage.addElement = function(e, parent, pos) {
	var img = document.createElement("img");
	img.setAttribute("id", parent.id+"!input");
	img.setAttribute("class", "dataimage");
	img.position = pos;
	img.databox = e;
	if (e.multivalue) {
		if (pos < e.value.length) img.setAttribute("src", e.value[pos]);
	} else {
		img.setAttribute("src",e.value);
	}
	parent.appendChild(img);

	if (e.getAttribute("selecticon") &&  !e.readOnly && !e.disabled) {
		var b = document.createElement("button");
		b.setAttribute("type", "button");
		b.setAttribute("class", "icon-button");
		b.position = pos;
		b.databoxid = e.getAttribute("id");
		parent.appendChild(b);
		zk.listen(b, "click", zkDataCommon.onOpenSelect);
		var s = document.createElement("img");
		s.setAttribute("class","selecticon")
		s.setAttribute("src", e.getAttribute("selecticon"))
		b.appendChild(s);
		if (e.getAttribute("selecticon2") != null) {
			s.classList.add("menu2std");
			s = document.createElement("img");
			s.setAttribute("class","selecticon menu2rev")
			s.setAttribute("src", e.getAttribute("selecticon2"))
			b.appendChild(s);
		}
	}

	zkDataCommon.createRemoveIcon(e, parent, pos);
	/* add event listeners */
	if ( !e.readOnly && !e.disabled) {
		img.databoxid = e.getAttribute("id");
		zk.listen(img, "click", zkDataCommon.onOpenSelect);
	}
    
    /* set css class */
    if (e.readOnly) {
        zk.addClass(img, "readonly")
        img.setAttribute("readonly","readonly")
    }
    if (e.disabled) {
        zk.addClass(img, "disabled")
        img.setAttribute("disabled","disabled")
    }
}

zkDataImage.init = function (e) {
	e.addInputElement = zkDataImage.addElement;
	zkDataCommon.init(e);
};

zkDataImage.cleanup = zkDataImage.onHide = function(B) {
    var A = $real(B);
	zkDataCommon.cleanup(e);
}

zkDataImage.setAttr = function (ed, name, value) {
	return zkDataCommon.setAttr(ed,name,value);
}

/********************* BINARY ELEMENT ********************/
zkDataBinary.addElement = function(e, parent, pos) {
	var rowValue = e.multivalue ? (pos < e.value.length? e.value[pos]: ""): e.value;

	var span = document.createElement("span");
	span.setAttribute("class", "binary");
	parent.appendChild(span);
	span.innerText = rowValue;

	var b = document.createElement("button");
	b.setAttribute("id", parent.id+"!input");
	b.position = pos;
	b.databox = e;
	var hasValue = true;
	if (e.multivalue) {
		hasValue = pos < e.value.length; 
	} else {
		hasValue = e.value != null; 
	}
	b.setAttribute("type", "button");
	b.setAttribute("class", "icon-button");
	b.databoxid = e.getAttribute("id");
	parent.appendChild(b);

	zk.listen(b, "click", zkDataBinary.onOpenMenu);
	zk.listen(b, "blur", zkDataBinary.onblur);
    
	var s = document.createElement("img");
	s.setAttribute("class","selecticon")
	if (e.disabled ) {
		s.setAttribute("src", e.getAttribute("downloadicon"))
		s.setAttribute("title", e.getAttribute("downloadmessage"));
	}
	else if (hasValue) {
		s.setAttribute("src", e.getAttribute("selecticon"))
	}
	else
	{
		s.setAttribute("src", e.getAttribute("uploadicon"))
		s.setAttribute("title", e.getAttribute("uploadmessage"));
	}
	b.appendChild(s);
    /* set css class */
    if (e.readOnly) {
        zk.addClass(b, "readonly")
        b.setAttribute("readonly","readonly")
    }
    if (e.disabled) {
        zk.addClass(img, "disabled")
        b.setAttribute("disabled","disabled")
    }
//	zkDataCommon.createRemoveIcon(e, parent, pos);
}

zkDataBinary.onblur = function(A) {
    var el = zkau.evtel(A);
    var B = el.databox;
    var C = zkDataCommon._noonblur(el, B);
    zkau.onblur(A, C);
    if (el.popup) {
    	setTimeout(()=>{
    		try{
    			if(el.popup) {
					zkDataCommon.inSearchPopup = false;
    				el.popup.remove(); 
					el.popup=null;
					zkDataCommon.popup = null;
    			}
    		} catch(e) {
    			
    		}
    	}, 150);
    }
}

zkDataBinary.onOpenMenu = function(event) {
	var b = event.currentTarget;
	var position = b.position;
	var e = b.databox;
	
	b.focus();

	var hasValue = true;
	if (e.multivalue) {
		hasValue = position < e.value.length; 
	} else {
		hasValue = e.value != null; 
	}

	if (e.disabled ) {
        zkau.send({
            uuid: e.id,
            cmd: "onDownload",
            data: [b.position]
        }, 100)
	}
	else if (hasValue) {
		zkDataBinary.openMenu(b, e, position);
	}
	else
	{
        zkau.send({
            uuid: e.id,
            cmd: "onUpload",
            data: [b.position]
        }, 100)
	}
}

zkDataBinary.openMenu = function(button, box, position) {
	if (!button.popup) {
		var div = document.createElement("div");
		div.setAttribute("id", button.getAttribute("id")+"-popup")
		div.setAttribute("class", "databox-popup");
		button.popup = div;
		div.input = button;
		var rect = button.getBoundingClientRect();
		var left = rect.left;
		var top = rect.bottom;
		var parent = document.body;
		parent.insertBefore(div, parent.firstElementChild);
		div.style.position="fixed";
		div.style.left = ""+left+"px";
		div.style.top = ""+top+"px";
		zkDataCommon.popup = div;
		zkDataCommon.intervalEvent  = setInterval(zkDataCommon.onScroll, 500);
		div.input = button;
		zkDataCommon.inSearchPopup = true;
		zkDataBinary.addOption(div, box.getAttribute("uploadicon"), box.getAttribute("uploadmessage"), zkDataBinary.uploadOption);
		zkDataBinary.addOption(div, box.getAttribute("downloadicon"), box.getAttribute("downloadmessage"), zkDataBinary.downloadOption);
		zkDataBinary.addOption(div, box.getAttribute("clearicon"), box.getAttribute("clearmessage"), zkDataBinary.clearOption);
	}
}

zkDataBinary.addOption = function(div, icon, message, action) {
	var option = document.createElement("div");
	option.setAttribute("class", "upload-menu-option");
	if (icon) {
		var img = document.createElement("img");
		img.setAttribute("src", icon);
		img.setAttribute("class", "upload-menu-icon");
		option.appendChild(img);
	}
	var s = document.createElement("span");
	s.setAttribute("class", "upload-menu-text");
	s.innerText = message;
	option.appendChild(s);
	zk.listen(option, "click", action);
	div.appendChild(option);
}

zkDataBinary.uploadOption = function(event) {
	var popup = event.currentTarget.parentElement;
	var input = popup.input;
	var position = input.position;
	var databox = input.databox;
    zkau.send({
        uuid: databox.id,
        cmd: "onUpload",
        data: [position]
	}, 100);
	input.popup.remove();
	input.popup = null;
}

zkDataBinary.clearOption = function(event) {
	var popup = event.currentTarget.parentElement;
	var input = popup.input;
	var position = input.position;
	var databox = input.databox;
    zkau.send({
        uuid: databox.id,
        cmd: "onClear",
        data: [position]
    }, 100);
	input.popup.remove();
	input.popup = null;
}

zkDataBinary.downloadOption = function(event) {
	var popup = event.currentTarget.parentElement;
	var input = popup.input;
	var position = input.position;
	var databox = input.databox;
    zkau.send({
        uuid: databox.id,
        cmd: "onDownload",
        data: [position]
    }, 100);
	input.popup.remove();
	input.popup = null;
}

zkDataBinary.init = function (e) {
	e.addInputElement = zkDataBinary.addElement;
	zkDataCommon.init(e);
};

zkDataBinary.cleanup = zkDataBinary.onHide = function(B) {
    var A = $real(B);
	zkDataCommon.cleanup(e);
}

zkDataBinary.setAttr = function (ed, name, value) {
	return zkDataCommon.setAttr(ed,name,value);
}

/********************* HTML ELEMENT ********************/
zkDataHtml.addElement = function(e, parent, pos) {
	var img = document.createElement("div");
	img.setAttribute("id", parent.id+"!input");
	img.setAttribute("class", "datahtml");
	img.position = pos;
	img.databox = e;
	if (e.multivalue) {
		if (pos < e.value.length) img.innerHTML = e.value[pos];
	} else {
		img.innerHTML = e.value;
	}
	parent.appendChild(img);

	zkDataCommon.createRemoveIcon(e, parent, pos);
	/* add event listeners */
	if (!e.readOnly && !e.disabled)  {
		img.databoxid = e.getAttribute("id");
		zk.listen(img, "click", zkDataCommon.onOpenSelect);
    }
    /* set css class */
    if (e.readOnly) {
        zk.addClass(img, "readonly")
        img.setAttribute("readonly","readonly")
    }
    if (e.disabled) {
        zk.addClass(img, "disabled")
        img.setAttribute("disabled","disabled")
    }
}

zkDataHtml.init = function (e) {
	e.addInputElement = zkDataHtml.addElement;
	zkDataCommon.init(e);
};

zkDataHtml.cleanup = zkDataHtml.onHide = function(B) {
    var A = $real(B);
	zkDataCommon.cleanup(e);
}

zkDataHtml.setAttr = function (ed, name, value) {
	return zkDataCommon.setAttr(ed,name,value);
}
/************* Common methods ******************/
zkDataCommon.createRemoveIcon=function(databox, parent, pos) {
	if (databox.readOnly || databox.disabled || databox.getAttribute("noremove") == "true") return;
	
	if (databox.multivalue && databox.getAttribute("removeicon")) {
		img = document.createElement("img");
		img.src = databox.getAttribute("removeicon");
		img.databox = databox;
		img.position = pos;
		img.id = parent.id+"!remove";
		if (pos >= databox.value.length  )
			img.style.display = "none";
		img.setAttribute("class", "remove-icon");
		zk.listen(img, "click", zkDataCommon.onremovevalue);
		parent.appendChild(img);
	}
	if (databox.getAttribute("warningicon") && !databox.readOnly && ! databox.disabled) {
		var w = document.createElement("div");
		w.setAttribute("id", parent.getAttribute("id")+"!Warning");
		w.setAttribute("class", "warning");
		w.style.display = "none";
		
		var wi = document.createElement("img");
		wi.setAttribute("class", "warning-icon");
		wi.setAttribute("src", databox.getAttribute("warningicon"));
		w.appendChild(wi);
		
		var wl = document.createElement("span");
		wl.setAttribute("id", parent.getAttribute("id")+"!WarningLabel");
		w.appendChild(wl);
		
		parent.appendChild(w);
	}
}

zkDataCommon.registerInput=function(databox, input) {
	var value = databox.getAttribute("dsid");
	if (value) {
		var ds = document.getElementById(value);
		if (ds) 
			zkDatasource.registerInput(ds, input);
	}
}

zkDataCommon.init = function (databox) {
	databox.multivalue = "true" == databox.getAttribute("multivalue");
	databox.required = "false" != databox.getAttribute("required")  && databox.getAttribute("required") != null;
	databox.disabled = "false" != databox.getAttribute("disabled")  && databox.getAttribute("disabled") != null;
	databox.readOnly = "false" != databox.getAttribute("readonly")  && databox.getAttribute("readonly") != null;
	databox.multiline = "true" == databox.getAttribute("multiline");
	var value = databox.getAttribute("value");
	if (value != null)
		value = JSON.parse(value);
	zkDataCommon.refresh(databox, value);
}

zkDataCommon.labelObserver = new IntersectionObserver((entries, observer) => {
		entries.forEach(entry => {
			var cs = getComputedStyle(entry.target);
			var w = Number(cs.width.substring(0, cs.width.length-2));
			var mw = Number(cs.minWidth.substring(0, cs.minWidth.length-2));
			if (w > mw +1) 
				entry.target.nextElementSibling.classList.add("wrapped");
			else			
				entry.target.nextElementSibling.classList.remove("wrapped");
		})
	}, 
	{
	  rootMargin: '0px',
	  threshold: 1.0
	}
);

zkDataCommon.refresh=function(databox, value) {
	var container = databox;
	if (databox.getAttribute("label")) {
		var labelContainer = document.createElement("div");
		labelContainer.setAttribute("class", "label");
		labelContainer.innerText = databox.getAttribute("label");
		databox.appendChild(labelContainer);
		
		container = document.createElement("div");
		container.setAttribute("class", "container");
		container.setAttribute("id", $uuid(databox)+"_container");
		databox.appendChild(container);
		try {
			var cs = getComputedStyle(labelContainer);
			if (cs.width == "auto" || ! cs.width.endsWith("px")) {
				zkDataCommon.labelObserver.observe(labelContainer);				
			}
			else {
				var w = Number(cs.width.substring(0, cs.width.length-2));
				var mw = Number(cs.minWidth.substring(0, cs.minWidth.length-2));
				if (w > mw +1) 
					container.classList.add("wrapped");
				else			
					container.classList.remove("wrapped");
			}
		} catch (error) { // Can fail if container is not visible yet 
		}
	}
	if (databox.multivalue) {
		if (value == null)
			value = [];
		databox.value = value;
		var lastEmpty = false;
		for (var i = 0; i < value.length; i++) {
	   		zkDataCommon.addScroll(databox, container);
			var div = document.createElement("div");
			div.setAttribute("id", $uuid(databox)+"_"+i)
			div.databoxid = $uuid(databox);
			container.appendChild(div);
			databox.addInputElement(databox, div, i);
			var v = value[i];
			if (databox.useDescription || databox.useNameDescription) {
				lastEmpty = (v == null || v[0] == null || v[0] == "");
			} else {
				lastEmpty = (v == null || v == "");
			}
		}
		if ( ! databox.readonly && !lastEmpty && databox.getAttribute("noadd") != "true") {
			var div = document.createElement("div");
			div.setAttribute("id", $uuid(databox)+"_"+value.length)
			div.databoxid = $uuid(databox);
			container.appendChild(div);
	   		zkDataCommon.addScroll(databox, container);
			databox.addInputElement(databox, div, value.length);
		}
	} else {
		databox.value = value;
		container.databoxid = $uuid(databox);
		databox.addInputElement(databox, container, 0);
	}
}

zkDataCommon.addScroll = function( databox, container ) {
	var mr = databox.getAttribute("maxrows");
	if (mr && mr == container.childElementCount) {
		var h = container.getClientRects()[0].height;
		container.style.maxHeight = "" + h + "px";
		container.classList.add("scrollable");
	}

}

zkDataCommon.onselect = function(A) {
	var el = zkau.evtel(A);
    var D = el.databox;
    var C = $outer(D);
    if (zkau.asap(C, "onSelection")) {
        var B = zk.getSelectionRange(D);
        zkau.send({
            uuid: C.id,
            cmd: "onSelection",
            data: [B[0], B[1], D.value.substring(B[0], B[1]), el.position]
        }, 100)
    }
}
;
zkDataCommon.onblur = function(A) {
    var el = zkau.evtel(A);
    var B = el.databox;
    var C = zkDataCommon._noonblur(el, B);
    if (B.useDescription) {
    	var w = document.getElementById($uuid(el)+"!Warning");
    	var wl = document.getElementById($uuid(el)+"!WarningLabel");
    	var value ;
        if (w && el.popup && zkDataCommon.inSearchPopup) {
        	if (el.value == null || el.value == "") {
        		w.style.display="none";
        		value = [el.value, el.value];
                zkDataCommon.updateChange(el, B, C);
        	}
        	else if (el.popup.childElementCount == 2) {
        		w.style.display="";
        		wl.innerText = "Wrong value";
        		value = [el.value, el.value];
                zkDataCommon.updateChange(el, B, C);
        	} else {
        		w.style.display = "none";
        		zkDataCommon.onSelectValue2(el.popup.firstElementChild);
        	}
        	el.popup.remove();
        	el.popup = null;
        	zkDataCommon.inSearchPopup = false;
        } else {
        	if ( el.value == "") 
        		el.actualValue = "";
        	if (el.actualValue == "")
        		el.value = "";
            zkDataCommon.updateChange(el, B, C);
        }
    } else if (B.useNameDescription) {
    	var w = document.getElementById($uuid(el)+"!Warning");
    	var value ;
        if (w && el.popup && zkDataCommon.inSearchPopup) {
        	if (el.popup.childElementCount == 2) {
        		w.style.display = "none";
        		zkDataCommon.onSelectValue2(el.popup.firstElementChild);
        	} else {
                zkDataCommon.updateChange(el, B, C);
        	}
    		el.popup.remove();
    		el.popup = null;
    		zkDataCommon.inSearchPopup = false;
        } else {
            zkDataCommon.updateChange(el, B, C);
        }
    } else {
        zkDataCommon.updateChange(el, B, C);
    }
    zkau.onblur(A, C);
    if (el.popup) {
    	setTimeout(()=>{
    		try{
    			if(el.popup && !zkDataCommon.inSearchPopup) {
    				el.popup.remove(); 
					el.popup=null;
					clearInterval(zkDataCommon.intervalEvent);
					zkDataCommon.popup = null;
    			}
    		} catch(e) {
    			
    		}
    	}, 150);
    }
}
;
zkDataCommon.updateChange = function(el, A, B) {
    if (zkVld.validating) {
        return true
    }
    if (el && el.id) {
        var C = !B ? zkVld.validate(el.id) : null;
        if (C) {
            zkVld.errbox(el.id, C);
            A.setAttribute("zk_err", "true");
            zkau.send({
                uuid: $uuid(A),
                cmd: "onError",
                data: [A.value, C, el.position]
            }, -1);
            return false
        }
        zkVld.closeErrbox(A.id)
    }
    if (!B) {
        zkDataCommon.onupdate(el, A, false)
    }
    return true
}
;
zkDataCommon._noonblur = function(el, B) {
    if (zk.alerting) {
        return true
    }
    var C = zkau.currentFocus;
    if (B && C && B != C) {
        var A = B;
        for (; ; A = A.parentNode) {
            if (!A) {
                return false
            }
            if (getZKAttr(A, "combo") == "true") {
                break
            }
            if (getZKAttr(A, "type")) {
                return false
            }
        }
        for (; C; C = $parent(C)) {
            if (C == A) {
                return true
            }
        }
    }
    return false
}
;
zkDataCommon.inSearchPopup = false;
zkDataCommon.onupdate = function(el,C, force) {
    var D = el.value;
    if (el.databox.useDescription)
    	D = el.actualValue;
    if (C.multivalue && el.position != undefined)
    {
    	if (C.useDescription || C.useNameDescription) {
    		C.value[el.position] = [ D, "" ];
    	}
    	else
    		C.value[el.position] = D;
    	
    }
    else {
    	if (C.useDescription || C.useNameDescription) {
    		C.value = [ D, "" ];
    	}
    	else
    		C.value = D;
    }
    if (D != el.defaultValue || force) {
        el.defaultValue = D;
        var B = $uuid(C);
        var A = zk.getSelectionRange(el);
		zkau.sendasap({
    			uuid: B,
    			cmd: "onChange",
    			data: [D, false, A[0], el.position]
    		}, zk.delayTime_onChange ? zk.delayTime_onChange : 150)

    	if (el.getAttribute("zk_err")) {
            el.removeAttribute("zk_err");
            zkau.send({
                uuid: $uuid(C),
                cmd: "onError",
                data: [D, null, el.position]
            }, -1)
        }
    }
}
;
zkDataCommon.onkey = function(A) {
    var el = zkau.evtel(A);
    var C = el.databox;
    var B = getZKAttr(C, "maxlen");
    if (B) {
        B = $int(B);
        if (B > 0 && el.value != el.defaultValue && el.value.length > B) {
            el.value = el.value.substring(0, B)
        }
    }
}
;
zkDataCommon.onkeydown = function(A) {
    var el = Event.element(A);
    var D = el.databox
      , B = $uuid(D)
      , C = $e(B)
      , E = Event.keyCode(A);
    if ((E == 13 && zkau.asap(C, "onOK")) || (E == 27 && zkau.asap(C, "onCancel"))) {
        zkDataCommon.updateChange(el, D, false)
    }
}
;
zkDataCommon.oninput = function(A) {
    var el = Event.element(A);
    zkDataCommon.updatedElement(el);
}

zkDataCommon.updatedElement = function(el) {
    var D = el.databox
      , B = $uuid(D)
      , C = $e(B);
    if (D.multivalue) {
    	var container = document.getElementById(D.id+"_container");
    	if (container == null)
    		container = D;
    	var div0 = document.getElementById(B+"_"+el.position);
    	if (div0.nextElementSibling == null) {
    		var ri = $e($uuid(el)+"!remove");
    		if (ri)
    			ri.style.display = "";
    		var pos = el.position + 1;
//    		if (D.useDescription || D.useNameDescription) 
//    			D.value[pos] = ["", ""];
//    		else
//    			D.value[pos] = null;
    		
    		
    		var div = document.createElement("div");
    		div.setAttribute("id", D.getAttribute("id")+"_"+pos)
			zkDataCommon.addScroll(D, container);
    		container.appendChild(div);
    		div.databoxid = D.getAttribute("id");
    		if (D.getAttribute("noadd") != "true")
    			D.addInputElement(D, div, pos);    		
    	}
    }
}
;
zkDataCommon.onfocus = function(A) {
    var el = zkau.evtel(A);
    var B = el.databox;
}
;

zkDataCommon.onremovevalue = function(A) {
    var el = zkau.evtel(A);
    var B = el.databox;
    B.value[el.position] = null;
    zkau.sendasap({
        uuid: $uuid(B),
        cmd: "onChange",
        data: [null, false, null, el.position]
    }, zk.delayTime_onChange ? zk.delayTime_onChange : 150);
    var div = document.getElementById(B.getAttribute("id")+"_"+el.position); 
    div.remove();
}

zkDataCommon._intervals = [];

zkDataCommon.setAttr = function (e, name, value) {
	if (name == "dsid") {
		if (e.zkDatasource) 
			zkDatasource.unregisterInput(e.zkDatasource, e);
		var ds = document.getElementById(value);
		if (ds) {
			zkDatasource.registerInput(ds, e);
		}
		return true;
	} else if (name == "value") {
		for (var v = e.firstElementChild; v != null; v = e.firstElementChild) 
			v.remove();
		if (value != null)
			value = JSON.parse(value);
		zkDataCommon.refresh(e, value);
		return true;
	} else if (name == "disabled") {
		e.disabled = value == "true";
		for (var v = e.firstElementChild; v != null; v = e.firstElementChild) 
			v.remove();
		zkDataCommon.refresh(e, e.value);
		return true;
	} else if (name.startsWith("warning_")) {
		var id = e.multivalue? $uuid(e)+ name.substring(7): $uuid(e);
    	var w = document.getElementById(id+"!Warning");
    	if (!w && ! e.multivalue) { // When label is used, the id is appended "_container"
    		id = id+"_container";
        	w = document.getElementById(id+"!Warning");
    	}
	    if (w)
	    	w.style.display = value != null && value.length > 0 ? "": "none";
	   	var wl = document.getElementById(id+"!WarningLabel");
	    if (wl)
	    	wl.innerText = value;
	    return true;
	} else {
		return false;
	}
}

zkDataCommon.cleanup = function (e) {
	if (e && e.zkDatasource) 
		zkDatasource.unregisterInput(e.zkDatasource, e);
}


zkDataCommon.onScroll=function(e) {
   if (zkDataCommon.popup != null) {
		var rect = zkDataCommon.popup.input.getBoundingClientRect();
		var left = rect.left;
		var top = rect.bottom;
		zkDataCommon.popup.style.left = ""+left+"px";
		zkDataCommon.popup.style.top = ""+top+"px";
   }	
}

// Global event listener
document.addEventListener('scroll', zkDataCommon.onScroll);

zkDataCommon.openSearchPopup = function (input, databox) {
	if (!input.popup) {
		var div = document.createElement("div");
		div.setAttribute("id", input.getAttribute("id")+"-popup")
		div.setAttribute("class", "databox-popup");
		input.popup = div;
		div.input = input;
		var ss = document.createElement("div");
		ss.setAttribute("class", "no-results");
		div.appendChild(ss);
		ss.style.display="none";
		var img = document.createElement("img");
		img.setAttribute("src", databox.getAttribute("waiticon"));
		img.setAttribute("class", "databox-wait");
		div.appendChild(img);
		var rect = input.getBoundingClientRect();
		var left = rect.left;
		var top = rect.bottom;
		var parent = document.body;
		parent.insertBefore(div, parent.firstElementChild);
		div.style.position="fixed";
		div.style.left = ""+left+"px";
		div.style.top = ""+top+"px";
		zkDataCommon.popup = div;
		zkDataCommon.intervalEvent  = setInterval(zkDataCommon.onScroll, 500);
		div.input = input;
	} else {
		for (var e = input.popup.firstElementChild; 
			e != input.popup.lastElementChild.previousElementSibling;
			e = input.popup.firstElementChild ) {
			e.remove();
		}
	}
	input.popup.lastElementChild.previousElementSibling.style.display = "none";
	input.popup.lastElementChild.style.display = "";
	input.popup.searchCriteria = input.value;
	var req = {uuid: databox.id, cmd: "onStartSearch", 
			data: [input.value, input.popup.getAttribute("id") ], ignorable:true};
	zkau.send (req, 0);		
	zkDataCommon.inSearchPopup = true;
}

zkDataCommon.onFocusSearchPopup = function(evt) {
}
zkDataCommon.onBlurSearchPopup = function(evt) {
	zkDataCommon.inSearchPopup = false;
	var menu = evt.currentTarget.parentElement;
	menu.remove(); 
	menu.input.popup=null;
	clearInterval(zkDataCommon.intervalEvent);
	zkDataCommon.popup = null;
}
zkDataCommon.onSelectValue=function(evt) {
	var opt = evt.currentTarget;
	var menu = opt.parentElement;
	zkDataCommon.inSearchPopup = false;
	zkDataCommon.onSelectValue2(opt);
	setTimeout(()=>{
		try{
			menu.remove(); 
			menu.input.popup=null;
			clearInterval(zkDataCommon.intervalEvent);
			zkDataCommon.popup = null;
		} catch(e) {}
	}, 150);		
}

zkDataCommon.onSelectValue2=function(opt) {
    if (opt.value) {
		var value = [opt.value[0], opt.value[1]];
		var menu = opt.parentElement;
		var input = menu.input;
		var databox = input.databox;
		var position = input.position;
	
		if (databox.multivalue) {
			databox.value[position] = value;
		} else {
			databox.value = value;
		}
		if (databox.useNameDescription) {
			input.value = value[0];
			zkDataNameDescription.setDescription(databox, position, value[1]);
		} else if (databox.useDescription) {
			input.value = value[1];
			input.actualValue = value[0];
		}
		var wi = document.getElementById($uuid(input)+"!Warning");
		if ( wi ) {
			wi.style.display = "none";
		}
	    zkDataCommon.updateChange(input, databox, false);
	}
}

zkDataCommon.highlightSearchResult=function(child, text, criteria) {
	var textlc = text.toLowerCase();
	var pos =  0;
	while ( pos < text.length) {
		var next = text.length;
		var tag = null;
		for (var j = 0; j < criteria.length; j++) {
			var pos2 = textlc.indexOf(criteria[j], pos);
			if (criteria[j].length > 0 && pos2 >= 0 && pos2 < next) {
				next = pos2;
				tag = text.substring(pos2, pos2+criteria[j].length);
			}
		}
		if (next > pos) {
			var span = document.createElement("span");
			span.innerText = text.substring(pos, next);
			child.appendChild(span);
			pos = next;
		}
		if (tag) {
			var span = document.createElement("span");
			span.setAttribute("class", "search-match")
			span.innerText = tag;
			child.appendChild(span);
			pos = pos + tag.length;
		}
	}
} 

zkDataCommon.addSearchResult=function(div, data, clear) {
	var databox = div.input.databox;
	var criteria = div.searchCriteria.toLowerCase().replace(/.,-/g, " ").split(" ");
	if (clear) {
		while (div.firstElementChild != div.lastElementChild.previousElementSibling)
			div.firstElementChild.remove();
	}
	for (var i = 0; i < data.length; i++) {
		var row = data[i];
		var name = row[0];
		var description = row[1];
		var text = row[2];
		var nameLength = 0;
		var child = document.createElement ("div");
		if ( text == undefined ) {
			if ( !databox.useDescription) {
				var span1 = document.createElement("span");
				span1.setAttribute("class", "name");
				child.appendChild(span1);
				zkDataCommon.highlightSearchResult(span1, row[0], criteria)
				zkDataCommon.highlightSearchResult(child, " : " + row[1], criteria)
			} else {
				zkDataCommon.highlightSearchResult(child, row[1], criteria)				
			}
		} else {
			zkDataCommon.highlightSearchResult(child, text, criteria)
		}

		child.setAttribute("tabindex", "-1");
		zk.listen(child, 'mousedown', zkDataCommon.onSelectValue);
		zk.listen(child, "focus", zkDataCommon.onFocusSearchPopup);
		zk.listen(child, "blur", zkDataCommon.onBlurSearchPopup);
		child.value = row;
		
		div.insertBefore(child, div.lastElementChild.previousElementSibling);

	}
	var data = div.input;
	setTimeout( () => {
	      if (div.input == data) {
			zkau.send( {uuid: databox.id, cmd: "onContinueSearch", 
				data: [div.getAttribute("id") ], ignorable:true}, 0);
		  }
		} , 100 );
}

zkDataCommon.onOpenSelect=function(ev) {
	var el = ev.currentTarget;
	var uuid = el.databoxid;
	var row = el.position;
    zkau.send({
            uuid: uuid,
            cmd: "onOpenSelect",
            data: [row.position]
        }, 100);
}


/********** Patch for PM/AM indicator ********************/

zk.formatDate = function(val, fmt) {
    if (!fmt)
        fmt = "yyyy/MM/dd";
    var txt = "";
    for (var j = 0, fl = fmt.length; j < fl; ++j) {
        var cc = fmt.charAt(j);
        if ((cc >= 'a' && cc <= 'z') || (cc >= 'A' && cc <= 'Z')) {
            var len = 1;
            for (var k = j; ++k < fl; ++len)
                if (fmt.charAt(k) != cc)
                    break;
            switch (cc) {
            case 'y':
                if (len <= 3)
                    txt += zk.formatFixed(val.getFullYear() % 100, 2);
                else
                    txt += zk.formatFixed(val.getFullYear(), len);
                break;
            case 'M':
                if (len <= 2)
                    txt += zk.formatFixed(val.getMonth() + 1, len);
                else if (len == 3)
                    txt += zk.SMON[val.getMonth()];
                else
                    txt += zk.FMON[val.getMonth()];
                break;
            case 'd':
                txt += zk.formatFixed(val.getDate(), len);
                break;
            case 'E':
                if (len <= 3)
                    txt += zk.SDOW[val.getDay()];
                else
                    txt += zk.FDOW[val.getDay()];
                break;
            case 'D':
                txt += zk.dayInYear(val);
                break;
            case 'd':
                txt += zk.dayInMonth(val);
                break;
            case 'w':
                txt += zk.weekInYear(val);
                break;
            case 'W':
                txt += zk.weekInMonth(val);
                break;
            case 'G':
                txt += "AD";
                break;
            case 'F':
                txt += zk.dayOfWeekInMonth(val);
                break;
            case 'H':
                txt += val.getHours();
                break;
            case 'h':
                txt += val.getHours() % 12;
                break;
            case 'm':
                txt += val.getMinutes();
                break;
            case 's':
                txt += val.getSeconds();
                break;
            case 'a':
                txt += val.getHours() >= 12 ? "PM": "AM";
                break;
            default:
                txt += '1'
            }
            j = k - 1
        } else {
            txt += cc
        }
    }
    return txt
};

zkDataText.focus = zkDataPassword.focus = zkDataSeparator.focus = zkDataList.focus = 
zkDataCommon.focus = zkDataDate.focus = zkDataNameDescription.focus = zkDataDescription.focus = 
zkDataSwitch.focus = zkDataImage.focus = zkDataHtml.focus = function (ed) {
	var d = document.getElementById( $uuid(ed)+"_container!input");
	if (!d) d = document.getElementById( $uuid(ed)+"_container!real");
	if (!d) d = document.getElementById( $uuid(ed)+"_container!slider");
	if (d) d.focus();
}