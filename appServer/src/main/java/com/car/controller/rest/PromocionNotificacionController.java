package com.car.controller.rest;

import com.car.business.logic.error.BusinessException;
import com.car.business.logic.service.PromocionService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/promociones")
public class PromocionNotificacionController {

    private final PromocionService promocionService;

    public PromocionNotificacionController(PromocionService promocionService) {
        this.promocionService = promocionService;
    }

    @PostMapping("/enviar")
    public ResponseEntity<?> enviarPromociones(@RequestBody List<String> promocionIds) {
        try {
            promocionService.enviarPromociones(promocionIds);
            return ResponseEntity.noContent().build();
        } catch (BusinessException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
