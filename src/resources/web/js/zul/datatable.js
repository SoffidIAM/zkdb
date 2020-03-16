/* Soffid table */

zk.load("zul.zul");

zkDatatable = {};

zkDatatable.init = function (ed) {
	var c = ed.getAttribute("columns");
	if (c)
	{
		try {
			ed.columns = JSON.parse(c);
		} catch (error) {
			alert("Error decoding table descriptor "+c+": "+error);
			ed.columns = [];
		}
	}
	else
		ed.columns = [];
	ed.multiselect = "true" == ed.getAttribute("multiselect");
	ed.sortColumn = ed.getAttribute("sortColumn");
	ed.sortDirection = ed.getAttribute("sortDirection");
	ed.enablefilter = "false" != ed.getAttribute("enablefilter");
	ed.footer = ed.getAttribute("footer") != "false";
	ed.index = [];
	ed.nextRowId = 0;
	ed.maxheight = ed.getAttribute("maxheight");
	ed.selectedPosition = 0;
	ed.selectedTr = null;
	ed.count = 0;
	if (!ed.pagers)
		ed.pagers=[];
	zkDatatable.refresh(ed);
};

zkDatatable.registerPager=function(ed, pager) {
	if (! ed.pagers) 
		ed.pagers = [];
	
	ed.pagers.push(pager);
	zkDatatable.updatePagers(ed);
}

zkDatatable.deregisterPager=function(ed, pager) {
	if (ed && ed.pagers) {
		var i = ed.pagers.indexOf(pager);
		if (i >= 0)
			ed.pagers.splice(i, 1);
	}
}

zkDatatable.updatePagers=function(ed) {
	for (var i = 0; i < ed.pagers.length; i++)
		zkPager.update(ed.pagers[i], ed.selectedPosition, ed.count);
}

zkDatatable.onSize=function(ed) {
	zkDatatable.fixupColumns(ed);
}

zkDatatable.refresh = function (t) {
	var v ;
	while ((v = t.firstChild) != null )
	{
		v.remove();
	}
	try {
		var th = document.createElement("table");
		th.setAttribute("id", t.id+"!thead");
		th.setAttribute("class", "thead")
		t.appendChild(th);
		var tbd = document.createElement("div")
		tbd.setAttribute("id", t.id+"!tbodydiv");
		tbd.setAttribute("class", "tbodydiv")
		t.appendChild(tbd);
		if (t.maxheight)
			tbd.style.maxHeight = t.maxheight;
		var tb = document.createElement("table");
		tb.setAttribute("id", t.id+"!tbody");
		tb.setAttribute("class", "tbody")
		tbd.appendChild(tb);
		var tf = document.createElement("div");
		tf.setAttribute("class", "tfoot")
		tf.setAttribute("id", t.id+"!tfoot");
		t.appendChild(tf);
		var tr = document.createElement("tr");
		th.appendChild(tr);
		zkDatatable.createHeaders ( t, tr );
		if ( t.enablefilter)
		{
			tr = document.createElement("tr");
			th.appendChild(tr);
			zkDatatable.createFilters(t, tr);
		}
		zkDatatable.createFooter(t);
	} catch (e) {
		console.log(e)
	}
};

zkDatatable.createHeaders = function (ed, tr) {
	var v ;
	while ((v = tr.firstChild) != null )
	{
		v.remove();
	}
	if (ed.multiselect)
	{
		var td = document.createElement("td");
		tr.appendChild(td);
		td.setAttribute("class", "selector");
		var cb = document.createElement("input");
		cb.setAttribute("type", "checkbox");
		cb.addEventListener("input", zkDatatable.onSelectAll)
		td.appendChild(cb);
	}
	for (var column =  0; column < ed.columns.length; column ++)
	{
		var td = document.createElement("td");
		tr.appendChild(td);
		if ( ed.columns[column].sort != false)
		{
			zkDatatable.createSortArrows(ed,td, column);
			td.setAttribute("class", "sortable-column")
			td.column = column;
			tr.addEventListener("click", zkDatatable.onSort)
		}
		td.appendChild( document.createTextNode ( ed.columns[column].name ));
	}
};

