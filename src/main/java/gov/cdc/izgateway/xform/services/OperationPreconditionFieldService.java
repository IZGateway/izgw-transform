package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.xform.model.OperationPreconditionField;
import gov.cdc.izgateway.xform.repository.XformRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OperationPreconditionFieldService extends GenericService<OperationPreconditionField> {
    protected OperationPreconditionFieldService(XformRepository<OperationPreconditionField> repo) {
        super(repo);
    }

    public List<OperationPreconditionField> getOperationList() {
        List<OperationPreconditionField> allFields = super.getList();
        List<OperationPreconditionField> operationFields = new ArrayList<>();
        for (OperationPreconditionField field : allFields) {
            if (Boolean.TRUE.equals(field.getForOperation())) {
                operationFields.add(field);
            }
        }
        return operationFields;
    }

    public List<OperationPreconditionField> getPreconditionList() {
        List<OperationPreconditionField> allFields = super.getList();
        List<OperationPreconditionField> preconditionFields = new ArrayList<>();
        for (OperationPreconditionField field : allFields) {
            if (Boolean.TRUE.equals(field.getForPrecondition())) {
                preconditionFields.add(field);
            }
        }
        return preconditionFields;
    }

}
