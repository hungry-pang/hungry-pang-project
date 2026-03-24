package com.example.hungrypangproject.domain.store.dto.request;

import com.example.hungrypangproject.domain.store.entity.StoreStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StoreStatusUpdateRequest {

    private StoreStatus status;
}
