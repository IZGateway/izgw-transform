package gov.cdc.izgateway.xform.model;

import gov.cdc.izgateway.model.IDestination;
import gov.cdc.izgateway.model.IDestinationId;

import java.util.Date;

@lombok.Data
public class Destination implements IDestination {
    private String facilityId;
    private IDestinationId id;
    private Date maintEnd;
    private String maintReason;
    private Date maintStart;
    private String msh22;
    private String msh3;
    private String msh4;
    private String msh5;
    private String msh6;
    private String password;
    private String rxa11;
    private String username;
    private String destUri;
    private String destVersion;
    private int jurisdictionId;
    private int destTypeId;
    private boolean underMaintenance;
    private String maintenanceDetail;
    private boolean is2011;
    private boolean is2014;
    private boolean isHub = true;
    private boolean isDex;
    private String destinationUri;
    private String destinationId;
    private String destId;
    private String destType;
    private String jurisdictionName;
    private String jurisdictionDesc;

    @Override
    public IDestination safeCopy() {
        return null;
    }
}
