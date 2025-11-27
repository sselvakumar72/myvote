package com.lvt.apps.myvote.admin.ms.exceptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyVoteError {

    private String code;
    private String message;
    private String description;
}
