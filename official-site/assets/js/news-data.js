window.DRO_NEWS = [
  {
    id: 'update-quality-of-life-2026-08',
    type: 'ATUALIZAÇÃO',
    title: 'Qualidade de vida — filtros, uso em lote e linhas evolutivas',
    summary: 'A Alpha recebeu melhorias para organizar o inventário e o Storage, acelerar o uso de itens, consultar origens e acompanhar linhas evolutivas.',
    date: '2026-08-28',
    version: 'Alpha — Agosto 2026',
    featured: true,
    content: [
      { heading: 'Inventário e Storage', items: [
        'Inventário e Storage agora possuem busca, filtros e opções de ordenação para localizar itens e Digimons com mais facilidade.',
        'O inventário passou a trabalhar com pilhas de até 999 unidades para itens empilháveis.',
        'Poções, pedras de treino, discos de XP e baús podem ser usados ou abertos em lote quando houver quantidade disponível.'
      ]},
      { heading: 'Progressão e Digimon Info', items: [
        'Discos de XP de 1%, 3%, 5%, 10%, 15% e 20% concedem experiência imediatamente ao Digimon ativo.',
        'A barra de experiência exibe o percentual atual no centro, facilitando o acompanhamento do próximo nível.',
        'Digimon Info passou a mostrar a Digitama de origem dos Digimons Baby elegíveis.',
        'Linhas evolutivas podem ser consultadas em uma visualização com imagens e navegação entre as formas cadastradas.'
      ]},
      { heading: 'Correio e conta', items: [
        'O Correio recebeu a ação “Marcar comuns como lidas”, preservando mensagens que ainda possuem recompensa ou ação pendente.',
        'A tela de Configurações permite alterar o e-mail da conta mediante confirmação da senha e renovar a sessão.'
      ]},
      { heading: 'Arena', items: [
        'A Arena recebeu novos oponentes controlados pelo servidor para ampliar a disponibilidade de desafios nas faixas mais altas de rating.',
        'O lobby identifica quando o oponente encontrado é um bot.'
      ]}
    ]
  },
  {
    id: 'patch-0-7-0',
    type: 'PATCH NOTES',
    title: 'Patch 0.7.0 — Baús, recompensas e melhorias da Alpha',
    summary: 'Novos baús e Loot Tables ampliam as recompensas de missões, bosses, Arena e Boss Mundial, acompanhados por melhorias no tutorial, inventário, segurança e sessões.',
    date: '2026-08-23',
    version: '0.7.0',
    featured: true,
    content: [
      { heading: 'Baús e Loot Tables', items: [
        'Novo sistema reutilizável de Loot Tables e definições de baús para organizar recompensas de diferentes atividades.',
        'Missões podem entregar baús de área, com abertura e exibição das recompensas diretamente no fluxo do jogador.',
        'Bosses regulares passam a contar com baús de recompensa configuráveis.',
        'A Arena recebeu baús de recompensa associados às faixas competitivas.',
        'O Boss Mundial passa a utilizar baús de recompensa configuráveis de acordo com o resultado do ciclo.',
        'A loja passa a utilizar baús de fragmentos no lugar de fragmentos legados em pontos preparados para esse novo fluxo.'
      ]},
      { heading: 'Boss Mundial', items: [
        'Adicionado tempo de recarga configurável entre ataques, com contagem regressiva visível para o jogador.',
        'Melhor organização do resultado dos ataques e das recompensas recebidas.',
        'Recompensas de conclusão passam a utilizar o novo sistema de baús.',
        'Ciclos podem ser reiniciados administrativamente quando necessário.'
      ]},
      { heading: 'Tutorial e experiência da Alpha', items: [
        'Recompensas do tutorial agora podem ser resgatadas manualmente nos passos correspondentes.',
        'Nomes amigáveis dos itens são exibidos nas recompensas em vez de identificadores técnicos.',
        'Textos e feedbacks do tutorial foram ajustados para melhorar a leitura e a compreensão do fluxo inicial.',
        'Inventário recebeu normalização e ordenação para apresentar itens de forma mais consistente.',
        'Recompensas de missões passaram a ser apresentadas em modal após o resgate.'
      ]},
      { heading: 'Segurança e sessões', items: [
        'Política de senha foi padronizada entre os fluxos da aplicação.',
        'Sessões antigas podem ser invalidadas de forma segura através da versão do token.',
        'A troca de senha passou a proteger também a atualização do token da sessão atual.',
        'Sessões inválidas são redirecionadas corretamente para autenticação.',
        'Origens permitidas pelo CORS e configurações sensíveis do ambiente foram reforçadas.',
        'Tratamento de conteúdo exibido no frontend e no site oficial recebeu proteção adicional contra injeção de HTML.'
      ]},
      { heading: 'Administração e estabilidade', items: [
        'Ferramentas internas e simuladores foram reforçados dentro do namespace administrativo.',
        'Autorização e auditoria das operações administrativas foram centralizadas.',
        'Catálogo de itens recebeu melhorias de busca e edição no painel administrativo.',
        'Abertura de baús e concessão de itens receberam proteções transacionais e de idempotência para evitar recompensas duplicadas em novas tentativas.'
      ]}
    ]
  },
  {
    id: 'update-event-reward-mail',
    type: 'ATUALIZAÇÃO',
    title: 'Correio — Premiações de eventos chegaram ao Mundo Digital',
    summary: 'Receba Bits e itens pelo Correio, confira a validade e resgate a premiação uma única vez com seu Digimon ativo.',
    date: '2026-08-19',
    version: 'Correio Sprint 4',
    featured: true,
    content: [
      { heading: 'Recompensas direto no Correio', paragraphs: [
        'Eventos e campanhas especiais agora podem enviar premiações diretamente para a Entrada do Correio. A mensagem mostra o que está disponível antes do resgate, para que você possa conferir a recompensa com segurança.',
        'A premiação pode conter Bits, um item ou os dois. O texto informa a quantidade, a validade e o que você precisa fazer para receber o prêmio.'
      ]},
      { heading: 'Como resgatar', items: [
        'Abra Mais → Correio e entre na mensagem da premiação.',
        'Confira os Bits, o item, a quantidade e a data limite exibidos na mensagem.',
        'Escolha o Digimon que deverá receber o prêmio como Digimon ativo.',
        'Clique em “Resgatar prêmio”.'
      ], paragraphs: [
        'Os Bits são entregues ao Digimon ativo e o item vai para o inventário dele. Sem Digimon ativo ou depois da validade, o resgate é bloqueado sem consumir a premiação.'
      ]},
      { heading: 'Entrega registrada e única', paragraphs: [
        'Cada premiação só pode ser resgatada uma vez. Depois da entrega, a própria mensagem registra a quantidade de Bits, o item recebido, o Digimon que recebeu e o horário do resgate. Uma nova tentativa não duplica a recompensa.'
      ]},
      { heading: 'Destinatários', paragraphs: [
        'As campanhas podem premiar um jogador específico, todos os membros de um clã ou uma lista de participantes. Cada jogador recebe sua própria mensagem no Correio.'
      ]}
    ]
  },
  {
    id: 'update-auction-house',
    type: 'ATUALIZAÇÃO',
    title: 'Casa de Leilões — O mercado entre jogadores está disponível',
    summary: 'Publique itens, compre recursos de outros jogadores e escolha entre anúncios de 24, 48 ou 72 horas com taxas progressivas.',
    date: '2026-08-17',
    version: 'Marketplace MVP',
    featured: true,
    content: [
      { heading: 'Um novo espaço para negociar', paragraphs: [
        'A Casa de Leilões chegou ao Mundo Digital como o primeiro marketplace entre jogadores. A partir de agora, itens negociáveis podem ser publicados para compra imediata por outros jogadores, sem disputa por lance e sem necessidade de combinar a negociação manualmente.',
        'O sistema foi pensado para facilitar a circulação de materiais, fragmentos, poções e outros recursos. Os Bits continuam vinculados ao Digimon ativo: ele é usado para pagar compras, publicar anúncios e receber o valor líquido das vendas.'
      ]},
      { heading: 'Como publicar um anúncio', items: [
        'Abra Mais → Casa de Leilões e escolha Publicar item.',
        'Selecione um item empilhável e negociável do inventário do Digimon ativo.',
        'Informe a quantidade, o preço por unidade e escolha uma duração de 24, 48 ou 72 horas.',
        'Confirme a publicação. O sistema reserva as unidades e desconta a taxa fixa de 100 Bits.',
        'Cada jogador pode manter até 10 anúncios ativos ao mesmo tempo.'
      ]},
      { heading: 'Duração e taxas', items: [
        '24 horas: comissão de 5% sobre cada compra realizada.',
        '48 horas: comissão de 7,5% sobre cada compra realizada.',
        '72 horas: comissão de 10% sobre cada compra realizada.'
      ], paragraphs: [
        'A comissão escolhida fica registrada no anúncio no momento da publicação e não muda depois. Ela é calculada apenas quando uma compra acontece, inclusive em compras parciais. Por exemplo, uma compra de 1.000 Bits em um anúncio de 48 horas gera uma comissão de 75 Bits, e o vendedor recebe 925 Bits.'
      ]},
      { heading: 'Compras parciais e segurança', items: [
        'O comprador escolhe quantas unidades deseja, respeitando a quantidade disponível.',
        'A compra não pode ultrapassar o limite de pilha do item no inventário do comprador.',
        'O jogador não pode comprar o próprio anúncio.',
        'O anúncio, os Digimons, os Bits e o inventário são protegidos durante a operação para evitar duplicação ou venda da mesma unidade para duas pessoas.'
      ]},
      { heading: 'Cancelamento e expiração', paragraphs: [
        'Na aba Meus anúncios, o vendedor pode abrir a confirmação de cancelamento e devolver as unidades que ainda não foram vendidas. A devolução volta para o Digimon que originou o anúncio, mesmo que o jogador tenha trocado de Digimon ativo depois. A taxa de publicação de 100 Bits não é reembolsada.',
        'Quando o prazo termina, o anúncio deixa de aparecer no Mercado e não pode mais ser comprado. O servidor marca o anúncio como expirado e tenta devolver automaticamente as unidades restantes ao Digimon de origem. Se a pilha estiver cheia, a devolução fica pendente até existir espaço, evitando que os itens sejam descartados.'
      ]},
      { heading: 'Limites do MVP', items: [
        'Equipamentos ainda não podem ser publicados. Eles possuem refinamento, tier, slot e atributos próprios e serão tratados em uma futura evolução do marketplace.',
        'A versão atual não possui leilão por lance, troca direta entre jogadores, ordens de compra ou negociação manual.',
        'O sistema trabalha com compra imediata e itens empilháveis e negociáveis.'
      ]}
    ]
  },
  {
    id: 'patch-0-6-0',
    type: 'PATCH NOTES',
    title: 'Patch 0.6.0 — Boss Mundial do Servidor',
    summary: 'Um único boss compartilhado por todo o servidor: todos os jogadores atacam, acumulam dano global e competem por ranking, com recompensas por hit e bônus final de derrota.',
    date: '2026-08-17',
    version: '0.6.0',
    featured: true,
    content: [
      { heading: 'Boss Mundial', items: [
        'Instância global compartilhada por todos os jogadores do servidor.',
        'Qualquer Digimon, estágio ou nível pode participar.',
        'Limite de 3 ataques por dia, consumindo energia.',
        'HP coletivo que reduz a cada ataque; ranking de dano global.',
        'Recompensas de XP/Bits a cada hit e bônus extra ao final do combate.',
        'Novo menu “Boss Mundial” na seção “Mais” do jogo.'
      ]},
      { heading: 'Administração', items: [
        'Comando admin para resetar tentativas diárias do Boss Mundial.',
        'Boss Mundial fica separado da lista de bosses normais e da Raid de Clã.'
      ]}
    ]
  },
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