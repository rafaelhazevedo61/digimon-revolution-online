-- Define todas as missões existentes para uma duração uniforme de 10 minutos.
-- duration_seconds é persistido em segundos; não há filtro para incluir missões ativas e inativas.
BEGIN;

UPDATE mission_definitions
SET duration_seconds = 10 * 60;

COMMIT;
