package latmod.ftbu.net;
import ftb.lib.client.FTBLibClient;
import latmod.ftbu.api.EventLMPlayerClient;
import latmod.ftbu.util.client.LatCoreMCClient;
import latmod.ftbu.world.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.*;
import net.minecraftforge.fml.relauncher.*;

public class MessageLMPlayerUpdate extends MessageFTBU
{
	public MessageLMPlayerUpdate() { super(DATA_LONG); }
	
	public MessageLMPlayerUpdate(LMPlayerServer p, boolean self)
	{
		this();
		io.writeInt(p.playerID);
		NBTTagCompound data = new NBTTagCompound();
		p.writeToNet(data, self);
		writeTag(data);
	}
	
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(MessageContext ctx)
	{
		LMPlayerClient p = LMWorldClient.inst.getPlayer(io.readInt());
		p.readFromNet(readTag(), p.getUUID().equals(FTBLibClient.getUUID()));
		new EventLMPlayerClient.DataChanged(p).post();
		LatCoreMCClient.onGuiClientAction();
		return null;
	}
}