import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

public class Z2Receiver {
    static final int datagramSize = 50;
    InetAddress localHost;
    int destinationPort;
    DatagramSocket socket;
    HashMap<Integer, Z2Packet> map;
    int counter;
    ReceiverThread receiver;

    public Z2Receiver(int myPort, int destPort)
            throws Exception {
        localHost = InetAddress.getByName("127.0.0.1");
        destinationPort = destPort;
        socket = new DatagramSocket(myPort);
        receiver = new ReceiverThread();
        map = new HashMap<>();
        counter = 0;
    }

    class ReceiverThread extends Thread {

        public void run() {
                    Z2Packet r = null;
            try {
                while (true) {
                    byte[] data = new byte[datagramSize];
                    DatagramPacket packet =
                            new DatagramPacket(data, datagramSize);
                    socket.receive(packet);
                    Z2Packet p = new Z2Packet(packet.getData());
                    if (r == null) r = p;
                    if (p.getIntAt(0) >= counter)
                        map.put(p.getIntAt(0), p);

                    while (map.containsKey(counter)){
                        r = map.remove(counter);
                        System.out.println("R:" + r.getIntAt(0) + ": " + (char) r.data[4]);
                        counter++;
                    }
                    // WYSLANIE POTWIERDZENIA
                        DatagramPacket outPacket = new DatagramPacket(r.data, r.data.length, localHost, destinationPort);
                        socket.send(outPacket);
                }
            } catch (Exception e) {
                System.out.println("Z2Receiver.ReceiverThread.run: " + e);
            }
        }

    }


    public static void main(String[] args)
            throws Exception {
        Z2Receiver receiver = new Z2Receiver(Integer.parseInt(args[0]),
                Integer.parseInt(args[1]));
        receiver.receiver.start();
    }


}
