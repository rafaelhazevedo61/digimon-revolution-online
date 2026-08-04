# Site Oficial — Digimon Revolution Online

Portal público e responsivo do projeto Digimon Revolution Online.

## Executar localmente

Por ser um site estático, basta servir a pasta raiz do projeto. A partir da pasta `digimon-revolution-online`:

```bash
python -m http.server 8080
```

Depois acesse:

- Site oficial: `http://localhost:8080/official-site/`
- Jogo: `http://localhost:8080/game-frontend/`

> Não abra o HTML diretamente por `file://` caso queira validar todos os links relativos.

## Estrutura

```text
official-site/
├── index.html
├── README.md
└── assets/
    ├── css/site.css
    ├── img/
    └── js/site.js
```

## Conteúdo entregue

- Header e navegação responsiva
- Hero com Digitama criado em CSS
- Apresentação do jogo
- Sistemas implementados
- Linha de evolução
- Simulação visual da interface do jogo
- Roadmap público
- FAQ
- CTA final e rodapé legal
- Animações com suporte a `prefers-reduced-motion`

## Galeria

As imagens da galeria ficam em `assets/img/gallery/` e os textos/categorias em `assets/js/gallery-data.js`.
Para substituir uma imagem sem alterar código, mantenha o mesmo nome do arquivo SVG ou atualize a propriedade `image` do item correspondente.