zkDatatable.fixupColumns=function(ed) {
	var tbodydiv = document.getElementById(ed.id+"!tbodydiv");
	var tbody = document.getElementById(ed.id+"!tbody");
	var thead = document.getElementById(ed.id+"!thead");
	var tfoot = document.getElementById(ed.id+"!tfoot");
	for (var tr = tbody.firstElementChild; tr != null; tr = tr.nextElementSibling)
	{
		if ( "none" != tr.style.display)
			break;
	}
	var trh = thead.firstElementChild; 
	if (tr && trh)
	{
		var size = 0.0;
		var v2 = trh.firstElementChild;
		for (var v = tr.firstElementChild; v != null && v2 != null; v = v.nextElementSibling, v2 = v2.nextElementSibling )
		{
			var w = v.offsetWidth ; //- v2.offsetWidth + v2.clientWidth - 2 /*border*/;
			if (v.nextElementSibling == null)
				w += tbodydiv.offsetWidth - tbodydiv.clientWidth; // Add scroll bar width
			v2.style.width = "" + w + "px";
			v2.style.maxWidth = "" + w + "px";
			size += w;
		}	
		if (tfoot)
			tfoot.style.width = "" + (size+1) + "px";
	}
}


zkDatatable.createFooter = function (ed) {
	var footer = document.getElementById(ed.id+"!tfoot");
	var v ;
	while ((v = footer.firstChild) != null )
	{
		v.remove();
	}
	if (  ed.footer)
	{
		footer.appendChild( document.createTextNode ( "Total rows: "+ document.getElementById(ed.id+"!tbody").childNodes.length ));
	}
};

zkDatatable.createFilters = function (ed, tr) {
	var v ;
	while ((v = tr.firstChild) != null )
	{
		v.remove();
	}
	if (ed.multiselect) {
		var td = document.createElement("td");
		td.setAttribute("class", "selector")
		tr.appendChild(td);
	}
	for (var column =  0; column < ed.columns.length; column ++)
	{
		var td = document.createElement("td");
		td.setAttribute("class", "filter-cell")
		tr.appendChild(td);
		if ( ed.columns[column].filter != false)
		{
			var tb = document.createElement("input");
			tb.setAttribute("type", "text");
			tb.setAttribute("class", "filter");
			tb.setAttribute("placeholder", "Filter");
			tb.column = column;
			tb.addEventListener("input", zkDatatable.onFilter)
			tb.setAttribute("id", ed.id+"!filter."+column);
			td.appendChild(tb);
		}
	}
};

zkDatatable.onFilter=function(ev) {
	var tr = ev.target.parentNode/*td*/.parentNode;
	var t = tr.parentNode/*thead*/.parentNode/*table*/;
	// Parse filters
	for (var column = 0; column < t.columns.length; column ++)
	{
		var input = document.getElementById(t.id+"!filter."+column);
		if (input)
		{
			t.columns[column].currentFilter = input.value.toLowerCase().split(" ");
		}
		else
			t.columns[column].currentFilter = [];
	}
	zkDatatable.doFilter(t);
}

zkDatatable.doFilter=function(t) {
	t.count = 0;
	t.selectedPosition = 0;
	var tb = document.getElementById(t.id+"!tbody");
	for (var row = tb.firstElementChild; row != null; row = row.nextElementSibling)
	{
		var visible = true;
		for (var column = 0; column < t.columns.length; column++)
		{
			var filter = t.columns[column].currentFilter;
			if (filter) {
				for (var f = 0; f < filter.length; f++)
				{
					if (row.childNodes[ t.multiselect ? column+1: column].textContent.toLowerCase().indexOf(filter) < 0)
					{
						visible = false;
						break;
					}
				}
			}
		}
		if ( visible ) {
			t.count ++; 
			if (row == t.selectedTr)
				t.selectedPosition = t.count;
			row.style.display=""; 
		}
		else row.style.display="none";
	}
	zkDatatable.fixupColumns(t);
	zkDatatable.updatePagers(t);
}

zkDatatable.createSortArrows=function(ed, td, column)
{
	var div = document.createElement("div");
	div.setAttribute("class", "sort-indicator");
	td.appendChild(div);
	if (column == ed.sortColumn)
	{
		var div1 = document.createElement("div");
		if (ed.sortDirection == +1)
			div1.setAttribute("class", "sort-indicator-disabled");
		else
			div1.setAttribute("class", "sort-indicator-enabled");
		div1.innerHTML="&#x25b2;";
		div.appendChild(div1);
		var div2 = document.createElement("div");
		if (ed.sortDirection == -1)
			div2.setAttribute("class", "sort-indicator-disabled");
		else
			div2.setAttribute("class", "sort-indicator-enabled");
		div2.innerHTML="&#x25bc;";
		div.appendChild(div2);
	}
	else
	{
		var div1 = document.createElement("div");
		div1.innerHTML="&#x25b3;";
		div.appendChild(div1);
		var div2 = document.createElement("div");
		div2.innerHTML="&#x25bd;";
		div.appendChild(div2);
	}
}

