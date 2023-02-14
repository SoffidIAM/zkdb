/* Soffid table */

zk.load("zul.zul");

zkDatatable = {};

/***
 data  -> array of data, sorted as received
   data.num -> Row number assigned by the server
   data.value -> received data
   data.text -> text formated data
   data.html -> html formated data
   data.trid -> id of the tr for its row
   data.positon -> server position
   data.displayed -> boolean value
   data.ts -> Timestamp when the data was added
 sortedData -> Data sorted
 filteredData -> Data sorted and filtered 
***/
zkDatatable.init = function (ed) {
	var c = ed.getAttribute("columns");
	if (c)
	{
		try {
			ed.columns = JSON.parse(c);
		} catch (error) {
			console.log("Error decoding table descriptor "+c);
			console.log(error);
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
	ed.reorder = ed.getAttribute("reorder") != "false";
	ed.nextRowId = 0;
	ed.maxheight = ed.getAttribute("maxheight");
	ed.selectedPosition = 0;
	ed.selectedTr = null;
	ed.selecteidServerPosition = 0;
	ed.count = 0;
	ed.sortAction = 0;
	ed.data = [];
	ed.sortedData = [];
	ed.filteredData = [];
	ed.filterTs = 0;
	ed.currentPage = 0;
	ed.pageSize = ed.getAttribute("pageSize");
	if (ed.pageSize == null) ed.pageSize = 200;
	if (!ed.pagers)
		ed.pagers=[];
	zkDatatable.refresh(ed);
	
	window.setTimeout(() => {zkDatatable.fixupColumns(ed);}, 100);
	ed.__hidden = true;
	// Detect visibilty
	try {
		var observer = new IntersectionObserver((entries, observer) => {
			entries.forEach(entry => {
				if (entry.intersectionRatio > 0) {
					if (ed.__hidden) {
						zkDatatable.fixupColumns(ed);
						ed.__hidden = false;
					}
				} else {
					ed.__hidden = true;
				}
			});
		}, {root: document.documentElement});
		
		observer.observe(ed);
	} catch (e) {
		// Not available in IE
	}
};

zkDatatable.onVisi=function(ed) {
	zkDatatable.fixupColumns(ed);
}

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
		th.style.position="relative";
		t.appendChild(th);
		var tbd = document.createElement("div")
		tbd.setAttribute("id", t.id+"!tbodydiv");
		tbd.setAttribute("class", "tbodydiv")
		t.appendChild(tbd);
		if (t.maxheight)
			tbd.style.maxHeight = t.maxheight;
		tbd.addEventListener("scroll", (ev) => {
			th.style.left="-"+tbd.scrollLeft+"px";
		});
		var tb = document.createElement("table");
		tb.setAttribute("id", t.id+"!tbody");
		tb.setAttribute("class", "tbody")
		zk.listen(tb, "dragover", zkDatatable.onDragover);
		zk.listen(tb, "drop", zkDatatable.onDrop);
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
		if (ed.columns[column].vertical) {
			var span = document.createElement("span");
			span.setAttribute("style", "writing-mode: vertical-lr");
			td.appendChild(span);
			span.appendChild( document.createTextNode ( ed.columns[column].name ));
		} else {
			td.appendChild( document.createTextNode ( ed.columns[column].name ));
			td.setAttribute("title", ed.columns[column].name);
		}
	}
};

zkDatatable.fixupColumns=function(ed) {
	var tbodydiv = document.getElementById(ed.id+"!tbodydiv");
	var tbody = document.getElementById(ed.id+"!tbody");
	var thead = document.getElementById(ed.id+"!thead");
	var tfoot = document.getElementById(ed.id+"!tfoot");
	for (var tr = tbody.firstElementChild; tr != null; tr = tr.nextElementSibling)
	{
		if ( "none" != tr.style.display && !tr.pager)
			break;
	}
	var trh = thead.firstElementChild; 
	if (tr && trh)
	{
		
		// Shrink
		for (var vv = trh.firstElementChild; vv != null; vv = vv.nextElementSibling)
		{
			vv.style.width = "0px";
			vv.style.maxWidth = "0px";
		}	
		// Stretch
		var size = 0.0;
		var v2 = trh.firstElementChild;
		for (var v = tr.firstElementChild; v != null && v2 != null; v = v.nextElementSibling, v2 = v2.nextElementSibling )
		{
			var w = v.offsetWidth ; // - v2.offsetWidth + v2.clientWidth - 2
									// /*border*/;
			if (v.nextElementSibling == null)
				w += tbodydiv.offsetWidth - tbodydiv.clientWidth; // Add
																	// scroll
																	// bar width
			v2.style.width = "" + w + "px";
			v2.style.maxWidth = "" + w + "px";
			size += w;
		}	
//		if (tfoot)
//			tfoot.style.width = "" + size + "px";
	}
}


zkDatatable.onFirstPageButton = function (event) {
	var ed = event.target.parentElement.parentElement.parentElement;
	if (ed.currentPage != null) {
		ed.currentPage = 0;
		zkDatatable.doFilter(ed);
	}
}

