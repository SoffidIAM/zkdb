/* Soffid table */

zk.load("zul.zul");

zkPager = {};

zkPager.init = function (e) {
	var id = e.getAttribute("datatable");
	if (id)
	{
		var table = document.getElementById(id);
		if (table)
		{
			zkDatatable.registerPager(table, e);
			var p = document.getElementById(e.id+"!prev");
			if (p) {
				p.datatable = table;
				zk.listen(p, "click", zkPager.onPrevious);
			}
			var n = document.getElementById(e.id+"!next");
			if (n) {
				n.datatable = table;
				zk.listen(n, "click", zkPager.onNext);
			}
		}
		
	}
	var id2 = e.getAttribute("datatree2");
	if (id2)
	{
		var tree = document.getElementById(id2);
		if (tree)
		{
			zkDatatree2.registerPager(tree, e);
			var p = document.getElementById(e.id+"!prev");
			if (p) {
				p.datatree2 = tree;
				zk.listen(p, "click", zkPager.onPrevious);
			}
			var n = document.getElementById(e.id+"!next");
			if (n) {
				n.datatree2 = tree;
				zk.listen(n, "click", zkPager.onNext);
			}
		}
		
	}
};

zkPager.cleanup = function (e) {
	var id = e.getAttribute("datatable");
	if (id)
	{
		var table = document.getElementById(id);
		if (table)
			zkDatatable.deregisterPager(e);
		
	}
	var id = e.getAttribute("datatree2");
	if (id)
	{
		var table = document.getElementById(id);
		if (table)
			zkDatatree2.deregisterPager(e);
		
	}
};

zkPager.update =function(e, pos, total)
{
	if (pos == 0)
		e.style.display = "none";
	else
		e.style.display = "";
	var t = document.getElementById(e.id+"!text");
	var p = document.getElementById(e.id+"!prev");
	var n = document.getElementById(e.id+"!next");
	if (t)
		t.innerHTML = "" + pos + " / " +total;
	if (p)
	{
		if (pos <= 1)
			p.style.visibility = "hidden";
		else
			p.style.visibility = "";
	}
	if (n)
	{
		if (pos >= total )
			n.style.visibility = "hidden";
		else
			n.style.visibility = "";
	}
}

zkPager.onNext = function(ev)
{
	var arrow = ev.currentTarget;
	if (arrow.datatable)
		zkDatatable.next ( arrow.datatable );
	if (arrow.datatree2)
		zkDatatree2.next ( arrow.datatree2 );
}

zkPager.onPrevious = function(ev)
{
	var arrow = ev.currentTarget;
	if (arrow.datatable)
		zkDatatable.previous ( arrow.datatable );
	if (arrow.datatree2)
		zkDatatree2.previous ( arrow.datatree2 );
}

