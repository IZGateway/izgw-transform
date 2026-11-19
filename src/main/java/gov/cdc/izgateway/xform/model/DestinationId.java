package gov.cdc.izgateway.xform.model;

import gov.cdc.izgateway.model.IDestinationId;

/**
 * The identifier of a destination: a destination id and a destination type.
 */
public class DestinationId implements IDestinationId {
    private String destId;
    private int destType;

    /**
     * Creates an empty destination identifier.
     */
    public DestinationId() {
    }

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
