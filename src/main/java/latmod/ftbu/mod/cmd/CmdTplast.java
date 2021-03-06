package latmod.ftbu.mod.cmd;

import ftb.lib.*;
import ftb.lib.cmd.*;
import latmod.ftbu.mod.FTBU;
import latmod.ftbu.mod.config.FTBUConfigCmd;
import latmod.ftbu.world.LMPlayerServer;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.*;

public class CmdTplast extends CommandLM
{
	public CmdTplast()
	{ super(FTBUConfigCmd.name_tplast.get(), CommandLevel.OP); }
	
	public String getCommandUsage(ICommandSender ics)
	{ return '/' + commandName + " [who] <to>"; }
	
	public Boolean getUsername(String[] args, int i)
	{ return (i == 0 || i == 1) ? Boolean.FALSE : null; }
	
	public IChatComponent onCommand(ICommandSender ics, String[] args) throws CommandException
	{
		checkArgs(args, 1);
		
		if(args.length == 3)
		{
			EntityPlayerMP ep = getCommandSenderAsPlayer(ics);
			double x = parseDouble(ep.posX, args[0], -30000000, 30000000, true);
			double y = parseDouble(ep.posY, args[1], -30000000, 30000000, true);
			double z = parseDouble(ep.posZ, args[2], -30000000, 30000000, true);
			LMDimUtils.teleportPlayer(ep, x, y, z, ep.dimension);
			return null;
		}
		
		EntityPlayerMP who;
		LMPlayerServer to;
		
		if(args.length == 1)
		{
			who = getCommandSenderAsPlayer(ics);
			to = LMPlayerServer.get(args[0]);
		}
		else
		{
			who = getPlayer(ics, args[0]);
			to = LMPlayerServer.get(args[1]);
		}
		
		EntityPos p = to.getPos();
		if(p == null) return error(new ChatComponentText("No last position!"));
		LMDimUtils.teleportPlayer(who, p);
		return new ChatComponentTranslation(FTBU.mod.assets + "cmd.warp_tp", to.getProfile().getName());
	}
}