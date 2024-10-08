package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.exceptions.OperationException;
import gov.cdc.izgateway.transformation.model.Code;
import gov.cdc.izgateway.transformation.model.Mapping;
import gov.cdc.izgateway.transformation.services.MappingService;
import gov.cdc.izgateway.transformation.services.SpringContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


@Slf4j
public class Hl7v2Mapper extends Mapper implements Operation {

    private final MappingService mappingService;
    public Hl7v2Mapper(Mapper mapper) {
        super(mapper);
        this.mappingService = SpringContext.getBean(MappingService.class);
    }

    @Override
    public void execute(ServiceContext context) throws OperationException {

        try {

            Message message = context.getCurrentMessage();
            Terser terser = new Terser(message);
            Code codeToMap = getCode(terser);
            Mapping mapping = mappingService.getMapping(context.getOrganizationId(), codeToMap);

            if (mapping == null) {
                log.debug(String.format("Mapping not found for %s / %s", codeToMap.codeSystem(), codeToMap.code()));
                return;
            }

            terser.set(this.getCodeField(), mapping.getTargetCode());
            terser.set(this.getCodeSystemField(), mapping.getTargetCodeSystem());
            context.setCurrentMessage(message);
        } catch (HL7Exception ex) {
            throw new OperationException(ex.getMessage(), ex.getCause());
        }
    }

    private Code getCode(Terser terser) throws HL7Exception {
        String code = terser.get(this.getCodeField());
        String codeSystem = StringUtils.isEmpty(terser.get(this.getCodeSystemField())) ?
                this.getCodeSystemDefault() : terser.get(this.getCodeSystemField());

        return new Code(code, codeSystem);
    }
}
