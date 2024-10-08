package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;
import gov.cdc.izgateway.transformation.configuration.OperationMapperConfig;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.exceptions.OperationException;
import gov.cdc.izgateway.transformation.model.Code;
import gov.cdc.izgateway.transformation.model.Mapping;
import gov.cdc.izgateway.transformation.services.MappingService;
import gov.cdc.izgateway.transformation.services.SpringContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


@Slf4j
public class Hl7v2MapOperation extends BaseOperation<OperationMapperConfig> implements Operation {

    private MappingService mappingService;
    public Hl7v2MapOperation(OperationMapperConfig config) {
        super(config);
        this.mappingService = SpringContext.getBean(MappingService.class);
    }

    @Override
    public void thisOperation(ServiceContext context) throws OperationException {

        log.trace(String.format("MAP Operation: %s / CODE FIELD %s CODE SYSTEM FIELD %s",
                this.getClass().getSimpleName(),
                this.operationConfig.getCodeField(),
                this.operationConfig.getCodeSystemField()));

        try {

            Message message = context.getCurrentMessage();
            Terser terser = new Terser(message);
            Code codeToMap = getCode(terser);
            Mapping mapping = mappingService.getMapping(context.getOrganizationId(), codeToMap);

            if (mapping == null) {
                log.debug(String.format("Mapping not found for %s / %s", codeToMap.codeSystem(), codeToMap.code()));
                return;
            }

            terser.set(operationConfig.getCodeField(), mapping.getTargetCode());
            terser.set(operationConfig.getCodeSystemField(), mapping.getTargetCodeSystem());
            context.setCurrentMessage(message);
        } catch (HL7Exception ex) {
            throw new OperationException(ex.getMessage(), ex.getCause());
        }
    }

    private Code getCode(Terser terser) throws HL7Exception {
        String code = terser.get(operationConfig.getCodeField());
        String codeSystem = StringUtils.isEmpty(terser.get(operationConfig.getCodeSystemField())) ?
                operationConfig.getCodeSystemDefault() : terser.get(operationConfig.getCodeSystemField());

        return new Code(code, codeSystem);
    }
}
