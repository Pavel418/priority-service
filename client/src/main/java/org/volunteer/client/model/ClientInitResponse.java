package org.volunteer.client.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public record ClientInitResponse(
        @SerializedName("clientId") String clientId,
        @SerializedName("services") List<Service> services
) {
    public boolean isValid() {
        return clientId != null && !clientId.isBlank() && services != null;
    }
}