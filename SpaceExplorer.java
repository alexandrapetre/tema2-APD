import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Set;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * Class for a space explorer.
 */
public class SpaceExplorer extends Thread {

	/**
	 * Creates a {@code SpaceExplorer} object.
	 *
	 * @param hashCount
	 *            number of times that a space explorer repeats the hash operation
	 *            when decoding
	 * @param discovered
	 *            set containing the IDs of the discovered solar systems
	 * @param channel
	 *            communication channel between the space explorers and the
	 *            headquarters
	 */

	private int hashCount;
	private Set<Integer> discoverd;
	public CommunicationChannel channel;

	private boolean stop = false;
	private int receivedFirst = 0;

	public SpaceExplorer(Integer hashCount, Set<Integer> discovered, CommunicationChannel channel) {
		this.hashCount = hashCount;
		this.discoverd = discovered;
		this.channel = channel;
	}

	@Override
	public void run() {

		Message messageReceived;
		int currentSS1, currentSS2;
		String data1, data2;
		String elements[];

		while(!stop){

			messageReceived = channel.getMessageHeadQuarterChannel();

			if(messageReceived!= null && messageReceived.getData() == HeadQuarter.EXIT){
				break;
			}

			if(messageReceived != null && messageReceived.getCurrentSolarSystem() == -2){

				elements = messageReceived.getData().split(" ");
				currentSS1 = Integer.parseInt(elements[0]);
				data1 = elements[1];
				currentSS2 = Integer.parseInt(elements[2]);
				data2 = elements[3];
				if(discoverd.contains(currentSS2) == false){
					discoverd.add(currentSS2);
					String encrypted = encryptMultipleTimes(data2, hashCount);
					Message newMessage = new Message(currentSS1, currentSS2, encrypted);
					channel.putMessageSpaceExplorerChannel(newMessage);
				}
			}
		}
	}


	/**
	 * Applies a hash function to a string for a given number of times (i.e.,
	 * decodes a frequency).
	 *
	 * @param input
	 *            string to he hashed multiple times
	 * @param count
	 *            number of times that the string is hashed
	 * @return hashed string (i.e., decoded frequency)
	 */
	private String encryptMultipleTimes(String input, Integer count) {
		String hashed = input;
		for (int i = 0; i < count; ++i) {
			hashed = encryptThisString(hashed);
		}

		return hashed;
	}

	/**
	 * Applies a hash function to a string (to be used multiple times when decoding
	 * a frequency).
	 *
	 * @param input
	 *            string to be hashed
	 * @return hashed string
	 */
	private static String encryptThisString(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));

			// convert to string
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String hex = Integer.toHexString(0xff & messageDigest[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
