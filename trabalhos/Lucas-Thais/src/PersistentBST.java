import java.util.ArrayDeque;

final class PersistentBST {
    static final int MAX_VERSIONS = 1001;
    static final String NO_SUCCESSOR = "inf";

    private final Node[] roots = new Node[MAX_VERSIONS];
    private int currentVersion;

    void insert(int key) {
        int version = startNewVersion();
        Node node = new Node(key, null, null, version);
        Node parent = null;
        Field side = Field.LEFT;
        for (Node cur = roots[version]; cur != null; cur = cur.latestChild(side)) {
            parent = cur;
            side = key < cur.key(Node.LATEST) ? Field.LEFT : Field.RIGHT;
        }
        if (parent == null) {
            roots[version] = node;
            return;
        }
        node.liveParent = parent;
        write(parent, side, 0, node);
    }

    void remove(int key) {
        startNewVersion();
        Node node = find(key);
        if (node == null) {
            return;
        }
        Node right = node.latestChild(Field.RIGHT);
        if (node.latestChild(Field.LEFT) != null && right != null) {
            Node successor = minimum(right);
            write(node, Field.KEY, successor.key(Node.LATEST), null);
            node = successor;
        }
        unlink(node);
    }

    String successor(int key, int version) {
        int v = existingVersion(version);
        String answer = NO_SUCCESSOR;
        Node cur = roots[v];
        while (cur != null) {
            int curKey = cur.key(v);
            if (curKey > key) {
                answer = Integer.toString(curKey);
                cur = cur.child(Field.LEFT, v);
            } else {
                cur = cur.child(Field.RIGHT, v);
            }
        }
        return answer;
    }

    String print(int version) {
        int v = existingVersion(version);
        StringBuilder out = new StringBuilder();
        ArrayDeque<Node> stack = new ArrayDeque<>();
        Node cur = roots[v];
        while (cur != null || !stack.isEmpty()) {
            for (; cur != null; cur = cur.child(Field.LEFT, v)) {
                stack.push(cur);
            }
            cur = stack.pop();
            if (!out.isEmpty()) {
                out.append(", ");
            }
            out.append(cur.key(v));
            cur = cur.child(Field.RIGHT, v);
        }
        return out.toString();
    }

    private int startNewVersion() {
        currentVersion++;
        roots[currentVersion] = roots[currentVersion - 1];
        return currentVersion;
    }

    private int existingVersion(int version) {
        return version < 0 || version > currentVersion ? currentVersion : version;
    }

    private Node find(int key) {
        Node cur = roots[currentVersion];
        while (cur != null) {
            int curKey = cur.key(Node.LATEST);
            if (key == curKey) {
                return cur;
            }
            cur = cur.latestChild(key < curKey ? Field.LEFT : Field.RIGHT);
        }
        return null;
    }

    private static Node minimum(Node node) {
        for (Node left = node.latestChild(Field.LEFT); left != null; left = node.latestChild(Field.LEFT)) {
            node = left;
        }
        return node;
    }

    private void unlink(Node node) {
        Node child = node.latestChild(Field.LEFT);
        if (child == null) {
            child = node.latestChild(Field.RIGHT);
        }
        Node parent = node.liveParent;
        if (child != null) {
            child.liveParent = parent;
        }
        if (parent == null) {
            roots[currentVersion] = child;
        } else {
            write(parent, sideOf(parent, node), 0, child);
        }
    }

    private static Field sideOf(Node parent, Node child) {
        return parent.latestChild(Field.LEFT) == child ? Field.LEFT : Field.RIGHT;
    }

    private Node write(Node node, Field field, int key, Node child) {
        if (node.createdVersion == currentVersion) {
            node.setOriginal(field, key, child);
            return node;
        }
        Mod existing = node.findMod(currentVersion, field);
        if (existing != null) {
            existing.key = key;
            existing.child = child;
            return node;
        }
        if (node.hasFreeMod()) {
            node.addMod(new Mod(currentVersion, field, key, child));
            return node;
        }
        return copy(node, field, key, child);
    }

    private Node copy(Node node, Field field, int key, Node child) {
        Node copy = node.copyLatest(currentVersion);
        copy.setOriginal(field, key, child);
        adoptChildren(copy);
        Node parent = node.liveParent;
        if (parent == null) {
            roots[currentVersion] = copy;
        } else {
            copy.liveParent = write(parent, sideOf(parent, node), 0, copy);
        }
        return copy;
    }

    private static void adoptChildren(Node node) {
        if (node.left != null) {
            node.left.liveParent = node;
        }
        if (node.right != null) {
            node.right.liveParent = node;
        }
    }
}
