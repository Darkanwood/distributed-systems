import java.rmi.server.ObjID;

public class uebung01_02b {

    private static int c = 0;
    private static Objekt LOCK = new Object();

    public static void increment() {
        
        synchronized(LOCK){
        int b = c;
        }

            b = b + 1;
            c = b;
        
        
    }

    public synchronized void decrement() {
        int d = c;
        d = d - 1;
        c = d;
    }

    public synchronized static int value() {
        return c;
    }

    static void main(String[] args) {

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                increment();
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                increment();
            }
        });

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(value());
    }

}
