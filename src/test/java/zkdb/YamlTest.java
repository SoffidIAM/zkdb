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
				"		  - value: lastLogin\n" + 
				"		  - value: passwordExpiration\n" + 
				"";
		s = new Yaml2Json().transform(s);
		System.out.println(s);
	}

}
