zk.load("zul.db"); 

zkDataText={};
zkDataList={};
zkDataCommon={};
zkDataDate={};
zkDataNameDescription={};
zkDataDescription={};
zkDataSwitch={};
zkDataImage={};
zkDataHtml={};
/********************* TEXT ELEMENT ********************/
zkDataText.addElement = function(e, parent, pos) {
	var i = document.createElement("input");
	if (e.required)
		i.setAttribute("class", "text required");
	else
		i.setAttribute("class", "text");
	i.setAttribute("id", parent.id+"!input");
	if (e.getAttribute("maxlength"))
		i.maxlen=e.getAttribute("maxlength");
	if (e.getAttribute("maxlength")) {
		i.style.maxWidth = e.getAttribute("maxlength")+"em";
	}
	i.position = pos;
	i.databox = e;
	if (e.multivalue) {
		if (pos < e.value.length) i.value = e.value[pos];
	} else {
		i.value = e.value;
	}
	parent.appendChild(i);
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

/********************* LISTBOX ELEMENT ********************/
zkDataList.addElement = function(e, parent, pos) {
	var sel = document.createElement("select");
	if (e.required)
		sel.setAttribute("class", "select required");
	else
		sel.setAttribute("class", "select");
	sel.setAttribute("id", parent.id+"!input");
	sel.position = pos;
	sel.databox = e;
	if (e.multivalue) {
		if (pos < e.value.length) sel.value = e.value[pos];
	} else {
		sel.value = e.value;
	}
	
	parent.appendChild(sel);

	var options = JSON.parse(e.getAttribute("values"));
	if (e.getAttribute("required") != "true") {
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
			label = option.substring(split+1).trim();
		} else {
			key = label = option;
		}
		var op = document.createElement("option");
		op.setAttribute("value", key);
		op.innerText = label;
		if ( key == sel.value)
			op.setAttribute("selected", "selected");
		sel.appendChild(op);
	}

	zkDataCommon.createRemoveIcon(e, parent, pos);
	zkDataCommon.registerInput (e, sel);
	/* add event listeners */
    zk.listen(sel, "input", zkDataList.onselect);
    
    /* set css class */
    if (e.readOnly) {
        zk.addClass(sel, "readonly")
        sel.setAttribute("readonly","readonly")
    }
    if (e.disabled) {
        zk.addClass(sel, "text-disd")
        sel.setAttribute("disabled","disabled")
    }
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
	zkDataCommon.onupdate(el, el.databox);
	zkDataCommon.oninput (ev);
}

/********************* SWITCH ********************/
zkDataSwitch.addElement = function(e, parent, pos) {
	parent.position = pos;
	parent.classList.add("switch");
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
	var rowValue = e.multivalue ? (pos < e.value.length? e.value[pos]: ""): e.value;
	var wi = null;
	if (e.getAttribute("warningicon")) {
		wi = document.createElement("img");
		wi.setAttribute("id", parent.getAttribute("id")+"!Warning");
		wi.setAttribute("class", "warning-icon");
		wi.setAttribute("src", e.getAttribute("warningicon"));
		if ( rowValue[2] && rowValue[2].length > 0) {
			wi.setAttribute("title", rowValue[2]);
		} else {
			wi.setAttribute("style", "display: none");
		}
		parent.appendChild(wi);
	}
	var i = document.createElement("input");
	if (e.required)
		i.setAttribute("class", "text required");
	else
		i.setAttribute("class", "text");
	i.setAttribute("id", parent.id+"!input");
	if (e.getAttribute("maxlength"))
		i.maxlen=e.getAttribute("maxlength");
	if (e.getAttribute("maxlength")) {
		i.style.maxWidth = e.getAttribute("maxlength")+"em";
	}
	i.position = pos;
	i.databox = e;
	i.value = rowValue[0];
	
	parent.appendChild(i);
	
	if (e.getAttribute("selecticon") && !e.readOnly && !e.disabled) {
		var s = document.createElement("img");
		s.setAttribute("src", e.getAttribute("selecticon"))
		s.position = pos;
		s.databox = e;
		parent.appendChild(s);
		zk.listen(s, "click", zkDataCommon.onOpenSelect);
	}
	
	var l = document.createElement("span");
	l.innerText = rowValue[1];
	l.setAttribute("id", parent.getAttribute("id")+"!Label");
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
}

