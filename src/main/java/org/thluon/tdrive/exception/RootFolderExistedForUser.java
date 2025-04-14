package org.thluon.tdrive.exception;

import com.github.thientoan3596.exception.BaseException;

public class RootFolderExistedForUser extends BaseException {
    public RootFolderExistedForUser(String message, String fieldName, String modelName, String rejectedValue) {
        super(message, fieldName, modelName, rejectedValue);
    }
}
