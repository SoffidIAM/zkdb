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
	slider.style.left = s.checked ? new String(slider.clientWidth) +"px": "0px";
}

zkSwitch.onClick = function (ev) {
	var s = ev.currentTarget;
	if ( !s.getAttribute("disabled"))
	{
		document.removeEventListener("mousemove", zkSwitch.onSliderMousemove);
		document.addEventListener("mouseup", zkSwitch.onSliderMouseup);
		var slider = document.getElementById(s.id+"!slider");
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
		if (s.disabled)
			s.classList.add("disabled");
		else
			s.classList.remove("disabled");
	}
	return false;
};

/*
define(['jquery'], function ($) {
	"use strict";

	function getSides($elem, $slider) {
		var left = -2;
		var right = $elem.outerWidth() - $slider.outerWidth() + 2;
		var center = right / 2 + left;
		return {left: left, right: right, center: center};
	}

	var methods = {
		init: function () {
			return this.each(function () {
				var $elem = $(this);
				var $slider = $('<span>', {'class': 'slider not-dragging'});
				$slider.append($('<span>', {'class': 'handle'}));
				var data = $elem.data('switch');
				if (!data)
				{
					data = {};
					data.activated = undefined;
					data.customized = null;
					$elem.data('switch', data);
				}

				function mouseup(e) {
					if($(this).hasClass('disabled'))
					{
						return false;
					}

					$slider.addClass('not-dragging');
					$(document).off('mousemove.slider').off('mouseup.slider');
					var s = getSides($elem, $slider);
					$slider.css('left', data.activated ? s.right : s.left);
					if (data.activated !== data.initialActivated)
					{
						$elem.trigger('changed', data.activated);
					}
					return false;
				}

				function mousemove(e) {
					if($(this).hasClass('disabled'))
					{
						return false;
					}

					var s = getSides($elem, $slider);
					var x = e.pageX - data.initialPageX + data.initialLeft;
					if (x < s.left)
					{
						x = s.left;
					}
					if (x > s.right)
					{
						x = s.right;
					}

					data.activated = x >= s.center;
					$elem.toggleClass('activated', data.activated);

					$slider.css('left', x);
					return false;
				}

				var contents = $elem.text();
				$elem.text('');

				var classNames = $elem.attr('class');
				var activated = $elem.hasClass('activated');

				$elem
					.removeClass()
					.addClass('_gnome-switch')
					.append($('<span>', {'class': 'on'}).text("ON"))
					.append($('<span>', {'class': 'off'}).text("OFF"))
					.append($('<span>', {'class': 'custom-content'}))
					.append($slider)

					// Disable selection.
					.css({
						'user-select': 'none',
						'-moz-user-select': 'none'
					})
					.attr('unselectable', 'on')
					.on('selectstart', function () {
						return false;
					});

				if (contents)
				{
					methods.customize.call($elem, contents, classNames);
				}
				methods.activate.call($elem, activated);

				$slider.on('mousedown', (e) => {
					if($elem.hasClass('disabled'))
					{
						return false;
					}

					data.initialActivated = data.activated;
					data.initialPageX = e.pageX;
					var left = $slider.position().left;
					data.initialLeft = left;
					$slider.css({'position': 'absolute', 'left': left});
					$slider.removeClass('not-dragging');
					$(document).on({
						'mousemove.slider': mousemove,
						'mouseup.slider': mouseup
					});
					return false;
				});

				$elem.on('click', function (e) {
					if($(this).hasClass('disabled'))
					{
						return false;
					}

					var doToggle;
					var isActivated = !!data.activated;

					if (data.customized !== null)
					{
						return true;
					}

					if (data.initialPageX === undefined)
					{
						doToggle = true;
					}
					else
					{
						// Make sure we didn't drag before toggling.
						var travelDistance = Math.abs(e.pageX - data.initialPageX);
						doToggle = travelDistance < 4 && isActivated === data.initialActivated;

						delete data.initialActivated;
						delete data.initialPageX;
						delete data.initialLeft;
					}

					if (doToggle)
					{
						methods.activate.call($elem, !isActivated);
					}

					e.stopImmediatePropagation();
					return false;
				});
			});
		},

		activate: function (value) {
			return this.each(function () {
				var $elem = $(this);

				var data = $elem.data('switch');
				if (data.activated === value)
				{
					return;
				}

				data.activated = value;

				$elem.trigger('changed', value);
				$elem.toggleClass('activated', value);

				var $slider = $elem.find('span.slider');
				var s = getSides($elem, $slider);
				$slider.css('left', value ? s.right : s.left);
			});
		},

		customize: function (label, styleClass, title) {
			return this.each(function () {
				var $elem = $(this);
				var $customContent = $elem.find('.custom-content');

				var data = $elem.data('switch');
				var customized = !!label || !!styleClass;

				if (data.customized)
				{
					$customContent.text('').removeClass(data.customized.styleClass);
					$elem.removeClass('customized').removeClass(data.customized.styleClass);
					$elem.removeProp('title');
					data.customized = null;
				}

				if (customized)
				{
					$customContent.text(label).addClass(styleClass);
					$elem.addClass('customized').addClass(styleClass);
					data.customized = {label: label, styleClass: styleClass};
				}

				if(title)
				{
					$elem.prop('title', title);
				}
			});
		},

		disable: function() {
			return this.addClass('disabled');
		},

		enable: function() {
			return this.removeClass('disabled');
		}
	};

	$.fn.switchify = function (method) {
		if(!method || typeof method === 'object')
		{
			return methods.init.apply(this, arguments);
		}

		if(!$(this).data('switch'))
		{
			methods.init.apply(this);
		}

		if (methods[method])
		{
			return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
		}
		else
		{
			$.error('Method ' + method + ' does not exist on jQuery.switchify');
		}
	};

});
*/