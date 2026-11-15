# Observabilidade das automações

Esta branch adiciona uma primeira camada de observabilidade para os workers de missão e incubação. O backend mantém o comportamento funcional existente, mas passa a emitir métricas sobre execuções, duração, falhas, pausas, retries e mensagens de sistema.

## Endpoints

O Actuator expõe `health`, `info`, `metrics` e `prometheus`. O endpoint Prometheus fica disponível em `/actuator/prometheus`. O health check continua sem detalhes internos por padrão.

Em produção, o acesso ao Actuator deve ser restrito por rede, proxy reverso ou autenticação. Não é recomendado deixar `/actuator/metrics` e `/actuator/prometheus` publicamente acessíveis sem proteção.

## Métricas de automação

| Métrica | Tags | Finalidade |
|---|---|---|
| `dro_automation_runs_total` | `mode` | Total de itens processados pelos workers. |
| `dro_automation_cycle_duration_seconds` | `mode` | Duração dos ciclos individuais. |
| `dro_automation_failures_total` | `mode`, `code` | Falhas por código tipado. |
| `dro_automation_pauses_total` | `mode`, `reason` | Pausas automáticas por motivo. |
| `dro_automation_retries_total` | `mode` | Retries registrados. |
| `dro_automation_system_mail_total` | `mode`, `reason` | Mensagens de sistema geradas pelos workers. |

Os valores de `mode`, `code` e `reason` são normalizados e devem permanecer em um conjunto pequeno. Não devem ser adicionados `playerId`, nome de item ou ID de missão como tags.

## Opção rápida: Sentry

Para capturar exceções do backend e do frontend, crie um projeto Java e um projeto JavaScript no Sentry, copie os DSNs e configure-os como secrets do ambiente de produção. O Sentry é complementar às métricas deste documento: ele ajuda a investigar erros individuais, enquanto Prometheus/Grafana ou SigNoz ajudam a acompanhar volume e tendência.

## Opção com Grafana Cloud

Configure um agente ou coletor para fazer scrape de `/actuator/prometheus`, usando o endereço interno do backend. Cadastre as credenciais do stack como secrets e envie métricas para o endpoint Prometheus remoto. Crie alertas para aumento de `dro_automation_failures_total`, pausas por capacidade e duração p95 elevada.

## Opção self-hosted

Para SigNoz ou outra plataforma OpenTelemetry, a instrumentação pode ser ampliada com o Java Agent. O agente deve ser instalado no processo do backend por configuração de startup, sem colocar o arquivo do agente no frontend ou no repositório público. O endpoint Prometheus continua útil para métricas locais mesmo quando traces forem exportados por OpenTelemetry.

## Checklist de produção

Antes de ativar a coleta, validar que o endpoint de health retorna sucesso, que `/actuator/prometheus` contém as métricas `dro_automation_*`, que o scraper consegue alcançar o backend e que os alertas não usam tags de alta cardinalidade. Depois, executar uma missão e uma incubação de teste, verificar um ciclo bem-sucedido e provocar uma pausa controlada por capacidade para confirmar a métrica e o Correio.
