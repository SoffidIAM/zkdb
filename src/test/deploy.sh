#!/bin/bash
mkdir -p $HOME/soffid-2.0/test//apache-tomee-plus-7.1.1/webapps/zk/WEB-INF/classes
mkdir -p $HOME/soffid-2.0/test//apache-tomee-plus-7.1.1/webapps/zk/WEB-INF/lib
cp -r src/test/webapp/* $HOME/soffid-2.0/test/apache-tomee-plus-7.1.1/webapps/zk
cp -r target/classes/* $HOME/soffid-2.0/test/apache-tomee-plus-7.1.1/webapps/zk/WEB-INF/classes
cp -ru target/dependency/* $HOME/soffid-2.0/test/apache-tomee-plus-7.1.1/webapps/zk/WEB-INF/lib

