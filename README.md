# Заң KZ

Қазақстан Республикасының заңдарын іздеуге және оқуға арналған мобильді қосымшаның сервер бөлігі.

> **Маңызды.** Қосымшадағы қарапайым түсіндірмелер ресми заң кеңесі болып табылмайды.
> Заң мәтіндерінің ресми дереккөзі — [adilet.zan.kz](https://adilet.zan.kz). Әр заңда дереккөз сілтемесі мен ақпараттың соңғы жаңартылған күні көрсетіледі.

## Мүмкіндіктер

- Заң категориялары, заңдар және баптар (қазақ, орыс, ағылшын тілдерінде)
- Әр бапқа қарапайым тілмен түсіндірме
- Заң терминдерінің сөздігі және іздеу
- Тіркелу, кіру, USER және ADMIN рөлдері
- Таңдаулылар және соңғы қаралған баптар тарихы
- Заң жаңартылғанда автоматты түрде хабарлама жасалады
- API құжаттамасы (Swagger)

## Технологиялар

| Бөлім | Технология |
|---|---|
| Тіл | Java 21 |
| Фреймворк | Spring Boot 4 |
| Деректер қоры | PostgreSQL, Spring Data JPA, Hibernate |
| Қауіпсіздік | Spring Security, BCrypt, Bearer токен |
| Тексеру | Bean Validation, Global Exception Handler |
| Құжаттама | springdoc-openapi (Swagger UI) |
| Тесттер | JUnit 5, Mockito, AssertJ |
| Құрастыру | Maven |
| Мобильді қосымша | React Native (Expo) |

## Архитектура

Сервер қабаттарға бөлінген (Layered Architecture):

```
Controller  →  Service  →  Repository  →  PostgreSQL
(REST API)    (логика)    (Spring Data)
```

| Бума | Мазмұны |
|---|---|
| `controller` | REST адрестері |
| `service` | бизнес-логика |
| `repository` | деректер қорымен жұмыс |
| `model` | кестелер (JPA entity) |
| `dto` | сұраныс және жауап объектілері |
| `security` | токенді тексеретін сүзгі |
| `config` | қауіпсіздік, Swagger, бастапқы деректер |
| `event` | оқиғалар және тыңдаушылар |
| `exception` | қателер және оларды өңдеу |

### Кестелер және байланыстар

```
Category 1 ──── * Law 1 ──── * Article
                 │
Notification * ──┘ (міндетті емес)

User 1 ──── * Favorite * ──── 1 Article
User 1 ──── * HistoryEntry * ──── 1 Article

Term (жеке кесте)
```

## Рөлдер

| Әрекет | Кірусіз | USER | ADMIN |
|---|---|---|---|
| Категорияларды, заңдарды, баптарды, терминдерді, хабарламаларды оқу | ✅ | ✅ | ✅ |
| Таңдаулылар және тарих | ❌ | ✅ | ✅ |
| Қосу, өзгерту, жою | ❌ | ❌ | ✅ |

Тіркелу арқылы тек USER рөлін алуға болады. Әкімші (ADMIN) сервер іске қосылғанда `local.properties` файлындағы деректер бойынша жасалады.

## API

Толық тізім және тексеру: `http://localhost:8080/swagger-ui.html`

| Әдіс | Адрес | Сипаттама |
|---|---|---|
| POST | `/api/auth/register` | тіркелу |
| POST | `/api/auth/login` | кіру, токен алу |
| GET | `/api/auth/me` | ағымдағы пайдаланушы |
| GET, POST, PUT, DELETE | `/api/categories` | категориялар |
| GET, POST, PUT, DELETE | `/api/laws` (`?categoryId=`) | заңдар |
| GET, POST, PUT, DELETE | `/api/articles` (`?lawId=`) | баптар |
| GET, POST, PUT, DELETE | `/api/terms` (`?q=`) | терминдер сөздігі |
| GET, POST, DELETE | `/api/notifications` | хабарламалар |
| GET, POST, DELETE | `/api/favorites` | таңдаулылар |
| GET, POST, DELETE | `/api/history` | қаралған баптар тарихы |

## Жобалау үлгілері (Design Patterns)

1. **Repository.** Деректер қорымен жұмыс `CategoryRepository`, `LawRepository`, `ArticleRepository` т.б. интерфейстерге бөлінген. Сервистер SQL жазбайды, тек репозиторийді шақырады. Деректер қорын ауыстырса, сервистер өзгермейді.

2. **Observer (бақылаушы).** Әкімші заңды өзгерткенде `LawService` тек `LawUpdatedEvent` оқиғасын жариялайды. Оны `LawUpdatedListener` тыңдап, барлық пайдаланушыларға хабарлама жасайды. Заң сервисі хабарламалар туралы ештеңе білмейді, сондықтан жаңа реакция қосу үшін (мысалы, email) тек жаңа тыңдаушы жазу жеткілікті. Тест: `LawServiceTest`.

3. **DTO (Data Transfer Object).** `RegisterRequest`, `AuthResponse`, `FavoriteResponse` т.б. Пайдаланушы кестесі сыртқа берілмейді, сондықтан құпиясөз хэші мен токен жауапқа түспейді.

4. **Dependency Injection.** Барлық тәуелділіктер конструктор арқылы беріледі. Бұл тесттерде нақты деректер қорының орнына Mockito бүркеншіктерін қоюға мүмкіндік береді.

## Іске қосу

### Қажет

- Java 21
- PostgreSQL
- Git

### Қадамдар

1. Репозиторийді жүктеу:
```
   git clone https://github.com/nurmukhan-20/zan-kz.git
   cd zan-kz/server
```

2. PostgreSQL-да `zankz` деректер қорын жасау.

3. `server` бумасында `local.properties` файлын жасау (бұл файл GitHub-қа жүктелмейді):
```properties
   spring.datasource.password=POSTGRES_ҚҰПИЯСӨЗІ
   app.admin.email=admin@zankz.kz
   app.admin.password=ӘКІМШІ_ҚҰПИЯСӨЗІ
```

4. Серверді іске қосу:
```
   .\mvnw.cmd spring-boot:run
```
   (Linux/macOS: `./mvnw spring-boot:run`)

5. Тексеру:
   - `http://localhost:8080/api/categories`
   - `http://localhost:8080/swagger-ui.html`

Бірінші іске қосқанда кестелер автоматты түрде жасалады және бастапқы деректер (категориялар, Конституцияның I бөлімі, терминдер) толтырылады.

### Тесттер

```
.\mvnw.cmd test
```

Тесттер деректер қорынсыз жұмыс істейді (Mockito).

## Деректер көзі

Конституция мәтіні: [Қазақстан Республикасының Конституциясы — «Әділет» АҚЖ](https://adilet.zan.kz/kaz/docs/K2600000000) (2026 жылғы 15 наурызда референдумда қабылданған, 2026 жылғы 1 шілдеден бастап күшіне енген).

## Автор

Иманқұл Нұрмұхан