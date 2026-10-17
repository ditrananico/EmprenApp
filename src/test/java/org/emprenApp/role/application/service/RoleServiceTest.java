package org.emprenApp.role.application.service;

import org.emprenApp.permission.domain.Permission;
import org.emprenApp.permission.domain.PermissionRepository;
import org.emprenApp.role.application.dto.RoleDTO;
import org.emprenApp.role.domain.Role;
import org.emprenApp.role.domain.RoleRepository;
import org.emprenApp.role.infrastructure.request.RoleCreateRequest;
import org.emprenApp.role.infrastructure.request.RoleUpdateRequest;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.exception.BaseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Spy
    private RoleValidationService roleValidationService = new RoleValidationService();

    @InjectMocks
    private RoleService roleService;


    @Test
    @DisplayName("CREATE: create OK")
    void createOk() throws BaseException {
        RoleCreateRequest request =new RoleCreateRequest();
        request.setName("Admin");
        request.setDescription("Administrator role");
        request.setPermissionIds(List.of(1L, 2L));

        when(roleRepository.existsByNameIgnoreCaseAndActiveTrue(request.getName())).thenReturn(false);
        when(permissionRepository.findByIdAndActiveTrue(request.getPermissionIds().get(0))).thenReturn(Optional.of(new Permission(1L, "Permission 1", "description 1", true)));
        when(permissionRepository.findByIdAndActiveTrue(request.getPermissionIds().get(1))).thenReturn(Optional.of(new Permission(2L, "Permission 2", "description 2",true)));

        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role guardada = invocation.getArgument(0);
            guardada.setId(1L);
            return guardada;
        });

        RoleDTO newRole = roleService.createRole(request);

        assertThat(newRole).isNotNull();
        assertThat(newRole.getId()).isEqualTo(1L);
        assertThat(newRole.getName()).isEqualTo("Admin");
        assertThat(newRole.getDescription()).isEqualTo("Administrator role");
        assertThat(newRole.getPermissionIds()).containsExactlyInAnyOrder(1L, 2L);


        verify(roleRepository).save(any(Role.class));

    }
    @Test
    @DisplayName("CREATE: space in name")
    void createSpaceInName() throws BaseException {
        RoleCreateRequest request =new RoleCreateRequest();
        request.setName("   Admin   ");
        request.setDescription("Administrator role");

        when(roleRepository.existsByNameIgnoreCaseAndActiveTrue("Admin")).thenReturn(false);

        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role guardada = invocation.getArgument(0);
            guardada.setId(1L);
            return guardada;
        });
        RoleDTO newRole = roleService.createRole(request);

        assertThat(newRole).isNotNull();
        assertThat(newRole.getId()).isEqualTo(1L);
        assertThat(newRole.getName()).isEqualTo("Admin");

        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("GET: get OK")
    void getOk() throws BaseException {

        Role role1 = new Role(1L, "Admin", "Administrator role", true, new HashSet<>());

        when(roleRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(role1));

        RoleDTO response = roleService.getRoleById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Admin");
        assertThat(response.getDescription()).isEqualTo("Administrator role");

    }

    @Test
    @DisplayName("GET: get id inexsitente")
    void getIdInexistente() {
        assertThatThrownBy(() -> roleService.getRoleById(999L))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.NOT_FOUND.getCode());
    }

    @Test
    @DisplayName("GET: get id 0")
    void getId0(){
        assertThatThrownBy(() -> roleService.getRoleById(0L))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.INVALID_PARAMETERS.getCode());
    }

    @Test
    @DisplayName("GET: List OK")
    void getListOk() throws BaseException {

        Role role1 = new Role(1L, "Admin", "Administrator role", true, new HashSet<>());
        Role role2 = new Role(2L, "User", "User role", true, new HashSet<>());

        Pageable pageable = PageRequest.of(0, 10);
        Page<Role> pageEntity = new PageImpl<>(Arrays.asList(role1, role2), pageable, 2);

        when(roleRepository.findAllByActiveTrue(pageable)).thenReturn(pageEntity);

        Page<RoleDTO> response = roleService.getActiveRoles(pageable);
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent().get(0).getId()).isEqualTo(1L);
        assertThat(response.getContent().get(1).getId()).isEqualTo(2L);

    }
    @Test
    @DisplayName("GET: List error de repositorio")
    void getListError() {
        Pageable pageable = PageRequest.of(0, 10);
        when(roleRepository.findAllByActiveTrue(pageable)).thenThrow(new RuntimeException("Error al acceder a la base de datos"));

        assertThatThrownBy(() -> roleService.getActiveRoles(pageable))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.GENERIC_ERROR.getCode());
    }

    @Test
    @DisplayName("PUT: put OK")
    void putOk() throws BaseException {
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setId(1L);
        request.setName("Updated Role");
        request.setDescription("Updated role description");
        request.setPermissionIds(List.of(1L, 2L));

        Role role = new Role(1L,"Role","description",true,  new HashSet<>(3,4));

        when(roleRepository.findByIdAndActiveTrue(role.getId())).thenReturn(Optional.of(role));
        when(roleRepository.existsByNameIgnoreCaseAndActiveTrueAndIdNot(request.getName(), role.getId())).thenReturn(false);
        when(permissionRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(new Permission(1L, "Permission 1","role description1", true)));
        when(permissionRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(new Permission(2L, "Permission 2","role description2", true)));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role guardada = invocation.getArgument(0);
            guardada.setId(1L);
            return guardada;
        });
        RoleDTO newRole = roleService.updateRole(request);

        assertThat(newRole).isNotNull();
        assertThat(newRole.getId()).isEqualTo(1L);
        assertThat(newRole.getName()).isEqualTo("Updated Role");
        assertThat(newRole.getDescription()).isEqualTo("Updated role description");
        assertThat(newRole.getPermissionIds()).containsExactlyInAnyOrder(1L, 2L);

        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("PUT: name duplicate")
    void putNameDuplicate() {
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setId(1L);
        request.setName("Updated Role");
        request.setDescription("Updated role description");
        request.setPermissionIds(List.of(1L, 2L));

        Role role = new Role(1L,"Role","description",true,  new HashSet<>(3,4));

        when(roleRepository.findByIdAndActiveTrue(role.getId())).thenReturn(Optional.of(role));
        when(roleRepository.existsByNameIgnoreCaseAndActiveTrueAndIdNot(request.getName(), role.getId())).thenReturn(true);

        assertThatThrownBy(() -> roleService.updateRole(request))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.DUPLICATE_KEY.getCode());

        verify(roleRepository, never()).save(any(Role.class));

    }

    @Test
    @DisplayName("DELETE: delete ok")
    void deleteOk() throws BaseException {

        Role role = new Role(1L,"Role","description",true,  new HashSet<>(3,4));

        when(roleRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role guardada = invocation.getArgument(0);
            guardada.setId(1L);
            return guardada;
        });

        boolean isDelete = roleService.deleteRole(1L);

        assertThat(isDelete).isTrue();

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(captor.capture());
        assertThat(captor.getValue().getActive()).isFalse();

    }

    @Test
    @DisplayName("DELETE: id Inexistente")
    void deleteIdInexistente(){
        assertThatThrownBy(() -> roleService.deleteRole(999L))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.NOT_FOUND.getCode());

        verify(roleRepository, never()).save(any(Role.class));

    }

    @Test
    @DisplayName("DELETE: id 0")
    void deleteId0(){
        assertThatThrownBy(() -> roleService.deleteRole(0L))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.INVALID_PARAMETERS.getCode());

        verify(roleRepository, never()).save(any(Role.class));

    }

    @Test
    @DisplayName("PATCH: active Role")
    void patchActiveRole() throws BaseException {
        Role role = new Role(1L,"Role","description",false,  new HashSet<>(3,4));

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.existsByNameIgnoreCaseAndActiveTrue(role.getName())).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role guardada = invocation.getArgument(0);
            guardada.setId(1L);
            guardada.setActive(true);
            return guardada;
        });

        RoleDTO response = roleService.activateRole(1L);

        assertThat(response.getActive()).isTrue();

        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("PATCH: name duplicate")
    void patchNameDuplicate(){

        Role role = new Role(1L,"Role","description",false,  new HashSet<>(3,4));

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.existsByNameIgnoreCaseAndActiveTrue(role.getName())).thenReturn(true);

        assertThatThrownBy(() -> roleService.activateRole(1L))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.DUPLICATE_KEY.getCode());

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @DisplayName("PATCH: id inexistente")
    void patchIdInexistent(){
        assertThatThrownBy(() -> roleService.activateRole(999L))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.NOT_FOUND.getCode());

        verify(roleRepository, never()).save(any(Role.class));
    }


}
