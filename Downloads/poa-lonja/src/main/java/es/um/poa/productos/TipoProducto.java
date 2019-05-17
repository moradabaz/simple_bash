package es.um.poa.productos;

public enum TipoProducto {
    DESCONOCIDO("desconocido"),
    PESCADO_AZUL("pescado azul"),
    PESCADO_GRIS("pescado gris"),
    CEFALOPODO("cefalopodo"),
    CRUSTACEO("crustaceo"),
    MARISCO("marisco");

    private String name;

    TipoProducto(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static TipoProducto getType(String name) {
        for (int i = 0; i < values().length; i++) {
            if (name.equals(values()[i].getName()))
                return values()[i];
        }
        return DESCONOCIDO;
    }

    @Override
    public String toString() {
        return name;
    }
}
