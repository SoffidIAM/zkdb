/* Soffid tree
 * Datatree2
 *  - !thead
 *  - !tbody
 *    - tree-itemholder -> value  id = !row
 *      - tree-collapse
 *      - tree-item
 *        - tree-label -> value
 *        - tree-children id = !children
 *          - tree-itemholder -> value
 *            - tree-label -> value
 *          - tree-itemholder -> value
 *            - tree-label -> value
 *          - tree-tail id=!tail
 *    - tree-itemholder -> value
 *      - tree-collapse
 *      - tree-item
 *        - tree-label -> value
 *          - tree-cell
 *          - tree-cell
 *          - tree-cell
 *        - tree-children
 */

zk.load("zul.zul");

zkDatatree2 = {};

zkDatatree2.init = function (ed) {
	ed.sortable = "true" == ed.getAttribute("sortable");
	ed.sortDirection = ed.getAttribute("sortDirection") == null ? 0: parseInt(ed.getAttribute("sortDirection"));
	ed.sortColumn = 0;
	ed.enablefilter = "false" != ed.getAttribute("enablefilter");
	ed.footer = ed.getAttribute("footer") != "false";
	ed.index = [];
	ed.nextRowId = 0;
	ed.maxheight = ed.getAttribute("maxheight");
	ed.selectedPosition = 0;
	ed.selectedRow = null;
	ed.selectedLabel = null;
	if (ed.getAttribute("columns") != null && ed.getAttribute("columns").length > 0)
		ed.columns = JSON.parse(ed.getAttribute("columns"));
	ed.count = 0;
	ed.pageSize = 50;
	ed.nextPageMsg = ed.getAttribute("msgnextpage");
	ed.previousPageMsg = ed.getAttribute("msgpreviouspage");
	ed.reorder = ed.getAttribute("reorder") != "false";
		ed.refreshConter = 0;
	if (!ed.pagers)
		ed.pagers=[];
	zkDatatree2.refresh(ed);
	
	new ResizeSensor(ed, function ()  { zkDatatree2.fixupColumns(ed); });
	try {
		var observer = new IntersectionObserver((entries, observer) => {
			entries.forEach(entry => {
				if (entry.isIntersecting) {
					if (ed.__hidden) {
						ed.__hidden = false;
						zkDatatree2.fixupColumns(ed);
					}
				} else if (!ed.__hidden) {
					ed.__hidden = true;
				}
			});
		}, {root: document.documentElement});
		
		ed.__hidden = true;
		observer.observe(ed);
	} catch (e) {
		// Not available in IE
	}
	if (ed.tmp_data) {zkDatatree2.setData(ed, ed.tmp_data); ed.tmp_data = null;}
};

zkDatatree2.registerPager=function(ed, pager) {
	if (! ed.pagers) 
		ed.pagers = [];
	
	ed.pagers.push(pager);
	zkDatatree2.updatePagers(ed);
}

zkDatatree2.deregisterPager=function(ed, pager) {
	if (ed && ed.pagers) {
		var i = ed.pagers.indexOf(pager);
		if (i >= 0)
			ed.pagers.splice(i, 1);
	}
}

zkDatatree2.updatePagers=function(ed) {
	for (var i = 0; i < ed.pagers.length; i++)
		zkPager.update(ed.pagers[i], ed.selectedPosition, ed.count);
}

zkDatatree2.onSize=function(ed) {
	zkDatatree2.fixupColumns(ed);
}

zkDatatree2.onVisi=function(ed) {
	zkDatatree2.fixupColumns(ed);
}

zkDatatree2.fixupColumns=function(ed) {
	var thead = document.getElementById(ed.id+"!thead");
	var tfilter = document.getElementById(ed.id+"!tfilter");
	if (ed.columns) {
		var w = ed.getBoundingClientRect().width ; // Total width
		var aw = 0; // Reserved width
		var uw = 0; // Components with unsetted width
		
		// Initial setted size
		for (var i = 0; i < ed.columns.length; i++) {
			var col = ed.columns[i];
			var cw = col.width;
			if (cw != null && cw.endsWith("px")) {
				var s = new Number(cw.substring(0, cw.length-2));
				col.actualWidth = s;
				aw += s;
			} else if (cw != null && cw.endsWith("%")) {
				var s = w * new Number(cw.substring(0,cw.length-1)) / 100.0;
				col.actualWidth = s;
				aw += s;
			} else {
				col.actualWidth = null;
				uw ++;
			}
		}
		// Assign rest of components
		ed.columnWidth = (w - aw) / (uw+1);
		var head = thead.firstElementChild;
		head.style.width = ""+ed.columnWidth+"px";
		var filter = null;
		if (tfilter != null) {
			filter = tfilter.firstElementChild;
			if (filter != null)
				filter.style.width = ""+ed.columnWidth+"px";
		}
		for (var i = 0; i < ed.columns.length; i++) {
			head = head.nextElementSibling;
			var col = ed.columns[i];
			if (col.actualWidth != null) {
				head.style.width = ""+col.actualWidth+"px";
			} else {
				head.style.width = ""+ed.columnWidth+"px";
			}
			if (tfilter != null && filter != null) {
				filter = filter.nextElementSibling;
				if (filter != null)
					filter.style.width = head.style.width;
			}
		}
		var tbody = document.getElementById(ed.id+"!tbody");
		var label;
		var labels = [];
		var iterator = document.evaluate(".//div[@style='' and @class='tree-itemholder']/div[@class='tree-item']/div[starts-with(@class,'tree-label')]", tbody, 
				null, XPathResult.UNORDERED_NODE_ITERATOR_TYPE, null);
		while ((label = iterator.iterateNext()) != null) {
			labels.push(label);
		}
		iterator = document.evaluate(".//div[@style='' and @class='tree-itemholder']/div[starts-with(@class,'tree-label')]", tbody, 
				null, XPathResult.UNORDERED_NODE_ITERATOR_TYPE, null);
		while ((label = iterator.iterateNext()) != null) {
			labels.push(label);
		}
		for (var i = 0; i < labels.length; i++)
			zkDatatree2.fixupLabelColumns(ed, labels[i]);
	} else {
		var head = thead.firstElementChild;
		head.style.width = "100%";
		var filter = null;
		if (tfilter != null) {
			filter = tfilter.firstElementChild;
			if (filter != null)
				filter.style.width = "100%";
		}
	}
}

zkDatatree2.fixupLabelColumns=function(tree, label) {
	var div0 = label.firstElementChild;
	
	var total = label.getBoundingClientRect().width;
	var div = div0;
	if (div != null && div.tagName == "DIV") { // Labels with columns only
		div0.style.width = "0px"; 		
		for (var i = 0; i < tree.columns.length; i++) {
			if (div != null) div = div.nextElementSibling;
			var col = tree.columns[i];
			if (col.actualWidth != null) {
				total -= col.actualWidth;
				if (div != null)
					div.style.width = ""+col.actualWidth+"px";
			} else {
				total -= tree.columnWidth;
				if (div != null)
					div.style.width = ""+tree.columnWidth+"px";
			}
		}
		div0.style.width = "" + total + "px"; 		
	}
}

