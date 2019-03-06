package huffmantree.com.br;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HuffmanEncoder {
	private final Map<String, Node> nodeMap = new HashMap<>();
	private final LinkedList<Node> nodeList = new LinkedList<>();
	private final List<Node> headerList = new ArrayList<>();

	private Node huffTree;
	private final File inputFile;
	private final File encodingFile;
	private final File decondingFile;
	private final File headerFile;

	private final String separator = ";";

	private int cutoffIndex;

	public HuffmanEncoder(File file) throws IOException {
		this.inputFile = file;
		String fileName = inputFile.getName().split("\\.")[0];

		encodingFile = new File(inputFile.getParentFile().getAbsolutePath() + File.separator + fileName + ".enc");
		decondingFile = new File(inputFile.getParentFile().getAbsolutePath() + File.separator + fileName + ".dec");
		headerFile = new File(inputFile.getParentFile().getAbsolutePath() + File.separator + fileName + ".hdr");

	}

	public void encode() throws IOException {
		readFile();
		buildHuffmanTree();
		encodingFile();
	}

	private void readingHeaderFile() throws IOException {
		final BufferedReader reader = new BufferedReader(new FileReader(headerFile));
		String line = null;
		String[] charFreq = null;
		String[] charPath = null;
		line = reader.readLine();
		if (line == null) {
			reader.close();
			return;
		}
		charFreq = line.split(separator);

		nodeList.clear();
		for (String path : charFreq) {
			charPath = path.split(":");
			nodeList.add(new Node(charPath[0].charAt(0), charPath[1], Long.parseLong(charPath[2])));
		}

		reader.close();
	}

	public void decode() throws IOException {
		readingHeaderFile();
		buildHuffmanTree();
		decodingFile();
	}

	private void decodingFile() throws IOException {
		StringBuilder bits = new StringBuilder();
		InputStream in = new FileInputStream(encodingFile);
		byte[] bytes = new byte[1];
		in.read(bytes);
		for (byte b : bytes) {
			appendBits(bits, b);
		}
		in.close();

		BufferedWriter writer = new BufferedWriter(new FileWriter(decondingFile));
		String encodingString = bits.toString();
		char[] encoding = encoding(encodingString.substring(cutoffIndex - 1, encodingString.length()));

		Node node = huffTree;
		String deconding = "";
		for (char c : encoding) {
			node = node.getChild(c);
			if (node.isLeaf()) {
				deconding = node.getCharacter() + deconding;
				node = huffTree;
			}
		}
		writer.write(deconding);
		writer.close();
	}

	private char[] encoding(String fileBits) {
		char[] encoding = fileBits.toString().toCharArray();
		char[] reverted = new char[encoding.length];
		for (int i = 0; i < reverted.length; i++) {
			reverted[i] = encoding[reverted.length - 1 - i];
		}
		return reverted;

	}

	private void buildHuffmanTree(Node minor, Node major) {
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
			buildHuffmanTree(minor, nodeList.removeFirst());
		} else {
			buildHuffmanTree(minor, null);
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

	private void encodingFile() throws IOException {
		writeHeader();
		writeContent();
	}

	private void writeContent() throws IOException {
		BufferedReader reader = null;

		OutputStream out = null;

		try {
			reader = new BufferedReader(new FileReader(inputFile));
			out = new FileOutputStream(encodingFile);

			String line = null;
			char[] chars = null;
			Node node = null;
			String character = null;
			StringBuilder encoding = new StringBuilder();

			while ((line = reader.readLine()) != null) {
				chars = line.toCharArray();
				for (char c : chars) {
					character = String.valueOf(c);
					if ((node = nodeMap.get(character)) == null) {
						throw new IllegalStateException("Falha na codificacao do arquivo " + inputFile.getAbsolutePath()
								+ ". Nao foi codificado o caracter \"" + character + "\"");
					}
					encoding.append(node.getPath());
				}
			}
			String encodingString = encoding.toString();
			cutoffIndex = encodingString.length() % 8;
			out.write(toByte(encodingString));
			System.out.println(encodingString);

		} finally {
			if (reader != null) {
				reader.close();
			}
			if (out != null) {
				out.close();
			}
		}

	}

	public void appendBits(StringBuilder bits, int bytes) {
		String b = "";
		for (int i = 0; i < 8; i++) {
			b = String.valueOf(bytes & 1) + b;
			bytes >>= 1;
		}
		bits.append(b);
	}

	private byte[] toByte(String encoding) {
		char[] chars = encoding.toCharArray();
		byte[] bytes = null;
		if (chars.length % 8 != 0) {
			bytes = new byte[(chars.length / 8) + 1];
		} else {
			bytes = new byte[chars.length / 8];
		}
		int idx = 0;
		int init = 0;
		for (int i = 0, j = 1; i < chars.length; j++, i++) {
			init <<= 1;
			init |= chars[i] == '1' ? 1 : 0;
			if (j != 1 && j % 8 == 0) {
				bytes[idx] = (byte) init;
				idx++;
				init = 0;
			}
		}
		if (idx == bytes.length - 1) {
			bytes[idx] = (byte) init;
		} else {
			throw new IllegalStateException("Falha na geracao da sequencia de bytes do conteudo.");
		}

		return bytes;
	}

	private void writeHeader() throws IOException {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(headerFile));

			StringBuilder header = new StringBuilder();
			for (Node node : headerList) {
				header.append(node.charAndPath()).append(separator);
			}
			header.append("\n");
			writer.write(header.toString());
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	private void buildHuffmanTree() {
		if (nodeList.isEmpty()) {
			return;
		}

		Node minor = nodeList.removeFirst();
		Node major = null;

		if (nodeList.size() >= 1) {
			major = nodeList.removeFirst();
		}

		buildHuffmanTree(minor, major);

	}

	public static void main(String... c) throws IOException {
		HuffmanEncoder huffman = new HuffmanEncoder(
				new File("C:\\Users\\vinic\\Documents\\ambiente_trabalho\\temp\\huffman_teste.txt"));

		huffman.encode();
		huffman.decode();
		// huffman.readBytes();
	}

	public void readBytes() throws IOException {
		byte[] bytes = Files.readAllBytes(encodingFile.toPath());
		for (byte b : bytes) {
			System.out.println(new String(new byte[] { b }, Charset.defaultCharset()));
		}
	}
}
