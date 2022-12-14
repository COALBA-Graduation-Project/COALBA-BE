package com.project.coalba.domain.workspace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter @Builder
public class WorkspaceResponse {

    private Long workspaceId;

    private String name;

    private String imageUrl;
}
