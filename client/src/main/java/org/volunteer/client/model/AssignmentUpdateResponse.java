package org.volunteer.client.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public record AssignmentUpdateResponse(
        @SerializedName("assignments") List<Assignment> assignments
) {}