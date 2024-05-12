import java.util.LinkedList;
import java.util.Queue;

public class BTree {
    private BTreeNode root;
    private int t; // Минимальная степень B-дерева
    private long insertOperations;
    private long searchOperations;
    private long deleteOperations;

    // Конструктор
    public BTree(int t) {
        this.t = t;
        root = null;
        insertOperations = 0;
        searchOperations = 0;
        deleteOperations = 0;
    }

    // Метод вставки ключа
    public void insert(int key) {
        if (root == null) {
            root = new BTreeNode(t, true);
            root.keys[0] = key;
            root.n = 1;
        } else {
            if (root.n == 2 * t - 1) {
                BTreeNode newNode = new BTreeNode(t, false);
                newNode.children[0] = root;
                splitChild(newNode, 0);
                root = newNode;
            } else {
                insertNonFull(root, key);
            }
        }
    }

    // Рекурсивное добавление ключа в не-полный узел
    private void insertNonFull(BTreeNode node, int key) {
        int i = node.n - 1;
        if (node.leaf) {
            while (i >= 0 && key < node.keys[i]) {
                node.keys[i + 1] = node.keys[i];
                i--;
                insertOperations++;
            }
            node.keys[i + 1] = key;
            node.n++;
            insertOperations++;
        } else {
            while (i >= 0 && key < node.keys[i]) {
                i--;
                insertOperations++;
            }
            if (node.children[i + 1].n == 2 * t - 1) {
                splitChild(node, i + 1);
                if (key > node.keys[i + 1]) {
                    i++;
                    insertOperations++;
                }
            }
            insertNonFull(node.children[i + 1], key);
        }
    }

    // Метод для разделения дочернего узла node
    private void splitChild(BTreeNode parentNode, int index) {
        BTreeNode childNode = parentNode.children[index];
        BTreeNode newNode = new BTreeNode(t, childNode.leaf);
        newNode.n = t - 1;

        for (int j = 0; j < t - 1; j++) {
            newNode.keys[j] = childNode.keys[j + t];
        }

        if (!childNode.leaf) {
            for (int j = 0; j < t; j++) {
                newNode.children[j] = childNode.children[j + t];
            }
        }

        childNode.n = t - 1;

        for (int j = parentNode.n; j >= index + 1; j--) {
            parentNode.children[j + 1] = parentNode.children[j];
        }

        parentNode.children[index + 1] = newNode;

        for (int j = parentNode.n - 1; j >= index; j--) {
            parentNode.keys[j + 1] = parentNode.keys[j];
        }

        parentNode.keys[index] = childNode.keys[t - 1];

        parentNode.n++;
        insertOperations++;
    }

    // Метод поиска ключа в дереве
    public boolean search(int key) {
        return (root == null) ? false : search(root, key);
    }

    // Рекурсивный метод поиска ключа в поддереве, начиная с узла node
    private boolean search(BTreeNode node, int key) {
        int i = 0;
        while (i < node.n && key > node.keys[i]) {
            i++;
            searchOperations++;
        }
        if (i < node.n && key == node.keys[i]) {
            return true;
        }
        return (node.leaf) ? false : search(node.children[i], key);
    }

    // Метод удаления ключа из дерева
    public void delete(int key) {
        if (root == null) {
            return;
        }
        delete(root, key);
    }

    // Рекурсивный метод удаления ключа из поддерева, начиная с узла node
    private void delete(BTreeNode node, int key) {
        int i = 0;
        while (i < node.n && key > node.keys[i]) {
            i++;
            deleteOperations++;
        }
        if (i < node.n && key == node.keys[i]) {
            deleteKeyFromNode(node, i);
        } else {
            if (node.leaf) {
                return;
            }
            boolean lastChild = (i == node.n);
            if (node.children[i].n < t) {
                fill(node, i);
            }
            if (lastChild && i > node.n) {
                delete(node.children[i - 1], key);
            } else {
                delete(node.children[i], key);
            }
        }
    }

    private void deleteKeyFromNode(BTreeNode node, int index) {
        if (node.leaf) {
            // Проверяем, не выходит ли индекс за пределы массива
            if (index >= 0 && index < node.n) {
                for (int i = index + 1; i < node.n; i++) {
                    node.keys[i - 1] = node.keys[i];
                }
                node.n--;
                deleteOperations++;
            } else {
                System.out.println("Ошибка: Недопустимый индекс в листовом узле: " + index);
            }
        } else {
            // Проверяем, не выходят ли индексы за пределы массивов и не равен ли дочерний узел null
            if (index >= 0 && index < node.n && node.children[index] != null) {
                int key = node.keys[index];
                if (node.children[index].n >= t) {
                    int pred = getPred(node, index);
                    node.keys[index] = pred;
                    delete(node.children[index], pred);
                } else if (index + 1 < node.children.length && node.children[index + 1] != null && node.children[index + 1].n >= t) {
                    int succ = getSucc(node, index);
                    node.keys[index] = succ;
                    delete(node.children[index + 1], succ);
                } else {
                    merge(node, index);
                    delete(node.children[index], key);
                }
            } else {
                System.out.println("Ошибка: Недопустимый индекс во внутреннем узле или отсутствует дочерний узел: " + index);
            }
        }
    }


