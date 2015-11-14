
package latmod.ftbu.net;
import ftb.lib.*;
import latmod.ftbu.api.EventLMWorldClient;
import latmod.ftbu.mod.client.FTBUClient;
import latmod.ftbu.world.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.*;
import net.minecraftforge.fml.relauncher.*;

public class MessageLMWorldJoined extends MessageFTBU
{
	public MessageLMWorldJoined() { super(DATA_LONG); }
	
	public MessageLMWorldJoined(int p)
	{
		this();
		io.writeInt(p);
		NBTTagCompound data = new NBTTagCompound();
		LMWorldServer.inst.writeDataToNet(data, p);
		writeTag(data);
	}
	
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(MessageContext ctx)
	{
		LMWorldClient.inst = new LMWorldClient(io.readInt());
		LMWorldClient.inst.readDataFromNet(readTag(), true);
		FTBLib.logger.info("Joined the server with PlayerID " + LMWorldClient.inst.getClientPlayer().playerID + " on world " + FTBWorld.client.getWorldIDS());
		FTBUClient.onWorldJoined();
		new EventLMWorldClient.Joined(LMWorldClient.inst).post();
		return null;
	}
}