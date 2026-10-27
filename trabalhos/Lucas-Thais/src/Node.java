final class Node {
    static final int MAX_MODS = 2;
    static final int LATEST = Integer.MAX_VALUE;

    int key;
    Node left;
    Node right;
    Node liveParent;
    final int createdVersion;
    private final Mod[] mods = new Mod[MAX_MODS];
    private int modCount;

    Node(int key, Node left, Node right, int createdVersion) {
        this.key = key;
        this.left = left;
        this.right = right;
        this.createdVersion = createdVersion;
    }

    int key(int version) {
        int value = key;
        for (int i = 0; i < modCount && mods[i].version <= version; i++) {
            if (mods[i].field == Field.KEY) {
                value = mods[i].key;
            }
        }
        return value;
    }

    Node child(Field side, int version) {
        Node value = side == Field.LEFT ? left : right;
        for (int i = 0; i < modCount && mods[i].version <= version; i++) {
            if (mods[i].field == side) {
                value = mods[i].child;
            }
        }
        return value;
    }

    Node latestChild(Field side) {
        return child(side, LATEST);
    }

    void setOriginal(Field field, int newKey, Node newChild) {
        switch (field) {
            case KEY -> key = newKey;
            case LEFT -> left = newChild;
            case RIGHT -> right = newChild;
        }
    }

    Mod findMod(int version, Field field) {
        for (int i = modCount - 1; i >= 0 && mods[i].version == version; i--) {
            if (mods[i].field == field) {
                return mods[i];
            }
        }
        return null;
    }

    boolean hasFreeMod() {
        return modCount < MAX_MODS;
    }

    void addMod(Mod mod) {
        mods[modCount++] = mod;
    }

    Node copyLatest(int version) {
        return new Node(key(LATEST), latestChild(Field.LEFT), latestChild(Field.RIGHT), version);
    }
}
