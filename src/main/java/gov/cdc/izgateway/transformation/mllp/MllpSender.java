package gov.cdc.izgateway.transformation.mllp;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.logging.Level;

@Log
public class MllpSender {

    private MllpSender() {}

    public static Message send(String host, int port, Message message) {

        try (HapiContext context = new DefaultHapiContext(new NoValidation())) {
            Connection connection = context.newClient(host, port, false);
            Initiator initiator = connection.getInitiator();
            return initiator.sendAndReceive(message);
        } catch (HL7Exception e) {
            log.log(Level.SEVERE, "HL7 Exception: " + e.getMessage());
        } catch (IOException e) {
            log.log(Level.SEVERE, "IO Exception: " + e.getMessage());
        } catch (LLPException e) {
            log.log(Level.SEVERE, "LLP Exception: " + e.getMessage());
        }
        return null;
    }
}

