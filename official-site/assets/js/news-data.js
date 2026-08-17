window.DRO_NEWS = [
  {
    id: 'patch-0-5-0',
    type: 'PATCH NOTES',
    title: 'Patch 0.5.0 — Clãs, Raids e Ferramentas Sociais',
    summary: 'Sistema de clãs com Honor Marks, upgrades, missões diárias, ranking de contribuição e a primeira Raid de Clã com boss compartilhado.',
    date: '2026-08-15',
    version: '0.5.0',
    featured: true,
    content: [
      { heading: 'Clãs e Honor Marks', items: [
        'Criação e entrada em clãs com papéis de líder e membros.',
        'Honor Marks como moeda coletiva do clã, acumulada através de missões.',
        'Loja de upgrades do clã: bônus de HP, ATK, DEF, energia máxima, capacidade de membros, redução de custo de energia e multiplicador de Honor Marks.',
        'Ranking de contribuição de Honor Marks por jogador dentro do clã.'
      ]},
      { heading: 'Missões de Clã', items: [
        'Missões diárias individuais que geram Honor Marks para o clã.',
        'Objetivos variados: derrotar bosses, completar missões comuns e duelar na arena.',
        'Resgate de recompensas com atualização automática dos Honor Marks do clã.'
      ]},
      { heading: 'Raid de Clã', items: [
        'Boss compartilhado do clã com HP coletivo, aberto a qualquer estágio de Digimon.',
        'Cada membro pode atacar até 3 vezes por dia gastando energia.',
        'Dano acumulado pelo clã, ranking de participação e recompensas de Honor Marks/XP ao derrotar o boss.',
        'Modal de resultado do ataque mostrando dano, XP, Bits e recompensas coletivas.'
      ]},
      { heading: 'Qualidade de vida e admin', items: [
        'Troca de senha pelo painel admin e pela tela de configurações do jogador.',
        'Reset diário de arena e raid preservando histórico de partidas e ataques.',
        'Comando admin para completar missões de clã em andamento.',
        'Correção na serialização de erros do painel admin.'
      ]}
    ]
  },
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
