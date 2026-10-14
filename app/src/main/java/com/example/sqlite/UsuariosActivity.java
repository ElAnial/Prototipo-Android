package com.example.sqlite;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class UsuariosActivity extends AppCompatActivity {

    // Vistas Usuarios
    private TextInputEditText etUsername, etNombreCompleto, etPassword;
    private Spinner spRol;
    private MaterialButton btnGuardarUsuario;
    private ListView lstUsuarios;

    // Vistas Roles
    private TextInputEditText etNombreRol, etDescripcionRol;
    private MaterialButton btnGuardarRol;
    private ListView lstRoles;

    // Tabs
    private MaterialButton btnTabUsuarios, btnTabRoles;
    private LinearLayout layoutUsuariosContainer, layoutRolesContainer;

    private DBHelper dbHelper;
    private SessionManager session;
    private List<Rol> listaRolesSpinner;
    private List<Rol> listaRolesList;
    private List<Usuario> listaUsuarios;
    private UsuarioAdapter usuarioAdapter;
    private RolAdapter rolAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);

        // Control de Sesión y Permisos: Solo Administrador (id_rol == 1)
        if (!session.isLoggedIn()) {
            finish();
            return;
        }

        if (session.getRoleId() != 1) {
            Toast.makeText(this, "Acceso no autorizado: Módulo exclusivo para Administradores", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_usuarios);

        dbHelper = new DBHelper(this);

        // Inicializar vistas de Usuarios
        etUsername = findViewById(R.id.etUsername);
        etNombreCompleto = findViewById(R.id.etNombreCompleto);
        etPassword = findViewById(R.id.etPassword);
        spRol = findViewById(R.id.spRol);
        btnGuardarUsuario = findViewById(R.id.btnGuardarUsuario);
        lstUsuarios = findViewById(R.id.lstUsuarios);

        // Inicializar vistas de Roles
        etNombreRol = findViewById(R.id.etNombreRol);
        etDescripcionRol = findViewById(R.id.etDescripcionRol);
        btnGuardarRol = findViewById(R.id.btnGuardarRol);
        lstRoles = findViewById(R.id.lstRoles);

        // Tabs y Containers
        btnTabUsuarios = findViewById(R.id.btnTabUsuarios);
        btnTabRoles = findViewById(R.id.btnTabRoles);
        layoutUsuariosContainer = findViewById(R.id.layoutUsuariosContainer);
        layoutRolesContainer = findViewById(R.id.layoutRolesContainer);

        // Adaptadores
        listaUsuarios = new ArrayList<>();
        usuarioAdapter = new UsuarioAdapter(this, listaUsuarios);
        lstUsuarios.setAdapter(usuarioAdapter);

        listaRolesSpinner = new ArrayList<>();
        listaRolesList = new ArrayList<>();
        rolAdapter = new RolAdapter(this, listaRolesList);
        lstRoles.setAdapter(rolAdapter);

        cargarRolesEnSpinner();
        cargarUsuarios();
        cargarRoles();

        // Listeners de pestañas por defecto en Usuarios
        seleccionarTab(true);

        btnTabUsuarios.setOnClickListener(v -> seleccionarTab(true));
        btnTabRoles.setOnClickListener(v -> seleccionarTab(false));

        btnGuardarUsuario.setOnClickListener(v -> guardarUsuario());
        btnGuardarRol.setOnClickListener(v -> guardarRol());

        // Click en elementos de usuarios para Editar o Eliminar
        lstUsuarios.setOnItemClickListener((parent, view, position, id) -> {
            Usuario usuarioSeleccionado = listaUsuarios.get(position);
            mostrarOpcionesUsuario(usuarioSeleccionado);
        });

        // Click en elementos de roles para Editar o Eliminar (Solo Administrador)
        lstRoles.setOnItemClickListener((parent, view, position, id) -> {
            Rol rolSeleccionado = listaRolesList.get(position);
            mostrarOpcionesRol(rolSeleccionado);
        });

        // Botón "Volver": cierra esta pantalla y regresa al menú
        findViewById(R.id.btnVolverUsuarios).setOnClickListener(v -> finish());
    }

    private void seleccionarTab(boolean esUsuarios) {
        if (esUsuarios) {
            if (layoutUsuariosContainer != null) layoutUsuariosContainer.setVisibility(View.VISIBLE);
            if (layoutRolesContainer != null) layoutRolesContainer.setVisibility(View.GONE);

            if (btnTabUsuarios != null) {
                btnTabUsuarios.setBackgroundColor(getResources().getColor(R.color.primary));
                btnTabUsuarios.setTextColor(getResources().getColor(android.R.color.white));
            }
            if (btnTabRoles != null) {
                btnTabRoles.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                btnTabRoles.setTextColor(getResources().getColor(R.color.primary));
            }
        } else {
            if (layoutUsuariosContainer != null) layoutUsuariosContainer.setVisibility(View.GONE);
            if (layoutRolesContainer != null) layoutRolesContainer.setVisibility(View.VISIBLE);

            if (btnTabRoles != null) {
                btnTabRoles.setBackgroundColor(getResources().getColor(R.color.primary));
                btnTabRoles.setTextColor(getResources().getColor(android.R.color.white));
            }
            if (btnTabUsuarios != null) {
                btnTabUsuarios.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                btnTabUsuarios.setTextColor(getResources().getColor(R.color.primary));
            }
        }
    }

    private void cargarRolesEnSpinner() {
        listaRolesSpinner.clear();
        listaRolesSpinner.addAll(dbHelper.obtenerRoles());
        ArrayAdapter<Rol> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                listaRolesSpinner
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRol.setAdapter(spinnerAdapter);
    }

    private void cargarUsuarios() {
        listaUsuarios.clear();
        List<Usuario> usuarios = dbHelper.obtenerUsuarios();
        listaUsuarios.addAll(usuarios);
        usuarioAdapter.notifyDataSetChanged();
    }

    private void cargarRoles() {
        listaRolesList.clear();
        List<Rol> roles = dbHelper.obtenerRoles();
        listaRolesList.addAll(roles);
        rolAdapter.notifyDataSetChanged();
    }

    private void guardarUsuario() {
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String nombreCompleto = etNombreCompleto.getText() != null ? etNombreCompleto.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Ingrese el nombre de usuario");
            return;
        }

        if (TextUtils.isEmpty(nombreCompleto)) {
            etNombreCompleto.setError("Ingrese el nombre completo");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Ingrese la contraseña");
            return;
        }

        Rol rolSeleccionado = (Rol) spRol.getSelectedItem();
        if (rolSeleccionado == null) {
            Toast.makeText(this, "Seleccione un rol válido", Toast.LENGTH_SHORT).show();
            return;
        }

        long resultado = dbHelper.insertarUsuario(username, password, nombreCompleto, rolSeleccionado.getIdRol());

        if (resultado != -1) {
            Toast.makeText(this, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show();
            limpiarFormularioUsuario();
            cargarUsuarios();
        } else {
            Toast.makeText(this, "Error: El nombre de usuario '" + username + "' ya existe", Toast.LENGTH_LONG).show();
        }
    }

    private void guardarRol() {
        String nombreRol = etNombreRol.getText() != null ? etNombreRol.getText().toString().trim() : "";
        String descripcion = etDescripcionRol.getText() != null ? etDescripcionRol.getText().toString().trim() : "";

        if (TextUtils.isEmpty(nombreRol)) {
            etNombreRol.setError("Ingrese el nombre del rol");
            return;
        }

        long resultado = dbHelper.insertarRol(nombreRol, descripcion);
        if (resultado != -1) {
            Toast.makeText(this, "Rol registrado exitosamente", Toast.LENGTH_SHORT).show();
            limpiarFormularioRol();
            cargarRoles();
            cargarRolesEnSpinner();
        } else {
            Toast.makeText(this, "Error: El rol '" + nombreRol + "' ya existe o no es válido", Toast.LENGTH_LONG).show();
        }
    }

    private void limpiarFormularioUsuario() {
        etUsername.setText("");
        etNombreCompleto.setText("");
        etPassword.setText("");
        etUsername.clearFocus();
        etNombreCompleto.clearFocus();
        etPassword.clearFocus();
        if (spRol.getAdapter() != null && spRol.getAdapter().getCount() > 0) {
            spRol.setSelection(0);
        }
    }

    private void limpiarFormularioRol() {
        etNombreRol.setText("");
        etDescripcionRol.setText("");
        etNombreRol.clearFocus();
        etDescripcionRol.clearFocus();
    }

    // Opciones de Usuario (Editar / Eliminar)
    private void mostrarOpcionesUsuario(Usuario usuario) {
        new AlertDialog.Builder(this)
                .setTitle("Usuario: @" + usuario.getUsername())
                .setItems(new String[]{"Editar", "Eliminar"}, (dialog, which) -> {
                    if (which == 0) {
                        mostrarDialogoEditarUsuario(usuario);
                    } else {
                        confirmarEliminarUsuario(usuario);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoEditarUsuario(Usuario usuario) {
        View vista = getLayoutInflater().inflate(R.layout.dialog_editar_usuario, null);
        TextInputEditText etEditUsername = vista.findViewById(R.id.etEditUsername);
        TextInputEditText etEditNombreCompleto = vista.findViewById(R.id.etEditNombreCompleto);
        TextInputEditText etEditPassword = vista.findViewById(R.id.etEditPassword);
        Spinner spEditRol = vista.findViewById(R.id.spEditRol);

        etEditUsername.setText(usuario.getUsername());
        etEditNombreCompleto.setText(usuario.getNombreCompleto());

        List<Rol> roles = dbHelper.obtenerRoles();
        ArrayAdapter<Rol> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                roles
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEditRol.setAdapter(spinnerAdapter);

        for (int i = 0; i < roles.size(); i++) {
            if (roles.get(i).getIdRol() == usuario.getIdRol()) {
                spEditRol.setSelection(i);
                break;
            }
        }

        AlertDialog dialogo = new AlertDialog.Builder(this)
                .setTitle("Editar Usuario")
                .setView(vista)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();
        dialogo.show();

        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nuevoUser = etEditUsername.getText() != null ? etEditUsername.getText().toString().trim() : "";
            String nuevoNombre = etEditNombreCompleto.getText() != null ? etEditNombreCompleto.getText().toString().trim() : "";
            String nuevaPass = etEditPassword.getText() != null ? etEditPassword.getText().toString().trim() : "";
            Rol rolSeleccionado = (Rol) spEditRol.getSelectedItem();

            if (TextUtils.isEmpty(nuevoUser)) {
                etEditUsername.setError("Ingrese el nombre de usuario");
                return;
            }

            if (TextUtils.isEmpty(nuevoNombre)) {
                etEditNombreCompleto.setError("Ingrese el nombre completo");
                return;
            }

            if (rolSeleccionado == null) {
                Toast.makeText(this, "Seleccione un rol válido", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dbHelper.actualizarUsuario(usuario.getIdUsuario(), nuevoUser, nuevaPass, nuevoNombre, rolSeleccionado.getIdRol())) {
                Toast.makeText(this, "Usuario actualizado correctamente", Toast.LENGTH_SHORT).show();
                dialogo.dismiss();
                cargarUsuarios();
            } else {
                Toast.makeText(this, "Error al actualizar el usuario (Username duplicado)", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmarEliminarUsuario(Usuario usuario) {
        if (usuario.getIdUsuario() == 1 || "admin".equalsIgnoreCase(usuario.getUsername())) {
            Toast.makeText(this, "No se puede eliminar el usuario Administrador principal", Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Eliminar Usuario")
                .setMessage("¿Está seguro de eliminar al usuario '@" + usuario.getUsername() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    if (dbHelper.eliminarUsuario(usuario.getIdUsuario())) {
                        Toast.makeText(this, "Usuario eliminado correctamente", Toast.LENGTH_SHORT).show();
                        cargarUsuarios();
                    } else {
                        Toast.makeText(this, "No se pudo eliminar el usuario", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Opciones de Rol (Editar / Eliminar) para Administrador
    private void mostrarOpcionesRol(Rol rol) {
        new AlertDialog.Builder(this)
                .setTitle("Rol: " + rol.getNombreRol())
                .setItems(new String[]{"Editar", "Eliminar"}, (dialog, which) -> {
                    if (which == 0) {
                        mostrarDialogoEditarRol(rol);
                    } else {
                        confirmarEliminarRol(rol);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoEditarRol(Rol rol) {
        View vista = getLayoutInflater().inflate(R.layout.dialog_editar_rol, null);
        TextInputEditText etEditNombre = vista.findViewById(R.id.etEditNombreRol);
        TextInputEditText etEditDesc = vista.findViewById(R.id.etEditDescripcionRol);

        etEditNombre.setText(rol.getNombreRol());
        etEditDesc.setText(rol.getDescripcion());

        AlertDialog dialogo = new AlertDialog.Builder(this)
                .setTitle("Editar Rol")
                .setView(vista)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();
        dialogo.show();

        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nuevoNombre = etEditNombre.getText() != null ? etEditNombre.getText().toString().trim() : "";
            String nuevaDesc = etEditDesc.getText() != null ? etEditDesc.getText().toString().trim() : "";

            if (TextUtils.isEmpty(nuevoNombre)) {
                etEditNombre.setError("Ingrese el nombre del rol");
                return;
            }

            if (dbHelper.actualizarRol(rol.getIdRol(), nuevoNombre, nuevaDesc)) {
                Toast.makeText(this, "Rol actualizado correctamente", Toast.LENGTH_SHORT).show();
                dialogo.dismiss();
                cargarRoles();
                cargarRolesEnSpinner();
                cargarUsuarios();
            } else {
                Toast.makeText(this, "Error al actualizar el rol (Nombre duplicado)", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmarEliminarRol(Rol rol) {
        if (rol.getIdRol() == 1) {
            Toast.makeText(this, "No se puede eliminar el rol Administrador principal", Toast.LENGTH_LONG).show();
            return;
        }

        int usuariosAsignados = dbHelper.contarUsuariosConRol(rol.getIdRol());
        if (usuariosAsignados > 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Eliminar Rol con Usuarios")
                    .setMessage("Este rol tiene " + usuariosAsignados + " usuario(s) asociado(s). Si continúa, se eliminarán el rol y sus usuarios asociados. ¿Desea continuar?")
                    .setPositiveButton("Eliminar Todo", (dialog, which) -> {
                        if (dbHelper.eliminarRolConUsuarios(rol.getIdRol())) {
                            Toast.makeText(this, "Rol y usuarios asociados eliminados correctamente", Toast.LENGTH_SHORT).show();
                            cargarRoles();
                            cargarRolesEnSpinner();
                            cargarUsuarios();
                        } else {
                            Toast.makeText(this, "No se pudo eliminar el rol", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Eliminar Rol")
                .setMessage("¿Está seguro de eliminar el rol '" + rol.getNombreRol() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    if (dbHelper.eliminarRol(rol.getIdRol())) {
                        Toast.makeText(this, "Rol eliminado correctamente", Toast.LENGTH_SHORT).show();
                        cargarRoles();
                        cargarRolesEnSpinner();
                    } else {
                        Toast.makeText(this, "No se pudo eliminar el rol", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Adaptador para mostrar la lista de usuarios
    private static class UsuarioAdapter extends ArrayAdapter<Usuario> {

        public UsuarioAdapter(@NonNull Context context, @NonNull List<Usuario> objects) {
            super(context, 0, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_usuario, parent, false);
            }

            Usuario usuario = getItem(position);
            if (usuario != null) {
                TextView tvUsername = itemView.findViewById(R.id.tvItemUsername);
                TextView tvRol = itemView.findViewById(R.id.tvItemRol);
                TextView tvEstado = itemView.findViewById(R.id.tvItemEstado);
                TextView tvNombreCompleto = itemView.findViewById(R.id.tvItemNombreCompleto);

                tvUsername.setText("@" + usuario.getUsername());
                tvNombreCompleto.setText(usuario.getNombreCompleto());

                String nombreRol = usuario.getNombreRol() != null ? usuario.getNombreRol().toUpperCase() : "ROL " + usuario.getIdRol();
                tvRol.setText(nombreRol);

                // Estilo según el Rol
                if (usuario.getIdRol() == 1) { // ADMINISTRADOR
                    tvRol.setBackgroundColor(0xFFE8F5E9); // Verde claro
                    tvRol.setTextColor(0xFF2E7D32);
                } else { // BODEGUERO u otros
                    tvRol.setBackgroundColor(0xFFFFF3E0); // Naranja claro
                    tvRol.setTextColor(0xFFE65100);
                }

                tvEstado.setText(usuario.getActivo() == 1 ? "Activo" : "Inactivo");
            }

            return itemView;
        }
    }

    // Adaptador robusto para mostrar la lista de roles
    private static class RolAdapter extends ArrayAdapter<Rol> {

        public RolAdapter(@NonNull Context context, @NonNull List<Rol> objects) {
            super(context, 0, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            }

            Rol rol = getItem(position);
            if (rol != null) {
                TextView text1 = itemView.findViewById(android.R.id.text1);
                TextView text2 = itemView.findViewById(android.R.id.text2);
                if (text1 != null) {
                    text1.setText("ID: " + rol.getIdRol() + " - " + rol.getNombreRol());
                    text1.setTextColor(0xFF000000);
                    text1.setTypeface(null, android.graphics.Typeface.BOLD);
                }
                if (text2 != null) {
                    text2.setText(TextUtils.isEmpty(rol.getDescripcion()) ? "Sin descripción" : rol.getDescripcion());
                    text2.setTextColor(0xFF666666);
                }
            }

            return itemView;
        }
    }
}
