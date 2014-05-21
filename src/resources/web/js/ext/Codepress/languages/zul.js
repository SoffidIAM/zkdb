/*
 * CodePress regular expressions for HTML syntax highlighting
 */

// HTML
Language.syntax = [
	{ input : /(&lt;[^!]*?&gt;)/g, output : '<b>$1</b>'	}, // all tags
	{ input : /(&lt;style.*?&gt;)(.*?)(&lt;\/style&gt;)/g, output : '<em>$1</em><em>$2</em><em>$3</em>' }, // style tags
	{ input : /(&lt;zscript.*?&gt;)(.*?)(&lt;\/zscript&gt;)/g, output : '<strong>$1</strong><tt>$2</tt><strong>$3</strong>' }, // script tags
	{ input : /=(".*?")/g, output : '=<s>$1</s>' }, // atributes double quote
	{ input : /=('.*?')/g, output : '=<s>$1</s>' }, // atributes single quote
	{ input : /(&lt;!--.*?--&gt.)/g, output : '<ins>$1</ins>' }, // comments 
	{ input : /\b(abstract|continue|for|new|switch|assert|default|goto|package|synchronized|boolean|do|if|private|this|break|double|implements|protected|throw|byte|else|import|public|throws|case|enum|instanceof|return|transient|catch|extends|int|short|try|char|final|interface|static|void|class|finally|long|strictfp|volatile|const|float|native|super|while)\b/g, output : '<i>$1</i>'}, // reserved words
	{ input : /([^:]|^)\/\/(.*?)(<br|<\/P)/g, output : '$1<i>//$2</i>$3'}, // comments //	
	{ input : /\/\*(.*?)\*\//g, output : '<i>/*$1*/</i>' }// comments /* */
]

Language.snippets = [
	{ input : 'aref', output : '<a href="$0"></a>' },
	{ input : 'h1', output : '<h1>$0</h1>' },
	{ input : 'h2', output : '<h2>$0</h2>' },
	{ input : 'h3', output : '<h3>$0</h3>' },
	{ input : 'h4', output : '<h4>$0</h4>' },
	{ input : 'h5', output : '<h5>$0</h5>' },
	{ input : 'h6', output : '<h6>$0</h6>' },
	{ input : 'html', output : '<html>\n\t$0\n</html>' },
	{ input : 'head', output : '<head>\n\t<meta http-equiv="content-type" content="text/html; charset=utf-8" />\n\t<title>$0</title>\n\t\n</head>' },
	{ input : 'img', output : '<img src="$0" alt="" />' },
	{ input : 'input', output : '<input name="$0" id="" type="" value="" />' },
	{ input : 'label', output : '<label for="$0"></label>' },
	{ input : 'legend', output : '<legend>\n\t$0\n</legend>' },
	{ input : 'link', output : '<link rel="stylesheet" href="$0" type="text/css" media="screen" charset="utf-8" />' },		
	{ input : 'base', output : '<base href="$0" />' }, 
	{ input : 'body', output : '<body>\n\t$0\n</body>' }, 
	{ input : 'css', output : '<link rel="stylesheet" href="$0" type="text/css" media="screen" charset="utf-8" />' },
	{ input : 'div', output : '<div>\n\t$0\n</div>' },
	{ input : 'divid', output : '<div id="$0">\n\t\n</div>' },
	{ input : 'dl', output : '<dl>\n\t<dt>\n\t\t$0\n\t</dt>\n\t<dd></dd>\n</dl>' },
	{ input : 'fieldset', output : '<fieldset>\n\t$0\n</fieldset>' },
	{ input : 'form', output : '<form action="$0" method="" name="">\n\t\n</form>' },
	{ input : 'meta', output : '<meta name="$0" content="" />' },
	{ input : 'p', output : '<p>$0</p>' },
	{ input : 'script', output : '<script type="text/javascript" language="javascript" charset="utf-8">\n\t$0\t\n</script>' },
	{ input : 'scriptsrc', output : '<script src="$0" type="text/javascript" language="javascript" charset="utf-8"></script>' },
	{ input : 'span', output : '<span>$0</span>' },
	{ input : 'table', output : '<table border="$0" cellspacing="" cellpadding="">\n\t<tr><th></th></tr>\n\t<tr><td></td></tr>\n</table>' },
	{ input : 'style', output : '<style type="text/css" media="screen">\n\t$0\n</style>' }
]
	
Language.complete = [
	{ input : '\'',output : '\'$0\'' },
	{ input : '"', output : '"$0"' },
	{ input : '(', output : '\($0\)' },
	{ input : '[', output : '\[$0\]' },
	{ input : '{', output : '{\n\t$0\n}' }		
]

Language.shortcuts = []
