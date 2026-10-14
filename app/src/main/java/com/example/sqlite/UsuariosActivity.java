package com.example.sqlite;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class UsuariosActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etNombreCompleto, etPassword;
    private Spinner spRol;
    private MaterialButton btnGuardarUsuario;
    private ListView lstUsuarios;

    private DBHelper dbHelper;
    private SessionManager session;
    private List<Rol> listaRoles;
    private List<Usuario> listaUsuarios;
    private UsuarioAdapter usuarioAdapter;

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

        etUsername = findViewById(R.id.etUsername);
        etNombreCompleto = findViewById(R.id.etNombreCompleto);
        etPassword = findViewById(R.id.etPassword);
        spRol = findViewById(R.id.spRol);
        btnGuardarUsuario = findViewById(R.id.btnGuardarUsuario);
        lstUsuarios = findViewById(R.id.lstUsuarios);

        listaUsuarios = new ArrayList<>();
        usuarioAdapter = new UsuarioAdapter(this, listaUsuarios);
        lstUsuarios.setAdapter(usuarioAdapter);

        cargarRolesEnSpinner();
        cargarUsuarios();

        btnGuardarUsuario.setOnClickListener(v -> guardarUsuario());

        // Botón "Volver": cierra esta pantalla y regresa al menú
        findViewById(R.id.btnVolverUsuarios).setOnClickListener(v -> finish());
    }

    private void cargarRolesEnSpinner() {
        listaRoles = dbHelper.obtenerRoles();
        ArrayAdapter<Rol> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                listaRoles
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
            limpiarFormulario();
            cargarUsuarios();
        } else {
            Toast.makeText(this, "Error: El nombre de usuario '" + username + "' ya existe", Toast.LENGTH_LONG).show();
        }
    }

    private void limpiarFormulario() {
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
}