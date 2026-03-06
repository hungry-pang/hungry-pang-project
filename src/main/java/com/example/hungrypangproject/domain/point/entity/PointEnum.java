package com.example.hungrypangproject.domain.point.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointEnum {
    USE,
    HOLDING,
    SAVE,
    EXPIRE;
}
