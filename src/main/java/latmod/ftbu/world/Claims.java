package latmod.ftbu.world;

import java.util.UUID;

import ftb.lib.*;
import ftb.lib.item.LMInvUtils;
import latmod.ftbu.mod.config.*;
import latmod.lib.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.*;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class Claims
{
	public final LMPlayerServer owner;
	private final FastList<ClaimedChunk> chunks;
	public final FastList<ClaimedChunk> loaded;
	
	public Claims(LMPlayerServer p)
	{
		owner = p;
		chunks = new FastList<ClaimedChunk>();
		loaded = new FastList<ClaimedChunk>();
	}
	
	public void readFromNBT(NBTTagCompound serverData)
	{
		chunks.clear();
		loaded.clear();
		
		NBTTagCompound tag = serverData.getCompoundTag("Claims");
		
		NBTTagList list = tag.getTagList("Chunks", LMNBTUtils.INT_ARRAY);
		
		if(list != null) for(int i = 0; i < list.tagCount(); i++)
		{
			int[] ai = list.getIntArray(i);
			chunks.add(new ClaimedChunk(this, ai[0], ai[1], ai[2]));
		}
		
		list = tag.getTagList("Loaded", LMNBTUtils.INT_ARRAY);
		
		if(list != null) for(int i = 0; i < list.tagCount(); i++)
		{
			int[] ai = list.getIntArray(i);
			loaded.add(new ClaimedChunk(this, ai[0], ai[1], ai[2]));
		}
	}
	
	public void writeToNBT(NBTTagCompound serverData)
	{
		NBTTagCompound tag = new NBTTagCompound();
		
		NBTTagList list = new NBTTagList();
		
		for(int j = 0; j < chunks.size(); j++)
		{
			ClaimedChunk c = chunks.get(j);
			list.appendTag(new NBTTagIntArray(new int[] { c.dim, c.posX, c.posZ }));
		}
		
		tag.setTag("Chunks", list);
		
		list = new NBTTagList();
		
		for(int j = 0; j < loaded.size(); j++)
		{
			ClaimedChunk c = loaded.get(j);
			list.appendTag(new NBTTagIntArray(new int[] { c.dim, c.posX, c.posZ }));
		}
		
		tag.setTag("Loaded", list);
		
		serverData.setTag("Claims", tag);
	}
	
	public ClaimedChunk getLocal(int dim, int cx, int cz)
	{
		for(int i = 0; i < chunks.size(); i++)
		{
			ClaimedChunk c = chunks.get(i);
			if(c.equalsChunk(dim, cx, cz)) return c;
		}
		
		return null;
	}
	
	public void claim(int dim, int cx, int cz)
	{
		if(FTBUConfigClaims.dimensionBlacklist.get().contains(dim)) return;
		
		int max = owner.getMaxClaimPower();
		if(max == 0) return;
		if(getClaimedChunks() >= max) return;
		
		ChunkType t = ChunkType.get(dim, cx, cz);
		if(!t.isClaimed() && t.canClaimChunk(owner))
			chunks.add(new ClaimedChunk(this, dim, cx, cz));
		
		owner.sendUpdate();
	}
	
	public void unclaim(int dim, int cx, int cz, boolean admin)
	{
		if(!chunks.isEmpty() && chunks.remove(new ClaimedChunk(this, dim, cx, cz)))
			owner.sendUpdate();
	}
	
	public void unclaimAll(int dim)
	{
		if(chunks.isEmpty()) return;
		int size0 = getClaimedChunks();
		/*
		FastList<ClaimedChunk> l = new FastList<ClaimedChunk>();
		
		for(int i = 0; i < chunks.size(); i++)
		{
			ClaimedChunk c = chunks.get(i);
			if(c.dim != dim) l.add(c);
		}
		
		*/
		
		for(int i = chunks.size() - 1; i >= 0; i--)
		{
			ClaimedChunk c = chunks.get(i);
			if(c.dim == dim) chunks.remove(i);
		}
		
		if(size0 != getClaimedChunks())
			owner.sendUpdate();
	}
	
	public void unclaimAll()
	{
		int i = chunks.size();
		chunks.clear();
		if(i > 0) owner.sendUpdate();
	}
	
	public int getClaimedChunks()
	{ return chunks.size(); }
	
	// Static //
	
	/** Server side */
	public static ClaimedChunk get(int dim, int cx, int cz)
	{
		for(int i = 0; i < LMWorldServer.inst.players.size(); i++)
		{
			ClaimedChunk c = LMWorldServer.inst.players.get(i).toPlayerMP().claims.getLocal(dim, cx, cz);
			if(c != null) return c;
		}
		
		return null;
	}
	
	/** Server side */
	public static boolean isInSpawn(int dim, int cx, int cz)
	{
		if(dim != 0) return false;
		//if(!LatCoreMC.isDedicatedServer()) return false;
		int radius = FTBLib.getServer().getSpawnProtectionSize();
		if(radius <= 0) return false;
		BlockPos c = LMDimUtils.getSpawnPoint(0);
		int minX = MathHelperLM.chunk(c.getX() + 0.5D - radius);
		int minZ = MathHelperLM.chunk(c.getZ() + 0.5D - radius);
		int maxX = MathHelperLM.chunk(c.getX() + 0.5D + radius);
		int maxZ = MathHelperLM.chunk(c.getZ() + 0.5D + radius);
		return cx >= minX && cx <= maxX && cz >= minZ && cz <= maxZ;
	}
	
	/** Server side */
	public static boolean isInSpawnF(int dim, double x, double z)
	{ return dim == 0 && isInSpawn(dim, MathHelperLM.chunk(x), MathHelperLM.chunk(z)); }
	
	/** Server side */
	public static boolean allowExplosion(int dim, int cx, int cz)
	{
		if(dim == 0 && FTBUConfigGeneral.safeSpawn.get() && isInSpawn(dim, cx, cz))
			return false;
		else if(LMWorldServer.inst.settings.isOutside(dim, cx, cz))
			return false;
		else
		{
			int fe = FTBUConfigClaims.forcedExplosions.get();
			
			ClaimedChunk c = get(dim, cx, cz);
			if(c != null)
			{
				if(fe == -1) return c.claims.owner.settings.explosions;
				else return fe == 1;
			}
		}
		
		return true;
	}
	
	public static boolean canPlayerInteract(EntityPlayer ep, BlockPos pos, boolean leftClick)
	{
		World w = ep.worldObj;
		boolean server = !w.isRemote;
		if(server && LMWorldServer.inst.settings.isOutsideF(w.provider.getDimensionId(), pos.getX(), pos.getZ())) return false;
		
		if(!server || FTBUConfigGeneral.allowCreativeInteractSecure(ep)) return true;
		
		return canInteract(ep.getGameProfile().getId(), w, pos, leftClick);
	}
	
	public static boolean canInteract(UUID playerID, World w, BlockPos pos, boolean leftClick)
	{
		if(leftClick && FTBUConfigClaims.breakWhitelist.get().contains(LMInvUtils.getRegName(w.getBlockState(pos).getBlock()))) return true;
		ChunkType type = ChunkType.getD(w.provider.getDimensionId(), pos.getX(), pos.getZ());
		return type.canInteract(LMWorldServer.inst.getPlayer(playerID), leftClick);
	}
}