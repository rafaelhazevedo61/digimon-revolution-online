# Convenções de Javadoc do backend

## Objetivo

O Javadoc do Digimon Revolution Online deve explicar o contrato e a intenção do código. Ele deve registrar as regras de negócio, pré-condições, efeitos colaterais, garantias de idempotência, comportamento transacional, limites e erros relevantes que não são óbvios apenas pelo nome da classe ou do método.

O Javadoc não deve descrever linha por linha a implementação. Quando a implementação mudar, a documentação deve continuar válida enquanto o contrato externo permanecer o mesmo.

## Idioma e nomenclatura

Os textos devem ser escritos em Português Brasileiro. Nomes de classes, métodos, propriedades, enums, estágios de Digimon e termos oficiais do código devem permanecer exatamente como aparecem na implementação. Os termos `Baby`, `Rookie`, `Champion`, `Ultimate`, `Mega`, `Vaccine`, `Data`, `Virus` e `Free` não devem ser traduzidos.

## O que documentar

Classes públicas devem explicar sua responsabilidade e, quando aplicável, o ciclo de vida ou as invariantes que representam. Use cases públicos devem documentar a operação, as pré-condições, os efeitos persistentes, as regras de autorização e as exceções relevantes. Controllers devem registrar o propósito da rota, o tipo de usuário autorizado e as regras principais de entrada. DTOs devem explicar campos obrigatórios, limites e relações entre propriedades. Repositories devem registrar consultas com ordenação especial, locks, idempotência e restrições de concorrência.

Métodos privados só precisam de Javadoc quando encapsulam uma regra de negócio não trivial ou uma transformação cujo motivo não seja evidente. Getters, setters, construtores triviais e implementações óbvias de métodos sobrescritos não devem receber comentários artificiais.

## Tags obrigatórias quando aplicáveis

Use `@param` para parâmetros cujo significado ou unidade não seja óbvio, `@return` para resultados não triviais, `@throws` para exceções que fazem parte do contrato, `@see` para apontar para uma regra relacionada e `{@code ...}` para nomes de código. Use `<p>` para separar parágrafos e `<strong>` somente quando a ênfase fizer sentido na documentação gerada.

Exemplo de use case:

```java
/**
 * Cria uma premiação individual por destinatário.
 *
 * <p>A combinação de {@code sourceType}, {@code sourceId} e jogador é
 * idempotente. Se a mesma origem for processada novamente, o jogador que já
 * possui a premiação é ignorado e não recebe uma segunda entrega.</p>
 *
 * @param token token JWT de um usuário administrativo
 * @param request conteúdo, validade e destinatários da premiação
 * @return resumo com as premiações criadas, ignoradas e seus identificadores
 * @throws ForbiddenException quando o usuário autenticado não é ADMIN
 * @throws ConflictException quando o conteúdo ou os destinatários são inválidos
 */
```

Exemplo de regra de domínio:

```java
/**
 * Informa se a premiação ainda pode ser resgatada no instante informado.
 *
 * @param now instante usado para validar a validade do prêmio
 * @return {@code true} somente quando o status é {@code PENDING} e o prazo
 *         ainda não terminou
 */
```

## Regras específicas do DRO

Documente explicitamente quando uma operação:

- exige usuário `ADMIN` ou pertence somente ao destinatário autenticado;
- altera Bits, inventário, Digimon ativo, clã ou mensagem de Correio;
- precisa ser executada dentro de uma transação;
- usa lock pessimista para impedir resgate ou transferência concorrente;
- é idempotente e qual combinação de dados define a idempotência;
- aplica limite de quantidade, validade, paginação ou tamanho de texto;
- altera o estado de uma entidade e quais transições são permitidas;
- pode falhar sem consumir o recurso do jogador.

## Critério de revisão

Uma PR de funcionalidade deve atualizar o Javadoc quando alterar uma regra documentada, um limite, um estado, uma exceção ou o efeito persistente de uma operação. O revisor deve rejeitar documentação que apenas repita o nome do método ou que prometa um comportamento não garantido pelos testes e pela implementação.

A geração do Javadoc será inicialmente uma validação informativa. Para gerar a documentação localmente, execute:

```bash
cd backend
./mvnw -DskipTests javadoc:javadoc
```

O resultado ficará em `backend/target/reports/apidocs`. Avisos de classes ainda não documentadas podem existir durante a cobertura progressiva, mas erros de sintaxe, links inválidos ou falhas nos módulos já documentados devem ser corrigidos na mesma PR. A exigência de documentação para novos contratos públicos pode ser ativada progressivamente depois que os módulos principais estiverem cobertos.
