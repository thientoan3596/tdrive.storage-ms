package org.thluon.tdrive.utilities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ValidationMessageExtractor {
    public static String extractMessage(Class<?> target, String fieldName, Class<? extends Annotation> annotation) {
        try {
            Field field = target.getDeclaredField(fieldName);
            Annotation fieldAnnotation = null;
            if (field.isAnnotationPresent(annotation)) {
                fieldAnnotation = field.getAnnotation(annotation);
            } else if (annotation.equals(NotNull.class) && field.isAnnotationPresent(NotBlank.class)) {
                fieldAnnotation = field.getAnnotation(NotBlank.class);
            }

            if (fieldAnnotation != null) {
                Method messageMethod = fieldAnnotation.annotationType().getDeclaredMethod("message");
                String msg = (String) messageMethod.invoke(fieldAnnotation);
                if (fieldAnnotation instanceof Size sizeAnnotation) {
                    msg = msg
                            .replace("{min}", String.valueOf(sizeAnnotation.min()))
                            .replace("{max}", String.valueOf(sizeAnnotation.max()));
                }
                return msg;
            }

        } catch (NoSuchFieldException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            return "";
        }
        return "";
    }
}
