class BTreeNode {
    int[] keys;
    BTreeNode[] children;
    boolean leaf;
    int n; // Количество текущих ключей в узле

    public BTreeNode(int t, boolean leaf) {
        this.keys = new int[2 * t - 1];
        this.children = new BTreeNode[2 * t];
        this.leaf = leaf;
        this.n = 0;
    }
}

