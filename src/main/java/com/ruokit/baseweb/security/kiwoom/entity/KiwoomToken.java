package com.ruokit.baseweb.security.kiwoom.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "kiwoom_tokens")
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class KiwoomToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_type", nullable = false, length = 50)
    private String tokenType;

    @Column(name = "token", nullable = false, length = 2000)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "expires_dt", nullable = false, length = 14)
    private String expiresDt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void update(String tokenType, String token, String expiresDt, Instant expiresAt, Instant updatedAt) {
        this.tokenType = tokenType;
        this.token = token;
        this.expiresDt = expiresDt;
        this.expiresAt = expiresAt;
        this.updatedAt = updatedAt;
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }
}
