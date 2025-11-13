# 🚀 Setup Rápido - Mobilidade Suave

## Passo 1: Clonar o Projeto
```bash
git clone <URL_DO_GITLAB>
cd CMU-TP
```

## Passo 2: Configurar Firebase

### 2.1 Criar Projeto Firebase
1. Aceder a [Firebase Console](https://console.firebase.google.com/)
2. Criar novo projeto: "Mobilidade-Suave"
3. Adicionar app Android:
   - Package name: `pt.ipp.estg.cmu`
   - Download `google-services.json`
   - Copiar para `app/google-services.json`

### 2.2 Ativar Serviços
1. **Firestore Database:**
   - Modo: Produção
   - Localização: europe-west1

2. **Realtime Database:**
   - Modo: Locked mode
   - Localização: europe-west1

### 2.3 Aplicar Regras de Segurança
```bash
# Instalar Firebase CLI
npm install -g firebase-tools

# Login
firebase login

# Inicializar projeto
firebase init

# Deploy das regras
firebase deploy --only firestore:rules
firebase deploy --only database:rules
```

**Alternativamente:** copiar manualmente das Firebase Console:
- Firestore: copiar conteúdo de `firestore.rules`
- Realtime DB: copiar conteúdo de `firebase-database-rules.json`

## Passo 3: Configurar Google Maps

### 3.1 Obter API Key
1. Aceder a [Google Cloud Console](https://console.cloud.google.com/)
2. Criar novo projeto ou selecionar existente
3. Ativar **Maps SDK for Android**
4. Criar credenciais → API Key
5. Restringir chave:
   - Tipo: Android apps
   - Package name: `pt.ipp.estg.cmu`
   - SHA-1: (do teu keystore)

### 3.2 Adicionar ao Projeto
Editar `app/src/main/AndroidManifest.xml`:
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="AIza..." />  <!-- Substituir pela tua chave -->
```

## Passo 4: Sync Gradle
```bash
# Windows
.\gradlew build

# Linux/Mac
./gradlew build
```

## Passo 5: Executar

### Via Android Studio
1. Abrir projeto no Android Studio
2. Sync Gradle (botão "Sync Now")
3. Selecionar dispositivo/emulador
4. Run → app

### Via Linha de Comandos
```bash
# Build
.\gradlew assembleDebug

# Instalar
adb install app\build\outputs\apk\debug\app-debug.apk

# Abrir
adb shell am start -n pt.ipp.estg.cmu/.MainActivity
```

## Passo 6: Testar Funcionalidades

### 6.1 Permissões
Garantir que são concedidas:
- ✅ Localização (sempre/em uso)
- ✅ Notificações
- ✅ Contactos (opcional)
- ✅ Chamadas (opcional)
- ✅ SMS (opcional)

### 6.2 Tracking
1. Abrir app → HomePage
2. Clicar "Iniciar Rastreamento"
3. Verificar notificação persistente
4. Movimentar-se (ou simular no emulador)
5. Clicar "Parar Rastreamento"
6. Verificar viagem em "Viagens"

### 6.3 Mapa
1. Navegar para "Mapa"
2. Verificar marcadores (se houver dados)
3. Clicar FAB "🔄" para atualizar operadores

### 6.4 Perfil
1. Navegar para "Perfil"
2. Verificar estatísticas
3. Testar botões:
   - "Partilhar Resultados"
   - "Sincronizar Dados"

### 6.5 Intents Android
Na HomePage:
- "Partilhar com Contacto" → abre contactos
- "Ligar Suporte" → abre dialer (112)
- "Enviar SMS" → abre SMS

## Troubleshooting

### Erro: "google-services.json not found"
**Solução:** Copiar ficheiro para `app/google-services.json`

### Erro: "API key not valid"
**Solução:** Verificar restrições da API Key no Google Cloud Console

### Erro: "Permission denied" (Location)
**Solução:**
1. Settings → Apps → Mobilidade Suave → Permissions
2. Ativar "Location" → "Allow all the time"

### Não aparecem operadores no mapa
**Solução:**
1. Verificar internet
2. Verificar Firebase Realtime Database
3. Adicionar dados manualmente:
```json
{
  "op1": {
    "name": "Estação Bicicletas",
    "type": "bike",
    "latitude": 41.1579,
    "longitude": -8.6291,
    "available": true
  }
}
```

### App crashs ao iniciar tracking
**Solução:**
1. Verificar permissões concedidas
2. Verificar Google Play Services instalado
3. Logs: `adb logcat | grep "TrackingService"`

## Comandos Úteis

```bash
# Ver logs em tempo real
adb logcat | grep "CMU"

# Limpar dados da app
adb shell pm clear pt.ipp.estg.cmu

# Simular localização (emulador)
adb emu geo fix -8.6291 41.1579

# Desinstalar
adb uninstall pt.ipp.estg.cmu

# Build Release (assinado)
.\gradlew assembleRelease
```

## Checklist Final

- [ ] Firebase configurado
- [ ] Google Maps API Key adicionada
- [ ] Gradle sync sem erros
- [ ] App instala e abre
- [ ] Permissões concedidas
- [ ] Tracking funciona
- [ ] Mapa exibe
- [ ] Viagens listadas
- [ ] Intents Android funcionam
- [ ] Multi-idioma testado (mudar idioma do sistema)

## Próximos Passos

1. **Autenticação:** Implementar Firebase Auth (substituir "user_demo")
2. **Testes:** Escrever unit tests e UI tests
3. **CI/CD:** Configurar pipeline GitLab
4. **Otimizações:** ProGuard/R8 para release
5. **Analytics:** Adicionar Firebase Analytics events

---

**Documentação Completa:** Ver `README.md`  
**Checklist Requisitos:** Ver `CHECKLIST.md`  
**Resumo Executivo:** Ver `RESUMO.md`

**Status:** ✅ Pronto para demonstração

