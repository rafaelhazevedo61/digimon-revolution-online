BEGIN;

-- Conteúdo progressivo da primeira expansão de linhas evolutivas.
-- Mantém-se inativo até a liberação manual pelo administrador.
INSERT INTO available_contents (
    code,
    name,
    description,
    active,
    release_order
)
VALUES (
    'EVOLUTION_EXPANSION_1',
    'Expansão Evolutiva I',
    'Dez novas linhas evolutivas do estágio BABY ao MEGA.',
    FALSE,
    2
)
ON CONFLICT (code) DO NOTHING;

COMMIT;
