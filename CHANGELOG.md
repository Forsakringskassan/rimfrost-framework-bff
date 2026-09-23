# rimfrost-framework-bff changelog

Changelog of rimfrost-framework-bff.

## 0.0.1 (2026-09-23)

### Features

-  publicera test-jar med återanvändbara WireMock-hjälpklasser (FKPOC-1071) ([3e1d9](https://github.com/Forsakringskassan/rimfrost-framework-bff/commit/3e1d930eb5993b7) LisaWedin_Ductus)  
-  lägg till try-with-resources-hjälpklass för MDC-loggkontext (FKPOC-1070) ([bb0db](https://github.com/Forsakringskassan/rimfrost-framework-bff/commit/bb0db5703227fdd) LisaWedin_Ductus)  
-  vidarebefordra Authorization-header automatiskt till utgående anrop (FKPOC-1069) ([d1291](https://github.com/Forsakringskassan/rimfrost-framework-bff/commit/d1291970e558bab) LisaWedin_Ductus)  
-  generalisera hälsokontroll mot bakomliggande tjänst (FKPOC-1068) ([aa7ac](https://github.com/Forsakringskassan/rimfrost-framework-bff/commit/aa7aced4eeaf156) LisaWedin_Ductus)  
-  identifiera maskerat nätverksfel i exception-grafen (FKPOC-1067) ([f9458](https://github.com/Forsakringskassan/rimfrost-framework-bff/commit/f94582ada6520bc) LisaWedin_Ductus)  
-  hantera nätverksfel mot bakomliggande tjänst (FKPOC-1066) ([f528d](https://github.com/Forsakringskassan/rimfrost-framework-bff/commit/f528de84179bc76) LisaWedin_Ductus)  
-  lägg till global exception mapper för uppströmsfel och oväntade fel (FKPOC-1065) ([3fefd](https://github.com/Forsakringskassan/rimfrost-framework-bff/commit/3fefd0d0a2b6b33) LisaWedin_Ductus)  
-  lägg till typad ErrorResponse-record (FKPOC-1064) ([db159](https://github.com/Forsakringskassan/rimfrost-framework-bff/commit/db159a63976f1d1) LisaWedin_Ductus)  
-  koppla in återanvändbara CI/release-workflows ([4fce7](https://github.com/Forsakringskassan/rimfrost-framework-bff/commit/4fce795478dccd8) LisaWedin_Ductus)  
-  add repo grundstruktur for rimfrost-framework-bff ([3dfeb](https://github.com/Forsakringskassan/rimfrost-framework-bff/commit/3dfeb7e3ad36cd3) LisaWedin_Ductus)  

### Bug Fixes

-  nyckla WireMock-servrar per subklass och mjuka upp överdriven kommentar (FKPOC-1071) ([74624](https://github.com/Forsakringskassan/rimfrost-framework-bff/commit/7462420db3c7df9) LisaWedin_Ductus)  
-  applies spotless ([01d58](https://github.com/Forsakringskassan/rimfrost-framework-bff/commit/01d584a27e65021) LisaWedin_Ductus)  
-  LogContext.close() återställer föregående MDC-värde istället för att alltid ta bort nyckeln ([78550](https://github.com/Forsakringskassan/rimfrost-framework-bff/commit/78550c9a3645b19) LisaWedin_Ductus)  
-  byt ut deprecated new URL(String) mot URI.create(...).toURL() i UpstreamHealthCheck ([de3eb](https://github.com/Forsakringskassan/rimfrost-framework-bff/commit/de3eb85552f1dbe) LisaWedin_Ductus)  
-  skydda mot att en redan satt Authorization-header skrivs över och logga vid inaktivt request scope ([aa679](https://github.com/Forsakringskassan/rimfrost-framework-bff/commit/aa67908d430f872) LisaWedin_Ductus)  
-  lägg till beans.xml så GlobalExceptionMapper indexeras av konsumenter ([33bc5](https://github.com/Forsakringskassan/rimfrost-framework-bff/commit/33bc5673d3ff262) LisaWedin_Ductus)  
-  applies spotless ([42dab](https://github.com/Forsakringskassan/rimfrost-framework-bff/commit/42dab4dbdb7369c) LisaWedin_Ductus)  
-  lägg till maven wrapper (mvnw) ([980ac](https://github.com/Forsakringskassan/rimfrost-framework-bff/commit/980ac736f60598b) LisaWedin_Ductus)  
-  adds ticket file to gitignore ([da46d](https://github.com/Forsakringskassan/rimfrost-framework-bff/commit/da46dacafe9658f) LisaWedin_Ductus)  

### Other changes

**Update .mvn/wrapper/maven-wrapper.properties**

* Co-authored-by: larpersso &lt;254442132+larpersso@users.noreply.github.com&gt; 

[e0355](https://github.com/Forsakringskassan/rimfrost-framework-bff/commit/e03555a5f4e9be5) LisaWedin-Ductus *2026-09-21 11:43:47*

**Initial commit**


[eca09](https://github.com/Forsakringskassan/rimfrost-framework-bff/commit/eca09ba2ce470e1) LisaWedin_Ductus *2026-09-21 06:32:11*


