package com.example.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "BD_INVENTARIO.db";
    private static final int DB_VERSION = 3;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Tabla ROL
        db.execSQL("CREATE TABLE rol (" +
                "id_rol INTEGER PRIMARY KEY, " +
                "nombre_rol TEXT NOT NULL UNIQUE, " +
                "descripcion TEXT)");

        // 2. Tabla USUARIO
        db.execSQL("CREATE TABLE usuario (" +
                "id_usuario INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL UNIQUE, " +
                "password_hash TEXT NOT NULL, " +
                "nombre_completo TEXT NOT NULL, " +
                "id_rol INTEGER NOT NULL, " +
                "activo INTEGER DEFAULT 1, " +
                "FOREIGN KEY (id_rol) REFERENCES rol(id_rol))");

        // 3. Tabla CATEGORIA
        db.execSQL("CREATE TABLE categoria (" +
                "id_categoria INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre_categoria TEXT NOT NULL, " +
                "descripcion TEXT, " +
                "activo INTEGER DEFAULT 1)");

        // 4. Tabla PROVEEDOR
        db.execSQL("CREATE TABLE proveedor (" +
                "id_proveedor INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "rut_empresa TEXT NOT NULL, " +
                "razon_social TEXT NOT NULL, " +
                "contacto TEXT)");

        // 5. Tabla PRODUCTO
        db.execSQL("CREATE TABLE producto (" +
                "id_producto INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "sku_codigo TEXT NOT NULL UNIQUE, " +
                "nombre TEXT NOT NULL, " +
                "descripcion TEXT, " +
                "stock_actual INTEGER NOT NULL DEFAULT 0, " +
                "stock_minimo INTEGER NOT NULL DEFAULT 0, " +
                "id_categoria INTEGER, " +
                "activo INTEGER DEFAULT 1, " +
                "FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria))");

        // 6. Database Seeding (Datos Iniciales)
        seedingDatosIniciales(db);
    }

    private void seedingDatosIniciales(SQLiteDatabase db) {
        // Insertar Roles Básicos
        db.execSQL("INSERT INTO rol (id_rol, nombre_rol, descripcion) VALUES (1, 'ADMINISTRADOR', 'Acceso total a la administración de usuarios, categorías e inventario')");
        db.execSQL("INSERT INTO rol (id_rol, nombre_rol, descripcion) VALUES (2, 'BODEGUERO', 'Acceso limitado a la consulta y gestión de productos y categorías')");

        // Insertar Usuario Administrador Inicial (admin / 1234)
        String passHashAdmin = hashPassword("1234");
        ContentValues adminValues = new ContentValues();
        adminValues.put("username", "admin");
        adminValues.put("password_hash", passHashAdmin);
        adminValues.put("nombre_completo", "Administrador del Sistema");
        adminValues.put("id_rol", 1);
        adminValues.put("activo", 1);
        db.insert("usuario", null, adminValues);

        // Insertar Usuario Bodeguero Inicial (bodeguero / 1234)
        String passHashBodega = hashPassword("1234");
        ContentValues bodegaValues = new ContentValues();
        bodegaValues.put("username", "bodeguero");
        bodegaValues.put("password_hash", passHashBodega);
        bodegaValues.put("nombre_completo", "Juan Bodeguero");
        bodegaValues.put("id_rol", 2);
        bodegaValues.put("activo", 1);
        db.insert("usuario", null, bodegaValues);

        // Insertar Categorías por Defecto
        db.execSQL("INSERT INTO categoria (nombre_categoria, descripcion, activo) VALUES ('Electrónica', 'Equipos y componentes electrónicos', 1)");
        db.execSQL("INSERT INTO categoria (nombre_categoria, descripcion, activo) VALUES ('Herramientas', 'Herramientas de trabajo y mantenimiento', 1)");
        db.execSQL("INSERT INTO categoria (nombre_categoria, descripcion, activo) VALUES ('Oficina', 'Insumos y útiles de oficina', 1)");

        // Insertar Producto de Ejemplo
        ContentValues prodValues = new ContentValues();
        prodValues.put("sku_codigo", "PROD-001");
        prodValues.put("nombre", "Laptop Lenovo 15\"");
        prodValues.put("descripcion", "Intel Core i5 16GB RAM");
        prodValues.put("stock_actual", 5);
        prodValues.put("stock_minimo", 10); // Alerta de stock bajo
        prodValues.put("id_categoria", 1);
        prodValues.put("activo", 1);
        db.insert("producto", null, prodValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS producto");
        db.execSQL("DROP TABLE IF EXISTS proveedor");
        db.execSQL("DROP TABLE IF EXISTS categoria");
        db.execSQL("DROP TABLE IF EXISTS usuario");
        db.execSQL("DROP TABLE IF EXISTS rol");
        db.execSQL("DROP TABLE IF EXISTS persona");
        onCreate(db);
    }

    // Método de utilidad estático para hashear contraseñas
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return password; // Fallback en caso imprevisto
        }
    }

    // Obtener todos los roles
    public List<Rol> obtenerRoles() {
        List<Rol> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT id_rol, nombre_rol, descripcion FROM rol ORDER BY id_rol ASC";
        try (Cursor cursor = db.rawQuery(sql, null)) {
            if (cursor.moveToFirst()) {
                int idIdx = cursor.getColumnIndexOrThrow("id_rol");
                int nomIdx = cursor.getColumnIndexOrThrow("nombre_rol");
                int descIdx = cursor.getColumnIndexOrThrow("descripcion");
                do {
                    Rol rol = new Rol();
                    rol.setIdRol(cursor.getInt(idIdx));
                    rol.setNombreRol(cursor.getString(nomIdx));
                    rol.setDescripcion(cursor.getString(descIdx));
                    lista.add(rol);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Error en base de datos: " + e.getMessage());
        }
        return lista;
    }

    // Obtener todos los usuarios registrados con su nombre de rol
    public List<Usuario> obtenerUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT u.id_usuario, u.username, u.password_hash, u.nombre_completo, u.id_rol, u.activo, r.nombre_rol " +
                "FROM usuario u " +
                "INNER JOIN rol r ON u.id_rol = r.id_rol " +
                "ORDER BY u.id_usuario DESC";
        try (Cursor cursor = db.rawQuery(sql, null)) {
            if (cursor.moveToFirst()) {
                int idIdx = cursor.getColumnIndexOrThrow("id_usuario");
                int userIdx = cursor.getColumnIndexOrThrow("username");
                int passIdx = cursor.getColumnIndexOrThrow("password_hash");
                int nomIdx = cursor.getColumnIndexOrThrow("nombre_completo");
                int rolIdIdx = cursor.getColumnIndexOrThrow("id_rol");
                int actIdx = cursor.getColumnIndexOrThrow("activo");
                int rolNomIdx = cursor.getColumnIndexOrThrow("nombre_rol");

                do {
                    Usuario u = new Usuario();
                    u.setIdUsuario(cursor.getInt(idIdx));
                    u.setUsername(cursor.getString(userIdx));
                    u.setPasswordHash(cursor.getString(passIdx));
                    u.setNombreCompleto(cursor.getString(nomIdx));
                    u.setIdRol(cursor.getInt(rolIdIdx));
                    u.setActivo(cursor.getInt(actIdx));
                    u.setNombreRol(cursor.getString(rolNomIdx));
                    lista.add(u);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Error en base de datos: " + e.getMessage());
        }
        return lista;
    }

    // Registrar nuevo usuario
    public long insertarUsuario(String username, String password, String nombreCompleto, int idRol) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password_hash", hashPassword(password));
        values.put("nombre_completo", nombreCompleto);
        values.put("id_rol", idRol);
        values.put("activo", 1);
        return db.insert("usuario", null, values);
    }

    // Insertar nuevo rol
    public long insertarRol(String nombreRol, String descripcion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre_rol", nombreRol.toUpperCase());
        values.put("descripcion", descripcion);
        return db.insert("rol", null, values);
    }

    // Actualizar rol
    public boolean actualizarRol(int idRol, String nombreRol, String descripcion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre_rol", nombreRol.toUpperCase());
        values.put("descripcion", descripcion);
        try {
            int filas = db.update("rol", values, "id_rol = ?", new String[]{String.valueOf(idRol)});
            return filas > 0;
        } catch (Exception e) {
            Log.e("DBHelper", "Error al actualizar rol: " + e.getMessage());
            return false;
        }
    }

    // Contar usuarios con un rol específico
    public int contarUsuariosConRol(int idRol) {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM usuario WHERE id_rol = ?", new String[]{String.valueOf(idRol)})) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Error al contar usuarios con rol: " + e.getMessage());
        }
        return count;
    }

    // Eliminar rol
    public boolean eliminarRol(int idRol) {
        if (idRol == 1) {
            return false; // No se puede eliminar el rol Administrador principal
        }
        if (contarUsuariosConRol(idRol) > 0) {
            return false; // No se puede eliminar si hay usuarios asignados a este rol
        }
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int filas = db.delete("rol", "id_rol = ?", new String[]{String.valueOf(idRol)});
            return filas > 0;
        } catch (Exception e) {
            Log.e("DBHelper", "Error al eliminar rol: " + e.getMessage());
            return false;
        }
    }

    // Eliminar rol y sus usuarios asociados en transacción
    public boolean eliminarRolConUsuarios(int idRol) {
        if (idRol == 1) {
            return false; // No se puede eliminar el rol Administrador principal
        }
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            db.delete("usuario", "id_rol = ?", new String[]{String.valueOf(idRol)});
            int filas = db.delete("rol", "id_rol = ?", new String[]{String.valueOf(idRol)});
            db.setTransactionSuccessful();
            return filas > 0;
        } catch (Exception e) {
            Log.e("DBHelper", "Error al eliminar rol con usuarios: " + e.getMessage());
            return false;
        } finally {
            db.endTransaction();
        }
    }

    // Actualizar usuario
    public boolean actualizarUsuario(int idUsuario, String username, String password, String nombreCompleto, int idRol) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("nombre_completo", nombreCompleto);
        values.put("id_rol", idRol);
        if (password != null && !password.trim().isEmpty()) {
            values.put("password_hash", hashPassword(password.trim()));
        }
        try {
            int filas = db.update("usuario", values, "id_usuario = ?", new String[]{String.valueOf(idUsuario)});
            return filas > 0;
        } catch (Exception e) {
            Log.e("DBHelper", "Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    // Eliminar usuario
    public boolean eliminarUsuario(int idUsuario) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int filas = db.delete("usuario", "id_usuario = ?", new String[]{String.valueOf(idUsuario)});
            return filas > 0;
        } catch (Exception e) {
            Log.e("DBHelper", "Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }
}
