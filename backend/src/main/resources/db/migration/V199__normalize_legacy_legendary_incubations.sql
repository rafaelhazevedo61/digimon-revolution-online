UPDATE incubations
SET finish_at = started_at + INTERVAL '1 second'
WHERE incubator_type = 'INCUBATOR_LEGENDARY'
  AND finish_at <= started_at
  AND status <> 'CLAIMED';
