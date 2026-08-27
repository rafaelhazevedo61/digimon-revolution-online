package com.dro.modules.digitama.application;

import com.dro.modules.digitama.api.dto.response.AvailableDigitamaPoolResponse;
import com.dro.modules.digitama.domain.DigitamaPool;
import com.dro.modules.digitama.infra.DigitamaPoolRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetAvailableDigitamaPoolsUseCaseTest {

    @Test
    void keepsAnEmptyPoolVisibleButNotSelectable() {
        DigitamaPoolRepository repository = mock(DigitamaPoolRepository.class);
        DigitamaPoolEligibilityService eligibilityService = mock(DigitamaPoolEligibilityService.class);
        DigitamaPool steelPool = DigitamaPool.builder()
                .code("DIGITAMA_STEEL")
                .name("Digitama de Metal")
                .description("Pool de Digimons BABY do elemento metal.")
                .entries(List.of())
                .build();
        when(repository.findByActiveTrueAndContentActiveTrue()).thenReturn(List.of(steelPool));
        when(eligibilityService.getEligibleEntries(steelPool)).thenReturn(List.of());

        List<AvailableDigitamaPoolResponse> result = new GetAvailableDigitamaPoolsUseCase(
                repository,
                eligibilityService
        ).execute();

        assertEquals(1, result.size());
        assertEquals("STEEL", result.get(0).type());
        assertEquals("STEEL", result.get(0).element());
        assertFalse(result.get(0).selectable());
        assertNotNull(result.get(0).lockedReason());
        assertEquals(List.of(), result.get(0).entries());
    }
}
