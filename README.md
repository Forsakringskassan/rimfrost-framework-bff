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

## Hälsokontroll mot bakomliggande tjänst

`UpstreamHealthCheck` är inte själv en CDI-upptäckt `@Readiness`-böna (den kan inte, eftersom
tjänstenamn, URL och timeout skiljer sig per bakomliggande tjänst). Varje konsumerande BFF
registrerar istället en instans per bakomliggande tjänst via en `@Produces`-metod:

```java
@ApplicationScoped
public class HealthCheckProducer
{
   @Readiness
   @Produces
   HealthCheck oulHealthCheck()
   {
      return UpstreamHealthCheck.fromConfig("oul-backend", "quarkus.rest-client.oul.url", 2000);
   }
}
```

Fler bakomliggande tjänster registreras genom att lägga till ytterligare en `@Produces`-metod
med eget tjänstenamn, config-property-nyckel och timeout.

## Vidarebefordran av Authorization-header

`IncomingAuthorizationHeaderFilter` och `OutgoingAuthorizationHeaderFilter` är globalt
registrerade JAX-RS-providers (`@Provider`) och kräver ingen egen registrering i konsumerande
BFF:er — så fort paketet finns på classpath vidarebefordras en inkommande `Authorization`-header
automatiskt till alla utgående REST-klientanrop, utan att endpoints eller klientgränssnitt
behöver deklarera `@HeaderParam("Authorization")`. Saknas headern i den inkommande förfrågan
skickas ingen tom eller påhittad header vidare. Headerns innehåll tolkas, verifieras eller
lagras aldrig av ramverket.

## Test-JAR

Paketet publicerar en test-JAR (`maven-jar-plugin`s `test-jar`-goal) med återanvändbara
WireMock-baserade hjälpklasser, en per komponent, som konsumerande BFF:er kan använda i sina
egna integrationstester istället för att bygga upp mockningen själva:

| Klass                            | Täcker                                                        |
|-----------------------------------|----------------------------------------------------------------|
| `errorhandling.UpstreamErrorWireMock` | Felscenarierna `GlobalExceptionMapper` hanterar (upstreamstatus, connection reset, maskerat nätverksfel) |
| `health.HealthCheckWireMock`      | Upp/ned/timeout-scenarier för `UpstreamHealthCheck`            |
| `auth.AuthorizationHeaderWireMock` | Verifiering av vilken `Authorization`-header ett utgående anrop faktiskt skickade |

Lägg till beroendet med `<classifier>tests</classifier>`:

```xml
<dependency>
  <groupId>se.fk.rimfrost.framework.bff</groupId>
  <artifactId>rimfrost-framework-bff</artifactId>
  <version>...</version>
  <classifier>tests</classifier>
  <scope>test</scope>
</dependency>
```

Ärv en hjälpklass, implementera `wiremockMapping(WireMockServer)` med den config-property-nyckel
som ska peka mot WireMock-servern, och registrera med `@QuarkusTestResource`. Servern lagras per
konkret subklass, inte i ett delat statiskt fält, så en BFF med flera bakomliggande tjänster kan
registrera flera subklasser sida vid sida och stubba/läsa dem oberoende av varandra — ange
subklassen som första argument till hjälpklassens statiska metoder:

```java
class OulWireMock extends HealthCheckWireMock
{
   @Override
   protected Map<String, String> wiremockMapping(WireMockServer server)
   {
      return Map.of("quarkus.rest-client.oul.url", server.baseUrl());
   }
}

@QuarkusTest
@QuarkusTestResource(OulWireMock.class)
class OulHealthCheckIT
{
   @Test
   void upstreamDown_healthCheckReportsDown()
   {
      HealthCheckWireMock.stubDown(OulWireMock.class, 503);
      // ... anropa hälsokontrollen och verifiera
   }
}
```
