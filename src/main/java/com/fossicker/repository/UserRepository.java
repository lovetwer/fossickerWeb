package com.fossicker.repository;

import com.fossicker.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByOpenId(String openId);

    Optional<User> findByPhone(String phone);

    Optional<User> findByNickname(String nickname);

    boolean existsByOpenId(String openId);

    boolean existsByPhone(String phone);

    boolean existsByNickname(String nickname);

    Page<User> findByNicknameContaining(String nickname, Pageable pageable);

    Page<User> findByPhoneContaining(String phone, Pageable pageable);

    List<User> findByNicknameContainingOrPhoneContaining(String nickname, String phone);
}
