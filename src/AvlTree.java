import java.util.ArrayList;

//this is a clasic basic avltree, i used a more basic version of the one i used in my previous homework, 
// i just added max values (and with it min values ofc) to keep track of the max values in the tree (finding it in O(1))
class Node<T extends Comparable<? super T>> {
    T key;
    int height;
    Node<T> left, right;

    Node(T key) {
        this.key = key;
        this.height = 1;
    }
}

class AvlTree<T extends Comparable<? super T>> {

    Node<T> root;

    // O(1) min/max 
    T maxValue;

    int size;

    AvlTree() {
        root = null;
        maxValue = null;
        size = 0;
    }

    int height(Node<T> node) {
        return (node == null) ? 0 : node.height;
    }

    int max(int a, int b) {
        return (a > b) ? a : b;
    }

    Node<T> rightRotate(Node<T> y) {
        Node<T> x = y.left;
        Node<T> T2 = x.right;

        x.right = y;
        y.left = T2;

        y.height = max(height(y.left), height(y.right)) + 1;
        x.height = max(height(x.left), height(x.right)) + 1;

        return x;
    }

    Node<T> leftRotate(Node<T> x) {
        Node<T> y = x.right;
        Node<T> T2 = y.left;

        y.left = x;
        x.right = T2;

        x.height = max(height(x.left), height(x.right)) + 1;
        y.height = max(height(y.left), height(y.right)) + 1;

        return y;
    }

    int getBalance(Node<T> node) {
        return (node == null) ? 0 : height(node.left) - height(node.right);
    }

    Node<T> insert(Node<T> node, T key) {
        if (node == null) {
            return new Node<>(key);
        }

        int cmp = key.compareTo(node.key);

        if (cmp < 0) {
            node.left = insert(node.left, key);
        } else if (cmp > 0) {
            node.right = insert(node.right, key);
        } else {
            return node;
        }

        node.height = 1 + max(height(node.left), height(node.right));
        int balance = getBalance(node);

        if (balance > 1 && key.compareTo(node.left.key) < 0) {
            return rightRotate(node);
        }

        if (balance < -1 && key.compareTo(node.right.key) > 0) {
            return leftRotate(node);
        }

        if (balance > 1 && key.compareTo(node.left.key) > 0) {
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }

        if (balance < -1 && key.compareTo(node.right.key) < 0) {
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }

        return node;
    }

    void insert(T key) {
        if (!contains(key)) {
            root = insert(root, key);
            size++;
            if (maxValue == null || key.compareTo(maxValue) > 0) {
                maxValue = key;
            }
        }
    }

    Node<T> findMinNode(Node<T> node) {
        if (node == null)
            return null;
        while (node.left != null)
            node = node.left;
        return node;
    }

    Node<T> findMaxNode(Node<T> node) {
        if (node == null)
            return null;
        while (node.right != null)
            node = node.right;
        return node;
    }

    Node<T> delete(Node<T> node, T key) {
        if (node == null)
            return null;

        int cmp = key.compareTo(node.key);

        if (cmp < 0) {
            node.left = delete(node.left, key);
        } else if (cmp > 0) {
            node.right = delete(node.right, key);
        } else {

            if (node.left == null || node.right == null) {
                Node<T> temp = (node.left != null) ? node.left : node.right;

                if (temp == null) {
                    node = null;
                } else {
                    node = temp;
                }
            } else {
                Node<T> successor = findMinNode(node.right);
                node.key = successor.key;
                node.right = delete(node.right, successor.key);
            }
        }

        if (node == null)
            return null;

        node.height = 1 + max(height(node.left), height(node.right));
        int balance = getBalance(node);

        if (balance > 1 && getBalance(node.left) >= 0) {
            return rightRotate(node);
        }

        if (balance > 1 && getBalance(node.left) < 0) {
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }

        if (balance < -1 && getBalance(node.right) <= 0) {
            return leftRotate(node);
        }

        if (balance < -1 && getBalance(node.right) > 0) {
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }

        return node;
    }

    void delete(T key) {
        if (contains(key)) {
            root = delete(root, key);
            size--;

            if (root == null) {
                maxValue = null;
            } else {
                Node<T> maxNode = findMaxNode(root);
                maxValue = (maxNode != null) ? maxNode.key : null;
            }
        }
    }

    private Node<T> search(Node<T> node, T key) {
        if (node == null)
            return null;

        int cmp = key.compareTo(node.key);

        if (cmp == 0)
            return node;
        if (cmp < 0)
            return search(node.left, key);
        return search(node.right, key);
    }

    boolean contains(T key) {
        return search(root, key) != null;
    }

    Node<T> search(T key) {
        return search(root, key);
    }

    int getSize() {
        return size;
    }

    T getMax() {
        return maxValue;
    }
    public ArrayList<T> findTopK(int k, MyHashMap<String, Boolean> blacklist) {
        ArrayList<T> results = new ArrayList<>();
        findTopKRec(root, k, blacklist, results);
        return results;
    }
    private void findTopKRec(Node<T> node, int k, MyHashMap<String, Boolean> blacklist, ArrayList<T> results) {
        if (node == null || results.size() >= k) {
            return;
        }

        // first check the rights (bigger)
        findTopKRec(node.right, k, blacklist, results);

        //check itself if there is still need
        if (results.size() < k) {
            boolean isAllowed = true;
            if (blacklist != null && node.key instanceof Freelancer) {
                Freelancer f = (Freelancer) node.key;
                if (blacklist.containsKey(f.freelancerID)) {
                    isAllowed = false;  
                }
            }

            if (isAllowed) {
                results.add(node.key);
            }

            if (results.size() >= k)
                return;
        }
        //if there is still need, check left (smallers)
        findTopKRec(node.left, k, blacklist, results);
    }
}