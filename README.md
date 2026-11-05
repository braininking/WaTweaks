# WaTweaks

Projeto Android/Xposed para auditoria técnica e personalização visual experimental do WhatsApp e WhatsApp Business.

## Estado atual
- Pacotes reconhecidos: `com.whatsapp` e `com.whatsapp.w4b`.
- Versão de referência: `2.26.41.77`.
- Entrada Xposed: `com.braininking.watweaks.core.MainHook`.
- Compilação Debug concluída.
- Catálogo inicial de ajustes visuais em `PrivacyEngine`.
- Rotina de auditoria de metadados em `HostAudit` (ainda não conectada ao ciclo Xposed).
- Nenhum hook visual está ativo nesta versão.
- Nenhuma função completa do Delta GB ou WaEnhancerX foi portada ainda.

## Compilar
Requisitos: Android SDK API 36, JDK 17+ e Gradle Wrapper incluído.

```powershell
.\gradlew.bat :app:assembleDebug
```

Saída: `app/build/outputs/apk/debug/app-debug.apk`.

## Próximas etapas
1. Mapear pontos de extensão visuais reais na versão oficial.
2. Criar tela de configurações e armazenamento local seguro.
3. Implementar um ajuste visual por vez, com proteção por versão.
4. Testar em ambiente de laboratório antes de instalar.

## Segurança e escopo
O projeto não falsifica assinatura/instalador, não burla integridade, não altera telemetria de segurança e não modifica Signal, criptografia ou protocolo de rede. Não lê conteúdo de mensagens, contatos, tokens ou chaves.

Consulte [docs/ANALISE_E_PLANO.md](docs/ANALISE_E_PLANO.md) para o estado detalhado.
