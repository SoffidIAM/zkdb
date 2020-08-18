/* Soffid table */

zk.load("zul.zul");
zk.load("zul.widget");

zkDatasource = {};

zkDatasource.init = function (e) {
};

zkDatasource.cleanup = function (e) {
};


zkDatasource.registerInput = function (e,i) {
	if (e.inputs == null)
		e.inputs = [];
	e.inputs.push(i);
	i.zDatasource = e;
	zk.listen(i, "input", zkDatasource.onInput);
};

zkDatasource.unregisterInput = function (e,i) {
	if (e.inputs != null) {
		var p = e.inputs.indexOf(i);
		if (p >= 0)
			e.inputs.splice(i,1);
		zk.unlisten(i, "input", zkDatasource.onInput);
	}
};

zkDatasource.onInput=function(ev)
{
	var input = ev.currentTarget;
	zkDatasource.updatedElement(input);
}

zkDatasource.updatedElement=function(input) {
	var ds = input.zDatasource;
	if (ds && ds.buttons) {
		for (var i = 0; i < ds.buttons.length; i++) {
			var button = ds.buttons[i];
			button.disabled = false;
		}
		zkau.confirmClose=true;
	}
} 

zkDatasource.registerButton = function (e,i) {
	if (e.buttons == null)
		e.buttons = [];
	e.buttons.push(i);
};

zkDatasource.unregisterButton = function (e,i) {
	if (e.buttons != null) {
		var p = e.buttons.indexOf(i);
		if (p >= 0)
			e.buttons.splice(i,1);
	}
};

zkDataTxbox={};

zkDataTxbox.init = function (e) {
	zkTxbox.init(e);
	var value = e.getAttribute("dsid");
	if (value) {
		var ds = document.getElementById(value);
		if (ds) 
			zkDatasource.registerInput(ds, e);
	}
		
};

zkDataTxbox.setAttr = function (ed, name, value) {
	if (name == "dsid") {
		if (e.zDatasource) 
			zkDatasource.unregisterInput(e.zkDatasource, e);
		var ds = document.getElementById(dsid);
		if (ds) {
			zkDatasource.registerInput(ds, e);
		}
		return true;
	} else if ("disabled" == name || "readOnly" == name) {
	    var i = $real(ed)
	        , a = i.type ? i.type.toUpperCase() : "";
        "PASSWORD" != a && "TEXT" != a && "TEXTAREA" != a || zk["true" == value ? "addClass" : "rmClass"](i, "disabled" == name ? "text-disd" : "readonly")
	    return false;
	} else {
		return zkTxbox.setAttr(ed,name,value);
	}
}

zkDataTxbox.cleanup = function(e) {
	if (e.zDatasource) 
		zkDatasource.unregisterInput(e.zDatasource, e);
	zkTxbox.cleanup(e);
}

zkDataTxbox.onHide = function(a) { zkTxbox.onHide(a)};
zkDataTxbox.onselect = function(a) { zkTxbox.onselect(a)};
zkDataTxbox.onblur = function(a) { zkTxbox.onblur(a)};
zkDataTxbox._scanStop = function(a) { zkTxbox._scanStop(a)};
zkDataTxbox.updateChange = function(a,b) { zkTxbox.updateChange(a,b)};
zkDataTxbox._noonblur = function(a) { zkTxbox._noonblur(a)};
zkDataTxbox.onupdate = function(a) { zkTxbox.onupdate(a)};
zkDataTxbox.onkey = function(a) { zkTxbox.onkey(a)};
zkDataTxbox.onkeydown = function(a) { zkTxbox.onkeydown(a)};
zkDataTxbox.onfocus = function(a) { zkTxbox.onfocus(a)};
zkDataTxbox._scanChanging = function(a) { zkTxbox._scanChanging(a)};
zkDataTxbox.sendOnChanging = function(a,b) { zkTxbox.sendOnChanging(a,b)};

zkCommitButton={};

zkCommitButton.init = function (e) {
	zkButton.init(e);
	var value = e.getAttribute("dsid");
	if (value) {
		var ds = document.getElementById(value);
		if (ds) 
			zkDatasource.registerButton(ds, e);
	}
		
};
zkCommitButton.setAttr = function (ed, name, value) {
	if (name == "dsid") {
		if (e.zDatasource) 
			zkDatasource.unregisterButton(e.zkDatasource, e);
		var ds = document.getElementById(dsid);
		if (ds) {
			zkDatasource.registerButton(ds, e);
		}
		return true;
	} else {
		return false;
	}
}

zkCommitButton.cleanup = function(e) {
	if (e.zDatasource) 
		zkDatasource.unregisterButton(e.zkDatasource, e);
//	zkButton.cleanup(e);
}
