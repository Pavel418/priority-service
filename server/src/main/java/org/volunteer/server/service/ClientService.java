package org.volunteer.server.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.volunteer.server.data.PreferenceStorage;
import org.volunteer.server.data.ServiceStorage;
import org.volunteer.server.model.VolunteerPreference;
import org.volunteer.server.model.dto.ClientInitResponse;
import org.volunteer.server.model.dto.PreferenceUpdateRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService 
{
    private final ServiceStorage serviceStorage;
    private final PreferenceStorage preferenceStorage;
    private final AssignmentService assignmentService;

    public ClientInitResponse initializeClient()
    {
        log.info("Client initalize request received");

        ClientInitResponse response = ClientInitResponse.builder()
            .clientId(UUID.randomUUID().toString())
            .services(serviceStorage.findAll())
            .build();

        log.info("Initialized client with id : {}", response.clientId());
        
        return response;
    }

    public void updatePreference(PreferenceUpdateRequest request)
    {
        log.info("Update preference request received");

        VolunteerPreference preference = new VolunteerPreference(
            request.clientId(), request.preferences());

        preferenceStorage.save(preference);

        log.info("Updated preference : {}", preference);
        
        assignmentService.startOptimisation();
    }
}
