package mod.stf.syconn.util.applications;

import mod.stf.syconn.api.util.ColorCodes;
import mod.stf.syconn.api.util.Mths;
import mod.stf.syconn.api.util.applications.BasicCommand;
import mod.stf.syconn.api.util.applications.CommandStatus;
import mod.stf.syconn.network.Network;
import mod.stf.syconn.network.messages.MessageHyperdrive;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

import java.util.Locale;

public class MoveCMD extends BasicCommand<NavigationApplication> {

    private int p1;
    private Direction p2;

    public MoveCMD(NavigationApplication application) {
        super("/", "move", application);
    }

    @Override
    public CommandStatus hasParameters(String cmd) {
        String[] parameters = cmd.trim().split("\\s+");

        if (Mths.isNumeric(parameters[0])){
            if (parameters.length > 1) {
                p1 = Integer.parseInt(parameters[0]);

                for (Direction direction : Direction.values()) {
                    if (direction.getName().matches(parameters[1].toLowerCase())) {
                        p2 = direction;
                        if (meetsFlightRequirements()) return new CommandStatus("3. 2. 1. GO!", CommandStatus.Status.SUCCESS);
                        return new CommandStatus("Navigator Not In Side of Ship", CommandStatus.Status.ERROR);
                    }
                }
                return new CommandStatus("Parameter not Valid Direction", CommandStatus.Status.ERROR);
            }
            return new CommandStatus("Missing Direction Parameter", CommandStatus.Status.ERROR);
        }

        return new CommandStatus("Parameter 1 not a int", CommandStatus.Status.ERROR);
    }

    @Override
    public void execute() {
        if (meetsFlightRequirements()){
            Network.getPlayChannel().sendToServer(new MessageHyperdrive(p2, p1, application.getShip()));
        }
    }

    @Override
    public CommandStatus info() {
        return new CommandStatus("/move " + ColorCodes.WHITE + "<distance> " + ColorCodes.WHITE + "<direction> EX - /move " + ColorCodes.AQUA + "10 " + ColorCodes.RED + "north", CommandStatus.Status.HELP);
    }

    public boolean meetsFlightRequirements(){
        return application.getShip() != null && application.getShip().containsBlockPos(application.getPos());
    }
}