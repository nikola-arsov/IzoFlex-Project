package bg.softuni.util.handler;

import bg.softuni.annotation.MatchFields;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MatchingFieldsValidator implements ConstraintValidator<MatchFields, Object> {
    private String firstFieldName;
    private String secondFieldName;
    private String message;

    @Override
    public void initialize(MatchFields constraintAnnotation) {
        this.firstFieldName = constraintAnnotation.first();
        this.secondFieldName = constraintAnnotation.second();
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(value);
        Object firstValue = wrapper.getPropertyValue(this.firstFieldName);
        Object secondValue = wrapper.getPropertyValue(this.secondFieldName);

        boolean valid = firstValue != null && firstValue.equals(secondValue);

        if (!valid) {
            this.buildConstraintViolation(context,this.message,this.secondFieldName);
            this.buildConstraintViolation(context,this.message,this.firstFieldName);
        }
        return valid;
    }

    private void buildConstraintViolation(ConstraintValidatorContext context, String message, String fieldName) {
        context.
                buildConstraintViolationWithTemplate(message)
                .addPropertyNode(fieldName)
                .addConstraintViolation()
                .disableDefaultConstraintViolation();
    }
}
