package com.example.sqlite;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ProductosActivity extends AppCompatActivity {

    private TextInputEditText etBuscarProducto;
    private MaterialButton btnNuevoProducto;
    private ListView lstProductos;

    private DBHelper dbHelper;
    private ArrayList<Producto> listaProductos;
    private ProductoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productos);

        dbHelper = new DBHelper(this);

        etBuscarProducto = findViewById(R.id.etBuscarProducto);
        btnNuevoProducto = findViewById(R.id.btnNuevoProducto);
        lstProductos = findViewById(R.id.lstProductos);

        listaProductos = new ArrayList<>();
        adapter = new ProductoAdapter(this, listaProductos);
        lstProductos.setAdapter(adapter);

        // Botón "Volver": cierra esta pantalla y regresa al menú
        findViewById(R.id.btnVolverProductos).setOnClickListener(v -> finish());

        btnNuevoProducto.setOnClickListener(v -> {
            Intent i = new Intent(ProductosActivity.this, EditarProductoActivity.class);
            startActivity(i);
        });

        // Búsqueda Dinámica y Reactiva por SKU o Nombre
        etBuscarProducto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cargarProductos(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Clic en fila para Editar
        lstProductos.setOnItemClickListener((parent, view, position, id) -> {
            Producto producto = listaProductos.get(position);
            Intent i = new Intent(ProductosActivity.this, EditarProductoActivity.class);
            i.putExtra("id_producto", producto.getIdProducto());
            i.putExtra("sku_codigo", producto.getSkuCodigo());
            i.putExtra("nombre", producto.getNombre());
            i.putExtra("descripcion", producto.getDescripcion());
            i.putExtra("stock_actual", producto.getStockActual());
            i.putExtra("stock_minimo", producto.getStockMinimo());
            i.putExtra("id_categoria", producto.getIdCategoria());
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String filtro = etBuscarProducto.getText() != null ? etBuscarProducto.getText().toString().trim() : "";
        cargarProductos(filtro);
    }

    private void cargarProductos(String filtro) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        listaProductos.clear();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.id_producto, p.sku_codigo, p.nombre, p.descripcion, p.stock_actual, p.stock_minimo, p.id_categoria, c.nombre_categoria ")
                .append("FROM producto p ")
                .append("LEFT JOIN categoria c ON p.id_categoria = c.id_categoria ")
                .append("WHERE p.activo = 1 ");

        String[] selectionArgs = null;
        if (filtro != null && !filtro.isEmpty()) {
            sql.append("AND (p.sku_codigo LIKE ? OR p.nombre LIKE ?) ");
            selectionArgs = new String[]{"%" + filtro + "%", "%" + filtro + "%"};
        }

        sql.append("ORDER BY p.nombre ASC");

        try (Cursor c = db.rawQuery(sql.toString(), selectionArgs)) {
            if (c.moveToFirst()) {
                int idIdx = c.getColumnIndexOrThrow("id_producto");
                int skuIdx = c.getColumnIndexOrThrow("sku_codigo");
                int nomIdx = c.getColumnIndexOrThrow("nombre");
                int descIdx = c.getColumnIndexOrThrow("descripcion");
                int stockActIdx = c.getColumnIndexOrThrow("stock_actual");
                int stockMinIdx = c.getColumnIndexOrThrow("stock_minimo");
                int catIdIdx = c.getColumnIndexOrThrow("id_categoria");
                int catNomIdx = c.getColumnIndexOrThrow("nombre_categoria");

                do {
                    Producto p = new Producto();
                    p.setIdProducto(c.getInt(idIdx));
                    p.setSkuCodigo(c.getString(skuIdx));
                    p.setNombre(c.getString(nomIdx));
                    p.setDescripcion(c.getString(descIdx));
                    p.setStockActual(c.getInt(stockActIdx));
                    p.setStockMinimo(c.getInt(stockMinIdx));
                    p.setIdCategoria(c.getInt(catIdIdx));
                    p.setNombreCategoria(c.getString(catNomIdx) != null ? c.getString(catNomIdx) : "Sin Categoría");
                    p.setActivo(1);

                    listaProductos.add(p);
                } while (c.moveToNext());
            }

            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar inventario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null && db.isOpen()) db.close();
        }
    }

    // Adaptador Personalizado para Ítems de Producto
    private static class ProductoAdapter extends ArrayAdapter<Producto> {

        public ProductoAdapter(@NonNull Context context, @NonNull List<Producto> objects) {
            super(context, 0, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_producto, parent, false);
            }

            Producto producto = getItem(position);
            if (producto != null) {
                TextView tvSku = convertView.findViewById(R.id.tvItemSku);
                TextView tvCategoria = convertView.findViewById(R.id.tvItemCategoria);
                TextView tvNombre = convertView.findViewById(R.id.tvItemNombre);
                TextView tvStockActual = convertView.findViewById(R.id.tvItemStockActual);
                TextView tvStockMinimo = convertView.findViewById(R.id.tvItemStockMinimo);
                TextView tvBajoStockAlert = convertView.findViewById(R.id.tvItemBajoStockAlert);

                tvSku.setText(producto.getSkuCodigo());
                tvCategoria.setText(producto.getNombreCategoria());
                tvNombre.setText(producto.getNombre());
                tvStockActual.setText("Stock: " + producto.getStockActual() + " u.");
                tvStockMinimo.setText("Mínimo: " + producto.getStockMinimo() + " u.");

                // Alerta de Bajo Stock
                if (producto.isStockBajo()) {
                    tvBajoStockAlert.setVisibility(View.VISIBLE);
                } else {
                    tvBajoStockAlert.setVisibility(View.GONE);
                }
            }

            return convertView;
        }
    }
}