Anwendung: JListImageListViewTestApp - MultiListFrame

8-jimagelistviews-1x1: 8 JGLImageListViews mit je Scalemode 1x1, und
ein Controller, der WindowLocation/-Width zwischen ihnen synchronisiert.
Anwendung starten, Profiler starten, mit rechter Maustaste in einer Liste
Fensterparameter veraendern. Aenderung wird auf die anderen Listen
synchronisiert.


1-jimagelistviews-3x3: 1 JGLImageListViews mit Scalemode 3x3, und
ein Controller, der WindowLocation/-Width zwischen den Zellen
synchronisiert.
Anwendung starten, Profiler starten, mit rechter Maustaste in einer Zelle
Fensterparameter veraendern. Aenderung wird auf die anderen Zellen
synchronisiert.


Aufwand bzgl. Neuzeichnen sollte in beiden Faellen etwa derselbe sein.

Die Profiler-Results sehen auch aehnlich aus, d.h. die in
GL-/Zeichenroutinen verbrachte CPU-Zeit ist aehnlich.

ABER: 8-jimagelistviews-1x1 ist wesentlich langsamer; man sieht schon
visuell eine leichte Verzoegerung beim Synchronisieren (noch nicht
gravierend, aber merklich). Bei 1-jimagelistviews-3x3 hingegen (nur
1 GLCanvas, Synchronisierung nur dort drin ohne Swing/AWT-Beteiligung)
ist es rasend schnell; keine sichtbare Verzoegerung. Aus die verbrauchte
Realzeit (Wallclock-Zeit) ist bei 8-jimagelistviews-1x1 bestimmt 10-mal
groesser, bis eine vergleichbare Zahl von Windowing-Parameter-set-Aufrufen
durchgelaufen ist.
