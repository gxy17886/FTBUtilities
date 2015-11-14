package latmod.ftbu.net;
import ftb.lib.LMNBTUtils;
import ftb.lib.api.LMNetworkWrapper;
import ftb.lib.item.LMInvUtils;
import latmod.ftbu.util.client.LatCoreMCClient;
import latmod.ftbu.world.*;
import latmod.lib.FastList;
import net.minecraft.nbt.*;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.network.simpleimpl.*;
import net.minecraftforge.fml.relauncher.*;

public class MessageLMPlayerInfo extends MessageFTBU
{
	public MessageLMPlayerInfo() { super(DATA_LONG); }
	
	public MessageLMPlayerInfo(int playerID)
	{
		this();
		LMPlayerServer p = LMWorldServer.inst.getPlayer(playerID);
		io.writeInt(p == null ? 0 : p.playerID);
		if(p == null) return;
		
		NBTTagCompound tag = new NBTTagCompound();
		
		FastList<IChatComponent> info = new FastList<IChatComponent>();
		p.getInfo(info);
		
		NBTTagList listInfo = new NBTTagList();
		
		for(int i = 0; i < info.size(); i++)
			listInfo.appendTag(new NBTTagString(IChatComponent.Serializer.componentToJson(info.get(i))));
		
		tag.setTag("I", listInfo);
		
		LMInvUtils.writeItemsToNBT(p.lastArmor, tag, "LI");
		
		writeTag(tag);
	}
	
	public LMNetworkWrapper getWrapper()
	{ return FTBUNetHandler.NET_INFO; }
	
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(MessageContext ctx)
	{
		if(LMWorldClient.inst == null) return null;
		LMPlayerClient p = LMWorldClient.inst.getPlayer(io.readInt());
		if(p == null) return null;
		
		NBTTagCompound tag = readTag();
		NBTTagList listInfo = tag.getTagList("I", LMNBTUtils.STRING);
		
		FastList<IChatComponent> info = new FastList<IChatComponent>();
		for(int i = 0; i < listInfo.tagCount(); i++)
			info.add(IChatComponent.Serializer.jsonToComponent(listInfo.getStringTagAt(i)));
		p.receiveInfo(info);
		
		LMInvUtils.readItemsFromNBT(p.lastArmor, tag, "LI");
		
		LatCoreMCClient.onGuiClientAction();
		return null;
	}
}