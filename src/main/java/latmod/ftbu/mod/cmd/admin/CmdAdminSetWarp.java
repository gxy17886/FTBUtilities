package latmod.ftbu.mod.cmd.admin;

import latmod.ftbu.cmd.*;
import latmod.ftbu.mod.FTBU;
import latmod.ftbu.world.LMWorldServer;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.*;

public class CmdAdminSetWarp extends CommandLM
{
	public CmdAdminSetWarp(String s)
	{ super(s, CommandLevel.OP); }

	public IChatComponent onCommand(ICommandSender ics, String[] args) throws CommandException
	{
		checkArgs(args, 1);
		EntityPlayerMP ep = getCommandSenderAsPlayer(ics);
		BlockPos c = ep.getPosition();
		LMWorldServer.inst.warps.set(args[0], c.getX(), c.getY(), c.getZ(), ep.worldObj.provider.getDimensionId());
		return new ChatComponentTranslation(FTBU.mod.assets + "cmd.warp_set", args[0]);
	}
}