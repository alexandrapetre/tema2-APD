public class MyOuter2 {
    private String x = "Outer2";
    void doStuff() {
        final String z = "local variable";
        class MyInner {
            public void seeOuter() {
                System.out.println("Outer x is " + x);
                System.out.println("Local variable z is " + z);
            } // close inner class method
        }
        MyInner inner = new MyInner();
        inner.seeOuter();// close inner class definition
    } // close outer class method doStuff()
} // close outer class