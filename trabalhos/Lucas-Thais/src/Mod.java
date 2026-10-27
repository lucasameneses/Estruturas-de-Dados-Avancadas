final class Mod {
    final int version;
    final Field field;
    int key;
    Node child;

    Mod(int version, Field field, int key, Node child) {
        this.version = version;
        this.field = field;
        this.key = key;
        this.child = child;
    }
}
