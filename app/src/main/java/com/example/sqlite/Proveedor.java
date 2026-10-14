package com.example.sqlite;

public class Proveedor {
    private int idProveedor;
    private String rutEmpresa;
    private String razonSocial;
    private String contacto;

    public Proveedor() {}

    public Proveedor(int idProveedor, String rutEmpresa, String razonSocial, String contacto) {
        this.idProveedor = idProveedor;
        this.rutEmpresa = rutEmpresa;
        this.razonSocial = razonSocial;
        this.contacto = contacto;
    }

    public int getIdProveedor() { return idProveedor; }
    public void setIdProveedor(int idProveedor) { this.idProveedor = idProveedor; }

    public String getRutEmpresa() { return rutEmpresa; }
    public void setRutEmpresa(String rutEmpresa) { this.rutEmpresa = rutEmpresa; }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }
}
