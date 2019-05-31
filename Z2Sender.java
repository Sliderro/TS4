import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;

class Z2Sender {
    static final int datagramSize = 50;
    static final int sleepTime = 1000;
    static final int maxPacket = 50;
    InetAddress localHost;
    int destinationPort;
    AtomicInteger deliveredID;
    DatagramSocket socket;
    SenderThread sender;
    ReceiverThread receiver;

    public Z2Sender(int myPort, int destPort)
            throws Exception {
        localHost = InetAddress.getByName("127.0.0.1");
        destinationPort = destPort;
        socket = new DatagramSocket(myPort);
        sender = new SenderThread();
        receiver = new ReceiverThread();
        deliveredID = new AtomicInteger(-1);
    }

    class SenderThread extends Thread {
        public void run() {
            int x;
            int sentID;
            try {
                for (sentID = 0; (x = System.in.read()) >= 0; sentID++) {
                    Z2Packet p = new Z2Packet(4 + 1);
                    p.setIntAt(sentID, 0);
                    p.data[4] = (byte) x;
                    DatagramPacket packet =
                            new DatagramPacket(p.data, p.data.length, localHost, destinationPort);
                    while (sentID > deliveredID.get()) {
                        System.err.println("S"+p.getIntAt(0));
                        socket.send(packet);
                        sleep(sleepTime);
                    }
                }
            } catch (Exception e) {
                System.out.println("Z2Sender.SenderThread.run: " + e);
            }
        }
    }


    class ReceiverThread extends Thread {

        public void run() {
            try {
                while (true) {
                    byte[] data = new byte[datagramSize];
                    DatagramPacket packet =
                            new DatagramPacket(data, datagramSize);
                    socket.receive(packet);
                    Z2Packet p = new Z2Packet(packet.getData());
                    System.err.println("R"+p.getIntAt(0));

                    if (deliveredID.get() < p.getIntAt(0)) {
                        deliveredID.set(p.getIntAt(0));
//                        System.out.println("S:" + p.getIntAt(0) + ": " + (char) p.data[4]);
                    }
                }
            } catch (Exception e) {
                System.out.println("Z2Sender.ReceiverThread.run: " + e);
            }
        }
    }


    public static void main(String[] args)
            throws Exception {
        Z2Sender sender = new Z2Sender(Integer.parseInt(args[0]),
                Integer.parseInt(args[1]));
        sender.sender.start();
        sender.receiver.start();
    }


}
