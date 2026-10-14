package com.example.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class EditarProductoActivity extends AppCompatActivity {

    private TextView tvTituloHeader;
    private TextInputEditText etSku, etNombre, etDescripcion, etStockActual, etStockMinimo;
    private Spinner spnCategoria;
    private MaterialButton btnGuardar, btnEliminar, btnVolver;

    private DBHelper dbHelper;
    private ArrayList<Categoria> listaCategorias;
    private ArrayAdapter<Categoria> adapterCategorias;

    private int idProducto = -1; // -1 indica creación de nuevo producto

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_producto);

        dbHelper = new DBHelper(this);

        tvTituloHeader = findViewById(R.id.tvTituloHeader);
        etSku = findViewById(R.id.etSku);
        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        etStockActual = findViewById(R.id.etStockActual);
        etStockMinimo = findViewById(R.id.etStockMinimo);
        spnCategoria = findViewById(R.id.spnCategoria);

        btnGuardar = findViewById(R.id.btnGuardar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnVolver = findViewById(R.id.btnVolver);

        listaCategorias = new ArrayList<>();
        adapterCategorias = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listaCategorias);
        spnCategoria.setAdapter(adapterCategorias);

        cargarCategoriasSpinner();

        // Recuperar Extras del Intent
        if (getIntent() != null && getIntent().hasExtra("id_producto")) {
            idProducto = getIntent().getIntExtra("id_producto", -1);
            String sku = getIntent().getStringExtra("sku_codigo");
            String nombre = getIntent().getStringExtra("nombre");
            String desc = getIntent().getStringExtra("descripcion");
            int stockAct = getIntent().getIntExtra("stock_actual", 0);
            int stockMin = getIntent().getIntExtra("stock_minimo", 0);
            int idCat = getIntent().getIntExtra("id_categoria", -1);

            tvTituloHeader.setText("Editar Producto");
            etSku.setText(sku);
            etNombre.setText(nombre);
            etDescripcion.setText(desc);
            etStockActual.setText(String.valueOf(stockAct));
            etStockMinimo.setText(String.valueOf(stockMin));

            seleccionarCategoriaEnSpinner(idCat);
            btnEliminar.setVisibility(View.VISIBLE);
        } else {
            tvTituloHeader.setText("Nuevo Producto");
            btnEliminar.setVisibility(View.GONE);
        }

        btnGuardar.setOnClickListener(v -> guardarOActualizar());
        btnEliminar.setOnClickListener(v -> eliminarBajaLogica());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void cargarCategoriasSpinner() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        listaCategorias.clear();

        try (Cursor c = db.rawQuery("SELECT id_categoria, nombre_categoria, descripcion, activo FROM categoria WHERE activo = 1 ORDER BY nombre_categoria ASC", null)) {
            if (c.moveToFirst()) {
                int idIdx = c.getColumnIndexOrThrow("id_categoria");
                int nomIdx = c.getColumnIndexOrThrow("nombre_categoria");
                int descIdx = c.getColumnIndexOrThrow("descripcion");

                do {
                    Categoria cat = new Categoria();
                    cat.setIdCategoria(c.getInt(idIdx));
                    cat.setNombreCategoria(c.getString(nomIdx));
                    cat.setDescripcion(c.getString(descIdx));
                    cat.setActivo(1);
                    listaCategorias.add(cat);
                } while (c.moveToNext());
            }
            adapterCategorias.notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar categorías: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null && db.isOpen()) db.close();
        }
    }

    private void seleccionarCategoriaEnSpinner(int idCategoriaTarget) {
        for (int i = 0; i < listaCategorias.size(); i++) {
            if (listaCategorias.get(i).getIdCategoria() == idCategoriaTarget) {
                spnCategoria.setSelection(i);
                break;
            }
        }
    }

    private void guardarOActualizar() {
        String sku = etSku.getText() != null ? etSku.getText().toString().trim() : "";
        String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        String desc = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";
        String stockActStr = etStockActual.getText() != null ? etStockActual.getText().toString().trim() : "";
        String stockMinStr = etStockMinimo.getText() != null ? etStockMinimo.getText().toString().trim() : "";

        if (TextUtils.isEmpty(sku)) {
            etSku.setError("Ingrese el código SKU");
            return;
        }

        if (TextUtils.isEmpty(nombre)) {
            etNombre.setError("Ingrese el nombre del producto");
            return;
        }

        if (TextUtils.isEmpty(stockActStr)) {
            etStockActual.setError("Ingrese el stock actual");
            return;
        }

        if (TextUtils.isEmpty(stockMinStr)) {
            etStockMinimo.setError("Ingrese el stock mínimo");
            return;
        }

        if (spnCategoria.getSelectedItem() == null) {
            Toast.makeText(this, "Debe registrar o seleccionar una categoría válida", Toast.LENGTH_SHORT).show();
            return;
        }

        int stockActual = Integer.parseInt(stockActStr);
        int stockMinimo = Integer.parseInt(stockMinStr);
        Categoria categoriaSeleccionada = (Categoria) spnCategoria.getSelectedItem();
        int idCategoria = categoriaSeleccionada.getIdCategoria();

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            // Verificar unicidad de SKU
            String checkSkuSql = "SELECT COUNT(*) FROM producto WHERE sku_codigo = ? AND id_producto != ?";
            try (Cursor c = db.rawQuery(checkSkuSql, new String[]{sku, String.valueOf(idProducto)})) {
                if (c.moveToFirst() && c.getInt(0) > 0) {
                    etSku.setError("Este código SKU ya está registrado en otro producto");
                    return;
                }
            }

            if (idProducto == -1) {
                // Nuevo producto
                ContentValues cv = new ContentValues();
                cv.put("sku_codigo", sku);
                cv.put("nombre", nombre);
                cv.put("descripcion", desc);
                cv.put("stock_actual", stockActual);
                cv.put("stock_minimo", stockMinimo);
                cv.put("id_categoria", idCategoria);
                cv.put("activo", 1);

                long newId = db.insert("producto", null, cv);
                if (newId != -1) {
                    Toast.makeText(this, "Producto registrado correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Error al registrar producto", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Actualizar producto
                String sqlUpdate = "UPDATE producto SET sku_codigo = ?, nombre = ?, descripcion = ?, stock_actual = ?, stock_minimo = ?, id_categoria = ? WHERE id_producto = ?";
                try (SQLiteStatement stmt = db.compileStatement(sqlUpdate)) {
                    stmt.bindString(1, sku);
                    stmt.bindString(2, nombre);
                    stmt.bindString(3, desc);
                    stmt.bindLong(4, stockActual);
                    stmt.bindLong(5, stockMinimo);
                    stmt.bindLong(6, idCategoria);
                    stmt.bindLong(7, idProducto);

                    int filasAfectadas = stmt.executeUpdateDelete();
                    if (filasAfectadas > 0) {
                        Toast.makeText(this, "Producto actualizado correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "No se pudo actualizar el producto", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error guardando producto: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (db != null && db.isOpen()) db.close();
        }
    }

    private void eliminarBajaLogica() {
        if (idProducto == -1) return;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            String sqlBaja = "UPDATE producto SET activo = 0 WHERE id_producto = ?";
            try (SQLiteStatement stmt = db.compileStatement(sqlBaja)) {
                stmt.bindLong(1, idProducto);
                int filas = stmt.executeUpdateDelete();
                if (filas > 0) {
                    Toast.makeText(this, "Producto dado de baja correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "No se encontró el producto", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (db != null && db.isOpen()) db.close();
        }
    }
}