zkDatatable.onLastPageButton = function (event) {
	var ed = event.target.parentElement.parentElement.parentElement;
	if (ed.currentPage != ed.pages - 1) {
		ed.currentPage = ed.pages-1;
		zkDatatable.doFilter(ed);		
	}
}
zkDatatable.onPreviousPageButton = function (event) {
	var ed = event.target.parentElement.parentElement.parentElement;
	if (ed.currentPage > 0) {
		ed.currentPage --;
		zkDatatable.doFilter(ed);
	}
}
zkDatatable.onNextPageButton = function (event) {
	var ed = event.target.parentElement.parentElement.parentElement;
	if (ed.currentPage < ed.pages - 1) {
		ed.currentPage ++;
		zkDatatable.doFilter(ed);
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
		var msg = ed.getAttribute("msgrows")+ ": "+ ed.filteredData.length;
		if (ed.pages > 1 && ed.pageSize > 0) {
			var first = Math.floor(1.5 + ed.currentPage * ed.pageSize);
			var end = Math.floor(0.5+(ed.currentPage + 1) * ed.pageSize);
			if ( end > ed.filteredData.length)
				end = ed.filteredData.length;
			var span = document.createElement("span");
			span.setAttribute("class", "tablepager");
			footer.appendChild(span);

			var t = document.createElement("span");
			t.addEventListener("click", zkDatatable.onFirstPageButton);
			t.innerHTML = "&#x25c3;&#x25c3;";
			span.appendChild(t);
			
			t = document.createElement("span");
			t.addEventListener("click", zkDatatable.onPreviousPageButton);
			t.innerHTML = "&#x25c4;";
			span.appendChild(t);

			span.appendChild(document.createTextNode (" "+first + " - "+end+" "));
			
			t = document.createElement("span");
			t.addEventListener("click", zkDatatable.onNextPageButton);
			t.innerHTML = "&#x25ba;";
			span.appendChild(t);

			t = document.createElement("span");
			t.addEventListener("click", zkDatatable.onLastPageButton);
			t.innerHTML = "&#x25b9;&#x25b9;";
			span.appendChild(t);

		} 
		footer.appendChild( document.createTextNode ( msg ));
		if (ed.getAttribute("msgdownload")) {
			var a = document.createElement("a");
			a.setAttribute("href", "#");
			var img = document.createElement("img");
			img.setAttribute("title", ed.getAttribute("msgdownload"));
			img.setAttribute("class", "imageclic");
			img.setAttribute("src", "/img/download.svg");
			a.appendChild (img);
			a.table = ed;
			footer.appendChild ( a );
			zk.listen(a, "click", zkDatatable.downloadvEvent)
		}
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
	var tr = ev.target.parentNode/* td */.parentNode;
	var t = tr.parentNode/* thead */.parentNode/* table */;
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
	t.filterTs = new Date().getTime();
	zkDatatable.doFilter(t);
}

zkDatatable.previousPage=function(event) {
	var t = event.currentTarget.datatable;
	t.currentPage --;
	zkDatatable.doFilter(t);
	var body = document.getElementById(t.id+"!tbodydiv");
	body.scrollTop = body.scrollHeight;
}

zkDatatable.nextPage=function(event) {
	var t = event.currentTarget.datatable;
	t.currentPage ++;
	zkDatatable.doFilter(t);
	var body = document.getElementById(t.id+"!tbodydiv");
	body.scrollTop = 0;
}

zkDatatable.isVisibleRow=function(t, row) {
	var visible = true;
	if (row.ts && row.ts > t.filterTs) {
		row.displayed = true;
		return true;
	}
	for (var column = 0; column < t.columns.length; column++)
	{
		var filter = t.columns[column].currentFilter;
		var col = t.columns[column];
		if (filter && filter.length > 0) {
			var text = zkDatatable.getRowText(t,row)[column]; 
			for (var f = 0; f < filter.length; f++)
			{
				if (text != null && new String(text).toLowerCase().indexOf(filter[f]) < 0)
				{
					visible = false;
					break;
				}
			}
		}
	}
	row.displayed = visible;
	return visible;
}

zkDatatable.addNextPageButton=function(t) {
	var body = document.getElementById(t.id+"!tbody");
	var tr = document.createElement('tr');
	tr.setAttribute("class", "next-page");
	body.appendChild(tr);
	if (t.multiselect) {
		var td2 = document.createElement("td");
		td2.setAttribute("class", "selector");
		tr.appendChild(td2);
	}
	var td = document.createElement("td");
	td.colSpan = t.columns.length; 
	td.innerText = t.getAttribute("msgnextpage");
	tr.appendChild(td);
	tr.pager = true;
	tr.datatable = t;
	tr.addEventListener("click", zkDatatable.nextPage);
}

zkDatatable.doFilter=function(t) {
	t.count = 0;
	t.selectedPosition = 0;
	t.filteredData = [];
	for (var i = 0; i < t.sortedData.length; i++) {
		var row = t.sortedData[i];
		var visible = zkDatatable.isVisibleRow(t,row);
		if ( visible ) {
			t.filteredData.push(row);
			t.count ++; 
			if (row.position == t.selectedServerPosition)
				t.selectedPosition = t.count;
		} else {
			row.selected = false;
		}
	}

	var first = 0;
	var end = t.filteredData.length;	
	if (t.pageSize > 0 && t.pageSize < t.filteredData.length) {
		t.pages = Math.floor((t.filteredData.length + t.pageSize - 1) / t.pageSize);
		if (t.currentPage == null) t.currentPage = 0;
		if (t.currentPage > t.pages) t.currentPage = t.pages - 1;
		first = t.currentPage * t.pageSize;
		end = (t.currentPage + 1) * t.pageSize;
	} else {
		t.pages = 1;
		t.currentPage = 0;
	}
	var body = document.getElementById(t.id+"!tbody");
	if (body) {
		// Clean document
		while ((v = body.firstChild) != null )
		{
			v.remove();
		}
		if (first > 0) {
			var tr = document.createElement('tr');
			tr.setAttribute("class", "previous-page");
			body.appendChild(tr);
			if (t.multiselect)
			{
				var td2 = document.createElement("td");
				td2.setAttribute("class", "selector");
				tr.appendChild(td2);
			}
			var td = document.createElement("td");
			td.colSpan = t.columns.length; 
			td.innerText = t.getAttribute("msgpreviouspage");
			tr.appendChild(td);
			tr.datatable = t;
			tr.pager = true;
			tr.addEventListener("click", zkDatatable.previousPage);
		}
		if ( end > t.filteredData.length)
			end = t.filteredData.length;
		// Generate rows
		for (var i = first; i < end; i++ )
		{
			zkDatatable.addRowInternal (t, t.filteredData[i] ); 
		}
		if (end < t.filteredData.length) {
			zkDatatable.addNextPageButton(t);
		}
	}
	
	zkDatatable.fixupColumns(t);
	zkDatatable.createFooter(t);
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
		data = JSON.parse(data);
		ed.data = [];
		ed.sortedData = [];
		ed.filteredData = [];
		
		for (var i = 0; i < data.length; i++ )
		{
			ed.sortedData[i] = ed.data[i] = {value: data[i], position: i};
		}
		
		if (!ed.enablefilter && ed.data.length > ed.pageSize)
		{
			ed.enablefilter = true;
			zkDatatable.refresh(ed);
		}

		zkDatatable.prepareSort(ed, true);
	}
}

