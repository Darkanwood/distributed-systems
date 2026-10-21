package Uebung01;

public class uebung01_02a {
    private static int c = 0;

    public static void increment () {
    int b = c ;
    b = b +1;
    c = b ;
    }

    public void decrement () {
    int d = c ;
    d =d -1;
    c = d ;
    }

    public static int value () {
    return c ;
    }




    static void main(String[] args){

        Thread t1 = new Thread(() -> {
            for(int i = 0; i < 100000; i++){
                increment();
            }
        });

        Thread t2 = new Thread(() ->{
            for(int i = 0; i < 100000; i++){
                increment();
            }
        });

        t1.start();
        t2.start();

        try{
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(value());


}

    
}
