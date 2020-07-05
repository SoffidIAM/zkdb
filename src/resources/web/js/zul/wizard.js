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
	var header = document.createElement("div");
	header.setAttribute("id", e.getAttribute("id")+"!header");
	header.setAttribute("class", "wizard-bar");
	e.insertBefore(header, e.firstElementChild);
	zkWizard.updateHeader(e);
	zkWizard.displayItems(e);
};

zkWizard.cleanup = function (e) {
};

zkWizard.updateHeader =function(e)
{
	var header = $e($uuid(e)+"!header");
	while (header.firstElementChild != null)
		header.firstElementChild.remove();
	for (var pos = 0; pos < e.steps.length; pos++ ) {
		if (pos > 0) {
			var s = document.createElement("div");
			if (pos == e.selected)
				s.setAttribute("class", "separator-p");
			else if (pos == e.selected + 1)
				s.setAttribute("class", "separator-n");
			else
				s.setAttribute("class", "separator-d");
			header.appendChild(s);
		}
		var part = document.createElement ("div");
		part.setAttribute("class", pos == e.selected ? "active-step" :
				pos < e.selected ? "previous-step": "next-step");
		part.innerText = e.steps[pos];
		header.appendChild(part);
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