zkDatatable.addRow=function(ed, pos, value)
{
	pos = parseInt(pos);
	value = JSON.parse(value);
	var data = { value: value, position: pos, ts: new Date().getTime(), displayed: true};
	ed.data.splice(pos, 0, data);
	if (!ed.enablefilter && ed.data.length > ed.pageSize)
	{
		ed.enablefilter = true;
		zkDatatable.refresh(ed);
	}
	if (ed.sortColumn < 0) {
		ed.sortedData.push(data);
		ed.filteredData.push(data);
		if (ed.pageSize == 0 ||
			ed.filteredData.length <= ed.pageSize * (1+ed.currentPage) &&
			ed.filteredData.length >= ed.pageSize * ed.currentPage ) {
			zkDatatable.addRowInternal (ed, ed.filteredData[ed.filteredData.length - 1] ); 	
		}
		else if (ed.filteredData.length == 1+ed.pageSize * (1+ed.currentPage))  {
			zkDatatable.addNextPageButton(ed);
		}
		ed.count++;
	}
	zkDatatable.prepareSort(ed, false);
}

zkDatatable.addRowIncremental=function(ed, pos, value)
{
	pos = parseInt(pos);
	var data = ed.data;
	for ( var i = 0; i < data.length; i++) {
		if (data[i].position >= pos)
			data[i].position ++;
	}
	zkDatatable.addRow(ed,pos,value);
}

zkDatatable.addRows=function(ed, pos, values)
{
	pos = parseInt(pos);
	var values = JSON.parse(values);
	for (var i = 0; i < values.length; i++) {
		var value = values[i];
		var data = { value: value, position: pos++, ts: new Date().getTime(), displayed: true};
		ed.data.push(data);
		if (ed.sortColumn < 0) {
			ed.sortedData.push(data);
			ed.filteredData.push(data);
			ed.count ++;
			if (ed.pageSize == 0 ||
				ed.filteredData.length <= ed.pageSize * (1+ed.currentPage)  &&
				ed.filteredData.length >= ed.pageSize * ed.currentPage ) {
				zkDatatable.addRowInternal (ed, ed.filteredData[ed.filteredData.length - 1] ); 	
			}
			else if (ed.filteredData.length == 1+ed.pageSize * (1+ed.currentPage) ) {
				zkDatatable.addNextPageButton(ed);
			}
		}
	}

	if (!ed.enablefilter && ed.data.length > ed.pageSize)
	{
		ed.enablefilter = true;
		zkDatatable.refresh(ed);
		zkDatatable.doFilter(ed);
	}
	zkDatatable.prepareSort(ed, false);
}

zkDatatable.addRowInternal=function(ed, data)
{
	var t = document.getElementById(ed.id+"!tbody");
	var tr = document.createElement("tr");
	tr.addEventListener("click", zkDatatable.onSelectRow)
	tr.id = ed.id+"!row."+(ed.nextRowId++);
	data.trid = tr.id;
	
	
	try {
		zkDatatable.fillRow(ed, tr, data);
	} catch (e) {
		console.log("Warning: Error rendering row ");
		console.log(data);
		console.log(e);
	}
	t.appendChild(tr);
}


