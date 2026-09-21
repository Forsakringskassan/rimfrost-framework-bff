# Krav — rimfrost-framework-bff

## Funktionella krav

### FBFF-FR-01 — Enhetlig felhantering

- **FBFF-FR-01.1** Ramverket ska tillhandahålla en global exception mapper som fångar
  `WebApplicationException`, `ProcessingException` (transportfel från MicroProfore REST Client)
  och övriga oväntade exceptions, och mappar dem till konsekventa HTTP-statuskoder.
- **FBFF-FR-01.2** Vid `ProcessingException` eller annat nätverksfel mot en bakomliggande tjänst
  ska ramverket returnera HTTP 502 och tydligt ange att uppströmstjänsten är otillgänglig.
- **FBFF-FR-01.3** Nätverksfel som maskeras som ett annat exception (t.ex. en `IOException` som
  hamnar som suppressed exception bakom ett NPE i loggningsfiltret vid connection-reset) ska
  ändå identifieras och behandlas som ett nätverksfel enligt FBFF-FR-01.2, inte som ett
  oväntat internt fel.
- **FBFF-FR-01.4** Ramverket ska definiera en enhetlig felresponstyp (t.ex. en `ErrorResponse`)
  som samtliga felvägar returnerar, istället för att varje BFF bygger sin egen ad-hoc-struktur.
- **FBFF-FR-01.5** Vid oväntade, okategoriserade fel ska svaret vara HTTP 500 utan att exponera
  stacktrace eller andra interna feldetaljer till klienten.

### FBFF-FR-02 — Hälsokontroll mot bakomliggande tjänst

- **FBFF-FR-02.1** Ramverket ska tillhandahålla en generisk, återanvändbar hälsokontroll som
  kontrollerar om en bakomliggande tjänst svarar, parametriserad med vilken
  konfigurationsegenskap som anger tjänstens URL.
- **FBFF-FR-02.2** Hälsokontrollen ska klassificera tjänsten som tillgänglig om anropet svarar
  med en statuskod under 500 inom en konfigurerbar timeout.
- **FBFF-FR-02.3** En BFF med flera bakomliggande tjänster ska kunna registrera en separat
  hälsokontroll per tjänst.

### FBFF-FR-03 — Vidarebefordran av auktoriseringsuppgifter

- **FBFF-FR-03.1** Ramverket ska automatiskt vidarebefordra den inkommande
  `Authorization`-headern till utgående REST-klientanrop mot bakomliggande tjänster, utan att
  varje endpoint eller klientgränssnitt explicit behöver deklarera och skicka den vidare.
- **FBFF-FR-03.2** Ramverket ska inte självt tolka, verifiera eller lagra
  auktoriseringsuppgifterna — det ansvaret ligger kvar hos de bakomliggande tjänsterna.
- **FBFF-FR-03.3** Om ingen `Authorization`-header finns i den inkommande förfrågan ska
  ramverket inte skicka en tom eller påhittad header vidare till bakomliggande tjänst.

### FBFF-FR-04 — Loggkontext och korrelation

- **FBFF-FR-04.1** Ramverket ska tillhandahålla en enhetlig mekanism för att sätta MDC-nycklar
  kring ett anrop (t.ex. via en try-with-resources-hjälpklass), istället för att varje BFF
  hanterar `MDC.put`/`MDC.remove` manuellt i varje endpoint.
- **FBFF-FR-04.2** Mekanismen ska garantera att satta MDC-nycklar rensas när anropet avslutas,
  även om ett exception kastas under körningen, för att undvika att kontext läcker mellan
  anrop.

---

## Icke-funktionella krav

### FBFF-NFR-01 — Kompatibilitet

- **FBFF-NFR-01.1** Ramverket ska paketeras som ett vanligt jar-bibliotek på samma
  Quarkus-version och parent-pom (`fk-maven-quarkus-parent`) som konsumerande BFF:er, i linje
  med övriga `rimfrost-framework-*`-paket.
- **FBFF-NFR-01.2** Ramverket ska samverka med den redan etablerade delade loggningskomponenten
  (`fk-logging`) utan att duplicera dess ansvar för loggformat eller request-loggning.
- **FBFF-NFR-01.3** En befintlig BFF ska kunna migreras till ramverket separat och oberoende av
  övriga BFF:er, utan krav på samtidig migrering av alla tjänster.

### FBFF-NFR-02 — Testbarhet

- **FBFF-NFR-02.1** Ramverket ska ha enhetstester som täcker samtliga grenar i felhanteringen
  (uppströmsfel, nätverksfel, maskerat nätverksfel, oväntat internt fel).
- **FBFF-NFR-02.2** Ramverket bör leverera återanvändbara testhjälpklasser (t.ex.
  WireMock-baserade) som konsumerande BFF:er kan använda i sina egna integrationstester.

### FBFF-NFR-03 — Underhållbarhet

- **FBFF-NFR-03.1** En ny BFF ska kunna få enhetlig felhantering, hälsokontroll,
  auktoriseringsvidarebefordran och loggkontext enbart genom att deklarera ramverket som
  beroende, utan egen kodduplicering av dessa mönster.

### FBFF-NFR-04 — Säkerhet

- **FBFF-NFR-04.1** Ramverket ska inte logga `Authorization`-headerns innehåll i klartext.

---

## Öppna frågor

- Ska befintlig `GlobalExceptionMapper` (från `rimfrost-regel-rtf-manuell-bff`) flyttas rakt av,
  eller generaliseras samtidigt (t.ex. till en `ErrorResponse`-record) innan den blir en delad
  komponent?
- Migreringsordning för befintliga BFF:er (`rimfrost-portal-bff`, `rimfrost-portal-admin-bff`,
  `rimfrost-regel-rtf-manuell-bff`, `rimfrost-regel-bekraftabeslut-bff`) — en i taget eller i en
  bestämd ordning?
- Ska CORS-konfigurationen (idag identisk `quarkus.http.cors.*`-block i varje BFF:s
  `application.properties`) standardiseras som en dokumenterad properties-mall, trots att den
  inte kan levereras som Java-kod i detta paket?
- Modellduplicering mellan `rimfrost-portal-bff` och `rimfrost-portal-admin-bff`
  (`Handlaggare`, `UppgiftMapper`, `RawOperativUppgift` m.fl.) är OUL-specifik, inte generell
  BFF-funktionalitet — hör den till ett separat OUL-adapterpaket snarare än detta ramverk?
