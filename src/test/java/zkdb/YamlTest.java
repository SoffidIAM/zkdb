package zkdb;

import java.io.IOException;

import es.caib.zkdb.yaml.Yaml2Json;

public class YamlTest {
	public static void main(String args[]) throws IOException {
		String s = 
				"		- path: /domini/politica\n" + 
				"		  value: ../name\n" + 
				"		  columns:\n" + 
				"		  - template: <img src=\"/img/pwd.svg\" title=\"${c:l('accounts.setPassword.title') }\"></img>\n" + 
				"		- path: /account\n" + 
				"		  columns:\n" + 
				"		  - value: lastUpdated\n" + 
				"		  - value: >-\n" + 
				"		      Very long\n" + 
				"		      expression\n" + 
				"		  - value: lastLogin\n" + 
				"		  - value: passwordExpiration\n" + 
				"";
		s = new Yaml2Json().transform(s);
		System.out.println(s);
		
		s =     "		- path: /effectiveRole\n" + 
				"		  template: >-\n" + 
				"		     #{accountName != null ? c:cat4(\"${c:l('auditoria.zul.account')} \", accountName,\" @ \",system): \"\"}\n" + 
				"		     #{groupName != null ? c:cat(\"${c:l('auditoria.zul.group')} \", groupName): \"\"}\n" + 
				"		     #{ruleName != null ? c:cat(\"${c:l('com.soffid.iam.api.Audit.rule')} \", ruleName): \"\"}\n" + 
				"		     #{roleName != null ? c:cat4(\"${c:l('com.soffid.iam.api.Audit.role')} \", roleName, \" @ \",system): \"\"}\n" + 
				"		  columns:\n" + 
				"		  - >-\n" + 
				"		     #{accountName != null ? accountDescription: \"\"}\n" + 
				"		     #{groupName != null ? groupDescription: \"\"}\n" + 
				"		     #{ruleName != null ? \"\": \"\"}\n" + 
				"		     #{roleName != null ? roleDescription: \"\"}\n" + 
				"		- path: /nested\n" + 
				"		  template: >-\n" + 
				"		     #{accountName != null ? c:cat4(\"${c:l('auditoria.zul.account')} \", accountName,\" @ \",system): \"\"}\n" + 
				"		     #{groupName != null ? c:cat(\"${c:l('auditoria.zul.group')} \", groupName): \"\"}\n" + 
				"		     #{ruleName != null ? c:cat(\"${c:l('com.soffid.iam.api.Audit.rule')} \", ruleName): \"\"}\n" + 
				"		     #{roleName != null ? c:cat4(\"${c:l('com.soffid.iam.api.Audit.role')} \", roleName, \" @ \",system): \"\"}\n" + 
				"		  columns:\n" + 
				"		  - >-\n" + 
				"		     #{accountName != null ? accountDescription: \"\"}\n" + 
				"		     #{groupName != null ? groupDescription: \"\"}\n" + 
				"		     #{ruleName != null ? \"\": \"\"}\n" + 
				"		     #{roleName != null ? roleDescription: \"\"}\n"; 
				
		s = new Yaml2Json().transform(s);
		System.out.println(s);
	}

}
