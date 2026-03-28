package com.fossicker.repository;

import com.fossicker.entity.AppVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppVersionRepository extends JpaRepository<AppVersion, String> {

    Optional<AppVersion> findFirstByPlatformAndStatusOrderByVersionCodeDesc(String platform, Integer status);

    List<AppVersion> findByPlatformAndStatus(String platform, Integer status);

    List<AppVersion> findByPlatformOrderByCreateTimeDesc(String platform);

    List<AppVersion> findAllByOrderByCreateTimeDesc();

    Optional<AppVersion> findByPlatformAndVersionCode(String platform, String versionCode);
}
