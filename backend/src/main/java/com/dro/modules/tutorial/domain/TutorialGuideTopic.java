package com.dro.modules.tutorial.domain;

/** Conteúdos educativos opcionais; reconhecer uma leitura não comprova uma ação de gameplay. */
public enum TutorialGuideTopic {
    COLLECTION("Seu progresso", "Conheça a coleção", "Cada combinação de espécie e raridade obtida pode ser registrada automaticamente na coleção. Repetir a mesma combinação não soma pontos; reunir as quatro raridades de uma espécie libera sua maestria. Consulte os registros e os bônus no álbum.", "collection", TutorialStep.HATCH_DIGIMON),
    ACHIEVEMENTS("Seu progresso", "Acompanhe suas conquistas", "Conquistas reconhecem atividades do jogo e oferecem Bits e itens. A atualização é periódica: uma ação pode levar um pouco para aparecer. Quando a meta estiver concluída, resgate a recompensa na página de Conquistas.", "achievements", TutorialStep.HATCH_DIGIMON),
    ENERGY("Seus parceiros", "Cuide da energia", "Quando o consumo de energia estiver ativo, confira a energia antes de iniciar atividades. Célula de Energia e Carga de Energia Total ajudam na recuperação. Veja no jogo a quantidade recuperada, o custo e a disponibilidade de cada atividade.", "inventory", TutorialStep.HATCH_DIGIMON),
    INCUBATION("Seus parceiros", "Amplie sua equipe", "Depois do primeiro parceiro, novos Digitamas podem ser incubados. Confira os recursos e o tempo de cada incubação e resgate o Digimon quando estiver pronto. O primeiro nascimento acontece na tela inicial, não nesta incubação.", "incubation", TutorialStep.HATCH_DIGIMON),
    TEAMS("Seus parceiros", "Monte seu primeiro time", "Em Meus Times, reúna de um a três Digimons e escolha o capitão. As formações podem ser usadas nas atividades compatíveis, incluindo a Torre Infinita. Confira os membros disponíveis e as missões pendentes antes de enviá-los.", "mission-teams", TutorialStep.COMPLETE_MISSION),
    BOSSES("Novos desafios", "Prepare-se para os chefes", "Chefes têm requisitos e recompensas próprios. Confira os atributos, a disponibilidade e o intervalo entre desafios antes de lutar. Também existem Chefe Mundial e incursões de clã, acessíveis pelo menu Mais e pelo seu clã.", "bosses", TutorialStep.COMPLETE_MISSION),
    ARENA("Novos desafios", "Conheça a arena", "Na Arena você desafia outros adversários e acompanha sua classificação. Confira os limites, os pontos e as recompensas na própria tela. Uma vitória na arena não é o mesmo que avançar um andar na torre.", "arena", TutorialStep.COMPLETE_MISSION),
    TOWER("Novos desafios", "Desafie a Torre Infinita", "Use um time de um a três Digimons para subir andares permanentes. Vitória avança e entrega a recompensa; derrota mantém seu progresso. Há chefes a cada cinco andares e marcos a cada dez. Não consome energia; respeite o intervalo informado na tela. O resultado permite conferir o HP final dos membros.", "tower", TutorialStep.COMPLETE_MISSION),
    RANKING("Seu progresso", "Compare sua jornada", "Em Classificação, a aba Jogadores soma o progresso de coleção, conquistas e torre. A atualização é periódica. Alterne entre Geral e Progresso semanal, busque amigos e clique nos nomes para ver os perfis públicos. A variação diária precisa de um registro válido de ontem.", "ranking", TutorialStep.COMPLETE_MISSION),
    FORGE("Próximos objetivos", "Aprimore seus equipamentos", "No Ferreiro, confira materiais, Bits, chances e riscos antes de refinar ou ascender um equipamento. Bônus de peças e conjuntos ajudam nos desafios. Não gaste recursos sem verificar as condições exibidas.", "forge", TutorialStep.EVOLVE_DIGIMON),
    REBIRTH("Próximos objetivos", "Planeje o renascimento", "O Renascimento é uma etapa posterior de progressão. Leia os requisitos, o que será mantido e o que será alterado antes de confirmar. Você não precisa renascer para concluir os primeiros passos ou ler este guia.", "rebirth", TutorialStep.EVOLVE_DIGIMON),
    VIP("Próximos objetivos", "Conheça os benefícios opcionais", "A página VIP reúne planos e benefícios, incluindo automações e vantagens de tempo. Confira as condições atuais na própria página. VIP é opcional e não é necessário para concluir o tutorial ou o guia.", "vip", TutorialStep.EVOLVE_DIGIMON);

    private final String group;
    private final String title;
    private final String description;
    private final String route;
    private final TutorialStep prerequisite;
    TutorialGuideTopic(String group, String title, String description, String route, TutorialStep prerequisite) {
        this.group = group; this.title = title; this.description = description;
        this.route = route; this.prerequisite = prerequisite;
    }
    public String getGroup() { return group; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getRoute() { return route; }
    public TutorialStep getPrerequisite() { return prerequisite; }
}
