package Uebung01;

public class uebung01_01 {
    public static void main(String[] args) {
        
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                System.out.println("Praised be the Omnissiah!");
            }
        });

        Thread t2 = new Thread(() -> {
            for(int i = 0; i < 10; i++){
                System.out.println("Praised be the Machine Spirit!");
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
}
}