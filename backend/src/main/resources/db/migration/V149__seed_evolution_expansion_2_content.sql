-- Conteúdo progressivo da segunda expansão de linhas evolutivas.
-- Permanece inativo até a liberação manual pelo administrador.
INSERT INTO available_contents (code, name, description, active, release_order)
VALUES (
    'EVOLUTION_EXPANSION_2',
    'Expansão Evolutiva II',
    'Trinta e sete novas linhas evolutivas do estágio BABY ao MEGA.',
    FALSE,
    3
)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    release_order = EXCLUDED.release_order;
