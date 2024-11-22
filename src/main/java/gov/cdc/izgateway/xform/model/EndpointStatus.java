package gov.cdc.izgateway.xform.model;

import gov.cdc.izgateway.model.IEndpointStatus;

import java.util.Date;

/**
 * A ticket has been added to address this class in the future.  It is not needed for Transformation Servivce.
 * This class contains no-ops for all methods.
 * Ticket to track this: https://support.izgateway.org/browse/IGDD-1665
 */
public class EndpointStatus implements IEndpointStatus {
    @Override
    public String getDetail() {
        return "";
    }

    @Override
    public String getDiagnostics() {
        return "";
    }

    @Override
    public String getRetryStrategy() {
        return "";
    }

    @Override
    public String getStatus() {
        return "";
    }

    @Override
    public Date getStatusAt() {
        return new Date();
    }

    @Override
    public String getStatusBy() {
        return "";
    }

    @Override
    public int getStatusId() {
        return 0;
    }

    @Override
    public void setDestId(String destId) {

    }

    @Override
    public void setDestUri(String destUri) {

    }

    @Override
    public void setDestVersion(String destVersion) {

    }

    @Override
    public void setDetail(String detail) {

    }

    @Override
    public void setDiagnostics(String diagnostics) {

    }

    @Override
    public void setJurisdictionId(int jurisdictionId) {

    }

    @Override
    public void setRetryStrategy(String retryStrategy) {

    }

    @Override
    public void setStatusAt(Date statusAt) {

    }

    @Override
    public void setStatusBy(String statusBy) {

    }

    @Override
    public void setStatusId(int statusId) {

    }

    @Override
    public IEndpointStatus copy() {
        return null;
    }

    @Override
    public void setStatus(String status) {

    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public boolean isCircuitBreakerThrown() {
        return false;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public void setDestTypeId(int destType) {

    }

    @Override
    public String connected() {
        return "";
    }

    @Override
    public String getDestId() {
        return "";
    }

    @Override
    public String getDestUri() {
        return "";
    }

    @Override
    public String getDestType() {
        return "";
    }

    @Override
    public int getDestTypeId() {
        return 0;
    }

    @Override
    public String getJurisdictionName() {
        return "";
    }

    @Override
    public String getJurisdictionDesc() {
        return "";
    }

    @Override
    public int getJurisdictionId() {
        return 0;
    }

    @Override
    public String getDestVersion() {
        return "";
    }
}
