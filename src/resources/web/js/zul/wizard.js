/* Soffid table */

zk.load("zul.zul");

zkWizard = {};

zkWizard.init = function (e) {
	var selected = Number(e.getAttribute("selected"));
	e.selected = selected;
	var steps = e.getAttribute("steps");
	if (steps) {
		e.steps = JSON.parse(steps);
	} else {
		e.steps = [];
	}
	e.disabledSteps=[];
	var header = document.createElement("div");
	header.setAttribute("id", e.getAttribute("id")+"!header");
	header.setAttribute("class", "wizard-bar");
	e.insertBefore(header, e.firstElementChild);
	zkWizard.updateHeader(e);
	zkWizard.displayItems(e);
};

zkWizard.cleanup = function (e) {
};

zkWizard.enableStep=function (e, step) {
	e.disabledSteps.remove(step);
	zkWizard.updateHeader(e);
}

zkWizard.disableStep= function (e, step){
    if ( !e.disabledSteps.contains(step)) {
		e.disabledSteps.push(step);
		zkWizard.updateHeader(e);
	}
}

zkWizard.updateHeader =function(e)
{
	var width = e.offsetWidth;
	var totalwidth = 0;
	var stepposition = 0;
	var header = $e($uuid(e)+"!header");
	while (header.firstElementChild != null)
		header.firstElementChild.remove();
	for (var pos = 0; pos < e.steps.length; pos++ ) {
	    if (! e.disabledSteps.contains(pos.toString())) {
			var part = document.createElement ("div");
			part.setAttribute("class", pos == e.selected ? "active-step" :
					pos < e.selected ? "previous-step": "next-step");
			part.innerText = e.steps[pos];
			header.appendChild(part);
			totalwidth += part.offsetWidth;
			if (pos < e.selected) stepposition += part.offsetWidth;
			// Add arrow
			var s = document.createElement("div");
			var next = pos+1;
			while (next < e.steps.length && e.disabledSteps.contains(next.toString()))
				next++;
			if (next == e.selected)
				s.setAttribute("class", "separator-p");
			else if (pos == e.selected)
				s.setAttribute("class", "separator-n");
			else
				s.setAttribute("class", "separator-d");
			header.appendChild(s);
			totalwidth += s.offsetWidth;
			if (pos < e.selected) stepposition += s.offsetWidth;
		}
	}
	if (totalwidth > width) {
		var maxpos = totalwidth - width;
		if (stepposition > maxpos) stepposition = maxpos;
		header.style.left = "-" + stepposition + "px";
	}
}

zkWizard.displayItems =function(e)
{
	var header = e.firstElementChild;
	var n = header.nextElementSibling;
	var p = 0;
	while (n != null) {
		if (p == e.selected)
			n.style.display = "";
		else
			n.style.display = "none"
		p++;
		n = n.nextElementSibling;
	}
}

/** Called by the server to set the attribute. */
zkWizard.setAttr = function (s, name, value) {
	switch (name) {
	case "selected":
		s.selected = Number(value);
		zkWizard.updateHeader(s);
		zkWizard.displayItems(s);
		return true;
	case "steps":
		e.steps = JSON.parse(value);
		zkWizard.updateHeader(s);
		return true;
	}
	return false;
};

