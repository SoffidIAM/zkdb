/* Soffid select */

zk.load("zul.zul");

zkSelect = {};

zkSelect.init = function (ed) {
	ed.sort = ed.getAttribute("sort") == "true";
	ed.value = ed.selectedValue = ed.getAttribute("value");
	ed.addEventListener("input", zkSelect.onSelect);
	var data = ed.getAttribute("options");
	ed.index = [];
	if (data) 
	{
		ed.data = JSON.parse(data);
		zkSelect.refresh(ed);
		ed.value = ed.selectedValue;
	}
};

zkSelect.onSize=function(ed) {
}

zkSelect.refresh = function (t) {
	var v ;
	while ((v = t.firstChild) != null )
	{
		v.remove();
	}
	for (var i = 0; i < t.data.length; i++)
	{
		var option = t.data[i];
		zkSelect.addRowInternal(t, i, option);
	}
	if (t.sort)
		zkSelect.doSort(t);
};

zkSelect.setData = function(ed, data) {
	try {
		ed.data = JSON.parse(data);
		zkSelect.refresh(ed);
	} catch (error) {
		alert("Error decoding select options "+data+": "+error);
	}
}

zkSelect.addRow=function(ed, pos, value)
{
	pos = parseInt(pos);
	zkSelect.addRowInternal (ed, pos, JSON.parse(value));
	zkSelect.createFooter(ed);
	if (ed.sortColumn >= 0)
		zkSelect.doSort(ed);
}

zkSelect.addRows=function(ed, pos, values)
{
	pos = parseInt(pos);
	var values = JSON.parse(values);
	for (var i = 0; i < values.length; i++)
		zkSelect.addRowInternal (ed, pos+i, values[i]);
	if (ed.sort)
		zkSelect.doSort(ed);
}

zkSelect.addRowInternal=function(ed, pos, value)
{
	var op = document.createElement("option");
	if ( pos >= ed.index.length)
	{
		ed.index[pos] = op.id;
	}	
	else
	{
		ed.index.splice (pos, 0, op.id);
	}
	zkSelect.fillRow(ed,op,value);
}

zkSelect.fillRow=function(t,op,option)
{
	if (option.value != undefined)
		op.setAttribute("value", option.value);

	if (option.label)
		op.appendChild( document.createTextNode ( option.label ));
	else if (option.value)
		op.appendChild( document.createTextNode ( option.value ));
	if ( option.value == t.selectedValue)
		op.setAttribute("selected", "selected");
	t.appendChild(op);
}


zkSelect.updateRow=function(ed, pos, value)
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
			zkSelect.fillRow(ed, tr, value);
		}
	}
	
};

zkSelect.cleanup = function (ed) {
};

/** Called by the server to set the attribute. */
zkSelect.setAttr = function (ed, name, value) {
	switch (name) {
	case "selected":
		ed.selectedValue = ed.value = value;
		return true;
	case "options":
		zkSelect.setData(ed, value);
		return true;
	}
	return false;
};

zkSelect.doSort=function(ed) {
	var children = [...ed.children];
	zkSelect.quickSort(children,
			(a,b) => {
				var r;
				var v1 = new String(a.value).toLowerCase();
				var v2 = new String(b.value).toLowerCase();
				if (v1 < v2) r = -1;
				else if (v1 > v2) r = +1;
				else r = 0; 
				return r;
			}
		);
	for (var i = 0; i < children.length; i++)
		tbody.insertBefore(children[i], null);
}

zkSelect.deleteRow=function(ed, pos)
{
	if (ed.index[pos])
	{
		var id = ed.index[pos];
		document.getElementById(id).remove();
		ed.index.splice(pos,1);
	}
}



zkSelect.quickSort = function (
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

zkSelect.onSelect = function(ev)
{
	var t = ev.currentTarget;
	t.selectedValue = t.value;
	var req = {uuid: t.id, cmd: "onSelect", data : [t.value]};
	zkau.send (req, 5);		
}
