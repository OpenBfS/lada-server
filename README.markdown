Lada-Server
===========
Die Software bietet Funktionalität zur Erfassung und Bearbeitung
von Messdaten. Sowie der Planung der Messungen.

Weitere Informationen finden sich auf der Projektwebseite unter
der Adresse: https://wald.intevation.org/projects/lada/

Die Software entstand im Rahmen einer Software Entwicklung durch die
Intevation GmbH im Auftrag des Bundesamt für Strahlenschutz in den Jahren 2013
bis 2015.

Kontakt
-------
Bundesamt für Strahlenschutz
SW2 Notfallschutz, Zentralstelle des Bundes (ZdB)
Willy-Brandt-Strasse 5
38226 Salzgitter
info@bfs.de

Lizenz
------
Die Software ist unter der GNU GPL v>=3 Lizenz verfügbar.
Details siehe die Datei `COPYING`.

Quelltext
---------
Die Quelldateien lassen sich wie folgt auschecken:
```
git clone https://github.com/OpenBfS/lada-server.git
```

Entwicklung
-----------
Für die Entwicklung wird ein JDK7 und maven3 oder höher benötigt. Sämtliche
Abhängigkeiten werden von dem maven build System aufgelöst.

Installation
------------
Die Installation des Lada-Servers erfolgt in einem Wildfly-Application-Server
(http://wildfly.org). Dazu müssen folgende Schritte unternommen werden:

 $ mvn clean compile package
 $ mv target/lada-server-$VERSION.war $JBOSS_HOME/standalone/deployments
 $ touch $JBOSS_HOME/standalone/deployments/lada-server-$VERSION.war.dodeploy

$JBOSS_HOME ist hierbei durch den Pfad zur Wildfly-Installation zu ersetzen,
$VERSION durch die aktuelle Versionsbezeichnung (entsprechend der Angabe in
pom.xml).

Zum Aktualisieren der Anwendung genügt es, das WAR-Archiv zu aktualisieren.

Die Anwendung ist dann unter dem Pfad "/lada-server-$VERSION" erreichbar.

Um zu garantieren, dass die von den REST-Schnittstellen ausgelieferten
Zeitstempel sich korrekt auf UTC beziehen, muss die entsprechende System-
Property `user.timezone=UTC` vor dem Start des Application-Servers gesetzt
werden (siehe `wildfly/standalone.conf`).

Das PostgreSQL-Datenbank-Backend des Lada-Servers kann als Nutzer `postgres`
(bzw. als PostgreSQL-Superuser) mit dem Skript `db_schema/setup-db.sh`
eingerichtet werden.

Details zur Installation können den Dateien `Dockerfile` und
`db_schema/Dockerfile` entnommen werden.

### Transformation von Ortskoordinaten

Die Transformation von Koordinaten aus dem CRS `EPSG:3146[6,7,8,9]` in das für intern
genutzte Geometrien CRS `EPSG:4326` kann optional mit einem ShiftGrid erfolgen.
Dies erhöht die Genauigkeit der resultierenden Koordinaten.
Hierfür wurde unter src/main/resources/org/geotools/referencing/factory/gridshift 
das ShiftGrid BETA2007.gsb eingefügt.
Quelle ist http://crs.bkg.bund.de/crseu/crs/descrtrans/BeTA/BETA2007.gsb 
(Siehe auch http://crs.bkg.bund.de/crseu/crs/descrtrans/BeTA/de_dhdn2etrs_beta.php)

Docker
------
Um schnell und automatisiert ein Entwicklungs-Setup für LADA aufsetzen zu
können, werden Dockerfiles mitgeliefert. Voraussetzung für die Anwendung ist
eine Docker-Installation. Folgendes Vorgehen führt zu einem
Vollständigen Setup inklusive LADA-Client, in dem jeweils der auf dem Host
vorhandene Quellcode in die Container gemounted wird, so dass auf dem Host
durchgeführte Änderungen leicht innerhalb der Container getestet werden können.

Bauen der Images:
 $ docker build -f db_schema/Dockerfile -t koala/lada_db db_schema
 $ docker build -f shibboleth/Dockerfile -t koala/lada_idp shibboleth
 $ docker build -t koala/lada_wildfly .
 $ cd your/repo/of/lada-client
 $ docker build -t koala/lada_client .

Oder für ein Client-Image mit Shibboleth-Unterstützung:

$ docker build -f Dockerfile.shibboleth -t koala/lada_client .

Aufbau eines Netzwerks für die LADA-Komponenten:
 $ docker network create lada_network

Starten der Container:
 $ docker run --name lada_db --net=lada_network \
          -v $PWD/db_schema:/opt/lada_sql/ \
          -d koala/lada_db:latest
 $ docker run --name lada-idp --net=lada_network \
             -p 20080:80 -p 28080:8080 -p 20443:443 -p 28443:8443 \
             -v $PWD/shibboleth:/usr/local/lada_shib/sources \
             -d koala/lada_idp
 $ docker run --name lada-server --net=lada_network \
          -v $PWD:/usr/src/lada-server \
          -d koala/lada_wildfly
 $ cd your/repo/of/lada-client
 $ docker run --name lada-client --net=lada_network \
              -v $PWD:/usr/local/lada \
              -p 8180-8185:80-85 -d koala/lada_client

Innerhalb des Client-Containers muss dann noch folgendes ausgeführt werden,
wenn zum ersten mal your/repo/of/lada-client als Volume in einen Container
eingebunden wurde:

 $ ./install-sencha2opt.sh
 $ ./install-dependencies.sh
 $ ./docker-build-app.sh

Die LADA-Anwendung kann dann unter den angegebenen Ports mit verschiedenen
Rollen im Browser ausgeführt werden. Die Ports 8180 - 8184 verwenden dabei
festgelegte Benutzer/Rollen, während der Client unter Port 8185 eine
Authentifizierung mit Shibboleth verwendet.
Um Shibboleth zu verwenden muss vorher noch die Erreichbarkeit des IDP-Dienstes
sichergestellt werden. In der Standardkonfiguration müssen alle Dienste auf der
selben Maschine laufen auf der auch der Client-Browser gestartet wird.
Ist dies nicht der Fall muss die Adresse des IDPs angepasst werden. Dazu können
in den Dateien {Server-Repository}/shibboleth/idp-metadata.xml und
{Client-Repository}/shibboleth/partner-metadata.xml die Attribute
Location="https://localhost:28443/..." zu einer Adresse verändert werden, die
vom Client-System erreichbar ist, etwa die lokale IP-Adresse des IDP-Systems.
Alternativ können auch die LADA-Anwendung und der IDP auf dem lokalen Rechner
mittels Port-Forwarding erreichbar gemacht werden, z.B.:

 $ ssh -L28443:docker-host:28443 -L8185:docker-host:8185 remote-host

Die Shibboleth-authentifizierte Anwendung ist dann unter
"http://localhost:8185" im lokalen Browser erreichbar.

Tests und Debugging
-------------------
Die auf Arquillian basierenden Tests erfordern einen vollständig konfigurierten
und gestarteten Wildfly Application-Server, da für die Schnittstellentest eine
Clientanwendung simuliert wird und HTTP-Requests ausgeführt werden.

Das Ausführen der Tests erfolgt durch das Kommando

 $ mvn -Premote-test clean test

und benötigt eine leere Datenbank mit dem Namen "lada_test", die z.B. mit

 $ ./db_schema/setup-db.sh -cn lada_test

angelegt werden kann.

Um die Tests selber mit einem Remote-Debugger (z.B. JDB) zu debuggen, die
Tests folgendermaßen ausführen:

 $ mvn -Dmaven.surefire.debug="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005" -P remote-test clean test

Sobald die Ausgabe "[INFO] Listening for transport dt_socket at address: 5005"
erscheint, kann die weitere Ausführung der Tests per mit dem genannten Port
verbundenem Debugger gesteuert werden.

Soll der durch die Schnittstellentest im LADA-Server ausgeführte
Code per Remote-Debugger erreicht werden, zunächst falls vorhanden das
Deployment des LADA-Servers entfernen

 $ rm $JBOSS_HOME/standalone/deployments/lada-server.war.deployed && \
   until ls $JBOSS_HOME/standalone/deployments/lada-server.war.undeployed
   do sleep 1
   done

und Folgendes in wildfly/standalone.conf einfügen:

 JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,address=*:8787,server=y,suspend=n"

Den Wildfly neu starten. Dann einen Debugger mit Port 8787 verbinden um z.B.
einen Breakpoint zu setzen und anschließend die Tests starten.

Natürlich kann man mit der genannten Einstellung in wildfly/standalone.conf
auch den LADA-Server ohne die automatischen Tests deployen und debuggen. Auch
hier vorher sicherstellen, dass nur ein Deployment vorhanden ist, und den
Wildfly neu starten.

Dokumenation
------------
Die Entwicklerdokumentation (Javadoc) kann mit dem folgenden Befehl im
Verzeichnis der Serveranwendung erzeugt werden:

 $ mvn javadoc:javadoc

Der Ordner 'target' enthält dann die Dokumentation im HTML-Format in dem
Verzeichnis 'site/apidocs'.

Erstellen von Queries
---------------------

Basequeries enthalten die grundlegenden Definitionen für Abfragen. Diese werden
fest in der Datenbank vorgegeben und sind in der Tabelle stamm.base_query
definiert. Die SQL-Abfrage in der Tabelle muss zumindest das SELECT- und
FROM-Statement enthalten. Den Ergebnisspalten der Abfrage sollte zudem
mithilfe des AS-Ausdrucks ein Alias zugewiesen werden.
Der Spaltenname 'extjs_id' wird intern vom Client genutzt und sollte nicht
vergeben werden.

Der Basequery zugeordnete Spalten werden zusätzlich in der Tabelle
stamm.grid_column festgelegt, wobei der gegebene DataIndex einem Alias der
zugeordneten Basequery entsprechen sollte. Der Datentyp data_type bestimmt
das Verhalten des Clients und den dort angezeigten Filterwidgets mit (siehe
unten). Die Position gibt die Stellung innerhalb der Basequery an, name ist die
im Ergebnisgrid anzuzeigende Spaltenbeschriftung.

Die Spalte filter innerhalb einer stamm.grid_column verweist auf einen Eintrag
in der Tabelle stamm.filter. Diese enthält Filter-Typ, das entsprechende
SQL-Statement und den Namen des Parameters.
Neben einfachen Text-, Zahlen- oder boolschen- Filtern existieren auch
Filter-Typen für von-bis-Datums-Filter, Multiselect-Filter und generische
Text-Filter. Multiselect- und Datums-Filter akzeptieren dabei einen String mit
Komma-separierten Werten.
Für die Definition der Filter mit SQL-Statement und Paramter gilt:
  * Datums-Filter: 2 Parameter. Beispielsweise:
    * SQL: probe.probeentnahme_beginn BETWEEN :fromTime AND :toTime
    * Spalte "Parameter": fromTime,toTime
  * Generischer Filter: 1 Parameter. Beispielsweise:
    * SQL: :genTextParam LIKE :genTextValue
    * Spalte "Parameter": genText
  * Sonst: 1 Parameter. Beispielsweise:
    * SQL: probe.id_alt LIKE :idAlt
    * Spalte "Parameter": idAlt

Einzelne Nutzer können aus bereits bestehenden Queries Kopien erstellen.
Hierfür gibt es zwei Speicherorte: In query_user werden die grundsätzlichen
Parameter festgelegt, wie etwa eine eigene Beschreibung oder ein eigener Namen
der kopierten Query.
In grid_column_values werden die Definitionen der
einzelnen Spalten (z.B. Sichtbarkeit, derzeitig gespeicherter Filter)
persistiert.

### Datentypen

Den einzelnen Spalten können verschiedene Datentypen zugeordnet werden. Dies
dient im Client zum einen der Darstellung von passenden Filtern und Spalten.
Einige Datentypen bieten zusätzliche Funktionalität.

Der Typ eines Ergebnisgrids leitet sich ebenfalls aus Datentypen in der
Basequery ab, welche Datenbankeinträge eindeutig identifizieren.
Hierbei existiert eine Hierarchie: weiter oben stehende Elemente ersetzen
das weiter unten stehende.

Datentypen mit ID-Funktionalität, in absteigender Hierarchie:
  1. 'messungId' - Zeile enthält eine Messung
  2. 'probeId' - Zeile enthält eine Probe
  3. 'mpId' - Zeile enthält ein Messprogramm
  4. 'ortId' - Zeile enthält einen (Stammdaten-)Ort
  5. 'pnehmer' - Zeile enthält einen Probenehmer
  6. 'dsatzerz'- Zeile enthält einen Datensatzerzeuger
  7. 'mprkat'- Zeile enthält eine Messprogrammkategorie
  8. 'id' - Zeile enthält beliebige Daten, die an Hand dieser Spalte
            identifiziert werden können (z.B. für den Export einzeln
            selektierter Zeilen).

Diese Datentypen sollten jeweils eine entsprechende Datenbank-ID enthalten,
bzw. im Fall von 'id' einen eindeutigen Wert (Zahl oder Text). Mehrere IDs von
verschiedenen Typen sind zulässig, und sind dann im Grid direkt auswähl- und
gegebenenfalls in eigenen Dialogen bearbeitbar.

Das Verhalten der 'Hinzufügen/Löschen' - Buttons und des Doppelklicks auf eine
Zeile richtet sich jeweils nach dem Datentyp mit der höchsten Hierarchiestufe.
(Beispiel: In einer Abfrage mit messungId, pnehmer und probeId können -bei
Berechtigung- alle drei Elemente bearbeitet werden. Die höchste Hierarchieebene
ist hier Messung, weshalb ein "Löschen" für die entsprechende Messung einer
Zeile gilt. Der 'Hinzufügen'- Button ist nicht verfügbar, da eine Messung nur
aus dem Kontext einer Probe hinzugefügt werden kann, und Proben in diesem Grid
nicht eindeutig sind).

Die eindeutige Identifizierung von Datensätzen für den Export richtet sich
ebenfalls nach dem Datentyp mit der höchsten Hierarchiestufe: Der Inhalt der
entsprechenden Spalte wird als eindeutig angenommen und dem Server zur
Identifikation der Datensätze übermittelt. Es muss also darauf geachtet werden,
dass in der Konfiguration einer Query die Verwendung des Datentyps mit der
höchsten Hierarchiestufe tatsächlich einer Spalte mit eindeutigen Werten
entspricht.

Um neue Queries für die Suche von Proben, Messungen und Messprogrammen zu
erstellen sind die folgenden Schritte erforderlich:

### Sonderfälle in Datentypen

Für einige in stamm.column definerten möglichen Datentypen erwartet der
Client spezielle Angaben:

* Resultate mit Geometrien (Typ 'geom') werden als GeoJSON erwartet. Hierfür
  kann die postgis- Funktion 'st_asgeojson' genutzt werden:
```
  'SELECT ST_ASGEOJSON(geom) AS geometrie FROM stamm.ort;'
```

* Resultate für Zahlen können in E-Notation erzwungen werden, wenn im Tabelle
  stamm.result_type das Format auf 'e' gesetzt wird.


Erstellen von Importerkonfigurationen
-------------------------------------

Konfigurationen für den Importer enthalten drei Typen von Aktionen, die auf die
zu importierenden Daten angewendet werden, bevor die Daten in die Datenbank
geschrieben werden:
1. "default": Standardwerte, die leere oder fehlende Angaben ergänzen
2. "convert": Datenumwandlungen, die einen Ersatz von vorhandenen Daten
   darstellen
3. "transform": Zeichenumwandlung, die einzelne Zeichen eines Wertes ändern

Eine Konfiguration wird in der Datenbanktabelle 'importer_config' im Schema
"stammdaten" angelegt und hat die folgenden Felder:

* id (serial): Primary Key
* name (character varying(30)): Name der Datenbank-Tabelle,
  z.B. bei einer Probe "probe". Die Zeitbasis hat den Namen "zeitbasis".
* attribute (character varying(30)): Name des Attributes das bearbeitet werden
  soll in CamelCase-Schreibweise. (Zeitbasis hat hier einen "dummy"-Eintrag)
  Tabellenspalten, die als Foreign-Key auf andere Tabellen verweisen, werden
  mit dem Tabellennamen referenziert und können so im Falle der Aktion
  'convert' mit den sprechenden Bezeichnung genutzt werden.
* mst_id (Foreign-Key auf mess_stelle): Enthält die Messstelle, für die diese
  Konfiguration gültig ist.
* from_value (character varying(100)): Für "default" bleibt diese Spalte leer,
  für "convert" und "transform" enthält diese Spalte den Ursprungswert.
* to_value (character varying(100)): Enthält den Zielwert der Konfiguration
* action (character varying(20)): Enthält eine der drei Aktionen als Text:
  "default", "convert" oder "transform"

Die Transformation im speziellen enthält in "from_value" und "to_value" die
hexadezimale Darstellung eines Zeichen in Unicode. Also z.B. für "+" den
Wert "2b", für "#" den Wert "23".
