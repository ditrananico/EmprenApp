package org.emprenApp.user.domain;

import org.emprenApp.shared.application.enums.EstadoUserEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmailAndEstado(String email, EstadoUserEnum estado);

    Optional<User> findByIdAndEstado(Long id, EstadoUserEnum estado);
}
