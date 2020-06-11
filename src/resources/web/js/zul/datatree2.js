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
 *        - tree-children
 */

zk.load("zul.zul");

zkDatatree2 = {};

zkDatatree2.init = function (ed) {
	ed.sortable = ed.getAttribute("sortable");
	ed.sortDirection = ed.getAttribute("sortDirection") == null ? 0: parseInt(ed.getAttribute("sortDirection"));
	ed.enablefilter = "false" != ed.getAttribute("enablefilter");
	ed.footer = ed.getAttribute("footer") != "false";
	ed.index = [];
	ed.nextRowId = 0;
	ed.maxheight = ed.getAttribute("maxheight");
	ed.selectedPosition = 0;
	ed.selectedRow = null;
	ed.selectedLabel = null;
	ed.count = 0;
	if (!ed.pagers)
		ed.pagers=[];
	zkDatatree2.refresh(ed);
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
		var tbd = document.createElement("div")
		tbd.setAttribute("id", t.id+"!tbody");
		tbd.setAttribute("class", "tbody")
		t.appendChild(tbd);
		if (t.maxheight)
			tbd.style.maxHeight = t.maxheight;
		var tf = document.createElement("div");
		tf.setAttribute("class", "tfoot")
		tf.setAttribute("id", t.id+"!tfoot");
		t.appendChild(tf);
		var tr = document.createElement("div");
		tr.setAttribute("id", t.id+"!tcolumn")
		th.appendChild(tr);
		zkDatatree2.createHeaders ( t, tr );
		if ( t.enablefilter)
		{
			tr = document.createElement("div");
			th.appendChild(tr);
			zkDatatree2.createFilters(t, tr);
		}
		zkDatatree2.createFooter(t);
	} catch (e) {
		console.log(e)
	}
};

zkDatatree2.createHeaders = function (ed, tr) {
	var v ;
	while ((v = tr.firstChild) != null )
	{
		v.remove();
	}
	if ( ed.sortable )
	{
		zkDatatree2.createSortArrows(ed, tr);
		tr.setAttribute("class", "sortable-column")
		if (!tr.registeredEvent) {
			tr.addEventListener("click", zkDatatree2.onSort)
			tr.registeredEvent = true;
		}
	}
	tr.appendChild( document.createTextNode ( ed.getAttribute("header") ));
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
		footer.appendChild( document.createTextNode ( "Total rows: "+ ed.count ));
	}
};

zkDatatree2.createFilters = function (ed, tr) {
	var v ;
	while ((v = tr.firstChild) != null )
	{
		v.remove();
	}
	tr.setAttribute("class", "filter-cell")
	var tb = document.createElement("input");
	tb.setAttribute("type", "text");
	tb.setAttribute("class", "filter");
	tb.setAttribute("placeholder", "Filter");
	tb.addEventListener("input", zkDatatree2.onFilter)
	tb.setAttribute("id", ed.id+"!filter");
	tr.appendChild(tb);
};

zkDatatree2.onFilter=function(ev) {
	var input = ev.currentTarget;
	var t = input.parentNode/* filter-cell */.parentNode/* thead */.parentNode;
	t.filter = input.value.toLowerCase().split(" ");
	zkDatatree2.doFilter(t);
	var tb = document.getElementById(t.id+"!tbody");
	zkDatatree2.expandFirst (t, tb );
}

