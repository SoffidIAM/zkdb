/*
 * CodePress regular expressions for HTML syntax highlighting
 */

// HTML
Language.syntax = [ //Mazinger
	{ input : /(&lt;[^!]*?&gt;)/g, output : '<b>$1</b>'	}, // all tags
	{ input : /(&lt;style.*?&gt;)(.*?)(&lt;\/style&gt;)/g, output : '<em>$1</em><em>$2</em><em>$3</em>' }, // style tags
	{ input : /(&lt;zscript.*?&gt;)(.*?)(&lt;\/zscript&gt;)/g, output : '<strong>$1</strong><tt>$2</tt><strong>$3</strong>' }, // script tags
	{ input : /=(".*?")/g, output : '=<s>$1</s>' }, // atributes double quote
	{ input : /=('.*?')/g, output : '=<s>$1</s>' }, // atributes single quote
	{ input : /(&lt;!--.*?--&gt.)/g, output : '<ins>$1</ins>' }, // comments 
	{ input : /\b(abstract|continue|for|new|switch|assert|default|goto|package|synchronized|boolean|do|if|private|this|break|double|implements|protected|throw|byte|else|import|public|throws|case|enum|instanceof|return|transient|catch|extends|int|short|try|char|final|interface|static|void|class|finally|long|strictfp|volatile|const|float|native|super|while)\b/g, output : '<i>$1</i>'}, // reserved words
	{ input : /([^:]|^)\/\/(.*?)(<br|<\/P)/g, output : '$1<i>//$2</i>$3'}, // comments //	
	{ input : /\/\*(.*?)\*\//g, output : '<i>/*$1*/</i>' }, // comments /* */
	{ input : /(&lt;Mazinger.*?&gt;|&lt;\/Mazinger.*?&gt;)/g, output : '<b class="mazinger">$1</b>'}, //Mazinger
	{ input : /(&lt;WebApplication.*?&gt;|&lt;\/WebApplication.*?&gt;)/g,output : '<b class="webapplication">$1</b>' }, //WebApplication
	{ input : /(&lt;DomainPassword.*?&gt;|&lt;\/DomainPassword.*?&gt;)/g, output : '<b class="domainpassword">$1</b>'}, //DomainPassword
	{ input : /(&lt;Application.*?&gt;|&lt;\/Application.*?&gt;)/g, output : '<b class="application">$1</b>'}, //Application
	{ input : /(&lt;Component.*?&gt;|&lt;\/Component.*?&gt;)/g,output : '<b class="component">$1</b>' }, //Component
	{ input : /(&lt;Input.*?&gt;|&lt;\/Input.*?&gt;)/g,output : '<b class="input">$1</b>' }, //Input
	{ input : /(&lt;Form.*?&gt;|&lt;\/Form.*?&gt;)/g,output : '<b class="form">$1</b>' }, //Form	
	{ input : /(&lt;Action.*?&gt;|&lt;\/Action.*?&gt;)/g, output : '<b class="action">$1</b>'}, //Action
	{ input : /(&lt;!\[CDATA\[.*?&gt;|]].*?&gt;)/g,output : '<b class="cdata">$1</b>' } //CDATA	
]
	
// Se activan con el tabulador tras la expresión
Language.snippets = [
	{ input : 'maz', output : '<Mazinger>\n$0\n</Mazinger>' },
	{ input : 'webapp', output : '   <WebApplication url="$0" title="" content="">\n      <Action type="setText|type|script|click" event="" repeat="true|false" delay="0" text="">\n      </Action>\n   </WebApplication>' },
	{ input : 'dom', output : '   <DomainPassword domain="$0" servers="" userSecret="" passwordSecret="">\n   </DomainPassword>' },
	{ input : 'app', output : '   <Application cmdLine="$0">\n      <Component ref-as="" class="" name="" text="" title="" check="partial|full" dlgId="" optional="true|false">\n      </Component>\n   </Application>' },
	{ input : 'com', output : '<Component ref-as="$0" class="" name="" text="" title="" check="partial|full" dlgId="" optional="true|false" >\n</Component>' },
	{ input : 'cdata', output : '<![CDATA[\n$0]]>' },	
	{ input : 'input', output : '<Input name="$0" ref-as="" optional="true|false" type="" value="" id="" /> ' },
	{ input : 't', output : '   $0' }, //3 espais
	{ input : 'form', output : '<Form action="$0" name="" method=""  ref-as="" optional="true|false" id="">\n</Form> ' },
	{ input : 'act', output : '      <Action type="setText|type|script|click" event="" repeat="true|false" delay="0" text="">\n      $0</Action>' }
]
// autocompletado	
Language.complete = [
	{ input : '\'',output : '\'$0\'' },
	{ input : '"', output : '"$0"' },
	{ input : '(', output : '\($0\)' },
	{ input : '{', output : '{$0}' }
]
// se activan con ctrl+shift y la combinación
Language.shortcuts = []
