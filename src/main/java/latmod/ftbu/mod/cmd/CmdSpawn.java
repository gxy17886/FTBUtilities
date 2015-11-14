package latmod.ftbu.mod.cmd;

import ftb.lib.*;
import latmod.ftbu.cmd.*;
import latmod.ftbu.mod.FTBU;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class CmdSpawn extends CommandLM
{
	public CmdSpawn()
	{ super("spawn", CommandLevel.ALL); }
	
	public IChatComponent onCommand(ICommandSender ics, String[] args) throws CommandException
	{
		EntityPlayerMP ep = getCommandSenderAsPlayer(ics);
		BlockPos spawnpoint = LMDimUtils.getSpawnPoint(0);
		
		World w = LMDimUtils.getWorld(0);
		
		while(w.getBlockState(spawnpoint).getBlock().isOpaqueCube())
			spawnpoint = spawnpoint.up();
		
		LMDimUtils.teleportPlayer(ep, new EntityPos(spawnpoint, 0));
		return new ChatComponentTranslation(FTBU.mod.assets + "cmd.spawn_tp");
	}
}