zkDatatree2.refresh = function (t) {
	var v ;
	while ((v = t.firstChild) != null )
	{
		v.remove();
	}
	try {
		var th = document.createElement("div");
		th.setAttribute("id", t.id+"!thead");
		th.setAttribute("class", "thead")
		t.appendChild(th);
		if ( t.enablefilter)
		{
			tr = document.createElement("div");
			tr.setAttribute("class", "thead");
			tr.setAttribute("id", t.id+"!tfilter");
			t.appendChild(tr);
			zkDatatree2.createFilters(t, tr);
		}
		var tbd = document.createElement("div")
		tbd.setAttribute("id", t.id+"!tbody");
		tbd.setAttribute("class", "tbody")
		tbd.currentPage = 0;
		tbd.soffidVisibleChildren = 0;
		t.appendChild(tbd);
		zkDatatree2.addPagination(t, tbd);
		if (t.maxheight)
			tbd.style.maxHeight = t.maxheight;
		zk.listen(tbd, "dragover", zkDatatree2.onDragover);
		zk.listen(tbd, "drop", zkDatatree2.onDrop);

		var tf = document.createElement("div");
		tf.setAttribute("class", "tfoot")
		tf.setAttribute("id", t.id+"!tfoot");
		t.appendChild(tf);
		zkDatatree2.createHeaders ( t, th );
		zkDatatree2.fixupColumns ( t );
		zkDatatree2.createFooter(t);
	} catch (e) {
		console.log(e)
	}
};

zkDatatree2.addPagination=function(t, parent) {
	var up = document.createElement("div");
	up.setAttribute("class", "tree-itemholder page-up");
	up.setAttribute("id" , t.id+"!row-up");
	up.setAttribute("style", "display: none");
	up.pager = true;
	up.pagerUp = true;
	up.innerText = t.previousPageMsg;
	up.addEventListener("click", zkDatatree2.onPageUp);
	parent.appendChild(up);

	var down = document.createElement("div");
	down.setAttribute("class", "tree-itemholder page-down");
	down.setAttribute("id" , t.id+"!row-down");
	down.setAttribute("style", "display: none");
	down.pagerDown = true;
	down.pager = true;
	down.innerText = t.nextPageMsg;
	down.addEventListener("click", zkDatatree2.onPageDown);
	parent.appendChild(down);
};

zkDatatree2.createHeaders = function (ed, th) {
	var v ;
	while ((v = th.firstChild) != null )
	{
		v.remove();
	}
	var tr = document.createElement("div");
	tr.setAttribute("class", "column")
	th.appendChild(tr);
	if ( ed.sortable)
	{
		zkDatatree2.createSortArrows(ed, tr, -1);
		tr.setAttribute("class", "column sortable-column")
		if (!tr.registeredEvent) {
			tr.addEventListener("click", zkDatatree2.onSort)
			tr.registeredEvent = true;
		}
	}
	tr.sortColumn = -1;
	tr.appendChild( document.createTextNode ( ed.getAttribute("header") ));
	if (ed.columns) {
		for (var i = 0; i < ed.columns.length; i++) {
			tr = document.createElement("div");
			tr.setAttribute("class", "column")
			th.appendChild(tr);
			if ( ed.sortable )
			{
				zkDatatree2.createSortArrows(ed, tr, i);
				tr.setAttribute("class", "column sortable-column")
				if (!tr.registeredEvent) {
					tr.addEventListener("click", zkDatatree2.onSort)
					tr.registeredEvent = true;
				}
			}
			tr.sortColumn = i;
			tr.appendChild( document.createTextNode ( ed.columns[i].name ));
		}
	}
};

zkDatatree2.createFooter = function (ed) {
	var footer = document.getElementById(ed.id+"!tfoot");
	var v ;
	while ((v = footer.firstChild) != null )
	{
		v.remove();
	}
	if (  ed.footer)
	{
		var body = document.getElementById(ed.id+"!tbody");
		if (body != null && body.soffidVisibleChildren > ed.pageSize > 0) {
			var first = Math.floor( 1.5 + body.currentPage * ed.pageSize );
			var end = Math.floor(0.5 + (body.currentPage + 1) * ed.pageSize);
			if ( end > ed.soffidVisibleChildren)
				end = ed.soffidVisibleChildren;
			var span = document.createElement("span");
			span.setAttribute("class", "tablepager");
			footer.appendChild(span);

			var t = document.createElement("span");
			t.addEventListener("click", zkDatatree2.onFirstPageButton);
			t.innerHTML = "&#x25c3;&#x25c3;";
			span.appendChild(t);
			
			t = document.createElement("span");
			t.addEventListener("click", zkDatatree2.onPreviousPageButton);
			t.innerHTML = "&#x25c4;";
			span.appendChild(t);

			span.appendChild(document.createTextNode (" "+first + " - "+end+" "));
			
			t = document.createElement("span");
			t.addEventListener("click", zkDatatree2.onNextPageButton);
			t.innerHTML = "&#x25ba;";
			span.appendChild(t);

			t = document.createElement("span");
			t.addEventListener("click", zkDatatree2.onLastPageButton);
			t.innerHTML = "&#x25b9;&#x25b9;";
			span.appendChild(t);

		} 
		footer.appendChild( document.createTextNode ( "Total rows: "+ ed.count ));
	}
};

zkDatatree2.onFirstPageButton = function (event) {
	var ed = event.target.parentElement.parentElement.parentElement;
	var body = document.getElementById(ed.id+"!tbody");

	body.currentPage = 0;
	zkDatatree2.applyPaging(ed, body);
}

zkDatatree2.onLastPageButton = function (event) {
	var ed = event.target.parentElement.parentElement.parentElement;
	var body = document.getElementById(ed.id+"!tbody");
	var pages = 1 + (body.soffidVisibleChildren - 1) / ed.pageSize;		
	if (body.currentPage != pages ) {
		body.currentPage = pages - 1;
		zkDatatree2.applyPaging(ed, body);
	}
}
zkDatatree2.onPreviousPageButton = function (event) {
	var ed = event.target.parentElement.parentElement.parentElement;
	var body = document.getElementById(ed.id+"!tbody");
	if (body.currentPage > 0) {
		body.currentPage --;
		zkDatatree2.applyPaging(ed, body);
	}
}
zkDatatree2.onNextPageButton = function (event) {
	var ed = event.target.parentElement.parentElement.parentElement;
	var body = document.getElementById(ed.id+"!tbody");
	var pages = 1 + (body.soffidVisibleChildren - 1) / ed.pageSize;		
	if (body.currentPage < pages - 1) {
		body.currentPage ++;
		zkDatatree2.applyPaging(ed, body);
	}
}

