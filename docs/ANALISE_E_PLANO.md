# WaTweaks — análise e plano de implementação

## Estado desta compilação
- Módulo Android/Xposed compilado em modo Debug.
- Pacotes reconhecidos: `com.whatsapp` e `com.whatsapp.w4b`.
- Versão de referência: WhatsApp `2.26.41.77`.
- A entrada Xposed registra apenas a inicialização do módulo e uma mensagem de segurança.
- `HostAudit` contém rotina para coletar versão, instalador e SHA-256 do certificado quando receber um Context; ainda não está ligada ao ciclo de inicialização.
- `PrivacyEngine` contém apenas o catálogo/modelo dos ajustes; ainda não há tela de configurações nem hooks visuais ativos.

## Recursos observados no Delta GB
A análise estática identificou:
- respostas automáticas agendadas;
- backup/restauração local;
- temas;
- personalização da navegação e elementos flutuantes;
- classes de interface para lista inicial e conversas.

O código do Delta não deve ser copiado cegamente: nomes e estruturas internas são diferentes e precisam ser mapeados para a versão oficial.

## Referência WaEnhancerX
WaEnhancerX é um módulo Xposed/LSPosed. O código público depende de um submódulo privado de licenciamento e da chave original de assinatura para uma compilação de distribuição completa. A compatibilidade declarada pelo projeto não garante que cada recurso funcione nesta instalação.

## Próximas etapas técnicas
1. Terminar o mapeamento estático das classes de interface da versão oficial.
2. Criar a tela local de configurações e persistência segura.
3. Validar primeiro um ajuste puramente visual, com guarda de versão e desativação simples.
4. Testar isoladamente em aparelho de laboratório antes de qualquer instalação.
5. Adicionar recursos do Delta somente após mapear comportamento, permissões e efeitos colaterais.

## Limites
Não estão implementados recursos de privacidade que alterem recibos, presença, mensagens apagadas ou mensagens temporárias. Não há hooks de criptografia, Signal, rede, assinatura, instalador ou mecanismos de integridade. Nenhum APK foi instalado ou substituído e nenhum repositório remoto foi alterado.