zkDataNameDescription.setDescription = function(e, pos, description, warning) {
	var id;
	if (e.multivalue) {
		if (e.value[pos] != null) 
			e.value[pos][1] = description;
		id = e.getAttribute("id")+"_"+pos; 
	} else {
		e.value[1] = description;
		id = e.getAttribute("id"); 
	}
	var label = document.getElementById(id+"!Label");
	if (label) label.innerText = description;
	var wi = document.getElementById(id+"!Warning");
	if ( wi ) {
		if (warning && warning.length > 0) {
			wi.style.display = "";
			wi.setAttribute("title", warning);
		}
		else
			wi.style.display = "none";
	}
	
}

zkDataNameDescription.init = function (e) {
	e.addInputElement = zkDataNameDescription.addElement;
	e.useDescription = false;
	e.useNameDescription = true;
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

zkDataNameDescription.onContinueSearchResponse = 
zkDataNameDescription.onStartSearchResponse = function(ed, id, data) {
	var div = document.getElementById(id);
	if (div) {
		var data = JSON.parse(data);
		zkDataCommon.addSearchResult(div, data);
	}
}

zkDataNameDescription.onEndSearchResponse = function(ed, id, data) {
	var div = document.getElementById(id);
	if (div) {
		div.lastElementChild.style.display="none";
	}
}

/********************* DESCRIPTION ****************/
zkDataDescription.addElement = function(e, parent, pos) {
	var rowValue = e.multivalue ? (pos < e.value.length? e.value[pos]: ""): e.value;
	var wi = null;
	if (e.getAttribute("warningicon")) {
		wi = document.createElement("img");
		wi.setAttribute("class", "warning-icon");
		wi.setAttribute("id", parent.getAttribute("id")+"!Warning");
		wi.setAttribute("src", e.getAttribute("warningicon"));
		if ( rowValue[2] && rowValue[2].length > 0) {
			wi.setAttribute("title", rowValue[2]);
		} else {
			wi.setAttribute("style", "display: none");
		}
		parent.appendChild(wi);
	}
	var i = document.createElement("input");
	if (e.required)
		i.setAttribute("class", "text required");
	else
		i.setAttribute("class", "text");
	i.setAttribute("id", parent.id+"!input");
	if (e.getAttribute("maxlength"))
		i.maxlen=e.getAttribute("maxlength");
	if (e.getAttribute("maxlength")) {
		i.style.maxWidth = e.getAttribute("maxlength")+"em";
	}
	i.position = pos;
	i.databox = e;
	var rowValue = e.multivalue ? (pos < e.value.length? e.value[pos]: ""): e.value;
	i.value = rowValue[1];
	
	parent.appendChild(i);
	
	if (e.getAttribute("selecticon") &&  !e.readOnly && !e.disabled) {
		var s = document.createElement("img");
		s.setAttribute("src", e.getAttribute("selecticon"))
		s.position = pos;
		s.databox = e;
		parent.appendChild(s);
		zk.listen(s, "click", zkDataCommon.onOpenSelect);
	}
	
	zkDataCommon.createRemoveIcon(e, parent, pos);
	zkDataCommon.registerInput (e, i);
	/* add event listeners */
    zk.listen(i, "focus", zkDataCommon.onfocus);
    zk.listen(i, "blur", zkDataCommon.onblur);
    zk.listen(i, "select", zkDataCommon.onselect);
    zk.listen(i, "keydown", zkDataCommon.onkeydown);
    zk.listen(i, "input", zkDataCommon.oninput);
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
	zkDataCommon.openSearchPopup(i, databox);
}

zkDataDescription.onStartSearchResponse =
zkDataDescription.onContinueSearchResponse = function(ed, id, data) {
	var div = document.getElementById(id);
	if (div) {
		var data = JSON.parse(data);
		zkDataCommon.addSearchResult(div, data);
	}
}

zkDataDescription.onEndSearchResponse = function(ed, id, data) {
	var div = document.getElementById(id);
	if (div) {
		div.lastElementChild.style.display="none";
	}
}
/********************* DATE ELEMENT ********************/
zkDataDate.addElement = function(e, parent, pos) {
	var span = document.createElement("span")
	span.setAttribute("class", "zbtnbk");
	parent.appendChild(span);
	
	// input
	var i = document.createElement("input");
	i.setAttribute("class", "dateboxinp");
	i.position = pos;
	i.databox = e;
	i.setAttribute("id", parent.id+"!real");
	if (e.multivalue) {
		if (pos < e.value.length) i.value = e.value[pos];
	} else {
		i.value = e.value;
	}
	span.appendChild(i);
	// button
	if (e.getAttribute("readonly") != "readonly") {
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
	return zkDataCommon.setAttr(ed,name,value);
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
};
zkDataDate.closepp = function (evt) {
	if (!evt) evt = window.event;
	var pp = Event.element(evt);
	for (; pp; pp = pp.parentNode) {
		if (pp.id) {
			if (pp.id.endsWith("!pp"))
				zkDataDate.close(pp, true);
			return; //done
		}
		if (pp.onclick) return;
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
		if (pos < e.value.length) img.value = e.value[pos];
	} else {
		img.value = e.value;
	}
	parent.appendChild(img);

	zkDataCommon.createRemoveIcon(e, parent, pos);
	/* add event listeners */
	if ( !e.readOnly && !e.disabled)
		zk.listen(img, "click", zkDataCommon.onOpenSelect);
    
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
	if (!e.readOnly && !e.disabled) 
		zk.listen(img, "click", zkDataCommon.onOpenSelect);
    
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
	if (databox.multivalue && databox.getAttribute("removeicon")) {
		img = document.createElement("img");
		img.src = databox.getAttribute("removeicon");
		img.databox = databox;
		img.position = pos;
		img.id = parent.id+"!remove";
		if (pos == databox.value.length - 1 )
			img.style.display = "none";
		zk.listen(img, "click", zkDataCommon.onremovevalue);
		parent.appendChild(img);
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
	databox.required = "true" == databox.getAttribute("required");
	databox.disabled = "true" == databox.getAttribute("disabled");
	databox.readOnly = "true" == databox.getAttribute("readonly");
	var value = databox.getAttribute("value");
	if (value != null)
		value = JSON.parse(value);
	zkDataCommon.refresh(databox, value);
}

zkDataCommon.refresh=function(databox, value) {
	var container = databox;
	if (databox.getAttribute("label")) {
		var labelContainer = document.createElement("div");
		labelContainer.setAttribute("class", "label");
		labelContainer.innerText = container.getAttribute("label");
		databox.appendChild(labelContainer);
		
		container = document.createElement("div");
		container.setAttribute("class", "container");
		container.setAttribute("id", $uuid(databox)+"_0");
		databox.appendChild(container);
	}
	if (databox.multivalue) {
		if (value == null)
			value = [];
		databox.value = value;
		databox.value[value.length] =  databox.useDescription || databox.useNameDescription ? [null, null] : null;
		for (var i = 0; i < value.length; i++) {
			var div = document.createElement("div");
			div.setAttribute("id", $uuid(databox)+"_"+i)
			div.databoxid = $uuid(databox);
			container.appendChild(div);
			databox.addInputElement(databox, div, i);
		}
	} else {
		databox.value = value;
		databox.databoxid = $uuid(databox);
		databox.addInputElement(databox, container, 0);
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
	console.log("on blur ");
    var el = zkau.evtel(A);
    var B = el.databox;
    var C = zkDataCommon._noonblur(el, B);
    if (B.useDescription) {
    	var w = document.getElementById($uuid(el)+"!Warning");
    	var value ;
        if (w && el.popup) {
        	if (el.popup.firstElementChild == el.popup.lastElementChild) {
        		w.style.display="";
        		w.setAttribute("title", "Wrong value");
        		value = [el.value, el.value];
                zkau.sendasap({
                    uuid: B,
                    cmd: "onChange",
                    data: [el.value, false, el.value, el.position]
                }, zk.delayTime_onChange ? zk.delayTime_onChange : 150)
        	} else {
        		w.style.display = "none";
        		zkDataCommon.onSelectValue2(el.popup.firstElementChild);
        	}
        } else {
    		value = [el.value, el.value];
    		if (w) {
    			w.style.display="";
    			w.setAttribute("title", "Wrong value");
    		}
            zkau.sendasap({
                uuid: B,
                cmd: "onChange",
                data: [el.value, false, el.value, el.position]
            }, zk.delayTime_onChange ? zk.delayTime_onChange : 150)
        }
    } else {
        zkDataCommon.updateChange(el, B, C);
    }
    zkau.onblur(A, C);
    if (el.popup) {
    	setTimeout(()=>{try{if(el.popup) el.popup.remove(); el.popup=null;} catch(e) {}}, 150);
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
        zkDataCommon.onupdate(el, A)
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
zkDataCommon.onupdate = function(el,C) {
    var D = el.value;
    if (C.multivalue && el.position != undefined)
    {
    	if (C.useDescription || C.useNameDescription) {
    		C.value[el.position][0] = D;
    		C.value[el.position][1] = "";
    	}
    	else
    		C.value[el.position] = D;
    	
    }
    else {
    	if (C.useDescription || C.useNameDescription) {
    		C.value[0] = D;
    		C.value[1] = "";
    	}
    	else
    		C.value = D;
    }
    if (D != el.defaultValue) {
        el.defaultValue = D;
        var B = $uuid(C);
        var A = zk.getSelectionRange(el);
        zkau.sendasap({
            uuid: B,
            cmd: "onChange",
            data: [D, false, A[0], el.position]
        }, zk.delayTime_onChange ? zk.delayTime_onChange : 150)
    } else {
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
    var D = el.databox
      , B = $uuid(D)
      , C = $e(B);
    if (D.multivalue && el.position == D.value.length - 1) {
    	var ri = $e($uuid(el)+"!remove");
    	if (ri)
    		ri.style.display = "";
    	var pos = el.position + 1;
    	if (D.useDescription || D.useNameDescription) 
    		D.value[pos] = ["", ""];
    	else
    		D.value[pos] = null;
		var div = document.createElement("div");
		div.setAttribute("id", D.getAttribute("id")+"_"+pos)
		D.appendChild(div);
		div.databoxid = D.getAttribute("id");
		D.addInputElement(D, div, pos);
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
		if (e.zDatasource) 
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
	} else {
		return false;
	}
}

zkDataCommon.cleanup = function (e) {
	if (e.zDatasource) 
		zkDatasource.unregisterInput(e.zDatasource, e);
}

zkDataCommon.openSearchPopup = function (input, databox) {
	if (!input.popup) {
		var div = document.createElement("div");
		div.setAttribute("id", input.getAttribute("id")+"-popup")
		div.setAttribute("class", "databox-popup");
		input.parentElement.insertBefore(div, input);
		input.popup = div;
		div.input = input;
		var img = document.createElement("img");
		img.setAttribute("src", databox.getAttribute("waiticon"));
		img.setAttribute("class", "databox-wait");
		div.appendChild(img);
	} else {
		for (var e = input.popup.firstElementChild; 
			e != input.popup.lastElementChild;
			e = input.popup.firstElementChild ) {
			e.remove();
		}
	}
	input.popup.lastElementChild.style.display = "";
	input.popup.searchCriteria = input.value;
	var req = {uuid: databox.id, cmd: "onStartSearch", 
			data: [input.value, input.popup.getAttribute("id") ], ignorable:true};
	zkau.send (req, 0);		
}

zkDataCommon.onSelectValue=function(evt) {
	var opt = evt.currentTarget;
	var menu = opt.parentElement;
	zkDataCommon.onSelectValue2(opt);
	setTimeout(()=>{try{menu.remove(); menu.input.popup=null;} catch(e) {}}, 150);
}

zkDataCommon.onSelectValue2=function(opt) {	
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
		zkDataNameDescription.setDescription(databox, position, value[1], null);
	} else if (databox.useDescription) {
		input.value = value[1];
	}
	var wi = document.getElementById($uuid(input)+"!Warning");
	if ( wi ) {
		wi.style.display = "none";
	}
    zkau.sendasap({
        uuid: $uuid(databox),
        cmd: "onChange",
        data: [value[0], false, value[0], position]
    }, zk.delayTime_onChange ? zk.delayTime_onChange : 150)
}

zkDataCommon.addSearchResult=function(div, data) {
	var criteria = div.searchCriteria.toLowerCase().replace(/.,-/, " ").split(" ");
	for (var i = 0; i < data.length; i++) {
		var row = data[i];
		var name = row[0];
		var description = row[1];
		var text = row[2];
		if ( text == undefined ) text = row[0] + " " + row [1];

		var child = document.createElement ("div");
		zk.listen(child, 'click', zkDataCommon.onSelectValue);
		child.value = row;
		div.insertBefore(child, div.lastElementChild);

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
				var span = document.createElement("b");
				span.innerText = tag;
				child.appendChild(span);
				pos = pos + tag.length;
			}
		}
	}
	var databox = div.input.databox;
	setTimeout( () => {
		zkau.send( {uuid: databox.id, cmd: "onContinueSearch", 
			data: [div.getAttribute("id") ], ignorable:true}, 0);
		} , 100 );
}

zkDataCommon.onOpenSelect=function(ev) {
	var el = ev.currentTarget;
	var uuid = $uuid(el);
	var row = $e(uuid);
    zkau.sendasap({
            uuid: uuid,
            cmd: "onOpenSelect",
            data: [row.position]
        }, 0);
}
