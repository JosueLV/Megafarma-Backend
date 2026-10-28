package pe.edu.upeu.megafarma.config;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.megafarma.entity.*;
import pe.edu.upeu.megafarma.enums.Rol;
import pe.edu.upeu.megafarma.repository.*;

@Component
public class DatosIniciales implements ApplicationRunner {
  private final UsuarioRepository usuarios;
  private final ProductoRepository productos;
  private final PasswordEncoder encoder;
  private final Clock clock;
  private final Environment env;
  private final boolean demo;
  private final String admin;
  private final String password;

  public DatosIniciales(
      UsuarioRepository usuarios,
      ProductoRepository productos,
      PasswordEncoder encoder,
      Clock clock,
      Environment env,
      @Value("${app.demo.enabled:false}") boolean demo,
      @Value("${app.bootstrap.username:}") String admin,
      @Value("${app.bootstrap.password:}") String password) {
    this.usuarios = usuarios;
    this.productos = productos;
    this.encoder = encoder;
    this.clock = clock;
    this.env = env;
    this.demo = demo;
    this.admin = admin;
    this.password = password;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    boolean prod = env.acceptsProfiles(Profiles.of("prod"));
    if (prod && demo) throw new IllegalStateException("Los datos demo están prohibidos en prod");
    if (prod && usuarios.count() == 0) {
      if (!admin.matches("[a-z0-9._-]{3,60}")
          || password.length() < 12
          || password.getBytes(StandardCharsets.UTF_8).length > 72)
        throw new IllegalStateException(
            "Primer arranque: define ADMIN_USERNAME válido y ADMIN_PASSWORD entre 12 caracteres y"
                + " 72 bytes");
      crearUsuario(admin, password, "Administrador Megafarma", Rol.ADMINISTRADOR);
    }
    if (demo) {
      crearUsuario("admin", "AdminDemo2026!", "Administrador de demostración", Rol.ADMINISTRADOR);
      crearUsuario(
          "inventario", "InventarioDemo2026!", "Responsable de inventario", Rol.INVENTARIO);
      crearUsuario("ventas", "VentasDemo2026!", "Personal de ventas", Rol.VENTAS);
      if (!productos.existsByCodigo("DEMO-PAR500")) {
        Producto p = new Producto();
        p.setCodigo("DEMO-PAR500");
        p.setNombre("Paracetamol 500 mg · demo");
        p.setPresentacion("Tableta");
        p.setPrincipioActivo("Paracetamol");
        p.setPrecioVenta(new BigDecimal("0.50"));
        p.setStockMinimo(10);
        p.setCreadoEn(clock.instant());
        p.setCreadoPor("admin");
        productos.save(p);
      }
    }
  }

  private void crearUsuario(String username, String password, String nombre, Rol rol) {
    if (usuarios.findByUsername(username).isEmpty()) {
      Usuario u = new Usuario();
      u.setUsername(username);
      u.setPasswordHash(encoder.encode(password));
      u.setNombreCompleto(nombre);
      u.setRol(rol);
      usuarios.save(u);
    }
  }
}
