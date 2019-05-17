package es.um.poa.productos;

public enum TipoProducto {
    DESCONOCIDO("desconocido"),
    PESCADO_AZUL("pescado azul"),
    PESCADO_GRIS("pescado gris"),
    CEFALOPODO("cefalopodo"),
    CRUSTACEO("crustaceo");

    private String name;

    TipoProducto(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public TipoProducto getType(String name) {
        if (this.getName().equals(name))
            return this;
        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}
