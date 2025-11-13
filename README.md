# Mobilidade Suave - Projeto CMU

App Android para promover práticas de mobilidade sustentável.

## ✅ Requisitos Obrigatórios Implementados

### 1. **Git e GitLab** ✅
- Projeto gerido com Git
- Push para repositório GitLab

### 2. **Suporte para diferentes dimensões (Telemóvel e Tablet)** ✅
- Implementado com `WindowSizeClass` (Material3)
- Layout adaptativo em `HomePage.kt`:
  - `HomeScreenCompact()` - Telemóvel (portrait)
  - `HomeScreenExpanded()` - Tablet/Landscape

### 3. **Material Design** ✅
- Biblioteca Material3 (Compose)
- Temas configurados em `ui/theme/`
- Componentes: Card, Button, TopAppBar, NavigationBar, etc.

### 4. **Jetpack Compose** ✅
- 100% Compose (sem XML para UI)
- Páginas: HomePage, MapPage, TripsPage, ProfilePage

### 5. **Notificações** ✅
- `TrackingService.kt` cria canal de notificações
- Notificação persistente durante rastreamento (foreground)
- Permissão `POST_NOTIFICATIONS` (Android 13+)

### 6. **Room + Firebase (cache local + online)** ✅
**Room (cache local):**
- `AppDatabase.kt` - Database principal
- Entidades: `TripEntity`, `OperatorEntity`, `UserProfileEntity`
- DAOs com Flow para observação reativa

**Firebase:**
- Firestore para dados privados (viagens, perfil)
- Realtime Database para dados públicos (operadores)

**Sincronização:**
- `MobilityRepository.kt` sincroniza Room ↔ Firebase
- Offline-first: dados guardados localmente e sincronizados quando online

### 7. **Padrão Publish/Subscribe com Firebase** ✅
- `FirebaseDataSource.kt`:
  - `observePublicOperators()` - Realtime Database ValueEventListener
  - `observeUserTrips()` - Firestore SnapshotListener
  - `observeUserProfile()` - Firestore SnapshotListener
- Updates em tempo real refletidos na UI

### 8. **Dados Públicos e Privados no Firebase** ✅
**Dados PÚBLICOS (Realtime Database):**
- `/public_operators` - todos podem ler, autenticados podem escrever

**Dados PRIVADOS (Firestore):**
- `/users/{userId}/trips` - apenas o próprio utilizador lê/escreve
- `/users/{userId}` - perfil privado

**Regras de Segurança:**
- `firebase-database-rules.json` (Realtime Database)
- `firestore.rules` (Firestore)

### 9. **Service Android** ✅
- `TrackingService.kt` - Foreground Service
- Tipo: `location` (Android 10+)
- Usa `startForeground()` com notificação obrigatória
- Rastreia localização em background

### 10. **Sensores de Localização + Mapas** ✅
**Localização:**
- `FusedLocationProviderClient` no `TrackingService`
- Permissões: `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `ACCESS_BACKGROUND_LOCATION`

**Mapas:**
- Google Maps Compose em `MapPage.kt`
- Marcadores de operadores de mobilidade
- API Key configurada no `AndroidManifest.xml`

### 11. **Retrofit (API REST)** ✅
- `MobilityApiService.kt` - interface Retrofit
- API exemplo: Overpass API (OpenStreetMap)
- **NOTA:** Validar API específica com o docente
- Interceptor de logging (OkHttp)
- Busca pontos de mobilidade próximos

### 12. **Internacionalização (vários idiomas)** ✅
- `res/values/strings.xml` (Português - padrão)
- `res/values-en/strings.xml` (Inglês)
- Uso de `stringResource()` em todo o Compose

### 13. **Interação com elementos Android** ✅
`AndroidUtils.kt` - Intents para:
- **Contactos:** `openContacts()` - `ACTION_PICK` + `READ_CONTACTS`
- **Dialer:** `openDialer()` - `ACTION_DIAL`
- **Chamadas:** `callPhone()` - `ACTION_CALL` + `CALL_PHONE`
- **SMS:** `sendSMS()` - `ACTION_SENDTO` + `SEND_SMS`
- **Partilha:** `shareText()` - `ACTION_SEND`

Botões implementados em `HomePage.kt`

### 14. **Outros Sensores (além de localização)** ✅
- **Acelerómetro** em `TrackingService.kt`
- `SensorManager` + `SensorEventListener`
- Deteta movimento para inferir se utilizador está em deslocação
- Variável `isMoving` calculada com threshold

---

## 📁 Estrutura do Projeto

```
app/src/main/java/pt/ipp/estg/cmu/
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt          # Room Database
│   │   └── Daos.kt                 # TripDao, OperatorDao, UserProfileDao
│   ├── remote/
│   │   └── FirebaseDataSource.kt  # Pub/Sub Firebase
│   └── model/
│       └── Entities.kt             # Room Entities
├── network/
│   └── MobilityApiService.kt       # Retrofit API
├── repository/
│   └── MobilityRepository.kt       # Room + Firebase + Retrofit
├── service/
│   └── TrackingService.kt          # Foreground Service (Location + Sensors)
├── ui/
│   ├── Content/
│   │   ├── HomePage.kt             # Ecrã principal (WindowSizeClass)
│   │   ├── MapPage.kt              # Google Maps
│   │   ├── TripsPage.kt            # Lista de viagens
│   │   └── ProfilePage.kt          # Perfil do utilizador
│   └── theme/                      # Material3 Theme
├── util/
│   └── AndroidUtils.kt             # Permissões + Intents Android
├── viewmodel/
│   └── MobilityViewModel.kt        # ViewModel (State Management)
├── MainActivity.kt
└── MainNavHost.kt                  # Navegação (Bottom Nav)
```

---

## 🔧 Configuração Necessária

### 1. Google Maps API Key
Editar `AndroidManifest.xml`:
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="SUA_API_KEY_AQUI" />
```

