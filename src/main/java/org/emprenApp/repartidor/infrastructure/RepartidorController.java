package org.emprenApp.repartidor.infrastructure;

import org.emprenApp.repartidor.application.RepartidorAdapter;
import org.emprenApp.shared.application.application.BaseRestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/repartidor")
public class RepartidorController extends BaseRestController {

    private final static Logger logger = LoggerFactory.getLogger(RepartidorController.class);

    @Autowired
    private RepartidorAdapter repartidorAdapter;
}
