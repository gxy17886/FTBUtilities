package latmod.ftbu.mod.cmd.admin;

import latmod.ftbu.cmd.*;
import latmod.ftbu.world.LMPlayerServer;
import net.minecraft.command.*;
import net.minecraft.util.*;

public class CmdAdminUnclaimAll extends CommandLM
{
	public CmdAdminUnclaimAll(String s)
	{ super(s, CommandLevel.OP); }
	
	public IChatComponent onCommand(ICommandSender ics, String[] args) throws CommandException
	{
		checkArgs(args, 1);
		LMPlayerServer p = getLMPlayer(args[0]);
		p.claims.unclaimAll();
		return new ChatComponentText("Unclaimed all " + p.getName() + "'s chunks");
	}
}