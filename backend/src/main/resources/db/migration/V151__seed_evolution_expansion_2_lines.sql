INSERT INTO evolution_lines (code, name, description, content_id, active)
SELECT data.code, data.name, data.description, content.id, TRUE
FROM (
    VALUES
    ('ALGOMON_LINE_1', 'Linha Algomon', 'Linha evolutiva de mutação algorítmica e controle de dados.'),
    ('BOMBMON_LINE_1', 'Linha Bombmon', 'Linha evolutiva militar de explosão, armadura e combate pesado.'),
    ('BOMMON_LINE_1', 'Linha Bommon', 'Linha evolutiva de demônio ígneo e poder das trevas.'),
    ('BUBBMON_LINE_1', 'Linha Bubbmon', 'Linha evolutiva aquática de anfíbio a entidade marinha.'),
    ('CHIBICKMON_LINE_1', 'Linha Chibickmon', 'Linha evolutiva de pequenos guerreiros e fusão Xros.'),
    ('CHOROMON_LINE_1', 'Linha Choromon', 'Linha evolutiva de malware elétrico e máquinas sombrias.'),
    ('COTSUCOMON_LINE_1', 'Linha Cotsucomon', 'Linha evolutiva de armas vivas e cavaleiros de metal.'),
    ('CURIMON_LINE_1', 'Linha Curimon', 'Linha evolutiva de primata brincalhão a senhor do entretenimento.'),
    ('DOKIMON_LINE_1', 'Linha Dokimon', 'Linha evolutiva de inseto mecânico e androide de combate.'),
    ('FUFUMON_LINE_1', 'Linha Fufumon', 'Linha evolutiva de dragão luminoso e cavaleiro dracônico.'),
    ('FUKAMON_LINE_1', 'Linha Fukamon', 'Linha evolutiva de réptil aquático e dragão marinho.'),
    ('FUSAMON_LINE_1', 'Linha Fusamon', 'Linha evolutiva de fera alada e demônio felino.'),
    ('KEEMON_LINE_1', 'Linha Keemon', 'Linha evolutiva de lobo demoníaco e força destrutiva.'),
    ('KEKOMON_LINE_1', 'Linha Kekomon', 'Linha evolutiva aquática e felina de combate.'),
    ('KETOMON_LINE_1', 'Linha Ketomon', 'Linha evolutiva de urso, fera e leão marcial.'),
    ('MOKUMON_LINE_1', 'Linha Mokumon', 'Linha evolutiva solar de fera flamejante.'),
    ('NYOKIMON_LINE_1', 'Linha Nyokimon', 'Linha evolutiva vegetal de broto a rosa guerreira.'),
    ('PAFUMON_LINE_1', 'Linha Pafumon', 'Linha evolutiva de fera veloz e combate aéreo.'),
    ('PAOMON_LINE_1', 'Linha Paomon', 'Linha evolutiva canídea de guarda e submundo.'),
    ('PETI_MERAMON_LINE_1', 'Linha Peti Meramon', 'Linha evolutiva de chama, fogo espectral e energia elétrica.'),
    ('PETITMON_LINE_1', 'Linha Petitmon', 'Linha evolutiva de dragão sagrado e cavaleiro santo.'),
    ('PIPIMON_LINE_1', 'Linha Pipimon', 'Linha evolutiva lunar de fera e magia astral.'),
    ('PITCHMON_LINE_1', 'Linha Pitchmon', 'Linha evolutiva de crustáceo, pirata e monstro marinho.'),
    ('POPOMON_LINE_1', 'Linha Popomon', 'Linha evolutiva de fera vegetal a leão mecânico pesado.'),
    ('PUPUMON_LINE_1', 'Linha Pupumon', 'Linha evolutiva de abelha e artilharia aérea.'),
    ('PURURUMON_LINE_1', 'Linha Pururumon', 'Linha evolutiva de ave sagrada e guerreira celestial.'),
    ('PUTTIMON_LINE_1', 'Linha Puttimon', 'Linha evolutiva angelical de luz e proteção.'),
    ('PUWAMON_LINE_1', 'Linha Puwamon', 'Linha evolutiva de ave pré-histórica e grifo.'),
    ('PUYOMON_LINE_1', 'Linha Puyomon', 'Linha evolutiva aquática de energia elétrica e mar profundo.'),
    ('PYONMON_LINE_1', 'Linha Pyonmon', 'Linha evolutiva de fera espiritual e coelho de combate.'),
    ('RELEMON_LINE_1', 'Linha Relemon', 'Linha evolutiva mística de raposa e sacerdotisa.'),
    ('SUNAMON_LINE_1', 'Linha Sunamon', 'Linha evolutiva terrestre de escavador a guardião ancestral.'),
    ('TOMORIMON_LINE_1', 'Linha Tomorimon', 'Linha evolutiva de inseto adaptativo e colmeia mecânica.'),
    ('TORIKARA_BALLMON_LINE_1', 'Linha Torikara Ballmon', 'Linha evolutiva de espadachim e cavaleiro sagrado.'),
    ('TSUBUMON_LINE_1', 'Linha Tsubumon', 'Linha evolutiva de armadura terrestre e tiranossauro.'),
    ('YOLKMON_LINE_1', 'Linha Yolkmon', 'Linha evolutiva aérea de pterossauro e dragão do vento.'),
    ('ZURUMON_LINE_1', 'Linha Zurumon', 'Linha evolutiva fantasmagórica de trevas e ilusão.')
) AS data(code, name, description)
JOIN available_contents content ON content.code = 'EVOLUTION_EXPANSION_2'
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    content_id = EXCLUDED.content_id,
    active = EXCLUDED.active;
