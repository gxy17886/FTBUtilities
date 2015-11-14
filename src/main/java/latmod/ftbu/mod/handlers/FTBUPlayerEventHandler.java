package latmod.ftbu.mod.handlers;

import java.util.List;

import ftb.lib.*;
import ftb.lib.item.LMInvUtils;
import latmod.ftbu.api.EventLMPlayerServer;
import latmod.ftbu.mod.FTBU;
import latmod.ftbu.mod.config.*;
import latmod.ftbu.net.*;
import latmod.ftbu.notification.Notification;
import latmod.ftbu.util.LatCoreMC;
import latmod.ftbu.world.*;
import latmod.lib.MathHelperLM;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class FTBUPlayerEventHandler
{
	@SubscribeEvent
	public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent e)
	{
		if(!(e.player instanceof EntityPlayerMP)) return;
		EntityPlayerMP ep = (EntityPlayerMP)e.player;
		
		LMPlayerServer p = LMWorldServer.inst.getPlayer(ep);
		
		boolean first = (p == null);
		boolean sendAll = false;
		
		if(first)
		{
			p = new LMPlayerServer(LMWorldServer.inst, LMPlayerServer.nextPlayerID(), ep.getGameProfile());
			LMWorldServer.inst.players.add(p);
			sendAll = true;
		}
		else if(!p.getName().equals(p.gameProfile.getName()))
		{
			p.setName(p.gameProfile.getName());
			sendAll = true;
		}
		
		p.setPlayer(ep);
		p.refreshStats();
		
		new MessageLMWorldJoined(p.playerID).sendTo(sendAll ? null : ep);
		new EventLMPlayerServer.LoggedIn(p, ep, first).post();
		new MessageLMPlayerLoggedIn(p, first, true).sendTo(ep);
		for(EntityPlayerMP ep1 : FTBLib.getAllOnlinePlayers(ep))
			new MessageLMPlayerLoggedIn(p, first, false).sendTo(ep1);
		
		if(first)
		{
			List<ItemStack> items = FTBUConfigLogin.getStartingItems(ep.getUniqueID());
			if(items != null && !items.isEmpty()) for(ItemStack is : items)
				LMInvUtils.giveItem(ep, is);
		}
		
		//new MessageLMPlayerInfo(p.playerID).sendTo(null);
		FTBUConfigLogin.printMotd(ep);
		Backups.shouldRun = true;
		
		//if(first) teleportToSpawn(ep);
		p.checkNewFriends();
		new MessageAreaUpdate(p.getPos(), 3, 3).sendTo(ep);
	}
	
	@SubscribeEvent
	public void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent e)
	{ if(e.player instanceof EntityPlayerMP) playerLoggedOut((EntityPlayerMP)e.player); }
	
	public static void playerLoggedOut(EntityPlayerMP ep)
	{
		LMPlayerServer p = LMWorldServer.inst.getPlayer(ep);
		if(p == null) return;
		p.refreshStats();
		
		for(int i = 0; i < 4; i++)
			p.lastArmor[i] = ep.inventory.armorInventory[i];
		p.lastArmor[4] = ep.inventory.getCurrentItem();
		
		new EventLMPlayerServer.LoggedOut(p, ep).post();
		new MessageLMPlayerLoggedOut(p).sendTo(null);
		
		if(FTBUConfigBackups.autoExportInvOnLogout.get())
			FTBLib.runCommand(FTBLib.getServer(), "admin player saveinv " + p.getName());
		
		p.setPlayer(null);
		//Backups.shouldRun = true;
	}
	
	@SubscribeEvent
	public void onChunkChanged(EntityEvent.EnteringChunk e)
	{
		if(e.entity.worldObj.isRemote || !(e.entity instanceof EntityPlayerMP)) return;
		
		EntityPlayerMP ep = (EntityPlayerMP)e.entity;
		LMPlayerServer player = LMWorldServer.inst.getPlayer(ep);
		if(player == null || !player.isOnline()) return;
		
		if(player.lastPos == null) player.lastPos = new EntityPos(ep);
		
		else if(!player.lastPos.equalsPos(ep))
		{
			if(LMWorldServer.inst.settings.isOutsideF(ep.dimension, ep.posX, ep.posZ))
			{
				ep.motionX = ep.motionY = ep.motionZ = 0D;
				IChatComponent warning = new ChatComponentTranslation(FTBU.mod.assets + ChunkType.WORLD_BORDER.lang + ".warning");
				warning.getChatStyle().setColor(EnumChatFormatting.RED);
				LatCoreMC.notifyPlayer(ep, new Notification("world_border", warning, 3000));
				
				if(LMWorldServer.inst.settings.isOutsideF(player.lastPos.dim, player.lastPos.x, player.lastPos.z))
				{
					FTBLib.printChat(ep, new ChatComponentTranslation(FTBU.mod.assets + "cmd.spawn_tp"));
					World w = LMDimUtils.getWorld(0);
					BlockPos pos = w.getSpawnPoint();
					pos = w.getTopSolidOrLiquidBlock(pos);
					LMDimUtils.teleportPlayer(ep, pos.getX() + 0.5D, pos.getY() + 1.25D, pos.getZ() + 0.5D, 0);
				}
				else LMDimUtils.teleportPlayer(ep, player.lastPos);
				ep.worldObj.playSoundAtEntity(ep, "random.fizz", 1F, 1F);
			}
			
			player.lastPos.set(ep);
		}
		
		int currentChunkType = ChunkType.getChunkTypeI(ep.dimension, e.newChunkX, e.newChunkZ);
		
		if(player.lastChunkType == -99 || player.lastChunkType != currentChunkType)
		{
			player.lastChunkType = currentChunkType;
			
			ChunkType type = ChunkType.getChunkTypeFromI(currentChunkType);
			IChatComponent msg = null;
			
			if(type.isClaimed())
				msg = new ChatComponentText(String.valueOf(LMWorldServer.inst.getPlayer(currentChunkType)));
			else
				msg = new ChatComponentTranslation(FTBU.mod.assets + type.lang);
			
			msg.getChatStyle().setColor(EnumChatFormatting.WHITE);
			msg.getChatStyle().setBold(true);
			
			Notification n = new Notification("chunk_changed", msg, 3000);
			n.setColor(type.getAreaColor(player));
			
			LatCoreMC.notifyPlayer(ep, n);
		}
	}
	
	@SubscribeEvent
	public void onBlockClick(PlayerInteractEvent e)
	{
		if(e.action == net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_AIR) return;
		else if(!canInteract(e.entityPlayer, e.pos, e.action == net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.LEFT_CLICK_BLOCK))
			e.setCanceled(true);
	}
	
	private boolean canInteract(EntityPlayer ep, BlockPos pos, boolean leftClick)
	{
		return Claims.canPlayerInteract(ep, pos, leftClick);
	}
	
	@SubscribeEvent
	public void onPlayerDeath(net.minecraftforge.event.entity.living.LivingDeathEvent e)
	{
		if(e.entity instanceof EntityPlayerMP)
		{
			LMPlayerServer p = LMWorldServer.inst.getPlayer(e.entity);
			
			if(p.lastDeath == null) p.lastDeath = new EntityPos(e.entity);
			else p.lastDeath.set(e.entity);
			
			p.refreshStats();
			new MessageLMPlayerDied(p).sendTo(null);
		}
	}
	
	@SubscribeEvent
	public void onPlayerAttacked(net.minecraftforge.event.entity.living.LivingAttackEvent e)
	{
		if(e.entity.worldObj.isRemote) return;
		
		int dim = e.entity.dimension;
		if(dim != 0 || !(e.entity instanceof EntityPlayerMP) || e.entity instanceof FakePlayer) return;
		
		Entity entity = e.source.getSourceOfDamage();
		
		if(entity != null && (entity instanceof EntityPlayerMP || entity instanceof IMob))
		{
			if(entity instanceof FakePlayer) return;
			else if(entity instanceof EntityPlayerMP && FTBUConfigGeneral.allowCreativeInteractSecure((EntityPlayerMP)entity)) return;
			
			int cx = MathHelperLM.chunk(e.entity.posX);
			int cz = MathHelperLM.chunk(e.entity.posZ);
			
			if(LMWorldServer.inst.settings.isOutside(dim, cx, cz) || (FTBUConfigGeneral.safeSpawn.get() && Claims.isInSpawn(dim, cx, cz))) e.setCanceled(true);
			/*else
			{
				ClaimedChunk c = Claims.get(dim, cx, cz);
				if(c != null && c.claims.settings.isSafe()) e.setCanceled(true);
			}*/
		}
	}
}