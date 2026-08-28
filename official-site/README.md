# Site Oficial — Digimon Revolution Online

Portal público e responsivo do projeto Digimon Revolution Online. O site reúne a apresentação do jogo, roadmap, notícias, patch notes, galeria e Wiki pública.

## Executar localmente

Por ser um site estático, basta servir a pasta raiz do projeto. A partir da pasta `digimon-revolution-online`:

```bash
python -m http.server 8080
```

Depois acesse:

- Site oficial: `http://localhost:8080/official-site/`
- Jogo: `http://localhost:8080/game-frontend/`

> Não abra o HTML diretamente por `file://` caso queira validar todos os links relativos.

## Estrutura atual

```text
official-site/
├── index.html                 # Página principal, sistemas, roadmap e FAQ
├── noticias.html              # Listagem pública de notícias e atualizações
├── patch-notes.html           # Visualização detalhada de patch notes
├── galeria.html               # Galeria pública do desenvolvimento
├── README.md                  # Documentação deste site
│
├── wiki/
│   ├── index.html             # Página inicial da Wiki
│   ├── digimons.html          # Mecânicas e atributos dos Digimons
│   ├── economia.html          # Economia, recursos e materiais
│   ├── estagios.html          # Estágios evolutivos
│   ├── glossario.html         # Termos e conceitos do jogo
│   └── sistemas.html          # Sistemas e linhas evolutivas
│
└── assets/
    ├── css/
    │   └── site.css           # Estilos compartilhados do portal
    ├── img/
    │   ├── icon-192.png
    │   └── icon-512.png
    └── js/
        ├── escape.js          # Escape/sanitização usada na renderização dinâmica
        ├── gallery-data.js    # Dados da galeria
        ├── gallery.js         # Renderização da galeria
        ├── news-data.js       # Fonte de notícias e patch notes
        ├── news.js            # Renderização de notícias e patch notes
        ├── site.js            # Comportamentos gerais do portal
        └── wiki-stats-calc.js # Calculadora utilizada pela Wiki
```

## Páginas públicas

### `index.html`

Página principal do portal. Apresenta o projeto, principais sistemas, progressão, roadmap público, FAQ e chamadas para outras áreas do site.

### `noticias.html`

Lista notícias, devlogs, atualizações e patch notes publicados. Os cards são carregados a partir de `assets/js/news-data.js` e renderizados por `assets/js/news.js`.

### `patch-notes.html`

Exibe o conteúdo detalhado de uma publicação selecionada pela URL. Utiliza a mesma fonte de dados de notícias (`assets/js/news-data.js`), evitando manter duas listas independentes.

### `galeria.html`

Galeria visual do desenvolvimento. Os dados dos cards ficam em `assets/js/gallery-data.js` e a renderização em `assets/js/gallery.js`.

### `wiki/`

Conjunto de páginas públicas de referência do jogo. A Wiki documenta progressão, estágios, sistemas, economia, atributos de Digimon e glossário.

## Notícias e patch notes

A fonte de dados compartilhada fica em:

```text
assets/js/news-data.js
```

Cada entrada define o conteúdo exibido na listagem de notícias e, quando aplicável, na página detalhada de patch notes.

A lógica de renderização fica em:

```text
assets/js/news.js
```

Ao publicar uma nova atualização, prefira adicionar os dados em `news-data.js` em vez de duplicar o conteúdo diretamente nos arquivos HTML.

## Galeria

Os dados da galeria ficam em:

```text
assets/js/gallery-data.js
```

A renderização fica em:

```text
assets/js/gallery.js
```

As imagens utilizadas pelo portal devem permanecer em `assets/img/` ou em subdiretórios apropriados dessa pasta. Ao substituir uma imagem existente, mantenha o caminho referenciado no dado correspondente ou atualize a propriedade utilizada pelo card.

## Assets compartilhados

- `assets/css/site.css`: estilos globais do portal e das páginas públicas.
- `assets/js/site.js`: navegação, tema e comportamentos gerais.
- `assets/js/escape.js`: utilitário de escape para conteúdo renderizado dinamicamente.
- `assets/js/news-data.js`: dados de notícias e patch notes.
- `assets/js/news.js`: renderização de notícias e patch notes.
- `assets/js/gallery-data.js`: dados da galeria.
- `assets/js/gallery.js`: renderização da galeria.
- `assets/js/wiki-stats-calc.js`: recursos interativos específicos da Wiki.

## Documentação relacionada e diretriz de manutenção

A documentação técnica do aplicativo dos jogadores está em [`../game-frontend/README.md`](../game-frontend/README.md). Ela descreve rotas, ordem de carregamento, integração HTTP, sessão, PWA e os fluxos atuais de Incubação, Storage, Inventário, Digimon Info e Correio.

A documentação deste arquivo deve acompanhar a estrutura real de `official-site/`. Ao criar, remover ou reorganizar uma página pública, atualize esta árvore e a seção correspondente no mesmo PR.

Conteúdo funcional divulgado no site e na Wiki também deve ser revisado quando uma mecânica mudar no jogo, evitando divergências entre implementação e documentação pública. Para alterações de gameplay, confira especialmente [`wiki/sistemas.html`](wiki/sistemas.html), [`wiki/economia.html`](wiki/economia.html) e [`wiki/estagios.html`](wiki/estagios.html).
