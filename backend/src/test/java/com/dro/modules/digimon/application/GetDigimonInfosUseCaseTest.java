package com.dro.modules.digimon.application;

import com.dro.modules.digitama.application.DigitamaPoolEligibilityService;
import com.dro.modules.digitama.domain.DigitamaPool;
import com.dro.modules.digitama.domain.DigitamaPoolEntry;
import com.dro.modules.digitama.infra.DigitamaPoolRepository;
import com.dro.modules.digimon.api.dto.response.DigimonInfoPageResponse;
import com.dro.modules.digimon.domain.DigimonInfos;
import com.dro.modules.digimon.domain.enums.Attribute;
import com.dro.modules.digimon.domain.enums.Element;
import com.dro.modules.digimon.domain.enums.Species;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.digimon.infra.DigimonInfosRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDigimonInfosUseCaseTest {

    @Mock
    private DigimonInfosRepository digimonInfosRepository;

    @Mock
    private DigitamaPoolRepository digitamaPoolRepository;

    @Mock
    private DigitamaPoolEligibilityService digitamaPoolEligibilityService;

    @Test
    void includesAllEligibleDigitamaOriginsForBaby() {
        DigimonInfos baby = digimonInfo(1L, "Botamon", Stage.BABY);
        DigitamaPool starter = DigitamaPool.builder().code("DIGITAMA_STARTER").name("Digitama Inicial").build();
        DigitamaPool fire = DigitamaPool.builder().code("DIGITAMA_FIRE").name("Digitama de Fogo").build();
        DigitamaPoolEntry starterEntry = DigitamaPoolEntry.builder().digimonInfo(baby).weight(10).active(true).build();
        DigitamaPoolEntry fireEntry = DigitamaPoolEntry.builder().digimonInfo(baby).weight(10).active(true).build();
        PageRequest pageRequest = PageRequest.of(0, 20);

        when(digitamaPoolRepository.findByActiveTrueAndContentActiveTrue()).thenReturn(List.of(starter, fire));
        when(digitamaPoolEligibilityService.getEligibleEntries(starter)).thenReturn(List.of(starterEntry));
        when(digitamaPoolEligibilityService.getEligibleEntries(fire)).thenReturn(List.of(fireEntry));
        when(digimonInfosRepository.findAll(org.mockito.ArgumentMatchers.<Specification<DigimonInfos>>any(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(baby), pageRequest, 1));

        DigimonInfoPageResponse response = new GetDigimonInfosUseCase(
                digimonInfosRepository, digitamaPoolRepository, digitamaPoolEligibilityService
        ).execute(null, null, null, null, null, pageRequest);

        assertEquals(2, response.items().get(0).digitamaOrigins().size());
        assertEquals("DIGITAMA_STARTER", response.items().get(0).digitamaOrigins().get(0).code());
        assertEquals("Digitama de Fogo", response.items().get(0).digitamaOrigins().get(1).name());
    }

    private DigimonInfos digimonInfo(Long id, String name, Stage stage) {
        DigimonInfos info = new DigimonInfos();
        info.setId(id);
        info.setName(name);
        info.setStage(stage);
        info.setAttribute(Attribute.DATA);
        info.setElement(Element.NEUTRAL);
        info.setSpecie(Species.DRAGON);
        info.setBaseHp(10);
        info.setBaseAtk(5);
        info.setBaseDef(5);
        return info;
    }
}
