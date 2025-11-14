package com.lvt.apps.myvote.ms.validators;


import com.optum.ofsc.bds.accounts.model.ResultType;
import com.optum.ofsc.bds.accounts.validation.constants.ValidationConstants;
import com.optum.ofsc.bds.accounts.validation.core.ValidationError;
import com.optum.ofsc.bds.accounts.validation.core.ValidationResult;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Simplified validator for ResultType parameters.
 * Validates available values for ResultType.
 */
@Slf4j
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ResultTypeValidator {

    /**
     * Validates resultType and returns ValidationResult.
     *
     * @param resultType the resultType to validate
     * @return ValidationResult indicating success or failure with details
     */
    public static ValidationResult validateResultType(ResultType resultType) {
        log.debug("Validating ResultType: {}", resultType);

        //  validate resultType
        if (resultType != null && resultType != ResultType.LIGHTWEIGHT && resultType != ResultType.DETAILS) {
            return ValidationResult.invalid(
                    ValidationError.builder()
                            .code(ValidationConstants.VALIDATION_ERROR_CODE)
                            .field(ValidationConstants.FIELD_RESULT_TYPE)
                            .message(ValidationConstants.ResultTypeMessages.VALIDATION_FAILED)
                            .debugMessage(ValidationConstants.ResultTypeMessages.DEBUG_INVALID_VALUE)
                            .build()
            );
        }

        return ValidationResult.valid();
    }
}