zkDatatree2.createFilters = function (ed, th) {
	var v ;
	while ((v = th.firstChild) != null )
	{
		v.remove();
	}

	var tr = document.createElement("div");
	tr.setAttribute("class", "filter-cell")
	th.appendChild(tr);
	var tb = document.createElement("input");
	tb.setAttribute("type", "text");
	tb.setAttribute("class", "filter");
	tb.setAttribute("placeholder", "Filter");
	tb.addEventListener("input", zkDatatree2.onFilter)
	tr.appendChild(tb);
	if (ed.columns) {
		for (var i = 0; i < ed.columns.length; i++) {
			var tr = document.createElement("div");
			tr.setAttribute("class", "filter-cell")
			th.appendChild(tr);
			if ( ed.columns[i].filter == null || ed.columns[i].filter) {
				var tb = document.createElement("input");
				tb.setAttribute("type", "text");
				tb.setAttribute("class", "filter");
				tb.setAttribute("placeholder", "Filter");
				tb.addEventListener("input", zkDatatree2.onFilter)
				tr.appendChild(tb);
			}
		}
	}
};

zkDatatree2.onFilter=function(ev) {
	var input = ev.currentTarget;
	var t = input.parentNode/* filter-cell */.parentNode/* thead */.parentNode;
	var thead = input.parentNode.parentNode;
	var filter = [];
	for (var cell = thead.firstElementChild; cell != null; cell = cell.nextElementSibling) {
		var input2 = cell.firstElementChild;
		if (input2 && input2.value.trim().length > 0)
			filter.push (input2.value.toLowerCase().split(" "));
		else
			filter.push ( []);
	}
	t.filter = filter;
	zkDatatree2.doFilter(t);
	var tb = document.getElementById(t.id+"!tbody");
	zkDatatree2.expandFirst (t, tb );
}

zkDatatree2.expandFirst=function(t, div) {
	for (var row = div.firstElementChild.nextElementSibling; row != div.lastElementChild; row = row.nextElementSibling)
	{
		if (!row.hidden && !row.pager && !row.isTail) {
			var treeitem = row.lastElementChild;
			if (treeitem !=null && treeitem.classList.contains("tree-item")) {
				var children = treeitem.lastElementChild;
				zkDatatree2.expandFirst (t, children);
				if (row.collapsed && row != t.firstMatch) {
					var indicator = row.firstElementChild;
					indicator.classList.add("open");
					children.style.display = "";
					t.count += row.count;
				}
			}
			break;
		}
	}
}

/**
 * Returns an object with two values visible: if the branch or any child matches
 * the filter count: number of visible items; firstMatch: first div to motch
 */
zkDatatree2.doFilterLoop=function(t, div, filter, parentMatch) {
	var r = {count: 0, visible: false, firstMatch: null};
	var count = 0;
	var nextPage = div.lastElementChild;
	if (nextPage.isTail)
		nextPage = nextPage.previousElementSibling;
	nextPage.style.display = "none";
	div.firstElementChild.style.display = "none";
	div.currentPage = 0;
	for (var row = div.firstElementChild.nextElementSibling; row != div.lastElementChild; row = row.nextElementSibling)
	{
		var visible = true;
		if ( !row.isTail && !row.pager) {
			var label = row.lastElementChild;
			var children = null;
			if (label.classList.contains("tree-item")) {
				children = label.lastElementChild;
				label = label.firstElementChild;
			}
			if (filter && !parentMatch) {
				if (! label.classList.contains("no-columns")) {
					var div2 = label.firstElementChild;
					for (var i = 0; i < filter.length; i++) {
						if (filter[i].length > 0) {
							if (div2 == null) {
								visible = false;
								break;
							} else if ( !zkDatatree2.checkFilter(div2, filter[i])) {
								visible = false;
								break;
							}
						}
						if (div2 != null) div2 = div2.nextElementSibling;
					}
				} else {
					visible = zkDatatree2.checkFilter(label, filter[0]);
					for (var i = 1; i < filter.length; i++) {
						if (filter[i].length > 0) {
							visible = false;
							break;
						}
					}
				}
			}
			if (visible && r.firstMatch == null)
				r.firstMatch = row;
			// Now, check children
			if (children != null) {
				var r2 = zkDatatree2.doFilterLoop (t, children, filter, visible);
				if (r2.visible) visible = true;
				if (r.firstMatch == null) r.firstMatch = r2.firstMatch;
				if (!row.collapsed)
					r.count += r2.count;
				row.count = r2.count;
			} else {
				row.count = 0;
			}
			if ( visible ) {
				count ++;
				if (count == t.pageSize+1) {
					nextPage.style.display = "";
				}
				r.visible = true;
				r.count ++;
				row.hidden = false;
				if (count > t.pageSize) {
					var m = count;
					if (m > 2 * t.pageSize)
						m = 2 * t.pageSize;
					nextPage.innerText = t.nextPageMsg+" ( " + (t.pageSize + 1) + " - " + (m) + " / " + count + " )";				
					row.style.display="none";
				} else {
					row.style.display="";
				}
				anyVisible = true;
			} else {
				row.hidden = true;
				row.style.display="none";
			}
		}
	}
	div.soffidVisibleChildren = count;
	return r;
}

zkDatatree2.checkFilter=function(label, filter) {
	for (var f = 0; f < filter.length; f++)
		if (label.textContent.toLowerCase().indexOf(filter[f]) < 0)
			return false;
	return true;
}

zkDatatree2.doFilter=function(t) {
	var tb = document.getElementById(t.id+"!tbody");
	var r = zkDatatree2.doFilterLoop (t, tb, t.filter, false);
	t.count = r.count;
	t.firstMatch = r.firstMatch;
	zkDatatree2.createFooter(t);
	zkDatatree2.updatePagers(t);
	zkDatatree2.fixupColumns(t);
}

