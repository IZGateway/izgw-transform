package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.security.IzgPrincipal;
import gov.cdc.izgateway.xform.camel.constants.EndpointUris;
import gov.cdc.izgateway.xform.logging.XformRequestContext;
import gov.cdc.izgateway.xform.model.Pipeline;
import gov.cdc.izgateway.xform.repository.RepositoryFactory;
import gov.cdc.izgateway.xform.repository.XformRepository;
import gov.cdc.izgateway.xform.security.Roles;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PipelineService#getPipelineByOrganizationAndEndpoints}, which is the
 * lookup used on the message-execution path to resolve which pipeline runs for a given
 * Organization + Inbound Endpoint + Outbound Endpoint.
 *
 * <p>
 * These tests focus on the requirement that <strong>inactive pipelines must never be executed</strong>:
 * the lookup must only return a pipeline whose {@code active} flag is {@code Boolean.TRUE}, and must
 * do so in a null-safe manner (an unset {@code active} is treated as not-active rather than throwing).
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class PipelineServiceTest {

    @Mock
    private XformRepository<Pipeline> pipelineRepository;

    @Mock
    private RepositoryFactory repositoryFactory;

    private PipelineService pipelineService;

    private static final UUID ORG_ID = UUID.randomUUID();
    private static final String INBOUND = EndpointUris.IZGTS_IISHubService;
    private static final String OUTBOUND = EndpointUris.IZGHUB_IISHubService;

    @BeforeEach
    void setUp() {
        when(repositoryFactory.pipelineRepository()).thenReturn(pipelineRepository);
        pipelineService = new PipelineService(repositoryFactory);

        // getPipelineByOrganizationAndEndpoints() flows through GenericService.getList(), which
        // consults RequestContext for access control and emits an API read event. Provide an ADMIN
        // principal so all entities are accessible, and disable API logging for this internal call.
        IzgPrincipal admin = new IzgPrincipal() {
            @Override
            public String getSerialNumberHex() {
                return null;
            }
        };
        admin.setName("test-admin");
        admin.setRoles(new HashSet<>(Set.of(Roles.ADMIN)));
        RequestContext.setPrincipal(admin);
        XformRequestContext.disableApiLogging();
    }

    @AfterEach
    void tearDown() {
        RequestContext.clear();
        XformRequestContext.clear();
    }

    private Pipeline pipeline(String inbound, String outbound, Boolean active) {
        Pipeline pipeline = new Pipeline();
        pipeline.setId(UUID.randomUUID());
        pipeline.setPipelineName("pipeline-" + UUID.randomUUID());
        pipeline.setOrganizationId(ORG_ID);
        pipeline.setInboundEndpoint(inbound);
        pipeline.setOutboundEndpoint(outbound);
        pipeline.setActive(active);
        pipeline.setPipes(new ArrayList<>());
        return pipeline;
    }

    private void givenPipelines(Pipeline... pipelines) {
        when(pipelineRepository.getEntitySet()).thenReturn(new LinkedHashSet<>(java.util.Arrays.asList(pipelines)));
    }

    @Test
    void returnsPipeline_whenMatchingAndActive() {
        Pipeline active = pipeline(INBOUND, OUTBOUND, true);
        givenPipelines(active);

        Pipeline result = pipelineService.getPipelineByOrganizationAndEndpoints(ORG_ID, INBOUND, OUTBOUND);

        assertEquals(active.getId(), result.getId());
    }

    @Test
    void returnsNull_whenMatchingPipelineIsInactive() {
        givenPipelines(pipeline(INBOUND, OUTBOUND, false));

        Pipeline result = pipelineService.getPipelineByOrganizationAndEndpoints(ORG_ID, INBOUND, OUTBOUND);

        assertNull(result, "An inactive pipeline must not be resolved for execution");
    }

    @Test
    void returnsNull_whenActiveFlagIsNull() {
        givenPipelines(pipeline(INBOUND, OUTBOUND, null));

        Pipeline result = pipelineService.getPipelineByOrganizationAndEndpoints(ORG_ID, INBOUND, OUTBOUND);

        assertNull(result, "A null active flag must be treated as not-active (null-safe), not throw");
    }

    @Test
    void skipsInactiveDuplicate_andReturnsActiveOne() {
        // Same Organization + Inbound + Outbound on both; the inactive one is listed first to prove
        // the active filter (not iteration order) determines the result.
        Pipeline inactive = pipeline(INBOUND, OUTBOUND, false);
        Pipeline active = pipeline(INBOUND, OUTBOUND, true);
        givenPipelines(inactive, active);

        Pipeline result = pipelineService.getPipelineByOrganizationAndEndpoints(ORG_ID, INBOUND, OUTBOUND);

        assertEquals(active.getId(), result.getId());
    }

    @Test
    void returnsNull_whenEndpointsDoNotMatch() {
        givenPipelines(pipeline(INBOUND, EndpointUris.IIS_IISService, true));

        Pipeline result = pipelineService.getPipelineByOrganizationAndEndpoints(ORG_ID, INBOUND, OUTBOUND);

        assertNull(result, "A pipeline with non-matching endpoints must not be resolved");
    }
}
