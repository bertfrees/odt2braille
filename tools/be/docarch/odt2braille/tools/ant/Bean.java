package be.docarch.odt2braille.tools.ant;

import java.util.List;
import java.util.ArrayList;

public class Bean {

    private List<Command> commands = new ArrayList<Command>();
    protected Class beanClass;

    public Bean() {}

    public Bean(Class beanClass) {
        this.beanClass = beanClass;
    }

    public SetCommand createSet() {
        SetCommand command = new SetCommand(beanClass);
        commands.add(command);
        return command;
    }

    public GetCommand createGet() {
        GetCommand command = new GetCommand(beanClass);
        commands.add(command);
        return command;
    }

    public void applyCommandsTo(Object object) {
        for (Command command : commands) {
            command.applyTo(object);
        }
    }
}
