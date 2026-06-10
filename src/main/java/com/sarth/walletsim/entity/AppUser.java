package com.sarth.walletsim.entity;

import com.sarth.walletsim.constants.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_users")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String fullName;

    @Column(nullable = false, unique = true)
    String email;

    @Column(nullable = false)
    String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    UserRole role = UserRole.USER;

    @Column(nullable = false)
    LocalDateTime createdAt = LocalDateTime.now();
}
