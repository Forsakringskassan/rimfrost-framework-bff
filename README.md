# rimfrost-framework-bff

Delat ramverksbibliotek med gemensam infrastruktur för Rimfrosts BFF:er (Backend-for-Frontend).
Ramverket samlar sådant som idag är duplicerat mellan BFF:erna — enhetlig felhantering,
hälsokontroller mot bakomliggande tjänster, vidarebefordran av `Authorization`-headern och
loggkontext — så att varje BFF kan ärva gemensam, beprövad infrastruktur istället för att
underhålla egna kopior.

Paketet är ett bibliotek, inte en körbar tjänst, och paketeras som ett vanligt jar på samma
Quarkus-version och parent-pom (`fk-maven-quarkus-parent`) som konsumerande BFF:er.

## Struktur

```
se.fk.rimfrost.framework.bff/
├── errorhandling/   # Typad felresponsmodell och global exception-mappning
├── health/          # Generisk hälsokontroll mot bakomliggande tjänster
├── auth/            # Vidarebefordran av inkommande Authorization-header
└── logging/         # Loggkontext-hjälpklasser (MDC)
```
