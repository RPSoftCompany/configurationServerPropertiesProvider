package pl.rpsoft.mule.provider;

import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.api.event.Event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ConsulTestCase extends MuleArtifactFunctionalTestCase {

    /**
     * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test
     * resources.
     */
    @Override
    protected String getConfigFile() {
        return "consul.xml";
    }

    @Test
    public void customPropertyProviderSuccessfullyConfigured() throws Exception {
        Event event = flowRunner("sccsFlow").run();

        Object payloadValue = event.getMessage()
                .getPayload()
                .getValue();

        assertThat(payloadValue, is("7000"));
    }

}