zkDatatree2.onPageUp=function(ev) {
	var button = ev.currentTarget;
	var children = button.parentElement;
	var t = children.parentElement;
	while ( t.getAttribute("z.type") != "zul.datatree2.Datatree2")
		t = t.parentElement;
	
	if  (children.currentPage > 0) {
		children.currentPage --;
		zkDatatree2.applyPaging(t, children);
	}
}
zkDatatree2.onPageDown=function(ev) {
	var button = ev.currentTarget;
	var children = button.parentElement;
	var t = children.parentElement;
	while ( t.getAttribute("z.type") != "zul.datatree2.Datatree2")
		t = t.parentElement;
	
	var pages = children.soffidVisibleChildren / t.pageSize;		
	
	if  (children.currentPage < pages - 1) {
		children.currentPage ++;
		zkDatatree2.applyPaging(t, children);
	}
}
zkDatatree2.applyPaging=function(t, children) {
	var pages = children.soffidVisibleChildren / t.pageSize;		
	
	var first = children.currentPage * t.pageSize;
	var last = first + t.pageSize;
	var count = 0;
	for (var child = children.firstElementChild; child != null; child = child.nextElementSibling) {
		if (child.pager) {
			if (child == children.firstElementChild ) {
				child.style.display = children.currentPage > 0 ? "": "none";
				child.innerText = t.previousPageMsg+" ( " + (first - t.pageSize + 1) + " - " + first + " )";				
			}
			else {
				child.style.display = children.currentPage < pages - 1 ? "": "none";
				var m = count;
				if (m > last + t.pageSize + 1)
					m = last + t.pageSize + 1;
				child.innerText = t.nextPageMsg+" ( " + (last + 1) + " - " + (m) + " / " + count + " )";				
			}
		}
		else if (! child.isTail) {
			if (! child.hidden) {
				if (count >= first && count < last)
					child.style.display = "";
				else
					child.style.display = "none";
				count ++;
			}
		}
	}
	zkDatatree2.fixupColumns(t);
	zkDatatree2.createFooter(t);
}

zkDatatree2.createSortArrows=function(ed, td, column)
{
	var div = document.createElement("div");
	div.setAttribute("class", "sort-indicator");
	td.appendChild(div);
	var direction = ed.sortColumn == column ? ed.sortDirection: 0;
	if (direction != 0)
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

zkDatatree2.setData = function(ed, data) {
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
		zkDatatree2.addPagination(ed, t);
		// Generate rows
		ed.index = [] ;
		data = JSON.parse(data);
		if (data.children) {
			for ( var i = 0; i < data.children.length; i++) {
				zkDatatree2.addBranchInternal (ed, data.children[i]);
			}	
		}
		zkDatatree2.createFooter(ed);
		if (ed.sortDirection != 0)
			zkDatatree2.doSortRecursive(ed, t);
		zkDatatree2.doFilter(ed);
	} else {
		ed.tmp_data = data;
	}
}


zkDatatree2.addBranch=function(t, value)
{
	var value = JSON.parse(value);
	var row = zkDatatree2.addBranchInternal (t, value);
	if (t.sortDirection != 0 && row != null) {
		var lastSort = row.parentElement.sortTs;
		if (lastSort == null) lastSort = 1;
		else lastSort ++;
		row.parentElement.sortTs = lastSort;
		window.setTimeout(() => {
			if (row.parentElement.sortTs == lastSort) {
				zkDatatree2.doSortRecursive(t, row.parentElement);
				zkDatatree2.doFilter(t);
				zkDatatree2.createFooter(t);
			}
		}, 100);
	}
}

zkDatatree2.addBranchInternal=function(ed, value)
{
	var pos = value.position;
	var parentId = "";
	for (var i = 0; i < pos.length-1; i++)
	{
		parentId += "."+pos[i];
	}
	var newId = "";
	for (var i = 0; i < pos.length; i++)
	{
		newId += "."+pos[i];
	}
	var parentDiv;
	if (pos.length > 1) {
		parentDiv = document.getElementById(ed.id+"!children"+parentId);
	} else {
		parentDiv = document.getElementById(ed.id+"!tbody");
	}
	if (parentDiv == null) return;
	
	var div = document.createElement("div");
	div.setAttribute("class", "tree-itemholder");
	div.setAttribute("id" , ed.id+"!row"+newId);
	div.soffid_page = 0;
	div.setAttribute("style", "");
	if (parentDiv.lastElementChild != null &&
		parentDiv.soffidVisibleChildren == ed.pageSize) 
	{
		var nextPage = parentDiv.lastElementChild;
		if (nextPage.isTail) nextPage = nextPage.previousElementSibling;
		nextPage.style.display = "";
	}
	parentDiv.soffidVisibleChildren ++;
	if (parentDiv.soffidVisibleChildren > ed.pageSize)
	{
		div.setAttribute("style", "display: none");
		var nextPage = parentDiv.lastElementChild;
		if (nextPage.isTail) nextPage = nextPage.previousElementSibling;
		var first = parentDiv.currentPage * ed.pageSize;
		var last = first + ed.pageSize;
		var m = parentDiv.soffidVisibleChildren ;
		if (m > last + ed.pageSize + 1)
			m = last + ed.pageSize + 1;
		nextPage.innerText = ed.nextPageMsg+" ( " + (last + 1) + " - " + (m) + " / " + parentDiv.soffidVisibleChildren + " )";				
	}
	div.value = value;
	div.collapsed = true;
	if ( parentDiv.lastElementChild != null && parentDiv.lastElementChild.isTail)
		parentDiv.insertBefore(div, parentDiv.lastElementChild.previousElementSibling);
	else
		parentDiv.insertBefore(div, parentDiv.lastElementChild);
		
	if ( ! value.leaf )
	{
		var fold = document.createElement("div");
		fold.setAttribute("class", "tree-collapse");
		var img = document.createElement("img");
		img.setAttribute("src", ed.getAttribute("foldBar"));
		fold.appendChild(img);
		zk.listen(fold, "click", zkDatatree2.onFoldUnfold)
		div.appendChild(fold);
		
		var container = document.createElement("div");
		container.setAttribute("class", "tree-item");
		div.appendChild (container);

		zkDatatree2.renderRow(ed, container, value);
		
		var children = document.createElement("div");
		children.setAttribute("class", "tree-children");
		container.appendChild(children);
		children.setAttribute("id", ed.id+"!children"+newId);
		zkDatatree2.addPagination(ed, children);
		children.currentPage = children.soffidVisibleChildren = 0;
		if (value.children)
		{
			if ( value.collapsed ) {
				div.collapsed = true;
				children.style.display = "none";
			}
			else {
				div.collapsed = false;
				fold.classList.add("open");
			}
			for (var i = 0; i < value.children.length; i++) {
				zkDatatree2.addBranchInternal(ed, value.children[i]);
			}
		} else {
			div.collapsed = true;
			children.style.display = "none";
		}
		if (value.tail) {
			var tail = document.createElement("div");
			tail.setAttribute("class", "tree-tail");
			tail.innerHTML = value.tail;
			tail.isTail = true;
			children.appendChild(tail);
		}
	} else {
		zkDatatree2.renderRow(ed, div, value);
	}
	return div;
}


