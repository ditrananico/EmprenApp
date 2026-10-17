package org.emprenApp.permission.application.service;

import org.emprenApp.permission.application.dto.PermissionDTO;
import org.emprenApp.permission.domain.Permission;
import org.emprenApp.permission.domain.PermissionRepository;
import org.emprenApp.permission.infrastructure.request.PermissionCreateRequest;
import org.emprenApp.permission.infrastructure.request.PermissionUpdateRequest;
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

import static org.assertj.core.api.Assertions.assertThat;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Spy
    private PermissionValidationService permissionValidationService = new PermissionValidationService();

    @InjectMocks
    private PermissionService permissionService;


    @Test
    @DisplayName("CREATE: create OK")
    void createOk() throws BaseException {
        PermissionCreateRequest request = new PermissionCreateRequest();
        request.setName("Permission Test");
        request.setDescription("Permission Test Description");

        when(permissionRepository.existsByNameIgnoreCaseAndActiveTrue(request.getName())).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenAnswer(invocation -> {
            Permission guardada = invocation.getArgument(0);
            guardada.setId(1L);
            return guardada;
        });

        PermissionDTO   created = permissionService.createPermission(request);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.getName()).isEqualTo(request.getName());
        assertThat(created.getDescription()).isEqualTo(request.getDescription());

    }

    @Test
    @DisplayName("CREATE: name duplicate")
    void createDuplicateName(){
        PermissionCreateRequest request = new PermissionCreateRequest();
        request.setName("Permission Test");
        request.setDescription("Permission Test Description");

        when(permissionRepository.existsByNameIgnoreCaseAndActiveTrue(request.getName())).thenReturn(false);
        when(permissionRepository.existsByNameIgnoreCaseAndActiveTrue(request.getName())).thenReturn(true);

        assertThatThrownBy(() -> permissionService.createPermission(request))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.DUPLICATE_KEY.getCode());

        verify(permissionRepository, never()).save(any(Permission.class));
    }

    @Test
    @DisplayName("CREATE: space in name")
    void createSpaceInName() throws BaseException {

        PermissionCreateRequest request = new PermissionCreateRequest();
        request.setName("  Permission Test  ");
        request.setDescription("Permission Test Description");

        when(permissionRepository.existsByNameIgnoreCaseAndActiveTrue("Permission Test")).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenAnswer(invocation -> {
            Permission guardada = invocation.getArgument(0);
            guardada.setId(1L);
            return guardada;
        });

        PermissionDTO   created = permissionService.createPermission(request);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.getName()).isEqualTo("Permission Test");

        verify(permissionRepository).save(any(Permission.class));
    }

    @Test
    @DisplayName("GET: get OK")
    void getOk() throws BaseException {
        Permission permission = new Permission(1L, "Permission Test", "Permission Test Description", true);

        when(permissionRepository.findByIdAndActiveTrue(1L)).thenReturn(java.util.Optional.of(permission));

        PermissionDTO get = permissionService.getPermissionById(1L);

        assertThat(get).isNotNull();
        assertThat(get.getId()).isEqualTo(permission.getId());
        assertThat(get.getName()).isEqualTo(permission.getName());
        assertThat(get.getDescription()).isEqualTo(permission.getDescription());

    }
    @Test
    @DisplayName("GET: get Id inexsitente")
    void getIdInexistente()  {
        assertThatThrownBy(() -> permissionService.getPermissionById(999L))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.NOT_FOUND.getCode());
    }

    @Test
    @DisplayName("GET: listGet OK")
    void getList() throws BaseException {
        Permission permission = new Permission(1L, "Permission Test", "Permission Test Description", true);
        Permission permission2 = new Permission(2L, "Permission Test 2", "Permission Test Description 2", true);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Permission> pageEntity = new PageImpl<>(Arrays.asList(permission, permission2), pageable, 2);

        when(permissionRepository.findAllByActiveTrue(pageable)).thenReturn(pageEntity);

        Page<PermissionDTO> page = permissionService.getActivePermissions(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Permission Test");
    }

    @Test
    @DisplayName("GET: error Repository")
    void getErrorRepository(){
        Pageable pageable = PageRequest.of(0, 10);
        when(permissionRepository.findAllByActiveTrue(pageable)).thenThrow(new RuntimeException());

        assertThatThrownBy(() -> permissionService.getActivePermissions(pageable))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.GENERIC_ERROR.getCode());
    }


    @Test
    @DisplayName("PUT: put OK")
    void putOK() throws BaseException {
        PermissionUpdateRequest request = new PermissionUpdateRequest();
        request.setId(1L);
        request.setName("Updated Permission Test");
        request.setDescription("Updated Permission Test Description");

        Permission permission = new Permission(1L, "Permission Test", "Permission Test Description", true);

        when(permissionRepository.findByIdAndActiveTrue(1L)).thenReturn(java.util.Optional.of(permission));
        when(permissionRepository.save(any(Permission.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(permissionRepository.existsByNameIgnoreCaseAndActiveTrueAndIdNot(request.getName(), request.getId())).thenReturn(false);

        PermissionDTO response = permissionService.updatePermission(request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(request.getName());
        assertThat(response.getDescription()).isEqualTo(request.getDescription());

    }

    @Test
    @DisplayName("PUT: name duplicate")
    void putNameDuplicate() {
        PermissionUpdateRequest request = new PermissionUpdateRequest();
        request.setId(1L);
        request.setName("Permission Test");
        request.setDescription("Updated Permission Test Description");

        Permission permission = new Permission(1L, "Permission Test", "Permission Test Description", true);

        when(permissionRepository.findByIdAndActiveTrue(1L)).thenReturn(java.util.Optional.of(permission));
        when(permissionRepository.existsByNameIgnoreCaseAndActiveTrueAndIdNot(request.getName(), request.getId())).thenReturn(true);

        assertThatThrownBy(() -> permissionService.updatePermission(request))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.DUPLICATE_KEY.getCode());

        verify(permissionRepository, never()).save(any(Permission.class));
    }

    @Test
    @DisplayName("DELETE: delete OK")
    void deleteOk() throws BaseException {

        Permission permission = new Permission(1L, "Permission Test", "Permission Test Description", true);

        when(permissionRepository.findByIdAndActiveTrue(1L)).thenReturn(java.util.Optional.of(permission));
        when(permissionRepository.save(any(Permission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean deleted = permissionService.deletePermission(1L);

        assertThat(deleted).isTrue();
        ArgumentCaptor<Permission> captor = ArgumentCaptor.forClass(Permission.class);
        verify(permissionRepository).save(captor.capture());
        assertThat(captor.getValue().getActive()).isFalse();

        verify(permissionRepository, never()).delete(any(Permission.class));
    }

    @Test
    @DisplayName("DELETE: id inexistente")
    void deleteIdInexistente(){
        when(permissionRepository.findByIdAndActiveTrue(999L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> permissionService.deletePermission(999L))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.NOT_FOUND.getCode());

        verify(permissionRepository, never()).save(any(Permission.class));

    }

}
