package bg.softuni.util.core;

public interface ValidatorUtil {
    <T> boolean isValid(T entity);

//    <T> Set<ConstraintViolation<T>> getViolations(T entity);
}
