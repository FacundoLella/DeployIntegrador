package com.car.business.logic.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import com.car.business.domain.enums.EstadoVehiculo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.car.business.logic.service.CaracteristicaVehiculoService;


import com.car.business.domain.Alquiler;

import jakarta.transaction.Transactional;

@Component
public class ScheduleTask {

    @Autowired
    private EmailService emailService;

    @Autowired
    private CaracteristicaVehiculoService caracteristicaVehiculoService;

    @Autowired
    private AlquilerService alquilerService;


    @Scheduled(cron = "0 30 18 * * *", zone = "America/Argentina/Mendoza")
    @Transactional
    public void enviarRecordatios(){
        LocalDate hoyMza = LocalDate.now(ZoneId.of("America/Argentina/Mendoza"));
        LocalDate maniana = hoyMza.plusDays(1);

        List<Alquiler> alquileres = alquilerService.buscarAlquileresVecManiana(maniana);

        for (Alquiler alquiler : alquileres){
            emailService.sendEmail(alquiler);
            
        }

        alquileres = alquilerService.buscarAlquileresEmpHoy(hoyMza);
        for (Alquiler alquiler : alquileres){
            if (!(alquiler.getVehiculo().getEstadoVehiculo().equals("ALQUILADO"))){
                alquiler.getVehiculo().setEstadoVehiculo(EstadoVehiculo.ALQUILADO);
                caracteristicaVehiculoService.sumarCantVehiculoAlquilado(alquiler.getVehiculo().getCaracteristicaVehiculo().getId());
            }
            
        }


    }
}

