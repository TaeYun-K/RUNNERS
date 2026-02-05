package com.runners.app.notification.repository;

import com.runners.app.notification.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    @Query("""
            select dt from DeviceToken dt
            where dt.user.id = :userId
            """)
    List<DeviceToken> findByUserId(@Param("userId") Long userId);

    @Query("""
            select dt from DeviceToken dt
            where dt.user.id in :userIds
            """)
    List<DeviceToken> findByUserIds(@Param("userIds") List<Long> userIds);

    Optional<DeviceToken> findByToken(String token);

    @Modifying
    @Transactional
    @Query("""
            delete from DeviceToken dt
            where dt.user.id = :userId
              and dt.token = :token
            """)
    void deleteByUserIdAndToken(@Param("userId") Long userId, @Param("token") String token);
}
