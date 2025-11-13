package com.car.config;

import com.car.business.domain.Departamento;
import com.car.business.domain.Direccion;
import com.car.business.domain.Empleado;
import com.car.business.domain.Imagen;
import com.car.business.domain.Localidad;
import com.car.business.domain.Pais;
import com.car.business.domain.Provincia;
import com.car.business.domain.Usuario;
import com.car.business.domain.enums.RolUsuario;
import com.car.business.domain.enums.TipoDocumento;
import com.car.business.domain.enums.TipoEmpleado;
import com.car.business.domain.enums.TipoImagen;
import com.car.business.percistence.repository.DepartamentoRepository;
import com.car.business.percistence.repository.LocalidadRepository;
import com.car.business.percistence.repository.PaisRepository;
import com.car.business.percistence.repository.ProvinciaRepository;
import com.car.business.percistence.repository.UsuarioRepository;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultAdminUserInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAdminUserInitializer.class);

    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "123456";

    private static final String DEFAULT_COUNTRY = "Argentina";
    private static final String DEFAULT_PROVINCE = "Buenos Aires";
    private static final String DEFAULT_DEPARTMENT = "CABA";
    private static final String DEFAULT_CITY = "Ciudad Autónoma de Buenos Aires";
    private static final String DEFAULT_POSTAL_CODE = "1000";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PaisRepository paisRepository;
    private final ProvinciaRepository provinciaRepository;
    private final DepartamentoRepository departamentoRepository;
    private final LocalidadRepository localidadRepository;

    @Override
    public void run(String... args) {
        if (usuarioRepository.findByNombreUsuarioAndEliminadoFalse(DEFAULT_USERNAME).isPresent()) {
            return;
        }

        Localidad localidad = ensureLocalidadHierarchy();
        Direccion direccion = buildDireccion(localidad);
        Imagen imagen = buildImagen();
        Empleado empleado = buildEmpleado(direccion, imagen);

        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(DEFAULT_USERNAME);
        usuario.setClave(passwordEncoder.encode(DEFAULT_PASSWORD));
        usuario.setRolUsuario(RolUsuario.JEFE);
        usuario.setPersona(empleado);
        usuario.setEliminado(false);

        usuarioRepository.save(usuario);
        LOGGER.info("Usuario administrador por defecto creado (usuario: {}).", DEFAULT_USERNAME);
    }

    private Localidad ensureLocalidadHierarchy() {
        Pais pais = paisRepository.findByNombre(DEFAULT_COUNTRY)
                .orElseGet(() -> {
                    Pais nuevo = new Pais();
                    nuevo.setNombre(DEFAULT_COUNTRY);
                    nuevo.setEliminado(false);
                    return paisRepository.save(nuevo);
                });

        Provincia provincia = provinciaRepository.findByNombreAndPais(DEFAULT_PROVINCE, pais)
                .orElseGet(() -> {
                    Provincia nueva = new Provincia();
                    nueva.setNombre(DEFAULT_PROVINCE);
                    nueva.setPais(pais);
                    nueva.setEliminado(false);
                    return provinciaRepository.save(nueva);
                });

        Departamento departamento = departamentoRepository.findByNombreAndProvincia(DEFAULT_DEPARTMENT, provincia)
                .orElseGet(() -> {
                    Departamento nuevo = new Departamento();
                    nuevo.setNombre(DEFAULT_DEPARTMENT);
                    nuevo.setProvincia(provincia);
                    nuevo.setEliminado(false);
                    return departamentoRepository.save(nuevo);
                });

        Optional<Localidad> existing = localidadRepository
                .findByNombreAndCodigoPostalAndDepartamento(DEFAULT_CITY, DEFAULT_POSTAL_CODE, departamento);
        if (existing.isPresent()) {
            return existing.get();
        }

        Localidad localidad = new Localidad();
        localidad.setNombre(DEFAULT_CITY);
        localidad.setCodigoPostal(DEFAULT_POSTAL_CODE);
        localidad.setDepartamento(departamento);
        localidad.setEliminado(false);
        return localidadRepository.save(localidad);
    }

    private Direccion buildDireccion(Localidad localidad) {
        Direccion direccion = new Direccion();
        direccion.setCalle("Av. Principal");
        direccion.setNumeracion("1000");
        direccion.setBarrio("Centro");
        direccion.setManzanaPiso(null);
        direccion.setCasaDepartamento(null);
        direccion.setReferencia("Sede administrativa");
        direccion.setLocalidad(localidad);
        direccion.setEliminado(false);
        return direccion;
    }

    private Imagen buildImagen() {
        Imagen imagen = new Imagen();
        imagen.setNombre("admin-profile");
        imagen.setMime("image/png");
        imagen.setContenido(new byte[0]);
        imagen.setTipoImagen(TipoImagen.PERSONA);
        imagen.setEliminado(false);
        return imagen;
    }

    private Empleado buildEmpleado(Direccion direccion, Imagen imagen) {
        Empleado empleado = new Empleado();
        empleado.setNombre("Admin");
        empleado.setApellido("Principal");
        empleado.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        empleado.setTipoDocumento(TipoDocumento.DNI);
        empleado.setNumeroDocumento("00000000");
        empleado.setDireccion(direccion);
        empleado.setImagen(imagen);
        empleado.setTipoEmpleado(TipoEmpleado.JEFE);
        empleado.setEliminado(false);
        return empleado;
    }
}
