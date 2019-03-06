package huffmantree.com.br;

public class Node implements Comparable<Node> {
	private Node leftChild;
	private Node rightChild;
	private long frequency;
	private final String name;
	private final char[] chars;
	private final Character character;
	private String path = "";

	public String getPath() {
		return path;
	}

	public char[] pathAsChars() {
		return path.toCharArray();
	}

	public Node(Node leftChild, Node rightChild) {
		this.chars = unifyChars(leftChild, rightChild);
		this.leftChild = leftChild;

		this.rightChild = rightChild;
		this.name = new String(chars);
		this.character = null;
		this.path = leftChild.path.substring(0, leftChild.path.length() <= 0 ? 0 : leftChild.path.length() - 1);
	}

	public Node(char character, long frequency) {
		this.character = character;
		this.chars = new char[] { character };
		this.frequency = frequency;
		name = new String(chars);
	}

	public Node(char character, String path, long frequency) {
		this(character, path);
		this.frequency = frequency;
	}

	public Node(char character, String path) {
		this.character = character;
		this.chars = new char[] { character };
		name = new String(chars);
		this.path = path;
	}

	public Node(char[] chars, long frequency) {
		this.character = null;
		this.chars = chars;
		this.frequency = frequency;
		name = new String(chars);
	}

	public Node(char[] chars, String path) {
		this.character = null;
		this.chars = chars;
		name = new String(chars);
		this.path = path;
	}

	public Node getChild(char path) {
		if (path == '0') {
			return leftChild;
		}
		return rightChild;
	}

	public Node unify(Node otherNode) {
		Node newNode = new Node(unifyChars(this, otherNode), frequency + otherNode.frequency);
		if (this.hasHigherFreq(otherNode)) {
			newNode.leftChild = otherNode;
			newNode.rightChild = this;
		} else {
			newNode.leftChild = this;
			newNode.rightChild = otherNode;
		}

		codify(newNode.leftChild, "0");
		codify(newNode.rightChild, "1");
		return newNode;
	}

	private char[] unifyChars(Node minor, Node major) {
		char[] newChars = new char[minor.chars.length + major.chars.length];
		for (int i = 0; i < minor.chars.length; i++) {
			newChars[i] = minor.chars[i];
		}

		for (int i = 0; i < major.chars.length; i++) {
			newChars[i + minor.chars.length] = major.chars[i];
		}

		return newChars;
	}

	public boolean isLeaf() {
		return leftChild == null;
	}

	public void printLeafs() {
		printLeafs(this);
	}

	private void printLeafs(Node node) {
		if (node.isLeaf()) {
			System.out.println(node);
			return;
		}
		printLeafs(node.leftChild);
		printLeafs(node.rightChild);
	}

	private void codify(Node node, String path) {
		node.path = path;
		if (node.isLeaf()) {
			// node.path = path + node.path;
			return;
		}

		codify(node.leftChild, node.path + "0");
		codify(node.rightChild, node.path + "1");
	}

	public Node getLeftChild() {
		return leftChild;
	}

	public Node getRightChild() {
		return rightChild;
	}

	public long getFrequency() {
		return frequency;
	}

	public String getName() {
		return name;
	}

	public char[] getChars() {
		return chars;
	}

	public Character getCharacter() {
		return character;
	}

	public void incFrequency() {
		frequency++;
	}

	@Override
	public int compareTo(Node o) {
		if (frequency == o.frequency) {
			if (name.charAt(0) > o.name.charAt(0)) {
				return 1;
			} else if (name.charAt(0) < o.name.charAt(0)) {
				return -1;
			}
			throw new IllegalStateException(
					"O primeiro caracter do nome dos node nao pode ser igual. Node " + name + " => Node " + o.name);
		} else if (frequency < o.frequency) {
			return -1;
		}
		return 1;
	}

	@Override
	public String toString() {
		return name + ":" + frequency + " => " + path;
	}

	public String charAndPath() {
		return character + ":" + path + ":" + frequency;
	}

	public boolean hasHigherFreq(Node node) {
		return frequency > node.frequency;
	}
}
