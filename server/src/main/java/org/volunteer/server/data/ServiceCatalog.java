// Path: src/main/java/org/volunteer/server/data/ServiceCatalog.java
package org.volunteer.server.data;

import org.springframework.stereotype.Component;
import org.volunteer.server.model.ServiceMeta;

import java.util.List;

/** Hard-coded catalogue loaded at boot; tweak as required. */
@Component
public class ServiceCatalog {

    private final List<ServiceMeta> services = List.of(
            new ServiceMeta("svc-reception", "Reception", "Front-desk welcome desk",        4),
            new ServiceMeta("svc-logistics", "Logistics", "Moving / carrying supplies",     5),
            new ServiceMeta("svc-food",      "Food-service", "Buffet & drinks",             6),
            new ServiceMeta("svc-security",  "Security", "Access control, crowd flow",      3),
            new ServiceMeta("svc-tech",      "Tech support", "A/V & projector ops",         2),
            new ServiceMeta("svc-clean",     "Cleanup", "Venue cleanup team",               4),
            new ServiceMeta("svc-runner",    "Runner", "Ad-hoc errands",                    3),
            new ServiceMeta("svc-stage",     "Stage hand", "Speaker coordination",          2),
            new ServiceMeta("svc-parking",   "Parking", "Car-park direction",               2),
            new ServiceMeta("svc-info",      "Info-desk", "General information point",      2)
    );

    public List<ServiceMeta> all() { return services; }
} 