package com.lvt.apps.common.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRoles {

    ADMIN ("ADMIN"),
    VOTER ("VOTER"),
    AUDITOR ("AUDITOR"),
    CONTRIBUTOR ("CONTRIBUTOR");

    private final String roleName;
}
