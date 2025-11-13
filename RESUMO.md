# 📱 Mobilidade Suave - Resumo Executivo

## 🎯 Objetivo
App Android para promover práticas de mobilidade sustentável através de gamificação, rastreamento de viagens e localização de operadores de mobilidade suave.

---

## ✅ Requisitos Obrigatórios - 14/14 Implementados

| # | Requisito | Status | Ficheiro Principal |
|---|-----------|--------|-------------------|
| 1 | Git + GitLab | ✅ | `.git/` |
| 2 | Responsive (Phone/Tablet) | ✅ | `HomePage.kt` (WindowSizeClass) |
| 3 | Material Design | ✅ | `ui/theme/` + Material3 |
| 4 | Jetpack Compose | ✅ | Todas as páginas |
| 5 | Notificações | ✅ | `TrackingService.kt` |
| 6 | Room + Firebase | ✅ | `AppDatabase.kt` + `FirebaseDataSource.kt` |
| 7 | Pub/Sub Firebase | ✅ | `FirebaseDataSource.kt` (listeners) |
| 8 | Dados Públicos/Privados | ✅ | `firestore.rules` + `firebase-database-rules.json` |
| 9 | Service Android | ✅ | `TrackingService.kt` (Foreground) |
| 10 | GPS + Mapas | ✅ | `TrackingService.kt` + `MapPage.kt` |
| 11 | Retrofit (API REST) | ✅ | `MobilityApiService.kt` |
| 12 | Internacionalização | ✅ | `values/strings.xml` (PT + EN) |
| 13 | Intents Android | ✅ | `AndroidUtils.kt` (Contacts/Dialer/SMS) |
| 14 | Outros Sensores | ✅ | `TrackingService.kt` (Acelerómetro) |

---

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────────┐
│              UI Layer (Compose)                 │
│  HomePage │ MapPage │ TripsPage │ ProfilePage   │
└────────────────────┬────────────────────────────┘
                     │
          ┌──────────▼──────────┐
          │  MobilityViewModel  │ (State Management)
          └──────────┬──────────┘
                     │
          ┌──────────▼──────────┐
          │ MobilityRepository  │ (Data Layer)
          └─────┬──────┬────────┘
                │      │
       ┌────────▼──┐  ┌▼────────────┐  ┌──────────┐
       │   Room    │  │   Firebase  │  │ Retrofit │
       │ (Local DB)│  │ (Cloud + RT)│  │  (API)   │
       └───────────┘  └─────────────┘  └──────────┘
```

---

## 🔑 Funcionalidades Principais

### 1. **Rastreamento de Viagens**
- Service foreground com notificação persistente
- GPS + Acelerómetro para deteção de movimento
- Cálculo automático de pontos (incentivo < 5km)
- Sincronização offline/online (Room → Firebase)

### 2. **Mapa Interativo**
- Google Maps Compose
- Marcadores de operadores (bicicletas, trotinetas, autocarros)
- Dados públicos em tempo real (Firebase Realtime Database)
- Integração com API REST externa (Overpass API)

### 3. **Sistema de Pontos**
```kotlin
// Lógica de gamificação
if (distância < 5km) {
    pontos = distância * 20  // Dobro de pontos (incentivo)
} else {
    pontos = distância * 10
}
```

### 4. **Perfil e Estatísticas**
- Total de pontos e viagens
- Partilha de resultados (Intent)
- Sincronização manual

### 5. **Integração com Android**
- 📞 Dialer (suporte técnico)
- 📱 SMS (partilha de resultados)
- 👤 Contactos (seleção para partilha)

---

## 📊 Tecnologias

| Categoria | Tecnologia |
|-----------|------------|
| **Linguagem** | Kotlin 2.0.21 |
| **UI** | Jetpack Compose + Material3 |
| **Arquitetura** | MVVM (ViewModel + Repository) |
| **Local DB** | Room 2.6.1 |
| **Cloud** | Firebase (Firestore + Realtime DB) |
| **API REST** | Retrofit 2.11 + OkHttp |
| **Mapas** | Google Maps Compose |
| **Async** | Coroutines + Flow |
| **DI** | Manual (Database singleton) |

---

## 🔐 Segurança & Privacidade

### Firebase Realtime Database Rules
```json
{
  "rules": {
    "public_operators": {
      ".read": true,                    // ✅ Dados PÚBLICOS
      ".write": "auth != null"
    },
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",   // 🔒 Dados PRIVADOS
        ".write": "$uid === auth.uid"
      }
    }
  }
}
```

### Firestore Rules
```javascript
match /users/{userId} {
  allow read, write: if request.auth.uid == userId;  // 🔒 PRIVADO
  
  match /trips/{tripId} {
    allow read, write: if request.auth.uid == userId;
  }
}
```

---

## 📱 Demonstração de Fluxo

### Fluxo Principal de Utilização
```
1. Utilizador abre app
   ↓
