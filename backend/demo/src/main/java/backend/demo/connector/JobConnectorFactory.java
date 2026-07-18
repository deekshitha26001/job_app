package backend.demo.connector;

import backend.demo.connector.exception.UnsupportedConnectorException;
import backend.demo.entity.AtsProvider;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Open for extension, closed for modification.
 * Dynamically registers any bean that implements JobConnector.
 * To add a new ATS, just create a new JobConnector component.
 */
@Component
public class JobConnectorFactory {

    private final Map<AtsProvider, JobConnector> registry = new EnumMap<>(AtsProvider.class);

    public JobConnectorFactory(List<JobConnector> connectors) {
        for (JobConnector connector : connectors) {
            registry.put(connector.getProvider(), connector);
        }
    }

    public JobConnector getConnector(AtsProvider provider) {
        JobConnector connector = registry.get(provider);
        if (connector == null) {
            throw new UnsupportedConnectorException("No connector implemented for ATS provider: " + provider);
        }
        return connector;
    }
}
