package client;

import java.util.ArrayList;

public abstract class Command {
    private final ArrayList<Repl.ClientContext> validStatuses = new ArrayList<>();
    private final String helpText;

    public boolean currentlyValid(Repl.ClientContext currentContext) {
        return validStatuses.contains(currentContext);
    }
    public String getHelpText() { return helpText; }

    Command(String helpText) {
        this.helpText = helpText;
    }

    public abstract void run(String[] args) throws Exception;

    public void addValidStatus(Repl.ClientContext context) { validStatuses.add(context); }
}
