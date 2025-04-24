package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.xform.logging.ApiEventLogger;
import gov.cdc.izgateway.xform.model.Pipeline;
import gov.cdc.izgateway.xform.model.User;
import gov.cdc.izgateway.xform.repository.XformRepository;
import gov.cdc.izgateway.xform.security.XformPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class PipelineService extends GenericService<Pipeline>{
    private final UserService userService;

    @Autowired
    public PipelineService(XformRepository<Pipeline> repo, UserService userService) {
        super(repo);
        this.userService = userService;
    }

    @Override
    public List<Pipeline> getList() {
        Set<UUID> allowedOrgIds = getAllowedOrganizationIds();

        ArrayList<Pipeline> list = new ArrayList<>(repo.getEntitySet());

        // Filter the pipelines based on the allowed organization IDs
        list.removeIf(pipeline -> !allowedOrgIds.contains(pipeline.getOrganizationId()));
        ApiEventLogger.logReadEvent(list);

        return list;
    }

    @Override
    public Pipeline getObject(UUID id) {
        Pipeline existing = repo.getEntity(id);

        if (existing == null) {
            return null;
        }

        // Check if the pipeline's organization ID is in the allowed organization IDs
        Set<UUID> allowedOrgIds = getAllowedOrganizationIds();
        if (!allowedOrgIds.contains(existing.getOrganizationId())) {
            return null;
        }

        ApiEventLogger.logReadEvent(existing);

        return existing;
    }

    @Override
    public void create(Pipeline obj) {
        Pipeline existing = repo.getEntity(obj.getId());

        if (existing != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Item already exists");
        }

        // Check if the pipeline's organization ID is in the allowed organization IDs
        Set<UUID> allowedOrgIds = getAllowedOrganizationIds();
        if (!allowedOrgIds.contains(obj.getOrganizationId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to create this pipeline");
        }

        repo.createEntity(obj);

        ApiEventLogger.logCreateEvent(obj);
    }

    @Override
    public void delete(UUID id) {
        Pipeline solution = getObject(id);
        if (solution == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found");
        }

        // Check if the pipeline's organization ID is in the allowed organization IDs
        Set<UUID> allowedOrgIds = getAllowedOrganizationIds();
        if (!allowedOrgIds.contains(solution.getOrganizationId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to delete this pipeline");
        }

        repo.deleteEntity(id);

        ApiEventLogger.logDeleteEvent(solution);
    }

    public Pipeline getPipelineByOrganizationAndEndpoints(UUID organizationId, String inboundEndpoint, String outboundEndpoint) {
        return getList()
                .stream()
                .filter(
                        p -> p.getOrganizationId().equals(organizationId)
                                && p.getInboundEndpoint().equals(inboundEndpoint)
                                && p.getOutboundEndpoint().equals(outboundEndpoint)
                ).findFirst().orElse(null);
    }

    private Set<UUID> getAllowedOrganizationIds() {
//        // Retrieve the current user's allowed organization IDs
//        String userName = RequestContext.getPrincipal().getName();
//        User user = userService.getUserByUserName(userName);
//        return user.getOrganizationIds();
        XformPrincipal principal = (XformPrincipal) RequestContext.getPrincipal();
        return principal.getAllowedOrganizationIds();
    }
}
