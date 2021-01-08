/* Soffid table */

zk.load("zul.zul");

zkSwitch = {};
zkSwitch.init = function (s) {
	s.checked = "checked" == s.getAttribute("checked");
	zkSwitch.syncSlider(s);
	s.addEventListener("click", zkSwitch.onClick);
	s.disabled = "disabled" == s.getAttribute("disabled");
	if (s.disabled)
		s.classList.add("disabled");
	else
		s.classList.remove("disabled");
	var slider = document.getElementById(s.id+"!slider");
	slider.zkSwitch = s; 
	slider.addEventListener("mousedown", zkSwitch.onSliderMouseDown);
}

zkSwitch.syncSlider = function (s) {
	var slider = document.getElementById(s.id+"!slider");
	slider.style.position = "absolute";
	slider.style.left = "";
	slider.classList.remove(s.checked?"slider-off":"slider-on");
	slider.classList.add(s.checked?"slider-on":"slider-off");
}

zkSwitch.onClick = function (ev) {
	var s = ev.currentTarget;
	if ( !s.getAttribute("disabled"))
	{
		var slider = document.getElementById(s.id+"!slider");
		zkSwitch.slider = slider;
		document.removeEventListener("mousemove", zkSwitch.onSliderMousemove);
		document.addEventListener("mouseup", zkSwitch.onSliderMouseup);
		if (slider.classList.contains("dragging"))
		{
			slider.classList.add("not-dragging");
			slider.classList.remove("dragging");
		} else {
			s.checked = ! s.checked;
		}
		var req = {uuid: s.id, cmd: "onCheck", data : [s.checked]};
		zkau.send (req, 5);		
		zkSwitch.syncSlider(s);
	}
}

zkSwitch.onSliderMouseDown=function (ev) {
	var slider = ev.currentTarget;
	var s = slider.zkSwitch;
	if ( !s.disabled)
	{
		if (slider.classList.contains("not-dragging"))
		{
			zkSwitch.slider = slider;
			zkSwitch.initialX = ev.clientX;
			if ( s.checked ) zkSwitch.initialX -= slider.clientWidth;
			document.addEventListener("mousemove", zkSwitch.onSliderMousemove);
			document.addEventListener("mouseup", zkSwitch.onSliderMouseup);
		}
	}
}

zkSwitch.onSliderMousemove=function (ev) {
	var slider = zkSwitch.slider;
	slider.classList.add("dragging");
	slider.classList.remove("not-dragging");
	var s = slider.zkSwitch;
	var x = ev.clientX - zkSwitch.initialX;
	x = x - slider.clientLeft;
	if (x < 0) x = 0;
	var rect = s.getBoundingClientRect();
	if (x > slider.clientWidth) x = slider.clientWidth;
	slider.style.left = new String(x) + "px";
	s.checked = x > slider.clientWidth / 2;
}

zkSwitch.onSliderMouseup=function (ev) {
	var s = zkSwitch.slider.zkSwitch;
	document.removeEventListener("mousemove", zkSwitch.onSliderMousemove);
	document.removeEventListener("mouseup", zkSwitch.onSliderMouseup);
}

/** Called by the server to set the attribute. */
zkSwitch.setAttr = function (s, name, value) {
	switch (name) {
	case "checked":
		s.checked = value == "checked" || value == "true" || value == true;
		zkSwitch.syncSlider(s);
		return true;
	case "disabled":
		s.disabled =  value == "checked" || value == "true" || value == true;
		if (s.disabled) {
			s.classList.add("disabled");
			s.setAttribute("disabled", "disabled");
		}
		else
		{
			s.classList.remove("disabled");
			s.removeAttribute("disabled");
		}
	}
	return false;
};