zkDatatable.setData = function(ed, data) {
	var t = document.getElementById(ed.id+"!tbody");
	if (t) {
		// Clean document
		ed.count = 0;
		ed.selectedPosition = 0;
		var v ;
		while ((v = t.firstChild) != null )
		{
			v.remove();
		}
		// Generate rows
		ed.index = [] ;
		data = JSON.parse(data);
		for (var i = 0; i < data.length; i++ )
		{
			zkDatatable.addRowInternal (ed, i, data[i] ); 
		}
		zkDatatable.createFooter(ed);
		if (ed.sortColumn >= 0)
			zkDatatable.doSort(ed);
		zkDatatable.doFilter(ed);
	}
}


zkDatatable.addRow=function(ed, pos, value)
{
	pos = parseInt(pos);
	zkDatatable.addRowInternal (ed, pos, JSON.parse(value));
	zkDatatable.createFooter(ed);
	if (ed.sortColumn >= 0)
		zkDatatable.doSort(ed);
	zkDatatable.fixupColumns(ed);
	ed.count ++;
	zkDatatable.findSelectedPosition(ed);
}

zkDatatable.addRows=function(ed, pos, values)
{
	pos = parseInt(pos);
	var values = JSON.parse(values);
	for (var i = 0; i < values.length; i++)
		zkDatatable.addRowInternal (ed, pos+i, values[i]);
	zkDatatable.createFooter(ed);
	ed.count += values.length;
	if (ed.sortColumn >= 0)
		zkDatatable.doSort(ed);
	zkDatatable.fixupColumns(ed);
	zkDatatable.findSelectedPosition(ed);
}

zkDatatable.addRowInternal=function(ed, pos, value)
{
	var t = document.getElementById(ed.id+"!tbody");
	var tr = document.createElement("tr");
	tr.addEventListener("click", zkDatatable.onSelectRow)
	tr.id = ed.id+"!row."+(ed.nextRowId++);
	if ( pos >= ed.index.length)
	{
		ed.index[pos] = tr.id;
	}	
	else
	{
		ed.index.splice (pos, 0, tr.id);
	}
		
	t.appendChild(tr);
	
	zkDatatable.fillRow(ed, tr, value);
}

zkDatatable.fillRow=function(ed,tr,value)
{
	if (ed.multiselect)
	{
		var td = document.createElement("td");
		td.setAttribute("class", "selector");
		tr.appendChild(td);
		var cb = document.createElement("input");
		cb.setAttribute("type", "checkbox");
		cb.addEventListener("input", zkDatatable.onSelect)
		cb.addEventListener("click", zkDatatable.dontBubble)
		td.appendChild(cb);
	}
	tr.value = value;
	for (var column =  0; column < ed.columns.length; column ++)
	{
		var td = document.createElement("td");
		tr.appendChild(td);
		var col = ed.columns[column];
		if (col.template)
			td.innerHTML = zkDatatable.replaceExpressions(col.template, value);
		else if (col.render)
			window[col.render](td, col, value);
		else if (col.value)
		{
			if (value[col.value] == undefined)
				td.appendChild( document.createTextNode ( "" ));
			else
				td.appendChild( document.createTextNode ( value[col.value] ));
		}
		else
			td.appendChild( document.createTextNode ( value[col.name] ));
	}
}

zkDatatable.dontBubble=function(ev) {
	ev.stopPropagation();
}

zkDatatable.updateRow=function(ed, pos, value)
{
	value = JSON.parse(value);
	var t = document.getElementById(ed.id+"!tbody");
	var trid = ed.index[pos];
	if (trid)
	{
		var tr = document.getElementById(trid);
		if (tr)
		{
			var v ;
			var selected;
			if ( ed.multiselect)
				selected = tr.firstChild.firstChild.checked;
			while ((v = tr.firstChild) != null )
			{
				v.remove();
			}
			zkDatatable.fillRow(ed, tr, value);
			if ( ed.multiselect)
				tr.firstChild.firstChild.checked = selected;
		}
	}
	zkDatatable.fixupColumns(ed);
	
};

