# Plano de teste manual — Resgate e finalização do tutorial

## Objetivo

Validar que uma etapa do tutorial é marcada como concluída quando a ação do jogo ocorre, mas sua recompensa só é entregue após o jogador clicar em **Resgatar**. A finalização do tutorial deve ficar disponível somente quando todas as etapas estiverem concluídas e todas as recompensas tiverem sido resgatadas.

## Pré-requisitos

Use a branch do PR do tutorial com a migration V116. Reinicie o backend para o Flyway adicionar `reward_claimed_at` e criar `tutorial_completions`. Use um jogador de teste que ainda não tenha finalizado o tutorial ou limpe apenas os dados desse jogador em um ambiente local de testes.

## Fluxo por etapa

1. Acesse o dashboard e localize o card **Primeiros Passos**.
2. Execute uma ação que conclua uma etapa com recompensa, como concluir uma missão ou comprar um produto.
3. Confirme que a etapa aparece como concluída, mas com o ícone de recompensa pendente e o botão **Resgatar**.
4. Confirme que Bits, experiência ou itens da recompensa não foram concedidos no momento da conclusão da etapa.
5. Clique em **Resgatar**.
6. Confirme que os recursos foram entregues, que o botão mudou para **Resgatada** e que a quantidade da recompensa aparece corretamente no inventário.
7. Tente repetir a mesma chamada de resgate pelo Postman ou cURL. A resposta deve permanecer idempotente e não conceder a recompensa novamente.

## Finalização

Conclua todas as etapas do tutorial. Enquanto existir qualquer recompensa pendente, o card deve informar que é necessário resgatá-la e não deve liberar a finalização.

Depois de resgatar todas as recompensas, deve aparecer o botão **Finalizar tutorial**. Clique nele, confirme o diálogo e verifique que o card **Primeiros Passos** desaparece do dashboard. Atualize a página para confirmar que o tutorial continua finalizado.

A tentativa de chamar `POST /tutorial/finish` antes de todas as etapas ou antes de todos os resgates deve retornar HTTP 400 e não deve criar o registro de finalização.

## Endpoints

```bash
curl --fail-with-body -i -X GET "$BASE_URL/tutorial" \
  -H "Authorization: Bearer $TOKEN"

curl --fail-with-body -i -X POST "$BASE_URL/tutorial/steps/COMPLETE_MISSION/claim" \
  -H "Authorization: Bearer $TOKEN"

curl --fail-with-body -i -X POST "$BASE_URL/tutorial/finish" \
  -H "Authorization: Bearer $TOKEN"
```

## Queries PostgreSQL

Verificar o estado das etapas:

```sql
SELECT
    player_id,
    step,
    completed_at,
    reward_claimed_at
FROM tutorial_progress
WHERE player_id = 'ID_DO_JOGADOR'
ORDER BY completed_at, step;
```

Uma etapa concluída aguardando resgate possui `completed_at` preenchido e `reward_claimed_at` nulo. Após o resgate, os dois campos ficam preenchidos.

Verificar a finalização:

```sql
SELECT player_id, finished_at
FROM tutorial_completions
WHERE player_id = 'ID_DO_JOGADOR';
```

Essa consulta deve retornar uma linha somente depois que o jogador concluir todas as etapas e resgatar todas as recompensas.

## Compatibilidade com dados antigos

A V116 marca como resgatadas as etapas que já existiam antes da alteração, porque suas recompensas foram concedidas automaticamente pelo fluxo anterior. Isso evita duplicidade de Bits e itens para jogadores antigos. Somente etapas concluídas depois da V116 terão resgate manual pendente.

## Critérios de aceite

- [ ] Concluir uma etapa não entrega mais automaticamente sua recompensa.
- [ ] Cada etapa concluída com recompensa exibe seu próprio botão **Resgatar**.
- [ ] Resgatar uma etapa entrega exatamente sua recompensa.
- [ ] Repetir o resgate não duplica Bits, experiência ou itens.
- [ ] A finalização não aparece enquanto houver recompensas pendentes.
- [ ] A finalização aparece após todas as etapas e recompensas serem concluídas.
- [ ] Confirmar a finalização remove o card do dashboard.
- [ ] Atualizar a página mantém o tutorial finalizado.
- [ ] Migrations anteriores não foram alteradas.


## Atualização do Guia do Treinador

- Conta nova: manter seis etapas e recompensas do tutorial. A etapa de nascimento leva à tela inicial, não à incubação posterior.
- Sem nascimento: textos do guia visíveis, mas leituras bloqueadas; o servidor também rejeita POST direto de tópico indisponível.
- Após nascer: liberar coleção, conquistas, energia e incubação, sugerindo um conteúdo por vez.
- Após resgatar a primeira missão: liberar times, chefes, arena, torre e classificação.
- Após evoluir: liberar ferreiro, renascimento e VIP. Conferir que isso não ignora os requisitos reais dessas atividades.
- Marcar Entendi, atualizar a página e acessar em outro dispositivo: leitura permanece; nenhum item, Bit, evento de gameplay ou ponto é concedido.
- Fazer POST repetido e dar duplo clique: uma leitura, mesma contagem, sem duplicatas.
- Finalizar o tutorial inicial com leituras opcionais pendentes: finalizar normalmente, sem obrigar torre, arena ou VIP.
- Conta que já finalizou: dashboard permanece sem o card antigo; guia acessível em Mais → Conta, sem repetir recompensas.
- Reabrir tópicos lidos e conferir a navegação da classificação na aba Jogadores.
- Falha de conexão ao ler: mostrar erro e permitir repetir. Navegar durante uma resposta pendente não pode sobrescrever outra página.
- Conferir grupos recolhíveis, botões, descrições e preferência de minimizar o card em celular e desktop.