zkDatatree2.renderRow=function(tree, container, value) {
	var label = document.createElement("div");
	label.setAttribute("class", "tree-label");
	label.value = value;
	if (tree.reorder)
		label.setAttribute("draggable", true);
	label.tree = tree;
	zk.listen(label, "dragstart", zkDatatree2.onDragStart);
	zk.listen(label, "dragend", zkDatatree2.onDragEnd);
	container.insertBefore(label, container.firstChild);
	
	var style = "tree-label";
	if (value['$class']) { 
		style = "tree-label "+value['$class'];
	}
	label.setAttribute("class", style);
	
	if (value.columns && value.columns.length > 0) {
		var div0 = document.createElement("div");
		div0.setAttribute("class", "tree-cell");
		label.appendChild(div0);
		zkDatatree2.renderValue(tree, div0, value.columns[0], value.icon);
		var total = label.getBoundingClientRect().width; 
			
		for (var i = 0; i < tree.columns.length; i++) {
			var div = document.createElement("div");
			div.setAttribute("class", "tree-cell");
			label.appendChild(div);
			var col = tree.columns[i];
			if (col.actualWidth != null) {
				total -= col.actualWidth;
				div.style.width = ""+col.actualWidth+"px";
			} else {
				total -= tree.columnWidth;
				div.style.width = ""+tree.columnWidth+"px";
			}
			if (i+1 < value.columns.length)
				zkDatatree2.renderValue(tree, div, value.columns[i+1], null);
		}
		div0.style.width = "" + total + "px"; 
	} else {
		label.setAttribute("class", style + " no-columns");
		zkDatatree2.renderValue(tree, label, value, value.icon);
	}
	zk.listen(label, "click", zkDatatree2.onSelectRow);
}

zkDatatree2.renderValue = function(tree, div, value, icon) {
	if (icon) {
		var iconel = document.createElement("img");
		iconel.setAttribute("src", icon);
		iconel.setAttribute("class", "tree-icon");
		div.appendChild(iconel);
	}
	
	if ( value.value)
		div.appendChild ( document.createTextNode ( value.value ));
	else if (value.html)
		div.innerHTML = div.innerHTML+value.html;
}


zkDatatree2.onFoldUnfold=function(ev) {
	var indicator = ev.currentTarget;
	var div = indicator.parentNode;
	var t = div;
	while ( t != null && t.getAttribute("z.type") != "zul.datatree2.Datatree2")
		t = t.parentNode;
	if (t) {
		var children = indicator.nextElementSibling.lastElementChild;
		div.collapsed =  ! div.collapsed;
		if (div.collapsed) {
			indicator.classList.remove("open");
			children.style.display = "none";
			t.count -= div.count;
		} else {
			indicator.classList.add("open");
			children.style.display = "";
			var data = div.value;
			t.count += div.count;
			if (! data.children ) {
				children.classList.add("waiting");
				var req = {uuid: t.id, cmd: "onAddChildren", 
						data: [JSON.stringify(data.position)], ignorable:true};
				zkau.send (req, 0);		
			}
		}
		zkDatatree2.createFooter(t);
		zkDatatree2.doFilter(t);
		zkDatatree2.findSelectedPosition(t);
	}
}

zkDatatree2.addChildren = function(ed, data) {
	var t = document.getElementById(ed.id+"!tbody");
	if (t) {
		var value = JSON.parse(data);
		var pos = value.position;
		var parentId = "";
		for (var i = 0; i < pos.length; i++)
		{
			parentId += "."+pos[i];
		}
		var parentDiv = document.getElementById(ed.id+"!children"+parentId);
		parentDiv.parentElement/* tree-item* */.parentElement/** tree-itemholder* */.value = value;
		var v;
		var tail;
		var previousPage = parentDiv.firstChild;
		previousPage.style.display = "none";
		while (! previousPage.nextElementSibling.pager) {
			previousPage.nextElementSibling.remove();
		}
		previousPage.nextElementSibling.style.display = "none";
		
		parentDiv.classList.remove("waiting");
		
		for ( var i = 0; i < value.children.length; i++) {
			zkDatatree2.addBranchInternal (ed, value.children[i]);
		}
		zkDatatree2.createFooter(ed);
		if (ed.sortDirection != 0)
			zkDatatree2.doSortRecursive(ed, parentDiv);
		zkDatatree2.doFilter(ed);
	}
}



zkDatatree2.dontBubble=function(ev) {
	ev.stopPropagation();
}

zkDatatree2.updateRow=function(ed, data)
{
	var t = document.getElementById(ed.id+"!tbody");
	if (t) {
		var value = JSON.parse(data);
		var pos = value.position;
		var parentId = "";
		for (var i = 0; i < pos.length; i++)
		{
			parentId += "."+pos[i];
		}
		var parentDiv = document.getElementById(ed.id+"!row"+parentId);
		if (parentDiv) {
			var row = parentDiv;
			row.value = value;
			var treelabel = parentDiv.firstChild;
			if (!treelabel.classList.contains("tree-label"))
			{
				treelabel = parentDiv.lastChild.firstChild;
				parentDiv = parentDiv.lastChild;
			}
			treelabel.remove();
			zkDatatree2.renderRow(ed, parentDiv, value);
			if (ed.sortDirection != 0)
				zkDatatree2.doSort(ed, row.parentElement);
			zkDatatree2.doFilter(ed);
		}
	}
};

zkDatatree2.updateBranch=function(ed, data)
{
	var t = document.getElementById(ed.id+"!tbody");
	if (t) {
		var value = JSON.parse(data);
		var pos = value.position;
		var parentId = "";
		for (var i = 0; i < pos.length; i++)
		{
			parentId += "."+pos[i];
		}
		
		var div = document.getElementById(ed.id+"!row"+parentId);

		var v ;
		if (div != null) {
			while ((v = div.firstChild) != null )
			{
				v.remove();
			}
	
			var selected = ed.selectedItem;
			
			if ( ! value.leaf )
			{
				zkDatatree2.renderBranch(ed, div, value, parentId);
			} else {
				zkDatatree2.renderRow(ed, div, value);
			}
	
			zkDatatree2.setSelected(ed, JSON.stringify(selected));
			if (ed.sortDirection != 0)
				zkDatatree2.doSort(ed, div.parentElement);
			zkDatatree2.doFilter(ed);
		}
	}
};

