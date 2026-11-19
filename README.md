# FIF-EDIT · Editor de camisas do FIFA 14

App de página única (HTML) para mudar o **número da camisa** dos jogadores no save da carreira
(`SAVECAREER...`) do FIFA 14 Android (`com.ea.game.fifa14_row`). Nada além do número é alterado. (O save guarda o número menos 1; o app já corrige isso.)

## Usar
1. Faça **backup** da pasta `Android/data/com.ea.game.fifa14_row/files`.
2. Abra `index.html` no navegador (ou pelo GitHub Pages: *Settings → Pages → branch `main`*).
3. Passo 1: escolha o `fifa.db` (nomes de jogadores e times; fica salvo no navegador).
3. Passo 2: escolha o arquivo `SAVECAREER...`.
4. Escolha o time. Para achar o jogador recém-contratado, use **"Descobrir o jogador que acabei de contratar"** com um save antigo: quem é novo no time aparece com a etiqueta NOVO.
5. Mude o número, toque em **Baixar save editado** e coloque o arquivo de volta na pasta do jogo (mesmo nome).

O save guarda só IDs; o `fifa.db` traduz ID → nome (busque jogador por nome ou ID). Para corrigir um nome, toque nele de um jogador para nomeá-lo (fica salvo no navegador), ou importe uma lista `p,ID,Nome` / `t,ID,Nome`.

## Comando `gitpush`
```sh
sh install-gitpush.sh      # uma vez
gitpush "minha mensagem"   # envia tudo para o GitHub
```
Sem instalar: `sh gitpush.sh "mensagem"`. Requer `git` e login no GitHub (token) no primeiro push.

## Aviso
Projeto não oficial, sem relação com a EA. Use em saves seus e mantenha backup. Os últimos 4 bytes do save são um CRC-32 (variante BZIP2, little-endian) que o app recalcula ao baixar. Sem isso o jogo recusa o save ("Sem jogos salvos"). Mantenha sempre o backup.

## Publicar online (Actions)
O workflow `.github/workflows/pages.yml` publica o app a cada push. Ative uma vez em
*Settings → Pages → Source: GitHub Actions*. O link aparece em *Actions* após o primeiro push.
O token do `gitpush` precisa das permissões `repo` e `workflow`.

## APK (Android)
O workflow `.github/workflows/apk.yml` empacota o `index.html` com Capacitor e gera o APK a cada push.
Baixe em *Releases → FIF-EDIT (APK mais recente)* ou em *Actions → Build APK → Artifacts*.
No app, **Baixar save editado** abre o menu de compartilhar: escolha *Salvar em arquivos* e depois copie o arquivo para a pasta do jogo (mesmo nome).

## Janela flutuante
No app, toque em **Abrir como janela flutuante** (libere *Sobrepor a outros apps* na primeira vez). Uma bolinha ⚽ fica por cima do jogo: toque para abrir/minimizar o editor, arraste a bolinha ou a barra do painel para mover. O ✕ fecha tudo. Ao baixar, o menu de compartilhar abre; salve em Arquivos e copie para a pasta do jogo.

## Atualizar o app
O APK é assinado sempre com a mesma chave (`native/debug.keystore`), então basta instalar o novo por cima. Se você tinha uma versão anterior a esta, desinstale uma única vez antes.