    // Метод для заполнения узла, если он содержит менее t ключей
    private void fill(BTreeNode node, int index) {
        if (index != 0 && node.children[index - 1].n >= t) {
            borrowFromPrev(node, index);
        } else if (index != node.n && node.children[index + 1].n >= t) {
            borrowFromNext(node, index);
        } else {
            if (index != node.n) {
                merge(node, index);
            } else {
                merge(node, index - 1);
            }
        }
    }

    private int getPred(BTreeNode node, int index) {
        BTreeNode current = node.children[index];
        while (!current.leaf) {
            current = current.children[current.n];
        }
        if (current.n > 0) {
            return current.keys[current.n - 1];
        } else if (index > 0 && !isEmpty(node.keys[index - 1])) {
            return node.keys[index - 1]; // Возвращаем последний ключ из предыдущего узла
        } else {
            return -1; // Возвращаем -1 только если узел пустой и без ключей
        }
    }

    private int getSucc(BTreeNode node, int index) {
        BTreeNode current = node.children[index + 1];
        while (!current.leaf) {
            current = current.children[0];
        }
        if (current.n > 0) {
            return current.keys[0];
        } else if (index < node.n - 1 && !isEmpty(node.keys[index + 1])) {
            return node.keys[index + 1]; // Возвращаем первый ключ из следующего узла
        } else {
            return -1; // Возвращаем -1 только если узел пустой и без ключей
        }
    }
    // Метод для заимствования ключа из предыдущего узла
    private void borrowFromPrev(BTreeNode node, int index) {
        BTreeNode child = node.children[index];
        BTreeNode sibling = node.children[index - 1];
        for (int i = child.n - 1; i >= 0; i--) {
            child.keys[i + 1] = child.keys[i];
        }
        if (!child.leaf) {
            for (int i = child.n; i >= 0; i--) {
                child.children[i + 1] = child.children[i];
            }
        }
        child.keys[0] = node.keys[index - 1];
        if (!child.leaf) {
            child.children[0] = sibling.children[sibling.n];
        }
        node.keys[index - 1] = sibling.keys[sibling.n - 1];
        child.n++;
        sibling.n--;
        deleteOperations++;
    }

    // Метод для заимствования ключа из следующего узла
    private void borrowFromNext(BTreeNode node, int index) {
        BTreeNode child = node.children[index];
        BTreeNode sibling = node.children[index + 1];
        child.keys[child.n] = node.keys[index];
        if (!child.leaf) {
            child.children[child.n + 1] = sibling.children[0];
        }
        node.keys[index] = sibling.keys[0];
        for (int i = 1; i < sibling.n; i++) {
            sibling.keys[i - 1] = sibling.keys[i];
        }
        if (!sibling.leaf) {
            for (int i = 1; i <= sibling.n; i++) {
                sibling.children[i - 1] = sibling.children[i];
            }
        }
        child.n++;
        sibling.n--;
        deleteOperations++;
    }

    // Метод для объединения двух узлов
    private void merge(BTreeNode node, int index) {
        BTreeNode child = node.children[index];
        BTreeNode sibling = node.children[index + 1];

        child.keys[t - 1] = node.keys[index];

        for (int i = 0; i < sibling.n; i++) {
            child.keys[i + t] = sibling.keys[i];
        }

        if (!child.leaf) {
            for (int i = 0; i <= sibling.n; i++) {
                child.children[i + t] = sibling.children[i];
            }
        }

        for (int i = index + 1; i < node.n; i++) {
            node.keys[i - 1] = node.keys[i];
        }

        for (int i = index + 2; i <= node.n; i++) {
            node.children[i - 1] = node.children[i];
        }

        child.n += sibling.n + 1;
        node.n--;
        deleteOperations++;
    }

    // Метод для вывода дерева по уровням
    private boolean isValidIndex(int length, int index) {
        return index >= 0 && index < length;
    }
    private boolean isEmpty(Integer key) {
        return key == null;
    }

    // Геттеры для счетчиков операций
    public long getInsertOperations() {
        return insertOperations;
    }
    public void resetInsertOperations() {
        insertOperations = 0;
    }

    public long getSearchOperations() {
        return searchOperations;
    }

    public void resetSearchOperations(){
        searchOperations = 0;
    }

    public long getDeleteOperations() {
        return deleteOperations;
    }

    public void resetDeleteOperations(){
        deleteOperations = 0;
    }
}