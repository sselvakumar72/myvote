package com.lvt.apps.myvote.ms.validators;

import com.lvt.apps.myvote.ms.exceptions.MyVoteError;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
public class ValidationError extends MyVoteError {

    private final String field;
    private final String value;
}
