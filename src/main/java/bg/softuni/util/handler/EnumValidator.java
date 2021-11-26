package bg.softuni.util.handler;

import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class EnumValidator {

    public EnumValidator() {
    }

    public static <T extends Enum<T>> void validateEnum(String value, Class<T> enumClass, String type) {
        if (EnumSet.allOf(enumClass).stream().noneMatch(e -> e.name().equalsIgnoreCase(value))) {
            String message = String.format("%s не е валидна!", value);

            if ("args".equals(type)) {
                throw new IllegalArgumentException(message);
            }
            throw new IllegalStateException(message);
        }//Check if enum value is valid
    }
}