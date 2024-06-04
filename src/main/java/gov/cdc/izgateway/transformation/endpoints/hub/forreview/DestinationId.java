package gov.cdc.izgateway.transformation.endpoints.hub.forreview;

import gov.cdc.izgateway.model.IDestinationId;

// TODO: Paul - created this because DestinationId is defined in the hub jar in the db package.
public class DestinationId implements IDestinationId {
    private String destId;
    private int destType;

    @Override
    public String getDestId() {
        return destId;
    }

    @Override
    public int getDestType() {
        return destType;
    }

    @Override
    public void setDestId(String destId) {
        this.destId = destId;
    }

    @Override
    public IDestinationId copy() {
        DestinationId copy = new DestinationId();
        copy.setDestId(this.destId);
        copy.setDestType(this.destType);
        return copy;
    }

    @Override
    public void setDestType(String destType) {
        this.destType = Integer.parseInt(destType);
    }

    @Override
    public void setDestType(int destType) {
        this.destType = destType;
    }
}