package com.example.hungrypangproject.domain.menu.dto.request;

import com.example.hungrypangproject.domain.menu.entity.MenuStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MenuStatusUpdateRequest {

    private MenuStatus status;
}
