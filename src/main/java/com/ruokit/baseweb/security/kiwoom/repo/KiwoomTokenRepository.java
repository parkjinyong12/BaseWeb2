package com.ruokit.baseweb.security.kiwoom.repo;

import com.ruokit.baseweb.security.kiwoom.entity.KiwoomToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KiwoomTokenRepository extends JpaRepository<KiwoomToken, Long> {

    Optional<KiwoomToken> findTopByOrderByIdAsc();
}