zkDatatree2.expandFirst=function(t, div) {
	for (var row = div.firstElementChild; row != null; row = row.nextElementSibling)
	{
		if (!row.hidden) {
			var treeitem = row.lastElementChild;
			if (treeitem.classList.contains("tree-item")) {
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
	for (var row = div.firstElementChild; row != null; row = row.nextElementSibling)
	{
		var visible = true;
		if ( !row.isTail) {
			var label = row.lastElementChild;
			var children = null;
			if (label.classList.contains("tree-item")) {
				children = label.lastElementChild;
				label = label.firstElementChild;
			}
			if (filter && !parentMatch) {
				for (var f = 0; f < filter.length; f++)
				{
					if (label.textContent.toLowerCase().indexOf(filter[f]) < 0)
					{
						visible = false;
						break;
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
				r.visible = true;
				r.count ++;
				row.hidden = false;
				row.style.display="";
				anyVisible = true;
			} else {
				row.hidden = true;
				row.style.display="none";
			}
		}
	}
	return r;
}

zkDatatree2.doFilter=function(t) {
	var tb = document.getElementById(t.id+"!tbody");
	var r = zkDatatree2.doFilterLoop (t, tb, t.filter, false);
	t.count = r.count;
	t.firstMatch = r.firstMatch;
	zkDatatree2.createFooter(t);
	zkDatatree2.updatePagers(t);
}

zkDatatree2.createSortArrows=function(ed, td, column)
{
	var div = document.createElement("div");
	div.setAttribute("class", "sort-indicator");
	td.appendChild(div);
	if (ed.sortDirection != 0)
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
		// Generate rows
		ed.index = [] ;
		data = JSON.parse(data);
		for ( var i = 0; i < data.children.length; i++) {
			zkDatatree2.addBranchInternal (ed, data.children[i]);
		}
		zkDatatree2.createFooter(ed);
		if (ed.sortDirection != 0)
			zkDatatree2.doSortRecursive(ed, t);
		zkDatatree2.doFilter(ed);
	}
}


zkDatatree2.addBranch=function(t, value)
{
	var value = JSON.parse(value);
	var row = zkDatatree2.addBranchInternal (t, value);
	if (t.sortDirection != 0)
		zkDatatree2.doSortRecursive(t, row.parentElement);
	zkDatatree2.createFooter(t);
	if (t.sortDirection != 0)
		zkDatatree2.doSortRecursive(t, row.parentElement);
	zkDatatree2.doFilter(t);
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
	
	var div = document.createElement("div");
	div.setAttribute("class", "tree-itemholder");
	div.setAttribute("id" , ed.id+"!row"+newId);
	div.value = value;
	div.collapsed = true;
	if ( parentDiv.lastElementChild != null && parentDiv.lastElementChild.isTail)
		parentDiv.appendChild(div, parentDiv.lastElementChild);
	else
		parentDiv.appendChild(div);
		
	if ( ! value.leaf )
	{
		var fold = document.createElement("div");
		fold.setAttribute("class", "tree-collapse");
		var img = document.createElement("img");
		img.setAttribute("src", "/img/foldBar.svg");
		fold.appendChild(img);
		zk.listen(fold, "click", zkDatatree2.onFoldUnfold)
		div.appendChild(fold);
		
		var container = document.createElement("div");
		container.setAttribute("class", "tree-item");
		div.appendChild (container);

		zkDatatree2.renderRow(container, value);
		
		var children = document.createElement("div");
		children.setAttribute("class", "tree-children");
		container.appendChild(children);
		children.setAttribute("id", ed.id+"!children"+newId);
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
		zkDatatree2.renderRow(div, value);
	}
	return div;
}


zkDatatree2.renderRow=function(container, value) {
	var label = document.createElement("div");
	label.setAttribute("class", "tree-label");
	label.value = value;
	
	if (value.icon) {
		var icon = document.createElement("img");
		icon.setAttribute("src", value.icon);
		icon.setAttribute("class", "tree-icon");
		label.appendChild(icon);
	}
	
	if ( value.value)
		label.appendChild ( document.createTextNode ( value.value ));
	else if (value.html)
		label.innerHTML = label.innerHTML+value.html;
	container.insertBefore(label, container.firstChild);
	zk.listen(label, "click", zkDatatree2.onSelectRow);
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
		while ((v = parentDiv.firstChild) != null ) {
			if (v.isTail) break;
			v.remove();
		}
		
		for ( var i = 0; i < value.children.length; i++) {
			zkDatatree2.addBranchInternal (ed, value.children[i]);
		}
		parentDiv.classList.remove("waiting");
		zkDatatree2.createFooter(ed);
		if (ed.sortColumn != 0)
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
			zkDatatree2.renderRow(parentDiv, value);
			if (ed.sortDirection != 0)
				zkDatatree2.doSort(ed, row.parentElement);
			zkDatatree2.doFilter(ed);
		}
	}
};

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

zkDatatree2.replaceExpressions = function (template,value) {
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
		if (!row.isTail && row.style.display != "none" ) {
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
		row.classList.add("selected");
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
	if (ed.sortDirection == 0)
		ed.sortDirection = 1;
	else
		ed.sortDirection = - ed.sortDirection;

	zkDatatree2.createHeaders (ed, target);
	var body = document.getElementById(ed.id+"!tbody");
	zkDatatree2.doSortRecursive(ed, body);
	zkDatatree2.findSelectedPosition(ed);
}

zkDatatree2.doSortRecursive=function(ed, container) {
	zkDatatree2.doSort(ed, container);
	for (var child = container.firstElementChild; child != null; child = child.nextElementSibling) {
		if ( !child.isTail) {
			var item = child.lastElementChild;
			if (item.getAttribute("class") == "tree-item")
				zkDatatree2.doSortRecursive (ed, item.lastElementChild);
		}
	}
}

zkDatatree2.doSort=function(ed, container) {
	var direction = ed.sortDirection;
	var children = [...container.children];
	zkDatatree2.quickSort(children,
			(a,b) => {
				if (a.isTail) return +1;
				if (b.isTail) return -1;
				var r;
				var v1 = a.value.value != null ? a.value.value : a.value.html;
				var v2 = b.value.value != null ? b.value.value : b.value.html;
				if (v1 < v2) r = -1;
				else if (v1 > v2) r = +1;
				else r = 0; 
				return r*direction;
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
				var siblingId = sibling.id;
				siblingId = siblingId.substring( ed.id.length+4/* !row */+parentId.length+1);
				siblingId = parseInt (siblingId);
				if (siblingId > pos[pos.length-1])
					zkDatatree2.renameIds (ed,
							parentId+"."+siblingId,
							parentId+"."+(siblingId-1),
							pos.length-1);
			}
			
			
			zkDatatree2.doFilter(ed);
			zkDatatree2.findSelectedPosition(ed);
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
			var lastStep = child.id.substring(child.id.lastIndexOf("."));
			zkDatatree2.renameIds (ed, oldId + lastStep, newId + lastStep);
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
		var found = false;
		while (! found) {
			var next = null;
			if (!row.value.leaf) {
				var container = row.lastElementChild.lastElementChild;
				if (container.style.display != 'none')
					next = container.firstElementChild;
			}
			if (next == null) {
				next = row;
				while (next.nextElementSibling == null)
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
			if (next.previousElementSibling == null) {
				next = next.parentElement/* tree-children */.parentElement/* tree-item */.parentElement/* tree-item */;
			} else {
				next = next.previousSibling;
				while (!next.value.leaf) {
					var container = next.lastElementChild.lastElementChild;
					if (container.style.display == 'none')
						break;
					else
						next = container.lastElementChild;
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

