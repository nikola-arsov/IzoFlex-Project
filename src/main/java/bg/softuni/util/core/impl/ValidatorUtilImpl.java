package bg.softuni.util.core.impl;


import bg.softuni.util.core.ValidatorUtil;

import javax.validation.Validation;
import javax.validation.Validator;

public class ValidatorUtilImpl implements ValidatorUtil {
    private final Validator validator;

    public ValidatorUtilImpl() {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Override
    public <T> boolean isValid(T entity) {
        return this.validator.validate(entity).isEmpty();
    }

//    @Override
//    public <T> Set<ConstraintViolation<T>> getViolations(T entity) {
//        return this.validator.validate(entity);
//    }
}