zkDatatable.getRowText=function(ed, data) {
	if (data.text)
		return data.text;

	data.text = [];
	for (var column =  0; column < ed.columns.length; column ++)
	{
		var col = ed.columns[column];
		if (col.template) {
			var v = zkDatatable.replaceExpressions(col.template, data.value);
			const floatingElement = document.createElement("div");
			floatingElement.innerHTML = v;
			const textValue = floatingElement.innerText;
			data.text.push(textValue + zkDatatable.inputValues(floatingElement));		
		}
		else if (col.render)
			data.text.push("");
		else if (col.value)
		{
			var t = "";
			if (!data.value.hasOwnProperty(col.value))
			{
				var value2 = zkDatatable.evaluateInContext (col.value, data.value);
				if (value2 == undefined)
					t= "";
				else
					t=value2;
			}
			else if (data.value[col.value] == null)
				t = "";
			else
				t = data.value[col.value] ;
			data.text.push (t);
		}
		else
			data.text.push(data.value[col.name]);
	}
	return data.text;
}

zkDatatable.fillRow=function(ed, tr, data)
{
	if (ed.reorder) {
		tr.table=ed;
		tr.setAttribute("draggable", "true");
		zk.listen(tr, "dragstart", zkDatatable.onDragStart);
		zk.listen(tr, "dragend", zkDatatable.onDragEnd);
	}
	tr.data = data;
	var value = data.value;
	if (value['$class']) {
		if (data.selected) {
			tr.setAttribute("class", "selected "+value['$class']);
		}
		else {
			tr.setAttribute("class", value['$class']);
		}
	} else {
		if (data.selected) {
			tr.setAttribute("class", "selected");
		}
		else {
			tr.setAttribute("class", "");
		}
	}
	if (ed.multiselect)
	{
		var td = document.createElement("td");
		td.setAttribute("class", "selector");
		tr.appendChild(td);
		var cb = document.createElement("input");
		cb.setAttribute("type", "checkbox");
		cb.checked = data.selected;
		cb.addEventListener("input", zkDatatable.onSelect)
		cb.addEventListener("click", zkDatatable.dontBubble)
		td.appendChild(cb);
		td.addEventListener("click", zkDatatable.onSelectCell)
	}
	var generateData = data.html == null;
	if (generateData) {
		data.text = [];
		data.html = [];
		data.title = [];		
	}
	for (var column =  0; column < ed.columns.length; column ++)
	{
		var td = document.createElement("td");
		tr.appendChild(td);
		var col = ed.columns[column];
		if (generateData || col.render) {
			var title = null;
			if (col.template)
				td.innerHTML = zkDatatable.replaceExpressions(col.template, data.value);
			else if (col.render) {
				window[col.render](td, col, data.value);
			}
			else if (col.value)
			{
				var t = "";
				if (!value.hasOwnProperty(col.value))
				{
					var value2 = zkDatatable.evaluateInContext (col.value, data.value);
					if (value2 == undefined)
						t= "";
					else
						t=value2;
				}
				else if (data.value[col.value] == null)
					t = "";
				else
					t = data.value[col.value] ;
				var v = col.multiline ? t:  zkDatatable.trimColumn(t);
				td.innerText = v;
				if (v != t) {
					title = t;
					td.setAttribute("title", t);					
				}
			}
			else {
				td.innerText = data.value[col.name];
			}
			data.text.push (td.innerText);
			data.html.push (td.innerHTML);
			data.title.push(title);				
		} else {
			if (col.render)
				window[col.render](td, col, data.value);
			else {
				td.innerHTML = data.html[column];
				if (data.title[column] != null)				
					td.setAttribute("title", data.title[column]);					
			}
		}
		if (col.className) {
			td.setAttribute("class", zkDatatable.replaceExpressions(col.className, data.value));
		}
	}
}

zkDatatable.trimColumn=function(v) {
    try {
		var i = v.indexOf("\n");
		if (i >= 0) v = v.substring(0, i)+ " ...";
		return v;
	} catch (error) {
		return v;
	}
}

zkDatatable.dontBubble=function(ev) {
	ev.stopPropagation();
}

zkDatatable.updateRow=function(ed, pos, value)
{
	value = JSON.parse(value);
	var t = document.getElementById(ed.id+"!tbody");
	var data = ed.data[pos];
	if (data && data.value != value) {
		data.value = value;
		data.text = null;
		data.html = null;
		var trid = data.trid;
		if (data.displayed && trid)
		{
			var tr = document.getElementById(trid);
			if (tr)
			{
				var v ;
				while ((v = tr.firstChild) != null )
				{
					v.remove();
				}
				zkDatatable.fillRow(ed, tr, data);
			}
			zkDatatable.fixupColumns(ed);
		}
	}
	
};

zkDatatable.sendClientAction=function(el,event, args)
{
	var target = el;
	while (target.tagName != 'TR')
		target = target.parentNode;
	var t = target.parentNode/* tbody */.parentNode/* bodydiv */.parentNode/* table */;
	var data = target.data;
	if (data.position >= 0)
	{
		zkau.send ({uuid: t.id, cmd: "onSelect", data : [data.position]}, 5);		
		if (args) {
			args.splice(0,0, event);
		} else {
			args = [ event];
		}
		var req = {uuid: t.id, cmd: "onClientAction", data: args};
		zkau.send (req, 5);		
	}	
}

