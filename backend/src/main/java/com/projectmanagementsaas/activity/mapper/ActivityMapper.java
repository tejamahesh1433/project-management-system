package com.projectmanagementsaas.activity.mapper;

import com.projectmanagementsaas.activity.dto.ActivityResponse;
import com.projectmanagementsaas.activity.entity.Activity;
import org.springframework.stereotype.Component;

@Component
public class ActivityMapper {
    public ActivityResponse toResponse(Activity activity) {
        return new ActivityResponse(activity.getId(), activity.getWorkspaceId(), activity.getProjectId(),
                activity.getActorId(), activity.getAction(), activity.getEntityType(), activity.getEntityId(),
                activity.getMessage(), activity.getCreatedAt());
    }
}
