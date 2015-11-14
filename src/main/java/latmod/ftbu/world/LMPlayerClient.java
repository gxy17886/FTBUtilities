package latmod.ftbu.world;

import com.mojang.authlib.GameProfile;

import ftb.lib.client.FTBLibClient;
import latmod.ftbu.api.EventLMPlayerClient;
import latmod.ftbu.badges.Badge;
import latmod.lib.FastList;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraftforge.fml.relauncher.*;

public class LMPlayerClient extends LMPlayer // LMPlayerServer
{
	public final FastList<IChatComponent> clientInfo;
	private ResourceLocation skinLocation;
	public boolean isOnline;
	public int claimedChunks;
	public int maxClaimPower;
	public Badge cachedBadge;
	
	public LMPlayerClient(LMWorldClient w, int i, GameProfile gp)
	{
		super(w, i, gp);
		clientInfo = new FastList<IChatComponent>();
		skinLocation = null;
		isOnline = false;
		cachedBadge = null;
	}
	
	public ResourceLocation getSkin()
	{
		if(skinLocation == null)
			skinLocation = FTBLibClient.getSkinTexture(getName());
		return skinLocation;
	}
	
	public boolean isOnline()
	{ return isOnline; }
	
	public LMPlayerServer toPlayerMP()
	{ return null; }
	
	@SideOnly(Side.CLIENT)
	public LMPlayerClient toPlayerSP()
	{ return this; }
	
	public EntityPlayerSP getPlayer()
	{ return isOnline() ? FTBLibClient.getPlayerSP(getUUID()) : null; }
	
	public void receiveInfo(FastList<IChatComponent> info)
	{
		clientInfo.clear();
		clientInfo.addAll(info);
		new EventLMPlayerClient.CustomInfo(this, clientInfo).post();
	}
	
	public void readFromNet(NBTTagCompound tag, boolean self)
	{
		isOnline = tag.getBoolean("ON");
		
		friends.clear();
		friends.addAll(tag.getIntArray("F"));
		
		commonPublicData = tag.getCompoundTag("CD");
		
		if(self)
		{
			commonPrivateData = tag.getCompoundTag("CPD");
			claimedChunks = tag.getInteger("CC");
			maxClaimPower = tag.getInteger("MCC");
		}
		
		settings.readFromNet(tag.getCompoundTag("CFG"), self);
	}
	
	public void onReloaded()
	{
		cachedBadge = null;
	}
}