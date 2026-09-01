package org.emprenApp.producto.application.service;

import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.regex.Pattern;

@Service
public class ProductoValidationService {

    private static final Pattern TEXTO_PLANO = Pattern.compile("^[\\p{L}\\p{N}\\s.,!?]+$");

    private static final int TITULO_MAX = 100;
    private static final int DESCRIPCION_MAX = 100;


    public void validarCamposProducto(String titulo, String descripcion, BigDecimal precio,
                                      Integer stock, Integer stockMinimo) throws ValidationException {

        if (titulo != null) {
            if (titulo.isBlank())                       throw new ValidationException(ErrorCodeEnum.INVALID_PARAMETERS);
            if (titulo.length() > TITULO_MAX)           throw new ValidationException(ErrorCodeEnum.INPUT_LENGTH);
            if (!TEXTO_PLANO.matcher(titulo).matches()) throw new ValidationException(ErrorCodeEnum.INVALID_PARAMETERS);
        }

        if (descripcion != null && !descripcion.isBlank()) {
            if (descripcion.length() > DESCRIPCION_MAX)      throw new ValidationException(ErrorCodeEnum.INPUT_LENGTH);
            if (!TEXTO_PLANO.matcher(descripcion).matches()) throw new ValidationException(ErrorCodeEnum.INVALID_PARAMETERS);
        }

        if (precio != null && precio.compareTo(BigDecimal.ZERO) <= 0)
            throw new ValidationException(ErrorCodeEnum.INVALID_PRICE);

        if (stock != null && stock < 0)
            throw new ValidationException(ErrorCodeEnum.INVALID_STOCK);

        if (stockMinimo != null && stockMinimo < 0)
            throw new ValidationException(ErrorCodeEnum.INVALID_STOCK);
    }
}
