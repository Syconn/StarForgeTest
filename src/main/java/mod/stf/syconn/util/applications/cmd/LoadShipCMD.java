package mod.stf.syconn.util.applications.cmd;

import mod.stf.syconn.api.util.ColorCodes;
import mod.stf.syconn.api.util.applications.BasicCommand;
import mod.stf.syconn.api.util.applications.CommandStatus;
import mod.stf.syconn.api.util.data.Schematic;
import mod.stf.syconn.network.Network;
import mod.stf.syconn.network.messages.MessageSetShip;
import mod.stf.syconn.util.applications.NavigationApplication;
import mod.stf.syconn.util.applications.subcmd.BasicSCM;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class LoadShipCMD extends BasicCommand<NavigationApplication> {

    private Player player;
    private BlockPos pos1;
    private BlockPos pos2;
    private Direction dir;

    public LoadShipCMD(NavigationApplication application) {
        super("/", "load", application);
    }

    @Override
    protected List<BasicSCM> getSubCMDS() {
        return null;
    }

    @Override
    public CommandStatus hasParameters(String cmd) {
        String[] parameters = cmd.trim().split("\\s+");

        if (parameters.length > 4) {
            pos1 = getBlockPos(parameters, 0);
            if (pos1 != BlockPos.ZERO) {
                pos2 = getBlockPos(parameters, 3);
                if (pos2 != BlockPos.ZERO) {
                    if (getDir(parameters[6]) != null) {
                        dir = getDir(parameters[6]);
                        return new CommandStatus("Successful BlockPos's", CommandStatus.Status.SUCCESS);
                    }
                    return new CommandStatus("Not Valid Direction", CommandStatus.Status.ERROR);
                }
                return new CommandStatus("Not Proper Pos 2", CommandStatus.Status.ERROR);
            }
        }

        if (parameters.length > 1){
            player = getPlayer(parameters[0]);
            if (player != null){
                if (getDir(parameters[1]) != null) {
                    dir = getDir(parameters[1]);
                    return new CommandStatus("Grabbed Player Schematic", CommandStatus.Status.SUCCESS);
                }
                return new CommandStatus("Not Valid Direction", CommandStatus.Status.ERROR);
            } else {
                return new CommandStatus("No Player Found", CommandStatus.Status.ERROR);
            }
        }

        if (parameters.length > 0){
            if (getDir(parameters[0]) != null) {
                dir = getDir(parameters[0]);
                return new CommandStatus("Grabbed Player Schematic", CommandStatus.Status.SUCCESS);
            }
        }
        return new CommandStatus("need more parameters", CommandStatus.Status.ERROR);
    }

    @Override
    public void execute() {
        if (player != null){
            Network.getPlayChannel().sendToServer(new MessageSetShip(Schematic.readSchematic(player.getMainHandItem().getOrCreateTag().getCompound("schematic")).cleanSchematic(), application.getPos(), dir));
        } else if (pos1 != null && pos2 != null){
            Network.getPlayChannel().sendToServer(new MessageSetShip(Schematic.genSchematic(pos1, pos2).cleanSchematic(), application.getPos(), dir));
        } else {
            Network.getPlayChannel().sendToServer(new MessageSetShip(Schematic.readSchematic(Minecraft.getInstance().player.getMainHandItem().getOrCreateTag().getCompound("schematic")).cleanSchematic(), application.getPos(), dir));
        }
    }

    @Override
    public CommandStatus info() {
        return new CommandStatus("/load " + ColorCodes.WHITE + "<pos1> " + ColorCodes.WHITE + "<pos2> " + ColorCodes.WHITE  + "<direction> " + "or /load " + ColorCodes.WHITE + "<playerName> " + ColorCodes.WHITE + "<direction> " + " EX - /load " + ColorCodes.AQUA + "10 " + ColorCodes.AQUA + "20 " + ColorCodes.AQUA + "30 " + ColorCodes.AQUA + "30 " + ColorCodes.AQUA + "20 "+ ColorCodes.AQUA + "5 or /load " + ColorCodes.GREEN + " Syconn", CommandStatus.Status.HELP);
    }
}
