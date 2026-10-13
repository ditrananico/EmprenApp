package org.emprenApp.categoria.application.service;

import org.emprenApp.categoria.application.dto.CategoriaDTO;
import org.emprenApp.categoria.domain.Categoria;
import org.emprenApp.categoria.domain.CategoriaRepository;
import org.emprenApp.categoria.infrastructure.request.CategoriaCreateRequest;
import org.emprenApp.categoria.infrastructure.request.CategoriaUpdateRequest;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    // Logica pura, sin dependencias externas: se usa la implementacion real.
    @Spy
    private CategoriaValidationService categoriaValidationService = new CategoriaValidationService();

    @InjectMocks
    private CategoriaService categoriaService;


    @Test
    @DisplayName("CREATE: con nombre null lanza INVALID_PARAMETERS")
    void createNameNull() {

        CategoriaCreateRequest request = new CategoriaCreateRequest();
        request.setNombre(null);
        request.setDescripcion("Comidas y bebidas artesanales");
        request.setTipo("PRODUCTO");

        assertThatThrownBy(() -> categoriaService.createCategoria(request))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.INVALID_PARAMETERS.getCode());

        verify(categoriaRepository, never()).save(any(Categoria.class));

    }

    @Test
    @DisplayName("CREATE: con datos válidos guarda y devuelve la categoría activa")
    void createOk() throws Exception {
        // Given
        CategoriaCreateRequest request = new CategoriaCreateRequest();
        request.setNombre(" Gastronomía    ADB  ");
        request.setDescripcion("Comidas y bebidas artesanales");
        request.setTipo("PRODUCTO");

        when(categoriaRepository.existsByNombreIgnoreCaseAndActiveTrue("Gastronomía ADB")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> {
            Categoria guardada = invocation.getArgument(0);
            guardada.setId(1L);
            return guardada;
        });

        // When
        CategoriaDTO resultado = categoriaService.createCategoria(request);

        // Then
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("Gastronomía ADB");
        assertThat(resultado.getActive()).isTrue();
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    @DisplayName("CREATE: con nombre duplicado lanza DUPLICATE_KEY y no guarda")
    void createDuplicado() {
        // Given
        CategoriaCreateRequest request = new CategoriaCreateRequest();
        request.setNombre("Gastronomía");

        when(categoriaRepository.existsByNombreIgnoreCaseAndActiveTrue("Gastronomía")).thenReturn(true);

        // When + Then
        assertThatThrownBy(() -> categoriaService.createCategoria(request))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.DUPLICATE_KEY.getCode());

        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    @DisplayName("CREATE: con espacios internos duplicado")
    void createDuplicadoEspaciosInternos() {
        // Given
        CategoriaCreateRequest request = new CategoriaCreateRequest();
        request.setNombre(" Gastronomía   Artesanal");

        when(categoriaRepository.existsByNombreIgnoreCaseAndActiveTrue("Gastronomía Artesanal")).thenReturn(true);

        // When + Then
        assertThatThrownBy(() -> categoriaService.createCategoria(request))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.DUPLICATE_KEY.getCode());

        verify(categoriaRepository, never()).save(any(Categoria.class));

    }

    @Test
    @DisplayName("CREATE: con nombre en blanco ")
    void createConNombreEnBlanco() {

        CategoriaCreateRequest request = new CategoriaCreateRequest();
        request.setNombre("  ");

        assertThatThrownBy(() -> categoriaService.createCategoria(request))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.INVALID_PARAMETERS.getCode());

        verify(categoriaRepository, never()).save(any(Categoria.class));

    }

    @Test
    @DisplayName("CREATE: con nombre de 101 caracteres")
    void createNameLengthMaxError() {
        CategoriaCreateRequest request = new CategoriaCreateRequest();
        request.setNombre("a".repeat(101));

        assertThatThrownBy(() -> categoriaService.createCategoria(request))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo( ErrorCodeEnum.INPUT_LENGTH.getCode());

        verify(categoriaRepository, never()).save(any(Categoria.class));
    }
    @Test
    @DisplayName("CREATE: con descripcion de 256 caracteres")
    void createDescriptionLengthMaxError() {
        CategoriaCreateRequest request = new CategoriaCreateRequest();
        request.setNombre("Gastronomía Artesanal");
        request.setDescripcion("b".repeat(5));
        request.setTipo("b".repeat(51));

        assertThatThrownBy(() -> categoriaService.createCategoria(request))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo( ErrorCodeEnum.INPUT_LENGTH.getCode());

        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    @DisplayName("CREATE: con tipo de 51 caracteres")
    void createTipoLengthMaxError() {
        CategoriaCreateRequest request = new CategoriaCreateRequest();
        request.setNombre("Gastronomía Artesanal");
        request.setDescripcion("b".repeat(5));
        request.setTipo("b".repeat(51));

        assertThatThrownBy(() -> categoriaService.createCategoria(request))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo( ErrorCodeEnum.INPUT_LENGTH.getCode());

        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    @DisplayName("CREATE: Error inesperado del repositorio")
    void createUnexpectedError() {
        CategoriaCreateRequest request = new CategoriaCreateRequest();
        request.setNombre("Gastronomía Artesanal");
        request.setDescripcion("Comidas y bebidas artesanales");
        request.setTipo("PRODUCTO");

        when(categoriaRepository.existsByNombreIgnoreCaseAndActiveTrue("Gastronomía Artesanal")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenThrow(new RuntimeException());

        assertThatThrownBy(() -> categoriaService.createCategoria(request))
                .isInstanceOf(GenericException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.GENERIC_ERROR.getCode());

        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    @DisplayName("CREATE: Nombre de exactamente 100 caracteres")
    void createLengthMax() throws BaseException {
        CategoriaCreateRequest request = new CategoriaCreateRequest();
        request.setNombre("a".repeat(100));
        request.setDescripcion("Comidas y bebidas artesanales");
        request.setTipo("PRODUCTO");

        when(categoriaRepository.existsByNombreIgnoreCaseAndActiveTrue("a".repeat(100))).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> {
            Categoria guardada = invocation.getArgument(0);
            guardada.setId(1L);
            return guardada;
        });
        // When
        CategoriaDTO resultado = categoriaService.createCategoria(request);

        assertThat(resultado.getNombre()).isEqualTo("a".repeat(100));
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    @DisplayName("GET: getCategoriaById(0L)")
    void getCategoriaById0L() {
        assertThatThrownBy(() -> categoriaService.getCategoriaById(0L))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.INVALID_PARAMETERS.getCode());
    }

    @Test
    @DisplayName("GET: getCategoriaById inexistente o inactiva")
    void getCategoriaByIdInexistenteOInactiva() {

        when(categoriaRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.getCategoriaById(10L))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.NOT_FOUND.getCode());
    }

    @Test
    @DisplayName("GET: getCategoriaById ok")
    void getCategoriaByIdOk() throws BaseException {
        Categoria categoria = new Categoria();
        categoria.setNombre("Gastronomía");
        categoria.setDescripcion("Comidas y bebidas artesanales");
        categoria.setActive(true);
        categoria.setId(1L);

        when(categoriaRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(categoria));

        CategoriaDTO resultado = categoriaService.getCategoriaById(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("Gastronomía");
        assertThat(resultado.getActive()).isTrue();
    }

    @Test
    @DisplayName("GET: getCategoriasActivas")
    void getCategoriasActivas() throws BaseException {
        Categoria categoria1 = new Categoria();
        categoria1.setNombre("Gastronomía");
        categoria1.setDescripcion("Comidas y bebidas artesanales");
        categoria1.setActive(true);
        categoria1.setId(1L);

        Categoria categoria2 = new Categoria();
        categoria2.setNombre("Bebidas");
        categoria2.setDescripcion("Bebidas artesanales");
        categoria2.setActive(true);
        categoria2.setId(2L);

        when(categoriaRepository.findAllByActiveTrue()).thenReturn(Arrays.asList(categoria1, categoria2));

        List<CategoriaDTO> resultados = categoriaService.getCategoriasActivas();

        assertThat(resultados).hasSize(2);
        assertThat(resultados.get(0).getId()).isEqualTo(1L);
        assertThat(resultados.get(1).getId()).isEqualTo(2L);
    }
    @Test
    @DisplayName("GET: categorias list vacio")
    void getCategoriasVacias() throws BaseException {
        when(categoriaRepository.findAllByActiveTrue()).thenReturn(List.of());
        List<CategoriaDTO> resultados = categoriaService.getCategoriasActivas();
        assertThat(resultados).isEmpty();
    }

    @Test
    @DisplayName("GET: categorias GenericException")
    void getCategoriasGenericException(){
        when(categoriaRepository.findAllByActiveTrue()).thenThrow(new RuntimeException());

        assertThatThrownBy(() -> categoriaService.getCategoriasActivas())
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.GENERIC_ERROR.getCode());
    }

    @Test
    @DisplayName("PUT: Update OK")
    void updateOK() throws BaseException {
        CategoriaUpdateRequest request = new CategoriaUpdateRequest();
        request.setId(1L);
        request.setNombre("Gastronomía NUEVA");
        request.setDescripcion("Comidas artesanales");
        request.setTipo("PRODUCTO NUEVO");

        Categoria categoria1 = new Categoria();
        categoria1.setId(1L);
        categoria1.setNombre("Gastronomía");
        categoria1.setDescripcion("Comidas y bebidas artesanales");
        categoria1.setTipo("PRODUCTO");

        when(categoriaRepository.findByIdAndActiveTrue(request.getId())).thenReturn(Optional.of(categoria1));
        when(categoriaRepository.existsByNombreIgnoreCaseAndActiveTrueAndIdNot(request.getNombre(), request.getId())).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> {
            Categoria guardada = invocation.getArgument(0);
            guardada.setId(1L);
            return guardada;
        });

        CategoriaDTO updated = categoriaService.updateCategoria(request);

        assertThat(updated.getId()).isEqualTo(1L);
        assertThat(updated.getNombre()).isEqualTo("Gastronomía NUEVA");
        assertThat(updated.getDescripcion()).isEqualTo("Comidas artesanales");
        assertThat(updated.getTipo()).isEqualTo("PRODUCTO NUEVO");
    }


    @Test
    @DisplayName("PUT: Nombre duplicado")
    void updateNameDuplicate()  {
        CategoriaUpdateRequest request = new CategoriaUpdateRequest();
        request.setId(1L);
        request.setNombre("Gastronomia");
        request.setDescripcion("Comidas artesanales");
        request.setTipo("PRODUCTO NUEVO");

        Categoria categoria1 = new Categoria();
        categoria1.setId(1L);
        categoria1.setNombre("Bebidas");
        categoria1.setDescripcion("Comidas y bebidas artesanales");
        categoria1.setTipo("PRODUCTO");
        when(categoriaRepository.findByIdAndActiveTrue(request.getId())).thenReturn(Optional.of(categoria1));
        when(categoriaRepository.existsByNombreIgnoreCaseAndActiveTrueAndIdNot(request.getNombre(), request.getId())).thenReturn(true);

        assertThatThrownBy(() -> categoriaService.updateCategoria(request))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.DUPLICATE_KEY.getCode());

        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    @DisplayName("PUT: Id inexistente o inactivo")
    void updateIdInactive()  {
        CategoriaUpdateRequest request = new CategoriaUpdateRequest();
        request.setId(10L);
        request.setNombre("Gastronomia");

        Categoria categoria1 = new Categoria();
        categoria1.setId(1L);
        categoria1.setNombre("Bebidas");
        when(categoriaRepository.findByIdAndActiveTrue(request.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.updateCategoria(request))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.NOT_FOUND.getCode());

        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    @DisplayName("PUT: Id 0")
    void updateId0()  {
        CategoriaUpdateRequest request = new CategoriaUpdateRequest();
        request.setId(0L);
        request.setNombre("Gastronomia");

        assertThatThrownBy(() -> categoriaService.updateCategoria(request))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.INVALID_PARAMETERS.getCode());

        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    @DisplayName("PUT: Id null")
    void updateIdNull()  {
        CategoriaUpdateRequest request = new CategoriaUpdateRequest();
        request.setId(null);
        request.setNombre("Gastronomia");

        assertThatThrownBy(() -> categoriaService.updateCategoria(request))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.INVALID_PARAMETERS.getCode());

        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    @DisplayName("PUT: Nombre blanco")
    void updateNombreBlanco()  {
        CategoriaUpdateRequest request = new CategoriaUpdateRequest();
        request.setId(1L);
        request.setNombre("  ");

        Categoria categoria1 = new Categoria();
        categoria1.setId(1L);
        categoria1.setNombre("Bebidas");

        when(categoriaRepository.findByIdAndActiveTrue(request.getId())).thenReturn(Optional.of(categoria1));

        assertThatThrownBy(() -> categoriaService.updateCategoria(request))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.INVALID_PARAMETERS.getCode());

        verify(categoriaRepository, never()).save(any(Categoria.class));
    }


    @Test
    @DisplayName("DELETE: Ok")
    void deleteOk() throws BaseException {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Bebidas");
        categoria.setActive(true);

        when(categoriaRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(categoria));

        boolean resultado = categoriaService.deleteCategoria(1L);

        assertThat(resultado).isTrue();

        ArgumentCaptor<Categoria> captor = ArgumentCaptor.forClass(Categoria.class);
        verify(categoriaRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1L);
        assertThat(captor.getValue().getActive()).isFalse();

        verify(categoriaRepository, never()).delete(any(Categoria.class));
        verify(categoriaRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("DELETE: Inexistente o inactiva")
    void deleteInexistenteOInactiva() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Bebidas");
        categoria.setActive(true);

        when(categoriaRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.deleteCategoria(1L))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.NOT_FOUND.getCode());

        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    @DisplayName("DELETE: Id 0")
    void deleteId0() {
        Categoria categoria = new Categoria();
        categoria.setId(0L);
        categoria.setNombre("Bebidas");
        categoria.setActive(true);

        assertThatThrownBy(() -> categoriaService.deleteCategoria(0L))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.INVALID_PARAMETERS.getCode());

        verify(categoriaRepository, never()).save(any(Categoria.class));
    }


}