zkDatatable.sendClientAction=function(el,event,data)
{
	var target = el;
	while (target.tagName != 'TR')
		target = target.parentNode;
	var t = target.parentNode/*tbody*/.parentNode/*bodydiv*/.parentNode/*table*/;
	var position = t.index.indexOf(target.id);
	if (position >= 0)
	{
		zkau.send ({uuid: t.id, cmd: "onSelect", data : [position]}, 5);		
		if (data) {
			data.splice(0,0, event);
		} else {
			data = [ event];
		}
		var req = {uuid: t.id, cmd: "onClientAction", data:data};
		zkau.send (req, 5);		
	}	
}

zkDatatable.evaluateInContext = function (js, context) {
    return function() { return eval(js); }.call(context);
}

zkDatatable.replaceExpressions = function (template,value) {
	var v = "";
	var j = 0;
	do {
		var i = template.indexOf("${", j); 
		if (i < 0)
		{
			v = v + template.substring(j);
			break;
		}
		else
		{
			v = v + template.substring(j,i);
			var i2 = template.indexOf("}", i);
			if (i2 < 0) {
				v = v+template.substring(i);
				break;
			} else {
				j = i2 + 1;
				var expr = template.substring(i+2, i2).trim();
				if ( value[expr] )
					v = v + zkDatatable.escapeHTML(value[expr]);
				else
					v = v + zkDatatable.escapeHTML(zkDatatable.evaluateInContext(expr, value));
			}
		}
	} while (true);
	return v;
}

