UPDATE incubations
SET finish_at = started_at + INTERVAL '10 seconds'
WHERE incubator_type = 'INCUBATOR_LEGENDARY'
  AND finish_at <= started_at
  AND status <> 'CLAIMED';