zkDatatree2.renderBranch=function(ed, div, value, newId) {
	var fold = document.createElement("div");
	fold.setAttribute("class", "tree-collapse");
	var img = document.createElement("img");
	img.setAttribute("src", ed.getAttribute("foldBar"));
	fold.appendChild(img);
	zk.listen(fold, "click", zkDatatree2.onFoldUnfold)
	div.appendChild(fold);
	
	var container = document.createElement("div");
	container.setAttribute("class", "tree-item");
	div.appendChild (container);

	zkDatatree2.renderRow(ed, container, value);
	
	var children = document.createElement("div");
	children.setAttribute("class", "tree-children");
	container.appendChild(children);
	children.setAttribute("id", ed.id+"!children"+newId);
	
	zkDatatree2.addPagination(ed, children);
	children.currentPage = children.soffidVisibleChildren = 0;
	
	if (value.children)
	{
		if ( value.collapsed ) {
			div.collapsed = true;
			children.style.display = "none";
		}
		else {
			div.collapsed = false;
			fold.classList.add("open");
		}
		
		for (var i = 0; i < value.children.length; i++) {
			zkDatatree2.addBranchInternal(ed, value.children[i]);
		}
	} else {
		div.collapsed = true;
		children.style.display = "none";
	}
	if (value.tail) {
		var tail = document.createElement("div");
		tail.setAttribute("class", "tree-tail");
		tail.innerHTML = value.tail;
		tail.isTail = true;
		children.appendChild(tail);
	}

}
zkDatatree2.sendClientAction=function(el,event,data)
{
	var target = el;
	while ( target != null && target.getAttribute("z.type") != "zul.datatree2.Datatree2") {
		target = target.parentNode;
	}

	var row = el;
	while ( ! row.classList.contains("tree-itemholder") ) 
		row = row.parentNode;

	var label = row.firstElementChild;
	if (label.classList.contains("tree-collapse"))
		label = label/*tree-collapse*/.nextElementSibling/*tree-item*/.firstElementChild/*label*/;
		
	
	zkDatatree2.selectRow(target, row, label);

	if (data) {
		data.splice(0,0, event);
	} else {
		data = [ event];
	}
	var req = {uuid: target.id, cmd: "onClientAction", data:data};
	zkau.send (req, 5);		
}

zkDatatree2.evaluateInContext = function (js, context) {
    return function() { return eval(js); }.call(context);
}

zkDatatree2.findExpression=function(v, j) {
	var i1 = v.indexOf("${", j);
	var i2 = v.indexOf("#{", j);
	if (i2 < 0) return i1;
	if (i1 < 0) return i2;
	if (i1 < i2) return i1;
	return i2;
}
zkDatatree2.replaceExpressions = function (template,value) {
	var v = "";
	var j = 0;
	do {
		var i = zkDatatree2.findExpression(template, j); 
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
					v = v + zkDatatree2.escapeHTML(value[expr]);
				else
					v = v + zkDatatree2.escapeHTML(zkDatatree2.evaluateInContext(expr, value));
			}
		}
	} while (true);
	return v;
}

zkDatatree2.escapeHTML=function(t) {
    return t.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

zkDatatree2.cleanup = function (ed) {
};

/** Called by the server to set the attribute. */
zkDatatree2.setAttr = function (ed, name, value) {
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
	case "data":
		window.setTimeout(() => {
			var header = document.getElementById(ed.id+"!thead");
			zkDatatree2.createHeaders (ed, header);
			zkDatatree2.setData(ed, value); 
		},50 );
		return true;
	case "footer":
		ed.footer = (value != "false");
		zkDatatree2.createFooter(ed);
		return true;
	case "multiselect":
		ed.multiselect = (value != "false");
		zkDatatree2.refresh(ed);
		return true;
	case "maxheight":
		ed.maxheight = value;
		document.getElementById(ed.id+"!tbody")
			.style.maxHeight = value;
		return true;
	}
	return false;
};

zkDatatree2.findSelectedPositionLoop=function(t, container, id) {
	for (var row = container.firstElementChild; row != null; row = row.nextElementSibling) {
		if (!row.pager && !row.isTail && row.style.display != "none" ) {
			t.selectedPosition ++;
			if (id == row.id) {
				return true;
			} else if (! row.value.leaf ) {
				var container2 = row.lastElementChild.lastElementChild;
				if (container2.style.display != "none" ) {
					if (zkDatatree2.findSelectedPositionLoop (t, container2, id))
						return true;
				}
			}
		}
	}
	return false;
};


// Finds the order number of the row in the full tree, for pagers
zkDatatree2.findSelectedPosition=function(t) {
	var id = t.id+"!row";
	t.selectedPosition = 0;
	if (t.selectedItem ) {
		for (var i = 0; i < t.selectedItem.length; i++)
			id += "."+t.selectedItem[i];
		var tb = document.getElementById(t.id+"!tbody");
		if ( ! zkDatatree2.findSelectedPositionLoop(t, tb, id) )
			t.selectedPosition = 0;
	}
	zkDatatree2.updatePagers(t);
};

zkDatatree2.onSelectRow=function(ev) {
	var target = ev.currentTarget;
	var t = target;
	var selectedRow = null;
	while ( t != null && t.getAttribute("z.type") != "zul.datatree2.Datatree2") {
		if (selectedRow == null && t.value != null) {
			selectedLabel = t;
			selectedRow = t.parentElement;
		}
		t = t.parentNode;
	}
	zkDatatree2.selectRow(t, selectedRow, target);
}

zkDatatree2.selectRow=function(t, selectedRow, label) {
	if (t.selectedRow == selectedRow) return; // Ignore duplicated message
	t.selectedRow = selectedRow;
	var value = label.value;
	var tbody = document.getElementById(t.id+"!tbody");
	var oldLabel = t.selectedLabel;
	if (oldLabel != null)
	{
		oldLabel.classList.remove("selected")
	}
	label.classList.add("selected");
	t.selectedLabel = label;
	t.selectedItem = value.position;
	if (value && value.position)
	{
		var req = {uuid: t.id, cmd: "onSelect", data : [ JSON.stringify( value.position )]};
		zkau.send (req, 5);		
	}	
	zkDatatree2.findSelectedPosition(t);
}

/** Selected by the server * */
zkDatatree2.setSelected=function(t, pos) {
	if (t.selectedLabel != null) {
		t.selectedLabel.classList.remove("selected");
	}
	t.selectedItem = JSON.parse(pos);
	t.selectedRow = null;
	t.selectedLabel = null;
	var id = t.id+"!row";
	for (var i = 0; i < t.selectedItem.length; i++)
		id += "."+t.selectedItem[i];
	var row = document.getElementById(id);
	if (row)
	{
		var label;
		if ( row.value.leaf )
			label = row.firstElementChild;
		else
			label = row.lastElementChild.firstElementChild;
		label.classList.add("selected");
		t.selectedRow = row;
		t.selectedLabel = label;
	}
	zkDatatree2.findSelectedPosition(t);
}

