package org.emprenApp.user.domain;

import org.emprenApp.shared.application.enums.UserStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmailAndStatus(String email, UserStatusEnum status);

    Optional<User> findByIdAndStatus(Long id, UserStatusEnum status);

    Page<User> findAllByStatus(UserStatusEnum status, Pageable pageable);
}
