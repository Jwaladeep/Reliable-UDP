package socketProgAssignment;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.reflect.FieldUtils;

public class UDPClient {

	public static void main(String s[]) throws Exception {

		DatagramSocket ds = new DatagramSocket();
		String st = "photo.jpg";
		byte b[] = new byte[4096];
		b = st.getBytes();
		InetAddress lh = InetAddress.getLocalHost();
		DatagramPacket dp = new DatagramPacket(b, b.length, lh, 9999);

		// Sending packet with req
		ds.send(dp);

		System.out.println("Resource Request for 'photo.jpg' sent to Server ");

		byte dataPacket[] = new byte[4096];
		ds = new DatagramSocket(9999 - 1);
		DatagramPacket dp2 = new DatagramPacket(dataPacket, dataPacket.length);

		ArrayList<Byte[]> packets = new ArrayList<>();
		ArrayList<Boolean> ackArr = new ArrayList<>();

		boolean flag = false;
		int counter = 0;
		while (!flag) {

			// Client receiving packets coming to this port
			ds.receive(dp2);
			dataPacket = dp2.getData();

			String str = new String(dataPacket);
			byte packetNumber = dataPacket[0];
			int packetNumberInt = packetNumInt(packetNumber);
			if (packetNumberInt == 255) {
				flag = true;
				accumulatePacket(packets, ds);
				return;
			}
			if (packetNumberInt == 254) {
				b = toByteArray(ackArr);

				sendAckToServer(b, ds);

			}

			// To test Reliability , skipping packet number 1 for the first time
			if (counter == 0 && packetNumberInt == 1) {
				counter++;
				continue;
			}

			if (packetNumberInt < 253) {
				if (ackArr.size() < (packetNumberInt + 1)) {
					int diff = (packetNumberInt + 1) - ackArr.size();
					for (int i = ackArr.size(); diff > 0; diff--, i++) {
						ackArr.add(i, false);
						packets.add(i, null);

					}
				}
				ackArr.set(packetNumberInt, true);
				packets.set(packetNumberInt, ArrayUtils.toObject(dataPacket));

			}
		}

	}

	private static void sendAckToServer(byte[] b, DatagramSocket ds) {
		// TODO Auto-generated method stub
		InetAddress lh;
		try {
			lh = InetAddress.getLocalHost();
			DatagramPacket dp = new DatagramPacket(b, b.length, lh, 9999);
			ds.send(dp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void accumulatePacket(ArrayList<Byte[]> packets, DatagramSocket ds) {
		// TODO Auto-generated method stub

		System.out.println("LET'S ACCUMULATE NOW!! Enter name of file/(.jpg/):");
		Scanner sc = new Scanner(System.in);
		String fName = sc.next();

		File file = new File(fName);
		OutputStream os;
		try {
			os = new FileOutputStream(file);

			// Starts writing the bytes in it

			for (int i = 0; i < packets.size(); i++) {
				os.write(ArrayUtils.toPrimitive(packets.get(i)), 1, 4095);

			}
			os.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Successfully" + packets.size()*4095 + " bytes inserted");

		// Close the file
		sc.close();
		ds.close();

	}

	private static int packetNumInt(byte packetNumber) {
		return (int) (packetNumber + 128);
	}

	static byte[] toByteArray(ArrayList<Boolean> ackArr) {
		// from ArrayList<Boolean> to boolean[]
		int sizeArr = ackArr.size();
		boolean[] bools = new boolean[sizeArr];
		for (int i = 0; i < sizeArr; i++) {
			bools[i] = ackArr.get(i);
		}
		// from boolean[] to byte[]
		BitSet bits = new BitSet(bools.length);
		for (int i = 0; i < bools.length; i++) {
			if (bools[i]) {
				bits.set(i);
			}
		}

		byte[] bytes = bits.toByteArray();
		return bytes;

	}
}
