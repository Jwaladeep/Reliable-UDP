package socketProgAssignment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.BitSet;

public class UDPServer {
	private static int clientPort;
	private static InetAddress clientHost;
	private static byte dataPacket[] = new byte[4096];
	private static DatagramSocket ds;
	private static DatagramPacket dp;

	public static void main(String st[]) throws Exception {
		ds = new DatagramSocket(9999);
		dp = new DatagramPacket(dataPacket, dataPacket.length);
		System.out.println("Wating for Request");
		if (!checkForRequest(ds, dp)) {
			notSupportedResponse("File not Found");

		}
		/*
		 * InputStream is
		 * =UDPServer.class.getClass().getClassLoader().getResourceAsStream("photo.jpg")
		 * ; byte[] streamedFile = IOUtils.toByteArray(is);
		 */
		// Convert file to byteStream and get the length
		byte[] streamedFile = convertToByteStream("photo.jpg");
		int totalLength = streamedFile.length;

		// Check if file of this length is possible to be transferred
		int noOfPackets = (int) Math.ceil((double) totalLength / (4095));
		if (noOfPackets > 256) {

			notSupportedResponse("File too long");
		}

		// Converting whole file Stream to smaller chunks
		byte[][] packets = divideFileToChunks(streamedFile, noOfPackets);

		// Sending to Client
		sendPacketsToClient(packets);

		System.out.println("All packets sent!");
		// Sending request for acknowledgement to Client -- array
		boolean flag = true;
		do {
			System.out.println("Requesting for Ack ");
			// request for acknowledgement from Client
			boolean b[] = reqForAck();
			System.out.println("Acknowledgment received by the Server: " + Arrays.toString(b));

			// Server Side Acknowledgement Handler
			if (b.length != noOfPackets) {
				for (int i = b.length; i < noOfPackets; i++) {
					b[i] = false;
				}
			}

			// for Selective Repeat

//			flag = true;
//			for (int i = 0; i < noOfPackets - 1; i++) {
//				if (b[i] == false) {
//					flag = false;
//					dataPacket = packets[i];
//					System.out.println("Resending Packet number: " + i);
//					sendPacket(dataPacket);
//
//				}
//			}
			// End of Selective Repeat

			// for GoBackN
			
			 int i;
			 flag = true;
			 for (i = 0; i < noOfPackets - 1; i++) {
			 if (b[i] == false) {
			 flag = false;
			 break;
			
			 }
			 }
			 if (flag == false) {
			 for (int j = i; j <= noOfPackets - 1; j++) {
			 dataPacket = packets[j];
			 System.out.println("Resending Packet number: " + j);
			 sendPacket(dataPacket);
			 }
			 }
			// End of GoBackN

		} while (!flag);

		// once done , send the acknowledgement..
		sendAck();

		// Comparing

		ds.close();

	}

	private static boolean[] reqForAck() {

		byte[] dataPacket = new byte[4096];
		dataPacket[0] = (byte) 126;
		// Send This string to Client
		DatagramPacket ackDP = new DatagramPacket(dataPacket, dataPacket.length, clientHost, 9998);
		try {
			ds.send(ackDP);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// Receiving ack Array from Client
		byte ackArr[] = new byte[4096];
		byte[] temp;

		try {
			boolean[] acknowledgement = new boolean[4096];

			DatagramPacket dp2 = new DatagramPacket(ackArr, ackArr.length);
			ds.receive(dp2);
			temp = dp2.getData();
			BitSet bitset = BitSet.valueOf(temp);

			for (int i = 0; i < bitset.length(); i++) {
				acknowledgement[i] = (boolean) bitset.get(i);
			}

			return acknowledgement;
		} catch (IOException e) {
			e.printStackTrace();
			return reqForAck();
		}

	}

	private static void sendAck() {

		byte[] buffer = new byte[4096];
		buffer[0] = (byte) 127;
		System.out.println("Successfully Sent Resource!");
		sendPacket(buffer);
		ds.close();

	}

	private static void sendPacket(byte[] buffer) {
		dp.setAddress(clientHost);
		dp.setPort(9999 - 1);
		dp.setData(buffer);
		try {
			ds.send(dp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private static void sendPacketsToClient(byte[][] packets) {

		for (int i = 0; i < (packets.length); i++) {
			dataPacket = packets[i];
			sendPacket(dataPacket);

		}
	}

	private static void notSupportedResponse(String msg) {
		String notSupportedMsg = msg;
		byte[] notSupportedByte = notSupportedMsg.getBytes();
		DatagramPacket dp2 = new DatagramPacket(notSupportedByte, notSupportedByte.length, clientHost, clientPort);
		try {
			ds.send(dp2);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ds.close();
		return;
	}

	private static boolean checkForRequest(DatagramSocket ds, DatagramPacket dp) throws Exception {

		while (true) {
			ds.receive(dp);
			clientPort = dp.getPort();
			clientHost = dp.getAddress();
			String str = new String(dp.getData());
			if ((str.trim()).equals("photo.jpg"))
				return true;
			else
				return false;

		}

	}

	private static int packetNumInt(byte packetNumber) {
		return (int) (packetNumber + 128);
	}

	private static byte[][] divideFileToChunks(byte[] streamedFile, int noOfPackets) {
		byte packetNumber = (byte) (-128);

		byte[][] packetArr = new byte[noOfPackets][];
		int i;
		for (i = 0; i < streamedFile.length && packetNumInt(packetNumber) <= noOfPackets - 2; i = i + 4095) {
			packetArr[packetNumInt(packetNumber)] = new byte[4096];
			packetArr[packetNumInt(packetNumber)][0] = packetNumber;
			System.arraycopy(streamedFile, i, packetArr[packetNumInt(packetNumber)], 1, 4095);
			packetNumber = (byte) (packetNumber + 1);
		}
		packetArr[packetNumInt(packetNumber)] = new byte[4096];
		packetArr[packetNumInt(packetNumber)][0] = packetNumber;
		System.arraycopy(streamedFile, i, packetArr[packetNumInt(packetNumber)], 1, (streamedFile.length) - i);

		return packetArr;

	}

	private static byte[] convertToByteStream(String string) {
		File file = new File("photo.jpg");
		byte[] bytesArray = new byte[(int) file.length()];

		try {
			FileInputStream fis = new FileInputStream(file);
			fis.read(bytesArray); // read file into bytes[]
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bytesArray;

	}

}
