package org.emprenApp.user.domain;

import org.emprenApp.shared.application.enums.UserStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmailAndStatus(String email, UserStatusEnum status);

    Optional<User> findByIdAndStatus(Long id, UserStatusEnum status);

    // Carga rol y permisos en una sola consulta (ambos son LAZY); lo usa la autenticacion.
    @Query("select u from User u join fetch u.role r left join fetch r.permissions where u.email = :email")
    Optional<User> findWithRoleAndPermissionsByEmail(@Param("email") String email);

    Page<User> findAllByStatus(UserStatusEnum status, Pageable pageable);

    // email y phone son unicos en toda la tabla, sin importar el estado (una baja logica sigue ocupandolos).
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Long id);
}
