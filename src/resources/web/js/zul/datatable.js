/* Soffid table */

zk.load("zul.zul");

zkDatatable = {};
zkDatatable.init = function (ed) {
	if (ed.getAttribute("columns"))
	{
		try {
			ed.columns = JSON.parse(ed.getAttribute("columns"));
		} catch (error) {
			alert("Error decoding table descriptor "+ed.getAttribute("columns")+": "+error);
			ed.columns = [];
		}
	}
	else
		ed.columns = [];
	ed.multiselect = "true" == ed.getAttribute("multiselect");
	ed.sortColumn = ed.getAttribute("sortColumn");
	ed.sortDirection = ed.getAttribute("sortDirection");
	ed.selectedRow = -1;
	ed.enablefilter = "false" != ed.getAttribute("enablefilter");
	ed.footer = ed.getAttribute("footer") != "false";
	ed.index = [];
	ed.nextRowId = 0;
	zkDatatable.refresh(ed);
};

zkDatatable.refresh = function (t) {
	var v ;
	while ((v = t.firstChild) != null )
	{
		v.remove();
	}
	var th = document.createElement("thead");
	th.setAttribute("id", t.id+"!thead");
	t.appendChild(th);
	var tb = document.createElement("tbody");
	tb.setAttribute("id", t.id+"!tbody");
	t.appendChild(tb);
	var tf = document.createElement("tfoot");
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


zkDatatable.createFooter = function (ed) {
	var footer = document.getElementById(ed.id+"!tfoot");
	var tr = footer.firstChild ;
	if ( ! ed.footer)
	{
		if (tr != null)
		{
			tr.remove();
		}
	} else {
		if (footer.firstChild == null )
		{
			tr = document.createElement("TR");
			footer.appendChild(tr);
		}

		var td = tr.firstChild;
		if (td == null)
		{
			td = document.createElement("TD");
			td.setAttribute("colspan", ( ed.multiselect ? 1: 0) + ed.columns.length)
			tr.appendChild(td);
		}
		
		// Remove previos message
		var v ;
		while ((v = td.firstChild) != null )
		{
			v.remove();
		}
		td.appendChild( document.createTextNode ( "Total rows: "+ document.getElementById(ed.id+"!tbody").childNodes.length ));
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
		td.setAttribute("class", "filter-cell")
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
	var t = tr.parentNode/*thdead*/.parentNode/*table*/;
	// Parse filters
	for (var column =  0; column < t.columns.length; column ++)
	{
		var input = document.getElementById(t.id+"!filter."+column);
		if (input)
		{
			t.columns[column].currentFilter = input.value.toLowerCase().split(" ");
		}
		else
			t.columns[column].currentFilter = [];
	}
	
	var tb = document.getElementById(t.id+"!tbody");
	for (var row = tb.firstElementChild; row != null; row = row.nextElementSibling)
	{
		var visible = true;
		for (var column = 0; column < t.columns.length; column++)
		{
			var filter = t.columns[column].currentFilter;
			for (var f = 0; f < filter.length; f++)
			{
				if (row.childNodes[column].textContent.toLowerCase().indexOf(filter) < 0)
				{
					visible = false;
					break;
				}
			}
		}
		if ( visible ) row.style.display="";
		else row.style.display="none";
	}
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
	}
}

zkDatatable.addRow=function(ed, pos, value)
{
	pos = parseInt(pos);
	zkDatatable.addRowInternal (ed, pos, JSON.parse(value));
	zkDatatable.createFooter(ed);
	if (ed.sortColumn >= 0)
		zkDatatable.doSort(ed);
}
zkDatatable.addRows=function(ed, pos, values)
{
	pos = parseInt(pos);
	var values = JSON.parse(values);
	for (var i = 0; i < values.length; i++)
		zkDatatable.addRowInternal (ed, pos+i, values[i]);
	zkDatatable.createFooter(ed);
	if (ed.sortColumn >= 0)
		zkDatatable.doSort(ed);
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
			col.render(td, col, value);
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
			while ((v = tr.firstChild) != null )
			{
				v.remove();
			}
			zkDatatable.fillRow(ed, tr, value);
		}
	}
	
};

zkDatatable.sendClientAction=function(el,event,data)
{
	var target = el;
	while (target.tagName != 'TR')
		target = target.parentNode;
	var t = target.parentNode/*tbody*/.parentNode/*table*/;
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
			alert("Error decoding table descriptor "+ed.getAttribute("columns")+": "+error);
		}
		return true;
	case "footer":
		ed.footer = (value != "false");
		zkDatatable.createFooter(ed);
		return true;
	case "footer":
		ed.multiselect = (value != "false");
		zkDatatable.refresh(ed);
		return true;
	}
	return false;
};

zkDatatable.onSelectRow=function(ev) {
	var target = ev.currentTarget;
	var t = target.parentNode/*tbody*/.parentNode/*table*/;
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
			}
			cb.checked = true;
		}
		target.classList.add("selected");
		zkDatatable.sendSelect(t);
	} else {
		var tbody = document.getElementById(t.id+"!tbody");
		for (var row = tbody.firstElementChild; row != null; row = row.nextElementSibling)
		{
			row.classList.remove("selected")
		}
		target.classList.add("selected");
		var position = t.index.indexOf(target.id);
		if (position >= 0)
		{
			var req = {uuid: t.id, cmd: "onSelect", data : [position]};
			zkau.send (req, 5);		
		}	
	}
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

zkDatatable.onSelectAll=function(ev) {
	var cb = ev.currentTarget;
	var table = cb.parentNode/*td*/.parentNode/*tr*/.parentNode/*thead*/.parentNode/*table*/;
	var tbody = document.getElementById(table.id+"!tbody");
	var row;
	for (row = tbody.firstElementChild; row != null; row = row.nextElementSibling)
	{
		var cb2 = row.firstElementChild/*td*/.firstElementChild/*input*/;
		cb2.checked = cb.checked;
		row.classList.add("selected");
	}
	zkDatatable.sendSelect(table);
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
	} else {
		row.classList.add("selected");
	}
	zkDatatable.sendSelect(table);
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
					var v1 = a.value;
					var v2 = b.value;
					if (v1[value] < v2[value]) r = -1;
					else if (v1[value] > v2[value]) r = +1;
					else r = 0; 
				} else {
					var v1 = a.childNodes[sortColumn];
					var v2 = a.childNodes[sortColumn];
					if (v1.innerHTML < v2.innerHTML) r = -1;
					else if (v1.innerHTML > v2.innerHTML) r = +1;
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
		document.getElementById(id).remove();
		ed.index.splice(pos,1);
		zkDatatable.createFooter(ed);
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