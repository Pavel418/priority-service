package org.volunteer.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.volunteer.server.data.ServiceCatalog;
import org.volunteer.server.dto.AssignmentDto;
import org.volunteer.server.dto.AssignmentUpdateResponse;
import org.volunteer.server.model.ServiceMeta;
import org.volunteer.server.model.VolunteerPreference;
import org.volunteer.server.web.PlainAssignmentHandler;

@Service
public class AssignmentService {

    private final PreferenceService preferenceService;
    private final GAService gaService;
    private final ServiceCatalog catalog;

    private final SimpMessagingTemplate stompBroker;   // old path
    private final PlainAssignmentHandler plainWs;      // new path

    private final AtomicReference<List<AssignmentDto>> last = new AtomicReference<>(List.of());

    public AssignmentService(PreferenceService prefSvc,
                             GAService gaSvc,
                             ServiceCatalog catalog,
                             @Autowired(required = false) SimpMessagingTemplate stompBroker,
                             PlainAssignmentHandler plainWs) {
        this.preferenceService = prefSvc;
        this.gaService = gaSvc;
        this.catalog = catalog;
        this.stompBroker = stompBroker;
        this.plainWs = plainWs;
    }

    /** Called every time preferences change (auto-trigger). */
    public void startOptimisation() {
        List<VolunteerPreference> snapshot = preferenceService.orderedSnapshot();
        if (snapshot.size() < 3) return;            // threshold
    
        gaService.solveAsync(snapshot, catalog.all())
                 .thenAccept(genes -> handleResult(snapshot, genes));
    }
    
    

    public List<AssignmentDto> current() { return last.get(); }

    /* ---------- private ---------- */
    private void handleResult(List<VolunteerPreference> vpList, int[] genes) {
        List<ServiceMeta> services = catalog.all();
        List<AssignmentDto> out = new ArrayList<>(genes.length);
    
        for (int i = 0; i < genes.length; i++) {
            VolunteerPreference vp = vpList.get(i);
            ServiceMeta svc       = services.get(genes[i]);
            out.add(new AssignmentDto(vp.volunteerId(),
                                      vp.volunteerName(),
                                      svc));
        }
    
        last.set(out);
        AssignmentUpdateResponse payload = new AssignmentUpdateResponse(out);
        if (stompBroker != null)
            stompBroker.convertAndSend("/topic/assignment", payload);
        plainWs.broadcast(payload);
    }
    
    
} 