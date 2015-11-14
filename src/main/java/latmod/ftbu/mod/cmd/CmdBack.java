package latmod.ftbu.mod.cmd;

import ftb.lib.LMDimUtils;
import latmod.ftbu.cmd.*;
import latmod.ftbu.mod.FTBU;
import latmod.ftbu.world.*;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.*;

public class CmdBack extends CommandLM
{
	public CmdBack()
	{ super("back", CommandLevel.ALL); }
	
	public IChatComponent onCommand(ICommandSender ics, String[] args) throws CommandException
	{
		EntityPlayerMP ep = getCommandSenderAsPlayer(ics);
		LMPlayerServer p = LMWorldServer.inst.getPlayer(ep);
		if(p.lastDeath == null) return error(new ChatComponentTranslation(FTBU.mod.assets + "cmd.no_dp"));
		LMDimUtils.teleportPlayer(ep, p.lastDeath);
		p.lastDeath = null;
		
		return null;
	}
}