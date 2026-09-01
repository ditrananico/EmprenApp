package org.emprenApp.ubicacion.infrastructure;

import org.emprenApp.shared.application.application.BaseRestController;
import org.emprenApp.ubicacion.application.UbicacionAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/ubicacion")
public class UbicacionController extends BaseRestController {

    @Autowired
    private UbicacionAdapter ubicacionAdapter;
}
