BEGIN;

-- Corrige apenas as imagens ausentes ou quebradas dos Digimons solicitados.
-- As URLs são públicas e foram validadas antes da criação desta migration.
UPDATE digimon_infos
SET image_url = CASE name
    WHEN 'SaberLeomon' THEN 'https://digi-api.com/images/digimon/w/Saber_Leomon.png'
    WHEN 'Shoutmon X7 (Superior Mode)' THEN 'https://digi-api.com/images/digimon/w/Shoutmon_X7(Superior_Mode).png'
    WHEN 'TigerVespamon' THEN 'https://digi-api.com/images/digimon/w/Tiger_Vespamon.png'
    WHEN 'Candlemon' THEN 'https://digimon.shadowsmith.com/img/candlemon.jpg'
    WHEN 'Salamon' THEN 'https://digimon.shadowsmith.com/img/salamon.jpg'
    WHEN 'CannonBeemon' THEN 'https://digi-api.com/images/digimon/w/Cannonbeemon.png'
    WHEN 'GrandGalemon' THEN 'https://digi-api.com/images/digimon/w/Grand_Galemon.png'
    WHEN 'GrapLeomon' THEN 'https://static.wikia.nocookie.net/digimon/images/1/1f/GrapLeomon_b.jpg/revision/latest?cb=20120824210622'
    WHEN 'LadyDevimon' THEN 'https://digi-api.com/images/digimon/w/Lady_Devimon.png'
    WHEN 'LoaderLiomon' THEN 'https://static.wikia.nocookie.net/digimon/images/0/0d/LoaderLiomon_b.jpg/revision/latest?cb=20090128165117'
    WHEN 'MachGaogamon' THEN 'https://digi-api.com/images/digimon/w/Mach_Gaogamon.png'
    WHEN 'MarineDevimon' THEN 'https://static.wikia.nocookie.net/digimon/images/e/e7/MarineDevimon_b.jpg/revision/latest?cb=20090828043054'
    WHEN 'MegaSeadramon' THEN 'https://digi-api.com/images/digimon/w/Mega_Seadramon.png'
    WHEN 'OmegaShoutmon' THEN 'https://digi-api.com/images/digimon/w/Omega_Shoutmon.png'
    WHEN 'SaviorHackmon' THEN 'https://digi-api.com/images/digimon/w/Savior_Hackmon.png'
    WHEN 'ShogunGekomon' THEN 'https://digimon.shadowsmith.com/img/shogungekomon.jpg'
    WHEN 'SkullGreymon' THEN 'https://digi-api.com/images/digimon/w/Skull_Greymon.png'
    WHEN 'SkullMeramon' THEN 'https://digimon.shadowsmith.com/img/skullmeramon.jpg'
    WHEN 'BlackGatomon' THEN 'https://static.wikia.nocookie.net/digimon/images/7/73/BlackGatomon_b.jpg/revision/latest?cb=20160819162816'
    WHEN 'Monmon' THEN 'https://static.wikia.nocookie.net/digimon/images/9/9d/Monmon_b.jpg/revision/latest?cb=20090130133210'
    WHEN 'SymbareAngoramon' THEN 'https://digi-api.com/images/digimon/w/Symbare_Angoramon.png'
    WHEN 'TeslaJellymon' THEN 'https://digi-api.com/images/digimon/w/Tesla_Jellymon.png'
    WHEN 'TiaLudomon' THEN 'https://digi-api.com/images/digimon/w/Tia_Ludomon.png'
    WHEN 'Vermilimon' THEN 'https://digimon.shadowsmith.com/img/vermilimon.jpg'
    WHEN 'BanchoLeomon' THEN 'https://digi-api.com/images/digimon/w/Bancho_Leomon.png'
    WHEN 'Belphemon: Sleep Mode' THEN 'https://digi-api.com/images/digimon/w/Belphemon(Sleep_Mode).png'
    WHEN 'Ebonwumon' THEN 'https://digimon.shadowsmith.com/img/ebonwumon.jpg'
    WHEN 'Gryphonmon' THEN 'https://static.wikia.nocookie.net/digimon/images/3/3f/Gryphonmon_b.jpg/revision/latest?cb=20100319030103'
    WHEN 'HeavyLeomon' THEN 'https://digi-api.com/images/digimon/w/Heavy_Leomon.png'
    WHEN 'Jesmon' THEN 'https://digi-api.com/images/digimon/w/JESmon.png'
    WHEN 'Justimon' THEN 'https://static.wikia.nocookie.net/digimon/images/b/bf/Justimon_%28Blitz_Arm%29_b.jpg/revision/latest?cb=20190226023632'
    WHEN 'MarineAngemon' THEN 'https://digimon.shadowsmith.com/img/marineangemon.jpg'
    WHEN 'MirageGaogamon' THEN 'https://digi-api.com/images/digimon/w/Mirage_Gaogamon.png'
    WHEN 'Ophanimon' THEN 'https://static.wikia.nocookie.net/digimon/images/1/10/Ophanimon_b.jpg/revision/latest?cb=20090227054225'
    WHEN 'QueenBeemon' THEN 'https://digi-api.com/images/digimon/w/Queenbeemon.png'
    WHEN 'RustTyrannomon' THEN 'https://static.wikia.nocookie.net/digimon/images/c/c1/RustTyranomon_b.jpg/revision/latest?cb=20170310061431'
END
WHERE name IN (
    'SaberLeomon',
    'Shoutmon X7 (Superior Mode)',
    'TigerVespamon',
    'Candlemon',
    'Salamon',
    'CannonBeemon',
    'GrandGalemon',
    'GrapLeomon',
    'LadyDevimon',
    'LoaderLiomon',
    'MachGaogamon',
    'MarineDevimon',
    'MegaSeadramon',
    'OmegaShoutmon',
    'SaviorHackmon',
    'ShogunGekomon',
    'SkullGreymon',
    'SkullMeramon',
    'BlackGatomon',
    'Monmon',
    'SymbareAngoramon',
    'TeslaJellymon',
    'TiaLudomon',
    'Vermilimon',
    'BanchoLeomon',
    'Belphemon: Sleep Mode',
    'Ebonwumon',
    'Gryphonmon',
    'HeavyLeomon',
    'Jesmon',
    'Justimon',
    'MarineAngemon',
    'MirageGaogamon',
    'Ophanimon',
    'QueenBeemon',
    'RustTyrannomon'
);

COMMIT;