### 2. Firebase
- Adicionar `google-services.json` em `app/`
- Configurar Firestore e Realtime Database
- Aplicar regras de segurança:
  ```bash
  firebase deploy --only firestore:rules
  firebase deploy --only database:rules
  ```

### 3. API REST Externa
- Validar API com o docente
- Configurar `BASE_URL` em `MobilityApiService.kt`

---

## 🚀 Como Executar

1. **Sincronizar Gradle:**
   ```bash
   ./gradlew build
   ```

2. **Instalar no dispositivo/emulador:**
   ```bash
   ./gradlew installDebug
   ```

3. **Executar testes:**
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

---

## 📱 Funcionalidades da App

### Sistema de Pontos
- Viagens < 5km: **20 pontos/km** (incentivo)
- Viagens ≥ 5km: **10 pontos/km**
- Penalizações: deslocações não registadas detetadas (futuro)

### Tracking
1. Botão "Iniciar Rastreamento" inicia `TrackingService`
2. Serviço monitoriza GPS + Acelerómetro
3. Viagem guardada em Room e sincronizada com Firebase
4. Pontos calculados e atualizados no perfil

### Mapa
- Visualiza operadores de mobilidade (bicicletas, trotinetas, autocarros)
- Dados públicos (Realtime Database + API REST)
- Atualização em tempo real

### Perfil
- Total de pontos e viagens
- Partilha de resultados (Intent)
- Sincronização manual

---

## 🔐 Permissões Runtime

App solicita permissões runtime para:
- Localização (foreground + background)
- Notificações (Android 13+)
- Contactos
- Chamadas
- SMS

Todas as permissões tratadas com `PermissionUtils.kt`

---

## 🌍 Idiomas Suportados

- 🇵🇹 Português (padrão)
- 🇬🇧 Inglês

---

## 📊 Tecnologias Utilizadas

- **Kotlin** - Linguagem principal
- **Jetpack Compose** - UI moderna
- **Material3** - Design System
- **Room** - Base de dados local
- **Firebase** (Firestore + Realtime Database) - Backend
- **Retrofit + OkHttp** - API REST
- **Google Maps Compose** - Mapas
- **Coroutines + Flow** - Programação assíncrona
- **Navigation Compose** - Navegação
- **ViewModel** - Arquitetura MVVM

---

## 📝 TODOs Futuros

- [ ] Implementar Firebase Authentication (substituir "user_demo")
- [ ] Inferir modo de transporte pelo acelerómetro
- [ ] Detetar deslocações não registadas (penalização)
- [ ] Leaderboard público (competição entre utilizadores)
- [ ] WorkManager para sincronização periódica
- [ ] Testes unitários e instrumentados
- [ ] CI/CD com GitLab

---

## 👥 Autores

Projeto desenvolvido para a disciplina de **Computação Móvel e Ubíqua (CMU)**.

---

## 📄 Licença

Este projeto é académico e não possui licença de distribuição.