zkDatatable.escapeHTML=function(t) {
    return t.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

zkDatatable.cleanup = function (ed) {
};

/** Called by the server to set the attribute. */
zkDatatable.setAttr = function (ed, name, value) {
	switch (name) {
	case "rows":
		ed.rows = value;
		return true;
	case "columns":
		try {
			ed.columns = JSON.parse(value);
		} catch (error) {
			alert("Error decoding table descriptor "+value+": "+error);
		}
		return true;
	case "footer":
		ed.footer = (value != "false");
		zkDatatable.createFooter(ed);
		return true;
	case "multiselect":
		ed.multiselect = (value != "false");
		zkDatatable.refresh(ed);
		return true;
	case "maxheight":
		ed.maxheight = value;
		document.getElementById(ed.id+"!tbody")
			.style.maxHeight = value;
		return true;
	}
	return false;
};

zkDatatable.findSelectedPosition=function(t) {
	var count = 0;
	t.selectedPosition = 0;
	var tb = document.getElementById(t.id+"!tbody");
	for (var row = tb.firstElementChild; row != null; row = row.nextElementSibling)
	{
		if ( row.style.display != "none" ) {
			count ++; 
			if (row == t.selectedTr) {
				t.selectedPosition = count;
				break;
			}
		}
	}
	zkDatatable.updatePagers(t);
};

zkDatatable.onSelectRow=function(ev) {
	var target = ev.currentTarget;
	var t = target.parentNode/*tbody*/.parentNode/*bodydiv*/.parentNode/*table*/;
	if ( t.multiselect )
	{
		var cb = target.firstElementChild/*td*/.firstElementChild/*input*/;
		if (!cb.checked)
		{
			var row = zkDatatable.findSelectedOne(t);
			if (row != null)
			{
				var cb2 = row.firstElementChild/*td*/.firstElementChild/*input*/;
				cb2.checked = false;
				row.classList.remove("selected");
			}
			cb.checked = true;
			target.classList.add("selected");
			t.selectedTr = target;
		} else {
			t.selectedTr = zkDatatable.findSelectedTr(t);
			cb.checked = false;
			target.classList.remove("selected");			
		}
		zkDatatable.sendSelect(t);
	} else {
		var tbody = document.getElementById(t.id+"!tbody");
		var row = t.selectedTr;
		if (row != null)
		{
			row.classList.remove("selected")
		}
		target.classList.add("selected");
		t.selectedTr = target;
		var position = t.index.indexOf(target.id);
		if (position >= 0)
		{
			var req = {uuid: t.id, cmd: "onSelect", data : [position]};
			zkau.send (req, 5);		
		}	
	}
	zkDatatable.findSelectedPosition(t);
}

zkDatatable.findSelectedOne=function(table) {
	var selected = null;
	var tbody = document.getElementById(table.id+"!tbody");
	var row;
	for (row = tbody.firstElementChild; row != null; row = row.nextElementSibling)
	{
		var cb2 = row.firstElementChild/*td*/.firstElementChild/*input*/;
		if ( cb2.checked )
		{
			if (selected == null) selected = row;
			else return null;
		}
	}
	return selected;	
}

zkDatatable.findSelectedTr=function(table) {
	var selected = null;
	var tbody = document.getElementById(table.id+"!tbody");
	var row;
	for (row = tbody.firstElementChild; row != null; row = row.nextElementSibling)
	{
		var cb2 = row.firstElementChild/*td*/.firstElementChild/*input*/;
		if ( cb2.checked )
		{
			return row;
		}
	}
	return null;	
}

zkDatatable.onSelectAll=function(ev) {
	var cb = ev.currentTarget;
	var table = cb.parentNode/*td*/.parentNode/*tr*/.parentNode/*thead*/.parentNode/*table*/;
	var tbody = document.getElementById(table.id+"!tbody");
	var row;
	table.selectedTr = null;
	for (row = tbody.firstElementChild; row != null; row = row.nextElementSibling)
	{
		if (selectedTr == null)
			selectedTr = row;
		var cb2 = row.firstElementChild/*td*/.firstElementChild/*input*/;
		cb2.checked = cb.checked;
		row.classList.add("selected");
	}
	zkDatatable.sendSelect(table);
	zkDatatable.findSelectedPosition(table);
}

zkDatatable.onSelect=function(ev) {
	var cb = ev.currentTarget;
	var row = cb.parentNode/*td*/.parentNode/*tr*/;
	var table = row.parentNode/*thead*/.parentNode/*table*/;
	if (!cb.checked)
	{
		var cb2 = table.firstElementChild/*thead*/.firstElementChild/*tr*/.firstElementChild/*td*/.firstElementChild;
		cb2.checked = false;
		row.classList.remove("selected");
		table.selectedTr = zkDatatable.findSelectedTr(table);
	} else {
		table.selectedTr = row;
		row.classList.add("selected");
	}
	zkDatatable.sendSelect(table);
}

/** Selected by the server **/
zkDatatable.setSelected=function(t, pos) {
	var tbody = document.getElementById(t.id+"!tbody");
	for (var row = tbody.firstElementChild; row != null; row = row.nextElementSibling)
	{
		row.classList.remove("selected")
		if ( t.multiselect )
		{
			var cb = row.firstElementChild/*td*/.firstElementChild/*input*/;
			cb.checked =false;
		}
	}
	t.selectedTr = null;
	var rowid =  t.index[pos];
	if (rowid)
	{
		var row = document.getElementById(rowid);
		if (row)
		{
			if ( t.multiselect )
			{
				var cb = row.firstElementChild/*td*/.firstElementChild/*input*/;
				cb.checked = true;
			}
			row.classList.add("selected");
			t.selectedTr = row;
		}
		zkDatatable.findSelectedPosition(t);
	} else {
		t.selectedPosition = 0;
		zkDatatable.updatePagers(t);
	}
}

zkDatatable.sendSelect=function(table) {
	var selected=[];
	var tbody = document.getElementById(table.id+"!tbody"); 
	for (var row = tbody.firstElementChild; row != null; row = row.nextElementSibling)
	{
		var cb2 = row.firstElementChild/*td*/.firstElementChild/*input*/;
		if (cb2.checked)
		{
			var position = table.index.indexOf(row.id);
			if (position >= 0)
			{
				selected.push(position);
			}	
		}
	}
	var req = {uuid: table.id, cmd: "onMultiSelect", data : [ JSON.stringify(selected)]};
	zkau.send (req, 5);		
}

zkDatatable.onSort=function(ev) {
	var target = ev.target;
	while (target.tagName != 'TD')
		target = target.parentNode;
	var column = target.column;
	if (column != undefined && column != null)
	{
		var ed = target.parentNode/*tr*/.parentNode/*thead*/.parentNode/*table*/;
		if (ed.sortColumn == column)
		{
			ed.sortDirection = - ed.sortDirection;
		} else {
			ed.sortColumn = column;
			ed.sortDirection = +1;
		}
		zkDatatable.createHeaders (ed, target.parentNode);
		zkDatatable.doSort(ed);
		zkDatatable.fixupColumns(ed);
		zkDatatable.findSelectedPosition(ed);
	}
}

zkDatatable.doSort=function(ed) {
	var sortColumn = ed.sortColumn;
	var value = ed.columns[ed.sortColumn].value;
	var direction = ed.sortDirection;
	var tbody = document.getElementById(ed.id+"!tbody");
	var children = [...tbody.children];
	zkDatatable.quickSort(children,
			(a,b) => {
				var r;
				if (value) {
					var v1 = new String(a.value[value]).toLowerCase();
					var v2 = new String(b.value[value]).toLowerCase();
					if (v1 < v2) r = -1;
					else if (v1 > v2) r = +1;
					else r = 0; 
				} else {
					var v1 = a.childNodes[sortColumn].innerHTML.toLowerCase();
					var v2 = b.childNodes[sortColumn].innerHTML.toLowerCase();
					if (v1 < v2) r = -1;
					else if (v1 > v2) r = +1;
					else r = 0; 
				}
				return r*direction;
			}
		);
	for (var i = 0; i < children.length; i++)
		tbody.insertBefore(children[i], null);
}

zkDatatable.deleteRow=function(ed, pos)
{
	if (ed.index[pos])
	{
		var id = ed.index[pos];
		var row = document.getElementById(id);
		if (row == ed.selectedTr)
			ed.selectedTr = null;
		row.remove();
		ed.index.splice(pos,1);
		zkDatatable.createFooter(ed);
		zkDatatable.fixupColumns(ed);
		ed.count --;
		zkDatatable.findSelectedPosition(ed);
	}
}



zkDatatable.quickSort = function (
  arrayToSort,
  sortingAlgorithm = defaultSortingAlgorithm)
{
  // immutable version
  const swapArrayElements = (arrayToSwap, i, j) => {
    const a = arrayToSwap[i];
    arrayToSwap[i] = arrayToSwap[j];
    arrayToSwap[j] = a;
  };; 

  const partition = (arrayToDivide, start, end) => {
    const pivot = arrayToDivide[end];
    let splitIndex = start;
    for (let j = start; j <= end - 1; j++) {
      const sortValue = sortingAlgorithm(arrayToDivide[j], pivot);
      if (sortValue === -1) {
        swapArrayElements(arrayToDivide, splitIndex, j);
        splitIndex++;
      }
    }
    swapArrayElements(arrayToDivide, splitIndex, end);
    return splitIndex;
  };

  // Recursively sort sub-arrays.
  const recursiveSort = (arraytoSort, start, end) => {
    // stop condition
    if (start < end) {
      const pivotPosition = partition(arraytoSort, start, end);
      recursiveSort(arraytoSort, start, pivotPosition - 1);
      recursiveSort(arraytoSort, pivotPosition + 1, end);
    }
  };

  // Sort the entire array.
  recursiveSort(arrayToSort, 0, arrayToSort.length - 1);
  return arrayToSort;
};

zkDatatable.next=function(t)
{
	var row = t.selectedTr;
	if (row) {
		var next = row.nextSibling;
		while (next != null && next.style.display == 'none')
			next = next.nextSibling;
		if (next) {
			if (! next.classList.contains("selected") && row.classList.contains("selected")) {
				row.classList.remove("selected");
				if (t.multiselect) {
					var cb = row.firstElementChild/*td*/.firstElementChild/*input*/;
					cb.checked = false;
				}
				next.classList.add("selected");
			}
			if (t.multiselect) {
				var cb = next.firstElementChild/*td*/.firstElementChild/*input*/;
				cb.checked = true;
				zkDatatable.sendSelect(t);
			} else {
				var position = t.index.indexOf(next.id);
				if (position >= 0)
				{
					var req = {uuid: t.id, cmd: "onSelect", data : [position]};
					zkau.send (req, 5);		
				}	
			}
			t.selectedTr = next;
			t.selectedPosition ++;
			zkDatatable.updatePagers(t);
		}
	}
}

zkDatatable.previous=function(t)
{
	var row = t.selectedTr;
	if (row) {
		var next = row.previousSibling;
		while (next != null && next.style.display == 'none')
			next = next.previousSibling;
		if (next) {
			if (! next.classList.contains("selected") && row.classList.contains("selected")) {
				row.classList.remove("selected");
				if (t.multiselect) {
					var cb = row.firstElementChild/*td*/.firstElementChild/*input*/;
					cb.checked = false;
				}
				next.classList.add("selected");
			}
			if (t.multiselect) {
				var cb = next.firstElementChild/*td*/.firstElementChild/*input*/;
				cb.checked = true;
				zkDatatable.sendSelect(t);
			} else {
				var position = t.index.indexOf(next.id);
				if (position >= 0)
				{
					var req = {uuid: t.id, cmd: "onSelect", data : [position]};
					zkau.send (req, 5);		
				}	
			}
			t.selectedTr = next;
			t.selectedPosition --;
			zkDatatable.updatePagers(t);
			
		}
	}
}

