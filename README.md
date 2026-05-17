# Lens Translate MVP

Protótipo Android em Kotlin inspirado no conceito do PlayTranslate, mas escrito do zero.

## O que vem pronto

- Tela inicial em Jetpack Compose.
- Solicitação de permissão para desenhar sobre outros apps.
- Serviço com botão flutuante `TR`.
- Captura da tela com MediaProjection.
- OCR local com Google ML Kit Text Recognition.
- Tradução local com Google ML Kit Translate, padrão Japonês -> Português.

## Como rodar

1. Abra a pasta no Android Studio.
2. Aguarde o Gradle sincronizar.
3. Rode no Android 8+; Android 11+ recomendado.
4. Toque em `Permitir sobrepor a outros apps`.
5. Toque em `Iniciar lente flutuante` e aceite a captura de tela.
6. Abra um jogo/app e toque no botão `TR`.

## Próximos recursos recomendados

- Seletor de idioma de origem/destino.
- Recorte da região de diálogo para melhorar OCR e privacidade.
- Modo automático com debounce: capturar a cada 1–2 segundos e só traduzir quando o texto mudar.
- Dicionário por palavra ao tocar/arrastar a lente.
- Exportação AnkiDroid.
- Cache de traduções por hash do texto.
- Configuração para DeepL, LibreTranslate ou servidor próprio.

## Observação de licença

O PlayTranslate original está sob GPL-3.0. Este MVP não copia arquivos do repositório; ele apenas implementa uma ideia semelhante com estrutura própria.

## Gerar APK pelo GitHub Actions pelo celular

Este projeto já inclui o workflow `.github/workflows/build-apk.yml`.

1. Envie todos os arquivos deste projeto para seu repositório GitHub.
2. Abra a aba **Actions** do repositório.
3. Toque em **Build Android APK**.
4. Toque em **Run workflow**.
5. Quando terminar, abra a execução e baixe o artefato **LensTranslateMVP-debug-apk**.
6. Extraia o `.zip` baixado; dentro dele estará o APK de debug.

No Android, talvez você precise permitir instalação de apps de origem desconhecida para instalar o APK.
