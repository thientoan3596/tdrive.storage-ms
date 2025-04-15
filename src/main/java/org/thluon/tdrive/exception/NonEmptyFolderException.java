package org.thluon.tdrive.exception;

import com.github.thientoan3596.exception.BaseException;

public class NonEmptyFolderException extends BaseException {
    public NonEmptyFolderException(String message, String fieldName, String modelName, String rejectedValue) {
        super(message, fieldName, modelName, rejectedValue);
    }
}
