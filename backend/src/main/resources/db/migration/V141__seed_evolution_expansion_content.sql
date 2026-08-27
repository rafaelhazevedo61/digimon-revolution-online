-- Conteúdo progressivo da primeira expansão de linhas evolutivas.
-- Mantém-se inativo até a liberação manual pelo administrador.
-- V27 inseriu IDs manualmente e deixou a sequence desalinhada; sincroniza-a
-- antes de usar o BIGSERIAL para evitar colisão de chave primária em bancos existentes.
SELECT setval(
    pg_get_serial_sequence('available_contents', 'id'),
    COALESCE(MAX(id), 0) + 1,
    FALSE
)
FROM available_contents;

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
