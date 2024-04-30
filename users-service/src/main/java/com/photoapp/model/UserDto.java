package com.photoapp.model;

import java.io.Serializable;
import lombok.Data;

@Data
public class UserDto implements Serializable {
    private static final long serialVersionUUID = -9878798787987987L;

    private String userId;

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private String encryptedPassword;
}