zkDatatable.evaluateInContext = function (js, context) {
	try {
		return function() { with (context) {return eval(js);} }.call(context);		
	} catch (e) {
	//	console.log(e);
		return "";
	}
}

zkDatatable.findExpression=function(v, j) {
	var i1 = v.indexOf("${", j);
	var i2 = v.indexOf("#{", j);
	if (i2 < 0) return i1;
	if (i1 < 0) return i2;
	if (i1 < i2) return i1;
	return i2;
}
zkDatatable.replaceExpressions = function (template,value) {
	var v = "";
	var j = 0;
	do {
		var i = zkDatatable.findExpression(template, j); 
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
    return t == null ? "":
    	t.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

zkDatatable.cleanup = function (ed) {
};

zkDatatable.refreshRows = function(ed) {
	var v;
	var thead = document.getElementById(ed.id+"!thead");
	while ((v = thead.firstChild) != null )
	{
		v.remove();
	}
	var tr = document.createElement("tr");
	thead.appendChild(tr);
	zkDatatable.createHeaders ( ed, tr );
	if ( ed.enablefilter)
	{
		tr = document.createElement("tr");
		thead.appendChild(tr);
		zkDatatable.createFilters( ed, tr);
	}
	
	for (var i = 0; i < ed.data.length; i++) {
		ed.data[i].text = ed.data[i].html = null;		
	}

	var t = document.getElementById(ed.id+"!tbody");
	for (var tr = t.firstElementChild; tr != null; tr = tr.nextElementSibling) 
	{
		while ((v = tr.firstChild) != null )
		{
			v.remove();
		}
		zkDatatable.fillRow(ed, tr, tr.data);
	}
	zkDatatable.fixupColumns(ed);
}

/** Called by the server to set the attribute. */
zkDatatable.setAttr = function (ed, name, value) {
	switch (name) {
	case "rows":
		ed.rows = value;
		return true;
	case "columns":
		try {
			ed.columns = JSON.parse(value);
			zkDatatable.refreshRows(ed);
		} catch (error) {
			console.log("Error decoding table descriptor "+value);
			console.log(error);
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
	case "reorder":
		ed.reorder = (value != "false");
		zkDatatable.refresh(ed);
		zkDatatable.doSort(ed);
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
	var count = t.pageSize * t.currentPage;
	t.selectedPosition = 0;
	var tb = document.getElementById(t.id+"!tbody");
	for (var row = tb.firstElementChild; row != null; row = row.nextElementSibling)
	{
		if ( row.style.display != "none" && !row.pager) {
			count ++; 
			if (row.data.position == t.selectedServerPosition) {
				t.selectedPosition = count;
				break;
			}
		}
	}
	zkDatatable.updatePagers(t);
};

zkDatatable.onSelectRow=function(ev) {
	var target = ev.currentTarget;
	var t = target.parentNode/* tbody */.parentNode/* bodydiv */.parentNode/* table */;
	if ( t.multiselect )
	{
		var cb = target.firstElementChild/* td */.firstElementChild/* input */;
		if (!cb.checked)
		{
			var row = zkDatatable.findSelectedOne(t);
			if (row != null)
			{
				var cb2 = row.firstElementChild/* td */.firstElementChild/* input */;
				cb2.checked = false;
				row.classList.remove("selected");
				row.data.selected = false;
				for (var i = 0; i < t.filteredData.length; i++) {
					t.filteredData[i].selected = false;
				}
			}
			cb.checked = true;
			target.classList.add("selected");
			target.data.selected = true;
			t.selectedServerPosition = target.data.position;
			t.selectedTr = target;
		} else {
			t.selectedTr = zkDatatable.findSelectedTr(t);
			target.data.selected = false;
			cb.checked = false;
			target.classList.remove("selected");			
		}
		zkDatatable.sendSelect(t, cb.checked);
	} else {
		var tbody = document.getElementById(t.id+"!tbody");
		var row = t.selectedTr;
		if (row != null)
		{
			row.classList.remove("selected")
			row.data.selected = false;
		}
		target.classList.add("selected");
		target.data.selected = true;
		t.selectedServerPosition = target.data.position;
		t.selectedTr = target;
		var position = target.data.position;
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
		if (!row.pager) {
			var cb2 = row.firstElementChild/* td */.firstElementChild/* input */;
			if ( cb2.checked )
			{
				if (selected == null) selected = row;
				else return null;
			}
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
		var cb2 = row.firstElementChild/* td */.firstElementChild/* input */;
		if ( cb2.checked )
		{
			return row;
		}
	}
	return null;	
}

zkDatatable.onSelectAll=function(ev) {
	var cb = ev.currentTarget;
	var table = cb.parentNode/* td */.parentNode/* tr */.parentNode/* thead */.parentNode/* table */;
	var tbody = document.getElementById(table.id+"!tbody");
	var row;
	table.selectedTr = null;
	for (var i = 0; i < table.filteredData.length; i++) {
		table.filteredData[i].selected = cb.checked;
	}
	for (row = tbody.firstElementChild; row != null; row = row.nextElementSibling)
	{
		if (table.selectedTr == null)
			table.selectedTr = row;
		var cb2 = row.firstElementChild/* td */.firstElementChild/* input */;
		if (cb2) {
			cb2.checked = cb.checked;
			if (cb.checked)
				row.classList.add("selected");
			else
				row.classList.remove("selected");
		}
	}
	zkDatatable.sendSelect(table, false);
	zkDatatable.findSelectedPosition(table);
}

zkDatatable.onSelectCell=function(ev) {
    var cb = ev.currentTarget.firstElementChild;
    cb.checked = ! cb.checked;
	zkDatatable.selectRow(cb);
	ev.stopPropagation();
}

zkDatatable.onSelect=function(ev) {
	var cb = ev.currentTarget;
	zkDatatable.selectRow(cb);
}

zkDatatable.selectRow=function(cb) {
	var row = cb.parentNode/* td */.parentNode/* tr */;
	var table = row.parentNode/* thead */.parentNode/* tbody */.parentNode/* table */;
	if (!cb.checked)
	{
		var cb2 = table.firstElementChild/* thead */.firstElementChild/* tr */.firstElementChild/* td */.firstElementChild;
		cb2.checked = false;
		row.data.selected = false;
		row.classList.remove("selected");
		table.selectedTr = zkDatatable.findSelectedTr(table);
		table.selectedPosition = 0;
	} else {
		table.selectedTr = row;
		table.selectedServerPosition = row.data.position;
		row.classList.add("selected");
		row.data.selected = true;
		table.selectedPosition = table.pageSize * table.currentPage ;
		for (var row2 = row.parentElement.firstElementChild; row2 != null; row2 = row2.nextElementSibling) {
			if (!row2.pager)
				table.selectedPosition ++;
			if (row2 == row) break;
		}
 	}
	zkDatatable.sendSelect(table, false);
}

/** Selected by the server * */
zkDatatable.setSelected=function(t, pos) {
	var tbody = document.getElementById(t.id+"!tbody");
	for (var i = 0; i < t.filteredData.length; i++) {
		t.filteredData[i].selected = false
	}
	for (var row = tbody.firstElementChild; row != null; row = row.nextElementSibling)
	{
		row.classList.remove("selected")
		if (!row.pager) {
			if ( t.multiselect)
			{
				var cb = row.firstElementChild/* td */.firstElementChild/* input */;
				cb.checked = false;
			}
		}
	}
	t.selectedTr = null;
	if (pos >= 0 && pos < t.data.length) {
		var rowid =  t.data[pos].trid;
		if (rowid)
		{
			var row = document.getElementById(rowid);
			if (row)
			{
				if ( t.multiselect )
				{
					var cb = row.firstElementChild/* td */.firstElementChild/* input */;
					cb.checked = true;
				}
				row.data.selected = true;
				row.classList.add("selected");
				t.selectedTr = row;
			}
			zkDatatable.findSelectedPosition(t);
		} else {
			t.selectedPosition = 0;
			zkDatatable.updatePagers(t);
		}
	} else {
		t.selectedPosition = 0;
		zkDatatable.updatePagers(t);
	}
	zkDatatable.clearMasterCheck(t);
}

/** Cleared by the server * */
zkDatatable.clearSelection=function(t, pos) {
	var tbody = document.getElementById(t.id+"!tbody");
	for (var i = 0; i < t.filteredData.length; i++) {
		t.filteredData[i].selected = false
	}
	for (var row = tbody.firstElementChild; row != null; row = row.nextElementSibling)
	{
		row.classList.remove("selected")
		if ( t.multiselect )
		{
			var cb = row.firstElementChild/* td */.firstElementChild/* input */;
			cb.checked = false;
		}
	}
	t.selectedTr = null;
	t.selectedPosition = 0;
	zkDatatable.updatePagers(t);
	zkDatatable.clearMasterCheck(t);
}

zkDatatable.setSelectedMulti=function(t, pos) {
	var tbody = document.getElementById(t.id+"!tbody");
	if ( tbody == null ) return;
	pos = JSON.parse(pos);
	for (var row = tbody.firstElementChild; row != null; row = row.nextElementSibling)
	{
		row.classList.remove("selected")
		if (row.data)
			row.data.selected = false;
		if ( t.multiselect )
		{
			var cb = row.firstElementChild/* td */.firstElementChild/* input */;
			if (cb && cb.checked)
				cb.checked =false;
		}
	}
	for (var i = 0; i < t.filteredData.length; i++) {
		t.filteredData[i].selected = false
	}
	t.selectedTr = null;
	for (var i = 0; i < pos.length; i++)
	{
		var data = t.data[pos[i]];
		data.selected = true;
		var rowid =  data.trid;
		var row = document.getElementById(rowid);
		if (row)
		{
			if ( t.multiselect )
			{
				var cb = row.firstElementChild/* td */.firstElementChild/* input */;
				cb.checked = true;
			}
			row.classList.add("selected");
			t.selectedTr = row;
		}
		if (i == 0)
			zkDatatable.findSelectedPosition(t);
	} 
	zkDatatable.updatePagers(t);
	zkDatatable.clearMasterCheck(t);
}

zkDatatable.clearMasterCheck=function(t){
	if (t.multiselect) {
		var h = document.getElementById(t.id+"!thead");
		var check = h.firstElementChild.firstElementChild.firstElementChild;
		check.checked=false;
	}
}

zkDatatable.sendSelect=function(table, singleSelect) {
	var selected=[];
	for (var i = 0; i < table.filteredData.length; i++) {
		var data = table.filteredData[i];
		if (data.selected) {
			selected.push(data.position);			
		}
	}
	if (singleSelect && selected.length == 1) {
		var req = {uuid: table.id, cmd: "onSelect", data : [ selected[0] ]};
		zkau.send (req, 5);		
	} else {
		var req = {uuid: table.id, cmd: "onMultiSelect", data : [ JSON.stringify(selected)]};
		zkau.send (req, 5);		
	}
}

zkDatatable.onSort=function(ev) {
	var target = ev.target;
	while (target.tagName != 'TD')
		target = target.parentNode;
	var column = target.column;
	if (column != undefined && column != null)
	{
		var ed = target.parentNode/* tr */.parentNode/* thead */.parentNode/* table */;
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

zkDatatable.prepareSort=function(ed, force) {
	ed.sortAction ++;
	var tbody = document.getElementById(ed.id+"!tbody");
	var rows = ed.data.length;
	if (rows < 50) {
		if (ed.sortColumn >= 0)
			zkDatatable.doSort(ed);
		else if (force)
			zkDatatable.doFilter(ed);
		zkDatatable.createFooter(ed);
		zkDatatable.fixupColumns(ed);
		zkDatatable.findSelectedPosition(ed);
	}
	else {
		var action = ed.sortAction;
		ed.sortPending = true;
		window.setTimeout(() => {
			if (action == ed.sortAction) {
				if (ed.sortColumn >= 0)
					zkDatatable.doSort(ed);
				else if (force)
					zkDatatable.doFilter(ed);
				zkDatatable.createFooter(ed);
				zkDatatable.fixupColumns(ed);
				zkDatatable.findSelectedPosition(ed);				
			}
		}, 100);
	}
}


zkDatatable.doSort=function(ed) {
	var sortColumn = ed.sortColumn;
	ed.sortedData = [...ed.data];
	if (sortColumn >= 0) {
		var col = ed.columns[ed.sortColumn];
		if (col) {
			var value = ed.columns[ed.sortColumn].value;
			var direction = ed.sortDirection;
			var tbody = document.getElementById(ed.id+"!tbody");
			zkDatatable.quickSort(ed.sortedData,
					(a,b) => {
						var r;
						if (value) {
							var v1,v2;
							if (a.value.hasOwnProperty(value)) 
								v1 = a.value[value];
							else {
								with (a.value) { 
									try {
										v1 = zkDatatable.evaluateInContext (value, a.value)
									} catch(e) {
										v1=""
									}
								}
							}
							if (b.value.hasOwnProperty(value)) 
								v2 = b.value[value];
							else {
								with (b.value) { 
									try {
										v2 = zkDatatable.evaluateInContext (value, b.value)
									} catch(e) {
										v2=""
									}
								}
							}
							if (isNaN(v1))
								v1 = new String(v1).toLowerCase();
							if (isNaN(v2))
								v2 = new String(v2).toLowerCase();
							if (v1 == null) v1 = "";
							if (v2 == null) v2 = "";
							if (v1 < v2) r = -1;
							else if (v1 > v2) r = +1;
							else r = 0; 
						} else {
							var v1 = zkDatatable.getRowText(ed,a)[sortColumn]; 
							var v2 = zkDatatable.getRowText(ed,b)[sortColumn]; 
							if (v1 < v2) r = -1;
							else if (v1 > v2) r = +1;
							else r = 0; 
						}
						return r*direction;
					}
				);
			
		}
	}
	zkDatatable.doFilter(ed);
}

zkDatatable.deleteRow=function(ed, pos)
{
	if (ed.data[pos])
	{
		var data = ed.data[pos];
		var id = data.trid;
		var row = document.getElementById(id);
		if (row) {
			var paging = ed.filteredData.length > ed.pageSize;
			if (row == ed.selectedTr)
				ed.selectedTr = null;
			row.remove();
			ed.data.splice(pos,1);
			var pos2 = ed.filteredData.indexOf(data);
			if (pos2 >= 0)
				ed.filteredData.splice( pos2, 1 );
			pos2 = ed.sortedData.indexOf(data);
			if (pos2 >= 0)
				ed.sortedData.splice( pos2, 1 );
			for (var i = 0; i < ed.data.length; i++) {
				if (ed.data[i].position > data.position)
					ed.data[i].position --;
			}
			if (paging) {
				zkDatatable.doFilter(ed);
			} else {
				zkDatatable.createFooter(ed);
				zkDatatable.fixupColumns(ed);
				zkDatatable.findSelectedPosition(ed);
			}			
			ed.count --;
		}
	}
}

zkDatatable.deleteRowIncremental=function(ed, pos) {
	zkDatatable.deleteRow(ed, pos);
	pos = parseInt(pos);
	var data = ed.data;
	for ( var i = 0; i < data.length; i++) {
		if (data[i] && data[i].position > pos)
			data[i].position --;
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
				row.data.selected = false;
				row.classList.remove("selected");
				if (t.multiselect) {
					var cb = row.firstElementChild/* td */.firstElementChild/* input */;
					cb.checked = false;
				}
				next.classList.add("selected");
				next.data.selected = true;
			}
			if (t.multiselect) {
				var cb = next.firstElementChild/* td */.firstElementChild/* input */;
				cb.checked = true;
				zkDatatable.sendSelect(t, true);
			} else {
				var position = next.data.position;
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
				row.data.selected = false;
				if (t.multiselect) {
					var cb = row.firstElementChild/* td */.firstElementChild/* input */;
					cb.checked = false;
				}
				next.classList.add("selected");
				next.data.selected = true;
			}
			if (t.multiselect) {
				var cb = next.firstElementChild/* td */.firstElementChild/* input */;
				cb.checked = true;
				zkDatatable.sendSelect(t, true);
			} else {
				var position = next.data.position;
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

zkDatatable.downloadCsvEvent=function(event) {
	var ed = event.currentTarget.table;
	zkDatatable.downloadCsv(ed);
}
zkDatatable.downloadCsv=function(ed) {
	var s = "";
	for (var column =  0; column < ed.columns.length; column ++)
	{
		if (column > 0) s+= ",";
		s += zkDatatable.quote(ed.columns[column].name);
	}
	s += "\n";
	var tbody = document.getElementById(ed.id+"!tbody");
	for (var i = 0; i < ed.filteredData.length; i++) {
		var row = ed.filteredData[i];
		var text = zkDatatable.getRowText(ed, row);
		for ( var j = 0; j < text.length; j++) {
			if (j > 0) s += ",";
			var t = text[j];
			s += zkDatatable.quote(t);
		}
		s+="\n";
	}
	
    var data = new Blob([s], {type: 'text/csv', name: 'export.csv'});
    data.name = 'soffid.csv';
    
    // If we are replacing a previously generated file we need to
    // manually revoke the object URL to avoid memory leaks.
    if (zkDatatable.file !== null) {
      window.URL.revokeObjectURL(zkDatatable.file);
    }

    zkDatatable.file = window.URL.createObjectURL(data);
    window.open( zkDatatable.file );
}

zkDatatable.quote = function(t) {
	return "\""+new String(t).replace(/"/g, "\\\"")+"\"";
}

zkDatatable.inputValues = function(td) {
	var s = "";
	for ( var input = td.firstElementChild; input != null; input = input.nextElementSibling) {
		if (input.tagName == "INPUT") {
			if (input.type = "checkbox" && input.checked) {
				s += "true";
			} else if (input.value) {
				s +=  input.value;
			}
		} else {
			s += zkDatatable.inputValues ( input );
		}
	}
	return s;
}

/************ DRAG & DROP SUPPORT */
zkDatatable.onDragover=function(event) {
	var row = zkDatatable.dragging;
	var table = row.table;
	var currentTable = event.currentTarget/*table*/.parentElement/*div.tbody*/.parentElement/*div.datatable*/;
	if (table == currentTable) {
		var tr = document.elementFromPoint(event.x, event.y);
		while (tr != null && tr.tagName != 'TR') {
			tr = tr.parentElement;
		}
		if (tr != null && tr.table == table) {
			if  (tr != row) {
				var cr = tr.getClientRects()[0];
				var middle = cr.top + cr.height / 2;
				if ( event.clientY > middle )
					zkDatatable.draggedBefore = tr.nextElementSibling;
				else
					zkDatatable.draggedBefore = tr;
				tr.parentElement.insertBefore(row, zkDatatable.draggedBefore);
			}
		}
		event.preventDefault();
	}
}

zkDatatable.onDragStart=function(event) {
	zkDatatable.dragging = event.currentTarget;
	zkDatatable.draggedBefore = event.currentTarget;
	event.dataTransfer.setData("text/plain", JSON.stringify(event.currentTarget.data));
	zkDatatable.dragging.classList.add("selected");
}

zkDatatable.onDragEnd = function(event) {
	var src = zkDatatable.dragging;
	var target = zkDatatable.draggedBefore;
	var t = src.table;
	if (t.multiselect) {
		if (src.firstElementChild/*td*/.firstElementChild/*input*/.checked) {
			zkDatatable.dragging.data.selected = true;					
			zkDatatable.dragging.classList.add("selected");
		}
		else {
			zkDatatable.dragging.data.selected = false;		
			zkDatatable.dragging.classList.remove("selected");
		}
	}
	else {
		zkDatatable.dragging.classList.remove("selected");
		zkDatatable.dragging.data.selected = false;		
	}
	if (target != src) {
		var targetPosition = target == null ?  
			-1:
			target.data.position;
		var srcPosition = src.data.position;
		if (srcPosition >= 0)
		{
			zkau.send ({uuid: t.id, cmd: "onReorder", data : [srcPosition, targetPosition]}, 5);		
		}			
	}
}

zkDatatable.onDrop = function(event) {
	event.preventDefault();
}