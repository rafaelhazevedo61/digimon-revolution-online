window.DRO_NEWS = [
  {
    id: 'patch-0-4-0',
    type: 'PATCH NOTES',
    title: 'Patch 0.4.0 — Segurança, estabilidade e experiência do jogador',
    summary: 'Autenticação JWT real, proteção de endpoints administrativos, padronização de erros e melhorias importantes no fluxo inicial da jornada.',
    date: '2026-08-04',
    version: '0.4.0',
    featured: true,
    content: [
      { heading: 'Segurança e autenticação', items: [
        'Implementação de autenticação JWT real para as sessões dos jogadores.',
        'Proteção dos endpoints administrativos com regras específicas de autorização.',
        'Revisão da validação do token nas rotas protegidas.'
      ]},
      { heading: 'Estabilidade do backend', items: [
        'Padronização das respostas de erro da API.',
        'Tratamento centralizado de exceções e mensagens mais claras para o frontend.',
        'Correções no acesso ao inventário após a migração para JWT.'
      ]},
      { heading: 'Experiência inicial', items: [
        'Correção do botão “Continuar jornada” após o nascimento do Digimon.',
        'Ajustes no fluxo de seleção do Digimon ativo e redirecionamento ao dashboard.',
        'Preparação da base para o futuro tutorial inicial.'
      ]}
    ]
  },
  {
    id: 'devlog-equipamentos',
    type: 'DEVLOG',
    title: 'Equipamentos, tiers e caminhos de ascensão',
    summary: 'Conheça a estrutura de armas, armaduras, acessórios, sets, raridades, refinamento e os cinco caminhos planejados para a ascensão de equipamentos.',
    date: '2026-07-18',
    version: 'Devlog #03',
    featured: true,
    content: [
      { heading: 'Sistema de equipamentos', paragraphs: [
        'Os equipamentos foram estruturados em três categorias principais: armas, armaduras e acessórios. Cada peça pode fazer parte de um set e contribuir para diferentes estilos de construção.'
      ]},
      { heading: 'Progressão', items: [
        'Tiers de T1 a T10 com crescimento controlado de atributos.',
        'Raridades Common, Rare, Epic e Legendary.',
        'Refinamento de +0 a +10.',
        'Ascensão com cinco possibilidades de especialização.'
      ]}
    ]
  },
  {
    id: 'devlog-bosses',
    type: 'DEVLOG',
    title: 'Bosses diários, semanais e mensais',
    summary: 'O planejamento dos desafios de boss evolui para uma rotação por estágio, com conteúdos fixos e rotativos sem repetição entre as categorias.',
    date: '2026-07-02',
    version: 'Devlog #02',
    featured: false,
    content: [
      { heading: 'Estrutura dos desafios', items: [
        'Boss diário fixo acompanhado por um boss rotativo.',
        'Boss semanal exclusivo para cada estágio.',
        'Boss mensal como desafio de maior dificuldade e recompensa.',
        'Pools separados para Rookie, Champion, Ultimate e Mega.'
      ]}
    ]
  },
  {
    id: 'alpha-fundacao',
    type: 'ATUALIZAÇÃO',
    title: 'A fundação da jornada está disponível',
    summary: 'Cadastro, login, Digitamas, incubação, missões, energia, inventário, loja, evolução e Rebirth formam a base atual do jogo.',
    date: '2026-06-20',
    version: 'Alpha',
    featured: false,
    content: [
      { heading: 'Sistemas disponíveis', items: [
        'Cadastro e login de jogadores.',
        'Seleção de Digitama inicial, incubação e nascimento.',
        'Missões com duração real e consumo de energia.',
        'Inventário, itens, loja e materiais.',
        'Linhas evolutivas até Champion.',
        'Sistema de Rebirth e progressão de atributos.'
      ]}
    ]
  }
];
