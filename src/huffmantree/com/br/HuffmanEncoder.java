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
	private int totBytes;
	private int cutoffIndex;
	private int restBits;

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

	private void readHeader() throws IOException {
		final BufferedReader reader = new BufferedReader(new FileReader(headerFile));
		String line = null;
		String[] charFreq = null;
		String[] fileInfo = null;
		String[] charPath = null;
		line = reader.readLine();
		if (line == null) {
			reader.close();
			return;
		}

		fileInfo = line.split(separator);
		totBytes = Integer.parseInt(fileInfo[0]);
		cutoffIndex = Integer.parseInt(fileInfo[1]);

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
		readHeader();
		buildHuffmanTree();
		decodingFile();
	}

	private void decodingFile() throws IOException {
		StringBuilder bytes = new StringBuilder();
		InputStream in = new FileInputStream(encodingFile);
		byte[] bytesFile = new byte[totBytes];
		in.read(bytesFile);
		for (byte b : bytesFile) {
			appendBits(bytes, b);
		}
		in.close();

		BufferedWriter writer = new BufferedWriter(new FileWriter(decondingFile));
		String bits = bytes.toString();
		bits = bits.substring(0, cutoffIndex);
		char[] encoding = bits.toCharArray();

		Node node = huffTree;
		String deconding = "";
		for (char c : encoding) {
			node = node.getChild(c);
			if (node.isLeaf()) {
				deconding = deconding + node.getCharacter();
				node = huffTree;
			}
		}
		writer.write(deconding);
		writer.close();
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
		writeContent();
		writeHeader();
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
			if (encoding.length() <= 8) {
				restBits = 8 - encoding.length();
				totBytes = 1;
				cutoffIndex = 8;
			} else {
				restBits = encoding.length() % 8;
				totBytes = (encoding.length() + restBits) / 8;
				cutoffIndex = encoding.length();
			}

			for (int i = 0; i < restBits; i++) {
				encoding.append("0");
			}

			out.write(toByte(encoding.toString()));
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
		byte[] bytes = new byte[totBytes];

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

		return bytes;
	}

	private void writeHeader() throws IOException {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(headerFile));

			StringBuilder header = new StringBuilder();
			header.append(totBytes).append(";").append(cutoffIndex).append("\n");
			for (Node node : headerList) {
				header.append(node.charAndPath()).append(separator);
			}
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
	}

}
