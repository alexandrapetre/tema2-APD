import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class that implements the channel used by headquarters and space explorers to communicate.
 */
public class CommunicationChannel {

	/**
	 * Creates a {@code CommunicationChannel} object.
	 */

	private  BlockingQueue<Message> messagesSpaceExplorers;
	private  BlockingQueue<Message> messagesHQ;
	private final ReentrantLock lock_hq = new ReentrantLock();
	private long last = -1;
	private int lock = 0;


	public CommunicationChannel() {
		messagesSpaceExplorers = new ArrayBlockingQueue<Message>(10000);
		messagesHQ = new ArrayBlockingQueue<Message>(10000);
	}

	/**
	 * Puts a message on the space explorer channel (i.e., where space explorers write to and
	 * headquarters read from).
	 *
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageSpaceExplorerChannel(Message message){
		try {
			messagesSpaceExplorers.put(message);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Gets a message from the space explorer channel (i.e., where space explorers write to and
	 * headquarters read from).
	 *
	 * @return message from the space explorer channel
	 */
	public Message getMessageSpaceExplorerChannel(){
		Message newMessage = null;
		try {
			newMessage = messagesSpaceExplorers.take();
		} catch (InterruptedException e) {
		}
		return newMessage;
	}

	/**
	 * Puts a message on the headquarters channel (i.e., where headquarters write to and
	 * space explorers read from).
	 *
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageHeadQuarterChannel(Message message) {

		lock_hq.lock();
		lock++;
		if(message.getData() == HeadQuarter.EXIT || message.getData() == HeadQuarter.END){
			if(message.getData() == HeadQuarter.EXIT) {
				try {
					messagesHQ.put(message);
				} catch (InterruptedException e) {

				}
			}
			lock_hq.unlock();
		}else{
			try {
				messagesHQ.put(message);
			} catch (InterruptedException e) {

			}finally {
				if(lock  == 2){
					lock_hq.unlock();
					lock_hq.unlock();
				}
			}

		}

	}

	/**
	 * Gets a message from the headquarters channel (i.e., where headquarters write to and
	 * space explorer read from).
	 *
	 * @return message from the header quarter channel
	 */
	public Message getMessageHeadQuarterChannel() {

		Message messageFromHq1 = null;
		Message messageFromHq2 = null;
		Message newMessage = null;

		while(true){
			synchronized (this) {
				try {
					messageFromHq1 = messagesHQ.take();
					messageFromHq2 = messagesHQ.take();
				} catch (InterruptedException e) {
				}
			}

			if (messageFromHq1 != null && messageFromHq2 != null) {
				String newData = "";
				String currentSystem = Integer.toString(messageFromHq1.getCurrentSolarSystem());
				newData = newData + currentSystem;
				newData = newData + " ";
				newData = newData + messageFromHq1.getData();
				newData = newData + " ";
				currentSystem = Integer.toString(messageFromHq2.getCurrentSolarSystem());
				newData = newData + currentSystem;
				newData = newData + " ";
				newData = newData + messageFromHq2.getData();
				newMessage = new Message(-2, newData);
				break;
			}
		}
		return newMessage;
	}
}
