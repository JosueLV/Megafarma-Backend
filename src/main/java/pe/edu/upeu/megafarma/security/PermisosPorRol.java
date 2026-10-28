package pe.edu.upeu.megafarma.security;

import java.util.*;
import pe.edu.upeu.megafarma.enums.*;

public final class PermisosPorRol {
  private PermisosPorRol() {}

  public static Set<Permiso> de(Rol rol) {
    return switch (rol) {
      case ADMINISTRADOR -> EnumSet.allOf(Permiso.class);
      case INVENTARIO ->
          EnumSet.of(
              Permiso.PRODUCTO_LEER,
              Permiso.PRODUCTO_CREAR,
              Permiso.INVENTARIO_LEER,
              Permiso.ENTRADA_CREAR);
      case VENTAS -> EnumSet.of(Permiso.PRODUCTO_LEER, Permiso.INVENTARIO_LEER);
    };
  }

  public static List<String> nombres(Rol rol) {
    return de(rol).stream().map(Enum::name).sorted().toList();
  }
}