2. HomePage mostra estatísticas (Room cache)
   ↓
3. Clica "Iniciar Rastreamento"
   ↓
4. TrackingService inicia (notificação visível)
   ↓
5. GPS + Acelerómetro monitorizam deslocação
   ↓
6. Ao terminar: viagem guardada (Room)
   ↓
7. Background sync com Firebase (quando online)
   ↓
8. TripsPage atualizada em tempo real
   ↓
9. Pontos somados ao perfil
```

---

## 🧪 Testes Críticos

### ✅ Funcionalidade
- [x] Tracking com GPS funciona
- [x] Viagens aparecem na lista
- [x] Mapa mostra operadores
- [x] Intents Android funcionam
- [x] Multi-idioma (PT/EN)

### ✅ Offline/Online
- [x] App funciona sem internet (Room cache)
- [x] Sincroniza ao reconectar

### ✅ Responsive
- [x] Layout Compact (phone)
- [x] Layout Expanded (tablet)

### ✅ Permissões
- [x] Solicita permissões runtime
- [x] Trata negação gracefully

---

## 📦 Entregáveis

```
CMU-TP/
├── README.md                    # Documentação completa
├── CHECKLIST.md                 # Checklist detalhado
├── RESUMO.md                    # Este ficheiro
├── firebase-database-rules.json # Regras Realtime DB
├── firestore.rules              # Regras Firestore
├── app/
│   ├── build.gradle.kts         # Dependências
│   ├── src/main/
│   │   ├── AndroidManifest.xml  # Permissões + Service
│   │   ├── java/pt/ipp/estg/cmu/
│   │   │   ├── data/            # Room + Firebase
│   │   │   ├── network/         # Retrofit
│   │   │   ├── repository/      # Sincronização
│   │   │   ├── service/         # TrackingService
│   │   │   ├── ui/              # Compose Pages
│   │   │   ├── util/            # Utils + Intents
│   │   │   └── viewmodel/       # ViewModel
│   │   └── res/
│   │       ├── values/          # Strings PT
│   │       └── values-en/       # Strings EN
│   └── google-services.json     # Firebase config
└── .git/                        # Repositório Git
```

---

## ⚠️ Configuração Necessária (Antes de Executar)

### 1. Firebase
```bash
# 1. Criar projeto Firebase
# 2. Adicionar app Android (package: pt.ipp.estg.cmu)
# 3. Download google-services.json → app/
# 4. Ativar Firestore e Realtime Database
# 5. Aplicar regras:
firebase deploy --only firestore:rules
firebase deploy --only database:rules
```

### 2. Google Maps API
```xml
<!-- AndroidManifest.xml -->
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_API_KEY_HERE" />
```

### 3. Build
```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 🎓 Notas para Avaliação

### Destaques Técnicos
1. **Sincronização Offline-First:** Room como cache + Firebase como backend
2. **Pub/Sub Real-Time:** Listeners Firebase com Flow
3. **Foreground Service:** Tracking em background com notificação
4. **Multi-Sensor:** GPS + Acelerómetro em conjunto
5. **Responsive Design:** WindowSizeClass para adaptação automática
6. **Clean Architecture:** Separation of concerns (UI → ViewModel → Repository → Data)

### Inovações
- Sistema de pontos com incentivo para viagens curtas (< 5km)
- Deteção de movimento com acelerómetro (inferência de transporte)
- Cache inteligente para funcionamento offline completo

---

## 📞 Suporte

Para questões técnicas, consultar:
- `README.md` - Documentação completa
- `CHECKLIST.md` - Verificação item a item
- Código comentado em todos os ficheiros principais

---

**Desenvolvido para:** Computação Móvel e Ubíqua (CMU)  
**Status:** ✅ **Pronto para demonstração**  
**Data:** Novembro 2025