zkDatatree2.sendSelect=function(table) {
	var selected=[];
	var tbody = document.getElementById(table.id+"!tbody"); 
	for (var row = tbody.firstElementChild; row != null; row = row.nextElementSibling)
	{
		var cb2 = row.firstElementChild/* td */.firstElementChild/* input */;
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

zkDatatree2.onSort=function(ev) {
	var target = ev.currentTarget;
	var ed = target.parentNode.parentNode;
	ed.sortColumn = target.sortColumn;
	if (ed.sortDirection == 0)
		ed.sortDirection = 1;
	else
		ed.sortDirection = - ed.sortDirection;

	var header = document.getElementById(ed.id+"!thead");
	zkDatatree2.createHeaders (ed, header);
	zkDatatree2.fixupColumns ( ed );
	var body = document.getElementById(ed.id+"!tbody");
	zkDatatree2.doSortRecursive(ed, body);
	zkDatatree2.findSelectedPosition(ed);
}

zkDatatree2.doSortRecursive=function(ed, container) {
	zkDatatree2.doSort(ed, container);
	for (var child = container.firstElementChild; child != null; child = child.nextElementSibling) {
		if ( !child.isTail && !child.pager) {
			var item = child.lastElementChild;
			if (item.getAttribute("class") == "tree-item")
				zkDatatree2.doSortRecursive (ed, item.lastElementChild);
		}
	}
}

zkDatatree2.extractValue = function (ed, a) {
	if (a.value.columns && ed.sortColumn+1 < a.value.columns.length)
		return a.value.columns[ed.sortColumn+1];
	if (a.value.columns == null && ed.sortColumn == -1)
		return a.value;
	else 
		return {value:""}; 
}

zkDatatree2.doSort=function(ed, container) {
	var direction = ed.sortDirection;
	var children = [...container.children];
	zkDatatree2.quickSort(children,
			(a,b) => {
				if (a.pager && a.pagerUp) return -1;
				if (b.pager && b.pagerUp) return +1;
				
				if (a.isTail) return +1;
				if (b.isTail) return -1;
				
				if (a.pager) return +1;
				if (b.pager) return -1;
				
				var r;
				
				var v1 = zkDatatree2.extractValue(ed, a);
				var v2 = zkDatatree2.extractValue(ed, b);
				
				v1 = v1.value != null ? v1.value : v1.html;
				v2 = v2.value != null ? v2.value : v2.html;
				
				if (v1 < v2) r = -direction;
				else if (v1 > v2) r = +direction;
				else {
					v1 = a.value.position[ a.value.position.length - 1];
					v2 = b.value.position[ b.value.position.length - 1];
					if (v1 < v2) r = -1;
					else if (v1 > v2) r = +1;
					else r = 0; 
				}
				return r;
			}
		);
	for (var i = 0; i < children.length; i++)
		container.insertBefore(children[i], null);
}

zkDatatree2.deleteRow=function(ed, pos)
{
	if (ed)
	{
		pos = JSON.parse(pos);
		var parentId = "";
		for (var i = 0; i < pos.length-1; i++)
		{
			parentId += "."+pos[i];
		}
		var id = "";
		for (var i = 0; i < pos.length; i++)
		{
			id += "."+pos[i];
		}
		var parentDiv;
		if (pos.length > 1) {
			parentDiv = document.getElementById(ed.id+"!children"+parentId);
		} else {
			parentDiv = document.getElementById(ed.id+"!tbody");
		}
		
		var toRemove = document.getElementById(ed.id+"!row"+id);
		if (toRemove) {
			if (ed.selectedRow == toRemove) {
				ed.selectedItem = null;
				ed.selectedLabel = null;
				ed.selectedRow = null;
				ed.selectedPosition = 0;
			}
			toRemove.remove();
			
			for (var sibling = parentDiv.firstElementChild; sibling != null; sibling = sibling.nextElementSibling) {
				if ( !sibling.pager) {
				var siblingId = sibling.id;
					siblingId = siblingId.substring( ed.id.length+4/* !row */+parentId.length+1);
					siblingId = parseInt (siblingId);
					if (siblingId > pos[pos.length-1])
						zkDatatree2.renameIds (ed,
								parentId+"."+siblingId,
								parentId+"."+(siblingId-1),
								pos.length-1);
				}
			}
			
			ed.refreshCounter ++;
			var counter = ed.refreshCounter;
			window.setTimeout( function() {
				if (counter == ed.refreshCounter) {
					zkDatatree2.doFilter(ed);
					zkDatatree2.findSelectedPosition(ed);
				}
			}, 100);
			
		}
	}
}

zkDatatree2.renameIds=function (ed, oldId, newId, index) {
	var row = document.getElementById (ed.id+"!row"+oldId);
	row.id = ed.id+"!row"+newId;
	var value = row.value;
	value.position[index] = value.position[index] - 1;
	var children = document.getElementById (ed.id+"!children"+oldId);
	if (children)
	{
		children.id = ed.id+"!children"+newId;
		for (var child = children.firstElementChild; child != null; child = child.nextElementSibling) {
			if (!child.pager && child.id) {
				var lastStep = child.id.substring(child.id.lastIndexOf("."));
				zkDatatree2.renameIds (ed, oldId + lastStep, newId + lastStep, index);
			}
		}
	}
}

zkDatatree2.quickSort = function (
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

zkDatatree2.next=function(t)
{
	var row = t.selectedRow;
	var label = t.selectedLabel;
	label.classList.remove("selected");
	
	if (row) {
		if (row.classList.contains("tree-item"))
			row = row.parentElement; /* tree-item-container */
		var found = false;
		while (! found) {
			var next = null;
			if (!row.value.leaf) {
				var container = row.lastElementChild.lastElementChild;
				if (container.style.display != 'none' && 
						container.firstElementChild.nextElementSibling != null &&
						! container.firstElementChild.nextElementSibling.pager ) 
					next = container.firstElementChild.nextElementSibling;
			}
			if (next == null) {
				next = row;
				while (next.nextElementSibling == null || next.nextElementSibling.isTail || next.nextElementSibling.pager)
					next = next.parentElement/* tree-children */.parentElement/* tree-item */.parentElement/* tree-item */;
				next = next.nextSibling;
			}
			row = next;
			if (row == null || row.value == null) return;
			else found = row.style.display != 'none';
		}
		t.selectedRow = row;
		if (t.selectedRow.value.leaf)
			t.selectedLabel = t.selectedRow.firstElementChild;
		else
			t.selectedLabel = t.selectedRow.lastElementChild.firstElementChild;
		t.selectedLabel.classList.add("selected");
		t.selectedItem = row.value.position;
		t.selectedPosition ++;
		zkDatatree2.updatePagers(t);
		if (row.value && row.value.position)
		{
			var req = {uuid: t.id, cmd: "onSelect", data : [ JSON.stringify( row.value.position )]};
			zkau.send (req, 5);		
		}	
	}
}

zkDatatree2.previous=function(t)
{
	var row = t.selectedRow;
	var label = t.selectedLabel;
	if (label)
		label.classList.remove("selected");
	
	if (row) {
		var found = false;
		while (! found) {
			var next = row;
			if (next.classList.contains("tree-item"))
				next = next.parentElement; /* tree-container */
			if (next.previousElementSibling == null ||
				next.previousElementSibling.pager) {
				next = next.parentElement/* tree-children */.parentElement/* tree-item */.parentElement/* tree-item */;
			} else {
				next = next.previousElementSibling;
				while ( next.value == null ||  !next.value.leaf ) {
					var container = next.lastElementChild.lastElementChild;
					if (container.style.display == 'none')
						break;
					else {
						var next2 = container.lastElementChild;
						while (next2 != null && (next2.isTail || next2.pager)) 
							next2 = next2.previousElementSibling;
						if (next2 != null) next = next2;
						else break;
					}
				}
			}
			row = next;
			if (row == null || row.value == null) return;
			else found = row.style.display != 'none';
		}
		t.selectedRow = row;
		if (t.selectedRow.value.leaf)
			t.selectedLabel = t.selectedRow.firstElementChild;
		else
			t.selectedLabel = t.selectedRow.lastElementChild.firstElementChild;
		t.selectedLabel.classList.add("selected");
		t.selectedItem = row.value.position;
		t.selectedPosition --;
		if (row.value && row.value.position)
		{
			var req = {uuid: t.id, cmd: "onSelect", data : [ JSON.stringify( row.value.position )]};
			zkau.send (req, 5);		
		}	
		zkDatatree2.updatePagers(t);
	}
}



zkDatatree2.inputValues = function(td) {
	var s = "";
	for ( var input = td.firstElementChild; input != null; input = input.nextElementSibling) {
		if (input.tagName == "INPUT") {
			if (input.type = "checkbox" && input.checked) {
				s += "true";
			} else if (input.value) {
				s +=  input.value;
			}
		} else {
			s += zkDatatree2.inputValues ( input );
		}
	}
	return s;
}

zkDatatree2.downloadTreeLabel = function (ed, level, label) {
	var s = "";
	s += level;
	if (ed.columns) {
		for (var child = label.firstElementChild; child != null; child = child.nextElementSibling) {
			s += ",";
			var t = child.textContent + zkDatatree2.inputValues(child);
			s += zkDatatree2.quote(t);
		}		
	}
	s += "\n";
	return s;
}


zkDatatree2.downloadTreeItemHolder = function (ed, level, treeitemHolder) {
	var s = "";
	if (treeitemHolder.getAttribute("class") == "tree-tail")
		return s;
	var item = treeitemHolder.lastElementChild;
	if (item == null)
		return s; //Page up or down
	if (item.getAttribute("class") == "tree-item") {
		s += zkDatatree2.downloadTreeLabel (ed, level, item.firstElementChild);
		var treeChildren = item.lastElementChild;
		for (var child = treeChildren.firstElementChild; child != null; child = child.nextElementSibling) {
			s += zkDatatree2.downloadTreeItemHolder (ed, level+1, child);
		}
	} else {
		s += zkDatatree2.downloadTreeLabel (ed, level, item);
	}
	return s;
}

zkDatatree2.downloadCsv=function(ed) {
	var s = "\"level\",";
	s += zkDatatree2.quote(ed.getAttribute("header") );
	if (ed.columns) {
		for (var i = 0; i < ed.columns.length; i++) {
			s += ","+zkDatatree2.quote ( ed.columns[i].name );
		}
	}

	s += "\n";
	var tbody = document.getElementById(ed.id+"!tbody");
	for (var tr = tbody.firstElementChild; tr != null; tr = tr.nextElementSibling) {
		s += zkDatatree2.downloadTreeItemHolder(ed, 1, tr);
	}
	
    var data = new Blob([s], {type: 'text/csv', name: 'export.csv'});
    data.name = 'soffid.csv';
    
    // If we are replacing a previously generated file we need to
    // manually revoke the object URL to avoid memory leaks.
    if (zkDatatree2.file !== null) {
      window.URL.revokeObjectURL(zkDatatree2.file);
    }

    zkDatatree2.file = window.URL.createObjectURL(data);
    window.open( zkDatatree2.file );
}

zkDatatree2.quote = function(t) {
	return "\""+t.replace(/"/g, "\\\"")+"\"";
}

/************ DRAG & DROP SUPPORT */
zkDatatree2.onDragover=function(event) {
	var row = zkDatatree2.dragging;
	var tree = row.tree;
	var currentTree = event.currentTarget/*div.tbody*/.parentElement/*div.datatable*/;
	if (tree == currentTree) {
		if (tree.previousDrag) {
			tree.previousDrag.classList.remove("drag-highlight");
			tree.previousDrag = null;
		}
		var tr = document.elementFromPoint(event.x, event.y);
		while (tr != null && ! (tr.tagName == 'DIV' && tr.classList.contains("tree-label"))) {
			tr = tr.parentElement;
		}
		if (tr != null && tr.tree == tree) {
			if  (tr != row) {
				tree.previousDrag = tr;
				tree.previousDrag.classList.add("drag-highlight");
			}
		}
		event.preventDefault();
	}
}

zkDatatree2.onDragStart=function(event) {
	zkDatatree2.dragging = event.currentTarget;
	event.dataTransfer.setData("text/plain", JSON.stringify(event.currentTarget.value));
	zkDatatree2.setSelected(event.currentTarget.tree, JSON.stringify(event.currentTarget.value.position));
	zkDatatree2.dragging.classList.add("selected");
}

zkDatatree2.onDragEnd = function(event) {
	var src = zkDatatree2.dragging;
	var target = zkDatatree2.draggedBefore;
	var tree = src.tree;
	if (tree.previousDrag) {
		tree.previousDrag.classList.remove("drag-highlight");
		zkDatatree2.dragging.classList.remove("selected");
		zkau.send ({uuid: tree.id, cmd: "onReorder", data : [
			JSON.stringify( src.value.position ),
			JSON.stringify( tree.previousDrag.value.position ) ]}, 5);		
		tree.previousDrag = null;
	}
}

zkDatatree2.onDrop = function(event) {
	event.preventDefault();
}

zkDatatree2.setFilter = function(t,data) {
	if (t.enablefilter) {
		var d = JSON.parse(data);
		var head = document.getElementById(t.id+"!tfilter");
		if (head) {
			var col = head.firstElementChild;
			var filter = [];
			for (var i = 0; i < head.childElementCount; i++) {
				var tb = col.firstElementChild;
				tb.value=d[i]; 
				if (d[i] && d[i].trim().length > 0)
					filter.push (d[i].toLowerCase().split(" "));
				else
					filter.push([]);
				col = col.nextElementSibling;
			}
			t.filter = filter;
			zkDatatree2.doFilter(t);
		} 		
	}	
	
}