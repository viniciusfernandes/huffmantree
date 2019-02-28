package huffmantree.com.br;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Compactfier {
	private final Map<String, Node> nodeMap = new HashMap<>();
	private final LinkedList<Node> nodeList = new LinkedList<>();
	private final List<Node> headerList = new ArrayList<>();

	private Node huffTree;
	private final File inputFile;
	private final File outputFile;
	private final String separator = ";";

	public Compactfier(File file) throws IOException {
		this.inputFile = file;
		String fileName = inputFile.getName().split("\\.")[0];
		outputFile = new File(inputFile.getParentFile().getAbsolutePath() + File.separator + fileName + ".comp");
	}

	public void compact() throws IOException {
		readFile();
		startHuffman();
		codifyFile();
	}

	public void decompact() throws IOException {
		final BufferedReader reader = new BufferedReader(new FileReader(outputFile));
		String line = null;
		String[] charFreq = null;
		String[] freq = null;
		line = reader.readLine();
		if (line == null) {
			reader.close();
			return;
		}
		charFreq = line.split(separator);

		nodeList.clear();
		for (String path : charFreq) {
			freq = path.split(":");
			nodeList.add(new Node(freq[0].charAt(0), freq[1]));
		}

		reader.close();
		startReverseHuffmanTree();
	}

	private void startReverseHuffmanTree() {
		if (nodeList.isEmpty()) {
			return;
		}
		Node minor = nodeList.removeFirst();
		Node major = null;

		if (nodeList.size() >= 1) {
			major = nodeList.removeFirst();
		}

		reverseHuffmanTree(minor, major);
	}

	private void reverseHuffmanTree(Node minor, Node major) {
		if (minor != null && major == null) {
			huffTree = minor;
			return;
		}

		minor = minor.unifyByPath(major);
		if (nodeList.size() >= 1) {
			reverseHuffmanTree(minor, nodeList.removeFirst());
		} else {
			reverseHuffmanTree(minor, null);
		}
	}

	private void readFile() throws IOException {
		final BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		String line = null;
		char[] chars = null;
		Node node = null;
		String character = null;
		while ((line = reader.readLine()) != null) {
			chars = line.toCharArray();
			for (char c : chars) {
				character = String.valueOf(c);
				if ((node = nodeMap.get(character)) == null) {
					node = new Node(c, 1);
					nodeMap.put(character, node);
					nodeList.add(node);
				} else {
					node.incFrequency();
				}
			}
		}
		Collections.sort(nodeList);
		headerList.addAll(nodeList);

		reader.close();
	}

	private void codifyFile() throws IOException {
		final BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		final BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

		writeHeader(writer);
		writeContent(reader, writer);

		reader.close();
		writer.close();
	}

	private void writeContent(BufferedReader reader, BufferedWriter writer) throws IOException {
		String line = null;
		char[] chars = null;
		Node node = null;
		String character = null;
		StringBuilder codified = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			chars = line.toCharArray();
			for (char c : chars) {
				character = String.valueOf(c);
				if ((node = nodeMap.get(character)) == null) {
					writer.close();
					reader.close();
					throw new IllegalStateException("Falha na codificacao do arquivo " + inputFile.getAbsolutePath()
							+ ". Nao foi codificado o caracter \"" + character + "\"");
				}
				codified.append(node.getPath());
			}
		}

		writer.write(codified.toString());
	}

	private void writeHeader(BufferedWriter writer) throws IOException {
		StringBuilder header = new StringBuilder();
		for (Node node : headerList) {
			header.append(node.charAndPath()).append(separator);
		}
		header.append("\n");
		writer.write(header.toString());
	}

	private void startHuffman() {
		if (nodeList.isEmpty()) {
			return;
		}

		Node minor = nodeList.removeFirst();
		Node major = null;

		if (nodeList.size() >= 1) {
			major = nodeList.removeFirst();
		}

		huffmanTree(minor, major);
		huffTree.printLeafs();

		System.out.println("+++++++++++++++++++");
		for (Node n : headerList) {
			System.out.println(n);
		}
	}

	private void huffmanTree(Node minor, Node major) {
		if (minor != null && major == null) {
			huffTree = minor;
			return;
		}

		if (minor.hasHigherFreq(major)) {
			nodeList.add(minor);
			Collections.sort(nodeList);
			minor = major;
		} else {
			minor = minor.unify(major);
		}

		if (nodeList.size() >= 1) {
			huffmanTree(minor, nodeList.removeFirst());
		} else {
			huffmanTree(minor, null);
		}
	}

	public static void main(String... c) throws IOException {
		Compactfier compactfier = new Compactfier(
				new File("C:\\Users\\Aluga.com\\Documents\\desenvolvimento\\temp\\usuario.txt"));
		// compactfier.compact();
		// compactfier.decompact();
		compactfier.readBytes();
	}

	public void readBytes() throws IOException {
		byte[] bytes = Files.readAllBytes(inputFile.toPath());
		for (byte b : bytes) {
			System.out.println(new String(new byte[] { b }, Charset.defaultCharset()));
		}
	}
}
