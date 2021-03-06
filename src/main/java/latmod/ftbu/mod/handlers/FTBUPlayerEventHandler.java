package latmod.ftbu.mod.handlers;

import ftb.lib.*;
import ftb.lib.api.item.ICreativeSafeItem;
import ftb.lib.api.tile.ISecureTile;
import ftb.lib.notification.Notification;
import latmod.ftbu.api.EventLMPlayerServer;
import latmod.ftbu.mod.FTBU;
import latmod.ftbu.mod.config.*;
import latmod.ftbu.net.*;
import latmod.ftbu.world.*;
import latmod.ftbu.world.claims.*;
import latmod.lib.MathHelperLM;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.*;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class FTBUPlayerEventHandler
{
	@SubscribeEvent
	public void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent e)
	{ if(e.player instanceof EntityPlayerMP) playerLoggedOut((EntityPlayerMP) e.player); }
	
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
		
		p.setPlayer(null);
		//Backups.shouldRun = true;
		
		FTBUChunkEventHandler.instance.markDirty(null);
	}
	
	@SubscribeEvent
	public void onChunkChanged(EntityEvent.EnteringChunk e)
	{
		if(e.entity.worldObj.isRemote || !(e.entity instanceof EntityPlayerMP)) return;
		
		EntityPlayerMP ep = (EntityPlayerMP) e.entity;
		LMPlayerServer player = LMWorldServer.inst.getPlayer(ep);
		if(player == null || !player.isOnline()) return;
		
		if(player.lastPos == null) player.lastPos = new EntityPos(ep);
		else player.lastPos.set(ep);
		
		int currentChunkType = LMWorldServer.inst.claimedChunks.getType(ep.dimension, e.newChunkX, e.newChunkZ).ID;
		
		if(player.lastChunkType == -99 || player.lastChunkType != currentChunkType)
		{
			player.lastChunkType = currentChunkType;
			
			ChunkType type = ClaimedChunks.getChunkTypeFromI(currentChunkType);
			IChatComponent msg = null;
			
			if(type.isClaimed())
				msg = new ChatComponentText(String.valueOf(LMWorldServer.inst.getPlayer(currentChunkType)));
			else msg = new ChatComponentTranslation(FTBU.mod.assets + type.lang);
			
			msg.getChatStyle().setColor(EnumChatFormatting.WHITE);
			msg.getChatStyle().setBold(true);
			
			Notification n = new Notification("chunk_changed", msg, 3000);
			n.setColor(type.getAreaColor(player));
			
			FTBLib.notifyPlayer(ep, n);
		}
	}
	
	@SubscribeEvent
	public void onBlockClick(PlayerInteractEvent e)
	{
		if(e.entityPlayer instanceof FakePlayer || e.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) return;
		else if(!canInteract(e.entityPlayer, e.pos, e.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK))
			e.setCanceled(true);
		else if(!e.world.isRemote)
		{
			TileEntity te = e.world.getTileEntity(e.pos);
			
			if(te != null && !te.isInvalid() && te instanceof TileEntitySign)
			{
				TileEntitySign t = (TileEntitySign) te;
				
				if(FTBUConfigGeneral.sign_home.get() && t.signText[1].equals("[home]"))
				{
					try
					{
						FTBLib.runCommand(null, FTBUConfigCmd.name_home.get(), new String[] {t.signText[2].getUnformattedText()});
					}
					catch(Exception ex) {}
					e.setCanceled(true);
					return;
				}
				else if(FTBUConfigGeneral.sign_warp.get() && !t.signText[2].getUnformattedText().isEmpty() && t.signText[1].getUnformattedText().equals("[warp]"))
				{
					try
					{
						FTBLib.runCommand(e.entityPlayer, FTBUConfigCmd.name_warp.get(), new String[] {t.signText[2].getUnformattedText()});
					}
					catch(Exception ex) {}
					e.setCanceled(true);
					return;
				}
			}
		}
	}
	
	private boolean canInteract(EntityPlayer ep, BlockPos pos, boolean leftClick)
	{
		ItemStack heldItem = ep.getHeldItem();
		
		if(ep.capabilities.isCreativeMode && leftClick && heldItem != null && heldItem.getItem() instanceof ICreativeSafeItem)
		{
			if(!ep.worldObj.isRemote) ep.worldObj.markBlockRangeForRenderUpdate(pos, pos);
			else ep.worldObj.markBlockForUpdate(pos);
			return false;
		}
		
		if(!ep.worldObj.isRemote)
		{
			IBlockState state = ep.worldObj.getBlockState(pos);
			
			if(state.getBlock().hasTileEntity(state))
			{
				TileEntity te = ep.worldObj.getTileEntity(pos);
				if(te instanceof ISecureTile && !te.isInvalid() && !((ISecureTile) te).canPlayerInteract(ep, leftClick))
				{
					((ISecureTile) te).onPlayerNotOwner(ep, leftClick);
					return false;
				}
			}
		}
		
		return ClaimedChunks.canPlayerInteract(ep, pos, leftClick);
	}
	
	@SubscribeEvent
	public void onPlayerDeath(LivingDeathEvent e)
	{
		if(e.entity instanceof EntityPlayerMP)
		{
			LMPlayerServer p = LMWorldServer.inst.getPlayer(e.entity);
			
			if(p.lastDeath == null) p.lastDeath = new EntityPos(e.entity);
			else p.lastDeath.set(e.entity);
			
			p.refreshStats();
			new MessageLMPlayerDied(p).sendTo(null);
			
			FTBUChunkEventHandler.instance.markDirty(null);
		}
	}
	
	@SubscribeEvent
	public void onPlayerAttacked(LivingAttackEvent e)
	{
		if(e.entity.worldObj.isRemote) return;
		
		int dim = e.entity.dimension;
		if(dim != 0 || !(e.entity instanceof EntityPlayerMP) || e.entity instanceof FakePlayer) return;
		
		Entity entity = e.source.getSourceOfDamage();
		
		if(entity != null && (entity instanceof EntityPlayerMP || entity instanceof IMob))
		{
			if(entity instanceof FakePlayer) return;
			else if(entity instanceof EntityPlayerMP && LMWorldServer.inst.getPlayer(entity).allowCreativeInteractSecure())
				return;
			
			int cx = MathHelperLM.chunk(e.entity.posX);
			int cz = MathHelperLM.chunk(e.entity.posZ);
			
			if((FTBUConfigGeneral.safe_spawn.get() && ClaimedChunks.isInSpawn(dim, cx, cz))) e.setCanceled(true);
			/*else
			{
				ClaimedChunk c = Claims.get(dim, cx, cz);
				if(c != null && c.claims.settings.isSafe()) e.setCanceled(true);
			}*/
		}
	}
}