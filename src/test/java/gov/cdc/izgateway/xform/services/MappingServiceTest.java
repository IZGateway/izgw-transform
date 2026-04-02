package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.xform.model.Code;
import gov.cdc.izgateway.xform.model.Mapping;
import gov.cdc.izgateway.xform.repository.RepositoryFactory;
import gov.cdc.izgateway.xform.repository.XformRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MappingService}, focusing on duplicate detection
 * behavior with the {@code notes} field.
 */
@ExtendWith(MockitoExtension.class)
class MappingServiceTest {

    @Mock
    private XformRepository<Mapping> mappingRepository;

    @Mock
    private RepositoryFactory repositoryFactory;

    private MappingService mappingService;

    private static final UUID ORG_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(repositoryFactory.mappingRepository()).thenReturn(mappingRepository);
        mappingService = new MappingService(repositoryFactory);
    }

    private Mapping createMapping(String codeSystem, String code,
                                  String targetCodeSystem, String targetCode,
                                  String notes) {
        Mapping mapping = new Mapping();
        mapping.setId(UUID.randomUUID());
        mapping.setActive(true);
        mapping.setOrganizationId(ORG_ID);
        mapping.setCodeSystem(codeSystem);
        mapping.setCode(code);
        mapping.setTargetCodeSystem(targetCodeSystem);
        mapping.setTargetCode(targetCode);
        mapping.setNotes(notes);
        return mapping;
    }

    @Test
    void getMapping_withNotes_returnsMapping() {
        Mapping existing = createMapping("CDCREC", "2106-3", "CDCREC3", "NEWCODE",
                "A note about this mapping");

        when(mappingRepository.getEntitySet())
                .thenReturn(new LinkedHashSet<>(Set.of(existing)));

        Mapping result = mappingService.getMapping(
                ORG_ID,
                new Code("2106-3", "CDCREC"));

        assertNotNull(result);
        assertEquals("A note about this mapping", result.getNotes());
    }

    @Test
    void getMapping_withoutNotes_returnsMapping() {
        Mapping existing = createMapping("CDCREC", "2106-3", "CDCREC3", "NEWCODE", null);

        when(mappingRepository.getEntitySet())
                .thenReturn(new LinkedHashSet<>(Set.of(existing)));

        Mapping result = mappingService.getMapping(
                ORG_ID,
                new Code("2106-3", "CDCREC"));

        assertNotNull(result);
        assertNull(result.getNotes());
    }
}
