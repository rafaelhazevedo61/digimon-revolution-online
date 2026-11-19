-- Remove a URL de imagem do boss de raid até termos uma URL confiável;
-- o frontend exibe um placeholder quando image_url é nulo.
UPDATE boss_definitions SET image_url = NULL WHERE code = 'CLAN_RAID_OMEGAMON';
