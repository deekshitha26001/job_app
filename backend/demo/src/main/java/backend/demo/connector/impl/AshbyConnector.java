package backend.demo.connector.impl;

import backend.demo.connector.JobConnector;
import backend.demo.connector.dto.CollectedJob;
import backend.demo.connector.exception.UnsupportedConnectorException;
import backend.demo.entity.AtsProvider;
import backend.demo.entity.Company;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AshbyConnector implements JobConnector {

    @Override
    public AtsProvider getProvider() {
        return AtsProvider.ASHBY;
    }

    @Override
    public List<CollectedJob> fetchJobs(Company company) {
        throw new UnsupportedConnectorException("Ashby connector is not yet implemented.");
    }
}
