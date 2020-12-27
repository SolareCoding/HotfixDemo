package pub.solare.hotfixdemo;

public class Bug {

    private boolean fix = false;

    public String getString() {
        if (fix)
            return "Bug fixed";
        else
            return "This is a bug";
    }
}
