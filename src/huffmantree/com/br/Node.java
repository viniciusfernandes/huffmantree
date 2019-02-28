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

	public Node(char character, long frequency) {
		this.character = character;
		this.chars = new char[] { character };
		this.frequency = frequency;
		name = new String(chars);
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

	public Node unify(Node otherNode) {
		char[] newChars = new char[chars.length + otherNode.chars.length];
		for (int i = 0; i < chars.length; i++) {
			newChars[i] = chars[i];
		}

		final int idx = chars.length;
		for (int i = 0; i < otherNode.chars.length; i++) {
			newChars[i + idx] = otherNode.chars[i];
		}

		Node newNode = new Node(newChars, frequency + otherNode.frequency);
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

	public Node unifyByPath(Node otherNode) {
		char[] newChars = new char[chars.length + otherNode.chars.length];
		for (int i = 0; i < chars.length; i++) {
			newChars[i] = chars[i];
		}

		final int idx = chars.length;
		for (int i = 0; i < otherNode.chars.length; i++) {
			newChars[i + idx] = otherNode.chars[i];
		}
		Node newNode = new Node(newChars, otherNode.path.substring(1));
		Node refNode = null;
		if (this.isLeaf()) {
			refNode = this;
		} else {
			refNode = otherNode;
		}

		if (refNode.path.charAt(0) == '0') {
			newNode.leftChild = refNode;
			newNode.rightChild = this;
		} else {
			newNode.leftChild = this;
			newNode.rightChild = refNode;
		}
		return newNode;
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
		if (node.isLeaf()) {
			node.path = path + node.path;
			return;
		}

		codify(node.leftChild, path);
		codify(node.rightChild, path);
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
			if (character != null && o.character != null) {
				return character.compareTo(o.character);
			}
			return 0;
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
		return character + ":" + path;
	}

	public boolean hasHigherFreq(Node node) {
		return frequency > node.frequency;
	}
}
