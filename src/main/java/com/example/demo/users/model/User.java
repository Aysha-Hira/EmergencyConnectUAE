package com.example.demo.users.model;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;

@Data
@Document(collection = "users")
public class User {
    @Id
    private String id;

    @Field(name = "username")
    private String username;

    @Field(name = "email")
    private String email;

    @Field(name = "password") // store as bcrypt hash
    private String password;

    @Field(name = "role")
    private UserRole role;

    @Field(name = "active")
    private boolean active = true;

    @Field(name = "mfaEnabled")
    private boolean mfaEnabled = false;

    @Field(name = "mfaSecret")
    private String mfaSecret; // used for TOTP later

    @Field(name = "createdAt")
    private LocalDateTime createdAt;
}