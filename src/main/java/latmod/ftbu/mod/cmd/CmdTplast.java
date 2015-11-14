package latmod.ftbu.mod.cmd;

import ftb.lib.*;
import latmod.ftbu.cmd.*;
import latmod.ftbu.mod.FTBU;
import latmod.ftbu.world.LMPlayerServer;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.*;

public class CmdTplast extends CommandLM
{
	public CmdTplast()
	{ super("tpl", CommandLevel.OP); }
	
	public NameType getUsername(String[] args, int i)
	{ if(i == 0 || i == 1) return NameType.OFF; return NameType.NONE; }
	
	public IChatComponent onCommand(ICommandSender ics, String[] args) throws CommandException
	{
		checkArgs(args, 1);
		
		if(args.length == 3)
		{
			EntityPlayerMP ep = getCommandSenderAsPlayer(ics);
			CoordinateArg x = func_175770_a(ep.posX, args[0], false);
			CoordinateArg y = func_175770_a(ep.posY, args[1], false);
			CoordinateArg z = func_175770_a(ep.posZ, args[2], false);
			LMDimUtils.teleportPlayer(ep, x.func_179628_a(), y.func_179628_a(), z.func_179628_a(), ep.dimension);
			return null;
		}
		
		EntityPlayerMP who;
		LMPlayerServer to;
		
		if(args.length == 1)
		{
			who = getCommandSenderAsPlayer(ics);
			to = getLMPlayer(args[0]);
		}
		else
		{
			who = getPlayer(ics, args[0]);
			to = getLMPlayer(args[1]);
		}
		
		EntityPos p = to.getPos();
		if(p == null) return error(new ChatComponentText("No last position!"));
		LMDimUtils.teleportPlayer(who, p);
		return new ChatComponentTranslation(FTBU.mod.assets + "cmd.warp_tp", to.getName());
	}
}