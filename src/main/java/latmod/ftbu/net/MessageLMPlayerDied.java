package latmod.ftbu.net;
import latmod.ftbu.api.EventLMPlayerClient;
import latmod.ftbu.world.*;
import net.minecraftforge.fml.common.network.simpleimpl.*;
import net.minecraftforge.fml.relauncher.*;

public class MessageLMPlayerDied extends MessageFTBU
{
	public MessageLMPlayerDied() { super(DATA_SHORT); }
	
	public MessageLMPlayerDied(LMPlayer p)
	{
		this();
		io.writeInt(p.playerID);
	}
	
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(MessageContext ctx)
	{
		LMPlayerClient p = LMWorldClient.inst.getPlayer(io.readInt());
		if(p != null) new EventLMPlayerClient.PlayerDied(p).post();
		return null;
	}
}